package almhirt.corex.spray.eventlog

import scala.concurrent._
import scala.concurrent.duration._
import scalaz.syntax.validation._
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

abstract class HttpEventLog(
//  endpointUri: String,
  serializer: EventStringSerializer,
  serializationChannel: String,
  serializationExecutor: ExecutionContext,
  mediaTypePrefix: String)(implicit theAlmhirt: Almhirt) extends EventLog with Actor with ActorLogging {
  import almhirt.eventlog.EventLog._

  implicit val executionContext = theAlmhirt.futuresExecutor

  def createUri(event: Event): String    
  def evaluateResponse(response: HttpResponse): AlmValidation[Unit]
  
  def eventContentType(channel: String): AlmValidation[ContentType] = {
    val eventMediaType = MediaType.custom(s"""$mediaTypePrefix.Event+$channel""")
    ContentType(eventMediaType, HttpCharsets.`UTF-8`).success
  }

  def protocolFromContentType(contentType: ContentType): AlmValidation[String] =
    if (contentType.mediaType.value.contains("+json"))
      "json".success
    else if (contentType.mediaType.value.contains("+xml"))
      "xml".success
    else
      MappingProblem(s""""${contentType.mediaType.value}" is not a valid content type.""").failure

  def createRequest(event: Event, channel: String): AlmValidation[HttpRequest] = {
    for {
      serialized <- serializer.serialize(channel)(event, Map.empty)
      contentType <- eventContentType(channel)
    } yield HttpRequest(
      method = HttpMethods.PUT,
      uri = Uri(createUri(event)),
      headers = Nil,
      entity = HttpEntity(contentType, serialized._1))
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
      execState <- AlmFuture(evaluateResponse(resonseAndTime._1))
    } yield (resonseAndTime._2)
  }

  final protected def currentState(serializationChannel: String): Receive = {
    case LogEvent(event) =>
      transmitEvent(event, serializationChannel).onComplete(
        fail => log.error(s"""Transmitting the event with id "${event.eventId} of type "${event.getClass().getName()}" failed: $fail"""),
        succ => ())

    case GetEvent(eventId) =>
      sender ! EventQueryFailed(eventId, UnspecifiedProblem(""""GetEvent" is not supported."""))

    case GetAllEvents =>
      sender ! FetchedEventsFailure(UnspecifiedProblem(""""GetAllEvents" is not supported."""))

    case GetEventsFrom(from) =>
      sender ! FetchedEventsFailure(UnspecifiedProblem(""""GetEventsFrom" is not supported."""))

    case GetEventsAfter(after) =>
      sender ! FetchedEventsFailure(UnspecifiedProblem(""""GetEventsAfter" is not supported."""))

    case GetEventsTo(to) =>
      sender ! FetchedEventsFailure(UnspecifiedProblem(""""GetEventsTo" is not supported."""))

    case GetEventsUntil(until) =>
      sender ! FetchedEventsFailure(UnspecifiedProblem(""""GetEventsUntil" is not supported."""))

    case GetEventsFromTo(from, to) =>
      sender ! FetchedEventsFailure(UnspecifiedProblem(""""GetEventsFromTo" is not supported."""))

    case GetEventsFromUntil(from, until) =>
      sender ! FetchedEventsFailure(UnspecifiedProblem(""""GetEventsFromUntil" is not supported."""))

    case GetEventsAfterTo(after, to) =>
      sender ! FetchedEventsFailure(UnspecifiedProblem(""""GetEventsAfterTo" is not supported."""))

    case GetEventsAfterUntil(after, until) =>
      sender ! FetchedEventsFailure(UnspecifiedProblem(""""GetEventsAfterUntil" is not supported."""))

  }

  override def receiveEventLogMsg = currentState(serializationChannel)
  override def receive: Receive = receiveEventLogMsg
}

