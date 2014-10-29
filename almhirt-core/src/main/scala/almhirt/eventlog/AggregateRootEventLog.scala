package almhirt.eventlog

import akka.actor._
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.almvalidation.kit._
import play.api.libs.iteratee.Enumerator
import almhirt.aggregates.{ AggregateRootId, AggregateRootVersion }

object AggregateRootEventLog {
  trait AggregateRootEventLogMessage
  trait AggregateRootEventLogQueryManyMessage extends AggregateRootEventLogMessage { def traverse: TraverseWindow }
  trait AggregateRootEventLogResponse

  sealed trait VersionRangeStartMarker
  final case class FromVersion(version: AggregateRootVersion) extends VersionRangeStartMarker
  final case object FromStart extends VersionRangeStartMarker

  object VersionRangeStartMarker {
    def fromLongOption(v: Option[Long]): VersionRangeStartMarker =
      v match {
        case Some(v) if v >= 0 ⇒ FromVersion(AggregateRootVersion(v))
        case _ ⇒ FromStart
      }

    def parseStringOption(v: Option[String]): AlmValidation[VersionRangeStartMarker] = {
      v match {
        case None ⇒ FromStart.success
        case Some(v) ⇒
          if (v.trim().isEmpty())
            FromStart.success
          else
            v.toLongAlm.map(v ⇒ fromLongOption(Some(v)))
      }
    }
  }

  sealed trait VersionRangeEndMarker
  final case class ToVersion(version: AggregateRootVersion) extends VersionRangeEndMarker
  final case object ToEnd extends VersionRangeEndMarker

  object VersionRangeEndMarker {
    def fromLongOption(v: Option[Long]): VersionRangeEndMarker =
      v match {
        case Some(v) if v >= 0 && v < Int.MaxValue ⇒ ToVersion(AggregateRootVersion(v))
        case Some(v) if v == Int.MaxValue ⇒ ToEnd
        case Some(v) if v < 0 ⇒ ToVersion(AggregateRootVersion(0L))
        case None ⇒ ToEnd
      }

    def parseStringOption(v: Option[String]): AlmValidation[VersionRangeEndMarker] = {
      v match {
        case None ⇒ ToEnd.success
        case Some(v) ⇒
          if (v.trim().isEmpty())
            ToEnd.success
          else
            v.toLongAlm.map(v ⇒ fromLongOption(Some(v)))
      }
    }
  }

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
  def path(root: RootActorPath) = almhirt.context.ContextActorPaths.eventLogs(root) / actorname 
}
