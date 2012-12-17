package almhirt.parts

import akka.util.Duration
import almhirt.common._
import almhirt.commanding._
import almhirt.common.AlmFuture
import almhirt.parts.impl.ConcurrentCommandHandlerRegistry

trait HasCommandHandlers {
  def addHandler(handler: HandlesCommand): Unit
  def removeHandlerByType(commandType: Class[_ <: DomainCommand]): Unit
  def removeHandler[T <: DomainCommand](implicit m: Manifest[T]) {removeHandlerByType(m.erasure.asInstanceOf[Class[T]])}
  def getHandlerByType(commandType: Class[_ <: DomainCommand]): AlmValidation[HandlesCommand] 
  def getHandler[T <: DomainCommand](implicit m: Manifest[T]): AlmValidation[HandlesCommand] = getHandlerByType(m.erasure.asInstanceOf[Class[T]])
  def getHandlerForCommand(command: DomainCommand): AlmValidation[HandlesCommand] = getHandlerByType(command.getClass)
}

object HasCommandHandlers {
  def apply(): HasCommandHandlers  = new ConcurrentCommandHandlerRegistry()
}