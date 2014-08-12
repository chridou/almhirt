package almhirt.streaming

import akka.actor._
import org.reactivestreams.api.{ Producer, Consumer }
import org.reactivestreams.spi.{ Subscriber, Subscription }
import akka.stream.actor.ActorProducer
import almhirt.common._

object StreamShipper {
  def apply[TElement](actor: ActorRef): (StreamBroker[TElement], Producer[TElement]) = {

    (new StreamBroker[TElement] {
      def signContract(contractor: SuppliesContractor[TElement]) {
        actor ! InternalBrokerMessages.InternalSignContract(contractor)
      }

      def newConsumer(): Consumer[TElement] = {
        import InternalBrokerMessages._
        new Consumer[TElement] {
          def getSubscriber(): Subscriber[TElement] = {
            val subscriberId = java.util.UUID.randomUUID().toString()
            new Subscriber[TElement] {
              override def onError(cause: Throwable): Unit =
                actor ! InternalOnError(subscriberId, cause)

              override def onSubscribe(subscription: Subscription): Unit = {
                actor ! InternalOnSubscribe(subscriberId, subscription)
              }

              override def onComplete(): Unit = {
                actor ! InternalOnComplete(subscriberId)
              }

              override def onNext(element: TElement): Unit = {
                actor ! InternalOnNext(subscriberId, element)
              }
            }
          }
        }
      }
    }, ActorProducer[TElement](actor))
  }

  def props[TElement](): Props = Props(new StreamShipperImpl[TElement]())
}

