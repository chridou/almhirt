package almhirt.common

import org.joda.time.DateTime
import almhirt.common._

trait CommandHeader {
  /** The events unique identifier */
  def id: java.util.UUID
  /** The events timestamp of creation */
  def timestamp: DateTime
  /**
   *
   */
  def metadata: Map[String, String]

  def changeMetadata(newMetadata: Map[String, String]): CommandHeader
}

object CommandHeader {
  def apply(anId: java.util.UUID, aTimestamp: DateTime, metaData: Map[String, String]): CommandHeader = BasicCommandHeader(anId, aTimestamp, metaData)
  def apply(anId: java.util.UUID, aTimestamp: DateTime): CommandHeader = BasicCommandHeader(anId, aTimestamp, Map.empty)
  def apply()(implicit ccuad: CanCreateUuidsAndDateTimes): CommandHeader = BasicCommandHeader(ccuad.getUuid, ccuad.getDateTime, Map.empty)
  def apply(metaData: Map[String, String])(implicit ccuad: CanCreateUuidsAndDateTimes): CommandHeader = BasicCommandHeader(ccuad.getUuid, ccuad.getDateTime, metaData)
  private case class BasicCommandHeader(id: java.util.UUID, timestamp: DateTime, metadata: Map[String, String]) extends CommandHeader {
    override def changeMetadata(newMetadata: Map[String, String]): BasicCommandHeader =
      this.copy(metadata = newMetadata)
  }
}

trait Command {
  def header: CommandHeader
  def changeMetadata(newMetadata: Map[String, String]): Command
  def metadata = header.metadata
}

object Command {
  implicit class CommandOps[T <: Command](self: T) {
    def mergeMetadata(toMerge: Map[String, String]): T =
      self.changeMetadata(toMerge.foldLeft(self.metadata)((acc, cur) => acc + cur)).asInstanceOf[T]
    def addMetadata(keyAndValue: (String, String)): T =
      self.changeMetadata(self.header.metadata + keyAndValue).asInstanceOf[T]
    def addGrouping(commandGrouping: CommandGrouping): T = commandGrouping.addToCommand(self)
    def getGrouping(commandGrouping: CommandGrouping): AlmValidation[CommandGrouping] = CommandGrouping.fromMap(self.metadata.lift)
    def isPartOfAGroup: Boolean = self.metadata.contains("group-label")
  }
}

final case class CommandGrouping(groupId: String, index: Int, isLast: Boolean) {
  def addToCommand[T <: Command](cmd: T): T = {
    val metadata = Map("group-label" -> groupId, "group-id" -> index.toString, "group-is-last" -> isLast.toString)
    cmd.mergeMetadata(metadata)
  }
}

object CommandGrouping {
  import scala.annotation.tailrec
  import almhirt.almvalidation.kit._

  def fromMap(getValues: String => Option[String]): AlmValidation[CommandGrouping] =
    for {
      label <- (getValues >! "group-label")
      idxStr <- (getValues >! "group-id")
      idx <- idxStr.toIntAlm
      isLastStr <- (getValues >! "group-is-last")
      isLast <- isLastStr.toBooleanAlm
    } yield CommandGrouping(label, idx, isLast)

  def groupCommands[T <: Command](groupLabel: String, commands: List[T]): List[T] = {
    @tailrec
    def groupRest(rest: List[T], idx: Int, acc: List[T]): List[T] =
      rest match {
        case Nil => acc
        case x :: Nil =>
          val grp = CommandGrouping(groupLabel, idx, true)
          groupRest(Nil, idx + 1, (x.addGrouping(grp)) :: acc)
        case x :: xs =>
          val grp = CommandGrouping(groupLabel, idx, false)
          groupRest(xs, idx + 1, (x.addGrouping(grp)) :: acc)
      }
    groupRest(commands, 1, Nil).reverse
  }
}

