package almhirt.streaming

import almhirt.common._
import akka.actor._

object ActorStillage {
  def apply[TElement](actor: ActorRef): Supplier[TElement] =
    new Supplier[TElement] {
      def signContract(trader: StreamBroker[TElement]) = {
        import InternalActorStillageMessages._
        trader.signContract(new SuppliesContractor[TElement] {
          def onProblem(problem: Problem) {
            actor ! OnProblem(problem)
          }

          def onStockroom(theStockroom: Stockroom[TElement]) {
            actor ! OnStockroom(theStockroom)
          }

          def onDeliverSuppliesNow(amount: Int) {
            actor ! OnDeliverSuppliesNow(amount)
          }

          def onContractExpired() {
            actor ! OnContractExpired
          }
        })
      }
    }

  def props[TElement](contents: Seq[TElement], packagingSize: Int): Props =
    Props(new ActorStillage[TElement](contents, packagingSize))

  def props[TElement](contents: Seq[TElement]): Props =
    Props(new ActorStillage[TElement](contents, 16))

  def create[TElement](contents: Seq[TElement], packagingSize: Int, actorName: String)(implicit system: ActorSystem): Supplier[TElement] =
    ActorStillage[TElement](system.actorOf(props(contents, packagingSize), actorName))

  def create[TElement](contents: Seq[TElement], actorName: String)(implicit system: ActorSystem): Supplier[TElement] =
    ActorStillage[TElement](system.actorOf(props(contents), actorName))

}

private[almhirt] object InternalActorStillageMessages {
  import scala.language.existentials
  final case class OnProblem(problem: Problem)
  final case class OnStockroom(theStockroom: Stockroom[_])
  final case class OnDeliverSuppliesNow(amount: Int)
  case object OnContractExpired
}

private[almhirt] class ActorStillage[TElement](contents: Seq[TElement], packagingSize: Int) extends Actor with ActorLogging {
  import InternalActorStillageMessages._
  var notYetOffered = contents
  var offered: Vector[TElement] = Vector.empty
  var toDeliverLeft = contents.size
  var stockroom: Option[Stockroom[TElement]] = None

  def offer() {
    stockroom.foreach(stockroom => {
      val nextBatch = notYetOffered.take(packagingSize)
      notYetOffered = notYetOffered.drop(nextBatch.size)
      stockroom.offerSupplies(nextBatch.size)
      offered = offered ++ nextBatch
    })
  }

  def receive: Receive = {
    case OnStockroom(theStockroom: Stockroom[TElement]) =>
      stockroom match {
        case None =>
          stockroom = Some(theStockroom)
          offer()
        case _ =>
          sys.error("There is already a stockroom")
      }

    case OnDeliverSuppliesNow(amount) =>
      if(amount > offered.size) {
    	  sys.error("The demand my not exceed the offers!")
      }
      
      stockroom.foreach(stockroom => {
        val toLoad = offered.take(amount)
        stockroom.deliverSupplies(toLoad)
        offered = offered.drop(amount)
        toDeliverLeft -= amount
        if (!notYetOffered.isEmpty && offered.size < packagingSize)
          offer()
        if (toDeliverLeft == 0)
          stockroom.cancelContract()
      })

    case OnContractExpired =>
      stockroom = None
      context.system.stop(self)

    case OnProblem(problem) =>
      problem.escalate()

  }

  override def preRestart(reason: Throwable, message: Option[Any]) {
    sys.error("A stillage can not be restarted")
  }

  override def postStop() {
    if (stockroom.isDefined)
      log.warning(s"I am still under contract!")
    if (!notYetOffered.isEmpty)
      log.warning(s"There are still ${notYetOffered.size} elements of ${contents.size} left that have not been offered.")
    if (!offered.isEmpty)
      log.warning(s"There are still ${offered.size} offered elements of ${contents.size} left that have not been delivered. That makes a total of ${notYetOffered.size+offered.size} elements that have not been delivered.")
  }
}