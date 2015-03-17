package almhirt.tracking

import scala.language.existentials
import org.joda.time.LocalDateTime
import almhirt.common.CommandId
import almhirt.common.Command
import almhirt.common.AggregateRootCommand

sealed trait CommandRepresentation {
  def toShortString: String
  def toVeryShortString: String
  def downgradeToIdAndType: CommandRepresentation
}

object CommandRepresentation {
  final case class FullCommand(cmd: Command) extends CommandRepresentation {
    override def toShortString: String =
      cmd match {
        case c: AggregateRootCommand ⇒
          s"""|${cmd.getClass().getName}(${c.commandId.value})
        	|AggregateRootId: ${c.aggId.value}
        	|AggregateRootVersion: ${c.aggVersion.value}""".stripMargin
        case _ ⇒
          s"""${cmd.getClass().getName}(${cmd.commandId.value})"""
      }

    override def toVeryShortString: String =
      s"""${cmd.getClass().getSimpleName}(${cmd.commandId.value})"""

    override def downgradeToIdAndType: CommandRepresentation = CommandIdAndType(cmd)

  }

  final case class CommandIdAndType(commandId: CommandId, commandType: Class[_ <: Command]) extends CommandRepresentation {
    override def toShortString: String =
      s"""${commandType.getName}(${commandId.value})"""

    override def toVeryShortString: String =
      s"""${commandType.getSimpleName}(${commandId.value})"""

    override def downgradeToIdAndType: CommandRepresentation = this
  }

  object CommandIdAndType {
    def apply(cmd: Command): CommandIdAndType = CommandIdAndType(cmd.commandId, cmd.getClass())
  }

  final case class CommandIdOnly(commandId: CommandId) extends CommandRepresentation {
    override def toShortString: String =
      s"""Command with id ${commandId.value})"""

    override def toVeryShortString: String =
      s"""Command with id ${commandId.value})"""

    override def downgradeToIdAndType: CommandRepresentation = this
  }
}

final case class CommandStatusDocument(representation: CommandRepresentation, timestamp: LocalDateTime, status: CommandStatus)
