package almhirt.httpx.spray.client

import scala.reflect.ClassTag
import scala.concurrent.duration.FiniteDuration
import akka.actor._
import almhirt.common._
import almhirt.almvalidation.kit._
import akka.stream.actor._
import almhirt.http._
import spray.http.StatusCode
import spray.http.MediaType
import spray.http.HttpMethod
import spray.http.Uri

trait ActorConsumerHttpPublisher[T] extends ActorSubscriber with ActorLogging with HttpExternalConnector with RequestsWithEntity with HttpExternalPublisher {
  implicit def entityTag: ClassTag[T]
  implicit def serializer: HttpSerializer[T]
  implicit def problemDeserializer: HttpDeserializer[Problem]
  def acceptAsSuccess: Set[StatusCode]
  def contentMediaType: MediaType
  def method: HttpMethod
  def createUri(entity: T): Uri

  private case object Processed
  
  final override val requestStrategy = ZeroRequestStrategy

  final override def receive: Receive = {
    case ActorSubscriberMessage.OnNext(element) ⇒
      element.castTo[T].fold(
        fail ⇒ log.warning(s"Received unprocessable item $element"),
        typedElem ⇒ {
          publishOverWire(typedElem).onComplete { res ⇒
            res match {
              case scalaz.Success(_) ⇒
                () // We are some kind of "Fire&Forget"
              case scalaz.Failure(prob) ⇒
                log.error(s"Failed to transmit an element over the wire:\n$prob")
            }
            self ! Processed
          }
        })
        
    case Processed ⇒
      request(1)
  }

  private def publishOverWire(entity: T): AlmFuture[(T, FiniteDuration)] = {
    val settings = EntityRequestSettings(createUri(entity), contentMediaType, Seq.empty, method, acceptAsSuccess)
    publishToExternalEndpoint(entity, settings)(serializer, problemDeserializer)
  }
  
  override def preStart() {
    super.preStart()
    request(1)
  }

}