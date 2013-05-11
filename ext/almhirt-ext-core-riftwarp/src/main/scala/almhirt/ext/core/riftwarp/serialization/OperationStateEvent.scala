package almhirt.ext.core.riftwarp.serialization

import scalaz._, Scalaz._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.util.OperationStateEvent
import riftwarp._
import riftwarp.std.kit._

object OperationStateEventWarpPacker extends WarpPacker[OperationStateEvent] with RegisterableWarpPacker {
  val warpDescriptor = WarpDescriptor(classOf[OperationStateEvent])
  val alternativeWarpDescriptors = Nil
  override def pack(what: OperationStateEvent)(implicit packers: WarpPackers): AlmValidation[WarpPackage] = {
    this.warpDescriptor ~>
      With("header", what.header, EventHeaderWarpPacker) ~>
      With("operationState", what.operationState, OperationStateWarpPacker)
  }
}

object OperationStateEventWarpUnpacker extends RegisterableWarpUnpacker[OperationStateEvent] {
  val warpDescriptor = WarpDescriptor(classOf[OperationStateEvent])
  val alternativeWarpDescriptors = Nil
  def unpack(from: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[OperationStateEvent] =
    withFastLookUp(from) { lookup =>
      for {
        header <- lookup.getWith("header", EventHeaderWarpUnpacker)
        operationState <- lookup.getWith("operationState", OperationStateWarpUnpacker)
      } yield OperationStateEvent(header, operationState)
    }
}