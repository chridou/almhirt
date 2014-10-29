package riftwarp.serialization.common

import scalaz._, Scalaz._
import scalaz.std._
import scalaz.Validation.FlatMap._
import almhirt.common._
import almhirt.almvalidation.kit._
import riftwarp._
import riftwarp.std.kit._

object WarpDescriptorPacker extends WarpPacker[WarpDescriptor] with RegisterableWarpPacker with SimpleWarpPacker[WarpDescriptor] {
  val warpDescriptor = WarpDescriptor(classOf[WarpDescriptor])
  val alternativeWarpDescriptors = Nil
  override def pack(what: WarpDescriptor)(implicit packers: WarpPackers): AlmValidation[WarpPackage] = {
    this.warpDescriptor ~>
      P[String]("type", what.toParsableString(";"))
  }
}

object WarpDescriptorUnpacker extends RegisterableWarpUnpacker[WarpDescriptor] {
  val warpDescriptor = WarpDescriptor(classOf[WarpDescriptor])
  val alternativeWarpDescriptors = Nil
  def unpack(from: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[WarpDescriptor] = {
    withFastLookUp(from) { lookup ⇒
      for {
        theTypeStr ← lookup.getAs[String]("type")
        wd ← WarpDescriptor.parse(theTypeStr)
      } yield wd
    }
  }
}
