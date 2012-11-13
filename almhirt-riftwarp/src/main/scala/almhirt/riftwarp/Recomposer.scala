package almhirt.riftwarp

import almhirt.common._

trait RawRecomposer{
  def recomposeRaw(from: RematerializationArray): AlmValidation[AnyRef]
}

/** atoms -> instance */
trait Recomposer[T] extends RawRecomposer {
  def recompose(from: RematerializationArray): AlmValidation[T] = recomposeRaw(from).map(_.asInstanceOf[T])
}

class EnrichedRawRecomposer[T](raw: RawRecomposer) extends Recomposer[T] {
  def recomposeRaw(from: RematerializationArray) = raw.recomposeRaw(from)
}