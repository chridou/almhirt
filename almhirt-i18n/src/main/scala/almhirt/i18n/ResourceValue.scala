package almhirt.i18n

import scalaz.syntax.validation._
import almhirt.common._
import com.ibm.icu.text.MessageFormat
import com.ibm.icu.util.ULocale
import java.text.FieldPosition

sealed trait ResourceValue

sealed trait TextResourceValue extends ResourceValue

final case class RawStringResourceValue(override val locale: ULocale, raw: String) extends TextResourceValue with AlmFormatter {
  override def formatArgsIntoAt(appendTo: StringBuffer, pos: FieldPosition, args: Map[String, Any]): AlmValidation[StringBuffer] =
    appendTo.append(raw).success

  override def formatIntoAt(appendTo: StringBuffer, pos: FieldPosition, args: (String, Any)*): AlmValidation[StringBuffer] =
    appendTo.append(raw).success

  override def formatInto(appendTo: StringBuffer, args: (String, Any)*): AlmValidation[StringBuffer] =
    appendTo.append(raw).success

  override def formatArgsInto(appendTo: StringBuffer, args: Map[String, Any]): AlmValidation[StringBuffer] =
    appendTo.append(raw).success

  override def format(args: (String, Any)*): AlmValidation[String] =
    raw.success

  override def formatArgs(args: Map[String, Any]): AlmValidation[String] =
    raw.success

  override def formatValuesIntoAt(appendTo: StringBuffer, pos: FieldPosition, values: Any*): AlmValidation[StringBuffer] =
    appendTo.append(raw).success

  override def formatValuesInto(appendTo: StringBuffer, values: Any*): AlmValidation[StringBuffer] =
    formatValuesIntoAt(appendTo, util.DontCareFieldPosition, values: _*)

  override def formatValues(values: Any*): AlmValidation[String] =
    raw.success

}

final class IcuResourceValue(private val _format: MessageFormat) extends TextResourceValue with Equals {
  def raw = formatInstance.toPattern()
  def formatInstance: MessageFormat = _format.clone().asInstanceOf[MessageFormat]

  def canEqual(other: Any) = {
    other.isInstanceOf[almhirt.i18n.IcuResourceValue]
  }

  override def equals(other: Any) = {
    other match {
      case that: almhirt.i18n.IcuResourceValue ⇒ that.canEqual(IcuResourceValue.this) && _format == that._format
      case _                                   ⇒ false
    }
  }

  override def hashCode() = {
    val prime = 41
    prime + _format.hashCode
  }
}

object IcuResourceValue {
  def apply(pattern: String, locale: ULocale): AlmValidation[IcuResourceValue] =
    try {
      val msgFrmt = new MessageFormat(pattern, locale)
      (new IcuResourceValue(msgFrmt)).success
    } catch {
      case scala.util.control.NonFatal(exn) ⇒ ParsingProblem(exn.getMessage, Some(pattern), cause = None).failure
    }
}

trait BasicValueResourceValue extends TextResourceValue {
  def locale: ULocale
  def formatable: AlmFormatter
}

trait NumericValueResourceValue extends BasicValueResourceValue {
  def locale: ULocale
  def formatable: AlmNumericFormatter
}

trait MeasuredValueResourceValue extends NumericValueResourceValue {
  def locale: ULocale
  def formatable: AlmMeasureFormatter
}

