package riftwarp

import almhirt.common._
import riftwarp.components._

trait RawRecomposer extends HasTypeDescriptor {
  def recomposeRaw(from: Rematerializer): AlmValidation[AnyRef]
}

/** atoms -> instance */
trait Recomposer[T] extends RawRecomposer {
  def recompose(from: Rematerializer): AlmValidation[T]
  def recomposeRaw(from: Rematerializer) = recompose(from).map(_.asInstanceOf[AnyRef])
}

class EnrichedRawRecomposer[T](raw: RawRecomposer) extends Recomposer[T] {
  val typeDescriptor = raw.typeDescriptor
  def recompose(from: Rematerializer) = raw.recomposeRaw(from).map(_.asInstanceOf[T])
}