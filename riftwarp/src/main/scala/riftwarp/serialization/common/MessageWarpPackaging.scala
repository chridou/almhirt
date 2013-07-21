package riftwarp.serialization.common


import java.util.{ UUID => JUUID }
import org.joda.time.DateTime
import scalaz._
import scalaz.Scalaz._
import almhirt.common._
import almhirt.almvalidation.kit._
import riftwarp._
import riftwarp.std.kit._

object MessageHeaderWarpPackaging extends WarpPacker[MessageHeader] with RegisterableWarpPacker with RegisterableWarpUnpacker[MessageHeader] { 
  val warpDescriptor = WarpDescriptor("MessageHeader")
  val alternativeWarpDescriptors = WarpDescriptor(classOf[MessageHeader]) :: Nil
  override def pack(what: MessageHeader)(implicit packers: WarpPackers): AlmValidation[WarpPackage] = {
    this.warpDescriptor ~>
      P("id", what.id) ~>
      P("timestamp", what.timestamp) ~>
      MLookUpForgiving("metadata", what.metadata)
  }
  
  def unpack(from: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[MessageHeader] =
    withFastLookUp(from) { lookup =>
      for {
        a <- lookup.getAs[JUUID]("id")
        b <- lookup.getAs[DateTime]("timestamp")
        c <- lookup.getPrimitiveAssocs[String, String]("metadata").map(_.toMap)
      } yield MessageHeader(a, b, c)
    }
  
}


object MessageWarpPackaging extends WarpPacker[Message] with RegisterableWarpPacker with RegisterableWarpUnpacker[Message]{ 
  val warpDescriptor = WarpDescriptor("Message")
  val alternativeWarpDescriptors = WarpDescriptor(classOf[Message]) :: Nil
  override def pack(what: Message)(implicit packers: WarpPackers): AlmValidation[WarpPackage] = {
    this.warpDescriptor ~>
      With("header", what.header, MessageHeaderWarpPackaging) ~>
      LookUp("payload", what.payload)
  }
  
  def unpack(from: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[Message] =
    withFastLookUp(from) { lookup =>
      for {
        header <- lookup.getWith("header", MessageHeaderWarpPackaging)
        payload <- lookup.getTyped[AnyRef]("payload")
      } yield Message(header, payload)
    }
  
}
