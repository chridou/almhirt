package almhirt.messaging

import almhirt.Closeable
import almhirt.almakka.AlmAkkaContext

trait MessageHub extends CreatesMessageChannels with CanDeliverMessages with Closeable {
}

object MessageHub {
  def apply(implicit almAkkaContext: AlmAkkaContext) = ActorBasedMessageHub(almAkkaContext)
}
