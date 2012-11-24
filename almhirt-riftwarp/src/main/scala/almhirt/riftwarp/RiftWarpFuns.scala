package almhirt.riftwarp

import almhirt.common._
import almhirt.almvalidation.funs._

object RiftWarpFuns {
  def prepareForWarp[TChannel <: RiftChannelDescriptor, To <: RiftTypedDimension[_], T <: AnyRef](what: T)(decomposer: Decomposer[T], dematerializer: Dematerializer[TChannel, To]): AlmValidation[To] =
    decomposer.decompose[TChannel, To](what)(dematerializer).bind(demat =>
      demat.dematerialize)

  def receiveFromWarp[TDimension <: RiftTypedDimension[_], TChannel <: RiftChannelDescriptor, T <: AnyRef](warpStream: TDimension)(factory: RematerializationArrayFactory[TDimension, TChannel], recomposer: Recomposer[T])(implicit hasRecomposers: HasRecomposers): AlmValidation[T] = {
    factory.createRematerializationArray(warpStream).bind(array => recomposer.recompose(array))
  }
}