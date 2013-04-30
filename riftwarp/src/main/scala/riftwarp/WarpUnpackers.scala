package riftwarp

import scala.reflect.ClassTag
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.almvalidation.kit._

trait WarpUnpackers {
  final def apply(descriptor: RiftDescriptor): AlmValidation[WarpUnpacker[Any]] = get(descriptor)
  def get(descriptor: RiftDescriptor): AlmValidation[WarpUnpacker[Any]]
  def getTyped[T](descriptor: RiftDescriptor)(implicit tag: ClassTag[T]): AlmValidation[WarpUnpacker[T]] =
    apply(descriptor).map(unpacker =>
      new WarpUnpacker[T] {
        def unpack(from: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[T] =
          computeSafely {
            unpacker.unpack(from).flatMap(_.castTo[T])
          }
      })
    
  def getByTag[T](implicit tag: ClassTag[T]): AlmValidation[WarpUnpacker[Any]] =
    apply(RiftDescriptor(tag.runtimeClass))
    
  def getByTagTyped[T](implicit tag: ClassTag[T]): AlmValidation[WarpUnpacker[T]] =
    apply(RiftDescriptor(tag.runtimeClass)).map(unpacker =>
      new WarpUnpacker[T] {
        def unpack(from: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[T] =
          computeSafely {
            unpacker.unpack(from).flatMap(_.castTo[T])
          }
      })
}

object WarpUnpackers {
  val NoWarpUnpackers = new WarpUnpackers { override def get(descriptor: RiftDescriptor) = UnspecifiedSystemProblem("NoWarpUnpackers has no unpackers").failure }
}