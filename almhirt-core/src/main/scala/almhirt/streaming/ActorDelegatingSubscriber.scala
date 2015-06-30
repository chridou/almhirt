package almhirt.streaming

import akka.actor._
import akka.stream.actor._
import org.reactivestreams.Publisher
import akka.stream.scaladsl._

object ActorDelegatingSubscriber {
  def props[T](delegateTo: ActorRef, bufferSize: Int): Props =
    ActorMayBeDelegatingSubscriberWithAutoSubscribe.props[T](Some(delegateTo), bufferSize, None)

  def props[T](delegateTo: ActorRef): Props =
    props[T](delegateTo, 16)
}

object ActorDevNullSubscriber {
  def props(bufferSize: Int): Props =
    ActorMayBeDelegatingSubscriberWithAutoSubscribe.props[Any](None, bufferSize, None)

  def props(): Props =
    ActorMayBeDelegatingSubscriberWithAutoSubscribe.props[Any](None, 16, None)

  //  def create(bufferSize: Int, actorname: String)(implicit system: ActorRefFactory): ActorRef =
  //    system.actorOf(props(bufferSize, autoConnectTo), actorname)
  //
  //  def create(actorname: String)(implicit system: ActorRefFactory): ActorRef =
  //    create(16, actorname)

}

object ActorDevNullSubscriberWithAutoSubscribe {
  def props[T](bufferSize: Int, autoConnectTo: Option[Publisher[T]]): Props =
    Props(new ActorDelegatingSubscriberImpl(None, bufferSize, autoConnectTo))
}

private[almhirt] object ActorMayBeDelegatingSubscriberWithAutoSubscribe {
  def props[T](delegateTo: Option[ActorRef], bufferSize: Int, autoConnectTo: Option[Publisher[T]]): Props =
    Props(new ActorDelegatingSubscriberImpl(delegateTo, bufferSize, autoConnectTo))
}

private[almhirt] class ActorDelegatingSubscriberImpl[T](delegateTo: Option[ActorRef], bufferSize: Int, autoConnectTo: Option[Publisher[T]]) extends ActorSubscriber {
  implicit def implicitFlowMaterializer = akka.stream.ActorMaterializer()(this.context)

  protected def requestStrategy: RequestStrategy = ZeroRequestStrategy

  protected var received: Int = 0

  private object Start
  def receive: Receive = {
    case Start ⇒
      autoConnectTo.foreach(pub ⇒ Source(pub).runWith(Sink(ActorSubscriber[T](self))))
      this.request(bufferSize)

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
    self ! Start
  }
}