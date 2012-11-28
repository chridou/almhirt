package riftwarp.impl.dematerializers.simplema

import scalaz.Cord
import scalaz.Cord._
import scalaz.syntax.validation._
import almhirt.common._
import riftwarp._
import riftwarp.impl.dematerializers.ToJsonCordDematerializerFuns


abstract class CanDematerializePrimitiveMAToJsonValueByToString[M[_] <: Iterable[_], A]()(implicit mM: Manifest[M[_]], mA: Manifest[A]) extends CanDematerializePrimitiveMABase[M,A, DimensionCord](RiftJson()){
  def dematerialize(ma: M[A]): AlmValidation[DimensionCord] =
    DimensionCord('[' + ma.mkString(",") + ']').success
}

abstract class CanDematerializePrimitiveMAToJsonString[M[_] <: Iterable[_], A]()(implicit mM: Manifest[M[_]], mA: Manifest[A]) extends CanDematerializePrimitiveMABase[M,A, DimensionCord](RiftJson()){
  def dematerialize(ma: M[A]): AlmValidation[DimensionCord] = {
    val strings = ma.iterator.map((x:Any) => x.toString)
    DimensionCord('[' + strings.map((a:String) => """"%s"""".format(a)).mkString(",") + ']').success
  }
}

abstract class CanDematerializePrimitiveMAToJsonStringLaundered[M[_] <: Iterable[_], A]()(implicit mM: Manifest[M[_]], mA: Manifest[A]) extends CanDematerializePrimitiveMABase[M,A, DimensionCord](RiftJson()){
  def dematerialize(ma: M[A]): AlmValidation[DimensionCord] = {
    val strings = ma.iterator.map((x:Any) => ToJsonCordDematerializerFuns.launderString(x.toString))
    DimensionCord('[' + strings.map((a:Cord) => '\"' -: a :- '\"').mkString(",") + ']').success
  }
}

