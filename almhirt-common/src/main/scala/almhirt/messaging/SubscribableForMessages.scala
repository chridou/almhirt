/* Copyright 2012 Christian Douven

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package almhirt.messaging

import java.util.UUID
import almhirt._

/** Someone you can subscribe to for any Method. */
trait SubscribableForMessages[T <: AnyRef] {
  def <-* (handler: Message[T] => Unit, classifier: Message[T] => Boolean): AlmFuture[RegistrationHolder]

  def <-* (handler: Message[T] => Unit): AlmFuture[RegistrationHolder] = 
  	<-* (handler, (_: Message[T]) => true)

  def <-#[TPayload <: T](handler: Message[TPayload] => Unit, classifier: Message[TPayload] => Boolean)(implicit m: Manifest[TPayload]): AlmFuture[RegistrationHolder] = {
    def wrappedHandler(message: Message[T]): Unit =
      handler(message.asInstanceOf[Message[TPayload]])
    def wrappedClassifier(message: Message[T]) = 
      if(m.erasure.isAssignableFrom(message.payload.getClass()))
      	classifier(message.asInstanceOf[Message[TPayload]])
      else
      	false
    <-* (wrappedHandler, wrappedClassifier)
  }
	
  def <-#[TPayload <: T](handler: Message[TPayload] => Unit)(implicit m: Manifest[TPayload]): AlmFuture[RegistrationHolder] = 
  	<-# [TPayload](handler, (_: Message[TPayload]) => true)(m)

}


