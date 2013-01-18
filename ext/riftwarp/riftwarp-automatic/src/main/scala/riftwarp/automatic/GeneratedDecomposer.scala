package riftwarp.automatic


import almhirt.common._
import riftwarp._

trait GeneratedDecomposer[TToDecompose <: AnyRef] extends Decomposer[TToDecompose] {
  def addTypeDescriptor: Boolean
  def decompose[TDimension <: RiftDimension](what: TToDecompose)(into: Dematerializer[TDimension]): AlmValidation[Dematerializer[TDimension]] = { 
    def decomposeUncurried(what: TToDecompose, into: Dematerializer[TDimension]) = GeneratedDecomposerImpl.decomposeUncurried[TToDecompose, TDimension](what, into)
    if(addTypeDescriptor)
      into.addTypeDescriptor(this.typeDescriptor).flatMap(demat => decomposeUncurried(what, demat))
    else
      decomposeUncurried(what, into)
  }
}
