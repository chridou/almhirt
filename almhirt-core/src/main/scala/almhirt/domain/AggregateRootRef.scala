package almhirt.domain

sealed trait AggregateRootRef{ 
  def id: java.util.UUID
  def tryGetVersion = this match { case SpecificVersion(_,v) => Some(v); case LatestVersion(_) => None}}
case class SpecificVersion(id: java.util.UUID, version: Long) extends AggregateRootRef
case class LatestVersion(id: java.util.UUID) extends AggregateRootRef