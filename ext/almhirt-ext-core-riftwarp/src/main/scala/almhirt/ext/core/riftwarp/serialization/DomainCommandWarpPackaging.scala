package almhirt.ext.core.riftwarp.serialization

import java.util.{ UUID => JUUID }
import org.joda.time.LocalDateTime
import scalaz._, Scalaz._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.commanding._
import riftwarp._
import riftwarp.std.kit._
import riftwarp.std.WarpObjectLookUp
import almhirt.domain.AggregateRootRef

object DomainCommandHeaderWarpPackaging extends WarpPacker[DomainCommandHeader] with RegisterableWarpPacker with RegisterableWarpUnpacker[DomainCommandHeader] {
  val warpDescriptor = WarpDescriptor("DomainCommandHeader")
  val alternativeWarpDescriptors = WarpDescriptor(classOf[DomainCommandHeader]) :: Nil
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

trait DomainCommandWarpPackagingTemplate[TEvent <: DomainCommand] extends WarpPacker[TEvent] with RegisterableWarpUnpacker[TEvent]{
  override def pack(what: TEvent)(implicit packers: WarpPackers): AlmValidation[WarpPackage] = {
    (this.warpDescriptor ~> With("header", what.header, DomainCommandHeaderWarpPackaging)).flatMap(obj =>
      addCommandParams(what, obj))
  }

  override def unpack(from: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[TEvent] =
    withFastLookUp(from) { lookup =>
      lookup.getWith("header", DomainCommandHeaderWarpPackaging).flatMap(header =>
        extractCommandParams(lookup, header))
    }
  
  def addCommandParams(what: TEvent, into: WarpObject)(implicit packers: WarpPackers): AlmValidation[WarpPackage]

  def extractCommandParams(from: WarpObjectLookUp, header: DomainCommandHeader)(implicit unpackers: WarpUnpackers): AlmValidation[TEvent]
}
