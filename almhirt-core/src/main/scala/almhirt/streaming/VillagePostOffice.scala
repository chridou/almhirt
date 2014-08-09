package almhirt.streaming

import akka.actor._
import almhirt.tracking.TrackingTicket

/**
 * A VillagePostOffice is intended to deliver small packages to a broker. After a package was sent, the sender of the package gets notified.
 *  The VillagePostOffice will dispatch only on package at any given time.
 * 	Packages will be sent in the order they are received. After a package has been sent, a DeliveryJobDone is sent.
 *  DeliveryJobDone notifications will be sent in the same order as the packages came in.
 */
trait VillagePostOffice[TElement] extends PostOffice[TElement]

class VillagePostOfficeStrategyFactory[TElement] extends PostOfficeStrategyFactory[TElement] {
  type MyStash = Vector[(Seq[TElement], Option[TrackingTicket], ActorRef)]

  def create(stockroom: Stockroom[TElement], maxBufferSize: Int): PostOfficeStrategy[TElement] = {
    var myStash: MyStash = Vector.empty
    var buffered: Int = 0

    new PostOfficeStrategy[TElement] {
      def stash(elements: Seq[TElement], ticket: Option[TrackingTicket], notify: ActorRef) = {
        if (!elements.isEmpty) {
          val numElementsToAdd = elements.size
          if (numElementsToAdd + buffered <= maxBufferSize) {
            stockroom.offerSupplies(numElementsToAdd)
            buffered = buffered + numElementsToAdd
            myStash = myStash :+ (elements, ticket, notify)
          } else {
            notify ! PackageNotAccepted(ticket)
          }
        } else {
          notify ! DeliveryJobDone(ticket)
        }
      }

      def deliverToBroker(amount: Int) {
        val (rest, toDeliver, notifies) = deliverFromPackages(myStash, amount)
        stockroom.deliverSupplies(toDeliver)
        buffered = buffered - toDeliver.size
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
  def maxBufferSize: Int
  protected final override def createStrategy(stockroom: Stockroom[TElement]) =
    new VillagePostOfficeStrategyFactory[TElement]().create(stockroom, maxBufferSize)
}



