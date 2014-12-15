package almhirt.i18n

import scalaz.syntax.validation._
import almhirt.common._
import com.ibm.icu.text.MessageFormat
import com.ibm.icu.util.ULocale
import java.text.FieldPosition

sealed trait ResourceValue

sealed trait TextResourceValue extends ResourceValue

final case class RawStringResourceValue(override val locale: ULocale, raw: String) extends TextResourceValue with Formatable {
  override def formatArgsIntoBufferAt(appendTo: StringBuffer, pos: FieldPosition, args: Map[String, Any]): AlmValidation[StringBuffer] =
    appendTo.append(raw).success

  override def formatIntoBufferAt(appendTo: StringBuffer, pos: FieldPosition, args: (String, Any)*): AlmValidation[StringBuffer] =
    appendTo.append(raw).success

  override def formatIntoBuffer(appendTo: StringBuffer, args: (String, Any)*): AlmValidation[StringBuffer] =
    appendTo.append(raw).success

  override def formatArgsIntoBuffer(appendTo: StringBuffer, args: Map[String, Any]): AlmValidation[StringBuffer] =
    appendTo.append(raw).success

  override def format(args: (String, Any)*): AlmValidation[String] =
    raw.success

  override def formatArgs(args: Map[String, Any]): AlmValidation[String] =
    raw.success

  override def formatValuesIntoBufferAt(appendTo: StringBuffer, pos: FieldPosition, values: Any*): AlmValidation[StringBuffer] =
    appendTo.append(raw).success

  override def formatValuesIntoBuffer(appendTo: StringBuffer, values: Any*): AlmValidation[StringBuffer] =
    formatValuesIntoBufferAt(appendTo, util.DontCareFieldPosition, values: _*)

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
      case scala.util.control.NonFatal(exn) ⇒ ParsingProblem(exn.getMessage, Some(pattern), cause = Some(exn)).failure
    }
}

trait BasicValueResourceValue extends TextResourceValue {
  def locale: ULocale
  def formatable: Formatable
}

