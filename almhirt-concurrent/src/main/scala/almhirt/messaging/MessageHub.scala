package almhirt.messaging

import almhirt.Closeable

trait MessageHub extends CreatesMessageStreams with CanDeliverMessages with Closeable {
}


