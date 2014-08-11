package almhirt.streaming

import org.reactivestreams.api.Consumer
import org.reactivestreams.spi.Subscriber
import almhirt.common._

trait StreamBroker[TElement] extends Consumer[TElement]{
  def signContract(contractor: SuppliesContractor[TElement]): Unit
  def getSubscriber(): Subscriber[TElement]
}


trait SuppliesContractor[TElement] {
  def onProblem(problem: Problem): Unit
  def onStockroom(stockroom: Stockroom[TElement]): Unit
  /** The contractor must deliver the specified amount immediately */
  def onDeliverSuppliesNow(amount: Int): Unit
  def onContractExpired(): Unit
}

trait Stockroom[TElement] {
  def cancelContract(): Unit
  def offerSupplies(amount: Int): Unit
  def deliverSupplies(elements: Seq[TElement]): Unit

}

private[almhirt] object InternalBrokerMessages {
  import scala.language.existentials
  final case class InternalSignContract(contractor: SuppliesContractor[_])
  final case class InternalCancelContract(contractor: SuppliesContractor[_])
  final case class InternalOfferSupplies(amount: Int, contractor: SuppliesContractor[_])
  final case class InternalDeliverSupplies(elements: Seq[_], contractor: SuppliesContractor[_])
}

