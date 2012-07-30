package almhirt.messaging

import almhirt.concurrent.AlmFuture

trait CreatesMessageChannels {
  def createMessageChannel(topic: Option[String]): AlmFuture[MessageChannel]
}