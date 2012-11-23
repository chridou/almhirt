package almhirt.riftwarp.impl.dematerializers.simplema

import scalaz.Cord
import scalaz.Cord._
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.riftwarp._
import almhirt.riftwarp.impl.dematerializers.ToJsonCordDematerializerFuns

//class CanDematerializeListStringToJsonCord extends CanDematerializePrimitiveMA[List, String, DimensionCord, RiftJson]{
//  def dematerialize(ma:List[String]): AlmValidation[DimensionCord] = {
//    DimensionCord(ma.map(ToJsonCordDematerializerFuns.launderString(_)).mkString("[", ",", "]")).success
//  }
//}
//

abstract class JsonCordPrimitiveDematerializerToListUsingToStringOnItem[A]()(implicit mA: Manifest[A]) extends CanDematerializePrimitiveListToCord[A, RiftJson](mA, manifest[RiftJson]){
  def dematerialize(ma: List[A]): AlmValidation[DimensionCord] =
    DimensionCord('[' + ma.mkString(",") + ']').success
}
