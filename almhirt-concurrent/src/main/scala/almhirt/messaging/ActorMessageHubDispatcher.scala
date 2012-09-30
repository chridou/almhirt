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
import scalaz.syntax.validation._
import akka.actor._
import almhirt._
import almhirt.almakka._

class ActorMessageHubDispatcher(implicit almAkkaContext: AlmAkkaContext) extends Actor with AlmActorLogging {
  private var channels: List[ActorBasedMessageChannel] = List()

  private def unregisterChannel(channel: ActorBasedMessageChannel) {
    channels = channels.filterNot(x => x == channel)
  }
  
  private def createAndRegisterChannel(pattern: Option[String]): AlmValidation[MessageChannel] = {
      val dispatcher =
	  	almAkkaContext.messageStreamDispatcherName match {
	      case Some(dispatcherName) =>
	        almAkkaContext.actorSystem.actorOf(Props(new ActorMessageChannelDispatcher()).withDispatcher(dispatcherName))
	      case None =>
	        almAkkaContext.actorSystem.actorOf(Props(new ActorMessageChannelDispatcher()))
	    }
      val newChannel = 
        new ActorBasedMessageChannel(dispatcher) { channel =>
	      val registration = 
	        Some( new Registration[UUID] {
	                val ticket = UUID.randomUUID
	                def dispose() { self ! UnregisterChannel(channel)} })
          val topicPattern = pattern
        }
      channels = newChannel :: channels
      newChannel.success[Problem]
  }
  
  def receive = {
    case PublishMessageCommand(msg) => 
      channels.foreach(_.deliver(msg))
    case CreateMessageChannelCommand(filter) =>
      sender ! createAndRegisterChannel(filter)
    case UnregisterChannel(channel) =>
      unregisterChannel(channel)
  }

  private case class UnregisterChannel(channel: ActorBasedMessageChannel)
  
  override def preStart() { log.info("MessageHubDispatcher '%s' starting".format(self.path)) } 
  override def postRestart(reason: Throwable) { log.info("MessageHubDispatcher '%s' restarted".format(self.path)) } 
  override def postStop() { log.info("MessageHubDispatcher '%s' stopped".format(self.path)) } 
  
}