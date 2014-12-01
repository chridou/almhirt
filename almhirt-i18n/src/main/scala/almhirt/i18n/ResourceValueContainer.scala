package almhirt.i18n

import scalaz.syntax.validation._
import almhirt.common._
import com.ibm.icu.text.MessageFormat
import com.ibm.icu.util.ULocale

sealed trait ResourceValue {
  def raw: String
}

final case class RawStringValue(raw: String) extends ResourceValue

final class IcuMessageFormat(private val _format: MessageFormat) extends ResourceValue with Equals {
  def raw = format.toPattern()
  def format: MessageFormat = _format.clone().asInstanceOf[MessageFormat]

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

