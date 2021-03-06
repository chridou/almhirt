package riftwarp

import scalaz.syntax.validation._
import almhirt.common._
import almhirt.almvalidation.kit._

trait WarpUnpacker[+T] extends HasWarpDescriptor {
  def warpDescriptor: WarpDescriptor
  final def apply(from: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[T] = unpack(from)
  def unpack(from: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[T]
}

trait RegisterableWarpUnpacker[+T] extends WarpUnpacker[T] {
  def alternativeWarpDescriptors: List[WarpDescriptor]
  def allDescriptors: List[WarpDescriptor] = warpDescriptor :: alternativeWarpDescriptors
}

trait DivertingWarpUnpacker[+T] { self: WarpUnpacker[T] ⇒
  def divert: Map[WarpDescriptor, WarpUnpacker[T]]

  def unpack(from: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[T] =
    std.funs.tryGetWarpDescriptor(from) match {
      case Some(wd) ⇒
        (divert.get(wd)) match {
          case Some(unpacker) ⇒
            unpacker.unpack(from)
          case None ⇒
            val fromStr = from.toString.ellipse(100)
            val wds = divert.map { case (wd, up) ⇒ s"$wd → ${up.getClass.getSimpleName}" }
            NoSuchElementProblem(s"""	|[DivertingWarpUnpacker(${this.warpDescriptor})]
            							|Tried to unpack a $fromStr
            							|Could not find an Unpacker for the found descriptor $wd
            							|I know the following descriptors:
            							|${wds.mkString("\n")}""".stripMargin).failure
        }
      case None ⇒
        UnspecifiedProblem(s"""[DivertingWarpUnpacker]:"${from.toString()} has no WarpDescriptor nor one can be derived!""").failure
    }
}

trait DivertingWarpUnpackerWithAutoRegistration[+T] { self: DivertingWarpUnpacker[T] ⇒
  def unpackers: List[RegisterableWarpUnpacker[T]]

  override lazy val divert = {
    val items = unpackers.map(up ⇒ up.allDescriptors.map(desc ⇒ (desc, up))).flatten
    items.toMap
  }
}

