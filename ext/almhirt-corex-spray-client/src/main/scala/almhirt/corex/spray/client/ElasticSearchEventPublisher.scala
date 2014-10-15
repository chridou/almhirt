package almhirt.corex.spray.client

import scala.reflect.ClassTag
import scala.concurrent.duration.FiniteDuration
import scalaz.syntax.validation._
import scalaz.Validation.FlatMap._
import akka.actor._
import almhirt.common._
import almhirt.akkax._
import almhirt.http._
import almhirt.configuration._
import almhirt.context._
import spray.http._
import spray.client.pipelining._
import org.reactivestreams.Subscriber
import akka.stream.actor._
import org.reactivestreams.{ Subscriber, Publisher }
import almhirt.streaming.ActorDevNullSubscriberWithAutoSubscribe
import com.typesafe.config.Config

object ElasticSearchEventPublisher {
  def propsRaw(
    host: String,
    index: String,
    fixedTypeName: Option[String],
    ttl: FiniteDuration,
    autoConnectTo: Option[Publisher[Event]],
    circuitControlSettings: CircuitControlSettings,
    circuitStateReportingInterval: Option[FiniteDuration],
    missedEventSeverity: almhirt.problem.Severity)(implicit serializer: HttpSerializer[Event], problemDeserializer: HttpDeserializer[Problem], almhirtContext: AlmhirtContext): Props =
    Props(new ElasticSearchEventPublisherImpl(host, index, fixedTypeName, ttl, autoConnectTo, circuitControlSettings, circuitStateReportingInterval, missedEventSeverity))

  def props(elConfigName: Option[String] = None)(implicit ctx: AlmhirtContext, serializer: HttpSerializer[Event], problemDeserializer: HttpDeserializer[Problem]): AlmValidation[Props] = {
    val path = "almhirt.components.misc.event-sink-hub.event-publishers.http-event-publishers.elastic-search-event-publisher" + elConfigName.map("." + _).getOrElse("")
    for {
      section <- ctx.config.v[com.typesafe.config.Config](path)
      enabled <- section.v[Boolean]("enabled")
      autoConnect <- section.v[Boolean]("auto-connect")
      res <- if (enabled) {
        for {
          host <- section.v[String]("host")
          index <- section.v[String]("index")
          fixedTypeName <- section.magicOption[String]("index")
          ttl <- section.v[FiniteDuration]("ttl")
          missedEventSeverity <- section.v[almhirt.problem.Severity]("missed-event-severity")
          circuitControlSettings <- section.v[CircuitControlSettings]("circuit-control")
          circuitStateReportingInterval <- section.magicOption[FiniteDuration]("circuit-state-reporting-interval")
        } yield propsRaw(host, index, fixedTypeName, ttl, if (autoConnect) Some(ctx.eventStream) else None, circuitControlSettings, circuitStateReportingInterval, missedEventSeverity)
      } else {
        ActorDevNullSubscriberWithAutoSubscribe.props[Event](1, if (autoConnect) Some(ctx.eventStream) else None).success
      }
    } yield res
  }

  def apply(elasticSearchEventPublischer: ActorRef): Subscriber[Event] =
    ActorSubscriber[Event](elasticSearchEventPublischer)

  val actorname = "elastic-search-event-publisher"
}

private[almhirt] class ElasticSearchEventPublisherImpl(
  host: String,
  index: String,
  fixedTypeName: Option[String],
  ttl: FiniteDuration,
  autoConnectTo: Option[Publisher[Event]],
  circuitControlSettings: CircuitControlSettings,
  circuitStateReportingInterval: Option[FiniteDuration],
  missedEventSeverity: almhirt.problem.Severity)(implicit serializer: HttpSerializer[Event], problemDeserializer: HttpDeserializer[Problem], executionContexts: HasExecutionContexts, override val almhirtContext: AlmhirtContext)
  extends ActorConsumerHttpPublisher[Event](autoConnectTo, Set(StatusCodes.Accepted, StatusCodes.Created), MediaTypes.`application/json`, HttpMethods.PUT, circuitControlSettings, circuitStateReportingInterval)(serializer, problemDeserializer, implicitly[ClassTag[Event]])
  with HasAlmhirtContext {
  val uriprefix = s"""http://$host/$index"""

  override def onFailure(item: Event, problem: Problem): Unit = {
    reportMissedEvent(item, missedEventSeverity, problem)
    reportFailure(problem, missedEventSeverity)
  }

  implicit override val executionContext = executionContexts.futuresContext
  override val serializationExecutionContext = executionContexts.futuresContext

  override val pipeline = (sendReceive)

  override def createUri(event: Event): Uri = {
    val typeName = fixedTypeName.getOrElse(event.getClass().getSimpleName())
    Uri(s"""$uriprefix/$typeName/${event.eventId}?op_type=create&ttl=${ttl.toMillis}""")
  }

}