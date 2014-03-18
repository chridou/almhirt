package almhirt.httpx.play22.client

import scala.concurrent.duration.FiniteDuration
import akka.actor._
import almhirt.common._
import almhirt.serialization.CanDeserializeFromWire
import almhirt.serialization.CanSerializeToWire
import almhirt.http.AlmMediaType

abstract class PlayWsSingleTypeHttpPublisher[T](implicit serializer: CanSerializeToWire[T], problemDeserializer: CanDeserializeFromWire[Problem]) extends PlayWsHttpExternalConnector with PlayWsRequestsWithEntity with PlayWsHttpExternalPublisher {

  def acceptAsSuccess: Set[Int]
  def contentMediaType: AlmMediaType

  def method: HttpMethod
  def createUri(entity: T): String
  
  def publishOverWire(entity: T): AlmFuture[(T, FiniteDuration)] = {
    val settings = EntityRequestSettings(createUri(entity), contentMediaType, Seq.empty, method, acceptAsSuccess)
    publishToExternalEndpoint(entity, settings)(serializer, problemDeserializer)
  }
}

abstract class PlayWsSingleTypeHttpQuery[U](implicit deserializer: CanDeserializeFromWire[U], problemDeserializer: CanDeserializeFromWire[Problem]) extends PlayWsHttpExternalConnector with PlayWsAwaitingEntityResponse with PlayWsHttpExternalQuery {
  type ResourceId

  def acceptAsSuccess: Set[Int]
  def acceptMediaTypes: Seq[AlmMediaType]
  def method: HttpMethod
  def createUri(id: ResourceId): String
  
  def queryOverWire(id: ResourceId): AlmFuture[(U, FiniteDuration)] = {
    val settings = BasicRequestSettings(createUri(id), acceptMediaTypes, method, acceptAsSuccess)
    externalQuery(settings)(deserializer, problemDeserializer)
  }
}

abstract class PlayWsSingleTypeHttpConversation[T, U](implicit serializer: CanSerializeToWire[T], deserializer: CanDeserializeFromWire[T], problemDeserializer: CanDeserializeFromWire[Problem]) extends PlayWsHttpExternalConnector with PlayWsRequestsWithEntity with PlayWsAwaitingEntityResponse with PlayWsHttpExternalConversation {
  def acceptAsSuccess: Set[Int]
  def contentMediaType: AlmMediaType
  def acceptMediaTypes: Seq[AlmMediaType]
  def method: HttpMethod
  def createUri(entity: T): String


  def conversationOverWire(entity: T): AlmFuture[(T, FiniteDuration)] = {
    val settings = EntityRequestSettings(createUri(entity), contentMediaType, acceptMediaTypes, method, acceptAsSuccess)
    conversationWithExternalEndpoint(entity, settings)(serializer, deserializer, problemDeserializer)
  }

}