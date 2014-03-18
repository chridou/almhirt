package almhirt.httpx.play21.client

import scala.concurrent._
import scala.concurrent.duration._
import scalaz._
import scalaz.Scalaz._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.almfuture.all._
import almhirt.serialization._
import almhirt.problem.ProblemCause.prob2ProblemCause
import almhirt.http.AlmMediaType
import play.api.libs.ws.Response
import almhirt.http.AlmMediaTypes

private[client] object Helper {
  def extractChannel(mediaTypeValue: String): AlmValidation[String] = {
    almhirt.almvalidation.funs.inTryCatch {
      val subtype = mediaTypeValue.split('/')(1)
      val potChannel = subtype.split('+') match {
        case Array(x) => x
        case Array(_, y) => y
        case _ => throw new Exception(s"""Invalid media type: "$mediaTypeValue"""")
      }
      if (potChannel.startsWith("x-"))
        potChannel.drop(2)
      else
        potChannel
    }
  }

  def extractChannel(mediaType: AlmMediaType): AlmValidation[String] = extractChannel(mediaType.value)
}

trait PlayWsHttpExternalConnector {
  sealed trait HttpMethod
  object Get extends HttpMethod
  object Put extends HttpMethod
  object Post extends HttpMethod
  object Patch extends HttpMethod
  object Head extends HttpMethod

  trait RequestSettings {
    def targetEndpoint: String
    def acceptMediaTypes: Seq[AlmMediaType]
    def method: HttpMethod
    def acceptAsSuccess: Set[Int]
  }

  case class BasicRequestSettings(
    targetEndpoint: String,
    acceptMediaTypes: Seq[AlmMediaType],
    method: HttpMethod,
    acceptAsSuccess: Set[Int]) extends RequestSettings

  case class EntityRequestSettings(
    targetEndpoint: String,
    contentMediaType: AlmMediaType,
    acceptMediaTypes: Seq[AlmMediaType],
    method: HttpMethod,
    acceptAsSuccess: Set[Int]) extends RequestSettings

  implicit def executionContext: ExecutionContext
  def serializationExecutionContext: ExecutionContext

  def sendRequest(settings: RequestSettings, payload: Option[(WireRepresentation, AlmMediaType)], requestParams: Seq[(String, String)]): AlmFuture[(Response, FiniteDuration)] = {
    val start = Deadline.now
    settings.method match {
      case Get =>
        payload match {
          case Some(pl) => AlmFuture.failed(UnspecifiedProblem("""Method "Get" may not provide a payload(body)"""))
          case None => play.api.libs.ws.WS.url(settings.targetEndpoint).withQueryString(requestParams: _*).get.map((_, start.lap)).toSuccessfulAlmFuture
        }
      case Put =>
        payload match {
          case Some((BinaryWire(pl), mt)) =>
            play.api.libs.ws.WS.url(settings.targetEndpoint).withHeaders(("Content-Type", mt.value)).withQueryString(requestParams: _*).put(pl).map((_, start.lap)).toSuccessfulAlmFuture
          case Some((TextWire(pl), mt)) =>
            play.api.libs.ws.WS.url(settings.targetEndpoint).withHeaders(("Content-Type", mt.value)).withQueryString(requestParams: _*).put(pl).map((_, start.lap)).toSuccessfulAlmFuture
          case None => AlmFuture.failed(UnspecifiedProblem("""Method "Put" must provide a payload(body)"""))
        }
      case Post =>
        payload match {
          case Some((BinaryWire(pl), mt)) =>
            play.api.libs.ws.WS.url(settings.targetEndpoint).withHeaders(("Content-Type", mt.value)).withQueryString(requestParams: _*).post(pl).map((_, start.lap)).toSuccessfulAlmFuture
          case Some((TextWire(pl), mt)) =>
            play.api.libs.ws.WS.url(settings.targetEndpoint).withHeaders(("Content-Type", mt.value)).withQueryString(requestParams: _*).post(pl).map((_, start.lap)).toSuccessfulAlmFuture
          case None =>
            play.api.libs.ws.WS.url(settings.targetEndpoint).withQueryString(requestParams: _*).post("").map((_, start.lap)).toSuccessfulAlmFuture
        }
      case Patch =>
        AlmFuture.failed(UnspecifiedProblem("""Method "Patch" is not supported by PlayWsHttpExternalConnector"""))
      case Head =>
        AlmFuture.failed(UnspecifiedProblem("""Method "Head" is not supported by PlayWsHttpExternalConnector"""))
    }
  }

  def deserialize[T](response: Response)(implicit deserializer: CanDeserializeFromWire[T]): AlmValidation[T] =
    (for {
      contentTypeHeader <- response.ahcResponse.getContentType().notEmptyOrWhitespace
      mediaType <- AlmMediaTypes.get(contentTypeHeader)
      deserialized <- {
        if (mediaType == AlmMediaTypes.`text/plain`)
          UnspecifiedProblem(s"""Received a text message on status code ${response.status}-"${response.statusText}": ${response.body}""").failure
        else {
          Helper.extractChannel(mediaType.value).flatMap(channel =>
            if (mediaType.binary && channel != "json") {
              val bytes = response.ahcResponse.getResponseBodyAsBytes
              deserializer.deserialize(channel)(BinaryWire(bytes), Map.empty)
            } else
              deserializer.deserialize(channel)(TextWire(response.body), Map.empty))
        }
      }
    } yield deserialized).leftMap { innerProb =>
      UnspecifiedProblem(s"""A problem occured handling the response: ${response.ahcResponse.getUri().toString()}""", cause = Some(innerProb))
    }

  def deserializeProblem(response: Response)(implicit deserializer: CanDeserializeFromWire[Problem]): AlmValidation[Problem] = deserialize[Problem](response)
}

trait PlayWsRequestsWithEntity { self: PlayWsHttpExternalConnector =>
  def serializeEntity[T: CanSerializeToWire](payload: T, settings: EntityRequestSettings): AlmValidation[WireRepresentation] = {
    val serializer = implicitly[CanSerializeToWire[T]]
    for {
      channel <- Helper.extractChannel(settings.contentMediaType)
      serialized <- serializer.serialize(channel)(payload, Map.empty)
    } yield serialized._1
  }
}

trait PlayWsAwaitingEntityResponse { self: PlayWsHttpExternalConnector =>
  def evaluateEntityResponse[T: CanDeserializeFromWire](response: Response, acceptAsSuccess: Set[Int])(implicit problemDeserializer: CanDeserializeFromWire[Problem]): AlmValidation[T] = {
    if (acceptAsSuccess(response.status))
      deserialize(response)
    else
      deserializeProblem(response).fold(
        fail => SerializationProblem(s"""The request failed with status code "${response.status}" but I could not deserialize the contained problem.""", cause = Some(fail)).failure,
        succ => succ.failure)
  }
}

trait PlayWsHttpExternalPublisher { self: PlayWsHttpExternalConnector with PlayWsRequestsWithEntity =>
  def publishToExternalEndpoint[T: CanSerializeToWire](payload: T, settings: EntityRequestSettings, requestParams: (String, String)*)(implicit problemDeserializer: CanDeserializeFromWire[Problem]): AlmFuture[(T, FiniteDuration)] =
    for {
      serializedPayload <- AlmFuture(serializeEntity(payload, settings))(serializationExecutionContext)
      responseAndTime <- sendRequest(settings, Some(serializedPayload, settings.contentMediaType), requestParams)
      _ <- AlmFuture(evaluateAck(responseAndTime._1, settings.acceptAsSuccess))(serializationExecutionContext)
    } yield (payload, responseAndTime._2)

  def evaluateAck(response: Response, acceptAsSuccess: Set[Int])(implicit problemDeserializer: CanDeserializeFromWire[Problem]): AlmValidation[Response] = {
    if (acceptAsSuccess(response.status))
      response.success
    else
      deserializeProblem(response).fold(
        fail => SerializationProblem(s"""The request failed with status code "${response.status}" but I could not deserialize the contained problem.""", cause = Some(fail)).failure,
        succ => succ.failure)
  }
}

trait PlayWsHttpExternalQuery { self: PlayWsHttpExternalConnector with PlayWsAwaitingEntityResponse =>
  def externalQuery[U: CanDeserializeFromWire](settings: RequestSettings, requestParams: (String, String)*)(implicit problemDeserializer: CanDeserializeFromWire[Problem]): AlmFuture[(U, FiniteDuration)] =
    for {
      resonseAndTime <- sendRequest(settings, None, requestParams)
      entity <- AlmFuture(evaluateEntityResponse(resonseAndTime._1, settings.acceptAsSuccess)(implicitly[CanDeserializeFromWire[U]], problemDeserializer))(serializationExecutionContext)
    } yield (entity, resonseAndTime._2)
}

trait PlayWsHttpExternalConversation { self: PlayWsHttpExternalConnector with PlayWsRequestsWithEntity with PlayWsAwaitingEntityResponse =>
  def conversationWithExternalEndpoint[T: CanSerializeToWire, U: CanDeserializeFromWire](payload: T, settings: EntityRequestSettings, requestParams: (String, String)*)(implicit problemDeserializer: CanDeserializeFromWire[Problem]): AlmFuture[(U, FiniteDuration)] =
    for {
      serializedEntity <- AlmFuture(serializeEntity(payload, settings))(serializationExecutionContext)
      resonseAndTime <- sendRequest(settings, Some(serializedEntity, settings.contentMediaType), requestParams)
      entity <- AlmFuture(evaluateEntityResponse(resonseAndTime._1, settings.acceptAsSuccess)(implicitly[CanDeserializeFromWire[U]], problemDeserializer))(serializationExecutionContext)
    } yield (entity, resonseAndTime._2)
}
