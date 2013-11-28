package almhirt.commanding

import scala.reflect.ClassTag
import scala.concurrent.ExecutionContext
import almhirt.common._
import almhirt.core.Almhirt
import almhirt.core.types._
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
  def apply[TCommand <: Command](f: TCommand => AlmFuture[String]): GenericCommandHandler { type TCom = TCommand } =
    new GenericCommandHandler {
      type TCom = TCommand
      override def execute(command: TCom) = f(command)
    }

  def fromSyncFun[TCommand <: Command](syncExecute: TCommand => AlmValidation[String], executionContext: ExecutionContext): GenericCommandHandler { type TCom = TCommand } =
    GenericCommandHandler[TCommand]((command: TCommand) => AlmFuture { syncExecute(command) }(executionContext))

  def createRegistryAdder[TCommand <: Command](execute: TCommand => AlmFuture[String])(implicit tagC: ClassTag[TCommand]): CommandHandlerRegistry => CommandHandlerRegistry =
    (registry: CommandHandlerRegistry) => {
      registry.addGenericCommandHandler[TCommand](GenericCommandHandler(execute))
      registry
    }
    
  def createRegistryAdderFromSyncFun[TCommand <: Command](syncExecute: TCommand => AlmValidation[String], executionContext: ExecutionContext)(implicit tagC: ClassTag[TCommand]): CommandHandlerRegistry => CommandHandlerRegistry =
    (registry: CommandHandlerRegistry) => {
      registry.addGenericCommandHandler[TCommand](fromSyncFun(syncExecute, executionContext))
      registry
    }
}

object CreatingDomainCommandHandler {
  def apply[TCommand <: DomainCommand with CreatingDomainCommand, TEvent <: DomainEvent, TAR <: AggregateRoot[TAR, TEvent]](f: TCommand => AlmFuture[(TAR, IndexedSeq[TEvent])])(implicit tag: ClassTag[TAR]): CreatingDomainCommandHandler { type TCom = TCommand; type Event = TEvent; type AR = TAR } =
    new CreatingDomainCommandHandler {
      type TCom = TCommand
      type Event = TEvent
      type AR = TAR
      val typeOfAr = tag.runtimeClass.asInstanceOf[Class[AR]]
      override def execute(command: TCom) = f(command)
    }

  def fromSyncFun[TCommand <: DomainCommand with CreatingDomainCommand, TEvent <: DomainEvent, TAR <: AggregateRoot[TAR, TEvent]](syncExecute: TCommand => AlmValidation[(TAR, IndexedSeq[TEvent])], executionContext: ExecutionContext)(implicit tag: ClassTag[TAR]): CreatingDomainCommandHandler { type TCom = TCommand; type Event = TEvent; type AR = TAR } =
    CreatingDomainCommandHandler[TCommand, TEvent, TAR]((command: TCommand) => AlmFuture { syncExecute(command) }(executionContext))

  def createRegistryAdder[TCommand <: DomainCommand with CreatingDomainCommand, TEvent <: DomainEvent, TAR <: AggregateRoot[TAR, TEvent]](execute: TCommand => AlmFuture[(TAR, IndexedSeq[TEvent])])(implicit tagAr: ClassTag[TAR], tagC: ClassTag[TCommand]): CommandHandlerRegistry => CommandHandlerRegistry =
    (registry: CommandHandlerRegistry) => {
      registry.addCreatingDomainCommandHandler[TCommand](CreatingDomainCommandHandler(execute))
      registry
    }

  def createRegistryAdderFromSyncFun[TCommand <: DomainCommand with CreatingDomainCommand, TEvent <: DomainEvent, TAR <: AggregateRoot[TAR, TEvent]](syncExecute: TCommand => AlmValidation[(TAR, IndexedSeq[TEvent])], executionContext: ExecutionContext)(implicit tagAr: ClassTag[TAR], tagC: ClassTag[TCommand]): CommandHandlerRegistry => CommandHandlerRegistry =
    (registry: CommandHandlerRegistry) => {
      registry.addCreatingDomainCommandHandler[TCommand](fromSyncFun(syncExecute, executionContext))
      registry
    }
    
}

object MutatingDomainCommandHandler {
  def apply[TCommand <: DomainCommand, TEvent <: DomainEvent, TAR <: AggregateRoot[TAR, TEvent]](f: (TAR, TCommand) => AlmFuture[(TAR, IndexedSeq[TEvent])])(implicit tag: ClassTag[TAR]): MutatingDomainCommandHandler { type TCom = TCommand; type Event = TEvent; type AR = TAR } =
    new MutatingDomainCommandHandler {
      type TCom = TCommand
      type Event = TEvent
      type AR = TAR
      val typeOfAr = tag.runtimeClass.asInstanceOf[Class[AR]]
      override def execute(ar: TAR, command: TCom) = f(ar, command)
    }
  
