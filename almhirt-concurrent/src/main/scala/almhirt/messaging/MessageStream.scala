package almhirt.messaging

import java.util.UUID
import almhirt._

/** Publishes messages to its subscribers. 
 * This is the weakest contract a channel must fulfill:
 * A Channel does
 * * guarantee that all message have been published by someone
 * * not guarantee that messages arrive in the same order as they were published  
 * * not guarantee that all messages will be published
 * * not guarantee that all handlers will be called on the same thread
 * * not guarantee that handlers won't be called concurrently
 */
trait MessageStream extends SubscribableForMessages with almhirt.MightBeRegisteredSomewhere with almhirt.Disposable {
  def topicPattern: Option[String]
  
  def dispose() =
    registration.foreach(_.dispose())
}

