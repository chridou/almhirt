package almhirt.i18n.impl

import java.text.FieldPosition
import scalaz.Validation.FlatMap._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.i18n._
import com.ibm.icu.util.ULocale

object BooleanValueFormatterBuilder {
  def apply(
    locale: ULocale,
    argname: String,
    trueText: String,
    falseText: String): BooleanValueFormatter = new BooleanValueFormatterImpl(locale, argname, trueText, falseText)
}

private[almhirt] final class BooleanValueFormatterImpl(
  override val locale: ULocale,
  override val argname: String,
  trueText: String,
  falseText: String) extends BooleanValueFormatter {

  def any2MeasuredValueArg(arg: Any): AlmValidation[Boolean] = arg.castTo[Boolean]

  override def renderIntoBuffer(arg: Any, appendTo: StringBuffer, pos: FieldPosition): AlmValidation[StringBuffer] =
    for {
      value ← any2MeasuredValueArg(arg)
      rendered ← inTryCatch { appendTo.append(if (value) trueText else falseText) }
    } yield rendered

  def formatable: Formatable = new BooleanValueFormatable(this)
}
