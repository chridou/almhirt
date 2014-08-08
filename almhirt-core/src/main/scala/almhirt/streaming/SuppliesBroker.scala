package almhirt.streaming

import akka.actor._
import akka.stream.actor.ActorProducer
import org.reactivestreams.api.Producer
import almhirt.common._

trait SuppliesBroker[TElement] {
  def signContract(contractor: SuppliesContractor[TElement]): Unit
}


trait SuppliesContractor[TElement] {
  def onProblem(problem: Problem): Unit
  def onLoadingBay(loadingBay: LoadingBay[TElement]): Unit
  /** The contractor must load the specified amount immediately */
  def onLoadSuppliesNow(amount: Int): Unit
  def onContractExpired(): Unit
}

trait LoadingBay[TElement] {
  def cancelContract(): Unit
  def offerSupplies(amount: Int): Unit
  def loadSupplies(elements: Seq[TElement]): Unit

}

private[almhirt] object InternalBrokerMessages {
  import scala.language.existentials
  final case class SignContract(contractor: SuppliesContractor[_])
  final case class CancelContract(contractor: SuppliesContractor[_])
  final case class OfferSupplies(amount: Int, contractor: SuppliesContractor[_])
  final case class LoadSupplies(elements: Seq[_], contractor: SuppliesContractor[_])
}

