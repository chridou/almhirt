package almhirt.messaging

import almhirt.concurrent.AlmFuture

trait CanCreateSubChannels {
  def createSubChannel(classifier: Message[AnyRef] => Boolean): AlmFuture[MessageStream]
  
  def createSubChannel[TPayload <: AnyRef](classifier: Message[TPayload] => Boolean)(implicit m: Manifest[TPayload]): AlmFuture[MessageStream] = {
    def wrappedClassifier(message: Message[AnyRef]) = 
      if(m.erasure.isAssignableFrom(message.payload.getClass()))
      	classifier(message.asInstanceOf[Message[TPayload]])
      else
      	false
    createSubChannel(wrappedClassifier(_))
  }
  
}