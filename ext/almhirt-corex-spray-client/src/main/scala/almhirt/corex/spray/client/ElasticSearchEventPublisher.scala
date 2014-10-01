package almhirt.corex.spray.client

import scala.reflect.ClassTag
import scala.concurrent.duration.FiniteDuration
import scalaz.syntax.validation._
import akka.actor._
import almhirt.common._
import almhirt.http._
import almhirt.configuration._
import almhirt.context.AlmhirtContext
import spray.http._
import spray.client.pipelining._
import org.reactivestreams.Subscriber
import akka.stream.actor._
import org.reactivestreams.{ Subscriber, Publisher }
import almhirt.streaming.ActorDevNullSubscriberWithAutoSubscribe
import com.typesafe.config.Config

object ElasticSearchEventPublisher {
  def propsRaw(
    host: String,
    index: String,
    fixedTypeName: Option[String],
    ttl: FiniteDuration,
    autoConnectTo: Option[Publisher[Event]])(implicit serializer: HttpSerializer[Event], problemDeserializer: HttpDeserializer[Problem], executionContexts: HasExecutionContexts): Props =
    Props(new ElasticSearchEventPublisherImpl(host, index, fixedTypeName, ttl, autoConnectTo))

  def props(elConfigName: Option[String] = None)(implicit ctx: AlmhirtContext, serializer: HttpSerializer[Event], problemDeserializer: HttpDeserializer[Problem]): AlmValidation[Props] = {
    val path = "almhirt.components.event-publishers.http-event-publishers.elastic-search-event-publisher" + elConfigName.map("." + _).getOrElse("")
    for {
      section <- ctx.config.v[com.typesafe.config.Config](path)
      enabled <- section.v[Boolean]("enabled")
      autoConnect <- section.v[Boolean]("auto-connect")
      res <- if (enabled) {
        for {
          host <- section.v[String]("host")
          index <- section.v[String]("index")
          fixedTypeName <- section.magicOption[String]("index")
          ttl <- section.v[FiniteDuration]("ttl")
        } yield propsRaw(host, index, fixedTypeName, ttl, if (autoConnect) Some(ctx.eventStream) else None)
      } else {
        ActorDevNullSubscriberWithAutoSubscribe.props[Event](1, if (autoConnect) Some(ctx.eventStream) else None).success
      }
    } yield res
  }

  def apply(elasticSearchEventPublischer: ActorRef): Subscriber[Event] =
    ActorSubscriber[Event](elasticSearchEventPublischer)

  val actorname = "elastic-search-event-publisher"
}

private[almhirt] class ElasticSearchEventPublisherImpl(
  host: String,
  index: String,
  fixedTypeName: Option[String],
  ttl: FiniteDuration,
  override val autoConnectTo: Option[Publisher[Event]])(implicit override val serializer: HttpSerializer[Event], override val problemDeserializer: HttpDeserializer[Problem], executionContexts: HasExecutionContexts) extends ActorConsumerHttpPublisher[Event] {

  val uriprefix = s"""http://$host/$index"""

  override val contentMediaType: MediaType = MediaTypes.`application/json`
  override val method: HttpMethod = HttpMethods.PUT
  override val acceptAsSuccess: Set[StatusCode] = Set(StatusCodes.Accepted)
  override val entityTag = implicitly[ClassTag[Event]]

  implicit override val executionContext = executionContexts.futuresContext
  override val serializationExecutionContext = executionContexts.futuresContext

  override val pipeline = (sendReceive)

  override def createUri(event: Event): Uri = {
    val typeName = fixedTypeName.getOrElse(event.getClass().getSimpleName())
    Uri(s"""$uriprefix/$typeName/${event.eventId}?op_type=create&ttl=${ttl.toMillis}""")
  }

}