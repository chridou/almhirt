package almhirt.commanding

import scala.reflect.ClassTag
import scalaz.syntax.validation._
import almhirt.common._

trait CommandHandlerRegistry {
  def register(forCommandType: Class[_], handler: CommandHandler)
  def get(commandType: Class[_]): AlmValidation[CommandHandler]
}

object CommandHandlerRegistry {
  def apply(): CommandHandlerRegistry = new impl.CommandHandlerRegistryImpl()

  implicit class CommandHandlerRegistryOps(self: CommandHandlerRegistry) {
    def addGenericCommandHandler[TCommand <: Command](handler: GenericCommandHandler { type TCom = TCommand })(implicit tag: ClassTag[TCommand]) =
      self.register(tag.runtimeClass, handler)

    def addCreatingDomainCommandHandler[TCommand <: DomainCommand with CreatingDomainCommand](handler: CreatingDomainCommandHandler { type TCom = TCommand })(implicit tag: ClassTag[TCommand]) =
      self.register(tag.runtimeClass, handler)

    def addMutatingDomainCommandHandler[TCommand <: DomainCommand](handler: MutatingDomainCommandHandler { type TCom = TCommand })(implicit tag: ClassTag[TCommand]) =
      self.register(tag.runtimeClass, handler)

    def getDomainCommandHandler(command: DomainCommand): AlmValidation[DomainCommandHandler] =
      self.get(command.getClass).flatMap(commandHandler =>
        if (commandHandler.isInstanceOf[DomainCommandHandler])
          commandHandler.asInstanceOf[DomainCommandHandler].success
        else
          UnspecifiedProblem(s""""${command.getClass.getName()}" is not bound to a domain command handler.""").failure)

    def getCreatingDomainCommandHandler(command: DomainCommand): AlmValidation[CreatingDomainCommandHandler] =
      if (command.creates) {
        self.get(command.getClass).flatMap(commandHandler =>
          if (commandHandler.isInstanceOf[CreatingDomainCommandHandler])
            commandHandler.asInstanceOf[CreatingDomainCommandHandler].success
          else
            UnspecifiedProblem(s""""${command.getClass.getName()}" is not bound to a creating domain command handler.""").failure)
      } else {
        UnspecifiedProblem(s""""${command.getClass.getName()}" is a mutating command.""").failure
      }

    def getMutatingDomainCommandHandler(command: DomainCommand): AlmValidation[MutatingDomainCommandHandler] =
      if (!command.creates) {
      self.get(command.getClass).flatMap(commandHandler =>
        if (commandHandler.isInstanceOf[MutatingDomainCommandHandler])
          commandHandler.asInstanceOf[MutatingDomainCommandHandler].success
        else
          UnspecifiedProblem(s""""${command.getClass.getName()}" is not bound to a mutating domain command handler.""").failure)
      }else {
        UnspecifiedProblem(s""""${command.getClass.getName()}" is a creating command.""").failure
      }
    
    def nextAdder(theAdder: CommandHandlerRegistry => CommandHandlerRegistry): CommandHandlerRegistry =
      theAdder(self)
  }
  
}