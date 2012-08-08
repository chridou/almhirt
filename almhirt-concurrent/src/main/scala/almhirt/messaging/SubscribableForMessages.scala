package almhirt.messaging

import java.util.UUID
import almhirt._
import concurrent.AlmFuture

/** Someone you can subscribe to for any Method. */
trait SubscribableForMessages {
  def <* (handler: Message[AnyRef] => Unit, classifier: Message[AnyRef] => Boolean): AlmFuture[RegistrationHolder]

  def <* (handler: Message[AnyRef] => Unit): AlmFuture[RegistrationHolder] = 
  	<* (handler, (_: Message[AnyRef]) => true)

  def <#[TPayload <: AnyRef](handler: Message[TPayload] => Unit, classifier: Message[TPayload] => Boolean)(implicit m: Manifest[TPayload]): AlmFuture[RegistrationHolder] = {
    def wrappedHandler(message: Message[AnyRef]): Unit =
      handler(message.asInstanceOf[Message[TPayload]])
    def wrappedClassifier(message: Message[AnyRef]) = 
      if(m.erasure.isAssignableFrom(message.payload.getClass()))
      	classifier(message.asInstanceOf[Message[TPayload]])
      else
      	false
    <* (wrappedHandler, wrappedClassifier)
  }
	
  def <#[TPayload <: AnyRef](handler: Message[TPayload] => Unit)(implicit m: Manifest[TPayload]): AlmFuture[RegistrationHolder] = 
  	<# [TPayload](handler, (_: Message[TPayload]) => true)(m)

}


