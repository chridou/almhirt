package almhirt.corex.riftwarp.serialization

import java.util.{ UUID => JUUID }
import org.joda.time.LocalDateTime
import scalaz._, Scalaz._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.core.types._
import riftwarp._
import riftwarp.std.kit._
import riftwarp.std.WarpObjectLookUp

object DomainCommandHeaderWarpPackaging extends WarpPacker[DomainCommandHeader] with RegisterableWarpPacker with RegisterableWarpUnpacker[DomainCommandHeader] {
  import almhirt.core.types.DomainCommandHeader.BasicDomainCommandHeader
  val warpDescriptor = WarpDescriptor("DomainCommandHeader")
  val alternativeWarpDescriptors = WarpDescriptor(classOf[DomainCommandHeader]) :: WarpDescriptor(classOf[BasicDomainCommandHeader]) :: Nil
  override def pack(what: DomainCommandHeader)(implicit packers: WarpPackers): AlmValidation[WarpPackage] = {
    this.warpDescriptor ~>
      P("id", what.id) ~>
      P("aggId", what.aggRef.id) ~>
      P("aggVersion", what.aggRef.version) ~>
      P("timestamp", what.timestamp) ~>
      MP("metadata", what.metadata)
  }

  def unpack(from: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[DomainCommandHeader] =
    withFastLookUp(from) { lookup =>
      for {
        id <- lookup.getAs[JUUID]("id")
        aggId <- lookup.getAs[JUUID]("aggId")
        aggVersion <- lookup.getAs[Long]("aggVersion")
        timestamp <- lookup.getAs[LocalDateTime]("timestamp")
        metadata <- lookup.getPrimitiveAssocs[String, String]("metadata").map(_.toMap)
      } yield DomainCommandHeader(id, AggregateRootRef(aggId, aggVersion), timestamp, metadata)
    }
}

trait DomainCommandWarpPackagingTemplate[TCommand <: DomainCommand] extends WarpPacker[TCommand] with RegisterableWarpUnpacker[TCommand] {
  override def pack(what: TCommand)(implicit packers: WarpPackers): AlmValidation[WarpPackage] = {
    (this.warpDescriptor ~> With("header", what.header, DomainCommandHeaderWarpPackaging)).flatMap(obj =>
      addCommandParams(what, obj))
  }

  override def unpack(from: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[TCommand] =
    withFastLookUp(from) { lookup =>
      lookup.getWith("header", DomainCommandHeaderWarpPackaging).flatMap(header =>
        extractCommandParams(lookup, header))
    }

  def addCommandParams(what: TCommand, into: WarpObject)(implicit packers: WarpPackers): AlmValidation[WarpPackage]

  def extractCommandParams(from: WarpObjectLookUp, header: DomainCommandHeader)(implicit unpackers: WarpUnpackers): AlmValidation[TCommand]
}
