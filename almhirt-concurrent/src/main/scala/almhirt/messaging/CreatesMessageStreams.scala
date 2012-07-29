package almhirt.messaging

import almhirt.concurrent.AlmFuture

trait CreatesMessageStreams {
  def createMessageStream(topic: Option[String]): AlmFuture[MessageStream]
}