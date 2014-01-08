package almhirt.messaging

import scala.reflect.ClassTag
import almhirt.common._
import akka.actor.ActorRef
import akka.actor.Actor

trait MessageChannel[T] {
  def subscribe(subscriber: ActorRef): AlmFuture[Subscription]
  def subscribe(subscriber: ActorRef, classifier: Classifier[T]): AlmFuture[Subscription]
  def channel[U <: T](implicit tag: ClassTag[U]): AlmFuture[MessageChannel[U]]
  def channel[U <: T](classifier: Classifier[U])(implicit tag: ClassTag[U]): AlmFuture[MessageChannel[U]]
}


