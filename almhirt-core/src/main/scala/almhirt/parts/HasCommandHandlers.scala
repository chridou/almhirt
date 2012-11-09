package almhirt.parts

import akka.util.Duration
import almhirt._
import almhirt.commanding._

sealed trait HasCommandHandlersCmd
case class AddCommandHandlerCmd(handler: HandlesCommand) extends HasCommandHandlersCmd
case class RemoveCommandHandlerCmd(commandType: Class[_ <: DomainCommand]) extends HasCommandHandlersCmd
case class GetCommandHandlerQry(commandType: Class[_ <: DomainCommand]) extends HasCommandHandlersCmd

sealed trait HasCommandHandlersRsp
case class CommandHandlerRsp(handler: AlmValidation[HandlesCommand]) extends HasCommandHandlersRsp


trait HasCommandHandlers {
  def addHandler(handler: HandlesCommand): Unit
  def removeHandlerByType(commandType: Class[_ <: DomainCommand]): Unit
  def removeHandler[T <: DomainCommand](implicit m: Manifest[T]) {removeHandlerByType(m.erasure.asInstanceOf[Class[T]])}
  def getHandlerByType(commandType: Class[_ <: DomainCommand])(implicit atMost: Duration): AlmFuture[HandlesCommand] 
  def getHandler[T <: DomainCommand](implicit atMost: Duration, m: Manifest[T]): AlmFuture[HandlesCommand] = getHandlerByType(m.erasure.asInstanceOf[Class[T]])
  def getHandlerForCommand(command: DomainCommand)(implicit atMost: Duration): AlmFuture[HandlesCommand] = getHandlerByType(command.getClass)
}