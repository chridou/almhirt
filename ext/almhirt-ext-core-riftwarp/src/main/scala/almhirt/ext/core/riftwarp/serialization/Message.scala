package almhirt.ext.core.riftwarp.serialization

import java.util.{ UUID => JUUID }
import org.joda.time.DateTime
import scalaz._
import scalaz.Scalaz._
import almhirt.common._
import almhirt.almvalidation.kit._
import riftwarp._
import riftwarp.std.kit._
import almhirt.messaging._
import almhirt.messaging.MessageGrouping

object MessageGroupingWarpPacker extends WarpPacker[MessageGrouping] with RegisterableWarpPacker { 
  val warpDescriptor = WarpDescriptor(classOf[MessageGrouping])
  val alternativeWarpDescriptors = Nil
  override def pack(what: MessageGrouping)(implicit packers: WarpPackers): AlmValidation[WarpPackage] = {
    this.warpDescriptor ~>
      P("groupId", what.groupId) ~>
      P("seq", what.seq) ~>
      P("isLast", what.isLast)
  }
}

object MessageGroupingWarpUnpacker extends RegisterableWarpUnpacker[MessageGrouping] {
  val warpDescriptor = WarpDescriptor(classOf[MessageGrouping])
  val alternativeWarpDescriptors = Nil
  def unpack(from: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[MessageGrouping] =
    withFastLookUp(from) { lookup =>
      for {
        id <- lookup.getAs[JUUID]("groupId")
        seq <- lookup.getAs[Int]("seq")
        last <- lookup.getAs[Boolean]("isLast")
      } yield MessageGrouping(id, seq, last)
    }
}

object MessageHeaderWarpPacker extends WarpPacker[MessageHeader] with RegisterableWarpPacker { 
  val warpDescriptor = WarpDescriptor(classOf[MessageHeader])
  val alternativeWarpDescriptors = Nil
  override def pack(what: MessageHeader)(implicit packers: WarpPackers): AlmValidation[WarpPackage] = {
    this.warpDescriptor ~>
      P("id", what.id) ~>
      WithOpt("grouping", what.grouping, MessageGroupingWarpPacker) ~>
      MLookUpForgiving("metaData", what.metaData) ~>
      P("timestamp", what.timestamp)
  }
}

object MessageHeaderWarpUnpacker extends RegisterableWarpUnpacker[MessageHeader] {
  val warpDescriptor = WarpDescriptor(classOf[MessageHeader])
  val alternativeWarpDescriptors = Nil
  def unpack(from: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[MessageHeader] =
    withFastLookUp(from) { lookup =>
      for {
        a <- lookup.getAs[JUUID]("id")
        b <- lookup.tryGetWith("grouping", MessageGroupingWarpUnpacker)
        c <- lookup.getAssocs[String]("metaData").map(_.toMap)
        d <- lookup.getAs[DateTime]("timestamp")
      } yield MessageHeader(a, b, c, d)
    }
}

object MessageWarpPacker extends WarpPacker[Message[AnyRef]] with RegisterableWarpPacker { 
  val warpDescriptor = WarpDescriptor("Message")
  val alternativeWarpDescriptors = WarpDescriptor(classOf[Message[AnyRef]]) :: Nil
  override def pack(what: Message[AnyRef])(implicit packers: WarpPackers): AlmValidation[WarpPackage] = {
    this.warpDescriptor ~>
      With("header", what.header, MessageHeaderWarpPacker) ~>
      LookUp("payload", what.payload)
  }
}

object MessageWarpUnpacker extends RegisterableWarpUnpacker[Message[AnyRef]] {
  val warpDescriptor = WarpDescriptor("Message")
  val alternativeWarpDescriptors = WarpDescriptor(classOf[Message[AnyRef]]) :: Nil
  def unpack(from: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[Message[AnyRef]] =
    withFastLookUp(from) { lookup =>
      for {
        header <- lookup.getWith("header", MessageHeaderWarpUnpacker)
        payload <- lookup.getTyped[AnyRef]("payload")
      } yield Message(header, payload)
    }
}