  def fromSyncFun[TCommand <: DomainCommand, TEvent <: DomainEvent, TAR <: AggregateRoot[TAR, TEvent]](syncExecute: (TAR, TCommand) => AlmValidation[(TAR, IndexedSeq[TEvent])], executionContext: ExecutionContext)(implicit tag: ClassTag[TAR]): MutatingDomainCommandHandler { type TCom = TCommand; type Event = TEvent; type AR = TAR } =
    MutatingDomainCommandHandler[TCommand, TEvent, TAR]((ar: TAR, command: TCommand) => AlmFuture { syncExecute(ar, command) }(executionContext))

  def createRegistryAdder[TCommand <: DomainCommand, TEvent <: DomainEvent, TAR <: AggregateRoot[TAR, TEvent]](execute: (TAR, TCommand) => AlmFuture[(TAR, IndexedSeq[TEvent])])(implicit tagAr: ClassTag[TAR], tagC: ClassTag[TCommand]): CommandHandlerRegistry => CommandHandlerRegistry =
    (registry: CommandHandlerRegistry) => {
      registry.addMutatingDomainCommandHandler[TCommand](MutatingDomainCommandHandler(execute))
      registry
    }

  def createRegistryAdderFromSyncFun[TCommand <: DomainCommand, TEvent <: DomainEvent, TAR <: AggregateRoot[TAR, TEvent]](syncExecute: (TAR, TCommand) => AlmValidation[(TAR, IndexedSeq[TEvent])], executionContext: ExecutionContext)(implicit tagAr: ClassTag[TAR], tagC: ClassTag[TCommand]): CommandHandlerRegistry => CommandHandlerRegistry =
    (registry: CommandHandlerRegistry) => {
      registry.addMutatingDomainCommandHandler[TCommand](fromSyncFun(syncExecute, executionContext))
      registry
    }

}

object CommandRegistryAddersBuilder {
  def apply[TAR <: AggregateRoot[TAR, TEvent], TEvent <: DomainEvent](f: AddContext[TAR, TEvent] => AddContext[TAR, TEvent])(implicit tagAr: ClassTag[TAR], executionContext: ExecutionContext): Iterable[CommandHandlerRegistry => CommandHandlerRegistry] = {
    val addContext = new AddContext[TAR, TEvent] {
      private var collected = Vector.empty[CommandHandlerRegistry => CommandHandlerRegistry]
      def addCreating[TCommand <: DomainCommand with CreatingDomainCommand](execute: TCommand => AlmFuture[(TAR, IndexedSeq[TEvent])])(implicit tagC: ClassTag[TCommand]): this.type = {
        collected = collected :+ CreatingDomainCommandHandler.createRegistryAdder[TCommand, TEvent, TAR](execute)
        this
      }
      def addMutating[TCommand <: DomainCommand](execute: (TAR, TCommand) => AlmFuture[(TAR, IndexedSeq[TEvent])])(implicit tagC: ClassTag[TCommand]): this.type = {
        collected = collected :+ MutatingDomainCommandHandler.createRegistryAdder[TCommand, TEvent, TAR](execute)
        this
      }
      def addCreatingFromSync[TCommand <: DomainCommand with CreatingDomainCommand](syncExecute: TCommand => AlmValidation[(TAR, IndexedSeq[TEvent])])(implicit tagC: ClassTag[TCommand]): this.type = {
        collected = collected :+ CreatingDomainCommandHandler.createRegistryAdderFromSyncFun[TCommand, TEvent, TAR](syncExecute, executionContext)
        this
      }
      def addMutatingFromSync[TCommand <: DomainCommand](syncExecute: (TAR, TCommand) => AlmValidation[(TAR, IndexedSeq[TEvent])])(implicit tagC: ClassTag[TCommand]): this.type = {
        collected = collected :+ MutatingDomainCommandHandler.createRegistryAdderFromSyncFun[TCommand, TEvent, TAR](syncExecute, executionContext)
        this
      }
      def result = collected
    }
    f(addContext).result
  }

  trait AddContext[TAR <: AggregateRoot[TAR, TEvent], TEvent <: DomainEvent] {
    def addCreating[TCommand <: DomainCommand with CreatingDomainCommand](execute: TCommand => AlmFuture[(TAR, IndexedSeq[TEvent])])(implicit tagC: ClassTag[TCommand]): this.type
    def addMutating[TCommand <: DomainCommand](execute: (TAR, TCommand) => AlmFuture[(TAR, IndexedSeq[TEvent])])(implicit tagC: ClassTag[TCommand]): this.type
    def addCreatingFromSync[TCommand <: DomainCommand with CreatingDomainCommand](syncExecute: TCommand => AlmValidation[(TAR, IndexedSeq[TEvent])])(implicit tagC: ClassTag[TCommand]): this.type
    def addMutatingFromSync[TCommand <: DomainCommand](syncExecute: (TAR, TCommand) => AlmValidation[(TAR, IndexedSeq[TEvent])])(implicit tagC: ClassTag[TCommand]): this.type
    def result: Iterable[CommandHandlerRegistry => CommandHandlerRegistry]
  }
}