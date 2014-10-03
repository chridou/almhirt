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
  import almhirt.common.LocalDateTimeRange._

  private var eventLog = Vector.empty[Event]

  def receive: Receive = {
    case LogEvent(event, acknowledge) ⇒
      eventLog = (eventLog :+ event).sortBy(_.timestamp)
      if (acknowledge)
        sender() ! EventLogged(event.eventId)
    case FindEvent(eventId) ⇒
      sender() ! FoundEvent(eventId, eventLog.find(_.eventId == eventId))
      
    case FetchEventsParts(BeginningOfTime, EndOfTime, skip, length) =>
      sender() ! FetchedEvents(Enumerator(eventLog: _*))
      
    case FetchEventsParts(From(from), EndOfTime, skip, length) =>
      sender() ! FetchedEvents(Enumerator(eventLog.filter(event ⇒ event.timestamp.compareTo(from) >= 0): _*))
      
    case FetchEventsParts(After(after), EndOfTime, skip, length) =>
      sender() ! FetchedEvents(Enumerator(eventLog.filter(event ⇒ event.timestamp.compareTo(after) > 0): _*))
      
    case FetchEventsParts(BeginningOfTime, To(to), skip, length) =>
      sender() ! FetchedEvents(Enumerator(eventLog.filter(event ⇒ event.timestamp.compareTo(to) <= 0): _*))
      
    case FetchEventsParts(BeginningOfTime, Until(until), skip, length) =>
      sender() ! FetchedEvents(Enumerator(eventLog.filter(event ⇒ event.timestamp.compareTo(until) < 0): _*))
      
    case FetchEventsParts(From(from), To(to), skip, length) =>
      sender() ! FetchedEvents(Enumerator(eventLog.filter(event ⇒ event.timestamp.compareTo(from) >= 0 && event.timestamp.compareTo(to) <= 0): _*))
      
    case FetchEventsParts(From(from), Until(until), skip, length) =>
      sender() ! FetchedEvents(Enumerator(eventLog.filter(event ⇒ event.timestamp.compareTo(from) >= 0 && event.timestamp.compareTo(until) < 0): _*))
      
    case FetchEventsParts(After(after), To(to), skip, length) =>
      sender() ! FetchedEvents(Enumerator(eventLog.filter(event ⇒ event.timestamp.compareTo(after) > 0 && event.timestamp.compareTo(to) <= 0): _*))
      
    case FetchEventsParts(After(after), Until(until), skip, length) =>
      sender() ! FetchedEvents(Enumerator(eventLog.filter(event ⇒ event.timestamp.compareTo(after) > 0 && event.timestamp.compareTo(until) < 0): _*))
      
  }
}
