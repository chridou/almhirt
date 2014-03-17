package almhirt.httpx.play21.client

import scala.concurrent._
import scala.concurrent.duration._
import scalaz._
import scalaz.Scalaz._
import almhirt.common._
import almhirt.almfuture.all._
import almhirt.serialization._
import almhirt.problem.ProblemCause.prob2ProblemCause
import almhirt.http.AlmMediaType
import play.api.http.{Status}
import play.api.libs.ws.Response

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

//  private val validMediaTypeChannels = Set("html", "json", "xml", "msgpack")
//  def isValidMediaType(mediaType: MediaType): Boolean =
//    try {
//      validMediaTypeChannels.contains(extractChannel(mediaType))
//    } catch {
//      case NonFatal(exn) => false
//    }
//
//  def validateMediaTypes(mediaTypes: Seq[MediaType]): AlmValidation[Seq[MediaType]] = {
//    val res =
//      mediaTypes.map(mediaType =>
//        if (isValidMediaType(mediaType))
//          mediaType.success
//        else
//          UnspecifiedProblem(s"""Not a valid media type: "${mediaType.value}".""").failure).map(_.toAgg).toList
//    res.sequence[AlmValidationAP, MediaType]
//  }
}

trait HttpExternalConnector {
  sealed trait HttpMethod
  object Get extends HttpMethod
  object Put extends HttpMethod
  object Post extends HttpMethod
  
  trait RequestSettings {
    def targetEndpoint: String
    def acceptMediaTypes: Seq[AlmMediaType]
    def method: HttpMethod
    def acceptAsSuccess: Set[Status]
  }

  case class BasicRequestSettings(
    targetEndpoint: String,
    acceptMediaTypes: Seq[AlmMediaType],
    method: HttpMethod,
    acceptAsSuccess: Set[Status]) extends RequestSettings

  case class EntityRequestSettings(
    targetEndpoint: String,
    contentMediaType: AlmMediaType,
    acceptMediaTypes: Seq[AlmMediaType],
    method: HttpMethod,
    acceptAsSuccess: Set[Status]) extends RequestSettings

  implicit def executionContext: ExecutionContext
  def serializationExecutionContext: ExecutionContext

  def sendRequest(request: HttpRequest): AlmFuture[(Response, FiniteDuration)] = {
    val start = Deadline.now
    pipeline(request).map { resp => (resp, start.lap)
    }.successfulAlmFuture[(HttpResponse, FiniteDuration)].foldV(
      fail => UnspecifiedProblem("The request failed.", cause = Some(fail)).failure,
      succ => succ.success)
  }

  def deserializeProblem(response: HttpResponse)(implicit problemDeserializer: CanDeserializeFromWire[Problem]): AlmValidation[Problem] =
    response.entity.toOption match {
      case Some(body) =>
        val mediaType = body.contentType.mediaType
        if (mediaType == MediaTypes.`text/plain`)
          UnspecifiedProblem(s"Received a text message on status code ${response.status}: ${body.asString}").failure
        else {
          val channel = almhirt.httpx.spray.marshalling.Helper.extractChannel(body.contentType.mediaType)
          if (mediaType.binary && channel != "json")
            problemDeserializer.deserialize(channel)(BinaryWire(body.data.toByteArray), Map.empty)
          else
            problemDeserializer.deserialize(channel)(TextWire(body.data.asString), Map.empty)
        }
      case None =>
        UnspecifiedProblem(s"""Event endpoint "XXXX" returned an empty response. Status code ${response.status}.""").failure
    }

}

trait RequestsWithEntity { self: HttpExternalConnector =>
  def createEntityRequest[T: CanSerializeToWire](payload: T, settings: EntityRequestSettings): AlmValidation[HttpRequest] = {
    val serializer = implicitly[CanSerializeToWire[T]]
    for {
      channel <- almhirt.httpx.spray.marshalling.Helper.extractChannel(settings.contentMediaType).success
      serialized <- serializer.serialize(channel)(payload, Map.empty)
    } yield HttpRequest(
      method = settings.method,
      uri = settings.targetEndpoint,
      headers = Nil,
      entity =
        serialized._1 match {
          case TextWire(data) => HttpEntity(ContentType(settings.contentMediaType), data)
          case BinaryWire(data) => HttpEntity(ContentType(settings.contentMediaType), data)
        })
  }
}

