package riftwarp

import scala.reflect.ClassTag
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.almvalidation.kit._
import riftwarp.components._

trait RawRecomposer extends HasAlternativeRiftDescriptors {
  def recomposeRaw(from: Rematerializer): AlmValidation[AnyRef]
}

/** atoms -> instance */
trait Recomposer[+T <: AnyRef] extends RawRecomposer {
  def recompose(from: Rematerializer): AlmValidation[T]
  def recomposeAsync(from: Rematerializer)(implicit hasExecContext: HasExecutionContext): AlmFuture[T] =
    AlmFuture { recompose(from) }
  def recomposeRaw(from: Rematerializer) = recompose(from).map(_.asInstanceOf[AnyRef])
}

class EnrichedRawRecomposer[+T <: AnyRef](raw: RawRecomposer)(implicit tag: ClassTag[T]) extends Recomposer[T] {
  val riftDescriptor = raw.riftDescriptor
  val alternativeRiftDescriptors = raw.alternativeRiftDescriptors
  def recompose(from: Rematerializer) = raw.recomposeRaw(from).flatMap(almhirt.almvalidation.funs.almCast[T](_))
}

abstract class DivertingRecomposer[+T <: AnyRef] extends Recomposer[T] {
  def divert: RiftDescriptor => Option[Recomposer[T]]
  override def recompose(from: Rematerializer): AlmValidation[T] =
    from.getRiftDescriptor.flatMap(rd =>
      (divert >? rd).fold(
        fail => KeyNotFoundProblem(s"Recomposer for ${this.riftDescriptor.toString} could not find a Recomposer for ${rd.toString}").failure,
        recomposer => recomposer.recompose(from)))
  override def recomposeAsync(from: Rematerializer)(implicit hasExecContext: HasExecutionContext): AlmFuture[T] =
    from.getRiftDescriptor.fold(
      fail => AlmFuture.failed(fail),
      rd =>
        (divert >? rd).fold(
          fail => AlmFuture.failed(KeyNotFoundProblem(s"Recomposer for ${this.riftDescriptor.toString} could not find a Recomposer for ${rd.toString}")),
          recomposer => recomposer.recomposeAsync(from)))
}