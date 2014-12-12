package almhirt.i18n.impl

import java.text.FieldPosition
import almhirt.common._
import almhirt.i18n._
import com.ibm.icu.util.ULocale
import com.ibm.icu.text.NumberFormat

object NumberFormatter {
  sealed trait Style { def parsableString: String }
  case object Percentage extends Style { val parsableString = "percentage" }
  case object Currency extends Style { val parsableString = "currency" }
  case object Scientific extends Style { val parsableString = "scientific" }
  case object NoStyle extends Style { val parsableString = "nostyle" }

  def apply(
    locale: ULocale,
    argname: String,
    style: Option[Style],
    minFractionDigits: Option[Int],
    maxFractionDigits: Option[Int],
    useDigitsGrouping: Option[Boolean]): AlmValidation[BasicValueFormatter] =
    ???
}

private[almhirt] final class NumberFormatter(
  override val locale: ULocale,
  override val argname: String,
  numberFormat: NumberFormat) extends BasicValueFormatter {

  override def renderIntoBuffer(arg: Any, appendTo: StringBuffer, pos: FieldPosition): AlmValidation[StringBuffer] =
    ???

  def formatable: Formatable = new SingleArgFormatable(this)
}