package riftwarp.serialization.common

import _root_.java.time.LocalDateTime
import scalaz._, Scalaz._
import scalaz.Validation.FlatMap._
import almhirt.common._
import almhirt.almvalidation.kit._
import riftwarp._
import riftwarp.std.kit._
import riftwarp.std.WarpObjectLookUp


object EventHeaderWarpPackaging extends WarpPacker[EventHeader] with RegisterableWarpPacker with RegisterableWarpUnpacker[EventHeader] {
  val warpDescriptor = WarpDescriptor("EventHeader")
  val alternativeWarpDescriptors = WarpDescriptor(classOf[EventHeader]) :: Nil
  override def pack(what: EventHeader)(implicit packers: WarpPackers): AlmValidation[WarpPackage] = {
    this.warpDescriptor ~>
      P("id", what.id.value) ~>
      P("timestamp", what.timestamp) ~>
      MP("metadata", what.metadata)
  }

  def unpack(from: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[EventHeader] =
    withFastLookUp(from) { lookup ⇒
      for {
        id ← lookup.getAs[String]("id").flatMap(ValidatedEventId(_))
        timestamp ← lookup.getAs[LocalDateTime]("timestamp")
        metadata ← lookup.getPrimitiveAssocs[String, String]("metadata").map(_.toMap)
      } yield EventHeader(id, timestamp, metadata)
    }

}

trait EventWarpPackagingTemplate[TEvent <: Event] extends WarpPacker[TEvent] with RegisterableWarpUnpacker[TEvent]{
  override def pack(what: TEvent)(implicit packers: WarpPackers): AlmValidation[WarpPackage] = {
    (this.warpDescriptor ~> With("header", what.header, EventHeaderWarpPackaging)).flatMap(obj ⇒
      addEventParams(what, obj))
  }

  override def unpack(from: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[TEvent] =
    withFastLookUp(from) { lookup ⇒
      lookup.getWith("header", EventHeaderWarpPackaging).flatMap(header ⇒
        extractEventParams(lookup, header))
    }
  
  def addEventParams(what: TEvent, into: WarpObject)(implicit packers: WarpPackers): AlmValidation[WarpPackage]

  def extractEventParams(from: WarpObjectLookUp, header: EventHeader)(implicit unpackers: WarpUnpackers): AlmValidation[TEvent]

}
