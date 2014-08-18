package almhirt.aggregates

import scala.concurrent.ExecutionContext
import scalaz._, Scalaz._
import almhirt.common._

trait AggregateRootCommandHandler[T <: AggregateRoot, E <: AggregateEvent] {
  implicit class FutureFOps(self: AlmFuture[(AggregateRootLifecycle[T], Seq[E])]) {
    def asyncResult: AsyncCommandResult[T, E] =
      AsyncCommandResult(self)
  }

  implicit class UpdateRecorderOps[T <: AggregateRoot, E <: AggregateEvent](self: UpdateRecorder[T, E]) {
    def syncResult: SyncCommandResult[T, E] =
      self.recordings.fold(
        fail => SyncCommandResult[T, E](fail.failure),
        succ => SyncCommandResult[T, E](succ.success))
  }

  implicit class FutureFUROps(self: AlmFuture[UpdateRecorder[T, E]]) {
    def asyncResult(implicit executionContext: ExecutionContext): AsyncCommandResult[T, E] =
      AsyncCommandResult(self.mapV(_.recordings))
  }

  implicit class ResultOps(self: AggregateCommandResult[T, E]) {
    def prependEvents(events: Seq[E])(implicit executionContext: ExecutionContext): AggregateCommandResult[T, E] =
      self match {
        case SyncCommandResult(res) =>
          res.fold(
            fail => SyncCommandResult(fail.failure),
            succ => SyncCommandResult((succ._1, events ++ succ._2).success))
        case AsyncCommandResult(resF) =>
          AsyncCommandResult(resF.foldV(
            fail => fail.failure,
            succ => (succ._1, events ++ succ._2).success))
      }

    def andThen(command: AggregateCommand)(implicit executionContext: ExecutionContext): AggregateCommandResult[T, E] =
      self match {
        case SyncCommandResult(res) =>
          res.fold(
            fail => SyncCommandResult(fail.failure),
            succ => {
              val (currentState, eventsSoFar) = succ
              handleAggregateCommand(command, currentState).prependEvents(eventsSoFar)
            })
        case AsyncCommandResult(resF) =>
          AsyncCommandResult(resF.flatMap {
            case (currentState, eventsSoFar) =>
              handleAggregateCommand(command, currentState).prependEvents(eventsSoFar) match {
                case SyncCommandResult(res) => AlmFuture.completed(res)
                case AsyncCommandResult(resF) => resF
              }
          })
      }
  }
  
  protected def chained(state: AggregateRootLifecycle[T], commands: Seq[AggregateCommand])(implicit executionContext: ExecutionContext): AggregateCommandResult[T, E] = 
    commands.foldLeft(SyncCommandResult((state, Seq[E]()).success): AggregateCommandResult[T, E]){ case (acc, cur) =>
      acc.andThen(cur)}

  protected def sync(ur: => UpdateRecorder[T, E]) =
    ur.syncResult

  protected def asyncCompute(ur: => UpdateRecorder[T, E])(implicit executionContext: ExecutionContext) =
    AlmFuture.compute(ur).asyncResult

  protected def async(ur: => AlmFuture[UpdateRecorder[T, E]])(implicit executionContext: ExecutionContext) =
    ur.asyncResult

  def handleAggregateCommand(command: AggregateCommand, agg: AggregateRootLifecycle[T]): AggregateCommandResult[T, E]
}