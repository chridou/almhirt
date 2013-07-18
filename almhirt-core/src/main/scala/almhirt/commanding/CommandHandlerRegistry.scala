package almhirt.commanding

import scala.reflect.ClassTag
import almhirt.common._

trait CommandHandlerRegistry {
  def register(forCommandType: Class[_], handler: CommandHandler)

  def get(commandType: Class[_]): AlmValidation[CommandHandler]
}

object CommandHandlerRegistry {
  def apply(): CommandHandlerRegistry = new impl.CommandHandlerRegistryImpl()

  implicit class CommandHandlerRegistryOps(self: CommandHandlerRegistry) {
    def addGenericCommandHandler[TCommand <: Command](command: TCommand, handler: GenericCommandHandler { type TCom = TCommand })(implicit tag: ClassTag[TCommand]) =
      self.register(tag.runtimeClass, handler)

    def addCreatingDomainCommandHandler[TCommand <: DomainCommand](command: TCommand, handler: CreatingDomainCommandHandler { type TCom = TCommand })(implicit tag: ClassTag[TCommand]) =
      self.register(tag.runtimeClass, handler)

    def addMutatingDomainCommandHandler[TCommand <: DomainCommand](command: TCommand, handler: MutatingDomainCommandHandler { type TCom = TCommand })(implicit tag: ClassTag[TCommand]) =
      self.register(tag.runtimeClass, handler)
  }
}