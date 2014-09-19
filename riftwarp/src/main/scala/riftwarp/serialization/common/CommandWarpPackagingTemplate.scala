package riftwarp.serialization.common

import java.util.{ UUID ⇒ JUUID }
import org.joda.time.LocalDateTime
import scalaz._, Scalaz._
import almhirt.common._
import almhirt.almvalidation.kit._
import riftwarp._
import riftwarp.std.kit._
import riftwarp.std.WarpObjectLookUp


object CommandHeaderWarpPackaging extends WarpPacker[CommandHeader] with RegisterableWarpPacker with RegisterableWarpUnpacker[CommandHeader] {
  val warpDescriptor = WarpDescriptor("CommandHeader")
  val alternativeWarpDescriptors = WarpDescriptor(classOf[CommandHeader]) :: WarpDescriptor(classOf[CommandHeader]) :: Nil
  override def pack(what: CommandHeader)(implicit packers: WarpPackers): AlmValidation[WarpPackage] = {
    this.warpDescriptor ~>
      P("id", what.id.value ) ~>
      P("timestamp", what.timestamp) ~>
      MP("metadata", what.metadata)
  }

  def unpack(from: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[CommandHeader] =
    withFastLookUp(from) { lookup ⇒
      for {
        id <- lookup.getAs[String]("id").flatMap(ValidatedCommandId(_))
        timestamp <- lookup.getAs[LocalDateTime]("timestamp")
        metadata <- lookup.getPrimitiveAssocs[String, String]("metadata").map(_.toMap)
      } yield CommandHeader(id, timestamp, metadata)
    }

}

trait CommandWarpPackagingTemplate[TCommand <: Command] extends WarpPacker[TCommand] with RegisterableWarpUnpacker[TCommand]{
  override def pack(what: TCommand)(implicit packers: WarpPackers): AlmValidation[WarpPackage] = {
    (this.warpDescriptor ~> With("header", what.header, CommandHeaderWarpPackaging)).flatMap(obj ⇒
      addCommandParams(what, obj))
  }

  override def unpack(from: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[TCommand] =
    withFastLookUp(from) { lookup ⇒
      lookup.getWith("header", CommandHeaderWarpPackaging).flatMap(header ⇒
        extractCommandParams(lookup, header))
    }
  
  def addCommandParams(what: TCommand, into: WarpObject)(implicit packers: WarpPackers): AlmValidation[WarpPackage]

  def extractCommandParams(from: WarpObjectLookUp, header: CommandHeader)(implicit unpackers: WarpUnpackers): AlmValidation[TCommand]

}
