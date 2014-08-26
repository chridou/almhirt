package almhirt.domain

import almhirt.common._
import almhirt.aggregates._

/** Mix in this trait together with an [[almhirt.aggregates.AggregateRootCommandHandler]] into your
 *  [[AggregateRootDrone]] to make it use the mixed in [[almhirt.aggregates.AggregateRootCommandHandler]].
 */
trait AggregateRootDroneCommandHandlerAdaptor[T <: AggregateRoot, E <: AggregateRootEvent] { self: AggregateRootDrone[T, E] with AggregateRootCommandHandler[T, E] ⇒
  final override val handleAggregateCommand: ConfirmationContext[E] ⇒ (AggregateCommand, AggregateRootLifecycle[T]) ⇒ Unit =
    (ctx) ⇒ (currentState, nextCommand) ⇒ {
      this.handleAggregateCommand(currentState, nextCommand).onComplete {
        ctx.reject
      } { case (_, events) ⇒ ctx.commit(events) }(this.futuresContext)
    }
}