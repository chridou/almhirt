package riftwarp.std

import java.util.{ UUID => JUUID }
import scala.concurrent.duration._
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.almvalidation.kit._
import riftwarp._

trait WarpPrimitiveConverter[T] {
  def convert(what: WarpPackage): AlmValidation[T]
  def convertBack(what: T): WarpPrimitive
}

trait WarpPrimitiveToBooleanConverter extends WarpPrimitiveConverter[Boolean] {
  override def convert(what: WarpPackage): AlmValidation[Boolean] =
    what match {
      case WarpBoolean(value) => value.success
      case x => UnspecifiedProblem(s""""${x.getClass().getName()}" can not be a Boolean""").failure
    }
  override def convertBack(what: Boolean) = WarpBoolean(what)
}

trait WarpPrimitiveToStringConverter extends WarpPrimitiveConverter[String] {
  override def convert(what: WarpPackage): AlmValidation[String] =
    what match {
      case WarpBoolean(value) => value.toString.success
      case WarpString(value) => value.success
      case WarpByte(value) => value.toString.success
      case WarpInt(value) => value.toString.success
      case WarpLong(value) => value.toString.success
      case WarpBigInt(value) => value.toString.success
      case WarpFloat(value) => value.toString.success
      case WarpDouble(value) => value.toString.success
      case WarpBigDecimal(value) => value.toString.success
      case WarpUuid(value) => value.toString.success
      case WarpUri(value) => value.toString.success
      case WarpDateTime(value) => value.toString().success
      case x => UnspecifiedProblem(s""""${x.getClass().getName()}" can not be a DateTime""").failure
    }
  override def convertBack(what: String) = WarpString(what)
  
}

trait WarpPrimitiveToByteConverter extends WarpPrimitiveConverter[Byte] {
  override def convert(what: WarpPackage): AlmValidation[Byte] =
    what match {
      case WarpString(value) => value.toByteAlm
      case WarpByte(value) => value.success
      case WarpInt(value) => value.toByte.success
      case WarpLong(value) => value.toByte.success
      case WarpFloat(value) => value.toByte.success
      case WarpDouble(value) => value.toByte.success
      case x => UnspecifiedProblem(s""""${x.getClass().getName()}" can not be a Byte""").failure
    }
  override def convertBack(what: Byte) = WarpByte(what)
}

trait WarpPrimitiveToIntConverter extends WarpPrimitiveConverter[Int] {
  override def convert(what: WarpPackage): AlmValidation[Int] =
    what match {
      case WarpString(value) => value.toIntAlm
      case WarpByte(value) => value.toInt.success
      case WarpInt(value) => value.success
      case WarpLong(value) => value.toInt.success
      case WarpFloat(value) => value.toInt.success
      case WarpDouble(value) => value.toInt.success
      case x => UnspecifiedProblem(s""""${x.getClass().getName()}" can not be an Int""").failure
    }
  override def convertBack(what: Int) = WarpInt(what)
}

trait WarpPrimitiveToLongConverter extends WarpPrimitiveConverter[Long] {
  override def convert(what: WarpPackage): AlmValidation[Long] =
    what match {
      case WarpString(value) => value.toLongAlm
      case WarpByte(value) => value.toLong.success
      case WarpInt(value) => value.toLong.success
      case WarpLong(value) => value.success
      case WarpFloat(value) => value.toLong.success
      case WarpDouble(value) => value.toLong.success
      case x => UnspecifiedProblem(s""""${x.getClass().getName()}" can not be a Long""").failure
    }
  override def convertBack(what: Long) = WarpLong(what)
}

trait WarpPrimitiveToBigIntConverter extends WarpPrimitiveConverter[BigInt] {
  override def convert(what: WarpPackage): AlmValidation[BigInt] =
    what match {
      case WarpString(value) => value.toBigIntAlm
      case WarpByte(value) => BigInt(value).success
      case WarpInt(value) => BigInt(value).success
      case WarpLong(value) => BigInt(value).success
      case WarpBigInt(value) => value.success
      case WarpFloat(value) => BigInt(value.toLong).success
      case WarpDouble(value) => BigInt(value.toLong).success
      case x => UnspecifiedProblem(s""""${x.getClass().getName()}" can not be a BigInt""").failure
    }
  override def convertBack(what: BigInt) = WarpBigInt(what)
}

