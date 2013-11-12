package riftwarp.impl

import scalaz.syntax.validation._
import almhirt.common._
import riftwarp._

class WarpPackerRegistry extends WarpPackers {
  private val packers = new _root_.java.util.concurrent.ConcurrentHashMap[WarpDescriptor, (BlindWarpPacker, Boolean)](256)
  private val predicatedPackers = new _root_.java.util.concurrent.CopyOnWriteArrayList[(Any => Boolean, BlindWarpPacker)]
  override def get(descriptor: WarpDescriptor): AlmValidation[BlindWarpPacker] =
    packers.get(descriptor) match {
      case null => NoSuchElementProblem(s"""No WarpPacker found for "${descriptor.toString}"""").failure
      case (x, _) => x.success
    }

  override def getTyped[T](descriptor: WarpDescriptor): AlmValidation[WarpPacker[T]] =
    packers.get(descriptor) match {
      case null => NoSuchElementProblem(s"""No WarpPacker found for "${descriptor.toString}"""").failure
      case (x, true) => x.asInstanceOf[WarpPacker[T]].success
      case (x, false) => blindToTyped[T](x).success
  }
  
  def getByPredicate(what: Any): AlmValidation[BlindWarpPacker] = {
    import scala.collection.JavaConversions._
    predicatedPackers.iterator().find(x => x._1(what)) match {
      case Some(x) => x._2.success
      case None => NoSuchElementProblem(s"""There is no packer registered with a predicate that returns true for the given argument of type "${what.getClass.getName()}".""").failure
    }
  }
  
  override def add(blindPacker: BlindWarpPacker with RegisterableWarpPacker) {
    (blindPacker.warpDescriptor :: blindPacker.alternativeWarpDescriptors).foreach(packers.put(_, (blindPacker, false)))
  }

  override def addTyped[T](packer: WarpPacker[T] with RegisterableWarpPacker) {
    (packer.warpDescriptor :: packer.alternativeWarpDescriptors).foreach(packers.put(_, (packer, true)))
  }
  
  override def addPredicated(pred: Any => Boolean, packer: BlindWarpPacker) {
    predicatedPackers.add(0, (pred, packer))
  }

}