package almhirt.domain

import almhirt.common._
import almhirt.aggregates._

/** Mix in this trait together with an [[almhirt.aggregates.AggregateRootCommandHandler]] into your
 *  [[AggregateDrone]] to make it use the mixed in [[almhirt.aggregates.AggregateRootCommandHandler]].
 */
trait AggregateDroneCommandHandlerAdaptor[T <: AggregateRoot, E <: AggregateEvent] { self: AggregateDrone[T, E] with AggregateRootCommandHandler[T, E] =>
  final override val handleAggregateCommand: ConfirmationContext[E] ⇒ (AggregateCommand, AggregateRootLifecycle[T]) ⇒ Unit =
    (ctx) => (currentState, nextCommand) => {
      this.handleAggregateCommand(currentState, nextCommand).onComplete {
        ctx.reject
      } { case (_, events) => ctx.commit(events) }(this.futuresContext)
    }
}