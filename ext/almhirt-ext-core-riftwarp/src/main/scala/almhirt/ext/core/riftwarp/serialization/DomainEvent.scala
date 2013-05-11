package almhirt.ext.core.riftwarp.serialization

import scalaz._, Scalaz._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.domain._
import riftwarp._
import riftwarp.std.kit._
import riftwarp.std.WarpObjectLookUp

trait DomainEventWarpPacker[TEvent <: DomainEvent] extends WarpPacker[TEvent] {
  override def pack(what: TEvent)(implicit packers: WarpPackers): AlmValidation[WarpPackage] = {
    (this.warpDescriptor ~> With("header", what.header, DomainEventHeaderWarpPacker)).flatMap(obj =>
      addEventParams(what, obj))
  }

  def addEventParams(what: TEvent, into: WarpObject)(implicit packers: WarpPackers): AlmValidation[WarpPackage]
}

trait DomainEventWarpUnpacker[TEvent <: DomainEvent] extends RegisterableWarpUnpacker[TEvent] {
  def unpack(from: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[TEvent] =
    withFastLookUp(from) { lookup =>
      lookup.getWith("header", DomainEventHeaderWarpUnpacker).flatMap(header =>
        extractEventParams(lookup, header))
    }

  def extractEventParams(from: WarpObjectLookUp, header: DomainEventHeader)(implicit unpackers: WarpUnpackers): AlmValidation[TEvent]
}