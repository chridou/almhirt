package almhirt.aggregates

import scalaz._, Scalaz._
import almhirt.common._

/** This is a strategy on how to validate a command */
trait AggregateRootCommandValidator {
  def checkCommand[TCmd <: AggregateRootCommand](command: TCmd, ar: AggregateRootLifecycle[AggregateRoot]): AlmValidation[TCmd]
  final def handleIfValid[TAr <: AggregateRoot, TCmd <: AggregateRootCommand, E <: AggregateRootEvent](command: TCmd, ar: AggregateRootLifecycle[TAr])(handle: (TCmd, AggregateRootLifecycle[TAr]) ⇒ AggregateCommandResult[TAr, E]) =
    checkCommand(command, ar).fold(
      problem ⇒ SyncCommandResult(problem.failure),
      res ⇒ handle(command, ar))
}

object AggregateRootCommandValidator {
  object NoValidation extends AggregateRootCommandValidator {
    override def checkCommand[TCmd <: AggregateRootCommand](command: TCmd, ar: AggregateRootLifecycle[AggregateRoot]): AlmValidation[TCmd] =
      command.success
  }

  object ValidatedLifecycle extends AggregateRootCommandValidator {
    override def checkCommand[TCmd <: AggregateRootCommand](command: TCmd, ar: AggregateRootLifecycle[AggregateRoot]): AlmValidation[TCmd] =
      ar match {
        case m: Mortuus ⇒
          IllegalOperationProblem(s"The aggregate root is already dead.").failure
        case _ ⇒
          command.success
      }
  }

  object Validated extends AggregateRootCommandValidator {
    override def checkCommand[TCmd <: AggregateRootCommand](command: TCmd, ar: AggregateRootLifecycle[AggregateRoot]): AlmValidation[TCmd] =
      ar match {
        case Vacat ⇒
          if (command.aggVersion != AggregateRootVersion(0L))
            IllegalOperationProblem(s"Aggregate root does not yet exist yet. The first command must target version 0.").failure
          else
            command.success
        case p: Postnatalis[AggregateRoot] ⇒
          if (command.aggId != p.id) {
            IllegalOperationProblem(s"Ids do not match: Command[${command.getClass().getName()}](${command.aggId.value}) vs Aggregate(${p.id.value}).").failure
          } else {
            p match {
              case v: Vivus[AggregateRoot] ⇒
                if (command.aggVersion != v.version)
                  VersionConflictProblem(
                    s"Versions do not match: Command(⇒${command.aggVersion.value}) vs Aggregate(${v.version.value}).").failure
                else
                  command.success
              case m: Mortuus ⇒
                IllegalOperationProblem(s"The aggregate root is already dead.").failure
            }
          }
      }
  }

  object ValidatedId extends AggregateRootCommandValidator {
    override def checkCommand[TCmd <: AggregateRootCommand](command: TCmd, ar: AggregateRootLifecycle[AggregateRoot]): AlmValidation[TCmd] =
      ar match {
        case Vacat ⇒
          command.success
        case postnatalis: Postnatalis[AggregateRoot] ⇒
          if (command.aggId != postnatalis.id) {
            IllegalOperationProblem(s"Ids do not match: Command(${command.aggId.value}) vs Aggregate(${postnatalis.id.value}).").failure
          } else {
            postnatalis match {
              case v: Vivus[AggregateRoot] ⇒
                command.success
              case m: Mortuus ⇒
                IllegalOperationProblem(s"The aggregate root is already dead.").failure
            }
          }
      }
  }

}