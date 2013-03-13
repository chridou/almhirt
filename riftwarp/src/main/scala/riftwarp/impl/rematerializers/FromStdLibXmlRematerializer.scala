package riftwarp.impl.rematerializers

import java.util.{ UUID => JUUID }
import java.net.{ URI => JURI }
import scala.reflect.ClassTag
import scala.collection.generic.CanBuildFrom
import scala.xml.{ Elem => XmlElem, NodeSeq }
import org.joda.time.DateTime
import scalaz._
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.xml.all._
import riftwarp._

private[rematerializers] object FromStdLibXmlRematerializerFuns {

  def extractString(value: XmlElem): AlmValidation[String] = value.text.success
  def extractBoolean(value: XmlElem): AlmValidation[Boolean] = value.extractBoolean
  def extractByte(value: XmlElem): AlmValidation[Byte] = value.extractByte
  def extractInt(value: XmlElem): AlmValidation[Int] = value.extractInt
  def extractLong(value: XmlElem): AlmValidation[Long] = value.extractLong
  def extractBigInt(value: XmlElem): AlmValidation[BigInt] = value.extractBigInt
  def extractFloat(value: XmlElem): AlmValidation[Float] = value.extractFloat
  def extractDouble(value: XmlElem): AlmValidation[Double] = value.extractDouble
  def extractBigDecimal(value: XmlElem): AlmValidation[BigDecimal] = value.extractDecimal
  def extractDateTime(value: XmlElem): AlmValidation[DateTime] = value.extractDateTime
  def extractUuid(value: XmlElem): AlmValidation[JUUID] = value.extractUuid
  def extractUri(value: XmlElem): AlmValidation[JURI] = value.extractUri

  def valueMapperFromTag[A](implicit tag: ClassTag[A]): AlmValidation[XmlElem => AlmValidation[A]] = {
    val clazz = tag.runtimeClass
    if (clazz == classOf[String])
      Success((extractString _).asInstanceOf[XmlElem => AlmValidation[A]])
    else if (clazz == classOf[_root_.java.lang.String])
      Success((extractString _).asInstanceOf[XmlElem => AlmValidation[A]])
    else if (clazz == classOf[Boolean])
      Success((extractBoolean _).asInstanceOf[XmlElem => AlmValidation[A]])
    else if (clazz == classOf[_root_.java.lang.Boolean])
      Success((extractBoolean _).asInstanceOf[XmlElem => AlmValidation[A]])
    else if (clazz == classOf[Byte])
      Success((extractByte _).asInstanceOf[XmlElem => AlmValidation[A]])
    else if (clazz == classOf[_root_.java.lang.Byte])
      Success((extractByte _).asInstanceOf[XmlElem => AlmValidation[A]])
    else if (clazz == classOf[Int])
      Success((extractInt _).asInstanceOf[XmlElem => AlmValidation[A]])
    else if (clazz == classOf[_root_.java.lang.Integer])
      Success((extractInt _).asInstanceOf[XmlElem => AlmValidation[A]])
    else if (clazz == classOf[Long])
      Success((extractLong _).asInstanceOf[XmlElem => AlmValidation[A]])
    else if (clazz == classOf[_root_.java.lang.Long])
      Success((extractLong _).asInstanceOf[XmlElem => AlmValidation[A]])
    else if (clazz == classOf[BigInt])
      Success((extractBigInt _).asInstanceOf[XmlElem => AlmValidation[A]])
    else if (clazz == classOf[Float])
      Success((extractFloat _).asInstanceOf[XmlElem => AlmValidation[A]])
    else if (clazz == classOf[_root_.java.lang.Float])
      Success((extractFloat _).asInstanceOf[XmlElem => AlmValidation[A]])
    else if (clazz == classOf[Double])
      Success((extractDouble _).asInstanceOf[XmlElem => AlmValidation[A]])
    else if (clazz == classOf[_root_.java.lang.Double])
      Success((extractDouble _).asInstanceOf[XmlElem => AlmValidation[A]])
    else if (clazz == classOf[BigDecimal])
      Success((extractBigDecimal _).asInstanceOf[XmlElem => AlmValidation[A]])
    else if (clazz == classOf[org.joda.time.DateTime])
      Success((extractDateTime _).asInstanceOf[XmlElem => AlmValidation[A]])
    else if (clazz == classOf[_root_.java.util.UUID])
      Success((extractUuid _).asInstanceOf[XmlElem => AlmValidation[A]])
    else if (clazz == classOf[_root_.java.net.URI])
      Success((extractUri _).asInstanceOf[XmlElem => AlmValidation[A]])
    else
      Failure(UnspecifiedProblem("No primitive rematerializer found for '%s'".format(clazz.getName())))
  }

  def isPrimitiveValue(value: XmlElem): Boolean =
    (value \@? "type") match {
      case Some(t) =>
        t == "String" ||
          t == "Boolean" ||
          t == "Byte" ||
          t == "Int" ||
          t == "Long" ||
          t == "BigInt" ||
          t == "Float" ||
          t == "Double" ||
          t == "BigDecimal" ||
          t == "DateTime" ||
          t == "Uuid" ||
          t == "Uri"
      case None => false
    }

  def extractPrimitiveFromElem(value: XmlElem): AlmValidation[Any] =
    (value \@ "type").fold(
      fail => BadDataProblem(s"Could not extract a primitive from an XmlElem because it lacks an attribute 'type' which specifies the contained data type. The element was: ${value.label}").failure,
      succ => succ match {
        case "String" => extractString(value)
        case "Boolean" => extractBoolean(value)
        case "Byte" => extractByte(value)
        case "Int" => extractInt(value)
        case "Long" => extractLong(value)
        case "BigInt" => extractBigInt(value)
        case "Float" => extractFloat(value)
        case "Double" => extractDouble(value)
        case "BigDecimal" => extractBigDecimal(value)
        case "DateTime" => extractDateTime(value)
        case "Uuid" => extractUuid(value)
        case "Uri" => extractUri(value)
        case x => BadDataProblem(s"Could not extract a primitive from an XmlElem because its attribute 'type' specifies an unknown data type '$x'. The element was: ${value.label}").failure
      })

}

