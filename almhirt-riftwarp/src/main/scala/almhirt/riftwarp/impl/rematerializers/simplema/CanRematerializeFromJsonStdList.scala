package almhirt.riftwarp.impl.rematerializers.simplema

import scalaz._, Scalaz._
import almhirt.almvalidation.kit._
import almhirt.common._
import almhirt.riftwarp._

abstract class CanRematerializeListAny[M[_], A, TDimension <: DimensionListAny, C](channel: RiftChannel)(implicit mM: Manifest[M[_]], mA: Manifest[A], mD: Manifest[TDimension]) extends CanRematerializePrimitiveMABase[M, A, TDimension](channel)(mM, mA,mD) {
  protected def rematerializeToListC(dim: DimensionListAny): AlmValidation[List[C]] = {
    almhirt.almvalidation.funs.inTryCatch {
      dim.manifestation.map(_.asInstanceOf[C])
    }
  }
  
  def cToA(c: C): AlmValidation[A]
  def createMA(la: List[A]): M[A]
  
  def rematerialize(dim: DimensionListAny): AlmValidation[M[A]] = 
    rematerializeToListC(dim).bind(lc =>
      lc.map(c => cToA(c).toAgg).sequence.map(la =>
        createMA(la)))
}