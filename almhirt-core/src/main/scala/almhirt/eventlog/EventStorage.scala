package almhirt.eventlog

import java.util.{ UUID => JUUID }
import org.joda.time.DateTime
import almhirt.common._
import almhirt.core.Almhirt

trait SyncEventStorage extends EventSink {
  def getEventById(id: JUUID): AlmValidation[Event]
  def getAllEvents(): AlmValidation[Iterable[Event]]
  def getAllEventsFrom(from: DateTime): AlmValidation[Iterable[Event]]
  def getAllEventsUntil(until: DateTime): AlmValidation[Iterable[Event]]
  def getAllEventsFromUntil(from: DateTime, until: DateTime): AlmValidation[Iterable[Event]]
}

trait AsyncEventStorage extends EventSink with HasEvents

object SyncEventStorage {
  def wrapAsync(syncEventStorage: SyncEventStorage)(implicit hasExecutionContext: HasExecutionContext): AsyncEventStorage = {
    new AsyncEventStorage {
      def consume(event: Event) { syncEventStorage.consume(event) }
      def getEventById(id: JUUID): AlmFuture[Event] = AlmFuture { syncEventStorage.getEventById(id) }
      def getAllEvents(): AlmFuture[Iterable[Event]] = AlmFuture { syncEventStorage.getAllEvents }
      def getAllEventsFrom(from: DateTime): AlmFuture[Iterable[Event]] = AlmFuture { syncEventStorage.getAllEventsFrom(from) }
      def getAllEventsUntil(until: DateTime): AlmFuture[Iterable[Event]] = AlmFuture { syncEventStorage.getAllEventsUntil(until) }
      def getAllEventsFromUntil(from: DateTime, until: DateTime): AlmFuture[Iterable[Event]] = AlmFuture { syncEventStorage.getAllEventsFromUntil(from, until) }
    }
  }

  implicit class SyncEventStorageOps(syncEventStorage: SyncEventStorage) {
    def toAsync(implicit hasExecutionContext: HasExecutionContext): AsyncEventStorage = wrapAsync(syncEventStorage)
    def toAsyncWithCruncher(implicit theAlmhirt: Almhirt): AsyncEventStorage = wrapAsync(syncEventStorage)(theAlmhirt.cruncher)
  }
}