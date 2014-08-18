package almhirt.aggregates

import scalaz._, Scalaz._
import almhirt.common._

/** Mix in this trait to get a basic validation on incoming commands.
 *  The overridden handler from [[AggregateRootCommandHandler]] calls the handler you have to provide.
 *  Before calling your handler, the overridden handler will check whether versions and ids match and whether
 *  the command can be handled due to the current lifecycle state of the aggregate root.
 */
trait VersionCheckingAggregateRootCommandHandler[T <: AggregateRoot, E <: AggregateEvent] { self: AggregateRootCommandHandler[T, E] =>
  /** Implement this handler to handle prevalidated commands */
  def handleValidatedAggregateCommand(command: AggregateCommand, agg: AggregateRootLifecycle[T]): AggregateCommandResult[T, E]

  final override def handleAggregateCommand(command: AggregateCommand, agg: AggregateRootLifecycle[T]): AggregateCommandResult[T, E] =
    agg match {
      case Vacat =>
        if (command.aggVersion != AggregateRootVersion(0L))
          SyncCommandResult(
            IllegalOperationProblem(
              s"Aggregate root does not yet exist yet. So the first command must target version 0.").failure)
        else
          handleValidatedAggregateCommand(command, agg)
      case p: Postnatalis[T] =>
        if (command.aggId != p.id) {
          SyncCommandResult(
            IllegalOperationProblem(
              s"Ids do not match: Command(=>${command.aggId.value}) vs Aggregate(${p.id.value}).").failure)
        } else {
          p match {
            case v: Vivus[T] =>
              if (command.aggVersion != v.version)
                SyncCommandResult(
                  IllegalOperationProblem(
                    s"Versions do not match: Command(=>${command.aggVersion.value}) vs Aggregate(${v.version.value}).").failure)
              else
                handleValidatedAggregateCommand(command, agg)
            case m: Mortuus =>
              SyncCommandResult(IllegalOperationProblem(s"The aggregate root is already dead.").failure)
          }
        }
    }
}