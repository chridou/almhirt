package almhirt.messaging

import almhirt._

trait CreatesMessageChannels {
  def createMessageChannel(topic: Option[String]): AlmFuture[MessageChannel]
}