package almhirt.httpx.play21.client

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration
import akka.actor._
import almhirt.common._
import almhirt.serialization.CanDeserializeFromWire
import almhirt.serialization.CanSerializeToWire
import almhirt.http.AlmMediaType

/** Publishes an entity of type T to an external end point. 
 *  
 *  @type T The type of the entity to send 
 */
abstract class PlayWsHttpPublisher[T](theSerializationExecutionContext: Option[ExecutionContext])(implicit serializer: CanSerializeToWire[T], problemDeserializer: CanDeserializeFromWire[Problem], theExecutionContext: ExecutionContext) extends PlayWsHttpExternalConnector with PlayWsRequestsWithEntity with PlayWsHttpExternalPublisher {
  implicit override val executionContext: ExecutionContext = theExecutionContext
  override val serializationExecutionContext: ExecutionContext = theSerializationExecutionContext.getOrElse(theExecutionContext)

  def acceptAsSuccess: Set[Int]
  def contentMediaType: AlmMediaType

  def method: HttpMethod
  
  /** Create an URI based on some data extracted from the entity */
  def createUri(entity: T): String

  /** Publish the entity T to the remote service
   *  
   *  @param entity The entity to publish
   *  @param requestParams URL encoded request parameters
   *  @return The published entity and the time the transmission took
   */
  def publishOverWire(entity: T, requestParams: (String, String)*): AlmFuture[(T, FiniteDuration)] = {
    val settings = EntityRequestSettings(createUri(entity), contentMediaType, Seq.empty, method, acceptAsSuccess)
    publishToExternalEndpoint(entity, settings, requestParams: _*)(serializer, problemDeserializer)
  }
}

/** Query an entity of type U from an external service with a variable segment in the URL.
 *
 * @Type PathSegment A type from which the segment is formed
 * @Type U The type of the expected entity
 */
abstract class PlayWsSingleSegmentHttpQuery[PathSegment, U](theSerializationExecutionContext: Option[ExecutionContext])(implicit deserializer: CanDeserializeFromWire[U], problemDeserializer: CanDeserializeFromWire[Problem], theExecutionContext: ExecutionContext) extends PlayWsHttpExternalConnector with PlayWsAwaitingEntityResponse with PlayWsHttpExternalQuery {

  implicit override val executionContext: ExecutionContext = theExecutionContext
  override val serializationExecutionContext: ExecutionContext = theSerializationExecutionContext.getOrElse(theExecutionContext)

  def acceptAsSuccess: Set[Int]
  def acceptMediaTypes: Seq[AlmMediaType]
  def method: HttpMethod
  
  /** Create an URI based on a given segment */
  def createUri(segment: PathSegment): String

  /** Publish the entity T to the remote service
   *  
   *  @param segment A segment inserted somewhere into the URI
   *  @param requestParams URL encoded request parameters
   *  @return The retrieved entity and the time the transmission took
   */
  def queryOverWire(segment: PathSegment, requestParams: (String, String)*): AlmFuture[(U, FiniteDuration)] = {
    val settings = BasicRequestSettings(createUri(segment), acceptMediaTypes, method, acceptAsSuccess)
    externalQuery(settings, requestParams: _*)(deserializer, problemDeserializer)
  }
}

/** Query an entity of type U from an external service with two variable segments in the URL.
 *
 * @Type PathSegment1 A type from which the first segment is formed
 * @Type PathSegment2 A type from which the second segment is formed
 * @Type U The type of the expected entity
 */
abstract class PlayDualSegmentHttpQuery[PathSegment1, PathSegment2, U](theSerializationExecutionContext: Option[ExecutionContext])(implicit deserializer: CanDeserializeFromWire[U], problemDeserializer: CanDeserializeFromWire[Problem], theExecutionContext: ExecutionContext) extends PlayWsHttpExternalConnector with PlayWsAwaitingEntityResponse with PlayWsHttpExternalQuery {
  implicit override val executionContext: ExecutionContext = theExecutionContext
  override val serializationExecutionContext: ExecutionContext = theSerializationExecutionContext.getOrElse(theExecutionContext)

