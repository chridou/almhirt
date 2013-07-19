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
  final def apply(command: Command, repositories: AggregateRootRepositoryRegistry): AlmFuture[String] =
    execute(command.asInstanceOf[TCom], repositories)

  def execute(command: TCom, repositories: AggregateRootRepositoryRegistry): AlmFuture[String]
}

trait DomainCommandHandler extends CommandHandler {
  type Event <: DomainEvent
  type AR <: AggregateRoot[AR, Event]
  type TCom <: DomainCommand
  def typeOfAr: Class[AR]
}

trait CreatingDomainCommandHandler extends DomainCommandHandler {
  final def apply(command: DomainCommand): AlmFuture[(IsAggregateRoot, IndexedSeq[DomainEvent])] =
    execute(command.asInstanceOf[TCom])

  def execute(command: TCom): AlmFuture[(AR, IndexedSeq[Event])]
}

trait MutatingDomainCommandHandler extends DomainCommandHandler {
  final def apply(currentState: IsAggregateRoot, command: DomainCommand): AlmFuture[(IsAggregateRoot, IndexedSeq[DomainEvent])] =
    execute(currentState.asInstanceOf[AR], command.asInstanceOf[TCom])

  def execute(currentState: AR, TCom: Command): AlmFuture[(AR, IndexedSeq[Event])]
}