package almhirt.common

import org.joda.time.LocalDateTime
import scalaz._, Scalaz._
import almhirt.common._

trait CommandHeader {
  /** The events unique identifier */
  def id: java.util.UUID
  /** The events timestamp of creation */
  def timestamp: LocalDateTime
  /**
   *
   */
  def metadata: Map[String, String]

  def changeMetadata(newMetadata: Map[String, String]): CommandHeader
}

object CommandHeader {
  def apply(anId: java.util.UUID, aTimestamp: LocalDateTime, metaData: Map[String, String]): CommandHeader = BasicCommandHeader(anId, aTimestamp, metaData)
  def apply(anId: java.util.UUID, aTimestamp: LocalDateTime): CommandHeader = BasicCommandHeader(anId, aTimestamp, Map.empty)
  def apply()(implicit ccuad: CanCreateUuidsAndDateTimes): CommandHeader = BasicCommandHeader(ccuad.getUuid, ccuad.getUtcTimestamp, Map.empty)
  def apply(metaData: Map[String, String])(implicit ccuad: CanCreateUuidsAndDateTimes): CommandHeader = BasicCommandHeader(ccuad.getUuid, ccuad.getUtcTimestamp, metaData)
  case class BasicCommandHeader(id: java.util.UUID, timestamp: LocalDateTime, metadata: Map[String, String]) extends CommandHeader {
    override def changeMetadata(newMetadata: Map[String, String]): BasicCommandHeader =
      this.copy(metadata = newMetadata)
  }
}

trait Command {
  def header: CommandHeader
  def changeMetadata(newMetadata: Map[String, String]): Command
  def metadata = header.metadata
  def commandId: java.util.UUID = header.id
}

object Command {
  implicit class CommandOps[T <: Command](self: T) {
    def mergeMetadata(toMerge: Map[String, String]): T =
      self.changeMetadata(toMerge.foldLeft(self.metadata)((acc, cur) => acc + cur)).asInstanceOf[T]
    def addMetadata(keyAndValue: (String, String)): T =
      self.changeMetadata(self.header.metadata + keyAndValue).asInstanceOf[T]
    def addGrouping(commandGrouping: CommandGrouping): T = commandGrouping.addToCommand(self)
    def getGrouping: AlmValidation[CommandGrouping] = CommandGrouping.fromMap(self.metadata.lift)
    def isPartOfAGroup: Boolean = self.metadata.contains("group-label")
    def tryGetGroupLabel: Option[String] = self.metadata.get("group-label")
    def trackableGroup: T = self.addMetadata("trackable-as-group", "true")
    def canBeTrackedAsGroup: Boolean = self.isPartOfAGroup && (self.metadata.get("trackable-as-group").map(_.toLowerCase() == "true") | false)
    def tryGetGroupTrackingId: Option[String] = 
      if(self.canBeTrackedAsGroup)
        self.metadata.get("group-label")
      else
        None
    def getGroupTrackingId: AlmValidation[String] = 
      tryGetGroupTrackingId match {
        case None => NoSuchElementProblem("Command is not trackable or has no tracking id for groups").failure
        case Some(id) => id.success
      }
    def track(trackId: String): T = self.addMetadata("track-id", trackId)
    def track(implicit ccud: CanCreateUuid): T = track(ccud.getUniqueString)
    def canBeTracked: Boolean = self.metadata.contains("track-id")
    def tryGetTrackingId: Option[String] = self.metadata.get("track-id")
    def trackingId: String = self.metadata("track-id")
  }
}

final case class CommandGrouping(groupLabel: String, index: Int, isLast: Boolean) {
  def addToCommand[T <: Command](cmd: T): T = {
    val metadata = Map("group-label" -> groupLabel, "group-id" -> index.toString, "group-is-last" -> isLast.toString)
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

  def groupCommands[T <: Command](groupLabel: String, commands: List[T], trackableAsGroup: Boolean = false): List[T] = {
    @tailrec
    def groupRest(rest: List[T], idx: Int, acc: List[T], first: Boolean): List[T] =
      rest match {
        case Nil => acc
        case x :: xs =>
          val grp = CommandGrouping(groupLabel, idx, xs.isEmpty)
          val cmd = if(first && trackableAsGroup) {
            x.addGrouping(grp).trackableGroup
          } else {
            x.addGrouping(grp)
          }
          groupRest(xs, idx + 1, cmd :: acc, false)
      }
    groupRest(commands, 1, Nil, true).reverse
  }
}


