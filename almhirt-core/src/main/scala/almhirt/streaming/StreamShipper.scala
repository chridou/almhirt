package almhirt.streaming

import akka.actor._
import org.reactivestreams.api.{ Producer, Consumer }
import org.reactivestreams.spi.{ Subscriber, Subscription }
import akka.stream.actor.ActorProducer
import almhirt.common._

object StreamShipper {
  def apply[TElement](actor: ActorRef): (StreamBroker[TElement], Producer[TElement], Stoppable) = {
    val broker =
      new StreamBroker[TElement] {
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
      }
    (broker, ActorProducer[TElement](actor), new Stoppable { def stop() { actor ! StopStreaming } })
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

    def addToBuffer(subscriberId: String, element: TElement): SubscriptionsState = {
      this.copy(bufferedElements = this.bufferedElements :+ (subscriberId -> element))
    }

    def takeElements(demand: Int): (Vector[(String, TElement)], SubscriptionsState) = {
      val taken = bufferedElements.take(demand)
      (taken, copy(bufferedElements = this.bufferedElements.drop(taken.size)))
    }

    def signalRequestMore(subscriberId: String, amount: Int) {
      subscriptions.get(subscriberId).foreach(_.requestMore(amount))
    }

  }

  private var totalToDispatch = 0L
  private var totalDispatched = 0L
  private var totalOffered = 0L
  private var totalDemanded = 0L
  private var totalDelivered = 0L

