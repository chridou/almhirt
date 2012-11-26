package almhirt.riftwarp

import almhirt.common._
import almhirt.almvalidation.kit._
import ma._

trait RiftWarpToolShed extends HasDematerializers with HasRematerializationArrayFactories with HasFunctionObjects {
  def tryGetRematerializationArray[TDimension <: RiftDimension](from: TDimension)(channel: RiftChannel)(implicit hasRecomposers: HasRecomposers, hasRematerializationArrayFactories: HasRematerializationArrayFactories, mD: Manifest[TDimension]): AlmValidation[Option[RematerializationArray]] =
    tryGetArrayFactory[TDimension](channel).map(factory => factory.createRematerializationArray(from)).validationOut

  def tryGetDematerializer[TDimension <: RiftDimension](to: TDimension)(channel: RiftChannel)(implicit hasDecomposers: HasDecomposers, hasFunctionObjects: HasFunctionObjects, mD: Manifest[TDimension]): AlmValidation[Option[Dematerializer[TDimension]]] =
    tryGetDematerializerFactory[TDimension](channel).map(factory => factory.createDematerializer).validationOut
}

object RiftWarpToolShed {
  def apply(hasDematerializers: HasDematerializers, hasRematerializationArrayFactories: HasRematerializationArrayFactories, functionObjectRegistry: HasFunctionObjects): RiftWarpToolShed = {
    new RiftWarpToolShed {
      def addDematerializerFactory(factory: DematerializerFactory[_ <: RiftDimension], asChannelDefault: Boolean) { hasDematerializers.addDematerializerFactory(factory, asChannelDefault) }
      def tryGetDematerializerFactory[TDimension <: RiftDimension](channel: RiftChannel, toolGroup: Option[ToolGroup] = None)(implicit md: Manifest[TDimension]) = hasDematerializers.tryGetDematerializerFactory(channel)

      def addCanDematerializePrimitiveMA[M[_], A](cdsma: CanDematerializePrimitiveMA[M, A, _ <: RiftDimension]) { hasDematerializers.addCanDematerializePrimitiveMA(cdsma) }
      def tryGetCanDematerializePrimitiveMAByTypes(tM: Class[_], tA: Class[_], channel: RiftChannel, tDimension: Class[_ <: RiftDimension]) = hasDematerializers.tryGetCanDematerializePrimitiveMAByTypes(tM, tA, channel, tDimension)

      def addArrayFactory(arrayFactory: RematerializationArrayFactory[_ <: RiftDimension], isChannelDefault: Boolean = false) { hasRematerializationArrayFactories.addArrayFactory(arrayFactory, isChannelDefault) }
      def tryGetArrayFactory[TDimension <: RiftDimension](channel: RiftChannel, toolGroup: Option[ToolGroup] = None)(implicit mD: Manifest[TDimension]) = hasRematerializationArrayFactories.tryGetArrayFactory(channel)

      def addCanRematerializePrimitiveMA[M[_], A, TDimension <: RiftDimension](crsma: CanRematerializePrimitiveMA[M, A, TDimension]) { hasRematerializationArrayFactories.addCanRematerializePrimitiveMA(crsma) }
      def tryGetCanRematerializePrimitiveMAByTypes(tM: Class[_], tA: Class[_], tDimension: Class[_ <: RiftDimension], channel: RiftChannel) = hasRematerializationArrayFactories.tryGetCanRematerializePrimitiveMAByTypes(tM, tA, tDimension, channel)

      def addToMADimensionFunctor[M[_]](fo: RegisterableToMADimensionFunctor[M]) { functionObjectRegistry.addToMADimensionFunctor(fo) }
      def tryGetToMADimensionFunctor[M[_]](implicit mM: Manifest[M[_]]) = functionObjectRegistry.tryGetToMADimensionFunctor
      def addToMDimensionFold[M[_], TDimension <: RiftDimension](fo: RegisterableToMDimensionFold[M, TDimension]) { functionObjectRegistry.addToMDimensionFold(fo) }
      def tryGetToMDimensionFold[M[_], TDimension <: RiftDimension](channel: RiftChannel)(implicit mM: Manifest[M[_]], mD: Manifest[RiftDimension]): Option[RegisterableToMDimensionFold[M, TDimension]] = functionObjectRegistry.tryGetToMDimensionFold(channel)

    }
  }

  def unsafe(): RiftWarpToolShed = {
    val unsafeReRegistry = new impl.UnsafeRematerializationArrayFactoryRegistry
    apply(new impl.UnsafeDematerializerRegistry, unsafeReRegistry, impl.UnsafeFunctionObjectRegistry())
  }
}