package almhirt.streaming

/** An ExpressPostOffice tries to deliver small packages to a broker. After a package was sent, the sender of the package gets notified. 
 *  The ExpressPostOffice may try to deliver multiple packages concurrently.
 *	Packages might not be sent in the order they are received. After a package has been sent, a DeliveryJobDone is sent.  
 */
trait ExpressPostOffice[TElement] extends PostOffice[TElement]