package almhirt.streaming

import scala.concurrent.duration._
import akka.actor._
import org.reactivestreams.{ Subscriber, Subscription, Publisher }
import akka.stream.actor._
import almhirt.common._

object StreamShipper {
  def apply[TElement](actor: ActorRef): (StreamBroker[TElement], Publisher[TElement], Stoppable) = {
    val broker =
      new StreamBroker[TElement] {
        def signContract(contractor: SuppliesContractor[TElement]) {
          actor ! InternalBrokerMessages.InternalSignContract(contractor)
        }

        def newSubscriber(): Subscriber[TElement] = {
          import InternalBrokerMessages._
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
    (broker, ActorPublisher[TElement](actor), new Stoppable { def stop() { actor ! StopStreaming } })
  }

  def props[TElement](): Props = Props(new StreamShipperImpl[TElement]())
}

/** This is a very naive implementation to get started! */
private[almhirt] class StreamShipperImpl[TElement](buffersizePerSubscriber: Int) extends Actor with ActorPublisher[TElement] with ActorLogging {
  import ActorPublisher._
  import InternalBrokerMessages._

  def this() = this(16)

  private object CheckForNoDemand

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
      this.copy(subscriptions = this.subscriptions + (subscriberId → subscription))
    }

    def removeSubscription(subscriberId: String): SubscriptionsState = {
      this.copy(subscriptions = this.subscriptions - subscriberId)
    }

    def addToBuffer(subscriberId: String, element: TElement): SubscriptionsState = {
      this.copy(bufferedElements = this.bufferedElements :+ (subscriberId → element))
    }

    def takeElements(demand: Int): (Vector[(String, TElement)], SubscriptionsState) = {
      val taken = bufferedElements.take(demand)
      (taken, copy(bufferedElements = this.bufferedElements.drop(taken.size)))
    }

    def signalRequestMore(subscriberId: String, amount: Int) {
      subscriptions.get(subscriberId).foreach(_.request(amount))
    }

  }

  private var totalToDispatch = 0L
  private var totalDispatched = 0L
  private var totalOffered = 0L
  private var totalDemanded = 0L
  private var totalDelivered = 0L

  private var toNotifyOnNoDemand: Option[(FiniteDuration ⇒ Unit, FiniteDuration, Cancellable)] = None
  private var noDemandSince: Option[Deadline] = Some(Deadline.now)

  def receiveCollectingOffers(
    offers: Vector[SuppliesContractor[TElement]],
    contractors: Set[SuppliesContractor[TElement]],
    subscriptions: SubscriptionsState): Receive = {

    case InternalOnSubscribe(subscriberId, subscription) ⇒
      subscription.request(buffersizePerSubscriber)
      context.become(receiveCollectingOffers(offers, contractors, subscriptions.addSubscription(subscriberId, subscription)))

    case InternalOnNext(subscriberId, untypedElem) ⇒
      if (subscriptions.isSubscribed(subscriberId)) {
        val element = untypedElem.asInstanceOf[TElement]
        totalToDispatch += 1
        if (totalDemand > 0 && subscriptions.nothingBuffered && offers.isEmpty) {
          onNext(element.asInstanceOf[TElement])
          totalDispatched += 1
          subscriptions.signalRequestMore(subscriberId, 1)
          // stay
        } else {
          chooseNextAction(offers, contractors, subscriptions.addToBuffer(subscriberId, element))
        }
      } else {
        log.warning(s"OnNext from unknown subscription $subscriberId for element untypedElem is ignored")
      }

    case InternalOnComplete(subscriberId) ⇒
      if (log.isDebugEnabled)
        log.debug(s"Publisher to consumer $subscriberId completed.")
      val newSubscriptions = subscriptions.removeSubscription(subscriberId)
      context.become(receiveCollectingOffers(offers, contractors, newSubscriptions))

    case InternalOnError(subscriberId, error) ⇒
      log.error(s"Subscriber $subscriberId reported an error. The subscriber will be removed.")
      context.become(receiveCollectingOffers(offers, contractors, subscriptions.removeSubscription(subscriberId)))

    case InternalSignContract(contractor: SuppliesContractor[TElement]) ⇒
      if (!contractors(contractor)) {
        contractor.onStockroom(stockroom(contractor))
        context.become(receiveCollectingOffers(
          offers,
          contractors + contractor,
          subscriptions))
      } else {
        contractor.onProblem(UnspecifiedProblem("[SignContract(collecting)]: You are already a contractor!"))
      }

    case InternalCancelContract(contractor: SuppliesContractor[TElement]) ⇒
      if (contractors(contractor)) {
        context.become(receiveCollectingOffers(
          offers.filterNot(_ == contractor),
          contractors - contractor,
          subscriptions))
      } else {
        contractor.onProblem(UnspecifiedProblem("[CancelContract(collecting)]: You are not a contractor!"))
      }

    case InternalOfferSupplies(amount: Int, contractor: SuppliesContractor[TElement]) ⇒
      if (contractors(contractor)) {
        totalOffered += amount
        val newOffers = offers ++ Vector.fill(amount)(contractor)
        chooseNextAction(newOffers, contractors, subscriptions)
      } else {
        contractor.onProblem(UnspecifiedProblem("[OfferSupplies(collecting)]: You are not a contractor!"))
      }

    case InternalDeliverSupplies(elements: Seq[TElement], contractor: SuppliesContractor[TElement]) ⇒
      if (contractors(contractor)) {
        contractor.onProblem(UnspecifiedProblem("[LoadSupplies(collecting)]: You have not been asked to load supplies! Are you too late?"))
      } else {
        contractor.onProblem(UnspecifiedProblem("[LoadSupplies(collecting)]: You are not a contractor!"))
      }

    case ActorPublisherMessage.Request(amount) ⇒
      if (totalDemand > 0)
        noDemandSince = None
      chooseNextAction(offers, contractors, subscriptions)

    case ActorPublisherMessage.Cancel ⇒
      if (log.isDebugEnabled)
        log.debug("The downstream was cancelled. Stopping.")
      stop(Map.empty, offers, contractors, subscriptions, "collecting offers")

    case InternalBrokerMessages.InternalNotifyOnNoDemand(duration, action) ⇒
      if (toNotifyOnNoDemand.isEmpty) {
        val timerCancel = context.system.scheduler.schedule(Duration.Zero, duration, self, CheckForNoDemand)(context.dispatcher)
        toNotifyOnNoDemand = Some(action, duration, timerCancel)
      }

    case CheckForNoDemand ⇒
      noDemandSince.flatMap(since ⇒ toNotifyOnNoDemand.map(n ⇒ (since, n._1, n._2))).foreach {
        case (since, action, dur) ⇒
          val timeWithNoDemand = since.lap
          if (timeWithNoDemand >= dur)
            action(timeWithNoDemand)
      }

    case StopStreaming ⇒
      stop(Map.empty, offers, contractors, subscriptions, "collecting offers")
  }

