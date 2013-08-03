package almhirt.serialization

import almhirt.common.{Command, CommandHeader}

final case class ChangeSerializationChannel(header: CommandHeader, newChannel: String) extends Command {
  override def changeMetadata(newMetaData: Map[String, String]): ChangeSerializationChannel = copy(header = this.header.changeMetadata(newMetaData))
}