package almhirt.streaming

import akka.actor._
import almhirt.common._

/**
 * Mix in this trait to get the capability of offering items to a broker.
 *  After offering items, the actor will wait until all items are delivered and
 *  signal success or failure.
 *
 *  There can be at most one contract with a broker at any time.
 */
trait StateChangingActorContractor[TElement] extends Actor {
  import InternalContractorMessages._

  protected type TPayload

  protected case class ReadyForDeliveries(payLoad: Option[TPayload])

  case class DeliveryResult(deliveryStatus: DeliveryStatus, payLoad: Option[TPayload]) {
    def isSuccess = deliveryStatus.isSuccess
    def isFailure = !isSuccess
  }

  /**
   * Sign a contract with a broker. The contract will be confirmed by sending a #ReadyForDeliveries
   *  to the state the actor was in when calling this method.
   *
   *  This method will enter a new state(Receive)  and a handler for messages you are expecting while waiting
   *  to be contracted can be added via appendix. You may not change the actor's state in the appendix.
   *
   *  Calling this method while already contracted is a serious offense and will result in an exception.
   *  You can check whether there is a contract vial #contracted.
   */
  protected def signContract(broker: StreamBroker[TElement], initialPayload: Option[TPayload] = None)(appendix: Receive = Actor.emptyBehavior) {
    enterSignContract(broker, None, appendix, initialPayload)
  }

  /**
   * Sign a contract with a broker. The contract will be confirmed by sending a #ReadyForDeliveries
   *  to the state specified by nextState.
   *
   *  This method will enter a new state(Receive)  and a handler for messages you are expecting while waiting
   *  to be contracted can be added via appendix. You may not change the actor's state in the appendix.
   *
   *  Calling this method while already contracted is a serious offense and will result in an exception.
   *  You can check whether there is a contract vial #contracted.
   */
  protected def signContractAndThen(broker: StreamBroker[TElement], initialPayload: Option[TPayload] = None)(appendix: Receive = Actor.emptyBehavior)(nextState: Receive) {
    enterSignContract(broker, Some(nextState), appendix, initialPayload)
  }

   /**
   * Offer items to the broker you have previously signed a contract with. The delivery confirmed by sending a [[DeliveryResult]]
   *  to the state the actor was in when calling this method.
   *
   *  This method will enter a new state(Receive) and a handler for messages you are expecting while waiting
   *  to for the delivery to be fulfilled can be added via appendix. You may not change the actor's state in the appendix.
   *
   *  You must not call this method from within the appendix. If you need to offer items while already delivering, call [[#offerMore]]
   *
   *  Do not cancel the contract while delivering.
   */
  protected def offer(items: Seq[TElement], initialPayload: Option[TPayload] = None)(appendix: Receive = Actor.emptyBehavior) {
    enterDelivery(items, None, appendix, initialPayload)
  }

  /**
   * Offer items to the broker you have previously signed a contract with. The delivery confirmed by sending a [[DeliveryResult]]
   *  to the state specified by andThen.
   *
   *  This method will enter a new state(Receive) and a handler for messages you are expecting while waiting
   *  to for the delivery to be fulfilled can be added via appendix. You may not change the actor's state in the appendix.
   *
   *  You must not call this method from within the appendix. If you need to offer items while already delivering, call [[#offerMore]]
   *
   *  Do not cancel the contract while delivering.
   */
  protected def offerAndThen(items: Seq[TElement], initialPayload: Option[TPayload] = None)(appendix: Receive = Actor.emptyBehavior)(nextState: Receive) {
    enterDelivery(items, Some(nextState), appendix, initialPayload)
  }
  /**
   * Offer items to the broker while already delivering. The items will be added to the current job.
   * Offering more while not delivering will result in an exception.
   */
  protected def offerMore(items: Seq[TElement]) {
    stockroom match {
      case Some(sr) ⇒
        if (!toDeliverInCurrentJob.isEmpty) {
          if (!items.isEmpty) {
            sr.offerSupplies(items.size)
            toDeliverInCurrentJob = toDeliverInCurrentJob ++ items
          }
        } else {
          throw new Exception("You must not call offerMore when there is no delivery job running!")
        }
      case None ⇒
        self ! DeliveryJobFailed(IllegalOperationProblem("No contract signed. Please sign a contract with your favorite local broker."))
    }
  }

