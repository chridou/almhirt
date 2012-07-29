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

  def createMessageStream(topic: Option[String]): AlmFuture[MessageStream] =
    (dispatcher ? CreateMessageStreamCommand(topic)).toAlmFuture[MessageStream]
  
  def close() {}
}