  def receiveTransportingSupplies(
    deliverySchedule: Map[SuppliesContractor[TElement], Int],
    offers: Vector[SuppliesContractor[TElement]],
    contractors: Set[SuppliesContractor[TElement]],
    subscriptions: SubscriptionsState): Receive = {

    case InternalOnSubscribe(subscriberId, subscription) ⇒
      if (deliverySchedule.isEmpty) sys.error("This must not happen!InternalOnSubscribe")
      subscription.request(buffersizePerSubscriber)
      context.become(receiveTransportingSupplies(deliverySchedule, offers, contractors, subscriptions.addSubscription(subscriberId, subscription)))

    case InternalOnNext(subscriberId, element) ⇒
      if (deliverySchedule.isEmpty) sys.error("This must not happen!InternalOnNext")
      if (subscriptions.isSubscribed(subscriberId)) {
        totalToDispatch += 1
        context.become(receiveTransportingSupplies(deliverySchedule, offers, contractors, subscriptions.addToBuffer(subscriberId, element.asInstanceOf[TElement])))
      } else {
        log.warning(s"OnNext from unknown subscription $subscriberId for element $element is ignored")
      }

    case InternalOnComplete(subscriberId) ⇒
      if (log.isDebugEnabled)
        log.debug(s"Publisher to consumer $subscriberId completed.")
      if (deliverySchedule.isEmpty) sys.error(s"This must not happen! InternalOnComplete($subscriberId)")
      context.become(receiveTransportingSupplies(deliverySchedule, offers, contractors, subscriptions.removeSubscription(subscriberId)))

    case InternalOnError(subscriberId, error) ⇒
      if (deliverySchedule.isEmpty) sys.error("This must not happen!InternalOnError")
      log.error(s"Subscriber $subscriberId reported an error. The subscriber will be removed.")
      context.become(receiveTransportingSupplies(deliverySchedule, offers, contractors, subscriptions.removeSubscription(subscriberId)))

    case InternalSignContract(contractor: SuppliesContractor[TElement]) ⇒
      if (deliverySchedule.isEmpty) sys.error("This must not happen!InternalSignContract")
      if (!contractors(contractor)) {
        contractor.onStockroom(stockroom(contractor))
        context.become(receiveTransportingSupplies(
          deliverySchedule,
          offers,
          contractors + contractor,
          subscriptions))
      } else {
        contractor.onProblem(UnspecifiedProblem("[SignContract(transporting)]: You are already a contractor!"))
      }

    case InternalCancelContract(contractor: SuppliesContractor[TElement]) ⇒
      if (deliverySchedule.isEmpty) sys.error("This must not happen!InternalCancelContract")
      if (contractors(contractor)) {
        deliverySchedule.get(contractor) match {
          case Some(c) ⇒
            log.warning(s"Contractor $contractor who is scheduled for delivery cancelled his contract!")
            context.become(receiveTransportingSupplies(
              deliverySchedule - contractor,
              offers.filterNot(_ == contractor),
              contractors - contractor,
              subscriptions))
          case None ⇒
            context.become(receiveTransportingSupplies(
              deliverySchedule,
              offers,
              contractors,
              subscriptions))
        }
      } else {
        contractor.onProblem(UnspecifiedProblem("[CancelContract(transporting)]: You are not a contractor!"))
      }

    case InternalOfferSupplies(amount: Int, contractor: SuppliesContractor[TElement]) ⇒
      if (deliverySchedule.isEmpty) sys.error("This must not happen!InternalOfferSupplies")
      if (contractors(contractor)) {
        totalOffered += amount
        val newOffers = offers ++ Vector.fill(amount)(contractor)
        if (!deliverySchedule.isEmpty) {
          context.become(receiveTransportingSupplies(
            deliverySchedule,
            newOffers,
            contractors,
            subscriptions))
        } else {
          sys.error("This must not happen!")
        }
      } else {
        contractor.onProblem(UnspecifiedProblem("[OfferSupplies(transporting)]: You are not a contractor!"))
      }

    case InternalDeliverSupplies(elements: Seq[TElement], contractor: SuppliesContractor[TElement]) ⇒
      deliverySchedule.get(contractor) match {
        case Some(amount) ⇒
          totalDelivered += elements.size
          if (elements.size == amount) {
            elements.foreach(onNext)
          } else {
            contractor.onProblem(UnspecifiedProblem(s"[LoadSupplies(transporting)]: You have delivered ${elements.size} elements instead of $amount. Elements have not been transported."))
          }
          val newDeliverySchedule = deliverySchedule - contractor
          if (newDeliverySchedule.isEmpty) {
            chooseNextAction(offers, contractors, subscriptions)
          } else {
            context.become(receiveTransportingSupplies(newDeliverySchedule, offers, contractors, subscriptions))
          }

        case None ⇒
          contractor.onProblem(UnspecifiedProblem("[LoadSupplies(transporting)]: You are not to send any elements!"))
          log.warning(s"Got ${elements.size} elements from contractor $contractor who is not in the delivery schedule.")
      }

    case ActorPublisherMessage.Request(amount) ⇒
      if (totalDemand > 0)
        noDemandSince = None
      if (deliverySchedule.isEmpty)
        chooseNextAction(offers, contractors, subscriptions)

    case StopStreaming ⇒
      stop(deliverySchedule, offers, contractors, subscriptions, "transporting")

    case ActorPublisherMessage.Cancel ⇒
      if (log.isDebugEnabled)
        log.debug("Received cancel from downstream. Stopping.")
      stop(Map.empty, offers, contractors, subscriptions, "transporting")
      
    case InternalBrokerMessages.InternalNotifyOnNoDemand(duration, action) ⇒
      if (toNotifyOnNoDemand.isEmpty) {
        val timerCancel = context.system.scheduler.schedule(Duration.Zero, duration, self, CheckForNoDemand)(context.dispatcher)
        toNotifyOnNoDemand = Some(action, duration, timerCancel)
      }

    case CheckForNoDemand ⇒
      noDemandSince.flatMap(since ⇒ toNotifyOnNoDemand.map(n ⇒ (since, n._1, n._2))).foreach {
        case (since, action, dur) ⇒
          val timeWithNoDemand = since.lap
          if (timeWithNoDemand >= dur)
            action(timeWithNoDemand)
      }
  }