  /**
   * Cancel the current contract. If there is none, nothing happens.
   */
  protected def cancelContract() {
    this.stockroom.foreach(_.cancelContract())
    this.stockroom = None
  }

  /**
   * Returns true if there is an active contract
   */
  protected def contracted: Boolean = stockroom.isDefined

  protected def payload: Option[TPayload] = payloadInternal
  protected def setPayload(payload: TPayload) { payloadInternal = Some(payload) }
  protected def modPayload(f: TPayload ⇒ TPayload) { payloadInternal = payloadInternal.map(f) }
  
  private var stockroom: Option[Stockroom[TElement]] = None
  private var payloadInternal: Option[TPayload] = None
  private var nextState: Option[Receive] = None
  
  private def enterDelivery(items: Seq[TElement], nextState: Option[Receive], appendix: Receive, initialPayload: Option[TPayload]) {
    stockroom match {
      case Some(sr) ⇒
        if (items.isEmpty) {
          self ! DeliveryResult(DeliveryJobDone(), initialPayload)
        } else if (!toDeliverInCurrentJob.isEmpty) {
          self ! DeliveryResult(DeliveryJobFailed(IllegalOperationProblem("There is already a delivery job running. Please wait for completion until issueing another job.")), initialPayload)
        } else {
          sr.offerSupplies(items.size)
          toDeliverInCurrentJob = items
          this.nextState = nextState
          context.become(receiveWaitForDelivery orElse appendix, true)
        }
      case None ⇒
        self ! DeliveryResult(DeliveryJobFailed(IllegalOperationProblem("No contract signed. Please sign a contract with your favorite local broker.")), initialPayload)
    }
  }

  private def enterSignContract(broker: StreamBroker[TElement], nextState: Option[Receive], appendix: Receive, initialPayLoad: Option[TPayload]) {
    stockroom match {
      case None ⇒
        broker.signContract(new SuppliesContractor[TElement] {
          def onProblem(problem: Problem) {
            self ! OnProblem(problem)
          }

          def onStockroom(theStockroom: Stockroom[TElement]) {
            self ! OnStockroom(theStockroom)
          }

          def onDeliverSuppliesNow(amount: Int) {
            self ! OnDeliverSuppliesNow(amount)
          }

          def onContractExpired() {
            self ! OnContractExpired
          }
        })
        this.nextState = nextState
        context.become(receiveWaitForStockroom orElse appendix, true)
      case Some(_) ⇒
        throw new Exception("We are already contracted. You cannot sign more than one contract.")
    }
  }

 
  private def receiveWaitForStockroom: Receive = {
    case OnStockroom(theStockroom: Stockroom[TElement]) ⇒
      this.stockroom = Some(theStockroom)
      context.unbecome()
      nextState.foreach(nx ⇒ context.become(nx))
      nextState = None
      val p = payloadInternal
      payloadInternal = None
      self ! ReadyForDeliveries(p)
  }

  private var toDeliverInCurrentJob: Seq[TElement] = Seq.empty
  private def receiveWaitForDelivery: Receive = {
    case OnDeliverSuppliesNow(amount) ⇒
      val toDeliverNow = toDeliverInCurrentJob.take(amount)
      val rest = toDeliverInCurrentJob.drop(toDeliverNow.size)
      stockroom.get.deliverSupplies(toDeliverNow)
      toDeliverInCurrentJob = rest
      if (toDeliverInCurrentJob.isEmpty) {
        context.unbecome()
        nextState.foreach(nx ⇒ context.become(nx))
        nextState = None
        val p = payloadInternal
        payloadInternal = None
        self ! DeliveryResult(DeliveryJobDone(), p)
      }
  }
}