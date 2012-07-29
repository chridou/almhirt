package almhirt.messaging

/** Someone who takes a message and delivers it somewhere else. */
trait CanDeliverMessages {
  def deliver(message: Message[AnyRef]): Unit
}
