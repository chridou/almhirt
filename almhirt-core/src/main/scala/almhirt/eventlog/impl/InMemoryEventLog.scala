package almhirt.eventlog.impl

import akka.actor._
import almhirt.common._
import almhirt.eventlog.EventLog

trait InMemoryEventLog extends EventLog { actor: Actor with ActorLogging =>
  import EventLog._

  private var eventLog = Vector.empty[Event]

  final protected def receiveEventLogMsg: Receive = {
    case LogEvent(event) =>
      eventLog = (eventLog :+ event).sortBy(_.timestamp)
    case GetEvent(eventId) =>
      sender ! QueriedEvent(eventId, eventLog.find(_.eventId == eventId))
    case GetAllEvents =>
      sender ! EventsChunk(0, true, eventLog)
    case GetEventsFrom(from) =>
      sender ! EventsChunk(0, true, eventLog.filter(event => event.timestamp.compareTo(from) >= 0))
    case GetEventsAfter(after) =>
      sender ! EventsChunk(0, true, eventLog.filter(event => event.timestamp.compareTo(after) > 0))
    case GetEventsTo(to) =>
      sender ! EventsChunk(0, true, eventLog.filter(event => event.timestamp.compareTo(to) <= 0))
    case GetEventsUntil(until) =>
      sender ! EventsChunk(0, true, eventLog.filter(event => event.timestamp.compareTo(until) < 0))
    case GetEventsFromTo(from, to) =>
      sender ! EventsChunk(0, true, eventLog.filter(event => event.timestamp.compareTo(from) >= 0 && event.timestamp.compareTo(to) <= 0))
    case GetEventsFromUntil(from, until) =>
      sender ! EventsChunk(0, true, eventLog.filter(event => event.timestamp.compareTo(from) >= 0 && event.timestamp.compareTo(until) < 0))
    case GetEventsAfterTo(after, to) =>
      sender ! EventsChunk(0, true, eventLog.filter(event => event.timestamp.compareTo(after) > 0 && event.timestamp.compareTo(to) <= 0))
    case GetEventsAfterUntil(after, until) =>
      sender ! EventsChunk(0, true, eventLog.filter(event => event.timestamp.compareTo(after) > 0 && event.timestamp.compareTo(until) < 0))
  }
}

class InMemoryEventLogImpl() extends InMemoryEventLog with Actor with ActorLogging {
  override def receive: Receive = receiveEventLogMsg
}

object InMemoryEventLog {
  def props(): Props = {
    Props(new InMemoryEventLogImpl())
  }
}