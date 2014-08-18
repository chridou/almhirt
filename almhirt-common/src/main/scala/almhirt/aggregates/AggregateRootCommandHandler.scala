package almhirt.aggregates

import almhirt.common._

trait AggregateRootCommandHandler[T <: AggregateRoot, E <: AggregateEvent] {
  def handleAggregateCommand(command: AggregateCommand): AggregateCommandResult[T, E]
}