package almhirt.httpx.spray.client

import scala.concurrent._
import scala.concurrent.duration._
import scalaz._
import scalaz.Scalaz._
import almhirt.common._
import almhirt.almfuture.all._
import _root_.spray.http._
import almhirt.http._
import almhirt.httpx.spray._

trait HttpExternalConnector {
  trait RequestSettings {
    def targetEndpoint: Uri
    def acceptMediaTypes: Seq[MediaType]
    def method: HttpMethod
    def acceptAsSuccess: Set[StatusCode]
  }

  case class BasicRequestSettings(
    targetEndpoint: Uri,
    acceptMediaTypes: Seq[MediaType],
    method: HttpMethod,
    acceptAsSuccess: Set[StatusCode]) extends RequestSettings

  case class EntityRequestSettings(
    targetEndpoint: Uri,
    contentMediaType: MediaType,
    acceptMediaTypes: Seq[MediaType],
    method: HttpMethod,
    acceptAsSuccess: Set[StatusCode]) extends RequestSettings

  implicit def executionContext: ExecutionContext
  def serializationExecutionContext: ExecutionContext

  def pipeline: HttpRequest ⇒ Future[HttpResponse]

  def sendRequest(request: HttpRequest): AlmFuture[(HttpResponse, FiniteDuration)] = {
    val start = Deadline.now
    pipeline(request).map { resp ⇒ (resp, start.lap)
    }.successfulAlmFuture[(HttpResponse, FiniteDuration)].foldV(
      fail ⇒ UnspecifiedProblem("The request failed.", cause = Some(fail)).failure,
      succ ⇒ succ.success)
  }

  def deserializeProblem(response: HttpResponse)(implicit problemDeserializer: HttpDeserializer[Problem]): AlmValidation[Problem] =
    response.entity.toOption match {
      case Some(body) ⇒
        val mediaType = body.contentType.mediaType.toAlmMediaType
        if (mediaType == AlmMediaTypes.`text/plain`)
          UnspecifiedProblem(s"Received a text message on status code ${response.status}: ${body.asString}").failure
        else {
          if (mediaType.binary)
            problemDeserializer.deserialize(mediaType, BinaryBody(body.data.toByteArray))
          else
            problemDeserializer.deserialize(mediaType, TextBody(body.data.asString))
        }
      case None ⇒
        UnspecifiedProblem(s"""Event endpoint "XXXX" returned an empty response. Status code ${response.status}.""").failure
    }

}

trait RequestsWithEntity { self: HttpExternalConnector ⇒
  def createEntityRequest[T: HttpSerializer](payload: T, settings: EntityRequestSettings): AlmValidation[HttpRequest] = {
    val serializer = implicitly[HttpSerializer[T]]
    for {
      serialized <- serializer.serialize(payload, settings.contentMediaType.toAlmMediaType)
    } yield HttpRequest(
      method = settings.method,
      uri = settings.targetEndpoint,
      headers = Nil,
      entity =
        serialized match {
          case TextBody(data) ⇒ HttpEntity(ContentType(settings.contentMediaType), data)
          case BinaryBody(data) ⇒ HttpEntity(ContentType(settings.contentMediaType), data)
        })
  }
}

trait AwaitingEntityResponse { self: HttpExternalConnector ⇒
  def evaluateEntityResponse[T: HttpDeserializer](response: HttpResponse, acceptAsSuccess: Set[StatusCode])(implicit problemDeserializer: HttpDeserializer[Problem]): AlmValidation[T] = {
    if (acceptAsSuccess(response.status))
      deserializeEntity[T](response)
    else
      deserializeProblem(response).fold(
        fail ⇒ SerializationProblem(s"""The request failed with status code "${response.status}" but I could not deserialize the contained problem.""", cause = Some(fail)).failure,
        succ ⇒ succ.failure)
  }

