package riftwarp.std

import java.net.URI
import java.util.{ UUID => JUUID }
import org.joda.time.DateTime
import scalaz.syntax.validation._
import almhirt.common.AlmValidation
import riftwarp._
import almhirt.common.UnspecifiedProblem

object BooleanWarpPacker extends WarpPacker[Boolean] with SimpleWarpPacker[Boolean] with RegisterableWarpPacker {
  override val warpDescriptor = WarpDescriptor("Boolean")
  override val alternativeWarpDescriptors = WarpDescriptor(classOf[Boolean]) :: Nil
  override def pack(what: Boolean)(implicit packers: WarpPackers): AlmValidation[WarpBoolean] = WarpBoolean(what).success
}

object BooleanWarpUnpacker extends RegisterableWarpUnpacker[Boolean] {
  override val warpDescriptor = WarpDescriptor("Boolean")
  override val alternativeWarpDescriptors = WarpDescriptor(classOf[Boolean]) :: Nil
  override def unpack(what: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[Boolean] = 
    what match {
    case WarpBoolean(v) => v.success
    case x => UnspecifiedProblem(s""""${x.getClass().getName()}" can not unpack to a Boolean""").failure
  }
}

object StringWarpPacker extends WarpPacker[String] with SimpleWarpPacker[String] with RegisterableWarpPacker {
  override val warpDescriptor = WarpDescriptor("String")
  override val alternativeWarpDescriptors = WarpDescriptor(classOf[String]) :: Nil
  override def pack(what: String)(implicit packers: WarpPackers): AlmValidation[WarpString] = WarpString(what).success
}

object StringWarpUnpacker extends RegisterableWarpUnpacker[String] {
  override val warpDescriptor = WarpDescriptor("String")
  override val alternativeWarpDescriptors = WarpDescriptor(classOf[String]) :: Nil
  override def unpack(what: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[String] = 
    what match {
    case WarpString(v) => v.success
    case x => UnspecifiedProblem(s""""${x.getClass().getName()}" can not unpack to a String""").failure
  }
}

object ByteWarpPacker extends WarpPacker[Byte] with SimpleWarpPacker[Byte] with RegisterableWarpPacker {
  override val warpDescriptor = WarpDescriptor("Byte")
  override val alternativeWarpDescriptors = WarpDescriptor(classOf[Byte]) :: Nil
  override def pack(what: Byte)(implicit packers: WarpPackers): AlmValidation[WarpByte] = WarpByte(what).success
}

object ByteWarpUnpacker extends RegisterableWarpUnpacker[Byte] {
  override val warpDescriptor = WarpDescriptor("Byte")
  override val alternativeWarpDescriptors = WarpDescriptor(classOf[Byte]) :: Nil
  override def unpack(what: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[Byte] = 
    what match {
    case WarpByte(v) => v.success
    case x => UnspecifiedProblem(s""""${x.getClass().getName()}" can not unpack to a Byte""").failure
  }
}
object IntWarpPacker extends WarpPacker[Int] with SimpleWarpPacker[Int] with RegisterableWarpPacker {
  override val warpDescriptor = WarpDescriptor("Int")
  override val alternativeWarpDescriptors = WarpDescriptor("Integer") :: WarpDescriptor(classOf[Int]) :: Nil
  override def pack(what: Int)(implicit packers: WarpPackers): AlmValidation[WarpInt] = WarpInt(what).success
}

object IntWarpUnpacker extends RegisterableWarpUnpacker[Int] {
  override val warpDescriptor = WarpDescriptor("Int")
  override val alternativeWarpDescriptors = WarpDescriptor(classOf[Int]) :: Nil
  override def unpack(what: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[Int] = 
    what match {
    case WarpInt(v) => v.success
    case x => UnspecifiedProblem(s""""${x.getClass().getName()}" can not unpack to an Int""").failure
  }
}

object LongWarpPacker extends WarpPacker[Long] with SimpleWarpPacker[Long] with RegisterableWarpPacker {
  override val warpDescriptor = WarpDescriptor("Long")
  override val alternativeWarpDescriptors = WarpDescriptor(classOf[Long]) :: Nil
  override def pack(what: Long)(implicit packers: WarpPackers): AlmValidation[WarpLong] = WarpLong(what).success
}

object LongWarpUnpacker extends RegisterableWarpUnpacker[Long] {
  override val warpDescriptor = WarpDescriptor("Long")
  override val alternativeWarpDescriptors = WarpDescriptor(classOf[Long]) :: Nil
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
  override val alternativeWarpDescriptors = WarpDescriptor(classOf[Float]) :: Nil
  override def pack(what: Float)(implicit packers: WarpPackers): AlmValidation[WarpFloat] = WarpFloat(what).success
}

object FloatWarpUnpacker extends RegisterableWarpUnpacker[Float] {
  override val warpDescriptor = WarpDescriptor("Float")
  override val alternativeWarpDescriptors = WarpDescriptor(classOf[Float]) :: Nil
  override def unpack(what: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[Float] = 
    what match {
    case WarpFloat(v) => v.success
    case x => UnspecifiedProblem(s""""${x.getClass().getName()}" can not unpack to a Float""").failure
  }
}

object DoubleWarpPacker extends WarpPacker[Double] with SimpleWarpPacker[Double] with RegisterableWarpPacker {
  override val warpDescriptor = WarpDescriptor("Double")
  override val alternativeWarpDescriptors = WarpDescriptor(classOf[Double]) :: Nil
  override def pack(what: Double)(implicit packers: WarpPackers): AlmValidation[WarpDouble] = WarpDouble(what).success
}

object DoubleWarpUnpacker extends RegisterableWarpUnpacker[Double] {
  override val warpDescriptor = WarpDescriptor("Double")
  override val alternativeWarpDescriptors = WarpDescriptor(classOf[Double]) :: Nil
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

object ByteArrayWarpPacker extends WarpPacker[Array[Byte]] with SimpleWarpPacker[Array[Byte]] with RegisterableWarpPacker {
  override val warpDescriptor = WarpDescriptor("Bytes")
  override val alternativeWarpDescriptors = Nil
  override def pack(what: Array[Byte])(implicit packers: WarpPackers): AlmValidation[WarpBytes] = WarpBytes(what).success
}

object ByteArrayWarpUnpacker extends RegisterableWarpUnpacker[Array[Byte]] {
  override val warpDescriptor = WarpDescriptor("Bytes")
  override val alternativeWarpDescriptors = Nil
  override def unpack(what: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[Array[Byte]] = 
    what match {
    case WarpBytes(v) => v.success
    case x => UnspecifiedProblem(s""""${x.getClass().getName()}" can not unpack to an Array[Byte]""").failure
  }
}

object Base64WarpPacker extends WarpPacker[Array[Byte]] with SimpleWarpPacker[Array[Byte]] with RegisterableWarpPacker {
  override val warpDescriptor = WarpDescriptor("Base64")
  override val alternativeWarpDescriptors = Nil
  override def pack(what: Array[Byte])(implicit packers: WarpPackers): AlmValidation[WarpBase64] = WarpBase64(what).success
}

object Base64WarpUnpacker extends RegisterableWarpUnpacker[Array[Byte]] {
  override val warpDescriptor = WarpDescriptor("Base64")
  override val alternativeWarpDescriptors = Nil
  override def unpack(what: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[Array[Byte]] = 
    what match {
    case WarpBase64(v) => v.success
    case x => UnspecifiedProblem(s""""${x.getClass().getName()}" can not unpack to an Array[Byte]""").failure
  }
}

