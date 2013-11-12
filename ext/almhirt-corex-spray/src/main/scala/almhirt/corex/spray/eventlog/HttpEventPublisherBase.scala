package almhirt.corex.spray.eventlog

import scala.concurrent._
import scala.concurrent.duration._
import scalaz._, Scalaz._
import akka.actor._
import almhirt.messaging.MessagePublisher
import almhirt.common._
import almhirt.configuration._
import almhirt.almfuture.all._
import almhirt.serialization._
import almhirt.core.Almhirt
import almhirt.eventlog.EventLog
import spray.http._
import spray.client.pipelining._

abstract class HttpEventPublisherBase(
  serializer: CanSerializeToWire[Event],
  problemSerializer: CanDeserializeFromWire[Problem],
  serializationChannel: String,
  serializationExecutor: ExecutionContext,
  mediaTypePrefix: String)(implicit theAlmhirt: Almhirt) extends Actor {
  import almhirt.eventlog.EventLog._

  implicit val executionContext = theAlmhirt.futuresExecutor

  def createUri(event: Event): String
  def onProblem(event: Event, problem: Problem)
 

  def eventContentType(channel: String): AlmValidation[ContentType] = {
    val eventMediaType = MediaType.custom(s"""$mediaTypePrefix.Event+$channel""")
    ContentType(eventMediaType, HttpCharsets.`UTF-8`).success
  }

  def createRequest(event: Event, channel: String): AlmValidation[HttpRequest] = {
    for {
      serialized <- serializer.serialize(channel)(event, Map.empty)
      contentType <- eventContentType(channel)
    } yield HttpRequest(
      method = HttpMethods.PUT,
      uri = Uri(createUri(event)),
      headers = Nil,
      entity =
        serialized._1 match {
          case TextWire(data) => HttpEntity(contentType, data)
          case BinaryWire(data) => HttpEntity(contentType, data)
        })
  }

  val pipeline: HttpRequest => Future[HttpResponse] = (sendReceive)

  def sendRequest(request: HttpRequest): AlmFuture[(HttpResponse, FiniteDuration)] = {
    val start = Deadline.now
    pipeline(request).map { resp => (resp, start.lap)
    }.successfulAlmFuture[(HttpResponse, FiniteDuration)].foldV(
      fail => UnspecifiedProblem("The request failed", cause = Some(fail)).failure,
      succ => succ.success)
  }

  def transmitEvent(event: Event, channel: String): AlmFuture[FiniteDuration] = {
    for {
      request <- AlmFuture(createRequest(event, channel))(serializationExecutor)
      resonseAndTime <- sendRequest(request)
      _ <- evaluateResponse(resonseAndTime._1) match {
        case Some(problem) => AlmFuture.failed(problem)
        case None => AlmFuture.successful(())
      }
    } yield (resonseAndTime._2)
  }

  private var currentSerializationChannel = serializationChannel
  protected def logEventHandler: Receive = {
    case LogEvent(event) =>
      transmitEvent(event, currentSerializationChannel).onFailure(
        fail => onProblem(event, fail))
  }
  
  
  private def evaluateResponse(response: HttpResponse): Option[Problem] = {
    response.status match {
      case StatusCodes.Created =>
        None
      case x =>
        response.entity.toOption match {
          case Some(body) =>
            if (body.contentType.mediaType.isText)
              UnspecifiedProblem(s"Received a text message on status code ${response.status}: ${body.asString}").some
            else
              UnspecifiedProblem(s"Received content on status code ${response.status}: ${body.asString}").some
          case None =>
            UnspecifiedProblem(s"""Event endpoint "XXXX" returned an error encoded in status code ${response.status}.""").some
        }
    }
  }

//  def protocolFromContentType(contentType: ContentType): AlmValidation[String] =
//    if (contentType.mediaType.value.contains("+json"))
//      "json".success
//    else if (contentType.mediaType.value.contains("+xml"))
//      "xml".success
//    else
//      MappingProblem(s""""${contentType.mediaType.value}" is not a valid content type.""").failure
  
}

