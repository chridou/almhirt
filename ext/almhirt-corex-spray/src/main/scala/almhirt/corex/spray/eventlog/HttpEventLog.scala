package almhirt.corex.spray.eventlog

import scala.concurrent._
import scala.concurrent.duration._
import scalaz.syntax.validation._
import akka.actor._
import akka.routing.RoundRobinRouter
import almhirt.messaging.MessagePublisher
import almhirt.common._
import almhirt.configuration._
import almhirt.almfuture.all._
import almhirt.serialization._
import almhirt.core.Almhirt
import almhirt.eventlog.EventLog
import spray.http._
import spray.client.pipelining._
import com.typesafe.config._
import almhirt.eventlog.impl.DevNullEventLog

class HttpEventLog(
  endpointUri: String,
  serializer: EventStringSerializer,
  serializationChannel: String,
  serializationExecutor: ExecutionContext,
  mediaTypePrefix: String)(implicit theAlmhirt: Almhirt) extends EventLog with Actor with ActorLogging {
  import almhirt.eventlog.EventLog._

  implicit val executionContext = theAlmhirt.futuresExecutor

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
      uri = Uri(endpointUri),
      headers = Nil,
      entity = HttpEntity(contentType, serialized._1))
  }

  def evaluateResponse(response: HttpResponse): AlmValidation[Boolean] =
    response.status match {
      case StatusCodes.OK =>
        true.success
      case x =>
        response.entity.toOption match {
          case Some(body) =>
            if (body.contentType.mediaType.isText)
              UnspecifiedProblem(s"Received a text message on status code ${response.status}: ${body.asString}").failure
            else
              UnspecifiedProblem(s"Received content on status code ${response.status}: ${body.asString}").failure
          case None =>
            UnspecifiedProblem(s"""Event log endpoint "$endpointUri" returned an error encoded in status code ${response.status}.""").failure
        }
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

object HttpEventLog {
  def propsRaw(endpointUri: String,
    serializer: EventStringSerializer,
    serializationChannel: String,
    serializationExecutor: ExecutionContext,
    mediaTypePrefix: String,
    theAlmhirt: Almhirt): Props =
    Props(new HttpEventLog(endpointUri, serializer, serializationChannel, serializationExecutor, mediaTypePrefix)(theAlmhirt))

  def propsRaw(endpointUri: String,
    serializer: EventStringSerializer,
    serializationChannel: String,
    mediaTypePrefix: String,
    theAlmhirt: Almhirt): Props =
    Props(new HttpEventLog(endpointUri, serializer, serializationChannel, theAlmhirt.numberCruncher, mediaTypePrefix)(theAlmhirt))

  def props(serializer: EventStringSerializer, configSection: Config, theAlmhirt: Almhirt): AlmValidation[Props] =
    for {
      endpointUri <- configSection.v[String]("endpoint-uri")
      serializationChannel <- configSection.v[String]("transfer-channel")
      mediaTypePrefix <- configSection.v[String]("media-type-prefix")
    } yield {
        theAlmhirt.log.info(s"""HttpEventLog: endpoint-uri = "$endpointUri"""")
        theAlmhirt.log.info(s"""HttpEventLog: transfer-channel = "$serializationChannel"""")
        theAlmhirt.log.info(s"""HttpEventLog: media-type-prefix = "$mediaTypePrefix"""")
      propsRaw(endpointUri, serializer, serializationChannel, mediaTypePrefix, theAlmhirt)
    }

  def props(serializer: EventStringSerializer, configPath: String, theAlmhirt: Almhirt): AlmValidation[Props] =
    theAlmhirt.config.v[Config](configPath).flatMap(configSection =>
      props(serializer, configSection, theAlmhirt: Almhirt))

  def props(serializer: EventStringSerializer, theAlmhirt: Almhirt): AlmValidation[Props] =
    props(serializer, "almhirt.http-event-log", theAlmhirt: Almhirt)

  def apply(serializer: EventStringSerializer, configSection: Config, theAlmhirt: Almhirt): AlmValidation[ActorRef] =
    configSection.v[Boolean]("enabled").flatMap(enabled =>
      if (enabled)
        props(serializer, configSection, theAlmhirt).map(props =>
        theAlmhirt.actorSystem.actorOf(props, "http-event-log"))
      else {
        theAlmhirt.log.warning("""HttpEventLog: THE HTTP EVENT LOG IS DISABLED""")
        theAlmhirt.actorSystem.actorOf(Props(new DevNullEventLog), "event-log").success
      })

  def apply(serializer: EventStringSerializer, configPath: String, theAlmhirt: Almhirt): AlmValidation[ActorRef] =
    theAlmhirt.config.v[Config](configPath).flatMap(configSection => apply(serializer, configSection, theAlmhirt))

  def apply(serializer: EventStringSerializer, theAlmhirt: Almhirt): AlmValidation[ActorRef] =
    apply(serializer, "almhirt.http-event-log", theAlmhirt)
}
