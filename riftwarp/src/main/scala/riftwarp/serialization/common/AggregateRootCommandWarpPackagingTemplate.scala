package riftwarp.serialization.common

import scalaz._, Scalaz._
import almhirt.common._
import almhirt.aggregates._
import almhirt.almvalidation.kit._
import riftwarp._
import riftwarp.std.kit._
import riftwarp.std.WarpObjectLookUp

trait AggregateRootCommandWarpPackagingTemplate[TCommand <: AggregateRootCommand] extends WarpPacker[TCommand] with RegisterableWarpUnpacker[TCommand] {
  override def pack(what: TCommand)(implicit packers: WarpPackers): AlmValidation[WarpPackage] = {
    (this.warpDescriptor ~>
      With("header", what.header, CommandHeaderWarpPackaging) ~>
      P("aggId", what.aggId.value) ~>
      P("aggVersion", what.aggVersion.value)).flatMap(obj ⇒
        addCommandParams(what, obj))
  }

  override def unpack(from: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[TCommand] =
    withFastLookUp(from) { lookup ⇒
      for {
        header <- lookup.getWith("header", CommandHeaderWarpPackaging)
        id <- lookup.getAs[String]("aggId").flatMap(ValidatedAggregatedRootId(_))
        version <- lookup.getAs[Long]("aggVersion").flatMap(ValidatedAggregateRootVersion(_))
        cmd <- extractCommandParams(lookup, header, id, version)
      } yield cmd
    }

  def addCommandParams(what: TCommand, into: WarpObject)(implicit packers: WarpPackers): AlmValidation[WarpPackage]

  def extractCommandParams(from: WarpObjectLookUp, header: CommandHeader, aggId: AggregateRootId, aggVersion: AggregateRootVersion)(implicit unpackers: WarpUnpackers): AlmValidation[TCommand]
}