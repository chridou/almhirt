package almhirt.riftwarp

import almhirt.common._
import almhirt.almvalidation.funs._

object RiftWarpFuns {
  def prepareForWarp[To <: RiftTypedDimension[_], TChannel <: RiftChannelDescriptor, T <: AnyRef](what: T)(decomposer: Decomposer[T], dematerializer: Dematerializer[To, TChannel]): AlmValidation[To] =
    decomposer.decompose[To, TChannel](what)(dematerializer).bind(demat =>
      demat.dematerialize)

  def receiveFromWarp[From <: RiftTypedDimension[_], T <: AnyRef](warpStream: From)(factory: RematerializationArrayFactory[From], recomposer: Recomposer[T])(implicit hasRecomposers: HasRecomposers): AlmValidation[T] = {
    factory.createRematerializationArray(warpStream).bind(array => recomposer.recompose(array))
  }
}