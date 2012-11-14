package almhirt.riftwarp

import almhirt.common._
import almhirt.almvalidation.funs._

object RiftWarpFuns {
  def prepareForWarp[To <: AnyRef, T <: AnyRef](what: T)(decomposer: Decomposer[T], dematerializer: Dematerializer[To]): AlmValidation[To] = 
    decomposer.decompose(what)(dematerializer).bind(funnel =>
      almCast[Dematerializer[To]](funnel).bind(demat =>
        demat.dematerialize))
}