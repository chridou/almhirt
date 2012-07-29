package almhirt.messaging

import scalaz.syntax.validation._
import akka.actor._
import almhirt.validation._
import almhirt.almakka._

class ActorMessageHubDispatcher extends Actor with AlmActorLogging with AlmAkka {
  private var streams: List[ActorBasedMessageStream] = List()

  private def unregisterStream(stream: ActorBasedMessageStream) {
    streams = streams.filterNot(x => x == stream)
  }
  
  private def createAndRegisterChannel(pattern: Option[String]): AlmValidation[MessageStream] = {
	  val dispatcher = 
	  	    defaultActorSystem.actorOf(
	  	        Props(new ActorMessageStreamDispatcher()).withDispatcher("almhirt.almhirt-messagestream"))
      val newStream = 
        new ActorBasedMessageStream(dispatcher) {
          def close() { self ! UnregisterStream(this) }
          val topicPattern = pattern
      }
      streams = newStream :: streams
      newStream.success[Problem]
  }
  
  def receive = {
    case PublishMessageCommand(msg) => 
      streams.foreach(_.publish(msg))
    case CreateMessageStreamCommand(filter) =>
      sender ! createAndRegisterChannel(filter)
    case UnregisterStream(stream) =>
      unregisterStream(stream)
  }

  private case class UnregisterStream(stream: ActorBasedMessageStream)
}