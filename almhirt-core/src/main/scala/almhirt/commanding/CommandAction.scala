package almhirt.commanding

import almhirt.domain.AggregateRoot
import almhirt.domain.DomainEvent

trait CommandAction {
  type Event <: DomainEvent
  type AR <: AggregateRoot[AR, Event]

}

object CommandAction {
  implicit class CommanActionOps(act: CommandAction) {
    def isCreator = act.isInstanceOf[CreatorCommandAction]
    def isMutator = act.isInstanceOf[MutatorCommandAction]
  }
}
trait MutatorCommandAction extends CommandAction

trait CreatorCommandAction extends CommandAction
