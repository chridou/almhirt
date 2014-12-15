package almhirt.i18n.impl

import java.text.FieldPosition
import scalaz.syntax.validation._
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
    useDigitsGrouping: Option[Boolean]): AlmValidation[BasicValueResourceValue] =
    construct(locale, argname, style, minFractionDigits, maxFractionDigits, useDigitsGrouping)

  private def construct(locale: ULocale,
                        argname: String,
                        style: Option[NumberFormatStyle],
                        minFractionDigits: Option[Int],
                        maxFractionDigits: Option[Int],
                        useDigitsGrouping: Option[Boolean]): AlmValidation[NumberFormatResourceValue] = {
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
    new NumberFormatResourceValue(locale, argname, numberFormat).success
  }

}

private[almhirt] final class NumberFormatResourceValue(
  override val locale: ULocale,
  val argname: String,
  format: NumberFormat) extends BasicValueResourceValue {

  def formatable: Formatable = {
    val numberFormat = format.clone().asInstanceOf[NumberFormat]
    new SingleArgFormatable(new SingleArgFormattingModule {
      val locale = NumberFormatResourceValue.this.locale
      val argname = NumberFormatResourceValue.this.argname
      def formatIntoBuffer(appendTo: StringBuffer, pos: FieldPosition, arg: Any): AlmValidation[StringBuffer] =
        inTryCatch { numberFormat.format(arg, appendTo, pos) }
    })
  }

}