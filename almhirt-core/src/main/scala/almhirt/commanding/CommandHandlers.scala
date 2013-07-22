package almhirt.commanding

import scala.reflect.ClassTag
import scala.concurrent.ExecutionContext
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
  final def apply(command: Command): AlmFuture[String] =
    execute(command.asInstanceOf[TCom])

  def execute(command: TCom): AlmFuture[String]
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

  def execute(currentState: AR, command: TCom): AlmFuture[(AR, IndexedSeq[DomainEvent])]
}

object GenericCommandHandler {
  def fromSyncFun[TCommand <: Command](syncExecute: TCommand => AlmValidation[String], executionContext: ExecutionContext): GenericCommandHandler { type TCom = TCommand } =
    new GenericCommandHandler {
      type TCom = TCommand
      override def execute(command: TCom) = AlmFuture { syncExecute(command) }(executionContext)
    }

  def createRegistryAdderFromSyncFun[TCommand <: Command](syncExecute: TCommand => AlmValidation[String], executionContext: ExecutionContext)(implicit tagC: ClassTag[TCommand]): CommandHandlerRegistry => CommandHandlerRegistry =
    (registry: CommandHandlerRegistry) => {
      registry.addGenericCommandHandler[TCommand](fromSyncFun(syncExecute, executionContext))
      registry
    }
}

object CreatingDomainCommandHandler {
  def fromSyncFun[TCommand <: DomainCommand, TEvent <: DomainEvent, TAR <: AggregateRoot[TAR, TEvent]](syncExecute: TCommand => AlmValidation[(TAR, IndexedSeq[TEvent])], executionContext: ExecutionContext)(implicit tag: ClassTag[TAR]): CreatingDomainCommandHandler { type TCom = TCommand; type Event = TEvent; type AR = TAR } =
    new CreatingDomainCommandHandler {
      type TCom = TCommand
      type Event = TEvent
      type AR = TAR
      val typeOfAr = tag.runtimeClass.asInstanceOf[Class[AR]]
      override def execute(command: TCom) = AlmFuture { syncExecute(command) }(executionContext)
    }

  def createRegistryAdderFromSyncFun[TCommand <: DomainCommand, TEvent <: DomainEvent, TAR <: AggregateRoot[TAR, TEvent]](syncExecute: TCommand => AlmValidation[(TAR, IndexedSeq[TEvent])], executionContext: ExecutionContext)(implicit tagAr: ClassTag[TAR], tagC: ClassTag[TCommand]): CommandHandlerRegistry => CommandHandlerRegistry =
    (registry: CommandHandlerRegistry) => {
      registry.addCreatingDomainCommandHandler[TCommand](fromSyncFun(syncExecute, executionContext))
      registry
    }

}

object MutatingDomainCommandHandler {
  def fromSyncFun[TCommand <: DomainCommand, TEvent <: DomainEvent, TAR <: AggregateRoot[TAR, TEvent]](syncExecute: (TAR, TCommand) => AlmValidation[(TAR, IndexedSeq[TEvent])], executionContext: ExecutionContext)(implicit tag: ClassTag[TAR]): MutatingDomainCommandHandler { type TCom = TCommand; type Event = TEvent; type AR = TAR } =
    new MutatingDomainCommandHandler {
      type TCom = TCommand
      type Event = TEvent
      type AR = TAR
      val typeOfAr = tag.runtimeClass.asInstanceOf[Class[AR]]
      override def execute(ar: TAR, command: TCom) = AlmFuture { syncExecute(ar, command) }(executionContext)
    }

  def createRegistryAdderFromSyncFun[TCommand <: DomainCommand, TEvent <: DomainEvent, TAR <: AggregateRoot[TAR, TEvent]](syncExecute: (TAR, TCommand) => AlmValidation[(TAR, IndexedSeq[TEvent])], executionContext: ExecutionContext)(implicit tagAr: ClassTag[TAR], tagC: ClassTag[TCommand]): CommandHandlerRegistry => CommandHandlerRegistry =
    (registry: CommandHandlerRegistry) => {
      registry.addMutatingDomainCommandHandler[TCommand](fromSyncFun(syncExecute, executionContext))
      registry
    }

}