package almhirt.i18n.impl

import scalaz._, Scalaz._
import scalaz.Validation.FlatMap._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.i18n.{ AlmFormatter, BasicValueResourceValue }
import com.ibm.icu.util.ULocale
import java.text.FieldPosition

class BooleanFormatterSwitch(
    override val locale: ULocale,
    conditionParameter: Option[String],
    noParamDefaultsTo: Option[Boolean],
    trueFormatter: () ⇒ AlmFormatter,
    falseFormatter: () ⇒ AlmFormatter) extends BasicValueResourceValue with AlmFormatter {
  val conditionName = conditionParameter getOrElse "condition"

  def formatArgsIntoAt(appendTo: StringBuffer, pos: FieldPosition, args: Map[String, Any]): AlmValidation[StringBuffer] = {
    val effConditionValueV =
      (args get conditionName).map(x ⇒ any2Boolean(x, conditionName)).validationOut.flatMap {
        case Some(c) ⇒ c.success
        case None ⇒ noParamDefaultsTo.cata(
          some = _.success,
          none = ArgumentProblem(s"Parameter '$conditionName' is missing and no default was defined.").failure)
      }
    effConditionValueV.flatMap {
      case true  ⇒ trueFormatter().formatArgsInto(appendTo, args)
      case false ⇒ falseFormatter().formatArgsInto(appendTo, args)
    }

  }

  private def any2Boolean(what: Any, paramName: String): AlmValidation[Boolean] =
    what match {
      case x: Boolean ⇒ x.success
      case x: String  ⇒ x.toBooleanAlm
      case x          ⇒ ArgumentProblem(s"Parameter '$paramName' of type ${x.getClass.getName} can not be converted to a Boolean.").failure
    }

  override def formatable = this

}