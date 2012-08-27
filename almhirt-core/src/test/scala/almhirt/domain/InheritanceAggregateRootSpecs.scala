package almhirt.domain

import java.util.UUID

trait InvoiceEvent extends DomainEvent
case class InvoiceCreated(aggRootId: UUID) extends InvoiceEvent with CreatingNewAggregateRootEvent
case class AmountAdded(aggRootId: UUID, aggRootVersion: Long, amount: BigDecimal, total: BigDecimal) extends InvoiceEvent
case class InvoiceSent(aggRootId: UUID, aggRootVersion: Long) extends InvoiceEvent
case class InvoicePaid(aggRootId: UUID, aggRootVersion: Long) extends InvoiceEvent
case class InvoiceReopened(aggRootId: UUID, aggRootVersion: Long) extends InvoiceEvent


sealed abstract class Invoice extends AggregateRoot[Invoice, InvoiceEvent] {
  def id : UUID
  def version: Long
  def total: BigDecimal
}

case class DraftInvoice(id: UUID, version: Long, total: BigDecimal) extends Invoice {
  protected def handlers = transitionToSent

  private def transitionToSent: PartialFunction[InvoiceEvent, SentInvoice] = {
    case InvoiceSent(id,aggRootVersion) => SentInvoice(id, aggRootVersion+1, total)
  }


} 

case class SentInvoice(id: UUID, version: Long, total: BigDecimal) extends Invoice {
  protected def handlers = transitionToPaid

  private def transitionToPaid: PartialFunction[InvoiceEvent, PaidInvoice] = {
    case InvoicePaid(id,aggRootVersion) => PaidInvoice(id, aggRootVersion+1, total)
  }
} 

case class PaidInvoice(id: UUID, version: Long, total: BigDecimal) extends Invoice {
  protected def handlers = transitionToDraft

  private def transitionToDraft: PartialFunction[InvoiceEvent, DraftInvoice] = {
    case InvoiceReopened(id,aggRootVersion) => DraftInvoice(id, aggRootVersion+1, total)
  }

} 

import org.specs2.mutable._

class InheritanceAggregateRootSpecs {

}