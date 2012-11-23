package almhirt.riftwarp.impl.dematerializers.simplema
//
//import scalaz.syntax.validation._
//import almhirt.common._
//import almhirt.riftwarp._
//import almhirt.riftwarp.impl.dematerializers.ToJsonCordDematerializerFuns
//
trait ToJsonCordPrimitiveMAs {
//  implicit val canDematerializeListStringToJsonCord = new CanDematerializeListStringToJsonCord
//  implicit val canDematerializeListIntToJsonCord = new CanDematerializeListIntToJsonCord
}
//
//
//class CanDematerializeListStringToJsonCord extends CanDematerializePrimitiveMA[List, String, DimensionCord, RiftJson]{
//  def dematerialize(ma:List[String]): AlmValidation[DimensionCord] = {
//    DimensionCord(ma.map(ToJsonCordDematerializerFuns.launderString(_)).mkString("[", ",", "]")).success
//  }
//}
//
//class CanDematerializeListIntToJsonCord extends CanDematerializePrimitiveMA[List, String, DimensionCord, RiftJson]{
//  def dematerialize(ma:List[String]): AlmValidation[DimensionCord] = {
//    DimensionCord(ma.map(_.toString()).mkString("[", ",", "]")).success
//  }
//}