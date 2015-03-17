package riftwarp.serialization.common

import scalaz._, Scalaz._
import scalaz.Validation.FlatMap._
import almhirt.common._
import almhirt.aggregates._
import almhirt.almvalidation.kit._
import riftwarp._
import riftwarp.std.kit._
import riftwarp.std.WarpObjectLookUp

trait AggregateRootEventWarpPackagingTemplate[TEvent <: AggregateRootEvent] extends WarpPacker[TEvent] with RegisterableWarpUnpacker[TEvent] {
  override def pack(what: TEvent)(implicit packers: WarpPackers): AlmValidation[WarpPackage] = {
    (this.warpDescriptor ~>
      With("header", what.header, EventHeaderWarpPackaging) ~>
      P("aggId", what.aggId.value) ~>
      P("aggVersion", what.aggVersion.value)).flatMap(obj ⇒
        addEventParams(what, obj))
  }

  override def unpack(from: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[TEvent] =
    withFastLookUp(from) { lookup ⇒
      for {
        header ← lookup.getWith("header", EventHeaderWarpPackaging)
        aggId ← lookup.getAs[String]("aggId").flatMap(ValidatedAggregateRootId(_))
        version ← lookup.getAs[Long]("aggVersion").flatMap(ValidatedAggregateRootVersion(_))
        cmd ← extractEventParams(lookup, header, aggId, version)
      } yield cmd
    }

  def addEventParams(what: TEvent, into: WarpObject)(implicit packers: WarpPackers): AlmValidation[WarpPackage]

  def extractEventParams(from: WarpObjectLookUp, header: EventHeader, aggId: AggregateRootId, aggVersion: AggregateRootVersion)(implicit unpackers: WarpUnpackers): AlmValidation[TEvent]
}