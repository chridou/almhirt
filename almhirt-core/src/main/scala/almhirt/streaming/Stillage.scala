package almhirt.streaming

import almhirt.common._

/** A Stillage is a Supplier that delivers its contents to a SuppliesBroker and the cancels the contract */
object Stillage {
  def apply[TElement](contents: Seq[TElement], packagingSize: Int): Supplier[TElement] =
    new StillageImpl(contents, packagingSize)

  def apply[TElement](contents: Seq[TElement]): Supplier[TElement] =
    new StillageImpl(contents, 16)
}

private[almhirt] class StillageImpl[TElement](contents: Seq[TElement], packagingSize: Int) extends Supplier[TElement] {
  def signContract(trader: SuppliesBroker[TElement]) {
    var stockroom: Option[Stockroom[TElement]] = None

    var notYetOffered = contents
    var offered: Vector[TElement] = Vector.empty

    def offer() {
      stockroom.foreach(stockroom => {
        val nextBatch = notYetOffered.take(packagingSize)
        notYetOffered = notYetOffered.drop(nextBatch.size)
        stockroom.offerSupplies(nextBatch.size)
        offered = offered ++ nextBatch
        if (offered.isEmpty && notYetOffered.isEmpty)
          stockroom.cancelContract()
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
        })
        if (offered.size < packagingSize)
          offer()
      }

      def onContractExpired() {
        stockroom = None
      }
    })
  }
}
