package riftwarp.std

import java.net.URI
import java.util.{ UUID => JUUID }
import org.joda.time.{ DateTime, LocalDateTime }
import scala.concurrent.duration._
import scalaz.syntax.validation._
import almhirt.common.AlmValidation
import riftwarp._
import almhirt.common.UnspecifiedProblem

object BooleanWarpPacker extends WarpPacker[Boolean] with SimpleWarpPacker[Boolean] with RegisterableWarpPacker {
  override val warpDescriptor = WarpDescriptor("Boolean")
  override val alternativeWarpDescriptors = WarpDescriptor(classOf[Boolean]) :: WarpDescriptor(classOf[java.lang.Boolean]) :: Nil
  override def pack(what: Boolean)(implicit packers: WarpPackers): AlmValidation[WarpBoolean] = WarpBoolean(what).success
}

object BooleanWarpUnpacker extends RegisterableWarpUnpacker[Boolean] {
  override val warpDescriptor = WarpDescriptor("Boolean")
  override val alternativeWarpDescriptors = WarpDescriptor(classOf[Boolean]) :: WarpDescriptor(classOf[java.lang.Boolean]) :: Nil
  override def unpack(what: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[Boolean] =
    what match {
      case WarpBoolean(v) => v.success
      case x => UnspecifiedProblem(s""""${x.getClass().getName()}" can not unpack to a Boolean""").failure
    }
}

object StringWarpPacker extends WarpPacker[String] with SimpleWarpPacker[String] with RegisterableWarpPacker {
  override val warpDescriptor = WarpDescriptor("String")
  override val alternativeWarpDescriptors = WarpDescriptor(classOf[String]) :: WarpDescriptor(classOf[java.lang.String]) :: Nil
  override def pack(what: String)(implicit packers: WarpPackers): AlmValidation[WarpString] = WarpString(what).success
}

object StringWarpUnpacker extends RegisterableWarpUnpacker[String] {
  override val warpDescriptor = WarpDescriptor("String")
  override val alternativeWarpDescriptors = WarpDescriptor(classOf[String]) :: WarpDescriptor(classOf[java.lang.String]) :: Nil
  override def unpack(what: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[String] =
    what match {
      case WarpString(v) => v.success
      case x => UnspecifiedProblem(s""""${x.getClass().getName()}" can not unpack to a String""").failure
    }
}

object ByteWarpPacker extends WarpPacker[Byte] with SimpleWarpPacker[Byte] with RegisterableWarpPacker {
  override val warpDescriptor = WarpDescriptor("Byte")
  override val alternativeWarpDescriptors = WarpDescriptor(classOf[Byte]) :: WarpDescriptor(classOf[java.lang.Byte]) :: Nil
  override def pack(what: Byte)(implicit packers: WarpPackers): AlmValidation[WarpByte] = WarpByte(what).success
}

object ByteWarpUnpacker extends RegisterableWarpUnpacker[Byte] {
  override val warpDescriptor = WarpDescriptor("Byte")
  override val alternativeWarpDescriptors = WarpDescriptor(classOf[Byte]) :: WarpDescriptor(classOf[java.lang.Byte]) :: Nil
  override def unpack(what: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[Byte] =
    what match {
      case WarpByte(v) => v.success
      case x => UnspecifiedProblem(s""""${x.getClass().getName()}" can not unpack to a Byte""").failure
    }
}
object ShortWarpPacker extends WarpPacker[Short] with SimpleWarpPacker[Short] with RegisterableWarpPacker {
  override val warpDescriptor = WarpDescriptor("Short")
  override val alternativeWarpDescriptors = WarpDescriptor("Short") :: WarpDescriptor(classOf[Short]) :: WarpDescriptor(classOf[java.lang.Short]) :: Nil
  override def pack(what: Short)(implicit packers: WarpPackers): AlmValidation[WarpShort] = WarpShort(what).success
}

object ShortWarpUnpacker extends RegisterableWarpUnpacker[Short] {
  override val warpDescriptor = WarpDescriptor("Short")
  override val alternativeWarpDescriptors = WarpDescriptor("Short") :: WarpDescriptor(classOf[Short]) :: WarpDescriptor(classOf[java.lang.Short]) :: Nil
  override def unpack(what: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[Short] =
    what match {
      case WarpShort(v) => v.success
      case x => UnspecifiedProblem(s""""${x.getClass().getName()}" can not unpack to a Short""").failure
    }
}

object IntWarpPacker extends WarpPacker[Int] with SimpleWarpPacker[Int] with RegisterableWarpPacker {
  override val warpDescriptor = WarpDescriptor("Int")
  override val alternativeWarpDescriptors = WarpDescriptor("Integer") :: WarpDescriptor(classOf[Int]) :: WarpDescriptor(classOf[java.lang.Integer]) :: Nil
  override def pack(what: Int)(implicit packers: WarpPackers): AlmValidation[WarpInt] = WarpInt(what).success
}

object IntWarpUnpacker extends RegisterableWarpUnpacker[Int] {
  override val warpDescriptor = WarpDescriptor("Int")
  override val alternativeWarpDescriptors = WarpDescriptor("Integer") :: WarpDescriptor(classOf[Int]) :: WarpDescriptor(classOf[java.lang.Integer]) :: Nil
  override def unpack(what: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[Int] =
    what match {
      case WarpInt(v) => v.success
      case x => UnspecifiedProblem(s""""${x.getClass().getName()}" can not unpack to an Int""").failure
    }
}

object LongWarpPacker extends WarpPacker[Long] with SimpleWarpPacker[Long] with RegisterableWarpPacker {
  override val warpDescriptor = WarpDescriptor("Long")
  override val alternativeWarpDescriptors = WarpDescriptor(classOf[Long]) :: WarpDescriptor(classOf[java.lang.Long]) :: Nil
  override def pack(what: Long)(implicit packers: WarpPackers): AlmValidation[WarpLong] = WarpLong(what).success
}

object LongWarpUnpacker extends RegisterableWarpUnpacker[Long] {
  override val warpDescriptor = WarpDescriptor("Long")
  override val alternativeWarpDescriptors = WarpDescriptor(classOf[Long]) :: WarpDescriptor(classOf[java.lang.Long]) :: Nil
  override def unpack(what: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[Long] =
    what match {
      case WarpLong(v) => v.success
      case x => UnspecifiedProblem(s""""${x.getClass().getName()}" can not unpack to a Long""").failure
    }
}

object BigIntWarpPacker extends WarpPacker[BigInt] with SimpleWarpPacker[BigInt] with RegisterableWarpPacker {
  override val warpDescriptor = WarpDescriptor("BigInt")
  override val alternativeWarpDescriptors = WarpDescriptor(classOf[BigInt]) :: Nil
  override def pack(what: BigInt)(implicit packers: WarpPackers): AlmValidation[WarpBigInt] = WarpBigInt(what).success
}

object BigIntWarpUnpacker extends RegisterableWarpUnpacker[BigInt] {
  override val warpDescriptor = WarpDescriptor("BigInt")
  override val alternativeWarpDescriptors = WarpDescriptor(classOf[BigInt]) :: Nil
  override def unpack(what: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[BigInt] =
    what match {
      case WarpBigInt(v) => v.success
      case x => UnspecifiedProblem(s""""${x.getClass().getName()}" can not unpack to a BigInt""").failure
    }
}

object FloatWarpPacker extends WarpPacker[Float] with SimpleWarpPacker[Float] with RegisterableWarpPacker {
  override val warpDescriptor = WarpDescriptor("Float")
  override val alternativeWarpDescriptors = WarpDescriptor(classOf[Float]) :: WarpDescriptor(classOf[java.lang.Float]) :: Nil
  override def pack(what: Float)(implicit packers: WarpPackers): AlmValidation[WarpFloat] = WarpFloat(what).success
}

object FloatWarpUnpacker extends RegisterableWarpUnpacker[Float] {
  override val warpDescriptor = WarpDescriptor("Float")
  override val alternativeWarpDescriptors = WarpDescriptor(classOf[Float]) :: WarpDescriptor(classOf[java.lang.Float]) :: Nil
  override def unpack(what: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[Float] =
    what match {
      case WarpFloat(v) => v.success
      case x => UnspecifiedProblem(s""""${x.getClass().getName()}" can not unpack to a Float""").failure
    }
}

object DoubleWarpPacker extends WarpPacker[Double] with SimpleWarpPacker[Double] with RegisterableWarpPacker {
  override val warpDescriptor = WarpDescriptor("Double")
  override val alternativeWarpDescriptors = WarpDescriptor(classOf[Double]) :: WarpDescriptor(classOf[java.lang.Double]) :: Nil
  override def pack(what: Double)(implicit packers: WarpPackers): AlmValidation[WarpDouble] = WarpDouble(what).success
}

object DoubleWarpUnpacker extends RegisterableWarpUnpacker[Double] {
  override val warpDescriptor = WarpDescriptor("Double")
  override val alternativeWarpDescriptors = WarpDescriptor(classOf[Double]) :: WarpDescriptor(classOf[java.lang.Double]) :: Nil
  override def unpack(what: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[Double] =
    what match {
      case WarpDouble(v) => v.success
      case x => UnspecifiedProblem(s""""${x.getClass().getName()}" can not unpack to a Double""").failure
    }
}

object BigDecimalWarpPacker extends WarpPacker[BigDecimal] with SimpleWarpPacker[BigDecimal] with RegisterableWarpPacker {
  override val warpDescriptor = WarpDescriptor("BigDecimal")
  override val alternativeWarpDescriptors = WarpDescriptor(classOf[BigDecimal]) :: Nil
  override def pack(what: BigDecimal)(implicit packers: WarpPackers): AlmValidation[WarpBigDecimal] = WarpBigDecimal(what).success
}

object BigDecimalWarpUnpacker extends RegisterableWarpUnpacker[BigDecimal] {
  override val warpDescriptor = WarpDescriptor("BigDecimal")
  override val alternativeWarpDescriptors = WarpDescriptor(classOf[BigDecimal]) :: Nil
  override def unpack(what: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[BigDecimal] =
    what match {
      case WarpBigDecimal(v) => v.success
      case x => UnspecifiedProblem(s""""${x.getClass().getName()}" can not unpack to a BigDecimal""").failure
    }
}

object UuidWarpPacker extends WarpPacker[JUUID] with SimpleWarpPacker[JUUID] with RegisterableWarpPacker {
  override val warpDescriptor = WarpDescriptor("UUID")
  override val alternativeWarpDescriptors = WarpDescriptor(classOf[JUUID]) :: Nil
  override def pack(what: JUUID)(implicit packers: WarpPackers): AlmValidation[WarpUuid] = WarpUuid(what).success
}

object UuidWarpUnpacker extends RegisterableWarpUnpacker[JUUID] {
  override val warpDescriptor = WarpDescriptor("UUID")
  override val alternativeWarpDescriptors = WarpDescriptor(classOf[JUUID]) :: Nil
  override def unpack(what: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[JUUID] =
    what match {
      case WarpUuid(v) => v.success
      case x => UnspecifiedProblem(s""""${x.getClass().getName()}" can not unpack to an UUID""").failure
    }
}

object UriWarpPacker extends WarpPacker[URI] with SimpleWarpPacker[URI] with RegisterableWarpPacker {
  override val warpDescriptor = WarpDescriptor("URI")
  override val alternativeWarpDescriptors = WarpDescriptor(classOf[URI]) :: Nil
  override def pack(what: URI)(implicit packers: WarpPackers): AlmValidation[WarpUri] = WarpUri(what).success
}

object UriWarpUnpacker extends RegisterableWarpUnpacker[URI] {
  override val warpDescriptor = WarpDescriptor("URI")
  override val alternativeWarpDescriptors = WarpDescriptor(classOf[URI]) :: Nil
  override def unpack(what: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[URI] =
    what match {
      case WarpUri(v) => v.success
      case x => UnspecifiedProblem(s""""${x.getClass().getName()}" can not unpack to an URI""").failure
    }
}

object DateTimeWarpPacker extends WarpPacker[DateTime] with SimpleWarpPacker[DateTime] with RegisterableWarpPacker {
  override val warpDescriptor = WarpDescriptor("DateTime")
  override val alternativeWarpDescriptors = WarpDescriptor(classOf[DateTime]) :: Nil
  override def pack(what: DateTime)(implicit packers: WarpPackers): AlmValidation[WarpDateTime] = WarpDateTime(what).success
}

object DateTimeWarpUnpacker extends RegisterableWarpUnpacker[DateTime] {
  override val warpDescriptor = WarpDescriptor("DateTime")
  override val alternativeWarpDescriptors = WarpDescriptor(classOf[DateTime]) :: Nil
  override def unpack(what: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[DateTime] =
    what match {
      case WarpDateTime(v) => v.success
      case x => UnspecifiedProblem(s""""${x.getClass().getName()}" can not unpack to a DateTime""").failure
    }
}

object LocalDateTimeWarpPacker extends WarpPacker[LocalDateTime] with SimpleWarpPacker[LocalDateTime] with RegisterableWarpPacker {
  override val warpDescriptor = WarpDescriptor("LocalDateTime")
  override val alternativeWarpDescriptors = WarpDescriptor(classOf[LocalDateTime]) :: Nil
  override def pack(what: LocalDateTime)(implicit packers: WarpPackers): AlmValidation[WarpLocalDateTime] = WarpLocalDateTime(what).success
}

object LocalDateTimeWarpUnpacker extends RegisterableWarpUnpacker[LocalDateTime] {
  override val warpDescriptor = WarpDescriptor("LocalDateTime")
  override val alternativeWarpDescriptors = WarpDescriptor(classOf[LocalDateTime]) :: Nil
  override def unpack(what: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[LocalDateTime] =
    what match {
      case WarpLocalDateTime(v) => v.success
      case x => UnspecifiedProblem(s""""${x.getClass().getName()}" can not unpack to a LocalDateTime""").failure
    }
}

object DurationWarpPacker extends WarpPacker[FiniteDuration] with SimpleWarpPacker[FiniteDuration] with RegisterableWarpPacker {
  override val warpDescriptor = WarpDescriptor("DateTime")
  override val alternativeWarpDescriptors = WarpDescriptor(classOf[FiniteDuration]) :: Nil
  override def pack(what: FiniteDuration)(implicit packers: WarpPackers): AlmValidation[WarpDuration] = WarpDuration(what).success
}

object DurationWarpUnpacker extends RegisterableWarpUnpacker[FiniteDuration] {
  override val warpDescriptor = WarpDescriptor("FiniteDuration")
  override val alternativeWarpDescriptors = WarpDescriptor(classOf[FiniteDuration]) :: Nil
  override def unpack(what: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[FiniteDuration] =
    what match {
      case WarpDuration(v) => v.success
      case x => UnspecifiedProblem(s""""${x.getClass().getName()}" can not unpack to a FiniteDuration""").failure
    }
}

object ByteArrayWarpPacker extends WarpPacker[IndexedSeq[Byte]] with SimpleWarpPacker[IndexedSeq[Byte]] with RegisterableWarpPacker {
  override val warpDescriptor = WarpDescriptor("Bytes")
  override val alternativeWarpDescriptors = Nil
  override def pack(what: IndexedSeq[Byte])(implicit packers: WarpPackers): AlmValidation[WarpBytes] = WarpBytes(what).success
}

object ByteArrayWarpUnpacker extends RegisterableWarpUnpacker[IndexedSeq[Byte]] {
  override val warpDescriptor = WarpDescriptor("Bytes")
  override val alternativeWarpDescriptors = Nil
  override def unpack(what: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[IndexedSeq[Byte]] =
    what match {
      case WarpBytes(v) => v.success
      case x => UnspecifiedProblem(s""""${x.getClass().getName()}" can not unpack to an Array[Byte]""").failure
    }
}

object Base64BlobWarpPacker extends WarpPacker[IndexedSeq[Byte]] with SimpleWarpPacker[IndexedSeq[Byte]] with RegisterableWarpPacker {
  override val warpDescriptor = WarpDescriptor("Base64Blob")
  override val alternativeWarpDescriptors = Nil
  override def pack(what: IndexedSeq[Byte])(implicit packers: WarpPackers): AlmValidation[WarpBlob] = WarpBlob(what).success
  def asWarpObject(bytes: IndexedSeq[Byte]) = {
    val elem = WarpElement("data", Some(WarpString(org.apache.commons.codec.binary.Base64.encodeBase64String(bytes.toArray))))
    val wd = WarpDescriptor("Base64Blob")
    WarpObject(Some(wd), Vector(elem))
  }
}

object Base64BlobWarpUnpacker extends RegisterableWarpUnpacker[IndexedSeq[Byte]] {
  override val warpDescriptor = WarpDescriptor("Base64Blob")
  override val alternativeWarpDescriptors = Nil
  override def unpack(what: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[IndexedSeq[Byte]] =
    what match {
      case WarpBlob(v) => v.success
      case WarpObject(Some(WarpDescriptor("Base64Blob")), elems) =>
        elems.headOption match {
          case Some(WarpElement("data", Some(WarpString(data)))) => ParseFuns.parseBase64Alm(data).map(IndexedSeq(_: _*))
          case _ => UnspecifiedProblem(s""""${what.getClass().getName()}" can not be unpacked to an Array[Byte] from a WarpObject because it is empty, has no element labeled "data" or its content is not a WarpString""").failure
        }
      case x => UnspecifiedProblem(s""""${x.getClass().getName()}" can not unpack to an Array[Byte]""").failure
    }
}

