package almhirt.domain

sealed trait AggregateRootRef{ def id: java.util.UUID }
case class SpecificVersion(id: java.util.UUID, version: Long) extends AggregateRootRef
case class LatestVersion(id: java.util.UUID) extends AggregateRootRef