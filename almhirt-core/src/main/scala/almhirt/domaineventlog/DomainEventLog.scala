package almhirt.domaineventlog

import java.util.{ UUID => JUUID }
import almhirt.common._
import almhirt.domain.DomainEvent
import akka.actor.Actor
import almhirt.domain.AggregateRoot

object DomainEventLog {
  sealed trait FetchedDomainEventsPart {
    def index: Int
    def isLast: Boolean
  }

  final case class DomainEventsChunk(
    /**
     * Starts with Zero
     */
    index: Int,
    isLast: Boolean,
    events: Iterable[DomainEvent]) extends FetchedDomainEventsPart

  final case class DomainEventsChunkFailure(
    /**
     * Starts with Zero
     */
    index: Int,
    problem: Problem) extends FetchedDomainEventsPart {
    override def isLast = true
  }

  final case class LogDomainEvents(events: IndexedSeq[DomainEvent])
  case object GetAllDomainEvents
  final case class GetDomainEvent(eventId: JUUID)
  final case class GetDomainEvents(aggId: JUUID)
  final case class GetDomainEventsFrom(aggId: JUUID, fromVersion: Long)
  final case class GetDomainEventsTo(aggId: JUUID, toVersion: Long)
  final case class GetDomainEventsUntil(aggId: JUUID, untilVersion: Long)
  final case class GetDomainEventsFromTo(aggId: JUUID, fromVersion: Long, toVersion: Long)
  final case class GetDomainEventsFromUntil(aggId: JUUID, fromVersion: Long, untilVersion: Long)

  final case class CommittedDomainEvents(committed: Iterable[DomainEvent], uncommitted: Option[(Iterable[DomainEvent], Problem)])

  object NothingCommitted {
    def unapply(what: CommittedDomainEvents): Boolean =
      what.committed.isEmpty && what.uncommitted.isEmpty
  }
  
  object AllDomainEventsSuccessfullyCommitted {
    def unapply(what: CommittedDomainEvents): Option[Iterable[DomainEvent]] =
      if (!what.committed.isEmpty && what.uncommitted.isEmpty) Some(what.committed) else None
  }

  object DomainEventsPartiallyCommitted {
    def unapply(what: CommittedDomainEvents): Option[(Iterable[DomainEvent], Problem, Iterable[DomainEvent])] =
      if (!what.committed.isEmpty && !what.uncommitted.isEmpty) Some((what.committed, what.uncommitted.get._2,  what.uncommitted.get._1)) else None
  }

  object CommitDomainEventsFailed {
    def unapply(what: CommittedDomainEvents): Option[(Problem, Iterable[DomainEvent])] =
      if (what.committed.isEmpty && what.uncommitted.isDefined) Some((what.uncommitted.get._2,  what.uncommitted.get._1)) else None
  }
  
}

trait DomainEventLog { self: Actor =>
  protected def receiveDomainEventLogMsg: Receive
}