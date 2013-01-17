package riftwarp.automatic

import scala.language.experimental.macros

import scala.reflect.macros.Context
import almhirt.common._
import riftwarp._

trait GeneratedDecomposer[TToDecompose <: AnyRef] extends Decomposer[TToDecompose] {
  def decompose[TDimension <: RiftDimension](what: TToDecompose)(into: Dematerializer[TDimension]): AlmValidation[Dematerializer[TDimension]] = uncurried[TDimension](what, into)
  private def uncurried[TDimension <: RiftDimension](what: TToDecompose, into: Dematerializer[TDimension]): AlmValidation[Dematerializer[TDimension]] = 
    macro GeneratedDecomposerImpl.decompose[TToDecompose, TDimension]
}

object GeneratedDecomposerImpl {
  def decompose[TToDecompose <: AnyRef: c.TypeTag, TDimension <: RiftDimension: c.TypeTag](c: Context)(what: c.Expr[TToDecompose], into: c.Expr[TDimension]): c.Expr[(TToDecompose, Dematerializer[TDimension]) => AlmValidation[Dematerializer[TDimension]]] = ???
}