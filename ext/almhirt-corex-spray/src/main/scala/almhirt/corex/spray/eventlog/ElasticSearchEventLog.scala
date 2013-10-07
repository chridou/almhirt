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
  host: String,
  index: String,
  fixedTypeName: Option[String],
  ttl: FiniteDuration,
  serializer: EventStringSerializer,
  serializationExecutor: ExecutionContext,
  mediaTypePrefix: String)(implicit theAlmhirt: Almhirt) extends HttpEventLog(serializer, "json", serializationExecutor, mediaTypePrefix)(theAlmhirt) {

  val uriprefix = s"""http://$host/$index"""

  override def createUri(event: Event) = {
    val typeName = fixedTypeName.getOrElse(event.getClass().getSimpleName())
    s"""$uriprefix/$typeName/${event.eventId}?op_type=create&ttl=${ttl.toMillis}"""
  }

  def evaluateResponse(response: HttpResponse): AlmValidation[Unit] =
    response.status match {
      case StatusCodes.Created =>
        ().success
      case x =>
        response.entity.toOption match {
          case Some(body) =>
            if (body.contentType.mediaType.isText)
              UnspecifiedProblem(s"Received a text message on status code ${response.status}: ${body.asString}").failure
            else
              UnspecifiedProblem(s"Received content on status code ${response.status}: ${body.asString}").failure
          case None =>
            UnspecifiedProblem(s"""Event log endpoint "uriprefix" returned an error encoded in status code ${response.status}.""").failure
        }
    }

}

object ElasticSearchEventLog {
  def propsRaw(
    host: String,
    index: String,
    fixedTypeName: Option[String],
    ttl: FiniteDuration,
    serializer: EventStringSerializer,
    serializationChannel: String,
    serializationExecutor: ExecutionContext,
    mediaTypePrefix: String,
    theAlmhirt: Almhirt): Props =
    Props(new ElasticSearchEventLog(host, index, fixedTypeName, ttl, serializer, serializationExecutor, mediaTypePrefix)(theAlmhirt))

  def propsRaw(
    host: String,
    index: String,
    fixedTypeName: Option[String],
    ttl: FiniteDuration,
    serializer: EventStringSerializer,
    mediaTypePrefix: String,
    theAlmhirt: Almhirt): Props =
    Props(new ElasticSearchEventLog(host, index, fixedTypeName, ttl, serializer, theAlmhirt.numberCruncher, mediaTypePrefix)(theAlmhirt))

  def props(serializer: EventStringSerializer, configSection: Config, theAlmhirt: Almhirt): AlmValidation[Props] =
    for {
      host <- configSection.v[String]("host")
      index <- configSection.v[String]("index")
      useFixedType <- configSection.v[Boolean]("use-fixed-type")
      fixedTypeName <- if (useFixedType)
        configSection.v[String]("fixed-type-name").map(Some(_))
      else
        None.success
      ttl <- configSection.v[FiniteDuration]("time-to-live")
      mediaTypePrefix <- configSection.v[String]("media-type-prefix")
    } yield {
      theAlmhirt.log.info(s"""ElasticSearchEventLog: host = "$host"""")
      theAlmhirt.log.info(s"""ElasticSearchEventLog: index = "$index"""")
      theAlmhirt.log.info(s"""ElasticSearchEventLog: use-fixed-type = $useFixedType""")
      fixedTypeName.foreach(ftn => theAlmhirt.log.info(s"""ElasticSearchEventLog: fixed-type-name = "$ftn""""))
      theAlmhirt.log.info(s"""ElasticSearchEventLog: time-to-live = ${ttl.defaultUnitString}""")
      theAlmhirt.log.info(s"""ElasticSearchEventLog: media-type-prefix = "$mediaTypePrefix"""")
      propsRaw(host, index, fixedTypeName, ttl, serializer, mediaTypePrefix, theAlmhirt)
    }

  def props(serializer: EventStringSerializer, configPath: String, theAlmhirt: Almhirt): AlmValidation[Props] =
    theAlmhirt.config.v[Config](configPath).flatMap(configSection =>
      props(serializer, configSection, theAlmhirt: Almhirt))

  def props(serializer: EventStringSerializer, theAlmhirt: Almhirt): AlmValidation[Props] =
    props(serializer, "almhirt.elastic-search-event-log", theAlmhirt: Almhirt)

  def apply(serializer: EventStringSerializer, configSection: Config, theAlmhirt: Almhirt): AlmValidation[ActorRef] =
    configSection.v[Boolean]("enabled").flatMap(enabled =>
      if (enabled)
        props(serializer, configSection, theAlmhirt).map(props =>
        theAlmhirt.actorSystem.actorOf(props, "elastic-search-event-log"))
      else {
        theAlmhirt.log.warning("""ElasticSearchEventLog: THE HTTP EVENT LOG IS DISABLED""")
        theAlmhirt.actorSystem.actorOf(Props(new DevNullEventLog), "event-log").success
      })

  def apply(serializer: EventStringSerializer, configPath: String, theAlmhirt: Almhirt): AlmValidation[ActorRef] =
    theAlmhirt.config.v[Config](configPath).flatMap(configSection => apply(serializer, configSection, theAlmhirt))

  def apply(serializer: EventStringSerializer, theAlmhirt: Almhirt): AlmValidation[ActorRef] =
    apply(serializer, "almhirt.elastic-search-event-log", theAlmhirt)
}
