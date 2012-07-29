package almhirt.messaging

import akka.actor._
import akka.pattern._
import akka.util.Timeout
import almhirt._
import almhirt.almakka.AlmAkka
import almhirt.concurrent.AlmFuture
import almhirt.concurrent.AlmFuture._

abstract class ActorBasedMessageStream(dispatcher: ActorRef) extends MessageStream with AlmAkka {
	implicit val timeout = Timeout(defaultTimeoutDuration)
	
    def +?=(handler: Message[AnyRef] => Unit, classifier: Message[AnyRef] => Boolean): AlmFuture[Disposable] = {
	  ask(dispatcher, RegisterMessageHandlerCommand(handler, classifier)).toAlmFuture[Disposable]
    }
	
	def publish(message: Message[AnyRef]) = dispatcher ! PublishMessageCommand(message)

    def subStream(classifier: Message[AnyRef] => Boolean): AlmFuture[MessageStream] = {
	  val dispatcher = 
	  	    defaultActorSystem.actorOf(
	  	        Props(new ActorMessageStreamDispatcher()).withDispatcher("almhirt.almhirt-messagestream"))
	  val subscription = this.+?=(msg => dispatcher ! PublishMessageCommand(msg) , classifier)
	  subscription.map{ s =>
	  	new ActorBasedMessageStream(dispatcher) {
	      override def close() {s.dispose}
	      val topicPattern = ActorBasedMessageStream.this.topicPattern
	    }
	  }
	}
}	

object ActorBasedMessageStream {
  import AlmAkka._
  def apply(name: String, createActor: (Props, String) => ActorRef): ActorBasedMessageStream = 
  	new ActorBasedMessageStreamImpl(createActor(Props(new ActorMessageStreamDispatcher()), name))
	
  def apply(name: String)(implicit actorFactory: ActorRefFactory): ActorBasedMessageStream =
  	apply(name, (p: Props, n: String) => actorFactory.actorOf(p, n))
  	
  def apply(actorFactory: ActorRefFactory): ActorBasedMessageStream =
  	apply(java.util.UUID.randomUUID().toString)(actorFactory)

  def apply(aChannelDispatcher: ActorRef): ActorBasedMessageStream =
  	new ActorBasedMessageStreamImpl(aChannelDispatcher)
  
  def apply(): ActorBasedMessageStream =
  	new ActorBasedMessageStreamImpl(
  	    actorSystem.actorOf(
  	        Props(new ActorMessageStreamDispatcher()).withDispatcher("almhirt.almhirt-messagestream"), "almhirt-messagestream"
  	))
  
  private class ActorBasedMessageStreamImpl(dispatcher: ActorRef) extends ActorBasedMessageStream(dispatcher) {
    def close() = ()
    val topicPattern = None
  }
}