package almhirt.messaging

import akka.actor._
import akka.pattern._
import akka.util.Timeout
import almhirt.almakka._
import almhirt.concurrent.AlmFuture
import almhirt.concurrent.AlmFuture._

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