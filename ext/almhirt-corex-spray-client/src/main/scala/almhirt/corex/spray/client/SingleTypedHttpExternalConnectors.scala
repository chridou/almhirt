package almhirt.corex.spray.client

import scala.concurrent.duration.FiniteDuration
import akka.actor._
import almhirt.common._
import almhirt.http.{ HttpSerializer, HttpDeserializer }
import spray.http._
import spray.client.pipelining._

abstract class SingleTypeHttpPublisher[T](implicit serializer: HttpSerializer[T], problemDeserializer: HttpDeserializer[Problem]) extends Actor with ActorLogging with HttpExternalConnector with RequestsWithEntity with HttpExternalPublisher {

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

abstract class SingleTypeOneParamHttpQuery[PathSegment, U](implicit deserializer: HttpDeserializer[U], problemDeserializer: HttpDeserializer[Problem]) extends Actor with ActorLogging with HttpExternalConnector with AwaitingEntityResponse with HttpExternalQuery {
  override val pipeline = (sendReceive)

  def acceptAsSuccess: Set[StatusCode]
  def acceptMediaTypes: Seq[MediaType]
  def method: HttpMethod
  def createUri(segment: PathSegment): Uri

  def queryOverWire(segment: PathSegment): AlmFuture[(U, FiniteDuration)] = {
    val settings = BasicRequestSettings(createUri(segment), acceptMediaTypes, method, acceptAsSuccess)
    externalQuery(settings)(deserializer, problemDeserializer)
  }
}

abstract class SingleTypeTwoParamHttpQuery[PathSegment1, PathSegment2, U](implicit deserializer: HttpDeserializer[U], problemDeserializer: HttpDeserializer[Problem]) extends Actor with ActorLogging with HttpExternalConnector with AwaitingEntityResponse with HttpExternalQuery {
  override val pipeline = (sendReceive)

  def acceptAsSuccess: Set[StatusCode]
  def acceptMediaTypes: Seq[MediaType]
  def method: HttpMethod
  def createUri(segment1: PathSegment1, segment2: PathSegment2): Uri

  def queryOverWire(segment1: PathSegment1, segment2: PathSegment2): AlmFuture[(U, FiniteDuration)] = {
    val settings = BasicRequestSettings(createUri(segment1, segment2), acceptMediaTypes, method, acceptAsSuccess)
    externalQuery(settings)(deserializer, problemDeserializer)
  }
}

abstract class SingleTypeHttpConversation[T, U](implicit serializer: HttpSerializer[T], deserializer: HttpDeserializer[U], problemDeserializer: HttpDeserializer[Problem]) extends Actor with ActorLogging with HttpExternalConnector with RequestsWithEntity with AwaitingEntityResponse with HttpExternalConversation {
  def acceptAsSuccess: Set[StatusCode]
  def contentMediaType: MediaType
  def acceptMediaTypes: Seq[MediaType]
  def method: HttpMethod
  def createUri(entity: T): Uri

  override val pipeline = (sendReceive)

  def conversationOverWire(entity: T): AlmFuture[(U, FiniteDuration)] = {
    val settings = EntityRequestSettings(createUri(entity), contentMediaType, acceptMediaTypes, method, acceptAsSuccess)
    conversationWithExternalEndpoint(entity, settings)(serializer, deserializer, problemDeserializer)
  }

}

abstract class SingleTypeHttpConversationWithParametrizedQuery[T, U](implicit serializer: HttpSerializer[T], deserializer: HttpDeserializer[U], problemDeserializer: HttpDeserializer[Problem]) extends Actor with ActorLogging with HttpExternalConnector with RequestsWithEntity with AwaitingEntityResponse with HttpExternalConversation {
  case class UriPartsAndParams(segments: Seq[String], params: Map[String, Option[String]]) {
    def appendTo(prefix: String): Uri = {
      val segmentsAppendix = if (segments.isEmpty) "" else "/" + segments.mkString("/")
      val paramsAppendix =
        if (params.isEmpty)
          ""
        else {
          val items = params.map {
            case (name, value) =>
              value match {
                case Some(v) => s"$name=$v"
                case None => s"$name"
              }
          }.mkString("&")
          s"?$items"
        }
      s"$prefix$segmentsAppendix$paramsAppendix"
    }
  }

  def acceptAsSuccess: Set[StatusCode]
  def contentMediaType: MediaType
  def acceptMediaTypes: Seq[MediaType]
  def method: HttpMethod
  def createUri(entity: T, uriPartsAndParams: UriPartsAndParams): Uri

  override val pipeline = (sendReceive)

  def conversationOverWire(entity: T, uriPartsAndParams: UriPartsAndParams): AlmFuture[(U, FiniteDuration)] = {
    val settings = EntityRequestSettings(createUri(entity, uriPartsAndParams), contentMediaType, acceptMediaTypes, method, acceptAsSuccess)
    conversationWithExternalEndpoint[T, U](entity, settings)(serializer, deserializer, problemDeserializer)
  }

}