package almhirt.messaging

import almhirt.Closeable

trait MessageHub extends CreatesMessageChannels with CanDeliverMessages with Closeable {
}

object MessageHub {
  def apply() = ActorBasedMessageHub()
}
