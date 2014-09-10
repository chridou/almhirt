package almhirt.httpx.spray.client

import scala.reflect.ClassTag
import scala.concurrent.duration.FiniteDuration
import akka.actor._
import almhirt.common._
import almhirt.http._
import spray.http._
import spray.client.pipelining._
import akka.stream.actor._
import org.reactivestreams.Subscriber
import akka.stream.FlowMaterializer

object HttpEventPublisher {
  def props(endpointUri: String,
    contentMediaType: MediaType,
    addEventId: Boolean)(implicit serializer: HttpSerializer[Event], problemDeserializer: HttpDeserializer[Problem], executionContexts: HasExecutionContexts): Props =
    Props(new HttpEventPublisherImpl(endpointUri, addEventId, contentMediaType))

  def apply(eventPublischer: ActorRef): Subscriber[Event] =
    ActorSubscriber[Event](eventPublischer)

  def withoutAggregateRootEvents(eventPublischer: ActorRef)(implicit materializer: FlowMaterializer): Subscriber[Event] = {
    import akka.stream.scaladsl.Duct
    val duct = Duct[Event].filter(!_.isInstanceOf[AggregateRootEvent])
    val (subscriber, publisher) = duct.build
    val eventSubscriber = HttpEventPublisher(eventPublischer)
    publisher.subscribe(eventSubscriber)
    subscriber
  }
}

private[almhirt] class HttpEventPublisherImpl(
  endpointUri: String,
  addEventId: Boolean,
  override val contentMediaType: MediaType)(implicit override val serializer: HttpSerializer[Event], override val problemDeserializer: HttpDeserializer[Problem], executionContexts: HasExecutionContexts) extends ActorConsumerHttpPublisher[Event] {

  override val method: HttpMethod = HttpMethods.PUT
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