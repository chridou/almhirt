package almhirt.domain

import scala.reflect.ClassTag
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.aggregates._

/**
 * Mix in this trait together with an [[almhirt.aggregates.AggregateRootCommandHandler]] into your
 *  [[AggregateRootDrone]] to make it use the mixed in [[almhirt.aggregates.AggregateRootCommandHandler]].
 */
trait AggregateRootDroneCommandHandlerAdaptor[T <: AggregateRoot, C <: AggregateRootCommand, E <: AggregateRootEvent] { self: AggregateRootDrone[T, E] with AggregateRootCommandHandler[T, C, E] ⇒
  implicit def aggregateCommandValidator: AggregateRootCommandValidator
  implicit def tag: ClassTag[C]
  final override val handleAggregateCommand: ConfirmationContext[E] ⇒ (AggregateRootCommand, AggregateRootLifecycle[T]) ⇒ Unit =
    (ctx) ⇒ (nextCommand, currentState) ⇒ {
      nextCommand.castTo[C].fold(
        fail ⇒ SyncCommandResult(ConstraintViolatedProblem(s"""Invalid command. It must be of type "${tag.runtimeClass.getName()}"."""", cause = Some(fail)).failure),
        castedCmd ⇒ this.handleAggregateCommand(castedCmd, currentState))
        .onComplete(
          fail ⇒ ctx.reject(fail),
          (_, events) ⇒ ctx.commit(events))(this.futuresContext)
    }
}