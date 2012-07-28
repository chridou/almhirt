package almhirt.messaging

trait CreatesMessageStreams {
  def createMessageStream(topic: Option[String])
}