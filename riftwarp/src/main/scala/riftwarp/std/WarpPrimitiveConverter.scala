package riftwarp.std

import java.util.{ UUID ⇒ JUUID }
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
      case WarpBoolean(value) ⇒ value.success
      case WarpString(value) ⇒ value.toBooleanAlm
      case WarpObject(wd, _) ⇒ 
       MappingProblem(s"""A WarpObject can not be a Boolean. The descriptor is ${wd.toString}""").failure
      case x ⇒ UnspecifiedProblem(s""""${x.getClass().getName()}" can not be a Boolean""").failure
    }
  override def convertBack(what: Boolean) = WarpBoolean(what)
}

trait WarpPrimitiveToStringConverter extends WarpPrimitiveConverter[String] {
  override def convert(what: WarpPackage): AlmValidation[String] =
    what match {
      case WarpString(value) ⇒ value.success
      case WarpBoolean(value) ⇒ value.toString.success
      case WarpByte(value) ⇒ value.toString.success
      case WarpInt(value) ⇒ value.toString.success
      case WarpShort(value) ⇒ value.toString.success
      case WarpLong(value) ⇒ value.toString.success
      case WarpBigInt(value) ⇒ value.toString.success
      case WarpFloat(value) ⇒ value.toString.success
      case WarpDouble(value) ⇒ value.toString.success
      case WarpBigDecimal(value) ⇒ value.toString.success
      case WarpUuid(value) ⇒ value.toString.success
      case WarpUri(value) ⇒ value.toString.success
      case WarpDateTime(value) ⇒ value.toString().success
      case WarpObject(wd, _) ⇒ 
       UnspecifiedProblem(s"""A WarpObject can not be a String. The descriptor is ${wd.toString}""").failure
      case x ⇒ 
        UnspecifiedProblem(s""""${x.getClass().getName()}" can not be a String""").failure
    }
  override def convertBack(what: String) = WarpString(what)
  
}

trait WarpPrimitiveToByteConverter extends WarpPrimitiveConverter[Byte] {
  override def convert(what: WarpPackage): AlmValidation[Byte] =
    what match {
      case WarpByte(value) ⇒ value.success
      case WarpString(value) ⇒ value.toByteAlm
      case WarpInt(value) ⇒ value.toByte.success
      case WarpShort(value) ⇒ value.toByte.success
      case WarpLong(value) ⇒ value.toByte.success
      case WarpFloat(value) ⇒ value.toByte.success
      case WarpDouble(value) ⇒ value.toByte.success
      case WarpBigDecimal(value) ⇒ value.toByte.success
      case WarpObject(wd, _) ⇒ 
       UnspecifiedProblem(s"""A WarpObject can not be a Byte. The descriptor is ${wd.toString}""").failure
      case x ⇒ UnspecifiedProblem(s""""${x.getClass().getName()}" can not be a Byte""").failure
    }
  override def convertBack(what: Byte) = WarpByte(what)
}

trait WarpPrimitiveToShortConverter extends WarpPrimitiveConverter[Short] {
  override def convert(what: WarpPackage): AlmValidation[Short] =
    what match {
      case WarpShort(value) ⇒ value.success
      case WarpString(value) ⇒ value.toShortAlm
      case WarpByte(value) ⇒ value.toShort.success
      case WarpInt(value) ⇒ value.toShort.success
      case WarpLong(value) ⇒ value.toShort.success
      case WarpFloat(value) ⇒ value.toShort.success
      case WarpDouble(value) ⇒ value.toShort.success
      case WarpBigDecimal(value) ⇒ value.toShort.success
      case WarpObject(wd, _) ⇒ 
       UnspecifiedProblem(s"""A WarpObject can not be a Short. The descriptor is ${wd.toString}""").failure
      case x ⇒ UnspecifiedProblem(s""""${x.getClass().getName()}" can not be a Short""").failure
    }
  override def convertBack(what: Short) = WarpShort(what)
}

trait WarpPrimitiveToIntConverter extends WarpPrimitiveConverter[Int] {
  override def convert(what: WarpPackage): AlmValidation[Int] =
    what match {
      case WarpInt(value) ⇒ value.success
      case WarpString(value) ⇒ value.toIntAlm
      case WarpByte(value) ⇒ value.toInt.success
      case WarpShort(value) ⇒ value.toInt.success
      case WarpLong(value) ⇒ value.toInt.success
      case WarpFloat(value) ⇒ value.toInt.success
      case WarpDouble(value) ⇒ value.toInt.success
      case WarpBigDecimal(value) ⇒ value.toInt.success
      case WarpObject(wd, _) ⇒ 
       UnspecifiedProblem(s"""A WarpObject can not be an Int. The descriptor is ${wd.toString}""").failure
      case x ⇒ UnspecifiedProblem(s""""${x.getClass().getName()}" can not be an Int""").failure
    }
  override def convertBack(what: Int) = WarpInt(what)
}

