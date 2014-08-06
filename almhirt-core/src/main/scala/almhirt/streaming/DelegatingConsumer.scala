package almhirt.streaming

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import org.reactivestreams.api.{ Consumer }
import org.reactivestreams.spi.{ Subscription, Subscriber }
import akka.actor._
import almhirt.common._

object DelegatingEventConsumer {
  def apply[T <: Event](delegateTo: ActorRef): Consumer[T] =
    new DelegatingConsumer[T](delegateTo)
}

object DelegatingCommandConsumer {
  def apply[T <: Command](delegateTo: ActorRef): Consumer[T] =
    new DelegatingConsumer[T](delegateTo)
}

private[streaming] class DelegatingConsumer[T](delegateTo: ActorRef) extends Consumer[T] {
  private[this] var sub: Option[Subscription] = None

  private def requestMore() = sub.map(_.requestMore(1))

  override def getSubscriber: Subscriber[T] = new Subscriber[T] {

    override def onError(cause: Throwable): Unit =
      scala.sys.error(cause.getMessage)

    override def onSubscribe(subscription: Subscription): Unit = {
      sub = Some(subscription)
      requestMore()
    }

    override def onComplete(): Unit = {
    }

    override def onNext(element: T): Unit = {
      delegateTo ! element
      requestMore()
    }
  }
}

