package riftwarp.components

import language.higherKinds
import scala.reflect.ClassTag
import almhirt.common._
import almhirt.almvalidation.kit._
import riftwarp._
import riftwarp.ma._

trait RiftWarpToolShed extends HasWarpSequencers with HasRematerializerFactories with HasFunctionObjects {
  def tryGetRematerializer[TDimension <: RiftDimension](from: TDimension)(channel: RiftChannel)(implicit hasRecomposers: HasRecomposers, hasRematerializerFactories: HasRematerializerFactories, hasFunctionObjects: HasFunctionObjects, mD: ClassTag[TDimension]): AlmValidation[Option[Rematerializer]] =
    tryGetRematerializerFactory[TDimension](channel).map(factory => factory.createRematerializer(from)).validationOut

  def tryGetWarpSequencer[TDimension <: RiftDimension](to: TDimension)(channel: RiftChannel)(implicit hasDecomposers: HasDecomposers, hasFunctionObjects: HasFunctionObjects, mD: ClassTag[TDimension]): AlmValidation[Option[WarpSequencer[TDimension]]] =
    tryGetWarpSequencerFactory[TDimension](channel).map(factory => factory.createWarpSequencer).validationOut
}

object RiftWarpToolShed {
  def apply(hasWarpSequencers: HasWarpSequencers, hasRematerializerFactories: HasRematerializerFactories, functionObjectRegistry: HasFunctionObjects): RiftWarpToolShed = {
    new RiftWarpToolShed {
      def addWarpSequencerFactory(factory: WarpSequencerFactory[_ <: RiftDimension], asChannelDefault: Boolean) { hasWarpSequencers.addWarpSequencerFactory(factory, asChannelDefault) }
      def tryGetWarpSequencerFactoryByType(tDimemsion: Class[_ <: RiftDimension])(channel: RiftChannel, toolGroup: Option[ToolGroup] = None) = hasWarpSequencers.tryGetWarpSequencerFactoryByType(tDimemsion)(channel, toolGroup)

      def addRematerializerFactory(arrayFactory: RematerializerFactory[_ <: RiftDimension], isChannelDefault: Boolean = false) { hasRematerializerFactories.addRematerializerFactory(arrayFactory, isChannelDefault) }
      def tryGetRematerializerFactoryByType(tDimension: Class[_ <: RiftDimension])(channel: RiftChannel, toolGroup: Option[ToolGroup] = None) = hasRematerializerFactories.tryGetRematerializerFactoryByType(tDimension)(channel, toolGroup)

      def addMAFunctions[M[_]](fo: RegisterableMAFunctions[M]) { functionObjectRegistry.addMAFunctions[M](fo) }
      def tryGetMAFunctions[M[_]](implicit mM: ClassTag[M[_]]) = functionObjectRegistry.tryGetMAFunctions[M]
      def addChannelFolder[A, B](fo: RegisterableChannelFolder[A, B]) = { functionObjectRegistry.addChannelFolder[A, B](fo) }
      def tryGetChannelFolder[A, B](channel: RiftChannel)(implicit mA: ClassTag[A], mB: ClassTag[B]): Option[Folder[A, B]] = functionObjectRegistry.tryGetChannelFolder[A, B](channel)
      def addConvertsMAToNA[M[_], N[_]](converter: RegisterableConvertsMAToNA[M, N]) { functionObjectRegistry.addConvertsMAToNA[M, N](converter) }
      def tryGetConvertsMAToNA[M[_], N[_]](implicit mM: ClassTag[M[_]], mN: ClassTag[N[_]]) = functionObjectRegistry.tryGetConvertsMAToNA
    }
  }

  import riftwarp.impl._
  
  def unsafe(): RiftWarpToolShed = 
    apply(new UnsafeWarpSequencerRegistry, new UnsafeRematerializerFactoryRegistry, UnsafeFunctionObjectRegistry())
  
  def concurrent(): RiftWarpToolShed = 
    apply(new ConcurrentWarpSequencerRegistry, new ConcurrentRematerializerFactoryRegistry, ConcurrentFunctionObjectRegistry())
  
}