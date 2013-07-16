package almhirt.domain

object DomainMessages {
  sealed trait DomainMessage
  final case class AggregateRootNotFound(arId: java.util.UUID) extends DomainMessage
  final case class AggregateRootWasDeleted(arId: java.util.UUID) extends DomainMessage
  final case class AggregateRootFetchError(problem: almhirt.common.Problem) extends DomainMessage
}