  def deserializeEntity[T: HttpDeserializer](response: HttpResponse): AlmValidation[T] =
    (response.entity.toOption match {
      case Some(body) ⇒
        val mediaType = body.contentType.mediaType.toAlmMediaType
        if (mediaType == AlmMediaTypes.`text/plain`)
          UnspecifiedProblem(s"""	|
        		  					|Expected an entity but received a text message.
        		  					|Status code: 
        		  					|${response.status}
          							|Headers:
          							|${response.headers}
          							|Body:
          							|${body.asString}""".stripMargin).failure
        else {
          val deserializer = implicitly[HttpDeserializer[T]]
          val channel = almhirt.httpx.spray.marshalling.Helper.extractChannel(body.contentType.mediaType)
          if (mediaType.binary && channel != "json")
            deserializer.deserialize(mediaType ,BinaryBody(body.data.toByteArray))
          else
            deserializer.deserialize(mediaType ,TextBody(body.data.asString))
        }
      case None ⇒
        UnspecifiedProblem(s"""	|
          						|Expected an entity from endpoint.
        						|There was no content.
        						|Status code: ${response.status}
        						|Headers: 
        						|${response.headers}""".stripMargin).failure
    }).leftMap { innerProb ⇒
      val headers = response.headers.map(_.toString).mkString("\nHeaders:\n", "\n", "\n")
      UnspecifiedProblem(s"""A problem occured handling the response.$headers""", cause = Some(innerProb))
    }
}

trait HttpExternalPublisher { self: HttpExternalConnector with RequestsWithEntity ⇒
  def publishToExternalEndpoint[T: HttpSerializer](payload: T, settings: EntityRequestSettings)(implicit problemDeserializer: HttpDeserializer[Problem]): AlmFuture[(T, FiniteDuration)] =
    for {
      request <- AlmFuture(createEntityRequest(payload, settings))(serializationExecutionContext)
      resonseAndTime <- sendRequest(request)
      _ <- AlmFuture(evaluateAck(resonseAndTime._1, settings.acceptAsSuccess))(serializationExecutionContext)
    } yield (payload, resonseAndTime._2)

  def evaluateAck(response: HttpResponse, acceptAsSuccess: Set[StatusCode])(implicit problemDeserializer: HttpDeserializer[Problem]): AlmValidation[HttpResponse] = {
    if (acceptAsSuccess(response.status))
      response.success
    else
      deserializeProblem(response).fold(
        fail ⇒ SerializationProblem(s"""The request failed with status code "${response.status}" but I could not deserialize the contained problem.""", cause = Some(fail)).failure,
        succ ⇒ succ.failure)
  }
}

trait HttpExternalQuery { self: HttpExternalConnector with AwaitingEntityResponse ⇒
  def createSimpleQueryRequest(settings: RequestSettings): HttpRequest =
    HttpRequest(
      method = settings.method,
      uri = settings.targetEndpoint,
      headers = Nil,
      entity = HttpData.Empty)

  def externalQuery[U: HttpDeserializer](settings: RequestSettings)(implicit problemDeserializer: HttpDeserializer[Problem]): AlmFuture[(U, FiniteDuration)] =
    for {
      resonseAndTime <- sendRequest(createSimpleQueryRequest(settings))
      entity <- AlmFuture(evaluateEntityResponse(resonseAndTime._1, settings.acceptAsSuccess)(implicitly[HttpDeserializer[U]], problemDeserializer))(serializationExecutionContext)
    } yield (entity, resonseAndTime._2)
}

trait HttpExternalConversation { self: HttpExternalConnector with RequestsWithEntity with AwaitingEntityResponse ⇒
  def conversationWithExternalEndpoint[T: HttpSerializer, U: HttpDeserializer](payload: T, settings: EntityRequestSettings)(implicit problemDeserializer: HttpDeserializer[Problem]): AlmFuture[(U, FiniteDuration)] =
    for {
      request <- AlmFuture(createEntityRequest(payload, settings))(serializationExecutionContext)
      resonseAndTime <- sendRequest(request)
      entity <- AlmFuture(evaluateEntityResponse(resonseAndTime._1, settings.acceptAsSuccess)(implicitly[HttpDeserializer[U]], problemDeserializer))(serializationExecutionContext)
    } yield (entity, resonseAndTime._2)
}
