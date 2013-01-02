package riftwarp

import almhirt.common._
import almhirt.almvalidation.funs._
import riftwarp.components._

object RiftWarpFuns {
  def prepareForWarp[TDimension <: RiftDimension, T <: AnyRef](channel: RiftChannel)(what: T)(decomposer: Decomposer[T], dematerializer: Dematerializer[TDimension]): AlmValidation[TDimension] =
    decomposer.decompose(what)(dematerializer).map(demat =>
      demat.dematerialize)

  def receiveFromWarp[TDimension <: RiftDimension, T <: AnyRef](channel: RiftChannel)(warpStream: TDimension)(factory: RematerializerFactory[TDimension], recomposer: Recomposer[T])(implicit hasRecomposers: HasRecomposers, hasFunctionObject: ma.HasFunctionObjects): AlmValidation[T] = {
    factory.createRematerializer(warpStream).flatMap(array => recomposer.recompose(array))
  }
}