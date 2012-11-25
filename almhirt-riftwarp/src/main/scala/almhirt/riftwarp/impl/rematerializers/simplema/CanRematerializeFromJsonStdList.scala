package almhirt.riftwarp.impl.rematerializers.simplema

import scalaz._, Scalaz._
import almhirt.almvalidation.kit._
import almhirt.common._
import almhirt.riftwarp._

abstract class CanRematerializeListAny[M[_], A, C](implicit mM: Manifest[M[_]], mA: Manifest[A]) extends CanRematerializePrimitiveMABase[M, A, DimensionListAny, RiftJson](mM, mA, manifest[DimensionListAny], manifest[RiftJson]) {
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