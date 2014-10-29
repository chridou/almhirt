package almhirt.corex.spray.client

import scala.reflect.ClassTag
import scala.concurrent.duration.FiniteDuration
import scalaz.syntax.validation._
import scalaz.Validation.FlatMap._
import akka.actor._
import almhirt.common._
import almhirt.akkax._
import almhirt.http._
import almhirt.configuration._
import almhirt.almvalidation.kit._
import almhirt.context._
import spray.http._
import spray.client.pipelining._
import org.reactivestreams.{ Subscriber, Publisher }
import akka.stream.actor._
import almhirt.streaming.ActorDevNullSubscriberWithAutoSubscribe
import com.typesafe.config.Config

object HttpEventPublisher {
  def propsRaw(
    endpointUri: String,
    method: HttpMethod,
    contentMediaType: MediaType,
    addEventId: Boolean,
    autoConnectTo: Option[Publisher[Event]],
    circuitControlSettings: CircuitControlSettings,
    circuitStateReportingInterval: Option[FiniteDuration],
    missedEventSeverity: almhirt.problem.Severity)(implicit serializer: HttpSerializer[Event], problemDeserializer: HttpDeserializer[Problem], almhirtContext: AlmhirtContext): Props =
    Props(new HttpEventPublisherImpl(endpointUri, addEventId, method, contentMediaType, autoConnectTo, circuitControlSettings, circuitStateReportingInterval, missedEventSeverity))

  def props(httpEventPublisherName: String)(implicit ctx: AlmhirtContext, serializer: HttpSerializer[Event], problemDeserializer: HttpDeserializer[Problem]): AlmValidation[Props] = {
    implicit val extr = almhirt.httpx.spray.HttpMethodConfigExtractor
    val path = s"almhirt.components.misc.event-sink-hub.event-publishers.http-event-publishers.$httpEventPublisherName"
    for {
      section ← ctx.config.v[com.typesafe.config.Config](path)
      enabled ← section.v[Boolean]("enabled")
      autoConnect ← section.v[Boolean]("auto-connect")
      res ← if (enabled) {
        for {
          endpointUri ← section.v[String]("endpoint-uri")
          method ← section.v[HttpMethod]("method")
          contentMediaTypeStr ← section.v[String]("content-media-type")
          mediaType ← inTryCatch { MediaType.custom(contentMediaTypeStr) }
          addEventId ← section.v[Boolean]("add-event-id")
          missedEventSeverity ← section.v[almhirt.problem.Severity]("missed-event-severity")
          circuitControlSettings ← section.v[CircuitControlSettings]("circuit-control")
          circuitStateReportingInterval ← section.magicOption[FiniteDuration]("circuit-state-reporting-interval")
        } yield propsRaw(endpointUri, method, mediaType, addEventId, if (autoConnect) Some(ctx.eventStream) else None, circuitControlSettings, circuitStateReportingInterval, missedEventSeverity)
      } else {
        ActorDevNullSubscriberWithAutoSubscribe.props[Event](1, if (autoConnect) Some(ctx.eventStream) else None).success
      }
    } yield res
  }

  def apply(eventPublischer: ActorRef): Subscriber[Event] =
    ActorSubscriber[Event](eventPublischer)
}

private[almhirt] class HttpEventPublisherImpl(
  endpointUri: String,
  addEventId: Boolean,
  method: HttpMethod,
  contentMediaType: MediaType,
  autoConnectTo: Option[Publisher[Event]],
  circuitControlSettings: CircuitControlSettings,
  circuitStateReportingInterval: Option[FiniteDuration],
  missedEventSeverity: almhirt.problem.Severity)(implicit serializer: HttpSerializer[Event], problemDeserializer: HttpDeserializer[Problem], executionContexts: HasExecutionContexts, override val almhirtContext: AlmhirtContext)
  extends ActorConsumerHttpPublisher[Event](autoConnectTo, Set(StatusCodes.OK, StatusCodes.Accepted), contentMediaType, method, circuitControlSettings, circuitStateReportingInterval)(serializer, problemDeserializer, implicitly[ClassTag[Event]]) {

  override def onFailure(item: Event, problem: Problem): Unit = {
    reportMissedEvent(item, missedEventSeverity, problem)
    reportFailure(problem, missedEventSeverity)
  }
  
  override def filter(item: Event) = !item.header.isLocal

  implicit override val executionContext = executionContexts.futuresContext
  override val serializationExecutionContext = executionContexts.futuresContext

  override val pipeline = (sendReceive)

  override def createUri(event: Event): Uri = {
    if (addEventId)
      Uri(s"""$endpointUri/${event.eventId.value}""")
    else
      Uri(endpointUri)
  }
}