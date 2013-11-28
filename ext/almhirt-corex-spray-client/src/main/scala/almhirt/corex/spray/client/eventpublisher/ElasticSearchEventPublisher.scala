package almhirt.corex.spray.client.eventpublisher

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

class ElasticSearchEventPublisher(
  host: String,
  index: String,
  fixedTypeName: Option[String],
  ttl: FiniteDuration)(implicit serializer: CanSerializeToWire[Event], problemDeserializer: CanDeserializeFromWire[Problem], myAlmhirt: Almhirt) extends HttpEventPublisher() {

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
    ttl: FiniteDuration)(implicit serializer: CanSerializeToWire[Event], problemDeserializer: CanDeserializeFromWire[Problem], theAlmhirt: Almhirt): Props =
    Props(new ElasticSearchEventPublisher(host, index, fixedTypeName, ttl))

  def propsRaw(
    host: String,
    index: String,
    fixedTypeName: Option[String],
    ttl: FiniteDuration,
    mediaTypePrefix: String)(implicit serializer: CanSerializeToWire[Event], problemDeserializer: CanDeserializeFromWire[Problem], theAlmhirt: Almhirt): Props =
    Props(new ElasticSearchEventPublisher(host, index, fixedTypeName, ttl))

  def props(configSection: Config)(implicit serializer: CanSerializeToWire[Event], problemDeserializer: CanDeserializeFromWire[Problem], theAlmhirt: Almhirt): AlmValidation[Props] =
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
      propsRaw(host, index, fixedTypeName, ttl,mediaTypePrefix)
    }

  def props(configPath: String)(implicit serializer: CanSerializeToWire[Event], problemDeserializer: CanDeserializeFromWire[Problem], theAlmhirt: Almhirt): AlmValidation[Props] =
    theAlmhirt.config.v[Config](configPath).flatMap(configSection =>
      props(configSection))

  def props(implicit serializer: CanSerializeToWire[Event], problemDeserializer: CanDeserializeFromWire[Problem], theAlmhirt: Almhirt): AlmValidation[Props] =
    props("almhirt.elastic-search-event-log")

  def apply(configSection: Config)(implicit serializer: CanSerializeToWire[Event], problemDeserializer: CanDeserializeFromWire[Problem], theAlmhirt: Almhirt): AlmValidation[ActorRef] =
    configSection.v[Boolean]("enabled").flatMap(enabled =>
      if (enabled)
        props(configSection).map(props =>
        theAlmhirt.actorSystem.actorOf(props, "elastic-search-event-log"))
      else {
        theAlmhirt.log.warning("""ElasticSearchEventLog: THE ELASTIC SEARCH EVENT LOG IS DISABLED""")
        theAlmhirt.actorSystem.actorOf(Props(new DisabledElasticSearchPublisher()), "elastic-search-event-log").success
      })

  def apply(configPath: String)(implicit serializer: CanSerializeToWire[Event], problemDeserializer: CanDeserializeFromWire[Problem], theAlmhirt: Almhirt): AlmValidation[ActorRef] =
    theAlmhirt.config.v[Config](configPath).flatMap(configSection => apply(configSection))

  def apply()(implicit serializer: CanSerializeToWire[Event], problemDeserializer: CanDeserializeFromWire[Problem], theAlmhirt: Almhirt): AlmValidation[ActorRef] =
    apply("almhirt.elastic-search-event-log")
    
  private class DisabledElasticSearchPublisher extends Actor {
    override def receive: Receive = {
      case ev: Event => ()
    }
  }  
    
}
