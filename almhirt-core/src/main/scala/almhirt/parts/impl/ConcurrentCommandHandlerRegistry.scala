package almhirt.parts.impl

import scalaz.syntax.validation._
import almhirt.common._
import almhirt.parts.HasCommandHandlers
import almhirt.commanding._

class ConcurrentCommandHandlerRegistry extends HasCommandHandlers {
  private val registeredHandlers = new java.util.concurrent.ConcurrentHashMap[Class[_ <: DomainCommand], HandlesCommand](512)

  def addHandler(handler: HandlesCommand) {
    registeredHandlers.put(handler.commandType, handler)
  }

  def getHandlerByType(commandType: Class[_ <: DomainCommand]): AlmValidation[HandlesCommand] = 
    registeredHandlers.get(commandType) match {
      case null => UnspecifiedProblem("No implementation found for handler '%s'".format(commandType.getName())).failure
      case handler => handler.success
    }
  
  def removeHandlerByType(commandType: Class[_ <: DomainCommand]) {
	  registeredHandlers.remove(commandType)
  }
}