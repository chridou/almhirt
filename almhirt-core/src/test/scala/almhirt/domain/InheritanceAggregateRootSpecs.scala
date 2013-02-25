package almhirt.domain

import java.util.UUID
import org.joda.time.DateTime

trait InvoiceEvent extends DomainEvent
case class InvoiceCreated(id: UUID, aggRef: AggregateRootRef, timestamp: DateTime = new DateTime) extends InvoiceEvent with CreatingNewAggregateRootEvent
case class AmountAdded(id: UUID, aggRef: AggregateRootRef, amount: BigDecimal, total: BigDecimal, timestamp: DateTime = new DateTime) extends InvoiceEvent
case class InvoiceSent(id: UUID, aggRef: AggregateRootRef, timestamp: DateTime = new DateTime) extends InvoiceEvent
case class InvoicePaid(id: UUID, aggRef: AggregateRootRef, timestamp: DateTime = new DateTime) extends InvoiceEvent
case class InvoiceReopened(id: UUID, aggRef: AggregateRootRef, timestamp: DateTime = new DateTime) extends InvoiceEvent

sealed abstract class Invoice extends AggregateRootWithHandlers[Invoice, InvoiceEvent] {
  def total: BigDecimal
}

case class DraftInvoice(ref: AggregateRootRef, total: BigDecimal) extends Invoice {
  protected def handlers = transitionToSent

  private def transitionToSent: PartialFunction[InvoiceEvent, SentInvoice] = {
    case InvoiceSent(_, ref, _) => SentInvoice(ref.inc, total)
  }

}

case class SentInvoice(ref: AggregateRootRef, total: BigDecimal) extends Invoice {
  protected def handlers = transitionToPaid

  private def transitionToPaid: PartialFunction[InvoiceEvent, PaidInvoice] = {
    case InvoicePaid(_, ref, _) => PaidInvoice(ref.inc, total)
  }
}

case class PaidInvoice(ref: AggregateRootRef, total: BigDecimal) extends Invoice {
  protected def handlers = transitionToDraft

  private def transitionToDraft: PartialFunction[InvoiceEvent, DraftInvoice] = {
    case InvoiceReopened(_, ref, _) => DraftInvoice(ref.inc, total)
  }
}

class InheritanceAggregateRootSpecs {

}