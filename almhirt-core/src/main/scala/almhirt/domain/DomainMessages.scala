package almhirt.domain

object DomainMessages {
  sealed trait DomainMessage
  final case class GetAggregateRoot(arId: java.util.UUID) extends DomainMessage

  sealed trait AggregateRootGetResponse extends DomainMessage
  final case class RequestedAggregateRoot(ar: IsAggregateRoot) extends AggregateRootGetResponse
  final case class AggregateRootNotFound(arId: java.util.UUID) extends AggregateRootGetResponse
  final case class AggregateRootFetchFailed(arId: java.util.UUID, problem: almhirt.common.Problem) extends AggregateRootGetResponse

  final case class UpdateAggregateRoot(newState: IsAggregateRoot, eventsToNewState: IndexedSeq[DomainEvent]) extends DomainMessage
  sealed trait AggregateRootUpdateResponse extends DomainMessage
  final case class AggregateRootUpdated(newState: IsAggregateRoot) extends AggregateRootUpdateResponse
  final case class AggregateRootUpdateFailed(problem: almhirt.common.Problem) extends AggregateRootUpdateResponse

  final case class IncompatibleAggregateRoot(ar: IsAggregateRoot, expected: String) extends DomainMessage
  final case class IncompatibleDomainEvent(expected: String) extends DomainMessage

}