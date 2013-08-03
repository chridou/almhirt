package almhirt.corex.riftwarp.serialization

import java.util.{ UUID => JUUID }
import scalaz._, Scalaz._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.domain._
import almhirt.commanding._
import riftwarp._
import riftwarp.std.kit._

object AggregateRootRefWarpPackaging extends WarpPacker[AggregateRootRef] with RegisterableWarpPacker with RegisterableWarpUnpacker[AggregateRootRef] {
  val warpDescriptor = WarpDescriptor(classOf[AggregateRootRef])
  val alternativeWarpDescriptors = Nil
  override def pack(what: AggregateRootRef)(implicit packers: WarpPackers): AlmValidation[WarpPackage] = {
    this.warpDescriptor ~>
      P("id", what.id) ~>
      P("version", what.version)
  }
  
  def unpack(from: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[AggregateRootRef] = {
    withFastLookUp(from) { lookup =>
      for {
        id <- lookup.getAs[JUUID]("id")
        version <- lookup.getAs[Long]("version")
      } yield AggregateRootRef(id, version)
    }
  }
}
