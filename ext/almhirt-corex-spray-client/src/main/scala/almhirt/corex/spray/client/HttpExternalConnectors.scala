package almhirt.corex.spray.client

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
    }.mapCastTo[(HttpResponse, FiniteDuration)].foldV(
      fail ⇒ NoTimelyResponseFromServiceProblem("The request failed.", cause = Some(fail)).failure,
      succ ⇒ succ.success)
  }

  def deserializeProblem(response: HttpResponse)(implicit problemDeserializer: HttpDeserializer[Problem]): AlmValidation[Problem] =
    response.entity.toOption match {
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
          val deserialized = if (mediaType.binary)
            problemDeserializer.deserialize(mediaType, BinaryBody(body.data.toByteArray))
          else
            problemDeserializer.deserialize(mediaType, TextBody(body.data.asString))
          deserialized.fold(
            fail ⇒ {
              if (mediaType == AlmMediaTypes.`application/json`) {
                SerializationProblem(s"Could not deserialize the response problem but I received some JSON:\n${body.data.asString}", cause = Some(fail)).failure
              } else {
                fail.failure
              }
            },
            succ ⇒ succ.success)
        }
      case None ⇒
        UnspecifiedProblem(s"""	|
          						|Expected an entity from endpoint.
        						|There was no content.
        						|Status code: ${response.status}
        						|Headers: 
        						|${response.headers}""".stripMargin).failure
    }

}

trait RequestsWithEntity { self: HttpExternalConnector ⇒
  def createEntityRequest[T: HttpSerializer](payload: T, settings: EntityRequestSettings): AlmValidation[HttpRequest] = {
    val serializer = implicitly[HttpSerializer[T]]
    for {
      serialized ← serializer.serialize(payload, settings.contentMediaType.toAlmMediaType)
    } yield HttpRequest(
      method = settings.method,
      uri = settings.targetEndpoint,
      headers = Nil,
      entity =
        serialized match {
          case TextBody(data)   ⇒ HttpEntity(ContentType(settings.contentMediaType), data)
          case BinaryBody(data) ⇒ HttpEntity(ContentType(settings.contentMediaType), data)
        })
  }
}

trait AwaitingEntityResponse { self: HttpExternalConnector ⇒
  def evaluateEntityResponse[T: HttpDeserializer](response: HttpResponse, acceptAsSuccess: Set[StatusCode])(implicit problemDeserializer: HttpDeserializer[Problem]): AlmValidation[T] = {
    if (acceptAsSuccess(response.status))
      deserializeEntity[T](response).fold(
        fail1 ⇒ {
          // Try whether there's a problem in the response
          deserializeProblem(response).fold(
            fail2 ⇒ SerializationProblem(s"""	|
            									|This is a strange failure(client side).
            									|1) This response(${response.status}) WAS accepted as a success.
            									|2) The content was not an entity.
            									|3) The content was not a problem(which would be strange with a success status...): "${fail1.message}"""".stripMargin, cause = Some(fail2)).failure,
            aProblem ⇒ SerializationProblem(s"""	|
            										|The response was accepted(${response.status}) as a success but couldn't be deserialized to the entity.
            										|so I tried to deserialize to a Problem which succeeded.
            										|The problem received from server side is contained as this problem's cause.""".stripMargin, cause = Some(aProblem)).failure)
        },
        deserializedEntity ⇒ deserializedEntity.success)
    else {
      if (response.status == StatusCodes.RequestEntityTooLarge) {
        TooMuchDataProblem(s"The request entity was too large.").failure
      } else if (response.status == StatusCodes.RequestTimeout) {
        NoTimelyResponseFromServiceProblem(s"The service did not respond in time.").failure
      } else {
        // In case there is no problem contained, we check whether its the entity...
        deserializeProblem(response).fold(
          fail1 ⇒ deserializeEntity[T](response).fold(
            fail2 ⇒ SerializationProblem(s"""	|
        		  								|This is a strange failure(client side).
            									|1) This response(${response.status}) WAS NOT accepted as a success.
            									|2) The content was not a problem: "${fail1.message}"
            									|3) The content was not an entity.""".stripMargin, cause = Some(fail2)).failure,
            succ ⇒ succ.success),
          succ ⇒ succ.failure)
      }
    }
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
            deserializer.deserialize(mediaType, BinaryBody(body.data.toByteArray))
          else
            deserializer.deserialize(mediaType, TextBody(body.data.asString))
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
      request ← AlmFuture(createEntityRequest(payload, settings))(serializationExecutionContext)
      resonseAndTime ← sendRequest(request)
      _ ← AlmFuture(evaluateAck(resonseAndTime._1, settings.acceptAsSuccess))(serializationExecutionContext)
    } yield (payload, resonseAndTime._2)

  def evaluateAck(response: HttpResponse, acceptAsSuccess: Set[StatusCode])(implicit problemDeserializer: HttpDeserializer[Problem]): AlmValidation[HttpResponse] = {
    if (acceptAsSuccess(response.status))
      response.success
    else
      deserializeProblem(response).fold(
        fail ⇒ SerializationProblem(s"""The request failed(client side, I received something...) with status code "${response.status}" but I could not deserialize the contained problem.""", cause = Some(fail)).failure,
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
      resonseAndTime ← sendRequest(createSimpleQueryRequest(settings))
      entity ← AlmFuture(evaluateEntityResponse(resonseAndTime._1, settings.acceptAsSuccess)(implicitly[HttpDeserializer[U]], problemDeserializer))(serializationExecutionContext)
    } yield (entity, resonseAndTime._2)
}

trait HttpExternalConversation { self: HttpExternalConnector with RequestsWithEntity with AwaitingEntityResponse ⇒
  def conversationWithExternalEndpoint[T: HttpSerializer, U: HttpDeserializer](payload: T, settings: EntityRequestSettings)(implicit problemDeserializer: HttpDeserializer[Problem]): AlmFuture[(U, FiniteDuration)] =
    for {
      request ← AlmFuture(createEntityRequest(payload, settings))(serializationExecutionContext)
      resonseAndTime ← sendRequest(request)
      entity ← AlmFuture(evaluateEntityResponse(resonseAndTime._1, settings.acceptAsSuccess)(implicitly[HttpDeserializer[U]], problemDeserializer))(serializationExecutionContext)
    } yield (entity, resonseAndTime._2)
}
