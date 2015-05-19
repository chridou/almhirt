package almhirt.i18n.impl

import scala.collection.mutable.HashMap
import almhirt.common._
import almhirt.i18n._
import com.ibm.icu.util.ULocale
import java.text.FieldPosition

private[almhirt] final class SingleArgFormatter(formatter: SingleArgFormattingModule) extends AlmFormatter {
  override def locale = formatter.locale
  override def formatArgsIntoAt(appendTo: StringBuffer, pos: FieldPosition, args: Map[String, Any]): AlmValidation[StringBuffer] =
    args get (formatter.argname) match {
      case Some(v) ⇒
        formatter.formatIntoBuffer(appendTo, pos, v)
      case None ⇒
        scalaz.Failure(NoSuchElementProblem(s"""An argument named "${formatter.argname}" was not found."""))
    }

  override def formatValuesIntoAt(appendTo: StringBuffer, pos: FieldPosition, values: Any*): AlmValidation[StringBuffer] =
    if (values.isEmpty) {
      scalaz.Failure(NoSuchElementProblem(s"""An argument is required."""))
    } else {
      formatter.formatIntoBuffer(appendTo, pos, values.head)
    }
}

private[almhirt] final class NumericArgFormatter(formatter: NumericArgFormattingModule) extends AlmNumericFormatter {
  override def locale = formatter.locale
  override def formatArgsIntoAt(appendTo: StringBuffer, pos: FieldPosition, args: Map[String, Any]): AlmValidation[StringBuffer] =
    args get (formatter.argname) match {
      case Some(v) ⇒
        formatValuesIntoAt(appendTo, pos, v)
      case None ⇒
        scalaz.Failure(NoSuchElementProblem(s"""An argument named "${formatter.argname}" was not found."""))
    }

  override def formatValuesIntoAt(appendTo: StringBuffer, pos: FieldPosition, values: Any*): AlmValidation[StringBuffer] =
    if (values.isEmpty) {
      scalaz.Failure(NoSuchElementProblem(s"""An argument is required."""))
    } else {
      values.head match {
        case v: Byte ⇒
          formatNumericIntoAt(v, appendTo, pos)
        case v: Int ⇒
          formatNumericIntoAt(v, appendTo, pos)
        case v: Long ⇒
          formatNumericIntoAt(v, appendTo, pos)
        case v: Float ⇒
          formatNumericIntoAt(v, appendTo, pos)
        case v: Double ⇒
          formatNumericIntoAt(v, appendTo, pos)
        case v: BigDecimal ⇒
          formatNumericIntoAt(v, appendTo, pos)
      }
    }

  override def formatNumericIntoAt[T: Numeric](num: T, appendTo: StringBuffer, pos: FieldPosition): AlmValidation[StringBuffer] =
    formatter.formatIntoBuffer(appendTo, pos, num)

  override def formatNumericRangeIntoAt[T: Numeric](lower: T, upper: T, appendTo: StringBuffer, pos: FieldPosition): AlmValidation[StringBuffer] =
    formatter.formatRangeIntoBuffer(appendTo, pos, lower, upper)
}

private[almhirt] final class MeasuredArgFormatter(formatter: MeasuredArgFormattingModule) extends AlmMeasureFormatter {
  override def locale = formatter.locale
  override def formatArgsIntoAt(appendTo: StringBuffer, pos: FieldPosition, args: Map[String, Any]): AlmValidation[StringBuffer] =
    args get (formatter.argname) match {
      case Some(v) ⇒
        formatValuesIntoAt(appendTo, pos, v)
      case None ⇒
        scalaz.Failure(NoSuchElementProblem(s"""An argument named "${formatter.argname}" was not found."""))
    }

  override def formatValuesIntoAt(appendTo: StringBuffer, pos: FieldPosition, values: Any*): AlmValidation[StringBuffer] =
    if (values.isEmpty) {
      scalaz.Failure(NoSuchElementProblem(s"""An argument is required."""))
    } else {
      values.head match {
        case v: Measured ⇒
          formatMeasureIntoAt(v, appendTo, pos, None)
        case v: Byte ⇒
          formatMeasureIntoAt(formatter.defaultUnitOfMeasure.dimension.siMeasured(v), appendTo, pos, None)
        case v: Int ⇒
          formatMeasureIntoAt(formatter.defaultUnitOfMeasure.dimension.siMeasured(v), appendTo, pos, None)
        case v: Long ⇒
          formatMeasureIntoAt(formatter.defaultUnitOfMeasure.dimension.siMeasured(v), appendTo, pos, None)
        case v: Float ⇒
          formatMeasureIntoAt(formatter.defaultUnitOfMeasure.dimension.siMeasured(v), appendTo, pos, None)
        case v: Double ⇒
          formatMeasureIntoAt(formatter.defaultUnitOfMeasure.dimension.siMeasured(v), appendTo, pos, None)
        case v: BigDecimal ⇒
          formatMeasureIntoAt(formatter.defaultUnitOfMeasure.dimension.siMeasured(v.toDouble), appendTo, pos, None)
      }
    }

  override def formatMeasureIntoAt(v: Measured, appendTo: StringBuffer, pos: FieldPosition, uomSys: Option[UnitsOfMeasurementSystem]): AlmValidation[StringBuffer] =
    formatter.formatIntoBuffer(appendTo, pos, v, uomSys)

  override def formatMeasureRangeIntoAt(lower: Measured, upper: Measured, appendTo: StringBuffer, pos: FieldPosition, uomSys: Option[UnitsOfMeasurementSystem]): AlmValidation[StringBuffer] =
    formatter.formatRangeIntoBuffer(appendTo, pos, lower, upper, uomSys)

  override def formatNumericIntoAt[T: Numeric](num: T, appendTo: StringBuffer, pos: FieldPosition): AlmValidation[StringBuffer] =
    formatValuesIntoAt(appendTo, pos, num)

  override def formatNumericRangeIntoAt[T](lower: T, upper: T, appendTo: StringBuffer, pos: FieldPosition)(implicit num: Numeric[T]): AlmValidation[StringBuffer] =
    formatMeasureRangeIntoAt(
      formatter.defaultUnitOfMeasure.dimension.siMeasured(num.toDouble(lower)),
      formatter.defaultUnitOfMeasure.dimension.siMeasured(num.toDouble(upper)),
      appendTo,
      pos,
      None)

  def valueWithUnitOfMeasurement(v: Measured, uomSys: Option[UnitsOfMeasurementSystem]): AlmValidation[(Double, String)] = {
    formatter.valueWithUnitOfMeasurement(v, uomSys)
  }

  def rangeWithUnitOfMeasurement(lower: Measured, upper: Measured, uomSys: Option[UnitsOfMeasurementSystem]): AlmValidation[(Double, Double, String)] = {
    formatter.rangeWithUnitOfMeasurement(lower, upper, uomSys)
  }
}