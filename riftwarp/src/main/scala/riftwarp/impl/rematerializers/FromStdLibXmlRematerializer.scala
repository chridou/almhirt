package riftwarp.impl.rematerializers

import scala.reflect.ClassTag
import scala.collection.generic.CanBuildFrom
import scala.xml.{ Elem => XmlElem, NodeSeq }
import scalaz._
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.xml.all._
import riftwarp._

private[rematerializers] object FromStdLibXmlRematerializerFuns {
  def valueMapperFromTag[A](implicit tag: ClassTag[A]): AlmValidation[XmlElem => AlmValidation[A]] = {
    val clazz = tag.runtimeClass
    if (clazz == classOf[String])
      Success(((x: XmlElem) => x.text.success).asInstanceOf[XmlElem => AlmValidation[A]])
    else if (clazz == classOf[_root_.java.lang.String])
      Success(((x: XmlElem) => x.text.success).asInstanceOf[XmlElem => AlmValidation[A]])
    else if (clazz == classOf[Boolean])
      Success(((x: XmlElem) => x.text.toBooleanAlm).asInstanceOf[XmlElem => AlmValidation[A]])
    else if (clazz == classOf[_root_.java.lang.Boolean])
      Success(((x: XmlElem) => x.text.toBooleanAlm).asInstanceOf[XmlElem => AlmValidation[A]])
    else if (clazz == classOf[Byte])
      Success(((x: XmlElem) => x.text.toByteAlm).asInstanceOf[XmlElem => AlmValidation[A]])
    else if (clazz == classOf[_root_.java.lang.Byte])
      Success(((x: XmlElem) => x.text.toByteAlm).asInstanceOf[XmlElem => AlmValidation[A]])
    else if (clazz == classOf[Int])
      Success(((x: XmlElem) => x.text.toIntAlm).asInstanceOf[XmlElem => AlmValidation[A]])
    else if (clazz == classOf[_root_.java.lang.Integer])
      Success(((x: XmlElem) => x.text.toIntAlm).asInstanceOf[XmlElem => AlmValidation[A]])
    else if (clazz == classOf[Long])
      Success(((x: XmlElem) => x.text.toLongAlm).asInstanceOf[XmlElem => AlmValidation[A]])
    else if (clazz == classOf[_root_.java.lang.Long])
      Success(((x: XmlElem) => x.text.toLongAlm).asInstanceOf[XmlElem => AlmValidation[A]])
    else if (clazz == classOf[BigInt])
      Success(((x: XmlElem) => x.text.toBigIntAlm).asInstanceOf[XmlElem => AlmValidation[A]])
    else if (clazz == classOf[Float])
      Success(((x: XmlElem) => x.text.toFloatAlm).asInstanceOf[XmlElem => AlmValidation[A]])
    else if (clazz == classOf[_root_.java.lang.Float])
      Success(((x: XmlElem) => x.text.toFloatAlm).asInstanceOf[XmlElem => AlmValidation[A]])
    else if (clazz == classOf[Double])
      Success(((x: XmlElem) => x.text.toDoubleAlm).asInstanceOf[XmlElem => AlmValidation[A]])
    else if (clazz == classOf[_root_.java.lang.Double])
      Success(((x: XmlElem) => x.text.toDoubleAlm).asInstanceOf[XmlElem => AlmValidation[A]])
    else if (clazz == classOf[BigDecimal])
      Success(((x: XmlElem) => x.text.toDecimalAlm).asInstanceOf[XmlElem => AlmValidation[A]])
    else if (clazz == classOf[org.joda.time.DateTime])
      Success(((x: XmlElem) => x.text.toDateTimeAlm).asInstanceOf[XmlElem => AlmValidation[A]])
    else if (clazz == classOf[_root_.java.util.UUID])
      Success(((x: XmlElem) => x.text.toUuidAlm).asInstanceOf[XmlElem => AlmValidation[A]])
    else if (clazz == classOf[_root_.java.net.URI])
      Success(((x: XmlElem) => x.text.toUriAlm).asInstanceOf[XmlElem => AlmValidation[A]])
    else
      Failure(UnspecifiedProblem("No primitive rematerializer found for '%s'".format(clazz.getName())))
  }

  def isPrimitive(value: XmlElem): Boolean =
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
}

