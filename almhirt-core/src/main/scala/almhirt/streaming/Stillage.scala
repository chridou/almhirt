package almhirt.streaming

import almhirt.common._

object Stillage {
  def apply[TElement](contents: Seq[TElement], packagingSize: Int): Supplier[TElement] =
    new StillageImpl(contents, packagingSize)

  def apply[TElement](contents: Seq[TElement]): Supplier[TElement] =
    new StillageImpl(contents, 16)
}

private[almhirt] class StillageImpl[TElement](contents: Seq[TElement], packagingSize: Int) extends Supplier[TElement] {
  def supply(trader: SuppliesTrader[TElement]) {
    var loadingBay: Option[LoadingBay[TElement]] = None

    var notYetOffered = contents
    var offered: Vector[TElement] = Vector.empty

    def offer() {
      loadingBay.foreach(bay => {
        val nextBatch = notYetOffered.take(packagingSize)
        notYetOffered = notYetOffered.drop(nextBatch.size)
        bay.offerSupplies(nextBatch.size)
        offered = offered ++ nextBatch
        if (offered.isEmpty && notYetOffered.isEmpty)
          bay.cancelContract()
      })
    }

    trader.signContract(new SuppliesContractor[TElement] {
      def onProblem(problem: Problem) {
        problem.escalate()
      }

      def onLoadingBay(theLoadingBay: LoadingBay[TElement]) {
        loadingBay = Some(theLoadingBay)
        offer()
      }

      def onLoadSuppliesNow(amount: Int) {
        loadingBay.foreach(bay => {
          val toLoad = offered.take(amount)
          bay.loadSupplies(toLoad)
          offered = offered.drop(amount)
        })
        if (offered.size < packagingSize)
          offer()
      }

      def onContractExpired() {
        loadingBay = None
      }
    })
  }
}
