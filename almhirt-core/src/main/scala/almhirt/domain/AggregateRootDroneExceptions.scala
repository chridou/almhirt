package almhirt.domain

import java.lang.RuntimeException
import almhirt.aggregates.AggregateRootId

abstract class AggregateRootDroneException(id: AggregateRootId, message: String, ex: Throwable) extends RuntimeException(message, ex)

final class AggregateRootDeletedException private (id: AggregateRootId, message: String, ex: Throwable) extends AggregateRootDroneException(id, message, ex)
object AggregateRootDeletedException {
  def apply(id: AggregateRootId, message: String): AggregateRootDeletedException =
    new AggregateRootDeletedException(id, message, null)
  def apply(id: AggregateRootId, message: String, ex: Throwable): AggregateRootDeletedException =
    new AggregateRootDeletedException(id, message, ex)
}

final class AggregateEventStoreFailedReadingException private (id: AggregateRootId, message: String, ex: Throwable) extends AggregateRootDroneException(id, message, ex)
object AggregateEventStoreFailedReadingException {
  def apply(id: AggregateRootId, message: String): AggregateEventStoreFailedReadingException =
    new AggregateEventStoreFailedReadingException(id, message, null)
  def apply(id: AggregateRootId, message: String, ex: Throwable): AggregateEventStoreFailedReadingException =
    new AggregateEventStoreFailedReadingException(id, message, ex)
}

final class AggregateEventStoreFailedWritingException private (id: AggregateRootId, message: String, ex: Throwable) extends AggregateRootDroneException(id, message, ex)
object AggregateEventStoreFailedWritingException {
  def apply(id: AggregateRootId, message: String): AggregateEventStoreFailedWritingException =
    new AggregateEventStoreFailedWritingException(id, message, null)
  def apply(id: AggregateRootId, message: String, ex: Throwable): AggregateEventStoreFailedWritingException =
    new AggregateEventStoreFailedWritingException(id, message, ex)
}

final class RebuildAggregateRootFailedException private (id: AggregateRootId, message: String, ex: Throwable) extends AggregateRootDroneException(id, message, ex)
object RebuildAggregateRootFailedException {
  def apply(id: AggregateRootId, message: String): RebuildAggregateRootFailedException =
    new RebuildAggregateRootFailedException(id, message, null)
  def apply(id: AggregateRootId, message: String, ex: Throwable): RebuildAggregateRootFailedException =
    new RebuildAggregateRootFailedException(id, message, ex)
}

