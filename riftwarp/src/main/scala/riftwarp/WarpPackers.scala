package riftwarp

import almhirt.common._
import almhirt.almvalidation.kit._
import scala.reflect.ClassTag

trait WarpPackers extends Function1[RiftDescriptor, AlmValidation[BlindWarpPacker]] {
  def apply(descriptor: RiftDescriptor): AlmValidation[BlindWarpPacker]
  def get(descriptor: RiftDescriptor): AlmValidation[BlindWarpPacker] =
    apply(descriptor)
  def getByTag[T](implicit tag: ClassTag[T]): AlmValidation[BlindWarpPacker] =
    apply(RiftDescriptor(tag.runtimeClass))
  def getTyped[T](descriptor: RiftDescriptor): AlmValidation[WarpPacker[T]] =
    get(descriptor).map(blindPacker =>
      new WarpPacker[T] {
        override def checkIn(what: Any)(implicit packers: WarpPackers): AlmValidation[WarpPackage] =
          computeSafely {
            blindPacker.packBlind(what)
          }
      })
  def getByTagTyped[T](implicit tag: ClassTag[T]): AlmValidation[WarpPacker[T]] =
    getTyped[T](RiftDescriptor(tag.runtimeClass))
}