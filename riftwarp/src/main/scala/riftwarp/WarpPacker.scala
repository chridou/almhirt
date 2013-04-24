package riftwarp

import java.util.{ UUID => JUUID }
import org.joda.time.DateTime

object WarpPacker {
  def packElement(element: WarpElement, into: WarpObject): WarpObject = into.copy(elements = into.elements :+ element)

  def packBooleanElement(label: String, value: Boolean, into: WarpObject): WarpObject = packElement(WarpElement(label, Some(WarpBoolean(value))), into)
  def packOptionalBooleanElement(label: String, value: Option[Boolean], into: WarpObject): WarpObject = packElement(WarpElement(label, value.map(WarpBoolean(_))), into)
  def packStringElement(label: String, value: String, into: WarpObject): WarpObject = packElement(WarpElement(label, Some(WarpString(value))), into)
  def packOptionalStringElement(label: String, value: Option[String], into: WarpObject): WarpObject = packElement(WarpElement(label, value.map(WarpString(_))), into)
  def packByteElement(label: String, value: Byte, into: WarpObject): WarpObject = packElement(WarpElement(label, Some(WarpByte(value))), into)
  def packOptionalByteElement(label: String, value: Option[Byte], into: WarpObject): WarpObject = packElement(WarpElement(label, value.map(WarpByte(_))), into)
  def packIntElement(label: String, value: Int, into: WarpObject): WarpObject = packElement(WarpElement(label, Some(WarpInt(value))), into)
  def packOptionalIntElement(label: String, value: Option[Int], into: WarpObject): WarpObject = packElement(WarpElement(label, value.map(WarpInt(_))), into)
  def packLongElement(label: String, value: Long, into: WarpObject): WarpObject = packElement(WarpElement(label, Some(WarpLong(value))), into)
  def packOptionalLongElement(label: String, value: Option[Long], into: WarpObject): WarpObject = packElement(WarpElement(label, value.map(WarpLong(_))), into)
  def packBigIntElement(label: String, value: BigInt, into: WarpObject): WarpObject = packElement(WarpElement(label, Some(WarpBigInt(value))), into)
  def packOptionalBigIntElement(label: String, value: Option[BigInt], into: WarpObject): WarpObject = packElement(WarpElement(label, value.map(WarpBigInt(_))), into)
  def packFloatElement(label: String, value: Float, into: WarpObject): WarpObject = packElement(WarpElement(label, Some(WarpFloat(value))), into)
  def packOptionalFloatElement(label: String, value: Option[Float], into: WarpObject): WarpObject = packElement(WarpElement(label, value.map(WarpFloat(_))), into)
  def packDoubleElement(label: String, value: Double, into: WarpObject): WarpObject = packElement(WarpElement(label, Some(WarpDouble(value))), into)
  def packOptionalDoubleElement(label: String, value: Option[Double], into: WarpObject): WarpObject = packElement(WarpElement(label, value.map(WarpDouble(_))), into)
  def packBigDecimalElement(label: String, value: BigDecimal, into: WarpObject): WarpObject = packElement(WarpElement(label, Some(WarpBigDecimal(value))), into)
  def packOptionalBigDecimalElement(label: String, value: Option[BigDecimal], into: WarpObject): WarpObject = packElement(WarpElement(label, value.map(WarpBigDecimal(_))), into)
  def packUuidElement(label: String, value: JUUID, into: WarpObject): WarpObject = packElement(WarpElement(label, Some(WarpUuid(value))), into)
  def packOptionalUuidElement(label: String, value: Option[JUUID], into: WarpObject): WarpObject = packElement(WarpElement(label, value.map(WarpUuid(_))), into)
  def packUriElement(label: String, value: java.net.URI, into: WarpObject): WarpObject = packElement(WarpElement(label, Some(WarpUri(value))), into)
  def packOptionalUriElement(label: String, value: Option[java.net.URI], into: WarpObject): WarpObject = packElement(WarpElement(label, value.map(WarpUri(_))), into)
  def packDateTimeElement(label: String, value: DateTime, into: WarpObject): WarpObject = packElement(WarpElement(label, Some(WarpDateTime(value))), into)
  def packOptionalDateTimeElement(label: String, value: Option[DateTime], into: WarpObject): WarpObject = packElement(WarpElement(label, value.map(WarpDateTime(_))), into)
}