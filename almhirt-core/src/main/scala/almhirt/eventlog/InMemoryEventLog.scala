package almhirt.eventlog

import akka.actor._
import almhirt.common._
import play.api.libs.iteratee.Enumerator

object InMemoryEventLog {
  def props(): Props =
    Props(new InMemoryEventLog())
}

class InMemoryEventLog extends Actor with ActorLogging {
  import EventLog._

  private var eventLog = Vector.empty[Event]

  def receive: Receive = {
    case LogEvent(event, acknowledge) ⇒
      eventLog = (eventLog :+ event).sortBy(_.timestamp)
      if (acknowledge)
        sender() ! EventLogged(event.eventId)
    case FindEvent(eventId) ⇒
      sender() ! FoundEvent(eventId, eventLog.find(_.eventId == eventId))
    case FetchAllEvents(skip, take) ⇒
      sender() ! FetchedEvents(Enumerator(eventLog: _*))
    case FetchEventsFrom(from, skip, take) ⇒
      sender() ! FetchedEvents(Enumerator(eventLog.filter(event ⇒ event.timestamp.compareTo(from) >= 0): _*))
    case FetchEventsAfter(after, skip, take) ⇒
      sender() ! FetchedEvents(Enumerator(eventLog.filter(event ⇒ event.timestamp.compareTo(after) > 0): _*))
    case FetchEventsTo(to, skip, take) ⇒
      sender() ! FetchedEvents(Enumerator(eventLog.filter(event ⇒ event.timestamp.compareTo(to) <= 0): _*))
    case FetchEventsUntil(until, skip, take) ⇒
      sender() ! FetchedEvents(Enumerator(eventLog.filter(event ⇒ event.timestamp.compareTo(until) < 0): _*))
    case FetchEventsFromTo(from, to, skip, take) ⇒
      sender() ! FetchedEvents(Enumerator(eventLog.filter(event ⇒ event.timestamp.compareTo(from) >= 0 && event.timestamp.compareTo(to) <= 0): _*))
    case FetchEventsFromUntil(from, until, skip, take) ⇒
      sender() ! FetchedEvents(Enumerator(eventLog.filter(event ⇒ event.timestamp.compareTo(from) >= 0 && event.timestamp.compareTo(until) < 0): _*))
    case FetchEventsAfterTo(after, to, skip, take) ⇒
      sender() ! FetchedEvents(Enumerator(eventLog.filter(event ⇒ event.timestamp.compareTo(after) > 0 && event.timestamp.compareTo(to) <= 0): _*))
    case FetchEventsAfterUntil(after, until, skip, take) ⇒
      sender() ! FetchedEvents(Enumerator(eventLog.filter(event ⇒ event.timestamp.compareTo(after) > 0 && event.timestamp.compareTo(until) < 0): _*))
  }
}
