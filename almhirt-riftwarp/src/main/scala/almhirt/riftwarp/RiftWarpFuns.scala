package almhirt.riftwarp

import almhirt.common._
import almhirt.almvalidation.funs._

object RiftWarpFuns {
  def prepareForWarp[To <: RiftTypedDimension[_], T <: AnyRef](what: T)(decomposer: Decomposer[T], dematerializer: Dematerializer[To]): AlmValidation[To] = 
    decomposer.decompose(what)(dematerializer).bind(funnel =>
      almCast[Dematerializer[To]](funnel).bind(demat =>
        demat.dematerialize))
        
  def receiveFromWarp[From <: RiftTypedDimension[_], T <: AnyRef](warpStream: From)(factory: RematerializationArrayFactory[From], recomposer: Recomposer[T])(implicit hasRecomposers: HasRecomposers): AlmValidation[T] = {
    factory.createRematerializationArray(warpStream).bind(array => recomposer.recompose(array))
  }
}