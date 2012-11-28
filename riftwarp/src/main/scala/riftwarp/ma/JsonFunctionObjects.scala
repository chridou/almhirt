package riftwarp.ma

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
      if (funcObj.hasLinearCharacteristics) {
        val head = funcObj.head(ma)
        val tail = funcObj.tail(ma)
        funcObj.fold(tail)(head)((acc, elem) => DimensionCord((acc.manifestation :- ',') ++ elem.manifestation))
          .success
          .map(x => DimensionCord('[' -: x.manifestation :- ']'))
      } else {
        UnspecifiedProblem("Not yet supported").failure
      }
    }
  }
}

object JsonCordFolder extends JsonCordFolder

