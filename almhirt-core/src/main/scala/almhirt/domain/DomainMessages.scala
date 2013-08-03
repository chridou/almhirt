package almhirt.domain

object DomainMessages {
  sealed trait DomainMessage
  final case class GetAggregateRoot(arId: java.util.UUID) extends DomainMessage
  final case class RequestedAggregateRoot(ar: IsAggregateRoot) extends DomainMessage
  final case class AggregateRootNotFound(arId: java.util.UUID) extends DomainMessage
  final case class AggregateRootFetchFailed(arId: java.util.UUID, problem: almhirt.common.Problem) extends DomainMessage
  final case class AggregateRootUpdated(newState: IsAggregateRoot) extends DomainMessage
  final case class AggregateRootUpdateFailed(problem: almhirt.common.Problem) extends DomainMessage
  final case class UpdateAggregateRoot(newState: IsAggregateRoot, eventsToNewState: IndexedSeq[DomainEvent]) extends DomainMessage
  final case class IncompatibleAggregateRoot(ar: IsAggregateRoot, expected: String) extends DomainMessage
  final case class IncompatibleDomainEvent(expected: String) extends DomainMessage

}