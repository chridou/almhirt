package almhirt.parts

import scala.reflect.ClassTag
import almhirt.common._
import almhirt.commanding._
import almhirt.common.AlmFuture
import almhirt.parts.impl.ConcurrentCommandHandlerRegistry

trait HasCommandHandlers {
  def addHandler(handler: HandlesCommand): Unit
  def removeHandlerByType(commandType: Class[_ <: Command]): Unit
  def removeHandler[T <: Command](implicit m: ClassTag[T]) {removeHandlerByType(m.runtimeClass.asInstanceOf[Class[T]])}
  def getHandlerByType(commandType: Class[_ <: Command]): AlmValidation[HandlesCommand] 
  def getHandler[T <: Command](implicit m: ClassTag[T]): AlmValidation[HandlesCommand] = getHandlerByType(m.runtimeClass.asInstanceOf[Class[T]])
  def getHandlerForCommand(command: Command): AlmValidation[HandlesCommand] = getHandlerByType(command.getClass)
}

object HasCommandHandlers {
  def apply(): HasCommandHandlers  = new ConcurrentCommandHandlerRegistry()
}