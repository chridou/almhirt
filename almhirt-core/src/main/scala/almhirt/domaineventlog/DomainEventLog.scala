package almhirt.domaineventlog

import java.util.{ UUID => JUUID }
import almhirt.common._
import almhirt.domain.DomainEvent
import akka.actor.Actor
import almhirt.domain.AggregateRoot
import almhirt.messaging.MessagePublisher

object DomainEventLog {
  trait DomainEventLogMessage
  
  final case class CommitDomainEvents(events: Seq[DomainEvent]) extends DomainEventLogMessage
  case object GetAllDomainEvents extends DomainEventLogMessage
  final case class GetDomainEvent(eventId: JUUID) extends DomainEventLogMessage
  final case class GetAllDomainEventsFor(aggId: JUUID) extends DomainEventLogMessage
  final case class GetDomainEventsFrom(aggId: JUUID, fromVersion: Long) extends DomainEventLogMessage
  final case class GetDomainEventsTo(aggId: JUUID, toVersion: Long) extends DomainEventLogMessage
  final case class GetDomainEventsUntil(aggId: JUUID, untilVersion: Long) extends DomainEventLogMessage
  final case class GetDomainEventsFromTo(aggId: JUUID, fromVersion: Long, toVersion: Long) extends DomainEventLogMessage
  final case class GetDomainEventsFromUntil(aggId: JUUID, fromVersion: Long, untilVersion: Long) extends DomainEventLogMessage

  final case class CommittedDomainEvents(committed: Seq[DomainEvent]) extends DomainEventLogMessage

  sealed trait SingleDomainEventQueryResult extends DomainEventLogMessage
  final case class QueriedDomainEvent(eventId: JUUID, event: Option[DomainEvent])  extends SingleDomainEventQueryResult
  final case class DomainEventQueryFailed(eventId: JUUID, problem: Problem)  extends SingleDomainEventQueryResult

  sealed trait FetchedDomainEvents extends DomainEventLogMessage

  final case class FetchedDomainEventsBatch(
    events: Seq[DomainEvent]) extends FetchedDomainEvents
  
  final case class FetchedDomainEventsChunks() extends FetchedDomainEvents
  
  object NothingCommitted {
    def unapply(what: CommittedDomainEvents): Boolean =
      what.committed.isEmpty
  }
  
  object DomainEventsSuccessfullyCommitted {
    def unapply(what: CommittedDomainEvents): Option[Seq[DomainEvent]] =
      if (!what.committed.isEmpty) Some(what.committed) else None
  }
}

trait DomainEventLog { actor: Actor =>
  def publishCommittedEvent(event: DomainEvent)
  
  protected def receiveDomainEventLogMsg: Receive
}