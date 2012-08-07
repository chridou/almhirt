package almhirt.messaging

import akka.actor._
import akka.pattern._
import akka.util.Timeout
import almhirt.almakka._
import almhirt.concurrent.AlmFuture
import almhirt.concurrent.AlmFuture._

class ActorBasedMessageHub(dispatcher: ActorRef) extends MessageHub {
  implicit val timeout =  Timeout(AlmAkka.defaultTimeoutDuration)
  
  def deliver(message: Message[AnyRef]) {
    dispatcher ! PublishMessageCommand(message)
  }

  def createMessageChannel(topic: Option[String]): AlmFuture[MessageChannel] =
    (dispatcher ? CreateMessageChannelCommand(topic)).toAlmFuture[MessageChannel]
  
  def close() {}
}

object ActorBasedMessageHub {
  import AlmAkka._
  def apply(): ActorBasedMessageHub =
  	new ActorBasedMessageHub(
  	    actorSystem.actorOf(
  	        Props(new ActorMessageHubDispatcher()).withDispatcher("almhirt.messagehub-dispatcher"), "almhirt-messagehub"
  	))
}