trait WarpPrimitiveToLongConverter extends WarpPrimitiveConverter[Long] {
  override def convert(what: WarpPackage): AlmValidation[Long] =
    what match {
      case WarpLong(value) ⇒ value.success
      case WarpString(value) ⇒ value.toLongAlm
      case WarpByte(value) ⇒ value.toLong.success
      case WarpShort(value) ⇒ value.toLong.success
      case WarpInt(value) ⇒ value.toLong.success
      case WarpFloat(value) ⇒ value.toLong.success
      case WarpDouble(value) ⇒ value.toLong.success
      case WarpBigDecimal(value) ⇒ value.toLong.success
      case WarpObject(wd, _) ⇒ 
       UnspecifiedProblem(s"""A WarpObject can not be a Long. The descriptor is ${wd.toString}""").failure
      case x ⇒ UnspecifiedProblem(s""""${x.getClass().getName()}" can not be a Long""").failure
    }
  override def convertBack(what: Long) = WarpLong(what)
}

trait WarpPrimitiveToBigIntConverter extends WarpPrimitiveConverter[BigInt] {
  override def convert(what: WarpPackage): AlmValidation[BigInt] =
    what match {
      case WarpBigInt(value) ⇒ value.success
      case WarpString(value) ⇒ value.toBigIntAlm
      case WarpByte(value) ⇒ BigInt(value).success
      case WarpShort(value) ⇒ BigInt(value).success
      case WarpInt(value) ⇒ BigInt(value).success
      case WarpLong(value) ⇒ BigInt(value).success
      case WarpFloat(value) ⇒ BigInt(value.toLong).success
      case WarpDouble(value) ⇒ BigInt(value.toLong).success
      case WarpObject(wd, _) ⇒ 
       UnspecifiedProblem(s"""A WarpObject can not be a BigInt. The descriptor is ${wd.toString}""").failure
      case x ⇒ UnspecifiedProblem(s""""${x.getClass().getName()}" can not be a BigInt""").failure
    }
  override def convertBack(what: BigInt) = WarpBigInt(what)
}

trait WarpPrimitiveToFloatConverter extends WarpPrimitiveConverter[Float] {
  override def convert(what: WarpPackage): AlmValidation[Float] =
    what match {
      case WarpFloat(value) ⇒ value.success
      case WarpString(value) ⇒ value.toFloatAlm
      case WarpByte(value) ⇒ value.toFloat.success
      case WarpShort(value) ⇒ value.toFloat.success
      case WarpInt(value) ⇒ value.toFloat.success
      case WarpLong(value) ⇒ value.toFloat.success
      case WarpBigInt(value) ⇒ value.toFloat.success
      case WarpDouble(value) ⇒ value.toFloat.success
      case WarpBigDecimal(value) ⇒ value.toFloat.success
      case WarpObject(wd, _) ⇒ 
       UnspecifiedProblem(s"""A WarpObject can not be a Float. The descriptor is ${wd.toString}""").failure
      case x ⇒ UnspecifiedProblem(s""""${x.getClass().getName()}" can not be a Float""").failure
    }
  override def convertBack(what: Float) = WarpFloat(what)
}

trait WarpPrimitiveToDoubleConverter extends WarpPrimitiveConverter[Double] {
  override def convert(what: WarpPackage): AlmValidation[Double] =
    what match {
      case WarpDouble(value) ⇒ value.success
      case WarpString(value) ⇒ value.toDoubleAlm
      case WarpByte(value) ⇒ value.toDouble.success
      case WarpShort(value) ⇒ value.toDouble.success
      case WarpInt(value) ⇒ value.toDouble.success
      case WarpLong(value) ⇒ value.toDouble.success
      case WarpBigInt(value) ⇒ value.toDouble.success
      case WarpFloat(value) ⇒ value.toDouble.success
      case WarpBigDecimal(value) ⇒ value.toDouble.success
      case WarpObject(wd, _) ⇒ 
       UnspecifiedProblem(s"""A WarpObject can not be a Double. The descriptor is ${wd.toString}""").failure
      case x ⇒ UnspecifiedProblem(s""""${x.getClass().getName()}" can not be a Double""").failure
    }
  override def convertBack(what: Double) = WarpDouble(what)
}

