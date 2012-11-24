package almhirt.riftwarp.impl.rematerializers.simplema

import scalaz._, Scalaz._
import almhirt.almvalidation.kit._
import almhirt.common._
import almhirt.riftwarp._

abstract class CanRematerializeFromJsonStdList[M[_], A, C](implicit mM: Manifest[M[_]], mA: Manifest[A]) extends CanRematerializePrimitiveMABase[M, A, DimensionStdLibJsonList, RiftJson](mM, mA, manifest[DimensionStdLibJsonList], manifest[RiftJson]) {
  protected def rematerializeToListC(dim: DimensionStdLibJsonList): AlmValidation[List[C]] = {
    almhirt.almvalidation.funs.inTryCatch {
      dim.manifestation.map(_.asInstanceOf[C])
    }
  }
  
  def cToA(c: C): AlmValidation[A]
  def createMA(la: List[A]): M[A]
  
  def rematerialize(dim: DimensionStdLibJsonList): AlmValidation[M[A]] = 
    rematerializeToListC(dim).bind(lc =>
      lc.map(c => cToA(c).toAgg).sequence.map(la =>
        createMA(la)))
}