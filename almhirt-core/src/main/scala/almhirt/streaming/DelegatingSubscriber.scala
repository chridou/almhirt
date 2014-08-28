package almhirt.streaming

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import org.reactivestreams.{ Subscriber, Subscription }
import akka.actor._
import almhirt.common._

object DelegatingSubscriber {
  def apply[T](delegateTo: ActorRef): Subscriber[T] =
    new DelegatingSubscriber[T](delegateTo)
}

object DelegatingEventSubscriber {
  def apply[T <: Event](delegateTo: ActorRef): Subscriber[T] =
    DelegatingSubscriber[T](delegateTo)
}

object DelegatingCommandSubscriber {
  def apply[T <: Command](delegateTo: ActorRef): Subscriber[T] =
    DelegatingSubscriber[T](delegateTo)
}

private[streaming] class DelegatingSubscriber[T](delegateTo: ActorRef) extends Subscriber[T] {
  private[this] var sub: Option[Subscription] = None

  private def requestMore() = sub.map(_.request(1))
  override def onError(cause: Throwable): Unit =
    scala.sys.error(cause.getMessage)

  override def onSubscribe(subscription: Subscription): Unit = {
    sub = Some(subscription)
    requestMore()
  }

  override def onComplete(): Unit = {
    sub.foreach(s ⇒ {
      s.cancel()
      sub = None
    })
  }

  override def onNext(element: T): Unit = {
    delegateTo ! element
    requestMore()
  }
}

