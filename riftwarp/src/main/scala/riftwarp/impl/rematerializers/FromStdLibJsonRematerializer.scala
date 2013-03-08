package riftwarp.impl.rematerializers

import scala.collection.generic.CanBuildFrom
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.almvalidation.funs._
import riftwarp._

class FromStdLibJsonRematerializer extends RematerializerTemplate[DimensionStdLibJson] {
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
  
  override def resequence(value: ValueRepr): AlmValidation[Iterable[ValueRepr]] = almCast[List[ValueRepr]](value)
  override def retuplelize2(value: ValueRepr): AlmValidation[(ValueRepr, ValueRepr)] =
    resequence(value).flatMap { reprItems =>
      (reprItems.headOption, reprItems.tail.headOption) match {
        case (Some(a), Some(b)) => (a,b).success
        case _ => NoSuchElementProblem("Not enough items to build a tuple").failure
      }
    }
  
}