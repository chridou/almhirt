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

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration
import java.util.UUID
import akka.actor._
import akka.pattern._
import akka.util._
import akka.dispatch._
import almhirt.core._
import almhirt.common._
import almhirt.almfuture.all._
import almhirt.messaging.impl.MessageHubActor
import almhirt.environment.AlmhirtSystem
import almhirt.environment.configuration.ConfigPaths
import almhirt.environment.configuration.ConfigHelper
import almhirt.almakka.ActorBased
import almhirt.common.AlmFuture

trait MessageHub extends CreatesMessageChannels with CanBroadcastMessages with ActorBased with Closeable{
   def post(message: Message[AnyRef]): Unit
}

object MessageHub {
  def apply(actor: ActorRef, futureExecutionContext: ExecutionContext): MessageHub = {
    new ActorBasedMessageHubImpl(actor)(futureExecutionContext)
  }

  def apply(name: String)(implicit almhirtsystem: AlmhirtSystem): MessageHub = {
    val actor =
      ConfigHelper.tryGetDispatcherName(almhirtsystem.config)(ConfigPaths.messagehub) match {
        case None => almhirtsystem.actorSystem.actorOf(Props[MessageHubActor], name = name)
        case Some(dn) => almhirtsystem.actorSystem.actorOf(Props[MessageHubActor].withDispatcher(dn), name = name)
      }
    actor ! UseAlmhirtSystemMessage(almhirtsystem)
    apply(actor, almhirtsystem.executionContext)
  }

  private class ActorBasedMessageHubImpl(val actor: ActorRef)(implicit futureExecutionContext: ExecutionContext) extends MessageHub {
    def createMessageChannel[TPayload <: AnyRef](name: String)(implicit atMost: FiniteDuration, m: Manifest[TPayload]): AlmFuture[MessageChannel[TPayload]] = {
      (actor ? CreateSubChannelQry(name, MessagePredicate[TPayload]))(atMost)
        .mapTo[NewSubChannelRsp]
        .map(subchannel => subchannel.channel).mapToAlmFuture[ActorRef]
        .map(newActor => MessageChannel[TPayload](newActor, futureExecutionContext))
    }

    def broadcast(message: Message[AnyRef], topic: Option[String]) = actor ! BroadcastMessageCmd(message)
    def post(message: Message[AnyRef]) = actor ! PostMessageCmd(message)

    def close() {}
  }

}
