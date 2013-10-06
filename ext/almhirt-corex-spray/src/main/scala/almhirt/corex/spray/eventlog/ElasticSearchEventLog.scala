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

class ElasticSearchEventLog(
  endpointUri: String,
  serializer: EventStringSerializer,
  serializationExecutor: ExecutionContext,
  mediaTypePrefix: String)(implicit theAlmhirt: Almhirt) extends HttpEventLog(serializer, "json", serializationExecutor, mediaTypePrefix)(theAlmhirt) {

  override def createUri(event: Event) =
    s"""$endpointUri/${event.eventId}?op_type=create"""

  def evaluateResponse(response: HttpResponse): AlmValidation[Unit] =
    response.status match {
      case StatusCodes.OK =>
        ().success
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
    
}

object ElasticSearchEventLog {
  def propsRaw(
    endpointUri: String,
    serializer: EventStringSerializer,
    serializationChannel: String,
    serializationExecutor: ExecutionContext,
    mediaTypePrefix: String,
    theAlmhirt: Almhirt): Props =
    Props(new ElasticSearchEventLog(endpointUri, serializer, serializationExecutor, mediaTypePrefix)(theAlmhirt))

  def propsRaw(
    endpointUri: String,
    serializer: EventStringSerializer,
    mediaTypePrefix: String,
    theAlmhirt: Almhirt): Props =
    Props(new ElasticSearchEventLog(endpointUri, serializer, theAlmhirt.numberCruncher, mediaTypePrefix)(theAlmhirt))

  def props(serializer: EventStringSerializer, configSection: Config, theAlmhirt: Almhirt): AlmValidation[Props] =
    for {
      endpointUri <- configSection.v[String]("endpoint-uri")
      mediaTypePrefix <- configSection.v[String]("media-type-prefix")
    } yield {
        theAlmhirt.log.info(s"""HttpEventLog: endpoint-uri = "$endpointUri"""")
        theAlmhirt.log.info(s"""HttpEventLog: media-type-prefix = "$mediaTypePrefix"""")
      propsRaw(endpointUri, serializer, mediaTypePrefix, theAlmhirt)
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
