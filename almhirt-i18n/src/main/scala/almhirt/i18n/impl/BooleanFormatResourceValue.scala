package almhirt.i18n.impl

import java.text.FieldPosition
import scalaz.Validation.FlatMap._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.i18n._
import com.ibm.icu.util.ULocale

private[almhirt] object BooleanFormatResourceValue {
  def apply(
    locale: ULocale,
    argname: String,
    trueText: String,
    falseText: String): BasicValueResourceValue = new BooleanFormatResourceValue(locale, argname, trueText, falseText)
}

private[almhirt] final class BooleanFormatResourceValue(
  override val locale: ULocale,
  override val argname: String,
  trueText: String,
  falseText: String) extends BasicValueResourceValue with SingleArgFormattingModule {

  override def formatIntoBuffer(appendTo: StringBuffer, pos: FieldPosition, arg: Any): AlmValidation[StringBuffer] =
    for {
      value ←  arg.castTo[Boolean]
      rendered ← inTryCatch { appendTo.append(if (value) trueText else falseText) }
    } yield rendered

  def formatable: AlmFormatter = new SingleArgFormatter(this)
}
