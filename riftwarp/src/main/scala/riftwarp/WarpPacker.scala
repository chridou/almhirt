package riftwarp

import almhirt.common._
import almhirt.almvalidation.kit._



trait BlindWarpPacker extends HasWarpDescriptor {
  def warpDescriptor: WarpDescriptor
  def packBlind(what: Any)(implicit lookup: WarpPackers): AlmValidation[WarpPackage]
}

trait WarpPacker[T] extends BlindWarpPacker  {
  final def apply(what: T)(implicit packers: WarpPackers): AlmValidation[WarpPackage] = pack(what)
  def pack(what: T)(implicit packers: WarpPackers): AlmValidation[WarpPackage]
  override final def packBlind(what: Any)(implicit packers: WarpPackers): AlmValidation[WarpPackage] = unsafe { pack(what.asInstanceOf[T]) }
}

trait SimpleWarpPacker[T] { self: WarpPacker[T] ⇒
  def packSimple(what: T): AlmValidation[WarpPackage] = apply(what)(WarpPackers.NoWarpPackers)
}

trait RegisterableWarpPacker {
  def alternativeWarpDescriptors: List[WarpDescriptor]
}