trait AwaitingEntityResponse { self: HttpExternalConnector =>
  def evaluateEntityResponse[T: CanDeserializeFromWire](response: HttpResponse, acceptAsSuccess: Set[StatusCode])(implicit problemDeserializer: CanDeserializeFromWire[Problem]): AlmValidation[T] = {
    if (acceptAsSuccess(response.status))
      deserializeEntity(response)
    else
      deserializeProblem(response).fold(
        fail => SerializationProblem(s"""The request failed with status code "${response.status}" but I could not deserialize the contained problem.""", cause = Some(fail)).failure,
        succ => succ.failure)
  }

  def deserializeEntity[T: CanDeserializeFromWire](response: HttpResponse): AlmValidation[T] =
    (response.entity.toOption match {
      case Some(body) =>
        val mediaType = body.contentType.mediaType
        if (mediaType == MediaTypes.`text/plain`)
          UnspecifiedProblem(s"Expected an entity but received a text message on status code ${response.status}: ${body.asString}").failure
        else {
          val deserializer = implicitly[CanDeserializeFromWire[T]]
          val channel = almhirt.httpx.spray.marshalling.Helper.extractChannel(body.contentType.mediaType)
          if (mediaType.binary && channel != "json")
            deserializer.deserialize(channel)(BinaryWire(body.data.toByteArray), Map.empty)
          else
            deserializer.deserialize(channel)(TextWire(body.data.asString), Map.empty)
        }
      case None =>
        UnspecifiedProblem(s"""Expected an entity from endpoint "XXXX" there was no content. Status code ${response.status}.""").failure
    }).leftMap { innerProb =>
      val headers = response.headers.map(_.toString).mkString("\nHeaders:\n", "\n", "\n")
      UnspecifiedProblem(s"""A problem occured handling the response.$headers""", cause = Some(innerProb))
    }
}

trait HttpExternalPublisher { self: HttpExternalConnector with RequestsWithEntity =>
  def publishToExternalEndpoint[T: CanSerializeToWire](payload: T, settings: EntityRequestSettings)(implicit problemDeserializer: CanDeserializeFromWire[Problem]): AlmFuture[(T, FiniteDuration)] =
    for {
      request <- AlmFuture(createEntityRequest(payload, settings))(serializationExecutionContext)
      resonseAndTime <- sendRequest(request)
      _ <- AlmFuture(evaluateAck(resonseAndTime._1, settings.acceptAsSuccess))(serializationExecutionContext)
    } yield (payload, resonseAndTime._2)

  def evaluateAck(response: HttpResponse, acceptAsSuccess: Set[StatusCode])(implicit problemDeserializer: CanDeserializeFromWire[Problem]): AlmValidation[HttpResponse] = {
    if (acceptAsSuccess(response.status))
      response.success
    else
      deserializeProblem(response).fold(
        fail => SerializationProblem(s"""The request failed with status code "${response.status}" but I could not deserialize the contained problem.""", cause = Some(fail)).failure,
        succ => succ.failure)
  }
}

trait HttpExternalQuery { self: HttpExternalConnector with AwaitingEntityResponse =>
  def createSimpleQueryRequest(settings: RequestSettings): HttpRequest =
    HttpRequest(
      method = settings.method,
      uri = settings.targetEndpoint,
      headers = Nil,
      entity = HttpData.Empty)

  def externalQuery[U: CanDeserializeFromWire](settings: RequestSettings)(implicit problemDeserializer: CanDeserializeFromWire[Problem]): AlmFuture[(U, FiniteDuration)] =
    for {
      resonseAndTime <- sendRequest(createSimpleQueryRequest(settings))
      entity <- AlmFuture(evaluateEntityResponse(resonseAndTime._1, settings.acceptAsSuccess)(implicitly[CanDeserializeFromWire[U]], problemDeserializer))(serializationExecutionContext)
    } yield (entity, resonseAndTime._2)
}

trait HttpExternalConversation { self: HttpExternalConnector with RequestsWithEntity with AwaitingEntityResponse =>
  def conversationWithExternalEndpoint[T: CanSerializeToWire, U: CanDeserializeFromWire](payload: T, settings: EntityRequestSettings)(implicit problemDeserializer: CanDeserializeFromWire[Problem]): AlmFuture[(U, FiniteDuration)] =
    for {
      request <- AlmFuture(createEntityRequest(payload, settings))(serializationExecutionContext)
      resonseAndTime <- sendRequest(request)
      entity <- AlmFuture(evaluateEntityResponse(resonseAndTime._1, settings.acceptAsSuccess)(implicitly[CanDeserializeFromWire[U]], problemDeserializer))(serializationExecutionContext)
    } yield (entity, resonseAndTime._2)
}
