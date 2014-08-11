package almhirt.streaming

import akka.actor._
import almhirt.tracking.TrackingTicket

/**
 * A VillagePostOffice is intended to deliver small packages to a broker. After a package was sent, the sender of the package gets notified.
 *  The VillagePostOffice will dispatch only on package at any given time.
 * 	Packages will be sent in the order they are received. After a package has been sent, a DeliveryJobDone is sent.
 *  DeliveryJobDone notifications will be sent in the same order as the packages came in.
 */
object VillagePostOffice {
  def props[TElement](theBroker: SuppliesBroker[TElement], theMaxPackageBufferSize: Int): Props =
    Props(
      new ActorVillagePostOffice[TElement] with PostOfficeLoop[TElement] with Actor with ActorLogging {
        def broker = theBroker
        val maxPackageBufferSize = theMaxPackageBufferSize
        
        override def preStart() { super.preStart() }
      })
}

class VillagePostOfficeStrategyFactory[TElement] extends PostOfficeStrategyFactory[TElement] {
  private type MyStash = Vector[(Seq[TElement], Option[TrackingTicket], ActorRef)]

  final def create(stockroom: Stockroom[TElement], maxPackageBufferSize: Int): PostOfficeStrategy[TElement] = {
    var myStash: MyStash = Vector.empty

    new PostOfficeStrategy[TElement] {
      def stash(elements: Seq[TElement], ticket: Option[TrackingTicket], notify: ActorRef) = {
        if (!elements.isEmpty) {
          if (myStash.size < maxPackageBufferSize) {
            stockroom.offerSupplies(elements.size)
            myStash = myStash :+ (elements, ticket, notify)
          } else {
            notify ! DeliveryJobNotAccepted(ticket)
          }
        } else {
          notify ! DeliveryJobDone(ticket)
        }
      }

      def deliverToBroker(amount: Int) {
        val (rest, toDeliver, notifies) = deliverFromPackages(myStash, amount)
        stockroom.deliverSupplies(toDeliver)
        notifies()
        myStash = rest
      }
    }

  }

  private def deliverFromPackages(stash: MyStash, amount: Int): (MyStash, Seq[TElement], () => Unit) = {
    val (restOfPackage, ticket, notify) +: rest = stash
    val elementsToDeliver = restOfPackage.take(amount)
    val remainder = amount - elementsToDeliver.size
    val restOfPackageAferDelivery = restOfPackage.drop(elementsToDeliver.size)
    (restOfPackageAferDelivery, remainder) match {
      case (Seq(), 0) =>
        (rest, elementsToDeliver, () => notify ! DeliveryJobDone(ticket))
      case (elementsLeft, 0) =>
        ((elementsLeft, ticket, notify) +: rest, elementsToDeliver, () => ())
      case (Seq(), _) =>
        val (remainingStash, elements, notifies) = deliverFromPackages(rest, remainder)
        (remainingStash, elementsToDeliver ++ elements, () => { notify ! DeliveryJobDone(ticket); notifies() })
    }
  }
}

trait ActorVillagePostOffice[TElement] extends ActorPostOffice[TElement] { me: Actor with ActorLogging =>
  def maxPackageBufferSize: Int
  protected final override def createStrategy(stockroom: Stockroom[TElement]) =
    new VillagePostOfficeStrategyFactory[TElement]().create(stockroom, maxPackageBufferSize)
}



