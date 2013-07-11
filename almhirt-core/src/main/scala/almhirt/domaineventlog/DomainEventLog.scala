package almhirt.domaineventlog

import java.util.{ UUID => JUUID }
import almhirt.common._
import almhirt.domain.DomainEvent
import akka.actor.Actor

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
}

trait DomainEventLog { self: Actor =>
  protected def receiveDomainEventLogMsg: Receive
}