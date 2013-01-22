package riftwarp.automatic


import almhirt.common._
import riftwarp._
import riftwarp.macros.GeneratedDecomposerImpl;

trait GeneratedDecomposer[TToDecompose <: AnyRef] extends Decomposer[TToDecompose] {
  def addRiftDescriptor: Boolean
  def decompose[TDimension <: RiftDimension](what: TToDecompose)(into: Dematerializer[TDimension]): AlmValidation[Dematerializer[TDimension]] = { 
    def decomposeUncurried(what: TToDecompose, into: Dematerializer[TDimension]) = GeneratedDecomposerImpl.decomposeUncurried[TToDecompose, TDimension](what, into)
    if(addRiftDescriptor)
      into.addRiftDescriptor(this.riftDescriptor).flatMap(demat => decomposeUncurried(what, demat))
    else
      decomposeUncurried(what, into)
  }
}
