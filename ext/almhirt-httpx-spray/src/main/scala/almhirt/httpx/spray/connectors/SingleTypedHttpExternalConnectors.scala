package almhirt.httpx.spray.connectors

import akka.actor._
import almhirt.common._
import almhirt.serialization.CanDeserializeFromWire
import almhirt.serialization.CanSerializeToWire
import spray.http.Uri
import spray.client.pipelining._
import spray.http.StatusCode
import spray.http.MediaType
import spray.http.HttpMethod
import scala.concurrent.duration.FiniteDuration

abstract class SingleTypeHttpPublisher[T]() extends Actor with ActorLogging with HttpExternalConnector with RequestsWithEntity with HttpExternalPublisher {

  override val pipeline = (sendReceive)

  def acceptAsSuccess: Set[StatusCode]
  override def problemDeserializer: CanDeserializeFromWire[Problem]
  def serializer: CanSerializeToWire[T]
  def mediaType: MediaType
  def method: HttpMethod
  def createUri(entity: T): Uri
  
  def publishOverWire(entity: T): AlmFuture[(T, FiniteDuration)] = {
    val settings = RequestSettings(createUri(entity), mediaType, method, acceptAsSuccess)
    publishToExternalEndpoint(entity, settings)(serializer)
  }
}

abstract class SingleTypeHttpQuery[U]() extends Actor with ActorLogging with HttpExternalConnector with AwaitingEntityResponse with HttpExternalQuery {
  type ResourceId

  override val pipeline = (sendReceive)

  def acceptAsSuccess: Set[StatusCode]
  override def problemDeserializer: CanDeserializeFromWire[Problem]
  def deserializer: CanDeserializeFromWire[U]
  def mediaType: MediaType
  def method: HttpMethod
  def createUri(id: ResourceId): Uri
  
  def queryOverWire(id: ResourceId): AlmFuture[(U, FiniteDuration)] = {
    val settings = RequestSettings(createUri(id), mediaType, method, acceptAsSuccess)
    externalQuery(settings)(deserializer)
  }
}

abstract class SingleTypeHttpConversation[T, U]() extends Actor with ActorLogging with HttpExternalConnector with RequestsWithEntity with AwaitingEntityResponse with HttpExternalConversation {
  def serializer: CanSerializeToWire[T]
  def deserializer: CanDeserializeFromWire[T]
  def problemDeserializer: CanDeserializeFromWire[Problem]
  def acceptAsSuccess: Set[StatusCode]
  def mediaType: MediaType
  def method: HttpMethod
  def createUri(entity: T): Uri

  override val pipeline = (sendReceive)

  def conversationOverWire(entity: T): AlmFuture[(T, FiniteDuration)] = {
    val settings = RequestSettings(createUri(entity), mediaType, method, acceptAsSuccess)
    conversationWithExternalEndpoint(entity, settings)(serializer, deserializer)
  }

}