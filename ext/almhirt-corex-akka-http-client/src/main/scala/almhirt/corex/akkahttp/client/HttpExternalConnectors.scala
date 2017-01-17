package almhirt.corex.akkahttp.client

import scala.concurrent._
import scala.concurrent.duration._
import scalaz._
import scalaz.Scalaz._
import almhirt.common._
import almhirt.almfuture.all._
import akka.http.scaladsl.model._
import almhirt.http._
import almhirt.httpx.akkahttp._
import akka.stream.Materializer
import java.nio.charset.Charset
import akka.http.scaladsl.Http
import akka.actor.ActorSystem

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

  def sendRequest(request: HttpRequest)(implicit actorSystem: ActorSystem, materializer: Materializer): AlmFuture[(HttpResponse, FiniteDuration)] = {
    val start = Deadline.now
    Http().singleRequest(request).map { resp ⇒ (resp, start.lap)
    }.mapCastTo[(HttpResponse, FiniteDuration)].foldV(
      fail ⇒ NoTimelyResponseFromServiceProblem("The request failed.", cause = Some(fail)).failure,
      succ ⇒ succ.success)
  }

  def deserializeProblem(response: HttpResponse)(implicit problemDeserializer: HttpDeserializer[Problem], materializer: Materializer): Future[AlmValidation[Problem]] = {
    for {
      loadedEntity <- response.entity.toStrict(5.seconds)
      deserialized <- Future {
        if (loadedEntity.data.isEmpty) {
          UnspecifiedProblem(s"""	|
          						|Expected an entity from endpoint.
        						|There was no content.
        						|Status code: ${response.status}
        						|Headers: 
        						|${response.headers}""".stripMargin).failure
        } else {
          val mediaType = loadedEntity.contentType.mediaType.toAlmMediaType
          val bodyStringified: String = loadedEntity.data.decodeString(mediaType.charsetOption.getOrElse(HttpCharsets.`UTF-8`).nioCharset)
          if (mediaType == AlmMediaTypes.`text/plain`) {
            UnspecifiedProblem(s"""	|
        		  					|Expected an entity but received a text message.
        		  					|Status code: 
        		  					|${response.status}
          							|Headers:
          							|${response.headers}
          							|Body:
          							|${bodyStringified}""".stripMargin).failure
          } else {
            val deserialized = if (mediaType.binary)
              problemDeserializer.deserialize(mediaType, BinaryBody(loadedEntity.data.toArray))
            else
              problemDeserializer.deserialize(mediaType, TextBody(bodyStringified))
            deserialized.fold(
              fail ⇒ {
                if (mediaType == AlmMediaTypes.`application/json`) {
                  SerializationProblem(s"Could not deserialize the response problem but I received some JSON:\n${bodyStringified}", cause = Some(fail)).failure
                } else {
                  fail.failure
                }
              },
              succ ⇒ succ.success)
          }
        }
      }
    } yield deserialized
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
          case TextBody(data) =>
            val mediaType = MediaType.applicationWithFixedCharset(settings.contentMediaType.subType, HttpCharsets.`UTF-8`, settings.contentMediaType.fileExtensions: _*)
            val contentType: ContentType.NonBinary = ContentType.WithFixedCharset(mediaType)
            HttpEntity(contentType, data)
          case BinaryBody(data) => HttpEntity(ContentType.Binary(MediaType.applicationBinary(
            settings.contentMediaType.subType,
            settings.contentMediaType.comp,
            settings.contentMediaType.fileExtensions: _*)), data)
        })
  }
}

