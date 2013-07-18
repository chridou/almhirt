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
  final def apply(command: Command, repositories: AggregateRootRepositoryRegistry, theAlmhirt: Almhirt): AlmFuture[String] =
    execute(command.asInstanceOf[TCom], repositories, theAlmhirt)

  def execute(command: TCom, repositories: AggregateRootRepositoryRegistry, theAlmhirt: Almhirt): AlmFuture[String]
}

trait DomainCommandHandler extends CommandHandler {
  type Event <: DomainEvent
  type AR <: AggregateRoot[AR, Event]
  type TCom <: DomainCommand
  def typeOfAr: Class[AR]
}

trait CreatingDomainCommandHandler extends DomainCommandHandler {
  final def apply(command: DomainCommand, theAlmhirt: Almhirt): AlmFuture[(AR, IndexedSeq[Event])] =
    execute(command.asInstanceOf[TCom], theAlmhirt)

  def execute(command: TCom, theAlmhirt: Almhirt): AlmFuture[(AR, IndexedSeq[Event])]
}

trait MutatingDomainCommandHandler extends DomainCommandHandler {
  final def apply(currentState: IsAggregateRoot, command: DomainCommand, theAlmhirt: Almhirt): AlmFuture[(AR, IndexedSeq[Event])] =
    execute(currentState.asInstanceOf[AR], command.asInstanceOf[TCom], theAlmhirt)

  def execute(currentState: AR, TCom: Command, theAlmhirt: Almhirt): AlmFuture[(AR, IndexedSeq[Event])]
}