package almhirt.aggregates

import scala.concurrent.ExecutionContext
import scalaz._, Scalaz._
import almhirt.common._

/**
 * Mix in this trait to get helpers for handling commands on Aggregate roots.
 *  This trait doesn't validate the command in any way.
 */
trait AggregateRootCommandHandler[T <: AggregateRoot, E <: AggregateRootEvent] {
  /** This method has to be overridden with your logic to handle commands. */
  def handleAggregateCommand(command: AggregateRootCommand, agg: AggregateRootLifecycle[T]): AggregateCommandResult[T, E]

  implicit class FutureFOps(self: AlmFuture[(AggregateRootLifecycle[T], Seq[E])]) {
    /** Turn the AlmFuture of (AggregateRootLifecycle[T], Seq[E]) into an [[AggregateCommandResult]] */
    def asyncResult: AsyncCommandResult[T, E] =
      AsyncCommandResult(self)
  }

  implicit class UpdateRecorderOps[T <: AggregateRoot, E <: AggregateRootEvent](self: UpdateRecorder[T, E]) {
    /** Turn the [[UpdateRecorder]] into an [[AggregateCommandResult]] */
    def syncResult: SyncCommandResult[T, E] =
      self.recordings.fold(
        fail ⇒ SyncCommandResult[T, E](fail.failure),
        succ ⇒ SyncCommandResult[T, E](succ.success))
  }

  implicit class FutureFUROps(self: AlmFuture[UpdateRecorder[T, E]]) {
    /** Turn the AlmFuture of an [[UpdateRecorder]] into an [[AggregateCommandResult]] */
    def asyncResult(implicit executionContext: ExecutionContext): AsyncCommandResult[T, E] =
      AsyncCommandResult(self.mapV(_.recordings))
  }

  implicit class ResultOps(self: AggregateCommandResult[T, E]) {
    /**
     * Prepend the events to the events of this [[AggregateCommandResult]].
     *  Do that if and only if the current [[AggregateCommandResult]] is not a failure.
     */
    def prependEvents(events: Seq[E])(implicit executionContext: ExecutionContext): AggregateCommandResult[T, E] =
      self match {
        case SyncCommandResult(res) ⇒
          res.fold(
            fail ⇒ SyncCommandResult(fail.failure),
            succ ⇒ SyncCommandResult((succ._1, events ++ succ._2).success))
        case AsyncCommandResult(resF) ⇒
          AsyncCommandResult(resF.foldV(
            fail ⇒ fail.failure,
            succ ⇒ (succ._1, events ++ succ._2).success))
      }

    /**
     * Execute the next result and combine it's result with this result.
     *  Do that if and only if the current [[AggregateCommandResult]] is not a failure.
     */
    def andThen(nextCommand: AggregateRootCommand)(implicit executionContext: ExecutionContext): AggregateCommandResult[T, E] =
      self match {
        case SyncCommandResult(res) ⇒
          res.fold(
            fail ⇒ SyncCommandResult(fail.failure),
            succ ⇒ {
              val (currentState, eventsSoFar) = succ
              handleAggregateCommand(nextCommand, currentState).prependEvents(eventsSoFar)
            })
        case AsyncCommandResult(resF) ⇒
          AsyncCommandResult(resF.flatMap {
            case (currentState, eventsSoFar) ⇒
              handleAggregateCommand(nextCommand, currentState).prependEvents(eventsSoFar) match {
                case SyncCommandResult(res) ⇒ AlmFuture.completed(res)
                case AsyncCommandResult(resF) ⇒ resF
              }
          })
      }
  }

  /**
   * Execute the given commands on the initial state as long as none of the commands results in a failure.
   * If any command results in a failure the whole operation is a failure.
   */
  protected def chained(initialState: AggregateRootLifecycle[T], commands: Seq[AggregateRootCommand])(implicit executionContext: ExecutionContext): AggregateCommandResult[T, E] =
    commands.foldLeft(SyncCommandResult((initialState, Seq[E]()).success): AggregateCommandResult[T, E]) {
      case (acc, cur) ⇒
        acc.andThen(cur)
    }

  /** Turn a block that ends in an [[UpdateRecorder]] into an [[AggregateCommandResult]] */
  protected def sync(ur: ⇒ UpdateRecorder[T, E]) =
    ur.syncResult

  /**
   * Turn a block that ends in an [[UpdateRecorder]] into an [[AggregateCommandResult]] but perform
   * the handling on a different thread.
   * Use this for handlers that perform a cpu bound operation to not
   * stress the thread the handler is called on.
   */
  protected def asyncCompute(ur: ⇒ UpdateRecorder[T, E])(implicit executionContext: ExecutionContext) =
    AlmFuture.compute(ur).asyncResult

  /**
   * Turn a block that ends in an AlmFuture of an [[UpdateRecorder]] into an [[AggregateCommandResult]]
   *  Use this for handlers that call external services.
   */
  protected def async(ur: ⇒ AlmFuture[UpdateRecorder[T, E]])(implicit executionContext: ExecutionContext) =
    ur.asyncResult
}