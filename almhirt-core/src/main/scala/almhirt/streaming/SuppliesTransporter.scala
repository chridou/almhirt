package almhirt.streaming

import akka.actor._
import akka.stream.actor.ActorProducer
import org.reactivestreams.api.Producer
import almhirt.common._

object SuppliesTransporter {
  def apply[TElement](actor: ActorRef): (SuppliesBroker[TElement], Producer[TElement]) = {
    (new SuppliesBroker[TElement] {
      def signContract(contractor: SuppliesContractor[TElement]) {
        actor ! InternalBrokerMessages.InternalSignContract(contractor)
      }
    },
      ActorProducer[TElement](actor))
  }

  def props[TElement](): Props = Props(new SuppliesTransporterImpl[TElement]())
}

private[almhirt] class SuppliesTransporterImpl[TElement] extends Actor with ActorProducer[TElement] with ActorLogging {
  import ActorProducer._
  import InternalBrokerMessages._

  def stockroom(forSuppliesContractor: SuppliesContractor[TElement]) = new Stockroom[TElement] {
    def cancelContract() { self ! InternalCancelContract(forSuppliesContractor) }
    def offerSupplies(amount: Int) { self ! InternalOfferSupplies(amount, forSuppliesContractor) }
    def deliverSupplies(elements: Seq[TElement]) { self ! InternalDeliverSupplies(elements, forSuppliesContractor) }
  }

  def collectingOffers(
    offers: Vector[SuppliesContractor[TElement]],
    contractors: Set[SuppliesContractor[TElement]]): Receive = {

    case InternalSignContract(contractor: SuppliesContractor[TElement]) =>
      if (!contractors(contractor)) {
        contractor.onStockroom(stockroom(contractor))
        context.become(collectingOffers(
          offers,
          contractors + contractor))
      } else {
        contractor.onProblem(UnspecifiedProblem("[SignContract(collecting)]: You already a contractor!"))
      }

    case InternalCancelContract(contractor: SuppliesContractor[TElement]) =>
      if (contractors(contractor)) {
        context.become(collectingOffers(
          offers.filterNot(_ == contractor),
          contractors - contractor))
      } else {
        contractor.onProblem(UnspecifiedProblem("[CancelContract(collecting)]: You are not a contractor!"))
      }

    case InternalOfferSupplies(amount: Int, contractor: SuppliesContractor[TElement]) =>
       if (contractors(contractor)) {
        val newOffers = offers ++ Vector.fill(amount)(contractor)
         if (totalDemand > 0 && !newOffers.isEmpty) {
         callForSupplies(newOffers, contractors)
        } else {
          context.become(collectingOffers(
            newOffers,
            contractors))
        }
      } else {
        contractor.onProblem(UnspecifiedProblem("[OfferSupplies(collecting)]: You are not a contractor!"))
      }

    case InternalDeliverSupplies(elements: Seq[TElement], contractor: SuppliesContractor[TElement]) =>
      if (contractors(contractor)) {
        contractor.onProblem(UnspecifiedProblem("[LoadSupplies(collecting)]: You have not been asked to load supplies! Are you too late?"))
      } else {
        contractor.onProblem(UnspecifiedProblem("[LoadSupplies(collecting)]: You are not a contractor!"))
      }

    case Request(amount) =>
     if (!offers.isEmpty) {
        callForSupplies(offers, contractors)
      }
  }

  def transportingSupplies(
    deliverySchedule: Map[SuppliesContractor[TElement], Int],
    offers: Vector[SuppliesContractor[TElement]],
    contractors: Set[SuppliesContractor[TElement]]): Receive = {

    case InternalSignContract(contractor: SuppliesContractor[TElement]) =>
      if (!contractors(contractor)) {
        contractor.onStockroom(stockroom(contractor))
        context.become(transportingSupplies(
          deliverySchedule,
          offers,
          contractors + contractor))
      } else {
        contractor.onProblem(UnspecifiedProblem("[SignContract(transporting)]: You already a contractor!"))
      }

    case InternalCancelContract(contractor: SuppliesContractor[TElement]) =>
      if (contractors(contractor)) {
        context.become(transportingSupplies(
          deliverySchedule - contractor,
          offers.filterNot(_ == contractor),
          contractors - contractor))
      } else {
        contractor.onProblem(UnspecifiedProblem("[CancelContract(transporting)]: You are not a contractor!"))
      }

    case InternalOfferSupplies(amount: Int, contractor: SuppliesContractor[TElement]) =>
      if (contractors(contractor)) {
        val newOffers = offers ++ Vector.fill(amount)(contractor)
        if (!deliverySchedule.isEmpty) {
          context.become(transportingSupplies(
            deliverySchedule,
            newOffers,
            contractors))
        } else if (totalDemand > 0 && !newOffers.isEmpty) {
          callForSupplies(newOffers, contractors)
        } else {

          context.become(collectingOffers(
            newOffers,
            contractors))
        }
      } else {
        contractor.onProblem(UnspecifiedProblem("[OfferSupplies(transporting)]: You are not a contractor!"))
      }

    case InternalDeliverSupplies(elements: Seq[TElement], contractor: SuppliesContractor[TElement]) =>
      deliverySchedule.get(contractor) match {
        case Some(amount) =>
          if (elements.size == amount) {
            elements.foreach(onNext)
          } else {
            contractor.onProblem(UnspecifiedProblem("[LoadSupplies(transporting)]: You have loaded ${elements.size} elements instead of $amount. Elements have not been transported."))
          }

          val newDeliverySchedule = deliverySchedule - contractor
          if (newDeliverySchedule.isEmpty) {
            context.become(collectingOffers(
              offers,
              contractors))
          } else {
            context.become(transportingSupplies(newDeliverySchedule, offers, contractors))
          }

        case None =>
          contractor.onProblem(UnspecifiedProblem("[LoadSupplies(transporting)]: You are not to send any elements!"))
      }

    case Request(amount) =>
     ()
  }

  def receive: Receive = collectingOffers(Vector.empty, Set.empty)

  def callForSupplies(
    offers: Vector[SuppliesContractor[TElement]],
    contractors: Set[SuppliesContractor[TElement]]) {
    if (totalDemand > 0) {
      val toLoad = offers.take(totalDemand)
      val rest = offers.drop(toLoad.size)
      val deliveryPlan =
        toLoad
          .groupBy(identity)
          .map { case (sup, sups) => (sup, sups.size) }      

      deliveryPlan.foreach { case (sup, amount) => sup.onDeliverSuppliesNow(amount) }
      context.become(transportingSupplies(deliveryPlan, rest, contractors))
    }
  }

}

