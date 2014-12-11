package almhirt.i18n

import scalaz.syntax.validation._
import almhirt.common._
import com.ibm.icu.text.MessageFormat
import com.ibm.icu.util.ULocale
import java.text.FieldPosition

sealed trait ResourceValue

sealed trait TextResourceValue extends ResourceValue

final case class RawStringValue(raw: String) extends TextResourceValue with Formatable {
  def withArg(arg: (String, Any)): RawStringValue = {
    this
  }

  def withArgs(args: (String, Any)*): RawStringValue = {
    this
  }

  override def withUnnamedArg(arg: Any): Formatable = {
    this
  }

  override def withUnnamedArgs(args: Any*): Formatable = {
    this
  }

  def withRenderedArg(argname: String)(f: ULocale ⇒ String): RawStringValue = {
    this
  }

  override def render: AlmValidation[String] = scalaz.Success(raw)
  override def renderIntoBuffer(into: StringBuffer): AlmValidation[StringBuffer] = scalaz.Success(into.append(raw))
  override def renderIntoBuffer(into: StringBuffer, pos: FieldPosition) = scalaz.Success(into.append(raw))

  override def forceRender: String = raw
  override def forceRenderIntoBuffer(into: StringBuffer): StringBuffer = into.append(raw)
  override def forceRenderIntoBuffer(into: StringBuffer, pos: FieldPosition): StringBuffer = into.append(raw)

  override def snapshot: RawStringValue = {
    this
  }

}

final class IcuMessageFormat(private val _format: MessageFormat) extends TextResourceValue with Equals {
  def raw = formatInstance.toPattern()
  def formatInstance: MessageFormat = _format.clone().asInstanceOf[MessageFormat]

  def canEqual(other: Any) = {
    other.isInstanceOf[almhirt.i18n.IcuMessageFormat]
  }

  override def equals(other: Any) = {
    other match {
      case that: almhirt.i18n.IcuMessageFormat ⇒ that.canEqual(IcuMessageFormat.this) && _format == that._format
      case _                                   ⇒ false
    }
  }

  override def hashCode() = {
    val prime = 41
    prime + _format.hashCode
  }
}

object IcuMessageFormat {
  def apply(pattern: String, locale: ULocale): AlmValidation[IcuMessageFormat] =
    try {
      val msgFrmt = new MessageFormat(pattern, locale)
      (new IcuMessageFormat(msgFrmt)).success
    } catch {
      case scala.util.control.NonFatal(exn) ⇒ ParsingProblem(exn.getMessage, Some(pattern), cause = Some(exn)).failure
    }
}

trait MeasuredValueFormatter extends TextResourceValue {
  def locale: ULocale
  def formatable: Formatable
  def argname: String
  def renderIntoBuffer(arg: Any, appendTo: StringBuffer, pos: FieldPosition): AlmValidation[StringBuffer]
}

trait BooleanValueFormatter extends TextResourceValue {
  def locale: ULocale
  def formatable: Formatable
  def argname: String
  def renderIntoBuffer(arg: Any, appendTo: StringBuffer, pos: FieldPosition): AlmValidation[StringBuffer]
}

