package almhirt.storages

import org.joda.time.LocalDateTime
import akka.actor._
import almhirt.common._
import almhirt.tracking.{ CommandStatus, CommandStatusDocument }

object CommandStatusStorage {
  sealed trait CommandStatusStorageMessage

  final case class StoreCommandStatus(status: CommandStatusDocument) extends CommandStatusStorageMessage
  sealed trait StoreCommandStatusResult extends CommandStatusStorageMessage
  final case class CommandStatusStored(commandId: CommandId) extends StoreCommandStatusResult
  final case class CommandStatusNotStored(commandId: CommandId, problem: Problem) extends StoreCommandStatusResult

  final case class FetchCommandStatus(commandId: CommandId) extends CommandStatusStorageMessage
  sealed trait FetchCommandStatusResult extends CommandStatusStorageMessage
  final case class CommandStatusFetched(status: CommandStatusDocument) extends FetchCommandStatusResult
  final case class CommandStatusNotFetched(commandId: CommandId, problem: Problem) extends FetchCommandStatusResult
}

object DevNullCommandStatusStorage {
  def props: Props = InMemoryCommandStatusStorage.props(0)
}

object InMemoryCommandStatusStorage {
  def props(maxEntries: Int): Props = Props(new AStupidInMemoryCommandStatusStorage(maxEntries))
}

private[almhirt] class AStupidInMemoryCommandStatusStorage(maxEntries: Int) extends Actor {
  import CommandStatusStorage._

  private[this] var entries: Vector[CommandStatusDocument] = Vector.empty

  override def receive: Receive = {
    case StoreCommandStatus(status) ⇒
      if (maxEntries > 0) {
        if (entries.size < maxEntries) {
          entries = entries :+ status
        } else {
          entries = entries.tail :+ status
        }
      }
      sender() ! CommandStatusStored(status.commandId)

    case FetchCommandStatus(commandId) ⇒
      entries.find(_.commandId == commandId) match {
        case Some(status) ⇒
          sender() ! CommandStatusFetched(status)
        case None ⇒
          sender() ! CommandStatusNotFetched(commandId, NotFoundProblem(s"""No status for command "${commandId.value}"."""))
      }
  }
}