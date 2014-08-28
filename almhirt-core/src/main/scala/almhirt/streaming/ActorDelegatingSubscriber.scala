package almhirt.streaming

import akka.actor._
import akka.stream.actor._

object ActorDelegatingSubscriber {
  def props(delegateTo: ActorRef, bufferSize: Int): Props =
    Props(new ActorDelegatingSubscriberImpl(Some(delegateTo), bufferSize))

  def props(delegateTo: ActorRef): Props =
    props(delegateTo, 16)
}

object ActorDevNullSubscriber {
  def props(bufferSize: Int): Props =
    Props(new ActorDelegatingSubscriberImpl(None, bufferSize))
    
  def props(): Props =
    Props(new ActorDelegatingSubscriberImpl(None, 16))
    
  def create(bufferSize: Int, actorname: String)(implicit system: ActorRefFactory): ActorRef =
    system.actorOf(props(bufferSize), actorname)
    
  def create(actorname: String)(implicit system: ActorRefFactory): ActorRef =
    create(16, actorname)
    
}

private[almhirt] class ActorDelegatingSubscriberImpl(delegateTo: Option[ActorRef], bufferSize: Int) extends ActorSubscriber {

  protected def requestStrategy: RequestStrategy = ZeroRequestStrategy

  protected var received: Int = 0

  def receive: Receive = {
    case ActorSubscriberMessage.OnNext(element: Any) ⇒
      delegateTo.foreach(_ ! element)
      received += 1
      if (received == bufferSize) {
        request(bufferSize)
        received = 0
      }

    case ActorSubscriberMessage.OnComplete ⇒
      context.stop(self)
  }

  override def preStart() {
    this.request(bufferSize)
  }
}