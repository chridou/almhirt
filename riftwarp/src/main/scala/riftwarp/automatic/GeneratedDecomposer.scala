package riftwarp.automatic

import scala.language.experimental.macros

import scala.reflect.macros.Context
import almhirt.common._
import riftwarp._

trait GeneratedDecomposer[TToDecompose <: AnyRef] extends Decomposer[TToDecompose] {
  def decompose[TDimension <: RiftDimension](what: TToDecompose)(into: Dematerializer[TDimension]): AlmValidation[Dematerializer[TDimension]] = decomposeUncurried[TToDecompose, TDimension](what, into)
  private def decomposeUncurried[TTWhat <: AnyRef, TTDimension <: RiftDimension](what: TTWhat, into: Dematerializer[TTDimension]): AlmValidation[Dematerializer[TTDimension]] = 
    macro GeneratedDecomposerImpl.decomposeUncurried[TTWhat, TTDimension]
}

object GeneratedDecomposerImpl {
  def decomposeUncurried[TTWhat <: AnyRef: c.WeakTypeTag, TTDimension <: RiftDimension: c.WeakTypeTag](c: Context)(what: c.Expr[TTWhat], into: c.Expr[Dematerializer[TTDimension]]): c.Expr[AlmValidation[Dematerializer[TTDimension]]] = {
    ???
    }
}