package riftwarp.impl.rematerializers

import scala.reflect.ClassTag
import scala.collection.generic.CanBuildFrom
import scalaz._
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.almvalidation.funs._
import riftwarp._

private[rematerializers] object FromStdLibJsonRematerializerFuns {
  def valueMapperFromTag[A](implicit tag: ClassTag[A]): AlmValidation[Any => AlmValidation[A]] = {
    val clazz = tag.runtimeClass
    if (clazz == classOf[String])
      Success(((x: Any) => almCast[String](x)).asInstanceOf[Any => AlmValidation[A]])
    else if (clazz == classOf[_root_.java.lang.String])
      Success(((x: Any) => almCast[_root_.java.lang.String](x)).asInstanceOf[Any => AlmValidation[A]])
    else if (clazz == classOf[Boolean])
      Success(((x: Any) => almCast[Boolean](x)).asInstanceOf[Any => AlmValidation[A]])
    else if (clazz == classOf[_root_.java.lang.Boolean])
      Success(((x: Any) => almCast[_root_.java.lang.Boolean](x)).asInstanceOf[Any => AlmValidation[A]])
    else if (clazz == classOf[Byte])
      Success(((x: Any) => almCast[Double](x).map(_.toByte)).asInstanceOf[Any => AlmValidation[A]])
    else if (clazz == classOf[_root_.java.lang.Byte])
      Success(((x: Any) => almCast[Double](x).map(_.toByte)).asInstanceOf[Any => AlmValidation[A]])
    else if (clazz == classOf[Int])
      Success(((x: Any) => almCast[Double](x).map(_.toInt)).asInstanceOf[Any => AlmValidation[A]])
    else if (clazz == classOf[_root_.java.lang.Integer])
      Success(((x: Any) => almCast[Double](x).map(_.toInt)).asInstanceOf[Any => AlmValidation[A]])
    else if (clazz == classOf[Long])
      Success(((x: Any) => almCast[Double](x).map(_.toLong)).asInstanceOf[Any => AlmValidation[A]])
    else if (clazz == classOf[_root_.java.lang.Long])
      Success(((x: Any) => almCast[Double](x).map(_.toLong)).asInstanceOf[Any => AlmValidation[A]])
    else if (clazz == classOf[BigInt])
      Success(((x: Any) => almCast[String](x).flatMap(parseBigIntAlm(_))).asInstanceOf[Any => AlmValidation[A]])
    else if (clazz == classOf[Float])
      Success(((x: Any) => almCast[Double](x).map(_.toFloat)).asInstanceOf[Any => AlmValidation[A]])
    else if (clazz == classOf[_root_.java.lang.Float])
      Success(((x: Any) => almCast[Double](x).map(_.toFloat)).asInstanceOf[Any => AlmValidation[A]])
    else if (clazz == classOf[Double])
      Success(((x: Any) => almCast[Double](x)).asInstanceOf[Any => AlmValidation[A]])
    else if (clazz == classOf[_root_.java.lang.Double])
      Success(((x: Any) => almCast[Double](x)).asInstanceOf[Any => AlmValidation[A]])
    else if (clazz == classOf[BigDecimal])
      Success(((x: Any) => almCast[String](x).flatMap(parseDecimalAlm(_))).asInstanceOf[Any => AlmValidation[A]])
    else if (clazz == classOf[org.joda.time.DateTime])
      Success(((x: Any) => almCast[String](x).flatMap(parseDateTimeAlm(_))).asInstanceOf[Any => AlmValidation[A]])
    else if (clazz == classOf[_root_.java.util.UUID])
      Success(((x: Any) => almCast[String](x).flatMap(parseUuidAlm(_))).asInstanceOf[Any => AlmValidation[A]])
    else if (clazz == classOf[_root_.java.net.URI])
      Success(((x: Any) => almCast[String](x).flatMap(parseUriAlm(_))).asInstanceOf[Any => AlmValidation[A]])
    else
      Failure(UnspecifiedProblem("No primitive rematerializer found for '%s'".format(clazz.getName())))
  }

  def isPrimitive(value: Any): Boolean =
    (value.isInstanceOf[Boolean] ||
      value.isInstanceOf[String] ||
      value.isInstanceOf[Byte] ||
      value.isInstanceOf[Int] ||
      value.isInstanceOf[Long] ||
      value.isInstanceOf[BigInt] ||
      value.isInstanceOf[Float] ||
      value.isInstanceOf[Double] ||
      value.isInstanceOf[org.joda.time.DateTime] ||
      value.isInstanceOf[_root_.java.net.URI] ||
      value.isInstanceOf[_root_.java.util.UUID])
}