trait WarpPrimitiveToFloatConverter extends WarpPrimitiveConverter[Float] {
  override def convert(what: WarpPackage): AlmValidation[Float] =
    what match {
      case WarpString(value) => value.toFloatAlm
      case WarpByte(value) => value.toFloat.success
      case WarpInt(value) => value.toFloat.success
      case WarpLong(value) => value.toFloat.success
      case WarpBigInt(value) => value.toFloat.success
      case WarpFloat(value) => value.success
      case WarpDouble(value) => value.toFloat.success
      case WarpBigDecimal(value) => value.toFloat.success
      case x => UnspecifiedProblem(s""""${x.getClass().getName()}" can not be a Float""").failure
    }
  override def convertBack(what: Float) = WarpFloat(what)
}

trait WarpPrimitiveToDoubleConverter extends WarpPrimitiveConverter[Double] {
  override def convert(what: WarpPackage): AlmValidation[Double] =
    what match {
      case WarpString(value) => value.toDoubleAlm
      case WarpByte(value) => value.toDouble.success
      case WarpInt(value) => value.toDouble.success
      case WarpLong(value) => value.toDouble.success
      case WarpBigInt(value) => value.toDouble.success
      case WarpFloat(value) => value.toDouble.success
      case WarpDouble(value) => value.success
      case WarpBigDecimal(value) => value.toDouble.success
      case x => UnspecifiedProblem(s""""${x.getClass().getName()}" can not be a Double""").failure
    }
  override def convertBack(what: Double) = WarpDouble(what)
}

trait WarpPrimitiveToBigDecimalConverter extends WarpPrimitiveConverter[BigDecimal] {
  override def convert(what: WarpPackage): AlmValidation[BigDecimal] =
    what match {
      case WarpString(value) => value.toDecimalAlm
      case WarpByte(value) => BigDecimal(value).success
      case WarpInt(value) => BigDecimal(value).success
      case WarpLong(value) => BigDecimal(value).success
      case WarpBigInt(value) => BigDecimal(value).success
      case WarpFloat(value) => BigDecimal(value).success
      case WarpDouble(value) => BigDecimal(value).success
      case WarpBigDecimal(value) => value.success
      case x => UnspecifiedProblem(s""""${x.getClass().getName()}" can not be a BigDecimal""").failure
    }
  override def convertBack(what: BigDecimal) = WarpBigDecimal(what)
}

trait WarpPrimitiveToUuidConverter extends WarpPrimitiveConverter[JUUID] {
  override def convert(what: WarpPackage): AlmValidation[JUUID] =
    what match {
      case WarpString(value) => value.toUuidAlm
      case WarpUuid(value) => value.success
      case x => UnspecifiedProblem(s""""${x.getClass().getName()}" can not be a UUID""").failure
    }
  override def convertBack(what: JUUID) = WarpUuid(what)
}

trait WarpPrimitiveToUriConverter extends WarpPrimitiveConverter[java.net.URI] {
  override def convert(what: WarpPackage): AlmValidation[java.net.URI] =
    what match {
      case WarpString(value) => value.toUriAlm
      case WarpUri(value) => value.success
      case x => UnspecifiedProblem(s""""${x.getClass().getName()}" can not be an URI""").failure
    }
  override def convertBack(what: java.net.URI) = WarpUri(what)
}

trait WarpPrimitiveToDateTimeConverter extends WarpPrimitiveConverter[org.joda.time.DateTime] {
  override def convert(what: WarpPackage): AlmValidation[org.joda.time.DateTime] =
    what match {
      case WarpString(value) => value.toDateTimeAlm
      case WarpDateTime(value) => value.success
      case x => UnspecifiedProblem(s""""${x.getClass().getName()}" can not be a DateTime""").failure
    }
  override def convertBack(what: org.joda.time.DateTime) = WarpDateTime(what)
}

trait WarpPrimitiveToDurationConverter extends WarpPrimitiveConverter[FiniteDuration] {
  override def convert(what: WarpPackage): AlmValidation[FiniteDuration] =
    what match {
      case WarpString(value) => value.toDurationAlm
      case WarpDuration(value) => value.success
      case x => UnspecifiedProblem(s""""${x.getClass().getName()}" can not be a DateTime""").failure
    }
  override def convertBack(what: FiniteDuration) = WarpDuration(what)
}