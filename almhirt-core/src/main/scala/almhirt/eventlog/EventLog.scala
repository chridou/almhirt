package almhirt.eventlog

import java.util.{ UUID => JUUID }
import org.joda.time.DateTime
import almhirt.core.Event
import almhirt.common.AlmFuture

trait CanStoreEvents {
  def storeEvent(event: Event): AlmFuture[Event]
}

trait HasEvents {
  def getAllEvents: AlmFuture[Iterable[Event]]
  def getEvent(eventId: JUUID): AlmFuture[Event]
  def getEventsFrom(from: DateTime): AlmFuture[Iterable[Event]]
  def getEventsUntil(until: DateTime): AlmFuture[Iterable[Event]]
  def getEventsFromUntil(from: DateTime, until: DateTime): AlmFuture[Iterable[Event]]
}

trait EventLog extends CanStoreEvents with HasEvents with almhirt.almakka.ActorBased