  def collectingOffers(
    offers: Vector[SuppliesContractor[TElement]],
    contractors: Set[SuppliesContractor[TElement]],
    subscriptions: SubscriptionsState): Receive = {

    case InternalOnSubscribe(subscriberId, subscription) ⇒
      subscription.requestMore(buffersizePerSubscriber)
      context.become(collectingOffers(offers, contractors, subscriptions.addSubscription(subscriberId, subscription)))

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
      val newSubscriptions = subscriptions.removeSubscription(subscriberId)
      context.become(collectingOffers(offers, contractors, newSubscriptions))

    case InternalOnError(subscriberId, error) ⇒
      log.error(s"Subscriber $subscriberId reported an error. The subscriber will be removed.")
      context.become(collectingOffers(offers, contractors, subscriptions.removeSubscription(subscriberId)))

    case InternalSignContract(contractor: SuppliesContractor[TElement]) ⇒
      if (!contractors(contractor)) {
        contractor.onStockroom(stockroom(contractor))
        context.become(collectingOffers(
          offers,
          contractors + contractor,
          subscriptions))
      } else {
        contractor.onProblem(UnspecifiedProblem("[SignContract(collecting)]: You are already a contractor!"))
      }

    case InternalCancelContract(contractor: SuppliesContractor[TElement]) ⇒
      if (contractors(contractor)) {
        context.become(collectingOffers(
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

    case Request(amount) ⇒
      chooseNextAction(offers, contractors, subscriptions)

    case StopStreaming ⇒
      stop(Map.empty, offers, contractors, subscriptions, "collecting offers")
  }

  def transportingSupplies(
    deliverySchedule: Map[SuppliesContractor[TElement], Int],
    offers: Vector[SuppliesContractor[TElement]],
    contractors: Set[SuppliesContractor[TElement]],
    subscriptions: SubscriptionsState): Receive = {

    case InternalOnSubscribe(subscriberId, subscription) ⇒
      if (deliverySchedule.isEmpty) sys.error("This must not happen!InternalOnSubscribe")
      subscription.requestMore(buffersizePerSubscriber)
      context.become(transportingSupplies(deliverySchedule, offers, contractors, subscriptions.addSubscription(subscriberId, subscription)))

    case InternalOnNext(subscriberId, element) ⇒
      if (deliverySchedule.isEmpty) sys.error("This must not happen!InternalOnNext")
      if (subscriptions.isSubscribed(subscriberId)) {
        totalToDispatch += 1
        context.become(transportingSupplies(deliverySchedule, offers, contractors, subscriptions.addToBuffer(subscriberId, element.asInstanceOf[TElement])))
      } else {
        log.warning(s"OnNext from unknown subscription $subscriberId for element $element is ignored")
      }

    case InternalOnComplete(subscriberId) ⇒
      if (deliverySchedule.isEmpty) sys.error("This must not happen!InternalOnComplete")
      context.become(transportingSupplies(deliverySchedule, offers, contractors, subscriptions.removeSubscription(subscriberId)))

    case InternalOnError(subscriberId, error) ⇒
      if (deliverySchedule.isEmpty) sys.error("This must not happen!InternalOnError")
      log.error(s"Subscriber $subscriberId reported an error. The subscriber will be removed.")
      context.become(transportingSupplies(deliverySchedule, offers, contractors, subscriptions.removeSubscription(subscriberId)))

    case InternalSignContract(contractor: SuppliesContractor[TElement]) ⇒
      if (deliverySchedule.isEmpty) sys.error("This must not happen!InternalSignContract")
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

    case InternalCancelContract(contractor: SuppliesContractor[TElement]) ⇒
      if (deliverySchedule.isEmpty) sys.error("This must not happen!InternalCancelContract")
      if (contractors(contractor)) {
        deliverySchedule.get(contractor) match {
          case Some(c) ⇒
            log.warning(s"Contractor $contractor who is scheduled for delivery cancelled his contract!")
            context.become(transportingSupplies(
              deliverySchedule - contractor,
              offers.filterNot(_ == contractor),
              contractors - contractor,
              subscriptions))
          case None ⇒
            context.become(transportingSupplies(
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
          context.become(transportingSupplies(
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
            context.become(transportingSupplies(newDeliverySchedule, offers, contractors, subscriptions))
          }

        case None ⇒
          contractor.onProblem(UnspecifiedProblem("[LoadSupplies(transporting)]: You are not to send any elements!"))
          log.warning(s"Got ${elements.size} elements from contractor $contractor who is not in the delivery schedule.")
      }

    case Request(amount) ⇒
      if (deliverySchedule.isEmpty)
        chooseNextAction(offers, contractors, subscriptions)

    case StopStreaming ⇒
      stop(deliverySchedule, offers, contractors, subscriptions, "transporting")
  }

  def receive: Receive = collectingOffers(Vector.empty, Set.empty, SubscriptionsState(Map.empty, Vector.empty))

  def stop(
    deliverySchedule: Map[SuppliesContractor[TElement], Int],
    offers: Vector[SuppliesContractor[TElement]],
    contractors: Set[SuppliesContractor[TElement]],
    subscriptions: SubscriptionsState,
    stateMessagePart: String) {

    contractors.foreach(_.onContractExpired())
    subscriptions.subscriptions.foreach { case (_, s) ⇒ s.cancel() }

    if (!contractors.isEmpty)
      log.warning(s"${contractors.size} contractors still contracted.")

    if (!deliverySchedule.isEmpty)
      log.warning(s"There deliveries pending. I was $stateMessagePart. Remaining demand: $totalDemand.")

    if (!offers.isEmpty)
      log.warning(s"There are ${offers.size} offers left. I was $stateMessagePart. Remaining demand: $totalDemand. Number of contractors: ${contractors.size}.")

    if (subscriptions.isAnyBuffered)
      log.warning(s"There are still ${subscriptions.numBuffered} buffered elements left. I was $stateMessagePart. Remaining demand: $totalDemand.")

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
        context.become(collectingOffers(
          offers,
          contractors,
          subscriptions))
      }
    } else {
      context.become(collectingOffers(
        offers,
        contractors,
        subscriptions))
    }
  }

  def callForSuppliesOnly(
    offers: Vector[SuppliesContractor[TElement]],
    contractors: Set[SuppliesContractor[TElement]],
    demand: Int,
    subscriptions: SubscriptionsState) {
    val toLoad = offers.take(demand)
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
    context.become(transportingSupplies(deliveryPlan, rest, contractors, subscriptions))
  }

  def dispatchBufferedOnly(
    contractors: Set[SuppliesContractor[TElement]],
    demand: Int,
    subscriptions: SubscriptionsState) {
    val (toDispatch, newSubscriptions) = subscriptions.takeElements(demand)
    toDispatch.foreach {
      case (subscriberId, element) ⇒
        onNext(element)
        subscriptions.signalRequestMore(subscriberId, 1)
        totalDispatched += 1
    }
    context.become(collectingOffers(Vector.empty, contractors, newSubscriptions))
  }

  val rnd = scala.util.Random
  def dispatchAndCallForSupplies(
    offers: Vector[SuppliesContractor[TElement]],
    contractors: Set[SuppliesContractor[TElement]],
    demand: Int,
    subscriptions: SubscriptionsState) {
    if (rnd.nextBoolean) {
      val toLoad = offers.take(demand)
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
      context.become(transportingSupplies(deliveryPlan, rest, contractors, subscriptions))
    } else {
      val (toDispatch, newSubscriptions) = subscriptions.takeElements(demand)
      toDispatch.foreach {
        case (subscriberId, element) ⇒
          onNext(element)
          subscriptions.signalRequestMore(subscriberId, 1)
          totalDispatched += 1
      }
      val remainingDemand = demand - toDispatch.size
      if(remainingDemand > 0) {
        chooseNextAction(offers, contractors, newSubscriptions)
      } else {
       context.become(collectingOffers(offers, contractors, newSubscriptions))
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
