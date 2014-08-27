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
trait ActorContractor[TElement] extends Actor {
  case class OnBrokerProblem(problem: Problem)
  case class OnDeliverSuppliesNow(amount: Int)
  case object OnContractExpired

  protected case object ReadyForDeliveries

  /**
   * Sign a contract with a broker. The contract will be confirmed by sending a #ReadyForDeliveries
   *
   *  Calling this method while already contracted is a serious offense and will result in an exception.
   *  You can check whether there is a contract vial #contracted.
   */
  protected def signContract(broker: StreamBroker[TElement]) {
    stockroom match {
      case None =>
        broker.signContract(new SuppliesContractor[TElement] {
          def onProblem(problem: Problem) {
            self ! OnBrokerProblem(problem)
          }

          def onStockroom(theStockroom: Stockroom[TElement]) {
            stockroom = Some(theStockroom)
            self ! ReadyForDeliveries
          }

          def onDeliverSuppliesNow(amount: Int) {
            self ! OnDeliverSuppliesNow(amount)
          }

          def onContractExpired() {
            self ! OnContractExpired
          }
        })
      case Some(_) =>
        throw new Exception("We are already contracted. You cannot sign more than one contract.")
    }
  }

  protected def offer(amount: Int) {
    stockroom match {
      case Some(sr) =>
        sr.offerSupplies(amount)
      case None =>
        self ! DeliveryJobFailed(IllegalOperationProblem("No contract signed. Please sign a contract with your favorite local broker."))
    }
  }

  protected def deliver(items: Seq[TElement]) {
    stockroom match {
      case Some(sr) =>
        sr.deliverSupplies(items)
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
  private var stockroom: Option[Stockroom[TElement]] = None
}