package riftwarp.impl

import scala.reflect.ClassTag
import scalaz.syntax.validation._
import almhirt.common._
import riftwarp._

class WarpUnpackerRegistry extends WarpUnpackers {
  private val unpackers = new _root_.java.util.concurrent.ConcurrentHashMap[WarpDescriptor, (WarpUnpacker[Any], Boolean)](256)

  override def get(descriptor: WarpDescriptor): AlmValidation[WarpUnpacker[Any]] =
    unpackers.get(descriptor) match {
      case null => KeyNotFoundProblem(s"""No WarpUnpacker found for "${descriptor.toString}"""").failure
      case (x, _) => x.success
    }

  override def getTyped[T](descriptor: WarpDescriptor)(implicit tag: ClassTag[T]): AlmValidation[WarpUnpacker[T]] =
    unpackers.get(descriptor) match {
      case null => KeyNotFoundProblem(s"""No WarpPacker found for "${descriptor.toString}"""").failure
      case (x, true) => x.asInstanceOf[WarpUnpacker[T]].success
      case (x, false) => untypedToTyped[T](x).success
    }

  override def add(unpacker: RegisterableWarpUnpacker[Any]) {
    (unpacker.warpDescriptor :: unpacker.alternativeWarpDescriptors).foreach(unpackers.put(_, (unpacker, false)))
  }

  override def addTyped[T](unpacker: RegisterableWarpUnpacker[T]) {
    (unpacker.warpDescriptor :: unpacker.alternativeWarpDescriptors).foreach(unpackers.put(_, (unpacker, true)))
  }
}