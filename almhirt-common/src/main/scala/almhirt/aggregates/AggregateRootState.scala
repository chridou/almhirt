package almhirt.aggregates

sealed trait AggregateRootState[+T <: AggregateRoot]
sealed trait Antemortem[+T <: AggregateRoot] extends AggregateRootState[T]
sealed trait Postnatalis[+T <: AggregateRoot] extends AggregateRootState[T]
sealed trait Transcendental[+T <: AggregateRoot] extends AggregateRootState[T]
case object NeverExisted extends Transcendental[Nothing] with Antemortem[Nothing]
final case class Alive[T <: AggregateRoot](ar: T) extends Postnatalis[T] with Antemortem[T]
final case class Dead(id: AggregateRootId, version: AggregateRootVersion) extends Postnatalis[Nothing] with Transcendental[Nothing]
