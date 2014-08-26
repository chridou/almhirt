package almhirt.domain

import java.lang.RuntimeException
import almhirt.aggregates.AggregateRootId

abstract class AggregateRootDomainException(id: AggregateRootId, message: String, ex: Throwable) extends RuntimeException(message, ex)

final class AggregateEventStoreFailedReadingException private (id: AggregateRootId, message: String, ex: Throwable) extends AggregateRootDomainException(id, message, ex)
object AggregateEventStoreFailedReadingException {
  def apply(id: AggregateRootId, message: String): AggregateEventStoreFailedReadingException =
    new AggregateEventStoreFailedReadingException(id, message, null)
  def apply(id: AggregateRootId, message: String, ex: Throwable): AggregateEventStoreFailedReadingException =
    new AggregateEventStoreFailedReadingException(id, message, ex)
}

final class AggregateEventStoreFailedWritingException private (id: AggregateRootId, message: String, ex: Throwable) extends AggregateRootDomainException(id, message, ex)
object AggregateEventStoreFailedWritingException {
  def apply(id: AggregateRootId, message: String): AggregateEventStoreFailedWritingException =
    new AggregateEventStoreFailedWritingException(id, message, null)
  def apply(id: AggregateRootId, message: String, ex: Throwable): AggregateEventStoreFailedWritingException =
    new AggregateEventStoreFailedWritingException(id, message, ex)
}

final class RebuildAggregateRootFailedException private (id: AggregateRootId, message: String, ex: Throwable) extends AggregateRootDomainException(id, message, ex)
object RebuildAggregateRootFailedException {
  def apply(id: AggregateRootId, message: String): RebuildAggregateRootFailedException =
    new RebuildAggregateRootFailedException(id, message, null)
  def apply(id: AggregateRootId, message: String, ex: Throwable): RebuildAggregateRootFailedException =
    new RebuildAggregateRootFailedException(id, message, ex)
}

final class CouldNotDispatchAllAggregateEventsException private (id: AggregateRootId, commandId: almhirt.common.CommandId, message: String, ex: Throwable) extends AggregateRootDomainException(id, message, ex)
import almhirt.common._
object CouldNotDispatchAllAggregateEventsException {
  def apply(command: AggregateCommand): CouldNotDispatchAllAggregateEventsException =
    CouldNotDispatchAllAggregateEventsException(command , null)
  def apply(command: AggregateCommand, ex: Throwable): CouldNotDispatchAllAggregateEventsException = {
    val msg = s"Could not commit all events for command ${command.commandId.value} on aggregate ${command.aggId.value}."
    new CouldNotDispatchAllAggregateEventsException(command.aggId, command.commandId, msg, ex)
  }
}

final class WrongAggregateEventTypeException private (id: AggregateRootId, eventId: EventId, message: String, ex: Throwable) extends AggregateRootDomainException(id, message, ex)
import almhirt.common._
object WrongAggregateEventTypeException {
  def apply(event: AggregateEvent, tag: scala.reflect.ClassTag[_ <: AggregateEvent]): WrongAggregateEventTypeException =
    WrongAggregateEventTypeException(event, tag, null)
  def apply(event: AggregateEvent, tag: scala.reflect.ClassTag[_ <: AggregateEvent], ex: Throwable): WrongAggregateEventTypeException = {
    val msg = s"""Could not cast aggregate event of type "${event.getClass().getName()}" with id "${event.eventId.value}" on aggregate ${event.aggId} to the required type "${tag.runtimeClass}"."""
    new WrongAggregateEventTypeException(event.aggId, event.eventId, msg, ex)
  }
}