package almhirt.messaging

import almhirt.common._
import akka.actor.ActorRef

trait MessagePublisher {
  def publishMessage(message: Message)
}

object MessagePublisher {
  def publish(messageBus: MessagePublisher, payload: AnyRef)(implicit ccuad: CanCreateUuidsAndDateTimes) {
    messageBus.publishMessage(Message(payload))
  }

  def publish(messageBus: MessagePublisher, payload: AnyRef, metaData: Map[String, String])(implicit ccuad: CanCreateUuidsAndDateTimes) {
    messageBus.publishMessage(Message(payload, metaData))
  }

  def sendToActor(actor: ActorRef): MessagePublisher =
    new MessagePublisher {
      def publishMessage(message: Message) { actor ! message }
    }

  val devNull: MessagePublisher =
    new MessagePublisher {
      def publishMessage(message: Message) = {}
    }

  implicit class MessagePublisherOps(self: MessagePublisher) {
    def publish(payload: AnyRef)(implicit ccuad: CanCreateUuidsAndDateTimes) {
      MessagePublisher.publish(self, payload)
    }

    def publish(payload: AnyRef, metaData: Map[String, String])(implicit ccuad: CanCreateUuidsAndDateTimes) {
      MessagePublisher.publish(self, payload, metaData)
    }
  }
}
