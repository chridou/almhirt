package almhirt.aggregates

import scala.concurrent.ExecutionContext
import scalaz._, Scalaz._
import almhirt.common._

trait UserCommandHandler extends VersionCheckingAggregateRootCommandHandler[User, UserEvent] with AggregateRootCommandHandler[User, UserEvent] {self: UserUpdater =>
  implicit def futuresContext: ExecutionContext

  private def expensiveServiceCallForCreditCard(age: Int): AlmFuture[Boolean] =
    AlmFuture.compute(age >= 21)

  def handleValidatedAggregateCommand(command: AggregateCommand, agg: AggregateRootLifecycle[User]): AggregateCommandResult[User, UserEvent] =
    (agg, command) match {
      case (Vacat, CreateUser(_, aggId, aggVersion, surname, lastname)) =>
        create(aggId, surname, lastname).recordings.fold(
          fail => SyncCommandResult(fail.failure),
          succ => SyncCommandResult((succ).success))

      case (Vacat, RejectUser(_, aggId, aggVersion, surname, lastname)) =>
        doNotAccept(aggId, surname, lastname).syncResult

      case (Vivus(agg), ChangeUserSurname(_, _, _, surname)) =>
        changeSurname(agg, surname).syncResult

      case (Vivus(agg), ChangeUserLastname(_, _, _, lastname)) =>
        sync { changeLastname(agg, lastname) }

      case (Vivus(agg), ChangeUserFullName(_, _, _, surname, lastname)) =>
        AlmFuture.compute { changeFullName(agg, surname, lastname) }.asyncResult

      case (Vivus(agg), ChangeUserAge(_, _, _, age)) =>
        asyncCompute { changeAge(agg, age) }

      case (Vivus(agg), ChangeUserAgeForCreditCard(_, _, _, age)) =>
        async {
          for {
            goodForCC <- expensiveServiceCallForCreditCard(age)
            res <- AlmFuture.compute {
              if (goodForCC) {
                changeAge(agg, age)
              } else {
                UpdateRecorder.reject(UnspecifiedProblem("You are a kid. No credit card!"))
              }
            }
          } yield res
        }
        
      case (Vivus(agg), ConfirmUserCancelled(_, _, _)) =>
        sync { leave(agg) }

      case (Vivus(agg), ConfirmUserDeath(_, _, _)) =>
        sync { die(agg) }

      case (aggState, UserUow(_, _, _, cmds)) =>
        chained(aggState, cmds)
      
      case (aggState, cmd) =>
        SyncCommandResult(IllegalOperationProblem(s"Could not handle command $cmd").failure)
    }
}