package almhirt.streaming

import akka.actor._
import akka.stream.actor._

/** This thing is unbound. Handle with care! */
trait UnboundPublisher[T] {
  def publish(element: T)
}

object UnboundPublisher {
  def apply[T](unboundPublisher: ActorRef): UnboundPublisher[T] with almhirt.common.Stoppable = {
    new UnboundPublisher[T] with almhirt.common.Stoppable {
      override def publish(element: T) {
        unboundPublisher ! InternalUnboundPublisherMessages.Publish(element)
      }
      override def stop() {
        unboundPublisher ! InternalUnboundPublisherMessages.Stop
      }
    }
  }

  def props[T]: Props = Props(new UnboundActorPublisherImpl[T])
}

private[almhirt] object InternalUnboundPublisherMessages {
  case object Stop
  case class Publish(element: Any)
}

private[almhirt] class UnboundActorPublisherImpl[T]() extends ActorPublisher[T] with ActorLogging {
  private def deliver(queue: Vector[T]): Vector[T] = {
    if (isActive) {
      val toPublish = queue.take(this.totalDemand.toInt)
      val rest = queue.drop(toPublish.size)
      toPublish.foreach(onNext)
      rest
    } else {
      queue
    }
  }

  def receiveRunning(queue: Vector[T]): Receive = {
    case InternalUnboundPublisherMessages.Publish(element) =>
      context.become(receiveRunning(deliver(queue :+ element.asInstanceOf[T])))

    case ActorPublisherMessage.Request(amount) =>
      context.become(receiveRunning(deliver(queue)))

    case ActorPublisherMessage.Cancel =>
      if (!queue.isEmpty && log.isWarningEnabled)
        log.warning(s"${queue.size} element haven't been published on cancel.")
      ()

    case InternalUnboundPublisherMessages.Stop =>
      if (!queue.isEmpty && log.isWarningEnabled)
        log.warning(s"${queue.size} element haven't been published on stop.")

      this.onComplete()
  }

  override def receive: Receive = receiveRunning(Vector.empty)

}