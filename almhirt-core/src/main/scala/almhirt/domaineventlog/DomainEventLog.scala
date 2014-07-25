package almhirt.domaineventlog

import java.util.{ UUID => JUUID }
import almhirt.common._
import akka.actor._
import almhirt.core.types._
import play.api.libs.iteratee.Enumerator

object DomainEventLog {
  trait DomainEventLogMessage
  trait DomainEventLogResponse

  final case class CommitDomainEvents(events: Seq[DomainEvent]) extends DomainEventLogMessage
  sealed trait CommitDomainEventsResponse extends DomainEventLogResponse
  case object DomainEventsCommitted extends CommitDomainEventsResponse
  final case class CommitDomainEventsFailed(notCommited: Seq[JUUID]) extends CommitDomainEventsResponse
  
  case object GetAllDomainEvents extends DomainEventLogMessage
  final case class GetDomainEvent(eventId: JUUID) extends DomainEventLogMessage
  final case class GetAllDomainEventsFor(aggId: JUUID) extends DomainEventLogMessage
  final case class GetDomainEventsFrom(aggId: JUUID, fromVersion: Long) extends DomainEventLogMessage
  final case class GetDomainEventsTo(aggId: JUUID, toVersion: Long) extends DomainEventLogMessage
  final case class GetDomainEventsUntil(aggId: JUUID, untilVersion: Long) extends DomainEventLogMessage
  final case class GetDomainEventsFromTo(aggId: JUUID, fromVersion: Long, toVersion: Long) extends DomainEventLogMessage
  final case class GetDomainEventsFromUntil(aggId: JUUID, fromVersion: Long, untilVersion: Long) extends DomainEventLogMessage


  sealed trait GetDomainEventResponse extends DomainEventLogResponse
  final case class FetchedDomainEvent(eventId: JUUID, event: Option[DomainEvent]) extends GetDomainEventResponse
  final case class GetDomainEventFailed(eventId: JUUID, problem: Problem) extends GetDomainEventResponse

  sealed trait GetManyDomainEventsResponse extends DomainEventLogResponse
  final case class FetchedDomainEvents(enumerator: Enumerator[DomainEvent]) extends GetManyDomainEventsResponse
  final case class GetDomainEventsFailed(problem: Problem) extends GetManyDomainEventsResponse

  object CommitFailed {
    def unapply(what: CommitDomainEventsResponse): Boolean =
      what match {
        case CommitDomainEventsFailed(_) => true
        case _ => false
      }
  }

  object Committed {
    def unapply(what: CommitDomainEventsResponse): Boolean =
      what match {
        case CommitDomainEventsFailed(_) => false
        case _ => true
      }
  }
  
  val logicalPath: String = "user/almhirt/storage/domaineventlog"
}
