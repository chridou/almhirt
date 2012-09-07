package almhirt.messaging

import java.util.UUID
import akka.actor._
import akka.pattern._
import akka.util.Timeout
import almhirt._
import almakka.{AlmAkkaContext}
import almvalidation.kit._
import concurrent.all._

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