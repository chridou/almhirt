package almhirt.components

import scalaz._, Scalaz._
import scala.language.postfixOps
import scala.concurrent.duration._
import akka.actor._
import almhirt.common._
import akka.stream.actor._
import org.reactivestreams.Subscriber

object EventLogWriter {
  def props(
    eventLogPath: ActorPath,
    lookupInterval: FiniteDuration,
    maxLookupDuration: FiniteDuration,
    maxWaitForEventLog: FiniteDuration = 1 second): Props = {
    Props(new EventLogWriterImpl(
      eventLogPath,
      maxWaitForEventLog,
      lookupInterval: FiniteDuration,
      maxLookupDuration: FiniteDuration))
  }

  def apply(eventLogWriter: ActorRef): Subscriber[Event] =
    ActorSubscriber[Event](eventLogWriter)
}

private[almhirt] class EventLogWriterImpl(
  eventLogPath: ActorPath,
  maxWaitForEventLog: FiniteDuration,
  lookupInterval: FiniteDuration,
  maxLookupDuration: FiniteDuration) extends ActorSubscriber with ActorLogging {
  import almhirt.eventlog.EventLog

  override val requestStrategy = ZeroRequestStrategy

  private case class PotentialTimeout(eventId: EventId)

  private case class ResolvePath(path: ActorPath)
  private case class SelectionResolved(actor: ActorRef)
  private case class SelectionNotResolved(path: ActorPath, ex: Throwable)

  def receiveLookup(start: Deadline): Receive = {
    case ResolvePath(path) =>
      val selection = context.actorSelection(path)
      selection.resolveOne(1.second).onComplete {
        case scala.util.Success(actor) => self ! SelectionResolved(actor)
        case scala.util.Failure(ex) => self ! SelectionNotResolved(path, ex)
      }(context.dispatcher)

    case SelectionResolved(eventLog) =>
      context.become(running(eventLog, None))
      request(1)

    case SelectionNotResolved(path, ex) =>
      if (start.lapExceeds(maxLookupDuration)) {
        log.error(s"""	|Could not resolve "${path}" after ${maxLookupDuration.defaultUnitString}.
        				|Cause:
    		  			|$ex""".stripMargin)
        throw ex
      } else {
        log.warning(s"""	|Could not resolve "${path}" after ${maxLookupDuration.defaultUnitString}.
        					|Will retry in ${lookupInterval.defaultUnitString}.
        					|Cause:
        					|$ex""".stripMargin)
        context.system.scheduler.scheduleOnce(lookupInterval, self, ResolvePath(eventLogPath))(context.dispatcher)
      }
  }

  def running(eventLog: ActorRef, activeEvent: Option[Event]): Receive = {
    case ActorSubscriberMessage.OnNext(event: Event) ⇒
      if (activeEvent.isDefined) {
        sys.error(s"Only one event may be processed at any time. Currently event with id '${activeEvent.get.eventId.value}' is processed.")
      }

      context.system.scheduler.scheduleOnce(maxWaitForEventLog, self, PotentialTimeout(event.eventId))(context.dispatcher)
      eventLog ! EventLog.LogEvent(event)
      context.become(running(eventLog, activeEvent = Some(event)))

    case EventLog.EventLogged(id) ⇒
      if (activeEvent.map(_.eventId == id) | false) {
        request(1)
        context.become(running(eventLog, activeEvent = None))
      } else {
        log.warning(s"Received event logged for event '${id.value}' which is not the active event.")
      }

    case EventLog.EventNotLogged(id, problem) ⇒
      if (activeEvent.map(_.eventId == id) | false) {
        log.error(s"Could not log event '${id.value}':\n$problem")
        request(1)
        context.become(running(eventLog, activeEvent = None))
      } else {
        log.error(s"Received event not logged for event '${id.value}' which is not the active event:\n$problem")
      }

    case PotentialTimeout(eventId) ⇒
      if (activeEvent.map(_.eventId == eventId) | false) {
        log.warning(s"Writing event '${eventId.value}' timed out. It might have been written or not...")
        request(1)
        context.become(running(eventLog, activeEvent = None))
      } else {
        log.warning(s"Received timeout for event '${eventId.value}' which is not the active event.")
      }

    case ActorSubscriberMessage.OnNext(unprocessable) ⇒
      if (activeEvent.isDefined) {
        sys.error(s"Only one event may be processed at any time. Currently event with id '${activeEvent.get.eventId.value}' is processed.")
      } else {
        log.warning(s"received unprocessable element $unprocessable")
        request(1)
      }
  }

  override def receive: Receive = receiveLookup(Deadline.now)

  override def preStart() {
    self ! ResolvePath(eventLogPath)
  }
} 