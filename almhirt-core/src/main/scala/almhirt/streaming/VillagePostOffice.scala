package almhirt.streaming

import akka.actor._
import almhirt.tracking.TrackingTicket

/** A VillagePostOffice tries to deliver small packages to a broker. After a package was sent, the sender of the package gets notified. 
 *  The VillagePostOffice will dispatch only on package at any given time.
 *	Packages will be sent in the order they are received. After a package has been sent, a DeliveryJobDone is sent.  
 */
trait VillagePostOffice[TElement] extends PostOffice[TElement]



trait ActorVillagePostOffice[TElement] { self: Actor with ActorLogging => 
  protected def broker: SuppliesBroker[TElement]
  
  protected def send(elements: TElement, ticket: Option[TrackingTicket]) {
    
  }
  
  
}



