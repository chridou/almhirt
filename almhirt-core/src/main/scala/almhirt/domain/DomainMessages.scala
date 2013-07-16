package almhirt.domain

object DomainMessages {
  sealed trait DomainMessage
  final case class GetAggregateRoot(arId: java.util.UUID) extends DomainMessage
  final case class RequestedAggregateRoot(ar: IsAggregateRoot) extends DomainMessage
  final case class AggregateRootNotFound(arId: java.util.UUID) extends DomainMessage
  final case class AggregateRootFetchFailed(problem: almhirt.common.Problem) extends DomainMessage
  final case class AggregateRootUpdated(newState: IsAggregateRoot) extends DomainMessage
  final case class AggregateRootUpdateFailed(problem: almhirt.common.Problem) extends DomainMessage
  final case class UpdateAggregateRoot(ar: IsAggregateRoot, events: IndexedSeq[DomainEvent]) extends DomainMessage

}