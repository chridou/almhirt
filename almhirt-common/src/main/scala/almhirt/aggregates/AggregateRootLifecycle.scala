package almhirt.aggregates

/** Represents the states that an aggregate can advance through in its 'lifetime' */
sealed trait AggregateRootLifecycle[+T <: AggregateRoot] {
  def version: AggregateRootVersion
}

/** The aggregate root is either [[Vacat]] or [[Vivus]] */
sealed trait Antemortem[+T <: AggregateRoot] extends AggregateRootLifecycle[T]
/** The aggregate root is either [[Vivus]] or [[Mortuus]] */
sealed trait Postnatalis[+T <: AggregateRoot] extends AggregateRootLifecycle[T] {
  def id: AggregateRootId
}
/** The aggregate root is either [[Vacat]] or [[Mortuus]] */
sealed trait Transcendentia[+T <: AggregateRoot] extends AggregateRootLifecycle[T]

/** The aggregate root does not exist and hasn't died yet. */
case object Vacat extends Antemortem[Nothing] with Transcendentia[Nothing] {
  val version: AggregateRootVersion = AggregateRootVersion(0L)
}
/** The aggregate root exists. */
final case class Vivus[T <: AggregateRoot](ar: T) extends Postnatalis[T] with Antemortem[T] {
  def version: AggregateRootVersion = ar.version
  def id: AggregateRootId = ar.id
}
/** The aggregate root does not exists anymore. */
final case class Mortuus(id: AggregateRootId, version: AggregateRootVersion) extends Postnatalis[Nothing] with Transcendentia[Nothing]

object AggregateRootLifecycle {
  implicit class LifecycleOps[T <: AggregateRoot](self: AggregateRootLifecycle[T]) {
    import scalaz._, Scalaz._
    import almhirt.common._

    def toOption: Option[T] =
      self match {
        case Vivus(ar) => Some(ar)
        case _ => None
      }

    def toAggregateRoot: AlmValidation[T] =
      self match {
        case Vivus(ar) => ar.success
        case Vacat => NotFoundProblem("The aggregate root does not exist.").failure
        case Mortuus(id, v) => AggregateRootDeletedProblem(id).failure
      }
  }
}

object Postnatalis {
  def unapply(p: Postnatalis[_ <: AggregateRoot]): Option[(AggregateRootId, AggregateRootVersion)] =
    Some((p.id, p.version))
}

object VivusRef {
  def unapply(v: Vivus[_ <: AggregateRoot]): Option[(AggregateRootId, AggregateRootVersion)] =
    Some((v.id, v.version))
}