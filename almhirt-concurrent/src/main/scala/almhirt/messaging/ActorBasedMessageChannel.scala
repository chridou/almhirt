package almhirt.messaging

import java.util.UUID
import akka.actor._
import akka.pattern._
import akka.util.Timeout
import almhirt._
import almhirt.almakka.AlmAkka
import almhirt.concurrent.AlmFuture
import almhirt.concurrent.AlmFuture._

abstract class ActorBasedMessageChannel(dispatcher: ActorRef) extends MessageChannel with AlmAkka {
	implicit val timeout = Timeout(defaultTimeoutDuration)
	
    def <*(handler: Message[AnyRef] => Unit, classifier: Message[AnyRef] => Boolean): AlmFuture[Registration[UUID]] = {
	  ask(dispatcher, RegisterMessageHandlerCommand(handler, classifier)).toAlmFuture[Registration[UUID]]
    }
	
	def deliver(message: Message[AnyRef]) = dispatcher ! PublishMessageCommand(message)

    def subStream(classifier: Message[AnyRef] => Boolean): AlmFuture[MessageStream] = {
	  val dispatcher = 
	  	    defaultActorSystem.actorOf(
	  	        Props(new ActorMessageChannelDispatcher()).withDispatcher("almhirt.almhirt-messagestream"))
	  val subscription = this.<*(msg => dispatcher ! PublishMessageCommand(msg) , classifier)
	  subscription.map{ s =>
	  	new ActorBasedMessageChannel(dispatcher) {
	      val registration = Some(s)
	      val topicPattern = ActorBasedMessageChannel.this.topicPattern
	    }
	  }
	}
}	

object ActorBasedMessageChannel {
  import AlmAkka._
  def apply(name: String, createActor: (Props, String) => ActorRef): ActorBasedMessageChannel = 
  	new ActorBasedMessageChannelImpl(createActor(Props(new ActorMessageChannelDispatcher()), name))
	
  def apply(name: String)(implicit actorFactory: ActorRefFactory): ActorBasedMessageChannel =
  	apply(name, (p: Props, n: String) => actorFactory.actorOf(p, n))
  	
  def apply(actorFactory: ActorRefFactory): ActorBasedMessageChannel =
  	apply(java.util.UUID.randomUUID().toString)(actorFactory)

  def apply(aChannelDispatcher: ActorRef): ActorBasedMessageChannel =
  	new ActorBasedMessageChannelImpl(aChannelDispatcher)
  
  def apply(): ActorBasedMessageChannel =
  	new ActorBasedMessageChannelImpl(
  	    actorSystem.actorOf(
  	        Props(new ActorMessageChannelDispatcher()).withDispatcher("almhirt.messagestream-dispatcher"), "almhirt-messagestream"
  	))
  
  private class ActorBasedMessageChannelImpl(dispatcher: ActorRef) extends ActorBasedMessageChannel(dispatcher) {
    val topicPattern = None
    val registration = None
  }
}