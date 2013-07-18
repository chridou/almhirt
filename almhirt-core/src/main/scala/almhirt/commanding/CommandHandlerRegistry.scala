package almhirt.commanding

import scala.reflect.ClassTag
import almhirt.common._

trait CommandHandlerRegistry {
  def addGenericCommandHandler[TCommand <: Command](command: TCommand, handler: GenericCommandHandler { type TCom = TCommand })(implicit tag: ClassTag[TCommand])
  def addCreatingDomainCommandHandler[TCommand <: DomainCommand](command: TCommand, handler: CreatingDomainCommandHandler { type TCom = TCommand })(implicit tag: ClassTag[TCommand])
  def addMutatingDomainCommandHandler[TCommand <: DomainCommand](command: TCommand, handler: MutatingDomainCommandHandler { type TCom = TCommand })(implicit tag: ClassTag[TCommand])

  def get(commandType: Class[_]): AlmValidation[CommandHandler]
}

object CommandHandlerRegistry {
  def apply(): CommandHandlerRegistry = new impl.CommandHandlerRegistryImpl()
}