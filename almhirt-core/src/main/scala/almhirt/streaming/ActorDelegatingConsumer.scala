package almhirt.streaming

import akka.actor._
import akka.stream.actor.ActorConsumer

class ActorDelegatingConsumer(delegateTo: ActorRef, bufferSize: Int) extends ActorConsumer {

  protected def requestStrategy: ActorConsumer.RequestStrategy = ActorConsumer.ZeroRequestStrategy

  protected var received: Int = 0

  def receive: Receive = {
    case ActorConsumer.OnNext(element: Any) =>
      delegateTo ! element
      received += 1
      if (received == bufferSize) {
        request(bufferSize)
        received = 0
      }

    case ActorConsumer.OnComplete =>
      context.stop(self)
  }

  override def preStart() {
    this.request(bufferSize)
  }
}