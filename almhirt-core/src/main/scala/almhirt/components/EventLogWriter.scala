package almhirt.components

import scalaz._, Scalaz._
import scala.language.postfixOps
import scala.concurrent.duration._
import akka.actor._
import almhirt.common._
import akka.stream.actor._
import org.reactivestreams.Subscriber

object EventLogWriter {
  def props(eventLog: ActorRef, maxWaitForEventLog: FiniteDuration = 1 second, writeAggregateRootEvents: Boolean = false): Props = {
    Props(new EventLogWriterImpl(eventLog, writeAggregateRootEvents, maxWaitForEventLog))
  }

  def apply(eventLogWriter: ActorRef): Subscriber[Event] =
    ActorSubscriber[Event](eventLogWriter)
}

private[almhirt] class EventLogWriterImpl(val eventLog: ActorRef, writeAggregateRootEvents: Boolean, maxWaitForEventLog: FiniteDuration) extends ActorSubscriber with ActorLogging {
  import almhirt.eventlog.EventLog

  override val requestStrategy = ZeroRequestStrategy

  private case class PotentialTimeout(eventId: EventId)

  def running(activeEvent: Option[Event]): Receive = {
    case ActorSubscriberMessage.OnNext(event: Event) ⇒
      if (activeEvent.isDefined) {
        sys.error(s"Only one event may be processed at any time. Currently event with id '${activeEvent.get.eventId.value}' is processed.")
      }

      if (!event.isInstanceOf[AggregateRootEvent] || writeAggregateRootEvents) {
        context.system.scheduler.scheduleOnce(maxWaitForEventLog, self, PotentialTimeout(event.eventId))(context.dispatcher)
        eventLog ! EventLog.LogEvent(event)
        context.become(running(activeEvent = Some(event)))
      } else {
        request(1)
      }

    case EventLog.EventLogged(id) ⇒
      if (activeEvent.map(_.eventId == id) | false) {
        request(1)
        context.become(running(activeEvent = None))
      } else {
        log.warning(s"Received event logged for event '${id.value}' which is not the active event.")
      }

    case EventLog.EventNotLogged(id, problem) ⇒
      if (activeEvent.map(_.eventId == id) | false) {
        log.error(s"Could not log event '${id.value}':\n$problem")
        request(1)
        context.become(running(activeEvent = None))
      } else {
        log.error(s"Received event not logged for event '${id.value}' which is not the active event:\n$problem")
      }

    case PotentialTimeout(eventId) ⇒
      if (activeEvent.map(_.eventId == eventId) | false) {
        log.warning(s"Writing event '${eventId.value}' timed out. It might have been written or not...")
        request(1)
        context.become(running(activeEvent = None))
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

  override def receive: Receive = running(activeEvent = None)

  override def preStart() {
    request(1)
  }
} 