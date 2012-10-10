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
import almhirt._
import almakka.{AlmAkkaContext}
import almvalidation.kit._
import almfuture.all._

abstract class ActorBasedMessageChannel(dispatcher: ActorRef)(implicit almAkkaContext: AlmAkkaContext) extends MessageChannel {
    implicit def timeout = Timeout(almAkkaContext.mediumDuration)
    implicit def executionContext = almAkkaContext.futureDispatcher
    def <*(handler: Message[AnyRef] => Unit, classifier: Message[AnyRef] => Boolean): AlmFuture[RegistrationHolder] = {
	  (dispatcher ? RegisterWildCardMessageHandlerCommand(handler, classifier)).toAlmFuture[RegistrationHolder]
    }
	
	def deliver(message: Message[AnyRef]) = dispatcher ! PublishMessageCommand(message)

    def createSubChannel(classifier: Message[AnyRef] => Boolean): AlmFuture[MessageChannel] = {
	  val newDispatcher = 
	  	almAkkaContext.messageStreamDispatcherName match {
	      case Some(dispatcherName) =>
	        almAkkaContext.actorSystem.actorOf(Props(new ActorMessageChannelDispatcher()).withDispatcher(dispatcherName))
	      case None =>
	        almAkkaContext.actorSystem.actorOf(Props(new ActorMessageChannelDispatcher()))
	    }
	  	        
	  val subscription = this.<*(msg => dispatcher ! PublishMessageCommand(msg) , classifier)
	  subscription.map{ s =>
	  	new ActorBasedMessageChannel(newDispatcher) {
	      val registration = Some(s)
	      val topicPattern = ActorBasedMessageChannel.this.topicPattern
	    }
	  }
	}
}	

object ActorBasedMessageChannel {
  def apply(name: String, createActor: (Props, String) => ActorRef)(implicit almAkkaContext: AlmAkkaContext): ActorBasedMessageChannel = 
  	new ActorBasedMessageChannelImpl(createActor(Props(new ActorMessageChannelDispatcher()), name))
	
  def apply(name: String)(implicit almAkkaContext: AlmAkkaContext): ActorBasedMessageChannel =
  	apply(name, (p: Props, n: String) => almAkkaContext.actorSystem.actorOf(p, n))
  	
  def apply(aChannelDispatcher: ActorRef)(implicit almAkkaContext: AlmAkkaContext): ActorBasedMessageChannel =
  	new ActorBasedMessageChannelImpl(aChannelDispatcher)
  
  def apply(implicit almAkkaContext: AlmAkkaContext): ActorBasedMessageChannel = {
	  val newDispatcher = 
	  	almAkkaContext.messageStreamDispatcherName match {
	      case Some(dispatcherName) =>
	        almAkkaContext.actorSystem.actorOf(Props(new ActorMessageChannelDispatcher()).withDispatcher(dispatcherName))
	      case None =>
	        almAkkaContext.actorSystem.actorOf(Props(new ActorMessageChannelDispatcher()))
	    }
  	new ActorBasedMessageChannelImpl(newDispatcher)
  }
  
  private class ActorBasedMessageChannelImpl(dispatcher: ActorRef)(implicit almAkkaContext: AlmAkkaContext) extends ActorBasedMessageChannel(dispatcher) {
    val topicPattern = None
    val registration = None
  }
}