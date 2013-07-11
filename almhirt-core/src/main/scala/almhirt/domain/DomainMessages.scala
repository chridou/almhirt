package almhirt.domain

object DomainMessages {
  final case class AggregateRootNotFound(arId: java.util.UUID)
  final case class AggregateRootWasDeleted(arId: java.util.UUID)
  final case class AggregateRootFetchError(problem: almhirt.common.Problem)
}