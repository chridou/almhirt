package riftwarp.components

import language.higherKinds

import almhirt.common._
import almhirt.almvalidation.kit._
import riftwarp._
import riftwarp.ma._

trait RiftWarpToolShed extends HasDematerializers with HasRematerializerFactories with HasFunctionObjects {
  def tryGetRematerializer[TDimension <: RiftDimension](from: TDimension)(channel: RiftChannel)(implicit hasRecomposers: HasRecomposers, hasRematerializerFactories: HasRematerializerFactories, hasFunctionObjects: HasFunctionObjects, mD: Manifest[TDimension]): AlmValidation[Option[Rematerializer]] =
    tryGetRematerializerFactory[TDimension](channel).map(factory => factory.createRematerializer(from)).validationOut

  def tryGetDematerializer[TDimension <: RiftDimension](to: TDimension)(channel: RiftChannel)(implicit hasDecomposers: HasDecomposers, hasFunctionObjects: HasFunctionObjects, mD: Manifest[TDimension]): AlmValidation[Option[Dematerializer[TDimension]]] =
    tryGetDematerializerFactory[TDimension](channel).map(factory => factory.createDematerializer).validationOut
}

object RiftWarpToolShed {
  def apply(hasDematerializers: HasDematerializers, hasRematerializerFactories: HasRematerializerFactories, functionObjectRegistry: HasFunctionObjects): RiftWarpToolShed = {
    new RiftWarpToolShed {
      def addDematerializerFactory(factory: DematerializerFactory[_ <: RiftDimension], asChannelDefault: Boolean) { hasDematerializers.addDematerializerFactory(factory, asChannelDefault) }
      def tryGetDematerializerFactoryByType(tDimemsion: Class[_ <: RiftDimension])(channel: RiftChannel, toolGroup: Option[ToolGroup] = None) = hasDematerializers.tryGetDematerializerFactoryByType(tDimemsion)(channel, toolGroup)

      def addRematerializerFactory(arrayFactory: RematerializerFactory[_ <: RiftDimension], isChannelDefault: Boolean = false) { hasRematerializerFactories.addRematerializerFactory(arrayFactory, isChannelDefault) }
      def tryGetRematerializerFactoryByType(tDimension: Class[_ <: RiftDimension])(channel: RiftChannel, toolGroup: Option[ToolGroup] = None) = hasRematerializerFactories.tryGetRematerializerFactoryByType(tDimension)(channel, toolGroup)

      def addMAFunctions[M[_]](fo: RegisterableMAFunctions[M]) { functionObjectRegistry.addMAFunctions[M](fo) }
      def tryGetMAFunctions[M[_]](implicit mM: Manifest[M[_]]) = functionObjectRegistry.tryGetMAFunctions[M]
      def addChannelFolder[A, B](fo: RegisterableChannelFolder[A, B]) = { functionObjectRegistry.addChannelFolder[A, B](fo) }
      def tryGetChannelFolder[A, B](channel: RiftChannel)(implicit mA: Manifest[A], mB: Manifest[B]): Option[Folder[A, B]] = functionObjectRegistry.tryGetChannelFolder[A, B](channel)
      def addConvertsMAToNA[M[_], N[_]](converter: RegisterableConvertsMAToNA[M, N]) { functionObjectRegistry.addConvertsMAToNA[M, N](converter) }
      def tryGetConvertsMAToNA[M[_], N[_]](implicit mM: Manifest[M[_]], mN: Manifest[N[_]]) = functionObjectRegistry.tryGetConvertsMAToNA
    }
  }

  import riftwarp.impl._
  
  def unsafe(): RiftWarpToolShed = 
    apply(new UnsafeDematerializerRegistry, new UnsafeRematerializerFactoryRegistry, UnsafeFunctionObjectRegistry())
  
  def concurrent(): RiftWarpToolShed = 
    apply(new ConcurrentDematerializerRegistry, new ConcurrentRematerializerFactoryRegistry, ConcurrentFunctionObjectRegistry())
  
}