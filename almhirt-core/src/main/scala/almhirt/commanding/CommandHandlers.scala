package almhirt.commanding

import almhirt.common._
import almhirt.core.Almhirt
import almhirt.domain._
import almhirt.components.AggregateRootRepositoryRegistry

/**
 * This is just a marker trait
 */
trait CommandHandler {
}

trait GenericCommandHandler extends CommandHandler {
  type TCom <: Command
  def execute(command: TCom, repositories: AggregateRootRepositoryRegistry, theAlmhirt: Almhirt): AlmFuture[Unit]
}

trait DomainCommandHandler extends CommandHandler {
  type Event <: DomainEvent
  type AR <: AggregateRoot[AR, Event]
  type TCom <: DomainCommand
}

trait CreatingDomainCommandHandler { self: DomainCommandHandler =>
  def execute(command: TCom, theAlmhirt: Almhirt): AlmFuture[UpdateRecorder[AR, Event]]
}

trait MutatingDomainCommandHandler { self: DomainCommandHandler =>
  def execute(currentState: AR, command: TCom, theAlmhirt: Almhirt): AlmFuture[UpdateRecorder[AR, Event]]
}