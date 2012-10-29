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
import akka.actor._
import akka.pattern._
import akka.util.Timeout
import akka.dispatch._
import almhirt._

trait MessageChannel[T <: AnyRef] extends MessageStream[T] with CanDeliverMessages[T] with CanCreateSubChannels[T]

object MessageChannel{ 
  def apply[T <: AnyRef](name: Option[String])(implicit almhirtsystem: AlmhirtSystem, m: Manifest[T]): MessageChannel[T] = {
	impl.ActorBasedMessageChannel[T](name, almhirtsystem)
  }
  def apply[T <: AnyRef](name: Option[String], actorSystem: ActorRefFactory, timeout: Timeout, futureDispatcher: ExecutionContext, actorDispatcherName: Option[String], registration: Option[RegistrationHolder], topicPattern: Option[String])(implicit m: Manifest[T]): MessageChannel[T] = 
	impl.ActorBasedMessageChannel[T](name, actorSystem, timeout, futureDispatcher, actorDispatcherName, registration, topicPattern)(m)
    
}