package almhirt.messaging

import java.util.UUID
import almhirt._
import concurrent.AlmFuture

/** Someone you can subscribe to for any Method. */
trait SubscribableForMessages {
  def <* (handler: Message[AnyRef] => Unit, classifier: Message[AnyRef] => Boolean): AlmFuture[Registration[UUID]]

  def <* (handler: Message[AnyRef] => Unit): AlmFuture[Registration[UUID]] = 
  	<* (handler, (_: Message[AnyRef]) => true)

  def <#[TPayload <: AnyRef](handler: Message[TPayload] => Unit, classifier: Message[TPayload] => Boolean)(implicit m: Manifest[TPayload]): AlmFuture[Registration[UUID]] = {
    def wrappedHandler(message: Message[AnyRef]): Unit =
      handler(message.asInstanceOf[Message[TPayload]])
    def wrappedClassifier(message: Message[AnyRef]) = 
      if(m.erasure.isAssignableFrom(message.payload.getClass()))
      	classifier(message.asInstanceOf[Message[TPayload]])
      else
      	false
    <* (wrappedHandler, wrappedClassifier)
  }
	
  def <#[TPayload <: AnyRef](handler: Message[TPayload] => Unit)(implicit m: Manifest[TPayload]): AlmFuture[Registration[UUID]] = 
  	<# [TPayload](handler, (_: Message[TPayload]) => true)(m)

}


