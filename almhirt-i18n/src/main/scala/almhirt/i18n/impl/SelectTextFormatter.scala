package almhirt.i18n.impl

import java.text.FieldPosition
import scalaz.syntax.validation._
import scalaz.Validation.FlatMap._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.i18n._
import com.ibm.icu.util.ULocale

object SelectTextFormatter {
  def apply(
    locale: ULocale,
    argname: String,
    defaultText: Option[String],
    select: Map[String, String]): BasicValueFormatter = new SelectTextFormatterImpl(locale, argname, defaultText, select)
}

private[almhirt] final class SelectTextFormatterImpl(
  override val locale: ULocale,
  override val argname: String,
  defaultText: Option[String],
  select: Map[String, String]) extends BasicValueFormatter {

  def any2String(arg: Any): AlmValidation[String] =
    arg match {
      case s: String => s.success
      case i: Int    => i.toString.success
      case x =>
        ArgumentProblem(s"""${x.getClass().getName} is not a valid argument for a SelectValueFormatter.""").failure
    }

  override def renderIntoBuffer(arg: Any, appendTo: StringBuffer, pos: FieldPosition): AlmValidation[StringBuffer] =
    for {
      selector ← any2String(arg)
      selected <- select get selector match {
        case Some(txt) =>
          txt.success
        case None =>
          defaultText match {
            case Some(txt) =>
              txt.success
            case None =>
              "".success
          }
      }
      rendered ← inTryCatch { appendTo.append(selected) }
    } yield rendered

  def formatable: Formatable = new SingleArgFormatable(this)
}
