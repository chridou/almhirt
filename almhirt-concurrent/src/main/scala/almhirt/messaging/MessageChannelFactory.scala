package almhirt.messaging

trait MessageChannelFactory {
  def create(name: String): MessageChannel
  def create(): MessageChannel = create(java.util.UUID.randomUUID.toString)
}