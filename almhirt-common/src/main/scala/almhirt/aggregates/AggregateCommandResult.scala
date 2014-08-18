package almhirt.aggregates

import almhirt.common._

trait AggregateCommandResult[T <: AggregateRoot, E <: AggregateEvent]

case class SyncCommandResult[T <: AggregateRoot, E <: AggregateEvent](r: AlmValidation[(AggregateRootLifecycle[T], Seq[E])]) extends AggregateCommandResult[T, E]
case class AsyncCommandResult[T <: AggregateRoot, E <: AggregateEvent](r: AlmFuture[(AggregateRootLifecycle[T], Seq[E])]) extends AggregateCommandResult[T, E]
