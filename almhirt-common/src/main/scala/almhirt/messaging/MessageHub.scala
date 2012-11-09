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
import akka.util._
import akka.dispatch._
import almhirt._
import almhirt.almfuture.all._

trait MessageHub extends CreatesMessageChannels with CanBroadcastMessages with ActorBased with Closeable

object MessageHub {
  def apply(actor: ActorRef, futureExecutionContext: ExecutionContext): MessageHub = {
    new ActorBasedMessageHubImpl(actor)(futureExecutionContext)
  }

  def apply(name: String)(implicit almhirtsystem: AlmhirtSystem): MessageHub = {
    val actor =
      almhirtsystem.messageHubDispatcherName match {
        case None => almhirtsystem.actorSystem.actorOf(Props[MessageHubActor], name = name)
        case Some(dn) => almhirtsystem.actorSystem.actorOf(Props[MessageHubActor].withDispatcher(dn), name = name)
      }
    actor ! UseAlmhirtSystemMessage(almhirtsystem)
    apply(actor, almhirtsystem.futureDispatcher)
  }

  private class ActorBasedMessageHubImpl(val actor: ActorRef)(implicit futureExecutionContext: ExecutionContext) extends MessageHub {
    def createMessageChannel[TPayload <: AnyRef](name: String)(implicit atMost: akka.util.Duration, m: Manifest[TPayload]): AlmFuture[MessageChannel[TPayload]] = {
      (actor ? CreateSubChannel(name, MessagePredicate[TPayload]))(atMost)
        .mapTo[NewSubChannel]
        .map(subchannel => subchannel.channel).toAlmFuture[ActorRef]
        .map(newActor => MessageChannel[TPayload](newActor, futureExecutionContext))
    }
    
    def broadcast(message: Message[AnyRef], topic: Option[String]) = actor ! BroadcastMessage(message)
    
    def close() {}
  }

}
