package almhirt.domaineventlog

import java.util.{ UUID => JUUID }
import almhirt.common._
import almhirt.domain.DomainEvent
import akka.actor.Actor
import almhirt.domain.AggregateRoot
import almhirt.messaging.MessagePublisher
import play.api.libs.iteratee.Enumerator

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

  sealed trait CommitDomainEventsResult extends DomainEventLogMessage
  final case class CommittedDomainEvents(committed: Seq[DomainEvent]) extends CommitDomainEventsResult
  final case class CommitDomainEventsFailed(problem: Problem) extends CommitDomainEventsResult

  sealed trait SingleDomainEventQueryResult extends DomainEventLogMessage
  final case class QueriedDomainEvent(eventId: JUUID, event: Option[DomainEvent]) extends SingleDomainEventQueryResult
  final case class DomainEventQueryFailed(eventId: JUUID, problem: Problem) extends SingleDomainEventQueryResult

  sealed trait FetchDomainEventsResult extends DomainEventLogMessage
  final case class FetchedDomainEvents(enumerator: Enumerator[DomainEvent]) extends FetchDomainEventsResult
  final case class FetchDomainEventsFailed(problem: Problem) extends FetchDomainEventsResult

//  final case class FetchedDomainEventsBatch(
//    events: Seq[DomainEvent]) extends FetchedDomainEvents


//  final case class FetchedDomainEventsFailure(problem: Problem) extends FetchedDomainEvents

  object CommitFailed {
    def unapply(what: CommitDomainEventsResult): Option[Problem] =
      what match {
        case CommitDomainEventsFailed(p) => Some(p)
        case _ => None
      }
  }

  object NothingCommitted {
    def unapply(what: CommitDomainEventsResult): Boolean =
      what match {
        case CommitDomainEventsFailed(p) => false
        case CommittedDomainEvents(committed) => committed.isEmpty
      }
  }

  object DomainEventsSuccessfullyCommitted {
    def unapply(what: CommitDomainEventsResult): Option[Seq[DomainEvent]] =
      what match {
        case CommitDomainEventsFailed(p) => None
        case CommittedDomainEvents(committed) =>
          if (committed.isEmpty)
            None
          else
            Some(committed)
      }
  }
}

trait DomainEventLog { actor: Actor =>
  def publishCommittedEvent(event: DomainEvent)

  protected def receiveDomainEventLogMsg: Receive
}