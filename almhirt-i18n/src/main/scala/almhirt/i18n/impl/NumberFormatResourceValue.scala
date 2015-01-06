package almhirt.i18n.impl

import java.text.FieldPosition
import scalaz.syntax.validation._
import scalaz.Validation.FlatMap._
import almhirt.common._
import almhirt.i18n._
import com.ibm.icu.util.ULocale
import com.ibm.icu.text.NumberFormat

object NumberFormatResourceValue {

  def apply(
    locale: ULocale,
    argname: String,
    style: Option[NumberFormatStyle],
    minFractionDigits: Option[Int],
    maxFractionDigits: Option[Int],
    useDigitsGrouping: Option[Boolean],
    rangeSeparator: Option[String]): AlmValidation[BasicValueResourceValue] =
    construct(locale, argname, style, minFractionDigits, maxFractionDigits, useDigitsGrouping, rangeSeparator)

  private def construct(locale: ULocale,
                        argname: String,
                        style: Option[NumberFormatStyle],
                        minFractionDigits: Option[Int],
                        maxFractionDigits: Option[Int],
                        useDigitsGrouping: Option[Boolean],
                        rangeSeparator: Option[String]): AlmValidation[NumberFormatResourceValue] = {
    val numberFormat =
      style match {
        case None                               ⇒ NumberFormat.getInstance(locale)
        case Some(NumberFormatStyle.NoStyle)    ⇒ NumberFormat.getInstance(locale)
        case Some(NumberFormatStyle.Scientific) ⇒ NumberFormat.getScientificInstance(locale)
        case Some(NumberFormatStyle.Integer)    ⇒ NumberFormat.getIntegerInstance(locale)
        case Some(NumberFormatStyle.Percentage) ⇒ NumberFormat.getPercentInstance(locale)
      }

    minFractionDigits.foreach { digits ⇒ numberFormat.setMinimumFractionDigits(digits) }
    maxFractionDigits.foreach { digits ⇒ numberFormat.setMaximumFractionDigits(digits) }
    useDigitsGrouping.foreach { useGrouping ⇒ numberFormat.setGroupingUsed(useGrouping) }
    new NumberFormatResourceValue(locale, argname, rangeSeparator getOrElse "-", numberFormat).success
  }

}

private[almhirt] final class NumberFormatResourceValue(
  override val locale: ULocale,
  val argname: String,
  val rangeSeparator: String,
  format: NumberFormat) extends NumericValueResourceValue {

  override def formatable: AlmNumericFormatter = {
    val numberFormat = format.clone().asInstanceOf[NumberFormat]
    new NumericArgFormatter(new NumericArgFormattingModule {
      val locale = NumberFormatResourceValue.this.locale
      val argname = NumberFormatResourceValue.this.argname

      override def formatIntoBuffer[T: Numeric](appendTo: StringBuffer, pos: FieldPosition, arg: T): AlmValidation[StringBuffer] =
        inTryCatch { numberFormat.format(arg, appendTo, pos) }

      override def formatRangeIntoBuffer[T: Numeric](appendTo: StringBuffer, pos: FieldPosition, arg1: T, arg2: T): AlmValidation[StringBuffer] = {
        for {
          a ← inTryCatch { numberFormat.format(arg1, appendTo, pos).append(rangeSeparator) }
          b ← inTryCatch { numberFormat.format(arg2, appendTo, pos) }
        } yield b
      }
    })
  }

}