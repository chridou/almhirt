package almhirt.httpx.play21.client

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

  def publishOverWire(entity: T, requestParams: (String, String)*): AlmFuture[(T, FiniteDuration)] = {
    val settings = EntityRequestSettings(createUri(entity), contentMediaType, Seq.empty, method, acceptAsSuccess)
    publishToExternalEndpoint(entity, settings, requestParams: _*)(serializer, problemDeserializer)
  }
}

abstract class PlayWsSingleTypeHttpQuery[U](implicit deserializer: CanDeserializeFromWire[U], problemDeserializer: CanDeserializeFromWire[Problem]) extends PlayWsHttpExternalConnector with PlayWsAwaitingEntityResponse with PlayWsHttpExternalQuery {
  type ResourceId

  def acceptAsSuccess: Set[Int]
  def acceptMediaTypes: Seq[AlmMediaType]
  def method: HttpMethod
  def createUri(id: ResourceId): String

  def queryOverWire(id: ResourceId, requestParams: (String, String)*): AlmFuture[(U, FiniteDuration)] = {
    val settings = BasicRequestSettings(createUri(id), acceptMediaTypes, method, acceptAsSuccess)
    externalQuery(settings, requestParams: _*)(deserializer, problemDeserializer)
  }
}

abstract class PlayWsSingleTypeStaticHttpQuery[U](implicit deserializer: CanDeserializeFromWire[U], problemDeserializer: CanDeserializeFromWire[Problem]) extends PlayWsHttpExternalConnector with PlayWsAwaitingEntityResponse with PlayWsHttpExternalQuery {
  def acceptAsSuccess: Set[Int]
  def acceptMediaTypes: Seq[AlmMediaType]
  def method: HttpMethod
  def uri: String

  def queryOverWire(requestParams: (String, String)*): AlmFuture[(U, FiniteDuration)] = {
    val settings = BasicRequestSettings(uri, acceptMediaTypes, method, acceptAsSuccess)
    externalQuery(settings, requestParams: _*)(deserializer, problemDeserializer)
  }
}


abstract class PlayWsSingleTypeHttpConversation[T, U](implicit serializer: CanSerializeToWire[T], deserializer: CanDeserializeFromWire[T], problemDeserializer: CanDeserializeFromWire[Problem]) extends PlayWsHttpExternalConnector with PlayWsRequestsWithEntity with PlayWsAwaitingEntityResponse with PlayWsHttpExternalConversation {
  def acceptAsSuccess: Set[Int]
  def contentMediaType: AlmMediaType
  def acceptMediaTypes: Seq[AlmMediaType]
  def method: HttpMethod
  def createUri(entity: T): String

  def conversationOverWire(entity: T, requestParams: (String, String)*): AlmFuture[(T, FiniteDuration)] = {
    val settings = EntityRequestSettings(createUri(entity), contentMediaType, acceptMediaTypes, method, acceptAsSuccess)
    conversationWithExternalEndpoint(entity, settings, requestParams: _*)(serializer, deserializer, problemDeserializer)
  }
}