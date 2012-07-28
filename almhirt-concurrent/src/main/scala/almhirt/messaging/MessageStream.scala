package almhirt.messaging

import almhirt.concurrent.AlmFuture

/** Publishes messages to its subscribers. 
 * This is the weakest contract a channel must fulfill:
 * A Channel does
 * * guarantee that all message have been published by someone
 * * not guarantee that messages arrive in the same order as they were published  
 * * not guarantee that all messages will be published
 * * not guarantee that all handlers will be called on the same thread
 * * not guarantee that handlers won't be called concurrently
 */
trait MessageStream extends SubscribableForMessages with almhirt.Closeable {
  def subStream(classifier: Message[AnyRef] => Boolean): AlmFuture[MessageStream]
  def subStream[TPayload <: AnyRef](classifier: Message[TPayload] => Boolean)(implicit m: Manifest[TPayload]): AlmFuture[MessageStream] = {
    def wrappedClassifier(message: Message[AnyRef]) = 
      if(m.erasure.isAssignableFrom(message.payload.getClass()))
      	classifier(message.asInstanceOf[Message[TPayload]])
      else
      	false
    subStream(wrappedClassifier(_))
  }
}

