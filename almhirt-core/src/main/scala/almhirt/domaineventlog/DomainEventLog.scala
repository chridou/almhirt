package almhirt.domaineventlog

import java.util.{ UUID => JUUID }
import almhirt.common._
import almhirt.domain.DomainEvent
import akka.actor.Actor
import almhirt.domain.AggregateRoot

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

  final case class CommittedDomainEvents(committed: Seq[DomainEvent], uncommitted: Option[(Seq[DomainEvent], Problem)]) extends DomainEventLogMessage

  sealed trait SingleDomainEventQueryResult extends DomainEventLogMessage
  final case class QueriedDomainEvent(eventId: JUUID, event: Option[DomainEvent])  extends SingleDomainEventQueryResult
  final case class DomainEventQueryFailed(eventId: JUUID, problem: Problem)  extends SingleDomainEventQueryResult

  sealed trait FetchedDomainEventsPart extends DomainEventLogMessage {
    def index: Int
    def isLast: Boolean
  }

  final case class DomainEventsChunk(
    /**
     * Starts with Zero
     */
    index: Int,
    isLast: Boolean,
    events: Seq[DomainEvent]) extends FetchedDomainEventsPart

  final case class DomainEventsChunkFailure(
    /**
     * Starts with Zero
     */
    index: Int,
    problem: Problem) extends FetchedDomainEventsPart {
    override def isLast = true
  }
  
  object NothingCommitted {
    def unapply(what: CommittedDomainEvents): Boolean =
      what.committed.isEmpty && what.uncommitted.isEmpty
  }
  
  object AllDomainEventsSuccessfullyCommitted {
    def unapply(what: CommittedDomainEvents): Option[Seq[DomainEvent]] =
      if (!what.committed.isEmpty && what.uncommitted.isEmpty) Some(what.committed) else None
  }

  object DomainEventsPartiallyCommitted {
    def unapply(what: CommittedDomainEvents): Option[(Seq[DomainEvent], Problem, Seq[DomainEvent])] =
      if (!what.committed.isEmpty && !what.uncommitted.isEmpty) Some((what.committed, what.uncommitted.get._2,  what.uncommitted.get._1)) else None
  }

  object CommitDomainEventsFailed {
    def unapply(what: CommittedDomainEvents): Option[(Problem, Seq[DomainEvent])] =
      if (what.committed.isEmpty && what.uncommitted.isDefined) Some((what.uncommitted.get._2,  what.uncommitted.get._1)) else None
  }
}

trait DomainEventLog { actor: Actor =>
  protected def receiveDomainEventLogMsg: Receive
}