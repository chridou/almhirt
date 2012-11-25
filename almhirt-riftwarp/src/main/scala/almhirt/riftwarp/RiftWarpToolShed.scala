package almhirt.riftwarp

import almhirt.common._
import almhirt.almvalidation.kit._

trait RiftWarpToolShed extends HasDematerializers with HasRematerializationArrayFactories with HasRematerializersForHKTs {
  def tryGetRematerializationArray[TDimension <: RiftTypedDimension[_], TChannel <: RiftChannelDescriptor](from: TDimension)(implicit hasRecomposers: HasRecomposers, hasRematerializersForHKTs: HasRematerializersForHKTs, mD: Manifest[TDimension], mC: Manifest[TChannel]): AlmValidation[Option[RematerializationArray]] =
    tryGetArrayFactory[TDimension, TChannel].map(factory => factory.createRematerializationArray(from)).validationOut
}

object RiftWarpToolShed {
  def apply(hasDematerializers: HasDematerializers, hasRematerializationArrayFactories: HasRematerializationArrayFactories, hasRematerializersForHKTs: HasRematerializersForHKTs): RiftWarpToolShed = {
    new RiftWarpToolShed {
      def addDematerializer[D <: Dematerializer[_, _], TChannel <: RiftChannelDescriptor, To <: RiftTypedDimension[_]](dematerializer: Dematerializer[TChannel, To], isChannelDefault: Boolean = false)(implicit m: Manifest[To]) = hasDematerializers.addDematerializer(dematerializer)
      def tryGetDematerializerByDescriptor[To <: RiftTypedDimension[_]](warpType: RiftDescriptor)(implicit m: Manifest[To]): Option[Dematerializer[_, To]] = hasDematerializers.tryGetDematerializerByDescriptor(warpType)
      def tryGetDematerializer[TChannel <: RiftChannelDescriptor, To <: RiftTypedDimension[_]](implicit md: Manifest[To], mc: Manifest[TChannel]): Option[Dematerializer[TChannel, To]] = hasDematerializers.tryGetDematerializer[TChannel, To]

      def addCanDematerializePrimitiveMA[M[_], A, TChannel <: RiftChannelDescriptor, TDimension <: RiftDimension](cdsma: CanDematerializePrimitiveMA[M, A, TChannel, TDimension]) = hasDematerializers.addCanDematerializePrimitiveMA(cdsma)
      def tryGetCanDematerializePrimitiveMAByTypes(tM: Class[_], tA: Class[_], tChannel: Class[_ <: RiftChannelDescriptor], tDimension: Class[_ <: RiftDimension]) = hasDematerializers.tryGetCanDematerializePrimitiveMAByTypes(tM, tA, tChannel, tDimension)

      def addArrayFactory[R <: RematerializationArrayFactory[TDimension, TChannel], TDimension <: RiftTypedDimension[_], TChannel <: RiftChannelDescriptor](arrayFactory: RematerializationArrayFactory[TDimension, TChannel], isChannelDefault: Boolean = false)(implicit mD: Manifest[TDimension], mC: Manifest[TChannel]) = hasRematerializationArrayFactories.addArrayFactory(arrayFactory)
      def tryGetArrayFactory[TDimension <: RiftTypedDimension[_], TChannel <: RiftChannelDescriptor](implicit mD: Manifest[TDimension], mC: Manifest[TChannel]): Option[RematerializationArrayFactory[TDimension, TChannel]] = hasRematerializationArrayFactories.tryGetArrayFactory

      def addCanRematerializePrimitiveMA[M[_], A, TDimension <: RiftTypedDimension[_], TChannel <: RiftChannelDescriptor](crsma: CanRematerializePrimitiveMA[M, A, TDimension, TChannel]) { hasRematerializersForHKTs.addCanRematerializePrimitiveMA(crsma) }
      def tryGetCanRematerializePrimitiveMAByTypes(tM: Class[_], tA: Class[_], tDimension: Class[_ <: RiftTypedDimension[_]], tChannel: Class[_ <: RiftChannelDescriptor]) = hasRematerializersForHKTs.tryGetCanRematerializePrimitiveMAByTypes(tM, tA, tDimension, tChannel)

    }
  }

  def unsafe(): RiftWarpToolShed = {
    val unsafeReRegistry = new impl.UnsafeRematerializationArrayFactoryRegistry
    apply(new impl.UnsafeDematerializerRegistry, unsafeReRegistry, unsafeReRegistry)
  }
}