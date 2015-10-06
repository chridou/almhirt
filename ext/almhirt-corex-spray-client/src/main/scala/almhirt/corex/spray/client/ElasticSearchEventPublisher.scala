package almhirt.corex.spray.client

import scala.concurrent.duration._
import almhirt.common._
import almhirt.akkax._
import almhirt.context.AlmhirtContext
import almhirt.http.HttpSerializer
import spray.http._
import spray.client.pipelining._
import almhirt.akkax.reporting._
import almhirt.akkax.reporting.Implicits._

object ElasticSearchEventPublisher {
  final case class ElasticSearchSettings(
      host: String,
      index: String,
      fixedTypeName: Option[String],
      ttl: Option[FiniteDuration]) {
    def uriPrefix: String = s"""http://$host/$index"""
    def uri(event: Event): Uri = {
      val typeName = fixedTypeName.getOrElse(event.getClass().getSimpleName())
      ttl match {
        case Some(timeToLive) ⇒
          Uri(s"""$uriPrefix/$typeName/${event.eventId}?op_type=create&timestamp=${event.timestamp}&ttl=${timeToLive.toMillis}""")
        case None ⇒
          Uri(s"""$uriPrefix/$typeName/${event.eventId}?op_type=create&timestamp=${event.timestamp}""")
      }
    }

  }
}

private[client] class ElasticSearchEventPublisherActor(
    elSettings: ElasticSearchEventPublisher.ElasticSearchSettings,
    maxParallel: Int,
    circuitControlSettings: CircuitControlSettings,
    serializer: HttpSerializer[Event])(implicit val almhirtContext: AlmhirtContext) extends AlmActor() with AlmActorSupport with AlmActorLogging with ControllableActor with StatusReportingActor with HttpExternalConnector with RequestsWithEntity with HttpExternalPublisher {

  private object DefaultComponentIdProvider extends ActorComponentIdProvider {
    def componentId = ComponentId(AppName("zeus-server"), ComponentName(self.path.name))
  }

  implicit override val executionContext = almhirtContext.futuresContext
  override val serializationExecutionContext = almhirtContext.futuresContext

  override val pipeline = (sendReceive)

  val circuitBreaker = AlmCircuitBreaker(circuitControlSettings, almhirtContext.futuresContext, context.system.scheduler)

  implicit override val componentNameProvider: ActorComponentIdProvider = DefaultComponentIdProvider
  override def componentControl = LocalComponentControl(self, ActorMessages.ComponentControlActions.none, Some(logWarning))

  val numEventsNotDispatched = new java.util.concurrent.atomic.AtomicLong(0L)
  val numEventsDispatched = new java.util.concurrent.atomic.AtomicLong(0L)
  val eventsInFlight = new java.util.concurrent.atomic.AtomicLong(0L)

  def receiveRunning: Receive = running() {
    reportsStatus(onReportRequested = createStatusReport) {
      case _ ⇒ ???
    }
  }

  override def receive: Receive = receiveRunning

  def publishEvent(event: Event, settings: ElasticSearchEventPublisher.ElasticSearchSettings): AlmFuture[(Event, FiniteDuration)] = {
    circuitBreaker.fused(publishOverWire(event, settings)).onComplete(
      fail ⇒ {
        numEventsNotDispatched.incrementAndGet()
        reportMinorFailure(fail)
        logError(s"Could not dispatch event $event: ${fail.message}")
      },
      succ ⇒ {
        numEventsDispatched.incrementAndGet()
      })
  }

  val acceptAsSuccess: Set[StatusCode] = Set(StatusCodes.Accepted, StatusCodes.Created)
  val contentMediaType = MediaTypes.`application/json`
  val method = HttpMethods.PUT

  private def publishOverWire(entity: Event, settings: ElasticSearchEventPublisher.ElasticSearchSettings): AlmFuture[(Event, FiniteDuration)] = {
    val requestSettings = EntityRequestSettings(settings.uri(entity), contentMediaType, Seq.empty, method, acceptAsSuccess)
    publishToExternalEndpoint(entity, requestSettings)(serializer, null)
  }

  private def createStatusReport(options: StatusReportOptions): AlmValidation[StatusReport] = {
    val elSettingsRep: StatusReport =
      StatusReport() addMany (
        "host" -> elSettings.host,
        "index" -> elSettings.index,
        "fixed-type-name" -> elSettings.fixedTypeName,
        "ttl" -> elSettings.ttl,
        "max-parallel" -> maxParallel)
    val baseReport = StatusReport("ElasticSearchEventPublisher-Report").withComponentState(componentState) addMany (
      "number-of-events-dispatched" -> numEventsDispatched.get,
      "number-of-events-not-dispatched" -> numEventsNotDispatched.get,
      "number-of-events-in-flight" -> eventsInFlight.get,
      "elastic-search-settings" -> elSettingsRep)

    scalaz.Success(baseReport)
  }

  override def preStart() {
    super.preStart()
    registerComponentControl()
    context.parent ! ActorMessages.ConsiderMeForReporting
    circuitBreaker.defaultActorListeners(self)
      .onWarning((n, max) ⇒ logWarning(s"$n failures in a row. $max will cause the circuit to open."))

    registerStatusReporter(description = Some("Logs events into an ElasticSearch instance."))
    registerCircuitControl(circuitBreaker)
    logInfo("Starting..")
  }

  override def postStop() {
    super.postStop()
    deregisterComponentControl()
    deregisterStatusReporter()
    deregisterCircuitControl()
    logWarning("Stopped")
  }
}

