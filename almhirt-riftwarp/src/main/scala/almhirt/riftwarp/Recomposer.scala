package almhirt.riftwarp

import almhirt.common._

trait RawRecomposer extends HasTypeDescriptor {
  def recomposeRaw(from: RematerializationArray): AlmValidation[AnyRef]
}

/** atoms -> instance */
trait Recomposer[T] extends RawRecomposer {
  def recompose(from: RematerializationArray): AlmValidation[T]
  def recomposeRaw(from: RematerializationArray) = recomposeRaw(from)
}

class EnrichedRawRecomposer[T](raw: RawRecomposer) extends Recomposer[T] {
  val typeDescriptor = raw.typeDescriptor
  def recompose(from: RematerializationArray) = raw.recomposeRaw(from).map(_.asInstanceOf[T])
}