  def receive: Receive = receiveCollectingOffers(Vector.empty, Set.empty, SubscriptionsState(Map.empty, Vector.empty))

  def stop(
    deliverySchedule: Map[SuppliesContractor[TElement], Int],
    offers: Vector[SuppliesContractor[TElement]],
    contractors: Set[SuppliesContractor[TElement]],
    subscriptions: SubscriptionsState,
    stateMessagePart: String) {

    if (!subscriptions.subscriptions.isEmpty) {
      log.warning(s"Cancelling ${subscriptions.subscriptions.size} subscription(s) which were still present on stop.")
      subscriptions.subscriptions.foreach { case (_, s) ⇒ s.cancel() }
    }

    if (!deliverySchedule.isEmpty)
      log.warning(s"There are deliveries pending. I was $stateMessagePart. Remaining demand: $totalDemand.")

    if (!contractors.isEmpty) {
      log.warning(s"Cancelling ${contractors.size} contract(s) which were still present on stop.")
      contractors.foreach(_.onContractExpired())
    }

    if (!offers.isEmpty)
      log.warning(s"There are ${offers.size} offers left. I was $stateMessagePart. Remaining demand: $totalDemand. Number of contractors: ${contractors.size}.")

    if (subscriptions.isAnyBuffered)
      log.warning(s"There are still ${subscriptions.numBuffered} buffered elements left. I was $stateMessagePart. Remaining demand: $totalDemand.")

    toNotifyOnNoDemand.foreach(_._3.cancel())

    context.stop(self)
  }

