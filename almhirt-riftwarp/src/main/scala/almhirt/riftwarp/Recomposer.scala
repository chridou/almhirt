package almhirt.riftwarp

import almhirt.common._

trait RawRecomposer extends HasTypeDescriptor {
  def recomposeRaw[TDimension <: RiftTypedDimension[_], TChannel <: RiftChannelDescriptor](from: RematerializationArray[TDimension, TChannel]): AlmValidation[AnyRef]
}

/** atoms -> instance */
trait Recomposer[T] extends RawRecomposer {
  def recompose[TDimension <: RiftTypedDimension[_], TChannel <: RiftChannelDescriptor](from: RematerializationArray[TDimension, TChannel]): AlmValidation[T]
  def recomposeRaw[TDimension <: RiftTypedDimension[_], TChannel <: RiftChannelDescriptor](from: RematerializationArray[TDimension, TChannel]) = recompose(from).map(_.asInstanceOf[AnyRef])
}

class EnrichedRawRecomposer[T](raw: RawRecomposer) extends Recomposer[T] {
  val typeDescriptor = raw.typeDescriptor
  def recompose[TDimension <: RiftTypedDimension[_], TChannel <: RiftChannelDescriptor](from: RematerializationArray[TDimension, TChannel]) = raw.recomposeRaw(from).map(_.asInstanceOf[T])
}