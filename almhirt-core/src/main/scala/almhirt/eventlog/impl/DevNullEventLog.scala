package almhirt.eventlog.impl

import akka.actor._
import almhirt.common._
import almhirt.eventlog.EventLog

class DevNullEventLog extends EventLog with Actor {
  import EventLog._

  final protected def receiveEventLogMsg: Receive = {
    case LogEvent(event) =>
    case GetEvent(eventId) =>
      sender ! QueriedEvent(eventId, None)
    case GetAllEvents =>
      sender ! FetchedEventsBatch(Vector.empty)
    case GetEventsFrom(from) =>
      sender ! FetchedEventsBatch(Vector.empty)
    case GetEventsAfter(after) =>
      sender ! FetchedEventsBatch(Vector.empty)
    case GetEventsTo(to) =>
      sender ! FetchedEventsBatch(Vector.empty)
    case GetEventsUntil(until) =>
      sender ! FetchedEventsBatch(Vector.empty)
    case GetEventsFromTo(from, to) =>
      sender ! FetchedEventsBatch(Vector.empty)
    case GetEventsFromUntil(from, until) =>
      sender ! FetchedEventsBatch(Vector.empty)
    case GetEventsAfterTo(after, to) =>
      sender ! FetchedEventsBatch(Vector.empty)
    case GetEventsAfterUntil(after, until) =>
      sender ! FetchedEventsBatch(Vector.empty)
  }

  override def receive: Receive = receiveEventLogMsg

}

object DevNullEventLog {
  def props(): Props = {
    Props(new DevNullEventLog())
  }
}