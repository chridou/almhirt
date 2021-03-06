package almhirt.corex.spray.client

import scala.concurrent.duration._
import scalaz.Validation.FlatMap._
import akka.actor._
import almhirt.common._
import almhirt.akkax._
import almhirt.context.AlmhirtContext
import almhirt.http._
import spray.http._
import spray.client.pipelining._
import almhirt.akkax.reporting._
import almhirt.akkax.reporting.Implicits._
import almhirt.serialization.SerializationParams
import almhirt.components.EventPublisher
import almhirt.akkax.events.FailureReported

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
          Uri(s"""$uriPrefix/$typeName/${event.eventId}?op_type=create&timestamp=${event.timestamp}&ttl=${timeToLive.toMinutes}m""")
        case None ⇒
          Uri(s"""$uriPrefix/$typeName/${event.eventId}?op_type=create&timestamp=${event.timestamp}""")
      }
    }

  }

  def propsRaw(
    elSettings: ElasticSearchEventPublisher.ElasticSearchSettings,
    maxParallel: Int,
    circuitControlSettings: CircuitControlSettings,
    serializer: HttpSerializer[Event])(implicit almhirtContext: AlmhirtContext): Props =
    Props(new ElasticSearchEventPublisherActor(elSettings, maxParallel, circuitControlSettings, serializer))

  def props(configPath: String)(serializer: HttpSerializer[Event])(implicit almhirtContext: AlmhirtContext): AlmValidation[Props] = {
    import com.typesafe.config.Config
    import almhirt.configuration._
    for {
      section ← almhirtContext.config.v[Config](configPath)
      maxParallel ← section.v[Int]("max-parallel")
      circuitControlSettings ← section.v[CircuitControlSettings]("circuit-control")
      elSettingsSection ← section.v[Config]("elastic-search")
      elSettings ← for {
        host ← elSettingsSection.v[String]("host")
        index ← elSettingsSection.v[String]("index")
        fixedTypeName ← elSettingsSection.magicOption[String]("fixed-type-name")
        ttl ← elSettingsSection.magicOption[FiniteDuration]("ttl")
      } yield ElasticSearchSettings(
        host = host,
        index = index,
        fixedTypeName = fixedTypeName,
        ttl = ttl)
    } yield propsRaw(elSettings, maxParallel, circuitControlSettings, serializer)
  }

  def componentFactory(configPath: String)(serializer: HttpSerializer[Event])(implicit ctx: AlmhirtContext): AlmValidation[ComponentFactory] =
    props(configPath)(serializer).map(props ⇒ ComponentFactory(props, actorname))

  val actorname = "elastic-search-event-publisher"

  val defaultConfigPath = "almhirt.components.misc.event-publisher-hub.event-publishers.elastic-search-event-publisher"

}

abstract class ElasticSearchEventPublisherFactory(configPath: String, serializer: HttpSerializer[Event]) extends almhirt.components.EventPublisherFactory {
  def this(serializer: HttpSerializer[Event]) = this(ElasticSearchEventPublisher.defaultConfigPath, serializer)
  override def create(implicit almhirtContext: AlmhirtContext): AlmValidation[ComponentFactory] =
    ElasticSearchEventPublisher.componentFactory(configPath)(serializer)
}

