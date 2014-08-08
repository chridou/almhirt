package almhirt.streaming

import akka.actor._
import almhirt.common._
import almhirt.tracking.TrackingTicket

trait PostOffice[TElement] {
  def deliver(blister: Seq[TElement], notify: ActorRef)
  def deliverTracked(blister: Seq[TElement], ticket: TrackingTicket, notify: ActorRef)
}


trait ActorPostOffice[TElement] { self: Actor with ActorLogging => 
  protected def broker: SuppliesBroker[TElement]
  
  val contractor = new SuppliesContractor[TElement] {
	  def onProblem(problem: Problem) = {
	    
	  }
	  
	  def onStockroom(stockroom: Stockroom[TElement])= {
	    
	  }
	  
	  def onDeliverSuppliesNow(amount: Int)= {
	    
	  }
	  
	  def onContractExpired()= {
	    
	  }
	  
  }
  
  protected def send(elements: TElement, ticket: Option[TrackingTicket]) {
    
  }
  
  
}