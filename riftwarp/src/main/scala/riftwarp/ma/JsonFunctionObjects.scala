package riftwarp.ma

import language.higherKinds

import scalaz.Cord
import scalaz.Cord._
import scalaz.syntax.validation._
import almhirt.common._
import riftwarp._

trait JsonCordFolder extends RegisterableChannelFolder[DimensionCord, DimensionCord] {
  val channel = RiftJson()
  val tA = classOf[DimensionCord]
  val tB = classOf[DimensionCord]
  def fold[M[_]](ma: M[DimensionCord])(funcObj: MAFunctions[M]): AlmValidation[DimensionCord] = {
    if (funcObj.isEmpty(ma)) {
      DimensionCord("[]").success
    } else {
      funcObj match {
        // Since it is automatically looked up, it should be the right thing...
        case fo: LinearMAFunctions[M] =>
          val head = fo.head(ma)
          val tail = fo.tail(ma)
          funcObj.fold(tail)(head)((acc, elem) => DimensionCord((acc.manifestation :- ',') ++ elem.manifestation))
            .success
            .map(x => DimensionCord('[' -: x.manifestation :- ']'))
        case _ =>
          UnspecifiedProblem("Not yet supported").failure
      }
    }
  }
}

object JsonCordFolder extends JsonCordFolder

