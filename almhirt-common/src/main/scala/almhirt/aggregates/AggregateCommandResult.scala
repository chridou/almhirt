package almhirt.aggregates

import almhirt.common._

trait AggregateCommandResult[T <: AggregateRoot, E <: AggregateEvent]

case class SyncResult[T <: AggregateRoot, E <: AggregateEvent](r: AlmValidation[(T, Seq[E])]) extends AggregateCommandResult[T, E]
case class AsyncResult[T <: AggregateRoot, E <: AggregateEvent](r: AlmFuture[(T, Seq[E])]) extends AggregateCommandResult[T, E]