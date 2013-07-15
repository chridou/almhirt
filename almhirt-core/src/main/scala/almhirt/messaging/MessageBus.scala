package almhirt.messaging

import scala.reflect.ClassTag
import almhirt.common._
import akka.actor._

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

  implicit class MessagePublisherOps(self: MessagePublisher) {
    def publish(payload: AnyRef)(implicit ccuad: CanCreateUuidsAndDateTimes) {
      MessagePublisher.publish(self, payload)
    }

    def publish(payload: AnyRef, metaData: Map[String, String])(implicit ccuad: CanCreateUuidsAndDateTimes) {
      MessagePublisher.publish(self, payload, metaData)
    }
  }
  
}

trait MessageBus extends MessagePublisher {
  def subscribe(subscriber: ActorRef, classifier: Classifier[AnyRef]): AlmFuture[Subscription]
  def subscribe(subscriber: ActorRef): AlmFuture[Subscription]
  def channel[T <: AnyRef](implicit tag: ClassTag[T]): AlmFuture[MessageChannel[T]]
  def channel[T <: AnyRef](classifier: Classifier[T])(implicit tag: ClassTag[T]): AlmFuture[MessageChannel[T]]
}

object MessageBus {
  def apply(system: ActorSystem): AlmFuture[(MessageBus, CloseHandle)] =
    impl.ActorSystemEventStreamMessageBus(system)
  
}
