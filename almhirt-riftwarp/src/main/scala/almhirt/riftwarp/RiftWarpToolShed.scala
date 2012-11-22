package almhirt.riftwarp

import almhirt.common._
import almhirt.almvalidation.kit._

trait RiftWarpToolShed extends HasDematerializers with HasRematerializationArrayFactories {
  def tryGetRematerializationArray[From <: RiftTypedDimension[_]](warpType: RiftDescriptor, from: From)(implicit hasRecomposers: HasRecomposers, m: Manifest[From]): AlmValidation[Option[RematerializationArray]] =
    tryGetArrayFactory[From](warpType).map(factory => factory.createRematerializationArray(from)).validationOut
}

object RiftWarpToolShed {
  def apply(hasDematerializers: HasDematerializers, hasRematerializationArrayFactories: HasRematerializationArrayFactories): RiftWarpToolShed = {
    new RiftWarpToolShed {
      def addDematerializer[D <: Dematerializer[_], To <: RiftTypedDimension[_]](dematerializer: Dematerializer[To], isChannelDefault: Boolean = false)(implicit m: Manifest[To]) = hasDematerializers.addDematerializer(dematerializer)
      def tryGetDematerializer[To <: RiftTypedDimension[_]](warpType: RiftDescriptor)(implicit m: Manifest[To]): Option[Dematerializer[To]] = hasDematerializers.tryGetDematerializer(warpType)
      def addArrayFactory[R <: RematerializationArrayFactory[_], From <: RiftTypedDimension[_]](arrayFactory: RematerializationArrayFactory[From], isChannelDefault: Boolean = false)(implicit m: Manifest[From]) = hasRematerializationArrayFactories.addArrayFactory(arrayFactory)
      def tryGetArrayFactory[From <: RiftTypedDimension[_]](warpType: RiftDescriptor)(implicit m: Manifest[From]): Option[RematerializationArrayFactory[From]] = hasRematerializationArrayFactories.tryGetArrayFactory(warpType)
    }
  }
  
  def unsafe(): RiftWarpToolShed = apply(new impl.UnsafeDematerializerRegistry, new impl.UnsafeRematerializationArrayFactoryRegistry)
}