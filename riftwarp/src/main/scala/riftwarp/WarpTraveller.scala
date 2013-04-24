package riftwarp

trait WarpTraveller

trait WarpValue extends WarpTraveller
final case class WarpElement(label: String, value: Option[WarpValue]) extends WarpTraveller

trait WarpPrimitive extends WarpValue
final case class WarpBoolean(value: Boolean) extends WarpPrimitive
final case class WarpString(value: String) extends WarpPrimitive
final case class WarpByte(value: WarpByte) extends WarpPrimitive
final case class WarpInt(value: Int) extends WarpPrimitive
final case class WarpLong(value: Long) extends WarpPrimitive
final case class WarpBigInt(value: BigInt) extends WarpPrimitive
final case class WarpFloat(value: Float) extends WarpPrimitive
final case class WarpDouble(value: Double) extends WarpPrimitive
final case class WarpBigDecimal(value: BigDecimal) extends WarpPrimitive
final case class WarpUuid(value: java.util.UUID) extends WarpPrimitive
final case class WarpUri(value: java.net.URI) extends WarpPrimitive
final case class DateTime(value: org.joda.time.DateTime) extends WarpPrimitive

final case class WarpObject(riftDescriptor: Option[RiftDescriptor], elements: Vector[WarpElement]) extends WarpValue
final case class WarpCollection(items: Vector[WarpValue]) extends WarpValue
final case class WarpTree(tree: scalaz.Tree[WarpValue]) extends WarpValue
final case class WarpAssociativeCollection(items: Vector[(WarpPrimitive, WarpValue)]) extends WarpValue
final case class WarpBase64(bytes: Array[Byte]) extends WarpValue
final case class WarpCompressed(bytes: Array[Byte]) extends WarpValue
final case class WarpByteArray(bytes: Array[Byte]) extends WarpValue


