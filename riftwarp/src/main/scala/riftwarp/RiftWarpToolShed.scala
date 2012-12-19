package riftwarp

import almhirt.common._
import almhirt.almvalidation.kit._
import ma._

trait RiftWarpToolShed extends HasDematerializers with HasRematerializationArrayFactories with HasFunctionObjects {
  def tryGetRematerializationArray[TDimension <: RiftDimension](from: TDimension)(channel: RiftChannel)(implicit hasRecomposers: HasRecomposers, hasRematerializationArrayFactories: HasRematerializationArrayFactories, hasFunctionObjects: HasFunctionObjects, mD: Manifest[TDimension]): AlmValidation[Option[RematerializationArray]] =
    tryGetArrayFactory[TDimension](channel).map(factory => factory.createRematerializationArray(from)).validationOut

  def tryGetDematerializer[TDimension <: RiftDimension](to: TDimension)(channel: RiftChannel)(implicit hasDecomposers: HasDecomposers, hasFunctionObjects: HasFunctionObjects, mD: Manifest[TDimension]): AlmValidation[Option[Dematerializer[TDimension]]] =
    tryGetDematerializerFactory[TDimension](channel).map(factory => factory.createDematerializer).validationOut
}

object RiftWarpToolShed {
  def apply(hasDematerializers: HasDematerializers, hasRematerializationArrayFactories: HasRematerializationArrayFactories, functionObjectRegistry: HasFunctionObjects): RiftWarpToolShed = {
    new RiftWarpToolShed {
      def addDematerializerFactory(factory: DematerializerFactory[_ <: RiftDimension], asChannelDefault: Boolean) { hasDematerializers.addDematerializerFactory(factory, asChannelDefault) }
      def tryGetDematerializerFactoryByType(tDimemsion: Class[_ <: RiftDimension])(channel: RiftChannel, toolGroup: Option[ToolGroup] = None) = hasDematerializers.tryGetDematerializerFactoryByType(tDimemsion)(channel, toolGroup)

      def addArrayFactory(arrayFactory: RematerializationArrayFactory[_ <: RiftDimension], isChannelDefault: Boolean = false) { hasRematerializationArrayFactories.addArrayFactory(arrayFactory, isChannelDefault) }
      def tryGetArrayFactoryByType(tDimension: Class[_ <: RiftDimension])(channel: RiftChannel, toolGroup: Option[ToolGroup] = None) = hasRematerializationArrayFactories.tryGetArrayFactoryByType(tDimension)(channel, toolGroup)

      def addMAFunctions[M[_]](fo: RegisterableMAFunctions[M]) { functionObjectRegistry.addMAFunctions[M](fo) }
      def tryGetMAFunctions[M[_]](implicit mM: Manifest[M[_]]) = functionObjectRegistry.tryGetMAFunctions[M]
      def addChannelFolder[A, B](fo: RegisterableChannelFolder[A, B]) = { functionObjectRegistry.addChannelFolder[A, B](fo) }
      def tryGetChannelFolder[A, B](channel: RiftChannel)(implicit mA: Manifest[A], mB: Manifest[B]): Option[Folder[A, B]] = functionObjectRegistry.tryGetChannelFolder[A, B](channel)
      def addConvertsMAToNA[M[_], N[_]](converter: RegisterableConvertsMAToNA[M, N]) { functionObjectRegistry.addConvertsMAToNA[M, N](converter) }
      def tryGetConvertsMAToNA[M[_], N[_]](implicit mM: Manifest[M[_]], mN: Manifest[N[_]]) = functionObjectRegistry.tryGetConvertsMAToNA
    }
  }

  def unsafe(): RiftWarpToolShed = 
    apply(new impl.UnsafeDematerializerRegistry, new impl.UnsafeRematerializationArrayFactoryRegistry, impl.UnsafeFunctionObjectRegistry())
  
  def concurrent(): RiftWarpToolShed = 
    apply(new impl.ConcurrentDematerializerRegistry, new impl.ConcurrentRematerializationArrayFactoryRegistry, impl.ConcurrentFunctionObjectRegistry())
  
}