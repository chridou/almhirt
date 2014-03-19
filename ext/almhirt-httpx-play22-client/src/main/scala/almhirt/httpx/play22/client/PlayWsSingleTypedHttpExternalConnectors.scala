package almhirt.httpx.play22.client

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration
import akka.actor._
import almhirt.common._
import almhirt.serialization.CanDeserializeFromWire
import almhirt.serialization.CanSerializeToWire
import almhirt.http.AlmMediaType

abstract class PlayWsSingleTypeHttpPublisher[T](theSerializationExecutionContext: Option[ExecutionContext])(implicit serializer: CanSerializeToWire[T], problemDeserializer: CanDeserializeFromWire[Problem], theExecutionContext: ExecutionContext) extends PlayWsHttpExternalConnector with PlayWsRequestsWithEntity with PlayWsHttpExternalPublisher {
  implicit override val executionContext: ExecutionContext = theExecutionContext
  override val serializationExecutionContext: ExecutionContext = theSerializationExecutionContext.getOrElse(theExecutionContext)

  def acceptAsSuccess: Set[Int]
  def contentMediaType: AlmMediaType

  def method: HttpMethod
  def createUri(entity: T): String

  def publishOverWire(entity: T, requestParams: (String, String)*): AlmFuture[(T, FiniteDuration)] = {
    val settings = EntityRequestSettings(createUri(entity), contentMediaType, Seq.empty, method, acceptAsSuccess)
    publishToExternalEndpoint(entity, settings, requestParams: _*)(serializer, problemDeserializer)
  }
}

abstract class PlayWsSingleTypeHttpQuery[PathSegment, U](theSerializationExecutionContext: Option[ExecutionContext])(implicit deserializer: CanDeserializeFromWire[U], problemDeserializer: CanDeserializeFromWire[Problem], theExecutionContext: ExecutionContext) extends PlayWsHttpExternalConnector with PlayWsAwaitingEntityResponse with PlayWsHttpExternalQuery {

  implicit override val executionContext: ExecutionContext = theExecutionContext
  override val serializationExecutionContext: ExecutionContext = theSerializationExecutionContext.getOrElse(theExecutionContext)

  def acceptAsSuccess: Set[Int]
  def acceptMediaTypes: Seq[AlmMediaType]
  def method: HttpMethod
  def createUri(segment: PathSegment): String

  def queryOverWire(segment: PathSegment, requestParams: (String, String)*): AlmFuture[(U, FiniteDuration)] = {
    val settings = BasicRequestSettings(createUri(segment), acceptMediaTypes, method, acceptAsSuccess)
    externalQuery(settings, requestParams: _*)(deserializer, problemDeserializer)
  }
}

abstract class PlayWsSingleTypeDualParamHttpQuery[PathSegment1, PathSegment2, U](theSerializationExecutionContext: Option[ExecutionContext])(implicit deserializer: CanDeserializeFromWire[U], problemDeserializer: CanDeserializeFromWire[Problem], theExecutionContext: ExecutionContext) extends PlayWsHttpExternalConnector with PlayWsAwaitingEntityResponse with PlayWsHttpExternalQuery {
  implicit override val executionContext: ExecutionContext = theExecutionContext
  override val serializationExecutionContext: ExecutionContext = theSerializationExecutionContext.getOrElse(theExecutionContext)

  def acceptAsSuccess: Set[Int]
  def acceptMediaTypes: Seq[AlmMediaType]
  def method: HttpMethod
  def createUri(segment1: PathSegment1, segment2: PathSegment2): String

  def queryOverWire(segment1: PathSegment1, segment2: PathSegment2, requestParams: (String, String)*): AlmFuture[(U, FiniteDuration)] = {
    val settings = BasicRequestSettings(createUri(segment1, segment2), acceptMediaTypes, method, acceptAsSuccess)
    externalQuery(settings, requestParams: _*)(deserializer, problemDeserializer)
  }
}

abstract class PlayWsSingleTypeStaticHttpQuery[U](theSerializationExecutionContext: Option[ExecutionContext])(implicit deserializer: CanDeserializeFromWire[U], problemDeserializer: CanDeserializeFromWire[Problem], theExecutionContext: ExecutionContext) extends PlayWsHttpExternalConnector with PlayWsAwaitingEntityResponse with PlayWsHttpExternalQuery {
  implicit override val executionContext: ExecutionContext = theExecutionContext
  override val serializationExecutionContext: ExecutionContext = theSerializationExecutionContext.getOrElse(theExecutionContext)

  def acceptAsSuccess: Set[Int]
  def acceptMediaTypes: Seq[AlmMediaType]
  def method: HttpMethod
  def uri: String

  def queryOverWire(requestParams: (String, String)*): AlmFuture[(U, FiniteDuration)] = {
    val settings = BasicRequestSettings(uri, acceptMediaTypes, method, acceptAsSuccess)
    externalQuery(settings, requestParams: _*)(deserializer, problemDeserializer)
  }
}


abstract class PlayWsSingleTypeHttpConversation[T, U](theSerializationExecutionContext: Option[ExecutionContext])(implicit serializer: CanSerializeToWire[T], deserializer: CanDeserializeFromWire[T], problemDeserializer: CanDeserializeFromWire[Problem], theExecutionContext: ExecutionContext) extends PlayWsHttpExternalConnector with PlayWsRequestsWithEntity with PlayWsAwaitingEntityResponse with PlayWsHttpExternalConversation {
  implicit override val executionContext: ExecutionContext = theExecutionContext
  override val serializationExecutionContext: ExecutionContext = theSerializationExecutionContext.getOrElse(theExecutionContext)

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