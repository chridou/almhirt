package almhirt.commanding.impl

import scala.reflect.ClassTag
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.commanding._

class CommandHandlerRegistryImpl extends CommandHandlerRegistry {
  private val handlers = new java.util.concurrent.ConcurrentHashMap[Class[_], CommandHandler](128)

  final override def addGenericCommandHandler[TCommand <: Command](command: TCommand, handler: GenericCommandHandler { type TCom = TCommand })(implicit tag: ClassTag[TCommand]) =
    handlers.put(tag.runtimeClass, handler)
    
  final override def addCreatingDomainCommandHandler[TCommand <: DomainCommand](command: TCommand, handler: CreatingDomainCommandHandler { type TCom = TCommand })(implicit tag: ClassTag[TCommand]) =
    handlers.put(tag.runtimeClass, handler)
    
  final override def addMutatingDomainCommandHandler[TCommand <: DomainCommand](command: TCommand, handler: MutatingDomainCommandHandler { type TCom = TCommand })(implicit tag: ClassTag[TCommand]) =
    handlers.put(tag.runtimeClass, handler)
    
  final override def get(commandType: Class[_]): AlmValidation[CommandHandler] =
    handlers.get(commandType) match {
      case null => UnspecifiedProblem(s"""No command handler found for "${commandType.getName()}"""").failure
      case handler => handler.success
    }
}