//object FromStdLibXmlRematerializer extends RematerializerTemplate[DimensionXmlElem] {
//  override def valueMapperFromTag[T](implicit tag: ClassTag[T]): AlmValidation[ValueRepr => AlmValidation[T]] = FromStdLibJsonRematerializerFuns.valueMapperFromTag
//  override def primitiveFromValue(value: ValueRepr): AlmValidation[Any] = 
//    if(FromStdLibJsonRematerializerFuns.isPrimitive(value))
//      value.success
//    else
//      UnspecifiedProblem("Not a primitive type").failure
//
//  override def isPrimitive(value: ValueRepr): Boolean = FromStdLibJsonRematerializerFuns.isPrimitive(value)
//
//  override def stringFromRepr(value: ValueRepr): AlmValidation[String] = almCast[String](value)
//  override def booleanFromRepr(value: ValueRepr): AlmValidation[Boolean] = almCast[Boolean](value)
//  override def byteFromRepr(value: ValueRepr): AlmValidation[Byte] = almCast[Double](value).map(_.toByte)
//  override def intFromRepr(value: ValueRepr): AlmValidation[Int] = almCast[Double](value).map(_.toInt)
//  override def longFromRepr(value: ValueRepr): AlmValidation[Long] = almCast[Double](value).map(_.toLong)
//  override def bigIntFromRepr(value: ValueRepr): AlmValidation[BigInt] = almCast[String](value).flatMap(parseBigIntAlm(_))
//  override def floatFromRepr(value: ValueRepr): AlmValidation[Float] = almCast[Double](value).map(_.toFloat)
//  override def doubleFromRepr(value: ValueRepr): AlmValidation[Double] = almCast[Double](value)
//  override def bigDecimalFromRepr(value: ValueRepr): AlmValidation[BigDecimal] = almCast[String](value).flatMap(parseDecimalAlm(_))
//  override def byteArrayFromRepr(value: ValueRepr): AlmValidation[Array[Byte]] =
//    almCast[List[Double]](value).map(x => x.toArray.map(_.toByte))
//  override def byteArrayFromBase64Repr(value: ValueRepr): AlmValidation[Array[Byte]] =
//    almCast[String](value).flatMap(ParseFuns.parseBase64Alm(_))
//  override def byteArrayFromBlobRepr(value: ValueRepr): AlmValidation[Array[Byte]] =
//    byteArrayFromBase64Repr(value)
//  override def dateTimeFromRepr(value: ValueRepr): AlmValidation[org.joda.time.DateTime] = almCast[String](value).flatMap(parseDateTimeAlm(_))
//  override def uriFromRepr(value: ValueRepr): AlmValidation[_root_.java.net.URI] = almCast[String](value).flatMap(parseUriAlm(_))
//  override def uuidFromRepr(value: ValueRepr): AlmValidation[_root_.java.util.UUID] = almCast[String](value).flatMap(parseUuidAlm(_))
//
//  override def traversableOfReprFromRepr(value: ValueRepr): AlmValidation[Iterable[ValueRepr]] = almCast[List[ValueRepr]](value)
//  override def tuple2OfReprFromRepr(value: ValueRepr): AlmValidation[(ValueRepr, ValueRepr)] =
//    traversableOfReprFromRepr(value).flatMap { reprItems =>
//      (reprItems.headOption, reprItems.tail.headOption) match {
//        case (Some(a), Some(b)) => (a, b).success
//        case _ => NoSuchElementProblem("Not enough items to build a tuple").failure
//      }
//    }
//
//}