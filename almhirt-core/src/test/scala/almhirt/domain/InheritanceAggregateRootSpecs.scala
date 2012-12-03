package almhirt.domain

import java.util.UUID
import org.joda.time.DateTime

trait InvoiceEvent extends DomainEvent
case class InvoiceCreated(id: UUID, aggId: UUID, timestamp: DateTime = new DateTime) extends InvoiceEvent with CreatingNewAggregateRootEvent
case class AmountAdded(id: UUID, aggId: UUID, aggVersion: Long, amount: BigDecimal, total: BigDecimal, timestamp: DateTime = new DateTime) extends InvoiceEvent
case class InvoiceSent(id: UUID, aggId: UUID, aggVersion: Long, timestamp: DateTime = new DateTime) extends InvoiceEvent
case class InvoicePaid(id: UUID, aggId: UUID, aggVersion: Long, timestamp: DateTime = new DateTime) extends InvoiceEvent
case class InvoiceReopened(id: UUID, aggId: UUID, aggVersion: Long, timestamp: DateTime = new DateTime) extends InvoiceEvent

sealed abstract class Invoice extends AggregateRootWithHandlers[Invoice, InvoiceEvent] {
  def id: UUID
  def version: Long
  def total: BigDecimal
}

case class DraftInvoice(id: UUID, version: Long, total: BigDecimal) extends Invoice {
  protected def handlers = transitionToSent

  private def transitionToSent: PartialFunction[InvoiceEvent, SentInvoice] = {
    case InvoiceSent(_, aggId, aggRootVersion, _) => SentInvoice(aggId, aggRootVersion + 1, total)
  }

}

case class SentInvoice(id: UUID, version: Long, total: BigDecimal) extends Invoice {
  protected def handlers = transitionToPaid

  private def transitionToPaid: PartialFunction[InvoiceEvent, PaidInvoice] = {
    case InvoicePaid(_, aggId, aggRootVersion, _) => PaidInvoice(aggId, aggRootVersion + 1, total)
  }
}

case class PaidInvoice(id: UUID, version: Long, total: BigDecimal) extends Invoice {
  protected def handlers = transitionToDraft

  private def transitionToDraft: PartialFunction[InvoiceEvent, DraftInvoice] = {
    case InvoiceReopened(_, aggId, aggRootVersion, _) => DraftInvoice(aggId, aggRootVersion + 1, total)
  }
}

import org.specs2.mutable._

class InheritanceAggregateRootSpecs {

}