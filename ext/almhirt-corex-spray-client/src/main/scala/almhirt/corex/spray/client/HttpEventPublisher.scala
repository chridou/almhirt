package almhirt.corex.spray.client

import scala.reflect.ClassTag
import scala.concurrent.duration.FiniteDuration
import akka.actor._
import almhirt.common._
import almhirt.http._
import almhirt.configuration._
import almhirt.almvalidation.kit._
import spray.http._
import spray.client.pipelining._
import org.reactivestreams.Subscriber
import akka.stream.actor._
import org.reactivestreams.Publisher
import com.typesafe.config.Config

object HttpEventPublisher {
  def propsRaw(
    endpointUri: String,
    method: HttpMethod,
    contentMediaType: MediaType,
    addEventId: Boolean)(implicit serializer: HttpSerializer[Event], problemDeserializer: HttpDeserializer[Problem], autoConnectTo: Option[Publisher[Event]], executionContexts: HasExecutionContexts): Props =
    Props(new HttpEventPublisherImpl(endpointUri, addEventId, method, contentMediaType, autoConnectTo))

  def props(config: Config, httpEventPublisherName: String)(implicit serializer: HttpSerializer[Event], problemDeserializer: HttpDeserializer[Problem], executionContexts: HasExecutionContexts): AlmValidation[Props] = {
    implicit val extr = almhirt.httpx.spray.HttpMethodConfigExtractor
    val path = s"almhirt.components.event-publishers.http-event-publishers.$httpEventPublisherName"
    for {
      section <- config.v[com.typesafe.config.Config](path)
      enabled <- section.v[Boolean]("enabled")
      endpointUri <- section.v[String]("endpoint-uri")
      method <- section.v[HttpMethod]("method")
      contentMediaTypeStr <- section.v[String]("content-media-type")
      mediaType <- inTryCatch { MediaType.custom(contentMediaTypeStr) }
      addEventId <- section.v[Boolean]("add-event-id")
    } yield propsRaw(endpointUri, method, mediaType, addEventId)
  }

  def apply(eventPublischer: ActorRef): Subscriber[Event] =
    ActorSubscriber[Event](eventPublischer)
}

private[almhirt] class HttpEventPublisherImpl(
  endpointUri: String,
  addEventId: Boolean,
  override val method: HttpMethod,
  override val contentMediaType: MediaType,
  autoConnectTo: Option[Publisher[Event]])(implicit override val serializer: HttpSerializer[Event], override val problemDeserializer: HttpDeserializer[Problem], executionContexts: HasExecutionContexts) extends ActorConsumerHttpPublisher[Event] {

  override val acceptAsSuccess: Set[StatusCode] = Set(StatusCodes.OK, StatusCodes.Accepted)
  override val entityTag = implicitly[ClassTag[Event]]

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