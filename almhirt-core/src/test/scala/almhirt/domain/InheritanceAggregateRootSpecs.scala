package almhirt.domain

import java.util.UUID
import org.joda.time.DateTime

trait InvoiceEvent extends DomainEvent
case class InvoiceCreated(header: DomainEventHeader) extends InvoiceEvent with CreatingNewAggregateRootEvent
case class AmountAdded(header: DomainEventHeader, amount: BigDecimal, total: BigDecimal) extends InvoiceEvent
case class InvoiceSent(header: DomainEventHeader) extends InvoiceEvent
case class InvoicePaid(header: DomainEventHeader) extends InvoiceEvent
case class InvoiceReopened(header: DomainEventHeader) extends InvoiceEvent

sealed abstract class Invoice extends AggregateRootWithHandlers[Invoice, InvoiceEvent] {
  def total: BigDecimal
}

case class DraftInvoice(ref: AggregateRootRef, total: BigDecimal) extends Invoice {
  protected def handlers = transitionToSent

  private def transitionToSent: PartialFunction[InvoiceEvent, SentInvoice] = {
    case InvoiceSent(header) => SentInvoice(ref.inc, total)
  }

}

case class SentInvoice(ref: AggregateRootRef, total: BigDecimal) extends Invoice {
  protected def handlers = transitionToPaid

  private def transitionToPaid: PartialFunction[InvoiceEvent, PaidInvoice] = {
    case InvoicePaid(_) => PaidInvoice(ref.inc, total)
  }
}

case class PaidInvoice(ref: AggregateRootRef, total: BigDecimal) extends Invoice {
  protected def handlers = transitionToDraft

  private def transitionToDraft: PartialFunction[InvoiceEvent, DraftInvoice] = {
    case InvoiceReopened(_) => DraftInvoice(ref.inc, total)
  }
}

class InheritanceAggregateRootSpecs {

}