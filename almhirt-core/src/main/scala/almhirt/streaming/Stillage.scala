package almhirt.streaming

import almhirt.common._

/** A Stillage is a Supplier that delivers its contents to a StreamBroker and the cancels the contract */
object Stillage {
  def apply[TElement](contents: Seq[TElement], packagingSize: Int): Supplier[TElement] =
    new StillageImpl(contents, packagingSize)

  def apply[TElement](contents: Seq[TElement]): Supplier[TElement] =
    new StillageImpl(contents, 16)
}

private[almhirt] class StillageImpl[TElement](contents: Seq[TElement], packagingSize: Int) extends Supplier[TElement] {
  def signContract(trader: StreamBroker[TElement]) {
    var stockroom: Option[Stockroom[TElement]] = None

    var notYetOffered = contents
    var offered: Vector[TElement] = Vector.empty
    var toDeliverLeft = contents.size

    def offer() {
      stockroom.foreach(stockroom => {
        val nextBatch = notYetOffered.take(packagingSize)
        notYetOffered = notYetOffered.drop(nextBatch.size)
        stockroom.offerSupplies(nextBatch.size)
        offered = offered ++ nextBatch
      })
    }

    trader.signContract(new SuppliesContractor[TElement] {
      def onProblem(problem: Problem) {
        problem.escalate()
      }

      def onStockroom(theStockroom: Stockroom[TElement]) {
        stockroom = Some(theStockroom)
        offer()
      }

      def onDeliverSuppliesNow(amount: Int) {
        stockroom.foreach(stockroom => {
          val toLoad = offered.take(amount)
          stockroom.deliverSupplies(toLoad)
          offered = offered.drop(amount)
          toDeliverLeft -= amount
          if (!notYetOffered.isEmpty && offered.size < packagingSize)
            offer()
          if (toDeliverLeft == 0)
            stockroom.cancelContract
        })
      }

      def onContractExpired() {
        stockroom = None
      }
    })
  }
}
