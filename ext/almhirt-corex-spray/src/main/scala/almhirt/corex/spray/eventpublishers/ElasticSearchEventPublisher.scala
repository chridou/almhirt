package almhirt.corex.spray.eventpublishers

import scala.concurrent._
import scala.concurrent.duration._
import scalaz._, Scalaz._
import akka.actor._
import almhirt.common._
import almhirt.configuration._
import almhirt.almfuture.all._
import almhirt.serialization._
import almhirt.core.Almhirt
import spray.http._
import spray.client.pipelining._
import com.typesafe.config._
import almhirt.eventlog.impl.DevNullEventLog

class ElasticSearchEventPublisher(
  host: String,
  index: String,
  fixedTypeName: Option[String],
  ttl: FiniteDuration,
  override val serializer: CanSerializeToWire[Event],
  override val problemDeserializer: CanDeserializeFromWire[Problem])(implicit theAlmhirt: Almhirt) extends HttpEventPublisher() {

  val uriprefix = s"""http://$host/$index"""

  override val contentMediaType: MediaType = MediaTypes.`application/json`
  override val method: HttpMethod = HttpMethods.PUT
  override val acceptAsSuccess: Set[StatusCode] = Set(StatusCodes.Accepted)

  override def createUri(event: Event): Uri = {
    val typeName = fixedTypeName.getOrElse(event.getClass().getSimpleName())
    Uri(s"""$uriprefix/$typeName/${event.eventId}?op_type=create&ttl=${ttl.toMillis}""")
  }

  override def onProblem(event: Event, problem: Problem, respondTo: ActorRef) {
    log.error(s"""Transmitting the event with id "${event.eventId} of type "${event.getClass().getName()}" failed: $problem""")
  }

  override def onSuccess(event: Event, time: FiniteDuration, respondTo: ActorRef) {}

}

object ElasticSearchEventLog {
  def propsRaw(
    host: String,
    index: String,
    fixedTypeName: Option[String],
    ttl: FiniteDuration,
    serializer: CanSerializeToWire[Event],
    problemSerializer: CanDeserializeFromWire[Problem],
    theAlmhirt: Almhirt): Props =
    Props(new ElasticSearchEventPublisher(host, index, fixedTypeName, ttl, serializer, problemSerializer)(theAlmhirt))

  def propsRaw(
    host: String,
    index: String,
    fixedTypeName: Option[String],
    ttl: FiniteDuration,
    serializer: CanSerializeToWire[Event],
    problemSerializer: CanDeserializeFromWire[Problem],
    mediaTypePrefix: String,
    theAlmhirt: Almhirt): Props =
    Props(new ElasticSearchEventPublisher(host, index, fixedTypeName, ttl, serializer, problemSerializer)(theAlmhirt))

  def props(serializer: CanSerializeToWire[Event], problemSerializer: CanDeserializeFromWire[Problem], configSection: Config, theAlmhirt: Almhirt): AlmValidation[Props] =
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
      propsRaw(host, index, fixedTypeName, ttl, serializer, problemSerializer, mediaTypePrefix, theAlmhirt)
    }

  def props(serializer: CanSerializeToWire[Event], problemSerializer: CanDeserializeFromWire[Problem], configPath: String, theAlmhirt: Almhirt): AlmValidation[Props] =
    theAlmhirt.config.v[Config](configPath).flatMap(configSection =>
      props(serializer, problemSerializer, configSection, theAlmhirt: Almhirt))

  def props(serializer: CanSerializeToWire[Event], problemSerializer: CanDeserializeFromWire[Problem], theAlmhirt: Almhirt): AlmValidation[Props] =
    props(serializer, problemSerializer, "almhirt.elastic-search-event-log", theAlmhirt: Almhirt)

  def apply(serializer: CanSerializeToWire[Event], problemSerializer: CanDeserializeFromWire[Problem], configSection: Config, theAlmhirt: Almhirt): AlmValidation[ActorRef] =
    configSection.v[Boolean]("enabled").flatMap(enabled =>
      if (enabled)
        props(serializer, problemSerializer, configSection, theAlmhirt).map(props =>
        theAlmhirt.actorSystem.actorOf(props, "elastic-search-event-log"))
      else {
        theAlmhirt.log.warning("""ElasticSearchEventLog: THE ELASTIC SEARCH EVENT LOG IS DISABLED""")
        theAlmhirt.actorSystem.actorOf(Props(new DevNullEventLog), "elastic-search-event-log").success
      })

  def apply(serializer: CanSerializeToWire[Event], problemSerializer: CanDeserializeFromWire[Problem], configPath: String, theAlmhirt: Almhirt): AlmValidation[ActorRef] =
    theAlmhirt.config.v[Config](configPath).flatMap(configSection => apply(serializer, problemSerializer, configSection, theAlmhirt))

  def apply(serializer: CanSerializeToWire[Event], problemSerializer: CanDeserializeFromWire[Problem], theAlmhirt: Almhirt): AlmValidation[ActorRef] =
    apply(serializer, problemSerializer, "almhirt.elastic-search-event-log", theAlmhirt)
}
