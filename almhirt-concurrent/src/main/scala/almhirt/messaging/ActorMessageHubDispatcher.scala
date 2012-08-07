package almhirt.messaging

import java.util.UUID
import scalaz.syntax.validation._
import akka.actor._
import almhirt.Registration
import almhirt.validation._
import almhirt.almakka._

class ActorMessageHubDispatcher extends Actor with AlmActorLogging with AlmAkkaDefaults {
  private var channels: List[ActorBasedMessageChannel] = List()

  private def unregisterStream(channel: ActorBasedMessageChannel) {
    channels = channels.filterNot(x => x == channel)
  }
  
  private def createAndRegisterChannel(pattern: Option[String]): AlmValidation[MessageChannel] = {
	  val dispatcher = 
	  	    defaultActorSystem.actorOf(
	  	        Props(new ActorMessageChannelDispatcher()).withDispatcher("almhirt.almhirt-messagestream"))
      val newChannel = 
        new ActorBasedMessageChannel(dispatcher) { channel =>
	      val registration = 
	        Some( new Registration[UUID] {
	                val ticket = UUID.randomUUID
	                def dispose() { self ! UnregisterStream(channel)} })
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
    case UnregisterStream(stream) =>
      unregisterStream(stream)
  }

  private case class UnregisterStream(channel: ActorBasedMessageChannel)
}