package almhirt.eventlog

import java.util.{ UUID => JUUID }
import org.joda.time.DateTime
import almhirt.common._

trait HasEvents {
  def getEventById(id: JUUID): AlmFuture[Event]
  def getAllEvents(): AlmFuture[Iterable[Event]]
  def getAllEventsFrom(from: DateTime): AlmFuture[Iterable[Event]]
  def getAllEventsUntil(until: DateTime): AlmFuture[Iterable[Event]]
  def getAllEventsFromUntil(from: DateTime, until: DateTime): AlmFuture[Iterable[Event]]
}

trait EventLog extends AsyncEventStorage with almhirt.almakka.ActorBased