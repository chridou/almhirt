package riftwarpx.almhirt.serialization.core

import scalaz._, Scalaz._
import scalaz.Validation.FlatMap._
import almhirt.common._
import riftwarp._
import riftwarp.std.kit._
import riftwarp.std.WarpObjectLookUp
import almhirt.akkax.events._
import almhirt.akkax.{ GlobalComponentId }

trait ComponentEventPackagingTemplate[TEvent <: ComponentEvent] extends WarpPacker[TEvent] with RegisterableWarpUnpacker[TEvent] with RegisterableWarpPacker {
  override def pack(what: TEvent)(implicit packers: WarpPackers): AlmValidation[WarpPackage] = {
    (this.warpDescriptor ~>
      With("header", what.header, riftwarp.serialization.common.EventHeaderWarpPackaging) ~>
      With("origin", what.origin, GlobalComponentIdWarpPackaging)).flatMap(obj ⇒
        addEventParams(what, obj))
  }

  override def unpack(from: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[TEvent] =
    withFastLookUp(from) { lookup ⇒
      for {
        header ← lookup.getWith("header", riftwarp.serialization.common.EventHeaderWarpPackaging)
        origin ← lookup.getWith("origin", GlobalComponentIdWarpPackaging)
        complete ← extractEventParams(lookup, header, origin)
      } yield complete
    }

  def addEventParams(what: TEvent, into: WarpObject)(implicit packers: WarpPackers): AlmValidation[WarpPackage]

  def extractEventParams(from: WarpObjectLookUp, header: EventHeader, origin: GlobalComponentId)(implicit unpackers: WarpUnpackers): AlmValidation[TEvent]

}