object FromStdLibJsonRematerializer extends RematerializerTemplate[DimensionStdLibJson] {
  override def valueMapperFromTag[T](implicit tag: ClassTag[T]): AlmValidation[ValueRepr => AlmValidation[T]] = FromStdLibJsonRematerializerFuns.valueMapperFromTag
  override def primitiveFromValue(value: ValueRepr): AlmValidation[Any] = 
    if(FromStdLibJsonRematerializerFuns.isPrimitive(value))
      value.success
    else
      UnspecifiedProblem("Not a primitive type").failure

  override def isPrimitive(value: ValueRepr): Boolean = FromStdLibJsonRematerializerFuns.isPrimitive(value)

  override def stringFromRepr(value: ValueRepr): AlmValidation[String] = almCast[String](value)
  override def booleanFromRepr(value: ValueRepr): AlmValidation[Boolean] = almCast[Boolean](value)
  override def byteFromRepr(value: ValueRepr): AlmValidation[Byte] = almCast[Double](value).map(_.toByte)
  override def intFromRepr(value: ValueRepr): AlmValidation[Int] = almCast[Double](value).map(_.toInt)
  override def longFromRepr(value: ValueRepr): AlmValidation[Long] = almCast[Double](value).map(_.toLong)
  override def bigIntFromRepr(value: ValueRepr): AlmValidation[BigInt] = almCast[String](value).flatMap(parseBigIntAlm(_))
  override def floatFromRepr(value: ValueRepr): AlmValidation[Float] = almCast[Double](value).map(_.toFloat)
  override def doubleFromRepr(value: ValueRepr): AlmValidation[Double] = almCast[Double](value)
  override def bigDecimalFromRepr(value: ValueRepr): AlmValidation[BigDecimal] = almCast[String](value).flatMap(parseDecimalAlm(_))
  override def byteArrayFromRepr(value: ValueRepr): AlmValidation[Array[Byte]] =
    almCast[List[Double]](value).map(x => x.toArray.map(_.toByte))
  override def byteArrayFromBase64Repr(value: ValueRepr): AlmValidation[Array[Byte]] =
    almCast[String](value).flatMap(ParseFuns.parseBase64Alm(_))
  override def byteArrayFromBlobRepr(value: ValueRepr): AlmValidation[Array[Byte]] =
    byteArrayFromBase64Repr(value)
  override def dateTimeFromRepr(value: ValueRepr): AlmValidation[org.joda.time.DateTime] = almCast[String](value).flatMap(parseDateTimeAlm(_))
  override def uriFromRepr(value: ValueRepr): AlmValidation[_root_.java.net.URI] = almCast[String](value).flatMap(parseUriAlm(_))
  override def uuidFromRepr(value: ValueRepr): AlmValidation[_root_.java.util.UUID] = almCast[String](value).flatMap(parseUuidAlm(_))

  override def traversableOfReprFromRepr(value: ValueRepr): AlmValidation[Iterable[ValueRepr]] = almCast[List[ValueRepr]](value)
  override def tuple2OfReprFromRepr(value: ValueRepr): AlmValidation[(ValueRepr, ValueRepr)] =
    traversableOfReprFromRepr(value).flatMap { reprItems =>
      (reprItems.headOption, reprItems.tail.headOption) match {
        case (Some(a), Some(b)) => (a, b).success
        case _ => NoSuchElementProblem("Not enough items to build a tuple").failure
      }
    }

}