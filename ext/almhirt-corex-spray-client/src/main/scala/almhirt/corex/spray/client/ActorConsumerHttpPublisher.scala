package almhirt.corex.spray.client

import scala.reflect.ClassTag
import scala.concurrent.duration._
import akka.actor._
import almhirt.common._
import almhirt.almvalidation.kit._
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
  circuitBreakerSettings: AlmCircuitBreaker.AlmCircuitBreakerSettings,
  circuitBreakerStateReportingInterval: Option[FiniteDuration])(implicit serializer: HttpSerializer[T], problemDeserializer: HttpDeserializer[Problem], entityTag: ClassTag[T]) extends ActorSubscriber with ActorLogging with HttpExternalConnector with RequestsWithEntity with HttpExternalPublisher with ImplicitFlowMaterializer {

  def createUri(entity: T): Uri

  private case object Processed
  private case object DisplayCircuitState

  final override val requestStrategy = ZeroRequestStrategy

  val circuitBreakerParams =
    AlmCircuitBreaker.AlmCircuitBreakerParams(
      settings = circuitBreakerSettings,
      onOpened = Some(() => self ! ActorMessages.CircuitOpened),
      onHalfOpened = Some(() => self ! ActorMessages.CircuitHalfOpened),
      onClosed = Some(() => self ! ActorMessages.CircuitClosed),
      onWarning = Some((n, max) => log.warning(s"$n failures in a row. $max will cause the circuit to open.")))

  val circuitBreaker = AlmCircuitBreaker(circuitBreakerParams, context.dispatcher, context.system.scheduler)

  private case object Start
  def receiveCircuitClosed: Receive = {
    case Start =>
      autoConnectTo.foreach(pub => FlowFrom[Event](pub).publishTo(ActorSubscriber[Event](self)))
      request(1)

    case ActorSubscriberMessage.OnNext(element) ⇒
      element.castTo[T].fold(
        fail ⇒ log.warning(s"Received unprocessable item $element"),
        typedElem ⇒ {
          circuitBreaker.fused(publishOverWire(typedElem)).onFailure(prob ⇒
            log.error(s"Failed to transmit an element over the wire:\n$prob"))
          self ! Processed
        })

    case Processed ⇒
      request(1)

    case ActorMessages.CircuitOpened =>
      log.warning("Circuit state chaged to opened")
      context.become(receiveCircuitOpen)
      self ! DisplayCircuitState

    case DisplayCircuitState =>
      if (log.isInfoEnabled)
        log.info(circuitBreaker.state.toString)
  }

  def receiveCircuitOpen: Receive = {
    case ActorSubscriberMessage.OnNext(element) ⇒
      request(1)

    case Processed ⇒
      request(1)

    case ActorMessages.CircuitClosed =>
      if (log.isInfoEnabled)
        log.info("Circuit state chaged to  closed")
      context.become(receiveCircuitClosed)
      self ! DisplayCircuitState

    case ActorMessages.CircuitHalfOpened =>
      if (log.isInfoEnabled)
        log.info("Circuit state chaged to half opend")
      context.become(receiveCircuitClosed)
      self ! DisplayCircuitState

    case DisplayCircuitState =>
      if (log.isInfoEnabled) {
        log.info(circuitBreaker.state.toString)
        circuitBreakerStateReportingInterval.foreach(interval =>
          context.system.scheduler.scheduleOnce(interval, self, DisplayCircuitState))
      }
  }

  def receive: Receive = receiveCircuitClosed

  private def publishOverWire(entity: T): AlmFuture[(T, FiniteDuration)] = {
    val settings = EntityRequestSettings(createUri(entity), contentMediaType, Seq.empty, method, acceptAsSuccess)
    publishToExternalEndpoint(entity, settings)(serializer, problemDeserializer)
  }

  override def preStart() {
    super.preStart()
    self ! Start
  }

}