package almhirt.eventlog

import almhirt.common._
import akka.actor._
import play.api.libs.iteratee.Enumerator
import almhirt.aggregates.AggregateRootId
import almhirt.aggregates.AggregateRootVersion

object AggregateRootEventLog {
  trait AggregateRootEventLogMessage
  trait AggregateRootEventLogResponse

  final case class CommitAggregateRootEvent(event: AggregateRootEvent) extends AggregateRootEventLogMessage
  sealed trait CommitAggregateRootEventResponse extends AggregateRootEventLogResponse
  final case class AggregateRootEventCommitted(id: EventId) extends CommitAggregateRootEventResponse
  final case class AggregateRootEventNotCommitted(id: EventId, problem: Problem) extends CommitAggregateRootEventResponse

  case object GetAllAggregateRootEvents extends AggregateRootEventLogMessage
  final case class GetAllAggregateRootEventsFor(aggId: AggregateRootId) extends AggregateRootEventLogMessage
  final case class GetAggregateRootEventsFrom(aggId: AggregateRootId, fromVersion: AggregateRootVersion) extends AggregateRootEventLogMessage
  final case class GetAggregateRootEventsTo(aggId: AggregateRootId, toVersion: AggregateRootVersion) extends AggregateRootEventLogMessage
  final case class GetAggregateRootEventsUntil(aggId: AggregateRootId, untilVersion: AggregateRootVersion) extends AggregateRootEventLogMessage
  final case class GetAggregateRootEventsFromTo(aggId: AggregateRootId, fromVersion: AggregateRootVersion, toVersion: AggregateRootVersion) extends AggregateRootEventLogMessage
  final case class GetAggregateRootEventsFromUntil(aggId: AggregateRootId, fromVersion: AggregateRootVersion, untilVersion: AggregateRootVersion) extends AggregateRootEventLogMessage
  sealed trait GetManyAggregateRootEventsResponse extends AggregateRootEventLogResponse
  final case class FetchedAggregateRootEvents(enumerator: Enumerator[AggregateRootEvent]) extends GetManyAggregateRootEventsResponse
  final case class GetAggregateRootEventsFailed(problem: Problem) extends GetManyAggregateRootEventsResponse

  final case class GetAggregateRootEvent(eventId: EventId) extends AggregateRootEventLogMessage
  sealed trait GetAggregateRootEventResponse extends AggregateRootEventLogResponse
  final case class FetchedAggregateRootEvent(eventId: EventId, event: Option[AggregateRootEvent]) extends GetAggregateRootEventResponse
  final case class GetAggregateRootEventFailed(eventId: EventId, problem: Problem) extends GetAggregateRootEventResponse

  val actorname = "aggregate-event-log"
  def logicalPath(actorname: String): String = s"/user/almhirt/components/event-logs/$actorname"
}
