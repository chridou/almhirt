package almhirt.tracking

import scala.language.existentials
import java.time.LocalDateTime
import almhirt.common.CommandId
import almhirt.common.Command
import almhirt.common.AggregateRootCommand

sealed trait CommandRepresentation {
  def toShortString: String
  //  def toVeryShortString: String
  def downgradeToIdAndType: CommandRepresentation.CommandIdAndType
  def commandId: CommandId
  def commandType: String
}

object CommandRepresentation {
  final case class FullCommand(cmd: Command) extends CommandRepresentation {
    //    override def toShortString: String =
    //      cmd match {
    //        case c: AggregateRootCommand ⇒
    //          s"""|${cmd.getClass().getName}(${c.commandId.value})
    //        	|AggregateRootId: ${c.aggId.value}
    //        	|AggregateRootVersion: ${c.aggVersion.value}""".stripMargin
    //        case _ ⇒
    //          s"""${cmd.getClass().getName}(${cmd.commandId.value})"""
    //      }

    override def commandId: CommandId = cmd.commandId
    override def commandType: String = cmd.getClass.getName

    override def toShortString: String =
      s"""${cmd.getClass().getSimpleName}(${cmd.commandId.value})"""

    override def downgradeToIdAndType: CommandIdAndType = CommandIdAndType(cmd)

  }

  final case class CommandIdAndType(commandId: CommandId, commandType: String) extends CommandRepresentation {
    override def toShortString: String =
      s"""${commandType}(${commandId.value})"""

    //    override def toVeryShortString: String =
    //      s"""${commandType.getSimpleName}(${commandId.value})"""

    override def downgradeToIdAndType: CommandIdAndType = this
  }

  object CommandIdAndType {
    def apply(cmd: Command): CommandIdAndType = CommandIdAndType(cmd.commandId, cmd.getClass().getName)
  }
}

final case class CommandStatusDocument(representation: CommandRepresentation, timestamp: LocalDateTime, status: CommandStatus)
