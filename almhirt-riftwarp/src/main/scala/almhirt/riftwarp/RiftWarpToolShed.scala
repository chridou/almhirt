package almhirt.riftwarp

import almhirt.common._
import almhirt.almvalidation.kit._

trait RiftWarpToolShed extends HasDematerializers with HasRematerializationArrayFactories {
  def tryGetRematerializationArray[From <: AnyRef](forChannel: RiftChannel, from: From)(implicit hasRecomposers: HasRecomposers, m: Manifest[From]): AlmValidation[Option[RematerializationArray]] =
    tryGetArrayFactory[From](forChannel).map(factory => factory.createRematerializationArray(from)).validationOut
}

object RiftWarpToolShed {
  def apply(hasDematerializers: HasDematerializers, hasRematerializationArrayFactories: HasRematerializationArrayFactories): RiftWarpToolShed = {
    new RiftWarpToolShed {
      def addDematerializer[D <: Dematerializer[_], To <: AnyRef](dematerializer: Dematerializer[To])(implicit m: Manifest[To]) = hasDematerializers.addDematerializer(dematerializer)
      def tryGetDematerializer[To <: AnyRef](forChannel: RiftChannel)(implicit m: Manifest[To]): Option[Dematerializer[To]] = hasDematerializers.tryGetDematerializer(forChannel)
      def addArrayFactory[R <: RematerializationArrayFactory[_], From <: AnyRef](arrayFactory: RematerializationArrayFactory[From])(implicit m: Manifest[From]) = hasRematerializationArrayFactories.addArrayFactory(arrayFactory)
      def tryGetArrayFactory[From <: AnyRef](forChannel: RiftChannel)(implicit m: Manifest[From]): Option[RematerializationArrayFactory[From]] = hasRematerializationArrayFactories.tryGetArrayFactory(forChannel)
    }
  }
  
  def unsafe(): RiftWarpToolShed = apply(new impl.UnsafeDematerializerRegistry, new impl.UnsafeRematerializationArrayFactoryRegistry)
}