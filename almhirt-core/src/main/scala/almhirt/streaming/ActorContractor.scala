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
trait ActorContractor[TElement] { me: Actor =>
  import InternalContractorMessages._

  protected case object ReadyForDeliveries

  private var stockroom: Option[Stockroom[TElement]] = None

  /**
   * Sign a contract with a broker. The contract will be confirmed by sending a #ReadyForDeliveries
   *  to the state the actor was in when calling this method.
   *
   *  This method will enter a new state and a handler for messages you are expecting while waiting
   *  to be contracted can be added via appendix. You may not change the actor's state in the appendix.
   *
   *  Calling this method while already contracted is a serious offense and will result in an exception.
   *  You can check whether there is a contract vial #contracted.
   */
  protected def signContract(broker: StreamBroker[TElement], appendix: Receive = Actor.emptyBehavior) {
    stockroom match {
      case None =>
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
        context.become(receiveWaitForStockroom orElse appendix, true)
      case Some(_) =>
        throw new Exception("We are already contracted. You cannot sign more than one contract.")
    }
  }

  /**
   * Offer items to the broker you have previously signed a contract with. The delivery confirmed by sending a [[DeliveryStatus]]
   *  to the state the actor was in when calling this method.
   *
   *  This method will enter a new state and a handler for messages you are expecting while waiting
   *  to for the delivery to be fulfilled can be added via appendix. You may not change the actor's state in the appendix.
   *
   *  Calling this method while already delivering items or not having signed a contract results in a [[DeliveryJobFailed]].
   */
  protected def offer(items: Seq[TElement], appendix: Receive = Actor.emptyBehavior) {
    stockroom match {
      case Some(sr) =>
        if (items.isEmpty) {
          self ! DeliveryJobDone()
        } else if (!toDeliverInCurrentJob.isEmpty) {
          self ! DeliveryJobFailed(IllegalOperationProblem("There is already a delivery job running. Please wait for completion until issueing another job."))
        } else {
          sr.offerSupplies(items.size)
          toDeliverInCurrentJob = items
          context.become(receiveWaitForDelivery orElse appendix, true)
        }
      case None =>
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

  private def receiveWaitForStockroom: Receive = {
    case OnStockroom(theStockroom: Stockroom[TElement]) =>
      this.stockroom = Some(theStockroom)
      self ! ReadyForDeliveries
      context.unbecome()
  }

  var toDeliverInCurrentJob: Seq[TElement] = Seq.empty
  private def receiveWaitForDelivery: Receive = {
    case OnDeliverSuppliesNow(amount) =>
      val toDeliverNow = toDeliverInCurrentJob.take(amount)
      val rest = toDeliverInCurrentJob.drop(toDeliverNow.size)
      stockroom.get.deliverSupplies(toDeliverNow)
      toDeliverInCurrentJob = rest
      if (toDeliverInCurrentJob.isEmpty) {
        context.unbecome()
        self ! DeliveryJobDone()
      }
  }
}