package riftwarp.std

import java.net.URI
import java.util.{ UUID => JUUID }
import org.joda.time.DateTime
import scalaz.syntax.validation._
import almhirt.common.AlmValidation
import riftwarp._

object BooleanWarpPacker extends WarpPacker[Boolean] with SimpleWarpPacker[Boolean] with RegisterableWarpPacker {
  override val riftDescriptor = RiftDescriptor("Boolean")
  override val alternateDescriptors = RiftDescriptor(classOf[Boolean]) :: Nil
  override def packSimple(what: Boolean): AlmValidation[WarpBoolean] = WarpBoolean(what).success
}

object StringWarpPacker extends WarpPacker[String] with SimpleWarpPacker[String] with RegisterableWarpPacker {
  override val riftDescriptor = RiftDescriptor("String")
  override val alternateDescriptors = RiftDescriptor(classOf[String]) :: Nil
  override def packSimple(what: String): AlmValidation[WarpString] = WarpString(what).success
}

object ByteWarpPacker extends WarpPacker[Byte] with SimpleWarpPacker[Byte] with RegisterableWarpPacker {
  override val riftDescriptor = RiftDescriptor("Byte")
  override val alternateDescriptors = RiftDescriptor(classOf[Byte]) :: Nil
  override def packSimple(what: Byte): AlmValidation[WarpByte] = WarpByte(what).success
}

object IntWarpPacker extends WarpPacker[Int] with SimpleWarpPacker[Int] with RegisterableWarpPacker {
  override val riftDescriptor = RiftDescriptor("Int")
  override val alternateDescriptors = RiftDescriptor("Integer") :: RiftDescriptor(classOf[Int]) :: Nil
  override def packSimple(what: Int): AlmValidation[WarpInt] = WarpInt(what).success
}

object LongWarpPacker extends WarpPacker[Long] with SimpleWarpPacker[Long] with RegisterableWarpPacker {
  override val riftDescriptor = RiftDescriptor("Long")
  override val alternateDescriptors = RiftDescriptor(classOf[Long]) :: Nil
  override def packSimple(what: Long): AlmValidation[WarpLong] = WarpLong(what).success
}

object BigIntWarpPacker extends WarpPacker[BigInt] with SimpleWarpPacker[BigInt] with RegisterableWarpPacker {
  override val riftDescriptor = RiftDescriptor("BigInt")
  override val alternateDescriptors = RiftDescriptor(classOf[BigInt]) :: Nil
  override def packSimple(what: BigInt): AlmValidation[WarpBigInt] = WarpBigInt(what).success
}

object FloatWarpPacker extends WarpPacker[Float] with SimpleWarpPacker[Float] with RegisterableWarpPacker {
  override val riftDescriptor = RiftDescriptor("Float")
  override val alternateDescriptors = RiftDescriptor(classOf[Float]) :: Nil
  override def packSimple(what: Float): AlmValidation[WarpFloat] = WarpFloat(what).success
}

object DoubleWarpPacker extends WarpPacker[Double] with SimpleWarpPacker[Double] with RegisterableWarpPacker {
  override val riftDescriptor = RiftDescriptor("Double")
  override val alternateDescriptors = RiftDescriptor(classOf[Double]) :: Nil
  override def packSimple(what: Double): AlmValidation[WarpDouble] = WarpDouble(what).success
}

object BigDecimalWarpPacker extends WarpPacker[BigDecimal] with SimpleWarpPacker[BigDecimal] with RegisterableWarpPacker {
  override val riftDescriptor = RiftDescriptor("BigDecimal")
  override val alternateDescriptors = RiftDescriptor(classOf[BigDecimal]) :: Nil
  override def packSimple(what: BigDecimal): AlmValidation[WarpBigDecimal] = WarpBigDecimal(what).success
}

object UuidWarpPacker extends WarpPacker[JUUID] with SimpleWarpPacker[JUUID] with RegisterableWarpPacker {
  override val riftDescriptor = RiftDescriptor("UUID")
  override val alternateDescriptors = RiftDescriptor(classOf[JUUID]) :: Nil
  override def packSimple(what: JUUID): AlmValidation[WarpUuid] = WarpUuid(what).success
}

object UriWarpPacker extends WarpPacker[URI] with SimpleWarpPacker[URI] with RegisterableWarpPacker {
  override val riftDescriptor = RiftDescriptor("URI")
  override val alternateDescriptors = RiftDescriptor(classOf[URI]) :: Nil
  override def packSimple(what: URI): AlmValidation[WarpUri] = WarpUri(what).success
}

object DateTimeWarpPacker extends WarpPacker[DateTime] with SimpleWarpPacker[DateTime] with RegisterableWarpPacker {
  override val riftDescriptor = RiftDescriptor("DateTime")
  override val alternateDescriptors = RiftDescriptor(classOf[DateTime]) :: Nil
  override def packSimple(what: DateTime): AlmValidation[WarpDateTime] = WarpDateTime(what).success
}

object ByteArrayWarpPacker extends WarpPacker[Array[Byte]] with SimpleWarpPacker[Array[Byte]] with RegisterableWarpPacker {
  override val riftDescriptor = RiftDescriptor("Bytes")
  override val alternateDescriptors = Nil
  override def packSimple(what: Array[Byte]): AlmValidation[WarpBytes] = WarpBytes(what).success
}

object Base64WarpPacker extends WarpPacker[Array[Byte]] with SimpleWarpPacker[Array[Byte]] with RegisterableWarpPacker {
  override val riftDescriptor = RiftDescriptor("Base64")
  override val alternateDescriptors = RiftDescriptor(classOf[Boolean]) :: Nil
  override def packSimple(what: Array[Byte]): AlmValidation[WarpBase64] = WarpBase64(what).success
}

