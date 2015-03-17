package almhirt.streaming

import scala.concurrent.duration.FiniteDuration
import scala.reflect.ClassTag
import akka.actor._
import almhirt.almvalidation.kit._
import akka.stream.actor._
import akka.stream.scaladsl.ImplicitFlowMaterializer
import org.reactivestreams.Subscriber

object StreamThrottle {
  def apply[T](actor: ActorRef): Subscriber[T] =
    ActorSubscriber[T](actor)

  def props[T: ClassTag](delayAfterEach: FiniteDuration): Props =
    Props(new StreamThrottleImpl[T](delayAfterEach))

  def create[T: ClassTag](delayAfterEach: FiniteDuration)(implicit factory: ActorRefFactory): Subscriber[T] =
    StreamThrottle(factory.actorOf(props(delayAfterEach)))
}

private[almhirt] class StreamThrottleImpl[T: ClassTag](delayAfterEach: FiniteDuration) extends ActorSubscriber {

  protected def requestStrategy: RequestStrategy = ZeroRequestStrategy

  private object Request
  def receive: Receive = {
    case Request ⇒
      this.request(1)

    case ActorSubscriberMessage.OnNext(element: Any) ⇒
      element.castTo[T].fold(
        fail ⇒ {
          this.cancel()
          sys.error(fail.escalate)
        },
        succ ⇒ context.system.scheduler.scheduleOnce(delayAfterEach, self, Request)(context.dispatcher))

    case ActorSubscriberMessage.OnComplete ⇒
      context.stop(self)
  }

  override def preStart() {
    self ! Request
  }
}