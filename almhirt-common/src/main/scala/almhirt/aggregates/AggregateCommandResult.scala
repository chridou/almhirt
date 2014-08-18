package almhirt.aggregates

import almhirt.common._

trait AggregateCommandResult[T <: AggregateRoot, E <: AggregateEvent]

case class SyncCommandResult[T <: AggregateRoot, E <: AggregateEvent](r: AlmValidation[(AggregateRootLifecycle[T], Seq[E])]) extends AggregateCommandResult[T, E]
case class AsyncCommandResult[T <: AggregateRoot, E <: AggregateEvent](r: AlmFuture[(AggregateRootLifecycle[T], Seq[E])]) extends AggregateCommandResult[T, E]

object AggregateCommandResult {
  import scala.concurrent.ExecutionContext
  import scala.concurrent.duration.FiniteDuration
  implicit class AggregateCommandResultOps[T <: AggregateRoot, E <: AggregateEvent](self: AggregateCommandResult[T, E]) {
    def await(atMost: FiniteDuration): AlmValidation[(AggregateRootLifecycle[T], Seq[E])] =
      self match {
        case SyncCommandResult(res) => res
        case AsyncCommandResult(resF) => resF.awaitResult(atMost)
      }
  }
}