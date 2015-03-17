package almhirt.i18n.impl

import java.text.FieldPosition
import scalaz.syntax.validation._
import scalaz.Validation.FlatMap._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.i18n._
import com.ibm.icu.util.ULocale

private[almhirt] object SelectTextFormatResourceValue {
  def apply(
    locale: ULocale,
    argname: String,
    defaultText: Option[String],
    select: Map[String, String]): BasicValueResourceValue = new SelectTextFormatResourceValue(locale, argname, defaultText, select)
}

private[almhirt] final class SelectTextFormatResourceValue(
  override val locale: ULocale,
  override val argname: String,
  defaultText: Option[String],
  select: Map[String, String]) extends BasicValueResourceValue with SingleArgFormattingModule {

  def any2String(arg: Any): AlmValidation[String] =
    arg match {
      case s: String ⇒ s.success
      case i: Int    ⇒ i.toString.success
      case x ⇒
        ArgumentProblem(s"""${x.getClass().getName} is not a valid argument for a SelectTextFormatResourceValue.""").failure
    }

  override def formatIntoBuffer(appendTo: StringBuffer, pos: FieldPosition, arg: Any): AlmValidation[StringBuffer] =
    for {
      selector ← any2String(arg)
      selected ← select get selector match {
        case Some(txt) ⇒
          txt.success
        case None ⇒
          defaultText match {
            case Some(txt) ⇒
              txt.success
            case None ⇒
              ArgumentProblem(s"""No value for selector "$selector" in parameter "$argname". Consider adding a value for "$selector" or define a default for unknown selectors.""").failure
          }
      }
      rendered ← inTryCatch { appendTo.append(selected) }
    } yield rendered

  override def formatable: AlmFormatter = new SingleArgFormatter(this)
}
