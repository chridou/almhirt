package riftwarp

import scalaz.syntax.validation._
import almhirt.common._
import almhirt.almvalidation.kit._

trait WarpUnpacker[+T] {
  final def apply(from: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[T] = unpack(from)
  def unpack(from: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[T]
}

trait RegisterableWarpUnpacker[+T] extends WarpUnpacker[T] {
  def warpDescriptor: WarpDescriptor
  def alternativeWarpDescriptors: List[WarpDescriptor]
}

trait DivertingWarpUnpacker[+T] { self: WarpUnpacker[T] =>
  def divert: WarpDescriptor => Option[WarpUnpacker[T]]

  def unpack(from: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[T] =
    std.funs.tryGetWarpDescriptor(from) match {
      case Some(wd) =>
        (divert >! wd).fold(
          fail => KeyNotFoundProblem(s"Could not find a Unpacker for ${wd.toString}").failure,
          unpacker => unpacker.unpack(from))
      case None =>
        UnspecifiedProblem(s""""${from.toString()} has no Warpdescriptor nor can one be derived!""").failure
    }
}
