package almhirt.ext.core.riftwarp.serialization

import java.util.{ UUID => JUUID }
import org.joda.time.DateTime
import scalaz._, Scalaz._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.domain.{ DomainEventHeader, AggregateRootRef }
import riftwarp._
import riftwarp.std.kit._

object BasicEventHeaderWarpPacker extends WarpPacker[BasicEventHeader] with RegisterableWarpPacker {
  val warpDescriptor = WarpDescriptor(classOf[BasicEventHeader])
  val alternativeWarpDescriptors = Nil
  override def pack(what: BasicEventHeader)(implicit packers: WarpPackers): AlmValidation[WarpPackage] = {
    this.warpDescriptor ~>
      P("id", what.id) ~>
      P("timestamp", what.timestamp) ~>
      POpt("sender", what.sender)
  }
}

object BasicEventHeaderWarpUnpacker extends RegisterableWarpUnpacker[BasicEventHeader] {
  val warpDescriptor = WarpDescriptor(classOf[BasicEventHeader])
  val alternativeWarpDescriptors = Nil
  def unpack(from: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[BasicEventHeader] =
    withFastLookUp(from) { lookup =>
      for {
        id <- lookup.getAs[JUUID]("id")
        timestamp <- lookup.getAs[DateTime]("timestamp")
        sender <- lookup.tryGetAs[String]("sender")
      } yield BasicEventHeader(id, timestamp, sender)
    }
}

object DomainEventHeaderWarpPacker extends WarpPacker[DomainEventHeader] with RegisterableWarpPacker {
  val warpDescriptor = WarpDescriptor(classOf[DomainEventHeader])
  val alternativeWarpDescriptors = Nil
  override def pack(what: DomainEventHeader)(implicit packers: WarpPackers): AlmValidation[WarpPackage] = {
    this.warpDescriptor ~>
      P("id", what.id) ~>
      P("aggId", what.aggRef.id) ~>
      P("aggVersion", what.aggRef.version) ~>
      P("timestamp", what.timestamp) ~>
      POpt("sender", what.sender)
  }
}

object DomainEventHeaderWarpUnpacker extends RegisterableWarpUnpacker[DomainEventHeader] {
  val warpDescriptor = WarpDescriptor(classOf[DomainEventHeader])
  val alternativeWarpDescriptors = Nil
  def unpack(from: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[DomainEventHeader] =
    withFastLookUp(from) { lookup =>
      for {
        id <- lookup.getAs[JUUID]("id")
        aggId <- lookup.getAs[JUUID]("aggId")
        aggVersion <- lookup.getAs[Long]("aggVersion")
        timestamp <- lookup.getAs[DateTime]("timestamp")
        sender <- lookup.tryGetAs[String]("sender")
      } yield DomainEventHeader(id, AggregateRootRef(aggId, aggVersion), timestamp, sender)
    }
}

object EventHeaderWarpPacker extends WarpPacker[EventHeader] with RegisterableWarpPacker {
  val warpDescriptor = WarpDescriptor(classOf[EventHeader])
  val alternativeWarpDescriptors = Nil
  override def pack(what: EventHeader)(implicit packers: WarpPackers): AlmValidation[WarpPackage] = {
    what match {
      case header: BasicEventHeader => BasicEventHeaderWarpPacker.pack(header)
      case header: DomainEventHeader => DomainEventHeaderWarpPacker.pack(header)
    }
  }
}

object EventHeaderWarpUnpacker extends RegisterableWarpUnpacker[EventHeader] with DivertingWarpUnpacker[EventHeader] {
  val warpDescriptor = WarpDescriptor(classOf[EventHeader])
  val alternativeWarpDescriptors = Nil
  val recomposers = Map(
    BasicEventHeaderWarpUnpacker.warpDescriptor -> BasicEventHeaderWarpUnpacker,
    DomainEventHeaderWarpUnpacker.warpDescriptor -> DomainEventHeaderWarpUnpacker)
  val divert = recomposers.lift
}
