package almhirt.util

import java.util.{ UUID => JUUID }
import almhirt.commanding.DomainCommand
import almhirt.domain.AggregateRootRef
import almhirt.domain.AggregateRootRef

sealed trait CommandInfo {
  def aggRef: Option[AggregateRootRef]
  def commandId: JUUID
  def commandType: String
  def toHeadCommandInfo: HeadCommandInfo =
    this match {
      case c: HeadCommandInfo => c
      case c: FullComandInfo => HeadCommandInfo(c)
    }
}

object CommandInfo {
  def apply(command: DomainCommand): CommandInfo = FullComandInfo(command)
  def apply(commandId: JUUID, commandType: String, aggRef: Option[AggregateRootRef]): CommandInfo = HeadCommandInfo(commandId, commandType, aggRef)
}

final case class FullComandInfo(command: DomainCommand) extends CommandInfo {
  def commandId = command.id
  def aggRef = command.aggRef
  def commandType = command.getClass().getName()
}

final case class HeadCommandInfo(commandId: JUUID, commandType: String, aggRef: Option[AggregateRootRef]) extends CommandInfo

object HeadCommandInfo {
  def apply(fullInfo: FullComandInfo): HeadCommandInfo =
    HeadCommandInfo(fullInfo.commandId, fullInfo.commandType, fullInfo.aggRef)
  def apply(command: DomainCommand): HeadCommandInfo =
    apply(FullComandInfo(command))
}
