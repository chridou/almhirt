package almhirt.streaming

import akka.actor._
import akka.stream.actor.ActorConsumer

object ActorDelegatingConsumer {
  def props(delegateTo: ActorRef, bufferSize: Int): Props =
    Props(new ActorDelegatingConsumerImpl(Some(delegateTo), bufferSize))

  def props(delegateTo: ActorRef): Props =
    props(delegateTo, 16)
}

object ActorDevNullConsumer {
  def props(bufferSize: Int): Props =
    Props(new ActorDelegatingConsumerImpl(None, bufferSize))
    
  def props(): Props =
    Props(new ActorDelegatingConsumerImpl(None, 16))
    
  def create(bufferSize: Int, actorname: String)(implicit system: ActorRefFactory): ActorRef =
    system.actorOf(props(bufferSize), actorname)
    
  def create(actorname: String)(implicit system: ActorRefFactory): ActorRef =
    create(16, actorname)
    
}

private[almhirt] class ActorDelegatingConsumerImpl(delegateTo: Option[ActorRef], bufferSize: Int) extends ActorConsumer {

  protected def requestStrategy: ActorConsumer.RequestStrategy = ActorConsumer.ZeroRequestStrategy

  protected var received: Int = 0

  def receive: Receive = {
    case ActorConsumer.OnNext(element: Any) =>
      delegateTo.foreach(_ ! element)
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