package almhirt.messaging

import scala.reflect.ClassTag
import almhirt.common._
import akka.actor.ActorRef

trait MessageBus {
  def subscribe(subscriber: ActorRef, classifier: Classifier[AnyRef]): AlmFuture[Subscription]
  def subscribe(subscriber: ActorRef): AlmFuture[Subscription]
  def publishMessage(message: Message)
  def channel[T <: AnyRef](implicit tag: ClassTag[T]): AlmFuture[MessageStream[T]]
  def channel[T <: AnyRef](classifier: Classifier[T])(implicit tag: ClassTag[T]): AlmFuture[MessageStream[T]]
}

object MessageBus {
  def publish(messageBus: MessageBus, payload: AnyRef)(implicit ccuad: CanCreateUuidsAndDateTimes) {
    messageBus.publishMessage(Message(payload))
  }

  def publish(messageBus: MessageBus, payload: AnyRef, metaData: Map[String, String])(implicit ccuad: CanCreateUuidsAndDateTimes) {
    messageBus.publishMessage(Message(payload, metaData))
  }

  implicit class MessageBusOps(self: MessageBus) {
    def publish(payload: AnyRef)(implicit ccuad: CanCreateUuidsAndDateTimes) {
      MessageBus.publish(self, payload)
    }

    def publish(payload: AnyRef, metaData: Map[String, String])(implicit ccuad: CanCreateUuidsAndDateTimes) {
      MessageBus.publish(self, payload, metaData)
    }
  }
}