  def chooseNextAction(
    offers: Vector[SuppliesContractor[TElement]],
    contractors: Set[SuppliesContractor[TElement]],
    subscriptions: SubscriptionsState) {
    val demand = totalDemand
    if (demand > 0) {
      if (!offers.isEmpty && subscriptions.nothingBuffered) {
        callForSuppliesOnly(offers, contractors, demand, subscriptions)
      } else if (subscriptions.isAnyBuffered && offers.isEmpty) {
        dispatchBufferedOnly(contractors, demand, subscriptions)
      } else if (!offers.isEmpty && subscriptions.isAnyBuffered) {
        dispatchAndCallForSupplies(offers, contractors, demand, subscriptions)
      } else {
        context.become(receiveCollectingOffers(
          offers,
          contractors,
          subscriptions))
      }
    } else {
      context.become(receiveCollectingOffers(
        offers,
        contractors,
        subscriptions))
    }
    if (totalDemand == 0 && noDemandSince.isEmpty) {
      noDemandSince = Some(Deadline.now)
    }
  }

  def callForSuppliesOnly(
    offers: Vector[SuppliesContractor[TElement]],
    contractors: Set[SuppliesContractor[TElement]],
    demand: Long,
    subscriptions: SubscriptionsState) {
    val toLoad = offers.take(demand.toInt)
    val rest = offers.drop(toLoad.size)
    val deliveryPlan =
      toLoad
        .groupBy(identity)
        .map { case (contractor, contractors) ⇒ (contractor, contractors.size) }

    deliveryPlan.foreach {
      case (contractor, amount) ⇒
        contractor.onDeliverSuppliesNow(amount)
        totalDemanded += amount
    }
    context.become(receiveTransportingSupplies(deliveryPlan, rest, contractors, subscriptions))
  }

  def dispatchBufferedOnly(
    contractors: Set[SuppliesContractor[TElement]],
    demand: Long,
    subscriptions: SubscriptionsState) {
    val (toDispatch, newSubscriptions) = subscriptions.takeElements(demand.toInt)
    toDispatch.foreach {
      case (subscriberId, element) ⇒
        onNext(element)
        subscriptions.signalRequestMore(subscriberId, 1)
        totalDispatched += 1
    }
    context.become(receiveCollectingOffers(Vector.empty, contractors, newSubscriptions))
  }

  val rnd = scala.util.Random
  def dispatchAndCallForSupplies(
    offers: Vector[SuppliesContractor[TElement]],
    contractors: Set[SuppliesContractor[TElement]],
    demand: Long,
    subscriptions: SubscriptionsState) {
    if (rnd.nextBoolean) {
      val toLoad = offers.take(demand.toInt)
      val rest = offers.drop(toLoad.size)
      val deliveryPlan =
        toLoad
          .groupBy(identity)
          .map { case (contractor, contractors) ⇒ (contractor, contractors.size) }

      deliveryPlan.foreach {
        case (contractor, amount) ⇒
          contractor.onDeliverSuppliesNow(amount)
          totalDemanded += amount
      }
      context.become(receiveTransportingSupplies(deliveryPlan, rest, contractors, subscriptions))
    } else {
      val (toDispatch, newSubscriptions) = subscriptions.takeElements(demand.toInt)
      toDispatch.foreach {
        case (subscriberId, element) ⇒
          onNext(element)
          subscriptions.signalRequestMore(subscriberId, 1)
          totalDispatched += 1
      }
      val remainingDemand = demand - toDispatch.size
      if (remainingDemand > 0) {
        chooseNextAction(offers, contractors, newSubscriptions)
      } else {
        context.become(receiveCollectingOffers(offers, contractors, newSubscriptions))
      }
    }
  }

  override def preRestart(reason: Throwable, message: Option[Any]) {
    sys.error(s"""Not restartable!. Error: "${reason.getMessage} Message: $message""")
  }

  override def postStop() {
    log.info(s"""	|
    				|Total to dispatch: $totalToDispatch 
    				|Total dispatched:  $totalDispatched
    				|Total offered:     $totalOffered
    				|Total demanded:    $totalDemanded
    				|Total delivered:   $totalDelivered""".stripMargin)
  }
}