object FromStdLibXmlRematerializer extends RematerializerTemplate[DimensionXmlElem] {
  import FromStdLibXmlRematerializerFuns._
  override def valueMapperFromTag[T](implicit tag: ClassTag[T]): AlmValidation[XmlElem => AlmValidation[T]] = valueMapperFromTag
  override def isPrimitive(value: XmlElem): Boolean = isPrimitiveValue(value)
  override def primitiveFromValue(value: XmlElem): AlmValidation[Any] = extractPrimitiveFromElem(value)


  override def stringFromRepr(value: XmlElem): AlmValidation[String] = extractString(value)
  override def booleanFromRepr(value: XmlElem): AlmValidation[Boolean] = extractBoolean(value)
  override def byteFromRepr(value: XmlElem): AlmValidation[Byte] = extractByte(value)
  override def intFromRepr(value: XmlElem): AlmValidation[Int] = extractInt(value)
  override def longFromRepr(value: XmlElem): AlmValidation[Long] = extractLong(value)
  override def bigIntFromRepr(value: XmlElem): AlmValidation[BigInt] = extractBigInt(value)
  override def floatFromRepr(value: XmlElem): AlmValidation[Float] = extractFloat(value)
  override def doubleFromRepr(value: XmlElem): AlmValidation[Double] = extractDouble(value)
  override def bigDecimalFromRepr(value: XmlElem): AlmValidation[BigDecimal] = extractBigDecimal(value)
  override def byteArrayFromRepr(value: XmlElem): AlmValidation[Array[Byte]] =
    almCast[List[Double]](value).map(x => x.toArray.map(_.toByte))
  override def byteArrayFromBase64Repr(value: XmlElem): AlmValidation[Array[Byte]] =
    almCast[String](value).flatMap(ParseFuns.parseBase64Alm(_))
  override def byteArrayFromBlobRepr(value: XmlElem): AlmValidation[Array[Byte]] =
    byteArrayFromBase64Repr(value)
  override def dateTimeFromRepr(value: XmlElem): AlmValidation[org.joda.time.DateTime] = extractDateTime(value)
  override def uriFromRepr(value: XmlElem): AlmValidation[_root_.java.net.URI] = extractUri(value)
  override def uuidFromRepr(value: XmlElem): AlmValidation[_root_.java.util.UUID] = extractUuid(value)

  override def traversableOfReprFromRepr(value: XmlElem): AlmValidation[Iterable[XmlElem]] = value.elems.success
  override def tuple2OfReprFromRepr(value: XmlElem): AlmValidation[(XmlElem, XmlElem)] =
    traversableOfReprFromRepr(value).flatMap { reprItems =>
      (reprItems.headOption, reprItems.tail.headOption) match {
        case (Some(a), Some(b)) => (a, b).success
        case _ => NoSuchElementProblem("Not enough items to build a tuple").failure
      }
    }

}