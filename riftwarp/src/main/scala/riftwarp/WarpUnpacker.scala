package riftwarp

import almhirt.common._


trait WarpUnpacker[+T] {
  final def apply(from: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[T] = unpack(from)
  def unpack(from: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[T]
}

trait RegisterableWarpUnpacker[+T] extends WarpUnpacker[T] {
  def warpDescriptor: WarpDescriptor
  def alternativeWarpDescriptors: List[WarpDescriptor]
}
