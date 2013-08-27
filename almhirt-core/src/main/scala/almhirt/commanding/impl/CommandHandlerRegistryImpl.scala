package almhirt.commanding.impl

import scala.reflect.ClassTag
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.commanding._

class CommandHandlerRegistryImpl extends CommandHandlerRegistry {
  private val handlers = new java.util.concurrent.ConcurrentHashMap[Class[_], CommandHandler](128)
  final override def register(forCommandType: Class[_], handler: CommandHandler) {
    handlers.put(forCommandType, handler)
  }
    
  final override def get(commandType: Class[_]): AlmValidation[CommandHandler] =
    handlers.get(commandType) match {
      case null => NoSuchElementProblem(s"""No command handler found for "${commandType.getName()}"""").failure
      case handler => handler.success
    }
}