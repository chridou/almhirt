package almhirt.httpx.spray.client

import scala.concurrent.duration.FiniteDuration
import akka.actor._
import almhirt.common._
import almhirt.serialization.CanDeserializeFromWire
import almhirt.serialization.CanSerializeToWire
import spray.http._
import spray.client.pipelining._

abstract class SingleTypeHttpPublisher[T](implicit serializer: CanSerializeToWire[T], problemDeserializer: CanDeserializeFromWire[Problem]) extends Actor with ActorLogging with HttpExternalConnector with RequestsWithEntity with HttpExternalPublisher {

  override val pipeline = (sendReceive)

  def acceptAsSuccess: Set[StatusCode]
  def contentMediaType: MediaType

  def method: HttpMethod
  def createUri(entity: T): Uri
  
  def publishOverWire(entity: T): AlmFuture[(T, FiniteDuration)] = {
    val settings = EntityRequestSettings(createUri(entity), contentMediaType, Seq.empty, method, acceptAsSuccess)
    publishToExternalEndpoint(entity, settings)(serializer, problemDeserializer)
  }
}

abstract class SingleTypeHttpQuery[U](implicit deserializer: CanDeserializeFromWire[U], problemDeserializer: CanDeserializeFromWire[Problem]) extends Actor with ActorLogging with HttpExternalConnector with AwaitingEntityResponse with HttpExternalQuery {
  type ResourceId

  override val pipeline = (sendReceive)

  def acceptAsSuccess: Set[StatusCode]
  def acceptMediaTypes: Seq[MediaType]
  def method: HttpMethod
  def createUri(id: ResourceId): Uri
  
  def queryOverWire(id: ResourceId): AlmFuture[(U, FiniteDuration)] = {
    val settings = BasicRequestSettings(createUri(id), acceptMediaTypes, method, acceptAsSuccess)
    externalQuery(settings)(deserializer, problemDeserializer)
  }
}

abstract class SingleTypeHttpConversation[T, U](implicit serializer: CanSerializeToWire[T], deserializer: CanDeserializeFromWire[T], problemDeserializer: CanDeserializeFromWire[Problem]) extends Actor with ActorLogging with HttpExternalConnector with RequestsWithEntity with AwaitingEntityResponse with HttpExternalConversation {
  def acceptAsSuccess: Set[StatusCode]
  def contentMediaType: MediaType
  def acceptMediaTypes: Seq[MediaType]
  def method: HttpMethod
  def createUri(entity: T): Uri

  override val pipeline = (sendReceive)

  def conversationOverWire(entity: T): AlmFuture[(T, FiniteDuration)] = {
    val settings = EntityRequestSettings(createUri(entity), contentMediaType, acceptMediaTypes, method, acceptAsSuccess)
    conversationWithExternalEndpoint(entity, settings)(serializer, deserializer, problemDeserializer)
  }

}