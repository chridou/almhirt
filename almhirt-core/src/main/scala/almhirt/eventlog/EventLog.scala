package almhirt.eventlog

import java.util.{ UUID => JUUID }
import org.joda.time.DateTime
import almhirt.core.Event
import almhirt.common.AlmFuture

trait EventLog {
  def storeEvent(event: Event): AlmFuture[Event]
  def getAllEvents: AlmFuture[Iterable[Event]]
  def getEvent(eventId: JUUID): AlmFuture[Event]
  def getEventsFrom(from: DateTime): AlmFuture[Iterable[Event]]
  def getEventsUntil(until: DateTime): AlmFuture[Iterable[Event]]
  def getEventsFromUntil(from: DateTime, until: DateTime): AlmFuture[Iterable[Event]]
}