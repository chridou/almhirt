package almhirt.eventlog

import almhirt.common._
import akka.actor._
import play.api.libs.iteratee.Enumerator
import almhirt.aggregates.AggregateRootId
import almhirt.aggregates.AggregateRootVersion

object AggregateEventLog {
  trait AggregateEventLogMessage
  trait AggregateEventLogResponse

  final case class CommitAggregateEvent(event: AggregateRootEvent) extends AggregateEventLogMessage
  sealed trait CommitAggregateEventResponse extends AggregateEventLogResponse
  final case class AggregateEventCommitted(id: EventId) extends CommitAggregateEventResponse
  final case class AggregateEventNotCommitted(id: EventId, problem: Problem) extends CommitAggregateEventResponse
  
  case object GetAllAggregateEvents extends AggregateEventLogMessage
  final case class GetAllAggregateEventsFor(aggId: AggregateRootId) extends AggregateEventLogMessage
  final case class GetAggregateEventsFrom(aggId: AggregateRootId, fromVersion: AggregateRootVersion) extends AggregateEventLogMessage
  final case class GetAggregateEventsTo(aggId: AggregateRootId, toVersion: AggregateRootVersion) extends AggregateEventLogMessage
  final case class GetAggregateEventsUntil(aggId: AggregateRootId, untilVersion: AggregateRootVersion) extends AggregateEventLogMessage
  final case class GetAggregateEventsFromTo(aggId: AggregateRootId, fromVersion: AggregateRootVersion, toVersion: AggregateRootVersion) extends AggregateEventLogMessage
  final case class GetAggregateEventsFromUntil(aggId: AggregateRootId, fromVersion: AggregateRootVersion, untilVersion: AggregateRootVersion) extends AggregateEventLogMessage
  sealed trait GetManyAggregateEventsResponse extends AggregateEventLogResponse
  final case class FetchedAggregateEvents(enumerator: Enumerator[AggregateRootEvent]) extends GetManyAggregateEventsResponse
  final case class GetAggregateEventsFailed(problem: Problem) extends GetManyAggregateEventsResponse


  final case class GetAggregateEvent(eventId: EventId) extends AggregateEventLogMessage
  sealed trait GetAggregateEventResponse extends AggregateEventLogResponse
  final case class FetchedAggregateEvent(eventId: EventId, event: Option[AggregateRootEvent]) extends GetAggregateEventResponse
  final case class GetAggregateEventFailed(eventId: EventId, problem: Problem) extends GetAggregateEventResponse
   
  object AggreagteEventLogCoordinates {
	  val actorname = "aggregate-event-log"
	  val logicalPath: String = s"user/almhirt/storage/$actorname"
  }
}
