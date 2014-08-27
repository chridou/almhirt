package almhirt.streaming

import akka.actor._
import almhirt.common._

trait ActorContractor[TElement] { me: Actor =>
  import InternalContractorMessages._

  protected case object ReadyForDeliveries

  private var stockroom: Option[Stockroom[TElement]] = None

  def signContract(broker: StreamBroker[TElement], appendix: Receive = Actor.emptyBehavior) {
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

  def offer(items: Seq[TElement], appendix: Receive = Actor.emptyBehavior) {
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

  def cancelContract() {
    this.stockroom.foreach(_.cancelContract())
    this.stockroom = None
  }

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
      if(toDeliverInCurrentJob.isEmpty) {
        context.unbecome()
        self ! DeliveryJobDone()
      }
  }
}