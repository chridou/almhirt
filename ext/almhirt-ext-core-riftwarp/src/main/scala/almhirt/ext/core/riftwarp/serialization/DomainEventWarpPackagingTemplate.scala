package almhirt.ext.core.riftwarp.serialization

import java.util.{ UUID => JUUID }
import org.joda.time.DateTime
import scalaz._, Scalaz._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.domain._
import riftwarp._
import riftwarp.std.kit._
import riftwarp.std.WarpObjectLookUp

object DomainEventHeaderWarpPackaging extends WarpPacker[DomainEventHeader] with RegisterableWarpPacker with RegisterableWarpUnpacker[DomainEventHeader] {
  val warpDescriptor = WarpDescriptor("DomainEventHeader")
  val alternativeWarpDescriptors = WarpDescriptor(classOf[DomainEventHeader]) :: Nil
  override def pack(what: DomainEventHeader)(implicit packers: WarpPackers): AlmValidation[WarpPackage] = {
    this.warpDescriptor ~>
      P("id", what.id) ~>
      P("aggId", what.aggRef.id) ~>
      P("aggVersion", what.aggRef.version) ~>
      P("timestamp", what.timestamp) ~>
      MP("metadata", what.metadata)
  }

  def unpack(from: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[DomainEventHeader] =
    withFastLookUp(from) { lookup =>
      for {
        id <- lookup.getAs[JUUID]("id")
        aggId <- lookup.getAs[JUUID]("aggId")
        aggVersion <- lookup.getAs[Long]("aggVersion")
        timestamp <- lookup.getAs[DateTime]("timestamp")
        metadata <- lookup.getPrimitiveAssocs[String, String]("metadata").map(_.toMap)
      } yield DomainEventHeader(id, AggregateRootRef(aggId, aggVersion), timestamp, metadata)
    }

}

trait DomainEventWarpPackagingTemplate[TEvent <: DomainEvent] extends WarpPacker[TEvent] with RegisterableWarpUnpacker[TEvent]{
  override def pack(what: TEvent)(implicit packers: WarpPackers): AlmValidation[WarpPackage] = {
    (this.warpDescriptor ~> With("header", what.header, DomainEventHeaderWarpPackaging)).flatMap(obj =>
      addEventParams(what, obj))
  }

  override def unpack(from: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[TEvent] =
    withFastLookUp(from) { lookup =>
      lookup.getWith("header", DomainEventHeaderWarpPackaging).flatMap(header =>
        extractEventParams(lookup, header))
    }
  
  def addEventParams(what: TEvent, into: WarpObject)(implicit packers: WarpPackers): AlmValidation[WarpPackage]

  def extractEventParams(from: WarpObjectLookUp, header: DomainEventHeader)(implicit unpackers: WarpUnpackers): AlmValidation[TEvent]

}
