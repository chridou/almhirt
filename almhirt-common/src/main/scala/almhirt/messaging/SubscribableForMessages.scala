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

/** Someone you can subscribe to to receive [[almhirt.messaging.Message]]s. 
 *
 * By subscribing you receive a registration for the subscription which can be disposed to unsubscribe a handler.
 *
 * ''Messages must be delivered to the handlers in the same order they are received''
 */
trait SubscribableForMessages[T <: AnyRef] {
  def <-* (handler: Message[T] => Unit, classifier: Message[T] => Boolean)(implicit atMost: akka.util.Duration): AlmFuture[RegistrationHolder]

  def <-* (handler: Message[T] => Unit)(implicit atMost: akka.util.Duration): AlmFuture[RegistrationHolder] = 
  	<-* (handler, (_: Message[T]) => true)

  def <-<* (handler: T => Unit)(implicit atMost: akka.util.Duration): AlmFuture[RegistrationHolder] = 
  	<-* (m => handler(m.payload), (_: Message[T]) => true)

  def <-<* (handler: T => Unit, classifier: T => Boolean)(implicit atMost: akka.util.Duration): AlmFuture[RegistrationHolder] = 
  	<-* (x => handler(x.payload), (x => classifier(x.payload)))
  	
  def <-#[TPayload <: T](handler: Message[TPayload] => Unit, classifier: Message[TPayload] => Boolean)(implicit atMost: akka.util.Duration, m: Manifest[TPayload]): AlmFuture[RegistrationHolder] = {
    def wrappedHandler(message: Message[T]): Unit =
      handler(message.asInstanceOf[Message[TPayload]])
    def wrappedClassifier(message: Message[T]) = 
      if(m.erasure.isAssignableFrom(message.payload.getClass()))
      	classifier(message.asInstanceOf[Message[TPayload]])
      else
      	false
    <-* (wrappedHandler, wrappedClassifier)
  }
	
  def <-#[TPayload <: T](handler: Message[TPayload] => Unit)(implicit atMost: akka.util.Duration, m: Manifest[TPayload]): AlmFuture[RegistrationHolder] = 
  	<-# [TPayload](handler, (_: Message[TPayload]) => true)

  def <-<#[TPayload <: T](handler: TPayload => Unit, classifier: TPayload => Boolean)(implicit atMost: akka.util.Duration, m: Manifest[TPayload]): AlmFuture[RegistrationHolder] = 
 	<-# ((x: Message[TPayload]) => handler(x.payload), ((x: Message[TPayload]) => classifier(x.payload)))

  def <-<#[TPayload <: T](handler: TPayload => Unit)(implicit atMost: akka.util.Duration, m: Manifest[TPayload]): AlmFuture[RegistrationHolder] = 
 	<-# ((x: Message[TPayload]) => handler(x.payload), ((x: Message[TPayload]) => true))

}


