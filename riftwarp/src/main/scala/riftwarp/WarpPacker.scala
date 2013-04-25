package riftwarp

import almhirt.common._

trait BlindWarpPacker {
  def packBlind(what: Any)(implicit lookup: WarpPackers): AlmValidation[WarpPackage]
}

trait WarpPacker[T] extends BlindWarpPacker {
  def pack(what: T)(implicit packers: WarpPackers): AlmValidation[WarpPackage]
  override final def packBlind(what: Any)(implicit packers: WarpPackers): AlmValidation[WarpPackage] = pack(what.asInstanceOf[T])
}

