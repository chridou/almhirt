package almhirt.corex.spray.client

import scala.reflect.ClassTag
import scala.concurrent.duration._
import akka.actor._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.context._
import almhirt.akkax._
import almhirt.http._
import akka.stream.actor._
import akka.stream.scaladsl2._
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
  circuitStateReportingInterval: Option[FiniteDuration])(implicit serializer: HttpSerializer[T], problemDeserializer: HttpDeserializer[Problem], entityTag: ClassTag[T]) extends ActorSubscriber with ActorLogging with HasAlmhirtContext with HttpExternalConnector with RequestsWithEntity with HttpExternalPublisher with ImplicitFlowMaterializer {

  def createUri(entity: T): Uri

  private final case class Processed(lastProblem: Option[Problem])
  private case object DisplayCircuitState

  final override val requestStrategy = ZeroRequestStrategy

  val circuitBreaker = AlmCircuitBreaker(circuitControlSettings, almhirtContext.futuresContext, context.system.scheduler)

  private case object Start
  def receiveCircuitClosed: Receive = {
    case Start =>
      autoConnectTo.foreach(pub => FlowFrom[Event](pub).publishTo(ActorSubscriber[Event](self)))
      request(1)

    case ActorSubscriberMessage.OnNext(element) ⇒
      element.castTo[T].fold(
        fail ⇒ {
          log.warning(s"Received unprocessable item $element")
          self ! Processed(None)
        },
        typedElem ⇒ {
          circuitBreaker.fused(publishOverWire(typedElem)).onComplete(
            prob ⇒ self ! Processed(Some(prob)),
            _ => self ! Processed(None))
        })

    case Processed(currentProblem) ⇒
      handleProcessed(currentProblem)

    case ActorMessages.CircuitOpened =>
      context.become(receiveCircuitOpen)
      self ! DisplayCircuitState

    case m: ActorMessages.CircuitAllWillFail =>
      context.become(receiveCircuitOpen)
      self ! DisplayCircuitState

    case DisplayCircuitState =>
      if (log.isInfoEnabled)
        circuitBreaker.state.onSuccess(s => log.info(s"Circuit state: $s"))
  }

  def receiveCircuitOpen: Receive = {
    case ActorSubscriberMessage.OnNext(element) ⇒
      request(1)

    case Processed(currentProblem) ⇒
      handleProcessed(currentProblem)

    case m: ActorMessages.CircuitNotAllWillFail =>
      context.become(receiveCircuitClosed)
      self ! DisplayCircuitState

    case DisplayCircuitState =>
      if (log.isInfoEnabled) {
        circuitBreaker.state.onSuccess(s => log.info(s"Circuit state: $s"))
        circuitStateReportingInterval.foreach(interval =>
          context.system.scheduler.scheduleOnce(interval, self, DisplayCircuitState))
      }
  }

  def receive: Receive = receiveCircuitClosed

  private def handleProcessed(currentProblem: Option[Problem]) {
    currentProblem match {
      case Some(CircuitOpenProblem(_)) =>
        ()
      case Some(otherProblem) =>
        log.error(s"A request failed:\n$otherProblem")
      case None =>
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
      .onWarning((n, max) => log.warning(s"$n failures in a row. $max will cause the circuit to open."))

    context.actorSelection(almhirtContext.localActorPaths.herder) ! almhirt.herder.HerderMessage.RegisterCircuitControl(self, circuitBreaker)

    self ! Start
  }

  override def postStop() {
    context.actorSelection(almhirtContext.localActorPaths.herder) ! almhirt.herder.HerderMessage.DeregisterCircuitControl(self)
  }
}