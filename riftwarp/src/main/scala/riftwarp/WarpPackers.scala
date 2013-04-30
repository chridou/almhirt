package riftwarp

import scala.reflect.ClassTag
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.almvalidation.kit._

trait WarpPackers extends Function1[RiftDescriptor, AlmValidation[BlindWarpPacker]] {
  final def apply(descriptor: RiftDescriptor): AlmValidation[BlindWarpPacker] = get(descriptor)
  def get(descriptor: RiftDescriptor): AlmValidation[BlindWarpPacker]
  def getByTag[T](implicit tag: ClassTag[T]): AlmValidation[BlindWarpPacker] =
    apply(RiftDescriptor(tag.runtimeClass))
  def getTyped[T](descriptor: RiftDescriptor): AlmValidation[WarpPacker[T]] =
    get(descriptor).map(blindPacker =>
      new WarpPacker[T] {
        override def pack(what: T)(implicit packers: WarpPackers): AlmValidation[WarpPackage] =
          computeSafely {
            blindPacker.packBlind(what)
          }
      })
  def getByTagTyped[T](implicit tag: ClassTag[T]): AlmValidation[WarpPacker[T]] =
    getTyped[T](RiftDescriptor(tag.runtimeClass))
}

object WarpPackers {
  val NoWarpPackers: WarpPackers = new WarpPackers { override def get(descriptor: RiftDescriptor) = UnspecifiedSystemProblem("NoWarpPackers has no packers").failure}
}