package almhirt.domain

import java.util.UUID

trait InvoiceEvent extends DomainEvent
case class InvoiceCreated(aggRootId: UUID) extends InvoiceEvent with CreatingNewAggregateRootEvent
case class AmountAdded(aggRootId: UUID, aggRootVersion: Long, amount: BigDecimal) extends InvoiceEvent
case class InvoiceSent(aggRootId: UUID, aggRootVersion: Long) extends InvoiceEvent
case class InvoicePaid(aggRootId: UUID, aggRootVersion: Long) extends InvoiceEvent
case class InvoiceReopened(aggRootId: UUID, aggRootVersion: Long) extends InvoiceEvent


sealed abstract class Invoice extends AggregateRoot[Invoice, InvoiceEvent] {
  def total: BigDecimal
  def isPaid: Boolean
  def isSent: Boolean
  
  private def transitionToDraft: PartialFunction[InvoiceEvent, DraftInvoice] = {
    case InvoiceCreated(id) => DraftInvoice(id, 0)
  }
  
}

case class DraftInvoice(id: UUID, version: Long, total: BigDecimal) extends Invoice {
  val isPaid = false
  val isSent = false
  protected def handlers = 
} 

case class SentInvoice(id: UUID, version: Long, total: BigDecimal) extends Invoice {
  val isPaid = false
  val isSent = true
  protected def handlers = 
} 

case class PaidInvoice(id: UUID, version: Long, total: BigDecimal) extends Invoice {
  val isPaid = true
  val isSent = true
  protected def handlers = 
} 

import org.specs2.mutable._

class InheritanceAggregateRootSpecs {

}