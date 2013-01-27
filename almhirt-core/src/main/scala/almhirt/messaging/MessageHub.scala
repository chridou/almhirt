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

import scala.reflect.ClassTag
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
import almhirt.environment._
import almhirt.environment.configuration._
import almhirt.almakka.ActorBased
import com.typesafe.config.Config

trait MessageHub extends CreatesMessageChannels with CanBroadcastMessages with ActorBased with Closeable {
  def post(message: Message[AnyRef]): Unit
}

object MessageHub {
  def apply(actor: ActorRef, hasExecutionContext: HasExecutionContext): MessageHub = {
    new ActorBasedMessageHubImpl(actor)(hasExecutionContext)
  }

  def apply(name: String, dispatcherName: Option[String], channelsDispatcherName: Option[String])(implicit hasActorSystem: HasActorSystem, hasExecutionContext: HasExecutionContext): MessageHub = {
    val actor =
      dispatcherName match {
        case None => hasActorSystem.actorSystem.actorOf(Props(new MessageHubActor(channelsDispatcherName)), name = name)
        case Some(dn) => hasActorSystem.actorSystem.actorOf(Props(new MessageHubActor(channelsDispatcherName)).withDispatcher(dn), name = name)
      }
    apply(actor, hasExecutionContext)
  }

  def apply(name: String, dispatcherName: Option[String])(implicit hasActorSystem: HasActorSystem, hasExecutionContext: HasExecutionContext): MessageHub = {
    apply(name, dispatcherName, None)
  }
  
  def apply(name: String)(implicit hasActorSystem: HasActorSystem, hasExecutionContext: HasExecutionContext): MessageHub = {
    apply(name, None, None)
  }

  def apply(name: String, config: Config)(implicit hasActorSystem: HasActorSystem, hasExecutionContext: HasExecutionContext): MessageHub = {
    val messageHubDispatcherName = ConfigHelper.lookupDispatcherConfigPath(config)(ConfigPaths.messagehub).toOption
    val messageChannelsDispatcherName = ConfigHelper.lookupDispatcherConfigPath(config)(ConfigPaths.messagechannels).toOption
    MessageHub(name, messageHubDispatcherName, messageChannelsDispatcherName)(hasActorSystem, hasExecutionContext)
  }
  
  
  private class ActorBasedMessageHubImpl(val actor: ActorRef)(implicit hasExecutionContext: HasExecutionContext) extends MessageHub {
    def createMessageChannel[TPayload <: AnyRef](name: String)(implicit atMost: FiniteDuration, m: ClassTag[TPayload]): AlmFuture[MessageChannel[TPayload]] = {
      (actor ? CreateSubChannelQry(name, MessagePredicate[TPayload]))(atMost)
        .mapTo[NewSubChannelRsp]
        .map(subchannel => subchannel.channel)(hasExecutionContext).mapToAlmFuture[ActorRef]
        .map(newActor => MessageChannel[TPayload](newActor, hasExecutionContext))
    }

    def broadcast(message: Message[AnyRef], topic: Option[String]) = actor ! BroadcastMessageCmd(message)
    def post(message: Message[AnyRef]) = actor ! PostMessageCmd(message)

    def close() { actor ! PoisonPill }
  }

}
