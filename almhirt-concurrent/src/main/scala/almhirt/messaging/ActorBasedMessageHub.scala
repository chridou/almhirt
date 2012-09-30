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
import almhirt._
import almhirt.almakka._
import almhirt.almfuture.all._

class ActorBasedMessageHub(dispatcher: ActorRef)(implicit almAkkaContext: AlmAkkaContext) extends MessageHub {
  implicit def timeout = Timeout(almAkkaContext.mediumDuration)
  implicit def executionContext = almAkkaContext.futureDispatcher
  def deliver(message: Message[AnyRef]) {
    dispatcher ! PublishMessageCommand(message)
  }

  def createMessageChannel(topic: Option[String]): AlmFuture[MessageChannel] =
    (dispatcher ? CreateMessageChannelCommand(topic)).toAlmFuture[MessageChannel]
  
  def close() {}
}

object ActorBasedMessageHub {
  def apply(implicit almAkkaContext: AlmAkkaContext): ActorBasedMessageHub = {
    val dispatcher =
	  	almAkkaContext.messageHubDispatcherName match {
	      case Some(dispatcherName) =>
	        almAkkaContext.actorSystem.actorOf(Props(new ActorMessageHubDispatcher()).withDispatcher(dispatcherName), "almhirt-messagehub")
	      case None =>
	        almAkkaContext.actorSystem.actorOf(Props(new ActorMessageHubDispatcher()), "almhirt-messagehub")
	    }
  	new ActorBasedMessageHub(dispatcher)
  }
}