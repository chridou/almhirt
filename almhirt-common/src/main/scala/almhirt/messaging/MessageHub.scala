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

import akka.actor._
import akka.pattern._
import akka.util.Timeout
import akka.dispatch.ExecutionContext
import almhirt._
import almhirt.Closeable
import almhirt.messaging.impl.ActorBasedMessageHub

trait MessageHub extends CreatesMessageChannels with CanBroadcastMessages with Closeable

object MessageHub {
  def apply(name: Option[String])(implicit almhirtsystem: AlmhirtSystem): MessageHub = impl.ActorBasedMessageHub(name, almhirtsystem)
  def apply(implicit almhirtsystem: AlmhirtSystem): MessageHub = apply(None)(almhirtsystem)
  def apply(name: Option[String], actorSystem: ActorRefFactory, timeout: Timeout, futureDispatcher: ExecutionContext, actorDispatcherName: Option[String]): MessageHub =
    ActorBasedMessageHub(name, actorSystem, timeout, futureDispatcher, actorDispatcherName)
}