trait WarpPrimitiveToBigDecimalConverter extends WarpPrimitiveConverter[BigDecimal] {
  override def convert(what: WarpPackage): AlmValidation[BigDecimal] =
    what match {
      case WarpBigDecimal(value) ⇒ value.success
      case WarpString(value) ⇒ value.toDecimalAlm
      case WarpByte(value) ⇒ BigDecimal(value).success
      case WarpShort(value) ⇒ BigDecimal(value).success
      case WarpInt(value) ⇒ BigDecimal(value).success
      case WarpLong(value) ⇒ BigDecimal(value).success
      case WarpBigInt(value) ⇒ BigDecimal(value).success
      case WarpFloat(value) ⇒ BigDecimal(value.toString).success
      case WarpDouble(value) ⇒ BigDecimal(value).success
      case WarpObject(wd, _) ⇒ 
       UnspecifiedProblem(s"""A WarpObject can not be a BigDecimal. The descriptor is ${wd.toString}""").failure
      case x ⇒ UnspecifiedProblem(s""""${x.getClass().getName()}" can not be a BigDecimal""").failure
    }
  override def convertBack(what: BigDecimal) = WarpBigDecimal(what)
}

trait WarpPrimitiveToUuidConverter extends WarpPrimitiveConverter[JUUID] {
  override def convert(what: WarpPackage): AlmValidation[JUUID] =
    what match {
      case WarpUuid(value) ⇒ value.success
      case WarpString(value) ⇒ value.toUuidAlm
      case WarpObject(wd, _) ⇒ 
       UnspecifiedProblem(s"""A WarpObject can not be a JUUID. The descriptor is ${wd.toString}""").failure
      case x ⇒ UnspecifiedProblem(s""""${x.getClass().getName()}" can not be a UUID""").failure
    }
  override def convertBack(what: JUUID) = WarpUuid(what)
}

trait WarpPrimitiveToUriConverter extends WarpPrimitiveConverter[java.net.URI] {
  override def convert(what: WarpPackage): AlmValidation[java.net.URI] =
    what match {
      case WarpUri(value) ⇒ value.success
      case WarpString(value) ⇒ value.toUriAlm
      case WarpObject(wd, _) ⇒ 
       UnspecifiedProblem(s"""A WarpObject can not be an URI. The descriptor is ${wd.toString}""").failure
      case x ⇒ UnspecifiedProblem(s""""${x.getClass().getName()}" can not be an URI""").failure
    }
  override def convertBack(what: java.net.URI) = WarpUri(what)
}

trait WarpPrimitiveToDateTimeConverter extends WarpPrimitiveConverter[java.time.ZonedDateTime] {
  override def convert(what: WarpPackage): AlmValidation[java.time.ZonedDateTime] =
    what match {
      case WarpDateTime(value) ⇒ value.success
      case WarpString(value) ⇒ value.toDateTimeAlm
      case WarpObject(wd, _) ⇒ 
       UnspecifiedProblem(s"""A WarpObject can not be a DateTime. The descriptor is ${wd.toString}""").failure
      case x ⇒ UnspecifiedProblem(s""""${x.getClass().getName()}" can not be a DateTime""").failure
    }
  override def convertBack(what: java.time.ZonedDateTime) = WarpDateTime(what)
}

trait WarpPrimitiveToLocalDateTimeConverter extends WarpPrimitiveConverter[java.time.LocalDateTime] {
  override def convert(what: WarpPackage): AlmValidation[java.time.LocalDateTime] =
    what match {
      case WarpLocalDateTime(value) ⇒ value.success
      case WarpString(value) ⇒ value.toLocalDateTimeAlm
      case WarpObject(wd, _) ⇒ 
       UnspecifiedProblem(s"""A WarpObject can not be a LocalDateTime. The descriptor is ${wd.toString}""").failure
      case x ⇒ UnspecifiedProblem(s""""${x.getClass().getName()}" can not be a LocalDateTime""").failure
    }
  override def convertBack(what: java.time.LocalDateTime) = WarpLocalDateTime(what)
}

trait WarpPrimitiveToDurationConverter extends WarpPrimitiveConverter[FiniteDuration] {
  override def convert(what: WarpPackage): AlmValidation[FiniteDuration] =
    what match {
      case WarpDuration(value) ⇒ value.success
      case WarpString(value) ⇒ value.toDurationAlm
      case WarpObject(wd, _) ⇒ 
       UnspecifiedProblem(s"""A WarpObject can not be a FiniteDuration. The descriptor is ${wd.toString}""").failure
      case x ⇒ UnspecifiedProblem(s""""${x.getClass().getName()}" can not be a FiniteDuration""").failure
    }
  override def convertBack(what: FiniteDuration) = WarpDuration(what)
}