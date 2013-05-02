package riftwarp.impl

import scalaz.syntax.validation._
import almhirt.common._
import riftwarp._

class WarpPackerRegistry extends WarpPackers {
  private val packers = new _root_.java.util.concurrent.ConcurrentHashMap[WarpDescriptor, (BlindWarpPacker, Boolean)](256)
  override def get(descriptor: WarpDescriptor): AlmValidation[BlindWarpPacker] =
    packers.get(descriptor) match {
      case null => KeyNotFoundProblem(s"""No WarpPacker found for "${descriptor.toString}"""").failure
      case (x, _) => x.success
    }

  override def getTyped[T](descriptor: WarpDescriptor): AlmValidation[WarpPacker[T]] =
    packers.get(descriptor) match {
      case null => KeyNotFoundProblem(s"""No WarpPacker found for "${descriptor.toString}"""").failure
      case (x, true) => x.asInstanceOf[WarpPacker[T]].success
      case (x, false) => blindToTyped[T](x).success
  }
  
  override def add(blindPacker: BlindWarpPacker with RegisterableWarpPacker) {
    (blindPacker.warpDescriptor :: blindPacker.alternativeWarpDescriptors).foreach(packers.put(_, (blindPacker, false)))
  }

  override def addTyped[T](packer: WarpPacker[T] with RegisterableWarpPacker) {
    (packer.warpDescriptor :: packer.alternativeWarpDescriptors).foreach(packers.put(_, (packer, true)))
  }
}