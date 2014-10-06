package almhirt.eventlog

import almhirt.common._
import akka.actor._
import play.api.libs.iteratee.Enumerator
import almhirt.aggregates.AggregateRootId
import almhirt.aggregates.AggregateRootVersion
import almhirt.aggregates.AggregateRootVersion
import almhirt.aggregates.AggregateRootVersion

object AggregateRootEventLog {
  trait AggregateRootEventLogMessage
  trait AggregateRootEventLogQueryManyMessage extends AggregateRootEventLogMessage { def traverse : TraverseWindow }
  trait AggregateRootEventLogResponse

  sealed trait VersionRangeStartMarker
  final case class FromVersion(version: AggregateRootVersion) extends VersionRangeStartMarker
  final case object FromStart extends VersionRangeStartMarker

  sealed trait VersionRangeEndMarker
  final case class ToVersion(version: AggregateRootVersion) extends VersionRangeEndMarker
  final case object ToEnd extends VersionRangeEndMarker

  final case class CommitAggregateRootEvent(event: AggregateRootEvent) extends AggregateRootEventLogMessage
  sealed trait CommitAggregateRootEventResponse extends AggregateRootEventLogResponse
  final case class AggregateRootEventCommitted(id: EventId) extends CommitAggregateRootEventResponse
  final case class AggregateRootEventNotCommitted(id: EventId, problem: Problem) extends CommitAggregateRootEventResponse

  final case class GetAllAggregateRootEvents(traverse: TraverseWindow) extends AggregateRootEventLogQueryManyMessage
  final case class GetAggregateRootEventsFor(aggId: AggregateRootId, start: VersionRangeStartMarker, end: VersionRangeEndMarker, traverse: TraverseWindow) extends AggregateRootEventLogQueryManyMessage
  object GetAllAggregateRootEventsFor {
    def apply(aggId: AggregateRootId, traverse: TraverseWindow): GetAggregateRootEventsFor =
      GetAggregateRootEventsFor(aggId, FromStart, ToEnd, traverse)
    def apply(aggId: AggregateRootId): GetAggregateRootEventsFor =
      GetAggregateRootEventsFor(aggId, FromStart, ToEnd, skip.none.takeAll)
  }
  object GetAggregateRootEventsFrom {
    def apply(aggId: AggregateRootId, from: AggregateRootVersion, traverse: TraverseWindow): GetAggregateRootEventsFor =
      GetAggregateRootEventsFor(aggId, FromVersion(from), ToEnd, traverse)
    def apply(aggId: AggregateRootId, from: AggregateRootVersion): GetAggregateRootEventsFor =
      GetAggregateRootEventsFor(aggId, FromVersion(from), ToEnd, skip.none.takeAll)
  }
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
