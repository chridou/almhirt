package almhirt.ext.core.riftwarp.serialization

import scalaz._, Scalaz._
import almhirt.common._
import almhirt.almvalidation.kit._
import riftwarp._
import riftwarp.std.kit._

object ProblemEventWarpPacker extends WarpPacker[ProblemEvent] with RegisterableWarpPacker {
  val warpDescriptor = WarpDescriptor(classOf[ProblemEvent])
  val alternativeWarpDescriptors = Nil
  override def pack(what: ProblemEvent)(implicit packers: WarpPackers): AlmValidation[WarpPackage] = {
    this.warpDescriptor ~>
      With("header", what.header, EventHeaderWarpPacker) ~>
      LookUp("problem", what.problem)
  }
}

object ProblemEventWarpUnpacker extends RegisterableWarpUnpacker[ProblemEvent] {
  val warpDescriptor = WarpDescriptor(classOf[ProblemEvent])
  val alternativeWarpDescriptors = Nil
  def unpack(from: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[ProblemEvent] =
    withFastLookUp(from) { lookup =>
      for {
        header <- lookup.getWith("header", EventHeaderWarpUnpacker)
        problem <- lookup.getTyped[Problem]("problem", None)
      } yield ProblemEvent(header, problem)
    }
}