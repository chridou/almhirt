package almhirt.streaming

import scala.reflect.ClassTag
import akka.actor._
import almhirt.common._
import org.reactivestreams.api.Producer
import akka.stream.actor.ActorProducer
import akka.stream.actor.ActorProducer.Request

trait DemandingConsumer[TElement] {
  def subscribe(subscriber: OfferingSubscriber[TElement])
}

object DemandingConsumer {
  def apply[TElement: ClassTag](actor: ActorRef): DemandingConsumer[TElement] =
    new DemandingConsumer[TElement] {
      def subscribe(subscriber: OfferingSubscriber[TElement]) {
        actor ! ActorDemandingConsumerInternal.NewSubscriberInternal(subscriber)
      }
    }
}

trait OfferingSubscriber[TElement] {
  def onSubscribe(subscription: SendWhenAskedSubscription[TElement]): Unit
  def onProblem(problem: Problem): Unit
  /** Send exactly the specified amount of elements immediately */
  def onDemand(amount: Int): Unit
  /** No more elements will be accepted */
  def onComplete(): Unit

}

trait SendWhenAskedSubscription[TElement] {
  /** Tell the consumer, that you have elements for him */
  def offerElements(amount: Int): Unit
  /** Send exactly the amount of Elements as demanded by the consumer */
  def sendElements(elements: Seq[TElement]): Unit
  /** You won't send any more elements */
  def cancel(): Unit
}

private[almhirt] object ActorDemandingConsumerInternal {
  import scala.language.existentials
  final case class NewSubscriberInternal(subscriber: OfferingSubscriber[_])
  final case class InternalOffer(subscriber: OfferingSubscriber[_], amount: Int)
  final case class InternalDelivery(subscriber: OfferingSubscriber[_], elements: Seq[_])
  final case class InternalUnsubscribe(subscriber: OfferingSubscriber[_])
}

class ActorDemandingConsumer[TElement] extends Actor with ActorProducer[TElement] {
  import ActorDemandingConsumerInternal._

  private var offers: Vector[OfferingSubscriber[TElement]] = Vector.empty
  private var subscribers: Set[OfferingSubscriber[TElement]] = Set.empty

  private def addOffer(offerer: OfferingSubscriber[TElement], amount: Int) {
    val newOffers = Vector.fill(amount)(offerer)
    offers = offers ++ newOffers
  }

  private def toSubscr(subscr: OfferingSubscriber[_]): OfferingSubscriber[TElement] =
    subscr.asInstanceOf[OfferingSubscriber[TElement]]

  private def subscribe(subscriber: OfferingSubscriber[TElement]) {
    subscribers = subscribers + toSubscr(subscriber)
    subscriber.onSubscribe(new SendWhenAskedSubscription[TElement] {
      def offerElements(amount: Int) { self ! InternalOffer(subscriber, amount) }
      def sendElements(elements: Seq[TElement]) { self ! InternalDelivery(subscriber, elements) }
      def cancel() { self ! InternalUnsubscribe(subscriber) }
    })
  }

  private def collectingOffers: Receive = {
    case NewSubscriberInternal(subscriber) =>
      subscribe(toSubscr(subscriber))

    case InternalOffer(subscriber, amount) =>
      addOffer(toSubscr(subscriber), amount)
      if (!offers.isEmpty && totalDemand > 0)
        demandElements()

    case InternalDelivery(subscriber, _) =>
      subscriber.onProblem(UnspecifiedProblem("You have not been asked to send elements!"))

    case InternalUnsubscribe(subscr) =>
      val subscriber = toSubscr(subscr)
      if (subscribers(subscriber)) {
        subscribers = subscribers - subscriber
        offers = offers.filterNot(_ == subscriber)
        subscriber.onComplete()
      } else {
        subscriber.onProblem(UnspecifiedProblem("You have are not subscribed!"))
      }

    case Request(amount) =>
      if (!offers.isEmpty && totalDemand > 0)
        demandElements()
  }

  private def dispatching(toCollect: Map[OfferingSubscriber[TElement], Int]): Receive = {
    case NewSubscriberInternal(subscriber) =>
      subscribe(toSubscr(subscriber))

    case InternalOffer(subscriber, amount) =>
      addOffer(toSubscr(subscriber), amount)
      if (toCollect == 0 && !offers.isEmpty && totalDemand > 0)
        demandElements()

    case InternalDelivery(sub, events) =>
      val subscriber = toSubscr(sub)

      toCollect.get(subscriber) match {
        case Some(requestedAmount) =>
          if (requestedAmount != events.size)
            subscriber.onProblem(UnspecifiedProblem(s"You have been asked to send $requestedAmount elements, not ${events.size}!"))
          else
            events.foreach(element => onNext(element.asInstanceOf[TElement]))
        case None =>
          subscriber.onProblem(UnspecifiedProblem("You have not been asked to send elements!"))
      }

      val newToCollect = toCollect - subscriber

      if (newToCollect.isEmpty && !offers.isEmpty && totalDemand > 0)
        demandElements()
      else if (newToCollect.isEmpty)
        context.become(collectingOffers)
      else
        context.become(dispatching(newToCollect))

    case InternalUnsubscribe(subscr) =>
      val subscriber = toSubscr(subscr)
      if (subscribers(subscriber)) {
        subscribers = subscribers - subscriber
        offers = offers.filterNot(_ == subscriber)

        subscriber.onComplete()

        val newToCollect = toCollect - subscriber

        if (newToCollect.isEmpty && !offers.isEmpty && totalDemand > 0)
          demandElements()
        else if (newToCollect.isEmpty)
          context.become(collectingOffers)
        else
          context.become(dispatching(newToCollect))
      } else {
        subscriber.onProblem(UnspecifiedProblem("You have are not subscribed!"))
      }

    case Request(amount) =>
      ()

  }

  def receive: Receive = collectingOffers

  private def demandElements() {
    val toDemand =
      offers.take(totalDemand)
        .groupBy(d => d)
        .map { case (subscriber, x) => (subscriber, x.size) }
    val numExpected = toDemand.map(_._2).sum
    offers = offers.drop(numExpected)
    toDemand.foreach { case (subscriber, amount) => subscriber.onDemand(amount) }
    context.become(dispatching(toDemand))
  }
}