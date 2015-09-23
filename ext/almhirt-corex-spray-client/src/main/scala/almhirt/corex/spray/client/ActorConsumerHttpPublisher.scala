package almhirt.corex.spray.client

import scala.reflect.ClassTag
import scala.concurrent.duration._
import akka.actor._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.context._
import almhirt.akkax._
import almhirt.akkax.reporting._
import almhirt.akkax.reporting.Implicits._
import almhirt.http._
import akka.stream.actor._
import akka.stream.scaladsl._
import spray.http.StatusCode
import spray.http.MediaType
import spray.http.HttpMethod
import spray.http.Uri
import org.reactivestreams.Publisher

abstract class ActorConsumerHttpPublisher[T](
    autoConnectTo: Option[Publisher[Event]],
    acceptAsSuccess: Set[StatusCode],
    contentMediaType: MediaType,
    method: HttpMethod,
    circuitControlSettings: CircuitControlSettings,
    circuitStateReportingInterval: Option[FiniteDuration])(implicit serializer: HttpSerializer[T], problemDeserializer: HttpDeserializer[Problem], entityTag: ClassTag[T]) extends ActorSubscriber with ActorLogging with AlmActor with AlmActorLogging with HttpExternalConnector with RequestsWithEntity with HttpExternalPublisher with ControllableActor with StatusReportingActor {

  implicit def implicitFlowMaterializer = akka.stream.ActorMaterializer()(this.context)

  override val componentControl = LocalComponentControl(self, ActorMessages.ComponentControlActions.none, Some(logWarning))

  def createUri(entity: T): Uri

  private final case class Processed(lastProblem: Option[Problem])

  final override val requestStrategy = ZeroRequestStrategy

  val circuitBreaker = AlmCircuitBreaker(circuitControlSettings, almhirtContext.futuresContext, context.system.scheduler)

  def onFailure(item: T, problem: Problem): Unit = Unit
  def onReportStatus(baseReport: StatusReport): AlmValidation[StatusReport]
  def filter(item: T): Boolean = true

  case object IncreaseFailedRequests
  case object IncreaseSuccessfulRequests

  private var numReceivedEvents = 0L
  private var numFilteredEvents = 0L
  private var numFailedRequests = 0L
  private var numsuccessfulRequests = 0L
  private var numMistypedInput = 0L

  private case object Start
  def receiveCircuitClosed: Receive = running() {
    reportsStatus(onReportRequested = createStatusReport) {
      case Start ⇒
        autoConnectTo.foreach(pub ⇒ Source[Event](pub).to(Sink(ActorSubscriber[Event](self))).run())
        request(1)

      case ActorSubscriberMessage.OnNext(element) ⇒
        numReceivedEvents = numReceivedEvents + 1L
        element.castTo[T].fold(
          fail ⇒ {
            logWarning(s"Received unprocessable item $element")
            numMistypedInput = numMistypedInput + 1L
            self ! Processed(None)
          },
          typedElem ⇒ {
            if (filter(typedElem)) {
              numFilteredEvents = numFilteredEvents + 1l
              circuitBreaker.fused(publishOverWire(typedElem)).onComplete(
                problem ⇒ {
                  self ! Processed(Some(problem))
                  self ! IncreaseFailedRequests
                  onFailure(typedElem, problem)
                },
                _ ⇒ {
                  self ! Processed(None)
                  self ! IncreaseSuccessfulRequests
                })
            } else {
              self ! Processed(None)
            }
          })

      case Processed(currentProblem) ⇒
        handleProcessed(currentProblem)

      case ActorMessages.CircuitOpened ⇒
        context.become(receiveCircuitOpen)

      case m: ActorMessages.CircuitAllWillFail ⇒
        context.become(receiveCircuitOpen)

      case IncreaseFailedRequests ⇒
        this.numFailedRequests = this.numFailedRequests + 1L

      case IncreaseSuccessfulRequests ⇒
        this.numsuccessfulRequests = this.numsuccessfulRequests + 1L
    }
  }

  def receiveCircuitOpen: Receive = error(CircuitOpenProblem("The circuit breaker is open.")) {
    reportsStatus(onReportRequested = createStatusReport) {
      case ActorSubscriberMessage.OnNext(element) ⇒
        element.castTo[T].fold(
          fail ⇒ logWarning(s"Received unprocessable item $element"),
          typedElem ⇒ onFailure(typedElem, CircuitOpenProblem()))
        request(1)

      case Processed(currentProblem) ⇒
        handleProcessed(currentProblem)

      case m: ActorMessages.CircuitNotAllWillFail ⇒
        context.become(receiveCircuitClosed)

      case IncreaseFailedRequests ⇒
        this.numFailedRequests = this.numFailedRequests + 1L

      case IncreaseSuccessfulRequests ⇒
        this.numsuccessfulRequests = this.numsuccessfulRequests + 1L
    }
  }

  def receive: Receive = receiveCircuitClosed

  def createStatusReport(options: StatusReportOptions): AlmValidation[StatusReport] = {
    val baseReport = StatusReport("HttpEventPublisher-Status").withComponentState(componentState) addMany (
      "actor-name" -> this.self.path.name,
      "number-of-successful-requests" -> numsuccessfulRequests,
      "number-of-failed-requests" -> numFailedRequests,
      "number-of-failed-requests" -> numFailedRequests,
      "number-of-mistyped-input-elemets" -> numMistypedInput,
      "number-of-received-events" -> numReceivedEvents,
      "number-of-filtered-events" -> numFilteredEvents)

    scalaz.Success(onReportStatus(baseReport).fold(
      fail ⇒ baseReport ~ ("error-occured" -> fail),
      succ ⇒ succ))
  }

  private def handleProcessed(currentProblem: Option[Problem]) {
    currentProblem match {
      case Some(CircuitOpenProblem(_)) ⇒
        ()
      case Some(otherProblem) ⇒
        log.error(s"A request failed:\n$otherProblem")
      case None ⇒
        ()
    }
    request(1)
  }

  private def publishOverWire(entity: T): AlmFuture[(T, FiniteDuration)] = {
    val settings = EntityRequestSettings(createUri(entity), contentMediaType, Seq.empty, method, acceptAsSuccess)
    publishToExternalEndpoint(entity, settings)(serializer, problemDeserializer)
  }

  override def preStart() {
    super.preStart()
    circuitBreaker.defaultActorListeners(self)
      .onWarning((n, max) ⇒ logWarning(s"$n failures in a row. $max will cause the circuit to open."))

    registerStatusReporter(description = Some("Publishes events to a consumer somewhere via HTTP"))
    registerCircuitControl(circuitBreaker)
    context.parent ! ActorMessages.ConsiderMeForReporting

    self ! Start
  }

  override def postStop() {
    deregisterCircuitControl()
    deregisterStatusReporter()
  }
}