/** This is a very naive implementation to get started! */
private[almhirt] class StreamShipperImpl[TElement](buffersizePerSubscriber: Int) extends Actor with ActorProducer[TElement] with ActorLogging {
  import ActorProducer._
  import InternalBrokerMessages._

  def this() = this(16)

  def stockroom(forSuppliesContractor: SuppliesContractor[TElement]) = new Stockroom[TElement] {
    def cancelContract() { self ! InternalCancelContract(forSuppliesContractor) }
    def offerSupplies(amount: Int) { self ! InternalOfferSupplies(amount, forSuppliesContractor) }
    def deliverSupplies(elements: Seq[TElement]) { self ! InternalDeliverSupplies(elements, forSuppliesContractor) }
  }

  private case class SubscriptionsState(
    subscriptions: Map[String, Subscription],
    bufferedElements: Vector[(String, TElement)]) {
    def isSubscribed(subscriberId: String) = subscriptions.contains(subscriberId)
    def numBuffered = bufferedElements.size
    def isAnyBuffered: Boolean = !bufferedElements.isEmpty
    def nothingBuffered: Boolean = bufferedElements.isEmpty
    def addSubscription(subscriberId: String, subscription: Subscription): SubscriptionsState = {
      this.copy(subscriptions = this.subscriptions + (subscriberId -> subscription))
    }

    def removeSubscription(subscriberId: String): SubscriptionsState = {
      this.copy(subscriptions = this.subscriptions - subscriberId)
    }

    def addToBuffer(subscriberId: String, element: TElement): SubscriptionsState =
      this.copy(bufferedElements = this.bufferedElements :+ (subscriberId -> element))

    def takeElements(demand: Int): (Vector[(String, TElement)], SubscriptionsState) = {
      val taken = bufferedElements.take(demand)
      (taken, copy(bufferedElements = this.bufferedElements.drop(taken.size)))
    }

    def signalRequestMore(subscriberId: String, amount: Int) {
      subscriptions.get(subscriberId).foreach(_.requestMore(amount))
    }

  }

  def collectingOffers(
    offers: Vector[SuppliesContractor[TElement]],
    contractors: Set[SuppliesContractor[TElement]],
    subscriptions: SubscriptionsState): Receive = {

    case InternalOnSubscribe(subscriberId, subscription) =>
      subscription.requestMore(buffersizePerSubscriber)
      context.become(collectingOffers(offers, contractors, subscriptions.addSubscription(subscriberId, subscription)))

    case InternalOnNext(subscriberId, element) =>
      if (subscriptions.isSubscribed(subscriberId)) {
        val newSubscriptions = subscriptions.addToBuffer(subscriberId, element.asInstanceOf[TElement])
        if (totalDemand > 0) {
          if (newSubscriptions.nothingBuffered && offers.isEmpty) {
            onNext(element.asInstanceOf[TElement])
            subscriptions.signalRequestMore(subscriberId, 1)
          } else {
            initiateDispatch(offers, contractors, totalDemand, newSubscriptions)
          }
        } else {
          context.become(collectingOffers(offers, contractors, newSubscriptions))
        }
      } else {
        log.warning(s"OnNext from unknown subscription $subscriberId for element $element is ignored")
      }

    case InternalOnComplete(subscriberId) =>
      val newSubscriptions = subscriptions.removeSubscription(subscriberId)
      context.become(collectingOffers(offers, contractors, newSubscriptions))

    case InternalOnError(subscriberId, error) =>
      log.error(s"Subscriber $subscriberId reported an error. The subscriber will be removed.")
      context.become(collectingOffers(offers, contractors, subscriptions.removeSubscription(subscriberId)))

    case InternalSignContract(contractor: SuppliesContractor[TElement]) =>
      if (!contractors(contractor)) {
        contractor.onStockroom(stockroom(contractor))
        context.become(collectingOffers(
          offers,
          contractors + contractor,
          subscriptions))
      } else {
        contractor.onProblem(UnspecifiedProblem("[SignContract(collecting)]: You already a contractor!"))
      }

    case InternalCancelContract(contractor: SuppliesContractor[TElement]) =>
      if (contractors(contractor)) {
        context.become(collectingOffers(
          offers.filterNot(_ == contractor),
          contractors - contractor,
          subscriptions))
      } else {
        contractor.onProblem(UnspecifiedProblem("[CancelContract(collecting)]: You are not a contractor!"))
      }

    case InternalOfferSupplies(amount: Int, contractor: SuppliesContractor[TElement]) =>
      if (contractors(contractor)) {
        val newOffers = offers ++ Vector.fill(amount)(contractor)
        if (totalDemand > 0 && (!newOffers.isEmpty || subscriptions.isAnyBuffered)) {
          initiateDispatch(newOffers, contractors, totalDemand, subscriptions)
        } else {
          context.become(collectingOffers(
            newOffers,
            contractors,
            subscriptions))
        }
      } else {
        contractor.onProblem(UnspecifiedProblem("[OfferSupplies(collecting)]: You are not a contractor!"))
      }

    case InternalDeliverSupplies(elements: Seq[TElement], contractor: SuppliesContractor[TElement]) =>
      if (contractors(contractor)) {
        contractor.onProblem(UnspecifiedProblem("[LoadSupplies(collecting)]: You have not been asked to load supplies! Are you too late?"))
      } else {
        contractor.onProblem(UnspecifiedProblem("[LoadSupplies(collecting)]: You are not a contractor!"))
      }

    case Request(amount) =>
      if (!offers.isEmpty || subscriptions.isAnyBuffered) {
        initiateDispatch(offers, contractors, totalDemand, subscriptions)
      }
  }

  def transportingSupplies(
    deliverySchedule: Map[SuppliesContractor[TElement], Int],
    offers: Vector[SuppliesContractor[TElement]],
    contractors: Set[SuppliesContractor[TElement]],
    subscriptions: SubscriptionsState): Receive = {

    case InternalOnSubscribe(subscriberId, subscription) =>
      subscription.requestMore(buffersizePerSubscriber)
      context.become(transportingSupplies(deliverySchedule, offers, contractors, subscriptions.addSubscription(subscriberId, subscription)))

    case InternalOnNext(subscriberId, element) =>
      if (subscriptions.isSubscribed(subscriberId)) {
        context.become(transportingSupplies(deliverySchedule, offers, contractors, subscriptions.addToBuffer(subscriberId, element.asInstanceOf[TElement])))
      } else {
        log.warning(s"OnNext from unknown subscription $subscriberId for element $element is ignored")
      }

    case InternalOnComplete(subscriberId) =>
      context.become(transportingSupplies(deliverySchedule, offers, contractors, subscriptions.removeSubscription(subscriberId)))

    case InternalOnError(subscriberId, error) =>
      log.error(s"Subscriber $subscriberId reported an error. The subscriber will be removed.")
      context.become(transportingSupplies(deliverySchedule, offers, contractors, subscriptions.removeSubscription(subscriberId)))

    case InternalSignContract(contractor: SuppliesContractor[TElement]) =>
      if (!contractors(contractor)) {
        contractor.onStockroom(stockroom(contractor))
        context.become(transportingSupplies(
          deliverySchedule,
          offers,
          contractors + contractor,
          subscriptions))
      } else {
        contractor.onProblem(UnspecifiedProblem("[SignContract(transporting)]: You are already a contractor!"))
      }

    case InternalCancelContract(contractor: SuppliesContractor[TElement]) =>
      if (contractors(contractor)) {
        context.become(transportingSupplies(
          deliverySchedule - contractor,
          offers.filterNot(_ == contractor),
          contractors - contractor,
          subscriptions))
      } else {
        contractor.onProblem(UnspecifiedProblem("[CancelContract(transporting)]: You are not a contractor!"))
      }

    case InternalOfferSupplies(amount: Int, contractor: SuppliesContractor[TElement]) =>
      if (contractors(contractor)) {
        val newOffers = offers ++ Vector.fill(amount)(contractor)
        if (!deliverySchedule.isEmpty) {
          context.become(transportingSupplies(
            deliverySchedule,
            newOffers,
            contractors,
            subscriptions))
        } else if (totalDemand > 0 && (!newOffers.isEmpty || subscriptions.isAnyBuffered)) {
          initiateDispatch(newOffers, contractors, totalDemand, subscriptions)
        } else {
          context.become(collectingOffers(
            newOffers,
            contractors,
            subscriptions))
        }
      } else {
        contractor.onProblem(UnspecifiedProblem("[OfferSupplies(transporting)]: You are not a contractor!"))
      }

    case InternalDeliverSupplies(elements: Seq[TElement], contractor: SuppliesContractor[TElement]) =>
      deliverySchedule.get(contractor) match {
        case Some(amount) =>
          if (elements.size == amount) {
            elements.foreach(onNext)
          } else {
            contractor.onProblem(UnspecifiedProblem("[LoadSupplies(transporting)]: You have loaded ${elements.size} elements instead of $amount. Elements have not been transported."))
          }
          val newDeliverySchedule = deliverySchedule - contractor
          if (newDeliverySchedule.isEmpty) {
            context.become(collectingOffers(
              offers,
              contractors,
              subscriptions))
          } else {
            context.become(transportingSupplies(newDeliverySchedule, offers, contractors, subscriptions))
          }

        case None =>
          contractor.onProblem(UnspecifiedProblem("[LoadSupplies(transporting)]: You are not to send any elements!"))
      }

    case Request(amount) =>
      log.info(s"R: $amount, totalDEmand: $totalDemand")
      ()
  }

  def receive: Receive = collectingOffers(Vector.empty, Set.empty, SubscriptionsState(Map.empty, Vector.empty))

  def initiateDispatch(
    offers: Vector[SuppliesContractor[TElement]],
    contractors: Set[SuppliesContractor[TElement]],
    demand: Int,
    subscriptions: SubscriptionsState) {
    if (demand > 0) {
      if (!offers.isEmpty && subscriptions.nothingBuffered) {
        callForSuppliesOnly(offers, contractors, demand, subscriptions)
      } else if (subscriptions.isAnyBuffered && offers.isEmpty) {
        dispatchBufferedOnly(contractors, demand, subscriptions)
      } else {
        dispatchAndCallForSupplies(offers, contractors, demand, subscriptions)
      }
    }
  }

  def callForSuppliesOnly(
    offers: Vector[SuppliesContractor[TElement]],
    contractors: Set[SuppliesContractor[TElement]],
    demand: Int,
    subscriptions: SubscriptionsState) {
    if (demand > 0) {
      val toLoad = offers.take(demand)
      val rest = offers.drop(toLoad.size)
      val deliveryPlan =
        toLoad
          .groupBy(identity)
          .map { case (sup, sups) => (sup, sups.size) }

      deliveryPlan.foreach { case (sup, amount) => sup.onDeliverSuppliesNow(amount) }
      context.become(transportingSupplies(deliveryPlan, rest, contractors, subscriptions))
    }
  }

  def dispatchBufferedOnly(
    contractors: Set[SuppliesContractor[TElement]],
    demand: Int,
    subscriptions: SubscriptionsState) {
    if (demand > 0) {
      val (toDispatch, newSubscriptions) = subscriptions.takeElements(demand)
      toDispatch.foreach {
        case (subscriberId, element) =>
          onNext(element)
          subscriptions.signalRequestMore(subscriberId, 1)
      }
      context.become(collectingOffers(Vector.empty, contractors, newSubscriptions))
    }
  }

  val rnd = scala.util.Random
  def dispatchAndCallForSupplies(
    offers: Vector[SuppliesContractor[TElement]],
    contractors: Set[SuppliesContractor[TElement]],
    demand: Int,
    subscriptions: SubscriptionsState) {
    if (demand == 1) {
      if (rnd.nextBoolean) {
        val toLoad = offers.take(demand)
        val rest = offers.drop(toLoad.size)
        val deliveryPlan =
          toLoad
            .groupBy(identity)
            .map { case (sup, sups) => (sup, sups.size) }

        deliveryPlan.foreach { case (sup, amount) => sup.onDeliverSuppliesNow(amount) }
        context.become(transportingSupplies(deliveryPlan, rest, contractors, subscriptions))
      } else {
        val (toDispatch, newSubscriptions) = subscriptions.takeElements(demand)
        toDispatch.foreach {
          case (subscriberId, element) =>
            onNext(element)
            subscriptions.signalRequestMore(subscriberId, 1)
        }
        context.become(collectingOffers(offers, contractors, newSubscriptions))
      }
    } else {
      if (rnd.nextBoolean) {
        val toLoad = offers.take(demand)
        val rest = offers.drop(toLoad.size)
        val deliveryPlan =
          toLoad
            .groupBy(identity)
            .map { case (sup, sups) => (sup, sups.size) }

        deliveryPlan.foreach { case (sup, amount) => sup.onDeliverSuppliesNow(amount) }
        context.become(transportingSupplies(deliveryPlan, rest, contractors, subscriptions))
      } else {
        val (toDispatch, newSubscriptions) = subscriptions.takeElements(demand)
        toDispatch.foreach {
          case (subscriberId, element) =>
            onNext(element)
            subscriptions.signalRequestMore(subscriberId, 1)
        }
        context.become(collectingOffers(offers, contractors, newSubscriptions))
      }
    }
  }

  override def postStop() {
  }
}

