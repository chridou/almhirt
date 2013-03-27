package almhirt.eventlog.util

import java.util.{ UUID => JUUID }
import org.joda.time.DateTime
import almhirt.common._
import almhirt.core.Event


trait SyncEventStorage {
  def storeEvent(event: Event): AlmValidation[Event]
  def getEventById(id: JUUID): AlmValidation[Event]
  def getAllEvents(): AlmValidation[Iterable[Event]]
  def getAllEvents(id: JUUID): AlmValidation[Iterable[Event]]
  def getAllEventsFrom(from: DateTime): AlmValidation[Iterable[Event]]
  def getAllEventsUntil(until: DateTime): AlmValidation[Iterable[Event]]
  def getAllEventsFromUntil(from: DateTime, until: DateTime): AlmValidation[Iterable[Event]]
}

trait AsyncEventStorage {
  def storeEvent(event: Event): AlmFuture[Event]
  def getEventById(id: JUUID): AlmFuture[Event]
  def getAllEvents(): AlmFuture[Iterable[Event]]
  def getAllEvents(id: JUUID): AlmFuture[Iterable[Event]]
  def getAllEventsFrom(from: DateTime): AlmFuture[Iterable[Event]]
  def getAllEventsUntil(until: DateTime): AlmFuture[Iterable[Event]]
  def getAllEventsFromUntil(from: DateTime, until: DateTime): AlmFuture[Iterable[Event]]
}