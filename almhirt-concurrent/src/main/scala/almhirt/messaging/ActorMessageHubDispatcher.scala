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