package almhirt.domain

import java.lang.RuntimeException
import almhirt.aggregates.AggregateRootId

abstract class AggregateRootDomainException(id: AggregateRootId, message: String, ex: Throwable) extends RuntimeException(message, ex)

final class AggregateRootEventStoreFailedReadingException private (id: AggregateRootId, message: String, ex: Throwable) extends AggregateRootDomainException(id, message, ex)
object AggregateRootEventStoreFailedReadingException {
  def apply(id: AggregateRootId, message: String): AggregateRootEventStoreFailedReadingException =
    new AggregateRootEventStoreFailedReadingException(id, message, null)
  def apply(id: AggregateRootId, message: String, ex: Throwable): AggregateRootEventStoreFailedReadingException =
    new AggregateRootEventStoreFailedReadingException(id, message, ex)
}

final class AggregateRootEventStoreFailedWritingException private (id: AggregateRootId, message: String, ex: Throwable) extends AggregateRootDomainException(id, message, ex)
object AggregateRootEventStoreFailedWritingException {
  def apply(id: AggregateRootId, message: String): AggregateRootEventStoreFailedWritingException =
    new AggregateRootEventStoreFailedWritingException(id, message, null)
  def apply(id: AggregateRootId, message: String, ex: Throwable): AggregateRootEventStoreFailedWritingException =
    new AggregateRootEventStoreFailedWritingException(id, message, ex)
}

final class RebuildAggregateRootFailedException private (id: AggregateRootId, message: String, ex: Throwable) extends AggregateRootDomainException(id, message, ex)
object RebuildAggregateRootFailedException {
  def apply(id: AggregateRootId, message: String): RebuildAggregateRootFailedException =
    new RebuildAggregateRootFailedException(id, message, null)
  def apply(id: AggregateRootId, message: String, ex: Throwable): RebuildAggregateRootFailedException =
    new RebuildAggregateRootFailedException(id, message, ex)
}

final class CouldNotDispatchAllAggregateRootEventsException private (id: AggregateRootId, commandId: almhirt.common.CommandId, message: String, ex: Throwable) extends AggregateRootDomainException(id, message, ex)
import almhirt.common._
object CouldNotDispatchAllAggregateRootEventsException {
  def apply(command: AggregateRootCommand): CouldNotDispatchAllAggregateRootEventsException =
    CouldNotDispatchAllAggregateRootEventsException(command , null)
  def apply(command: AggregateRootCommand, ex: Throwable): CouldNotDispatchAllAggregateRootEventsException = {
    val msg = s"Could not commit all events for command ${command.commandId.value} on aggregate ${command.aggId.value}."
    new CouldNotDispatchAllAggregateRootEventsException(command.aggId, command.commandId, msg, ex)
  }
}

final class WrongAggregateRootEventTypeException private (id: AggregateRootId, eventId: EventId, message: String, ex: Throwable) extends AggregateRootDomainException(id, message, ex)
import almhirt.common._
object WrongAggregateRootEventTypeException {
  def apply(event: AggregateRootEvent, tag: scala.reflect.ClassTag[_ <: AggregateRootEvent]): WrongAggregateRootEventTypeException =
    WrongAggregateRootEventTypeException(event, tag, null)
  def apply(event: AggregateRootEvent, tag: scala.reflect.ClassTag[_ <: AggregateRootEvent], ex: Throwable): WrongAggregateRootEventTypeException = {
    val msg = s"""Could not cast aggregate event of type "${event.getClass().getName()}" with id "${event.eventId.value}" on aggregate ${event.aggId} to the required type "${tag.runtimeClass}"."""
    new WrongAggregateRootEventTypeException(event.aggId, event.eventId, msg, ex)
  }
}