private[client] class ElasticSearchEventPublisherActor(
    elSettings: ElasticSearchEventPublisher.ElasticSearchSettings,
    maxParallel: Int,
    circuitControlSettings: CircuitControlSettings,
    serializer: HttpSerializer[Event])(implicit val almhirtContext: AlmhirtContext) extends AlmActor() with AlmActorSupport with AlmActorLogging with ControllableActor with StatusReportingActor with HttpExternalConnector with RequestsWithEntity with HttpExternalPublisher {

  private object DefaultComponentIdProvider extends ActorComponentIdProvider {
    def componentId = ComponentId(AppName("almhirt"), ComponentName(self.path.name))
  }

  implicit override val executionContext = almhirtContext.futuresContext
  override val serializationExecutionContext = almhirtContext.futuresContext

  override val pipeline = (sendReceive)

  val circuitBreaker = AlmCircuitBreaker(circuitControlSettings, almhirtContext.futuresContext, context.system.scheduler)

  private val problemDeserializer = new HttpDeserializer[Problem] {
    def deserialize(mediaType: AlmMediaType, what: AlmHttpBody)(implicit params: SerializationParams = SerializationParams.empty): AlmValidation[Problem] =
      scalaz.Failure(SerializationProblem("Cannot Deserialize"))
  }

  implicit override val componentNameProvider: ActorComponentIdProvider = DefaultComponentIdProvider
  override def componentControl = LocalComponentControl(self, ComponentControlActions.none, Some(logWarning))

  val myGlobalComponentId = GlobalComponentId(almhirtContext.localNodeName, this.componentNameProvider.componentId)

  val numEventsNotDispatched = new java.util.concurrent.atomic.AtomicLong(0L)
  val numEventsDispatched = new java.util.concurrent.atomic.AtomicLong(0L)
  val eventsInFlight = new java.util.concurrent.atomic.AtomicInteger(0)
  var eventsReceived = 0L
  var ownEventsOmmitted = 0L

  def receiveRunning: Receive = running() {
    reportsStatus(onReportRequested = createStatusReport) {
      case EventPublisher.PublishEvent(event) ⇒
        val toDispatch = event match {
          case e: FailureReported if e.origin == myGlobalComponentId ⇒
            ownEventsOmmitted = ownEventsOmmitted + 1L
            logInfo("I do not record my own published events")
            sender() ! EventPublisher.EventNotPublished(event, UnspecifiedProblem("That was my own event!"))
            None
          case x ⇒ Some(x)
        }

        toDispatch.foreach { event ⇒
          eventsReceived = eventsReceived + 1L
          if (eventsInFlight.get < maxParallel) {
            publishEvent(event, elSettings).mapOrRecoverThenPipeTo(
              map = _ ⇒ EventPublisher.EventPublished(event),
              recover = EventPublisher.EventNotPublished(event, _))(sender())
          } else {
            sender() ! EventPublisher.EventNotPublished(event, ServiceBusyProblem("Too many events in flight."))
          }
        }
      case EventPublisher.FireEvent(event) ⇒
        val toDispatch = event match {
          case e: FailureReported if e.origin == myGlobalComponentId ⇒
            ownEventsOmmitted = ownEventsOmmitted + 1L
            logInfo("I do not record my own fired events")
            None
          case x ⇒ Some(x)
        }

        toDispatch.foreach { event ⇒
          eventsReceived = eventsReceived + 1L
          if (eventsInFlight.get < maxParallel) {
            publishEvent(event, elSettings)
          }
        }
    }
  }

  override def receive: Receive = receiveRunning

  def publishEvent(event: Event, settings: ElasticSearchEventPublisher.ElasticSearchSettings): AlmFuture[(Event, FiniteDuration)] = {
    eventsInFlight.incrementAndGet()
    circuitBreaker.fused(publishOverWire(event, settings)).onComplete(
      fail ⇒ {
        numEventsNotDispatched.incrementAndGet()
        eventsInFlight.decrementAndGet()
        reportMinorFailure(fail)
        logError(s"Could not dispatch event $event: ${fail.message}")
      },
      succ ⇒ {
        eventsInFlight.decrementAndGet()
        numEventsDispatched.incrementAndGet()
      })
  }

  val acceptAsSuccess: Set[StatusCode] = Set(StatusCodes.Accepted, StatusCodes.Created)
  val contentMediaType = MediaTypes.`application/json`
  val method = HttpMethods.PUT

  private def publishOverWire(entity: Event, settings: ElasticSearchEventPublisher.ElasticSearchSettings): AlmFuture[(Event, FiniteDuration)] = {
    val requestSettings = EntityRequestSettings(settings.uri(entity), contentMediaType, Seq.empty, method, acceptAsSuccess)
    publishToExternalEndpoint(entity, requestSettings)(serializer, problemDeserializer)
  }

  private def createStatusReport(options: StatusReportOptions): AlmValidation[StatusReport] = {
    val elSettingsRep: StatusReport =
      StatusReport() addMany (
        "host" -> elSettings.host,
        "index" -> elSettings.index,
        "fixed-type-name" -> elSettings.fixedTypeName,
        "ttl" -> elSettings.ttl)
    val baseReport = StatusReport("ElasticSearchEventPublisher-Report").withComponentState(componentState) addMany (
      "number-of-events-received" -> eventsReceived,
      "number-of-events-dispatched" -> numEventsDispatched.get,
      "number-of-events-not-dispatched" -> numEventsNotDispatched.get,
      "number-of-events-in-flight" -> eventsInFlight.get,
      "number-of-own-events-ommitted" -> ownEventsOmmitted,
      "elastic-search-settings" -> elSettingsRep,
      "max-parallel" -> maxParallel)

    scalaz.Success(baseReport)
  }

  override def preStart() {
    super.preStart()
    registerCircuitControl(circuitBreaker)
    registerComponentControl()
    context.parent ! ActorMessages.ConsiderMeForReporting
    circuitBreaker.defaultActorListeners(self)
      .onWarning((n, max) ⇒ logWarning(s"$n failures in a row. $max will cause the circuit to open."))

    registerStatusReporter(description = Some("Logs events into an ElasticSearch instance."))
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

