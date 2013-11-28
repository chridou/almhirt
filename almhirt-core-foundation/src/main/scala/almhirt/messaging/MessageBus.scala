package almhirt.messaging

import scala.reflect.ClassTag
import almhirt.common._
import akka.actor._

  import akka.actor._

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
