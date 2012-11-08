package almhirt.messaging

trait MessageHandler {
  def handle(message: Message[AnyRef]): Unit
}