package almhirt.commanding

import almhirt.domain.AggregateRoot
import almhirt.domain.DomainEvent

trait BoundDomainCommand extends DomainCommand {
  type TEvent <: DomainEvent
  type TAR <: AggregateRoot[TAR, TEvent]
  type TAction <: CommandAction { type Event = TEvent; type AR = TAR }
  def aggRootRef: Option[AggregateRootRef]

  def actions: List[TAction]
}