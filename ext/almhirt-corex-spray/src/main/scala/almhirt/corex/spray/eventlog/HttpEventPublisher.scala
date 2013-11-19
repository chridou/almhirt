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
import almhirt.corex.spray.SingleTypeHttpPublisher
import com.typesafe.config.Config
import akka.event.LoggingAdapter

abstract class HttpEventPublisher()(implicit myAlmhirt: Almhirt) extends SingleTypeHttpPublisher[Event]() {
  import almhirt.eventlog.EventLog._

  override def createUri(event: Event): Uri
  def onProblem(event: Event, problem: Problem, respondTo: ActorRef)
  def onSuccess(event: Event, time: FiniteDuration, respondTo: ActorRef)

  def publishEventOverWire(event: Event) {
    publishOverWire(event).onComplete(
      fail => onProblem(event, fail, sender),
      success => onSuccess(success._1, success._2, sender))(myAlmhirt.futuresExecutor)
  }

  override def receive: Receive = {
    case LogEvent(event) => publishEventOverWire(event)
  }
}

object HttpEventPublisher {
  def props(theAlmhirt: Almhirt, mediaType: MediaType, serializesEvent: CanSerializeToWire[Event], deserialzesProblem: CanDeserializeFromWire[Problem], configSection: Config): AlmValidation[Props] = {
    for {
      endpointUri <- configSection.v[String]("endpoint-uri")
      failureLogMode <- configSection.opt[String]("on-failure")
      addEventId <- configSection.v[Boolean]("add-event-id-to-uri")
      failureAction <- failureLogMode match {
        case Some("debug") => ((msg: String, log: LoggingAdapter) => log.debug(msg)).success
        case Some("info") => ((msg: String, log: LoggingAdapter) => log.info(msg)).success
        case Some("warning") => ((msg: String, log: LoggingAdapter) => log.warning(msg)).success
        case Some("error") => ((msg: String, log: LoggingAdapter) => log.error(msg)).success
        case None => ((msg: String, log: LoggingAdapter) => ()).success
        case Some(x) => UnspecifiedProblem(s"""Invalid failure action: "$x"""").failure
      }
    } yield {
      Props(new HttpEventPublisherImpl(failureAction, addEventId, endpointUri)(theAlmhirt) {
        val method = HttpMethods.PUT
        val contentMediaType = mediaType
        override val problemDeserializer = deserialzesProblem
        override val serializer = serializesEvent
      })
    }
  }

  def apply(mediaType: MediaType, serializesEvent: CanSerializeToWire[Event], deserialzesProblem: CanDeserializeFromWire[Problem], configPath: String, theAlmhirt: Almhirt): AlmValidation[(ActorRef, CloseHandle)] = {
    theAlmhirt.log.info(s"""Creating HttpEventPublisher from config "$configPath".""")
    for {
      configSection <- theAlmhirt.config.v[Config](configPath)
      enabled <- configSection.v[Boolean]("enabled")
      actorName <- configSection.v[String]("actor-name")
      theProps <- if (enabled)
        props(theAlmhirt, mediaType, serializesEvent, deserialzesProblem, configSection)
      else {
        theAlmhirt.log.warning(s"""The HttpEventPublisher configured in "$configPath" is DISABLED.""")
        Props(new Actor { def receive = Actor.emptyBehavior }).success
      }
    } yield {
      val actor = theAlmhirt.actorSystem.actorOf(theProps, actorName)
      val close = CloseHandle.noop
      (actor, close)
    }
  }

  private abstract class HttpEventPublisherImpl(logAction: (String, LoggingAdapter) => Unit, addEventId: Boolean, endpointUri: String)(implicit myAlmhirt: Almhirt) extends HttpEventPublisher() {

    override val acceptAsSuccess: Set[StatusCode] = Set(StatusCodes.OK, StatusCodes.Accepted)

    override def createUri(event: Event): Uri = {
      if (addEventId)
        Uri(s"""$endpointUri/$event.eventId""")
      else
        Uri(endpointUri)
    }

    override def onSuccess(event: Event, time: FiniteDuration, respondTo: ActorRef) {}

    override def onProblem(event: Event, problem: Problem, respondTo: ActorRef) {
      val msg = s"""Failed to transmit event with id "${event.eventId}" of type "${event.getClass.getName}": $problem"""
      logAction(msg, this.log)
    }
  }

}

