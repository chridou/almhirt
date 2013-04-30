package riftwarp

import scalaz.syntax.validation._
import almhirt.common._
import riftwarp.std.WarpPrimitiveConverter

sealed trait WarpPackage {
  def asWarpObject: AlmValidation[WarpObject] =
    this match {
    case wo: WarpObject => wo.success
    case _ => UnspecifiedProblem("No WarpObject").failure
  }
}

final case class WarpElement(label: String, value: Option[WarpPackage])

sealed trait WarpPrimitive extends WarpPackage { def as[T](implicit conv: WarpPrimitiveConverter[T]): AlmValidation[T] = conv.convert(this) }
final case class WarpBoolean(value: Boolean) extends WarpPrimitive
final case class WarpString(value: String) extends WarpPrimitive
final case class WarpByte(value: Byte) extends WarpPrimitive
final case class WarpInt(value: Int) extends WarpPrimitive
final case class WarpLong(value: Long) extends WarpPrimitive
final case class WarpBigInt(value: BigInt) extends WarpPrimitive
final case class WarpFloat(value: Float) extends WarpPrimitive
final case class WarpDouble(value: Double) extends WarpPrimitive
final case class WarpBigDecimal(value: BigDecimal) extends WarpPrimitive
final case class WarpUuid(value: java.util.UUID) extends WarpPrimitive
final case class WarpUri(value: java.net.URI) extends WarpPrimitive
final case class WarpDateTime(value: org.joda.time.DateTime) extends WarpPrimitive

final case class WarpObject(riftDescriptor: Option[RiftDescriptor], elements: Vector[WarpElement]) extends WarpPackage {
  def getRiftDescriptor: AlmValidation[RiftDescriptor] =
    riftDescriptor match {
    case Some(rd) => rd.success
    case None => NoSuchElementProblem("Object has no RiftDescriptor").failure
  }
}

final case class WarpCollection(items: Vector[WarpPackage]) extends WarpPackage
final case class WarpAssociativeCollection(items: Vector[(WarpPackage, WarpPackage)]) extends WarpPackage
final case class WarpTree(tree: scalaz.Tree[WarpPackage]) extends WarpPackage

sealed trait BinaryWarpPackage extends WarpPackage
final case class WarpBytes(bytes: Array[Byte]) extends BinaryWarpPackage
final case class WarpBase64(bytes: Array[Byte]) extends BinaryWarpPackage
final case class WarpBlob(bytes: Array[Byte]) extends BinaryWarpPackage

object WarpElement {
  def apply(label: String): WarpElement = WarpElement(label, None)
}

object WarpObject {
  def apply(): WarpObject = WarpObject(None, Vector.empty)
  def apply(elements: Vector[WarpElement]): WarpObject = WarpObject(None, elements)
  def apply(riftDescriptor: RiftDescriptor): WarpObject = WarpObject(Some(riftDescriptor), Vector.empty)
  def apply(elements: Vector[WarpElement], riftDescriptor: RiftDescriptor): WarpObject = WarpObject(Some(riftDescriptor), elements)
}

object WarpCollection {
  def apply(): WarpCollection = WarpCollection(Vector.empty)
}

object WarpAssociativeCollection {
  def apply(): WarpAssociativeCollection = WarpAssociativeCollection(Vector.empty)
}

object WarpPackage extends WarpPackageFuns