trait AwaitingEntityResponse { self: HttpExternalConnector ⇒
  def evaluateEntityResponse[T: HttpDeserializer](response: HttpResponse, acceptAsSuccess: Set[StatusCode])(implicit problemDeserializer: HttpDeserializer[Problem], materializer: Materializer): AlmValidation[T] = {
    if (acceptAsSuccess(response.status))
      Await.result(deserializeEntity[T](response), 5.seconds).fold(
        fail1 ⇒ {
          // Try whether there's a problem in the response
          Await.result(deserializeProblem(response), 5.seconds).fold(
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
        Await.result(deserializeProblem(response), 5.seconds).fold(
          fail1 ⇒ Await.result(deserializeEntity[T](response), 5.seconds).fold(
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

  def deserializeEntity[T: HttpDeserializer](response: HttpResponse)(implicit materializer: Materializer): Future[AlmValidation[T]] = {
    for {
      loadedEntity <- response.entity.toStrict(5.seconds)
      deserialized <- Future {
        if (loadedEntity.data.isEmpty) {
          UnspecifiedProblem(s"""	|
          						|Expected an entity from endpoint.
        						|There was no content.
        						|Status code: ${response.status}
        						|Headers: 
        						|${response.headers}""".stripMargin).failure
        } else {
          val mediaType = loadedEntity.contentType.mediaType.toAlmMediaType
          val bodyStringified: String = loadedEntity.data.decodeString(mediaType.charsetOption.getOrElse(HttpCharsets.`UTF-8`).nioCharset)
          if (mediaType == AlmMediaTypes.`text/plain`) {
            UnspecifiedProblem(s"""	|
        		  					|Expected an entity but received a text message.
        		  					|Status code: 
        		  					|${response.status}
          							|Headers:
          							|${response.headers}
          							|Body:
          							|${bodyStringified}""".stripMargin).failure
          } else {
            val deserializer = implicitly[HttpDeserializer[T]]
            val channel = almhirt.httpx.akkahttp.marshalling.Helper.extractChannel(loadedEntity.contentType.mediaType)
            val deserialized = if (mediaType.binary && channel != "json")
              deserializer.deserialize(mediaType, BinaryBody(loadedEntity.data.toArray))
            else
              deserializer.deserialize(mediaType, TextBody(bodyStringified))
            deserialized.fold(
              fail ⇒ {
                if (mediaType == AlmMediaTypes.`application/json`) {
                  SerializationProblem(s"Could not deserialize the response problem but I received some JSON:\n${bodyStringified}", cause = Some(fail)).failure
                } else {
                  fail.failure
                }
              },
              succ ⇒ succ.success)
          }
        }
      }
    } yield deserialized
  }
}

trait HttpExternalPublisher { self: HttpExternalConnector with RequestsWithEntity ⇒
  def publishToExternalEndpoint[T: HttpSerializer](payload: T, settings: EntityRequestSettings)(implicit problemDeserializer: HttpDeserializer[Problem], actorSystem: ActorSystem, materializer: Materializer): AlmFuture[(T, FiniteDuration)] =
    for {
      request ← AlmFuture(createEntityRequest(payload, settings))(serializationExecutionContext)
      resonseAndTime ← sendRequest(request)
      _ ← AlmFuture(evaluateAck(resonseAndTime._1, settings.acceptAsSuccess))(serializationExecutionContext)
    } yield (payload, resonseAndTime._2)

  def evaluateAck(response: HttpResponse, acceptAsSuccess: Set[StatusCode])(implicit problemDeserializer: HttpDeserializer[Problem], materializer: Materializer): AlmValidation[HttpResponse] = {
    if (acceptAsSuccess(response.status))
      response.success
    else
      Await.result(deserializeProblem(response), 5.seconds).fold(
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
      entity = HttpEntity.Empty)

  def externalQuery[U: HttpDeserializer](settings: RequestSettings)(implicit problemDeserializer: HttpDeserializer[Problem], actorSystem: ActorSystem, materializer: Materializer): AlmFuture[(U, FiniteDuration)] =
    for {
      resonseAndTime ← sendRequest(createSimpleQueryRequest(settings))
      entity ← AlmFuture(evaluateEntityResponse(resonseAndTime._1, settings.acceptAsSuccess)(implicitly[HttpDeserializer[U]], problemDeserializer, materializer))(serializationExecutionContext)
    } yield (entity, resonseAndTime._2)
}

trait HttpExternalConversation { self: HttpExternalConnector with RequestsWithEntity with AwaitingEntityResponse ⇒
  def conversationWithExternalEndpoint[T: HttpSerializer, U: HttpDeserializer](payload: T, settings: EntityRequestSettings)(implicit problemDeserializer: HttpDeserializer[Problem], actorSystem: ActorSystem, materializer: Materializer): AlmFuture[(U, FiniteDuration)] =
    for {
      request ← AlmFuture(createEntityRequest(payload, settings))(serializationExecutionContext)
      resonseAndTime ← sendRequest(request)
      entity ← AlmFuture(evaluateEntityResponse(resonseAndTime._1, settings.acceptAsSuccess)(implicitly[HttpDeserializer[U]], problemDeserializer, materializer))(serializationExecutionContext)
    } yield (entity, resonseAndTime._2)
}