  def acceptAsSuccess: Set[Int]
  def acceptMediaTypes: Seq[AlmMediaType]
  def method: HttpMethod
  
  /** Create an URI based on the given segments */
  def createUri(segment1: PathSegment1, segment2: PathSegment2): String

  /** Request an entity T to the remote service
   *  
   *  @param segment1 The first segment inserted somewhere into the URI
   *  @param segment2 The second segment inserted somewhere into the URI
   *  @param requestParams URL encoded request parameters
   *  @return The retrieved entity and the time the transmission took
   */
  def queryOverWire(segment1: PathSegment1, segment2: PathSegment2, requestParams: (String, String)*): AlmFuture[(U, FiniteDuration)] = {
    val settings = BasicRequestSettings(createUri(segment1, segment2), acceptMediaTypes, method, acceptAsSuccess)
    externalQuery(settings, requestParams: _*)(deserializer, problemDeserializer)
  }
}

/** Query an entity of type U from an external service with a fixed URL.
 *
 * @Type U The type of the expected entity
 */
abstract class PlayWsFixedSegmentHttpQuery[U](theSerializationExecutionContext: Option[ExecutionContext])(implicit deserializer: CanDeserializeFromWire[U], problemDeserializer: CanDeserializeFromWire[Problem], theExecutionContext: ExecutionContext) extends PlayWsHttpExternalConnector with PlayWsAwaitingEntityResponse with PlayWsHttpExternalQuery {
  implicit override val executionContext: ExecutionContext = theExecutionContext
  override val serializationExecutionContext: ExecutionContext = theSerializationExecutionContext.getOrElse(theExecutionContext)

  def acceptAsSuccess: Set[Int]
  def acceptMediaTypes: Seq[AlmMediaType]
  def method: HttpMethod
  
  /** The fixed URL */
  def uri: String

  /** Request an entity T to the remote service
   *  
   *  @param requestParams URL encoded request parameters
   *  @return The retrieved entity and the time the transmission took
   */
  def queryOverWire(requestParams: (String, String)*): AlmFuture[(U, FiniteDuration)] = {
    val settings = BasicRequestSettings(uri, acceptMediaTypes, method, acceptAsSuccess)
    externalQuery(settings, requestParams: _*)(deserializer, problemDeserializer)
  }
}


/** Send an entity to a remote service and expect another entity as a response
 *
 * @Type T The type of the entity to send
 * @Type U The type of the expected entity
 */
abstract class PlayWsHttpConversation[T, U](theSerializationExecutionContext: Option[ExecutionContext])(implicit serializer: CanSerializeToWire[T], deserializer: CanDeserializeFromWire[T], problemDeserializer: CanDeserializeFromWire[Problem], theExecutionContext: ExecutionContext) extends PlayWsHttpExternalConnector with PlayWsRequestsWithEntity with PlayWsAwaitingEntityResponse with PlayWsHttpExternalConversation {
  implicit override val executionContext: ExecutionContext = theExecutionContext
  override val serializationExecutionContext: ExecutionContext = theSerializationExecutionContext.getOrElse(theExecutionContext)

  def acceptAsSuccess: Set[Int]
  def contentMediaType: AlmMediaType
  def acceptMediaTypes: Seq[AlmMediaType]
  def method: HttpMethod
  
  /** Create an URI based on some data extracted from the entity to send */
  def createUri(entity: T): String

  /** Publish the entity T to the remote service and expect a response
   *  
   *  @param entity The entity to send with the request
   *  @param requestParams URL encoded request parameters
   *  @return The response entity and the time the transmission took
   */
  def conversationOverWire(entity: T, requestParams: (String, String)*): AlmFuture[(T, FiniteDuration)] = {
    val settings = EntityRequestSettings(createUri(entity), contentMediaType, acceptMediaTypes, method, acceptAsSuccess)
    conversationWithExternalEndpoint(entity, settings, requestParams: _*)(serializer, deserializer, problemDeserializer)
  }
}

