package almhirt.aggregates

sealed trait AggregateRootLifecycle[+T <: AggregateRoot]

sealed trait Antemortem[+T <: AggregateRoot] extends AggregateRootLifecycle[T]
sealed trait Postnatalis[+T <: AggregateRoot] extends AggregateRootLifecycle[T] {
  def version: AggregateRootVersion
  def id: AggregateRootId
}
sealed trait Transcendentia[+T <: AggregateRoot] extends AggregateRootLifecycle[T]

case object Vacat extends Antemortem[Nothing] with Transcendentia[Nothing]
final case class Vivus[T <: AggregateRoot](ar: T) extends Postnatalis[T] with Antemortem[T] {
  def version: AggregateRootVersion = ar.version
  def id: AggregateRootId = ar.id
}
final case class Mortuus(id: AggregateRootId, version: AggregateRootVersion) extends Postnatalis[Nothing] with Transcendentia[Nothing]