/** Send an entity to a remote service and expect another entity as a response
 *
 * @Type PathSegment A type from which the segment is formed
 * @Type T The type of the entity to send
 * @Type U The type of the expected entity
 */
abstract class PlayWsHttpSingleSegmentConversation[PathSegment, T, U](theSerializationExecutionContext: Option[ExecutionContext])(implicit serializer: CanSerializeToWire[T], deserializer: CanDeserializeFromWire[T], problemDeserializer: CanDeserializeFromWire[Problem], theExecutionContext: ExecutionContext) extends PlayWsHttpExternalConnector with PlayWsRequestsWithEntity with PlayWsAwaitingEntityResponse with PlayWsHttpExternalConversation {
  implicit override val executionContext: ExecutionContext = theExecutionContext
  override val serializationExecutionContext: ExecutionContext = theSerializationExecutionContext.getOrElse(theExecutionContext)

  def acceptAsSuccess: Set[Int]
  def contentMediaType: AlmMediaType
  def acceptMediaTypes: Seq[AlmMediaType]
  def method: HttpMethod
  
  /** Create an URI based on some data extracted from the entity to send */
  def createUri(segment: PathSegment, entity: T): String

  /** Publish the entity T to the remote service and expect a response
   *  
   *  @param segment A segment inserted somewhere into the URI
   *  @param entity The entity to send with the request
   *  @param requestParams URL encoded request parameters
   *  @return The response entity and the time the transmission took
   */
  def conversationOverWire(segment: PathSegment, entity: T, requestParams: (String, String)*): AlmFuture[(T, FiniteDuration)] = {
    val settings = EntityRequestSettings(createUri(segment, entity), contentMediaType, acceptMediaTypes, method, acceptAsSuccess)
    conversationWithExternalEndpoint(entity, settings, requestParams: _*)(serializer, deserializer, problemDeserializer)
  }
}

/** Send an entity to a remote service and expect another entity as a response
 *
 * @Type PathSegment1 A type from which the first segment is formed
 * @Type PathSegment2 A type from which the second segment is formed
 * @Type T The type of the entity to send
 * @Type U The type of the expected entity
 */
abstract class PlayWsHttpDualSegmentConversation[PathSegment1, PathSegment2, T, U](theSerializationExecutionContext: Option[ExecutionContext])(implicit serializer: CanSerializeToWire[T], deserializer: CanDeserializeFromWire[T], problemDeserializer: CanDeserializeFromWire[Problem], theExecutionContext: ExecutionContext) extends PlayWsHttpExternalConnector with PlayWsRequestsWithEntity with PlayWsAwaitingEntityResponse with PlayWsHttpExternalConversation {
  implicit override val executionContext: ExecutionContext = theExecutionContext
  override val serializationExecutionContext: ExecutionContext = theSerializationExecutionContext.getOrElse(theExecutionContext)

  def acceptAsSuccess: Set[Int]
  def contentMediaType: AlmMediaType
  def acceptMediaTypes: Seq[AlmMediaType]
  def method: HttpMethod
  
  /** Create an URI based on the given segments */
  def createUri(segment1: PathSegment1, segment2: PathSegment2, entity: T): String

  /** Publish the entity T to the remote service and expect a response
   *  
   *  @param segment A segment inserted somewhere into the URI
   *  @param entity The entity to send with the request
   *  @param requestParams URL encoded request parameters
   *  @return The response entity and the time the transmission took
   */
  def conversationOverWire(segment1: PathSegment1, segment2: PathSegment2, entity: T, requestParams: (String, String)*): AlmFuture[(T, FiniteDuration)] = {
    val settings = EntityRequestSettings(createUri(segment1, segment2, entity), contentMediaType, acceptMediaTypes, method, acceptAsSuccess)
    conversationWithExternalEndpoint(entity, settings, requestParams: _*)(serializer, deserializer, problemDeserializer)
  }
}