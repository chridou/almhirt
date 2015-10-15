package almhirt.i18n.impl

import almhirt.common._
import almhirt.i18n._
import com.ibm.icu.util.ULocale
import java.text.FieldPosition

object ZeroTextResourceValue {
  def apply(locale: ULocale) = new ZeroTextResourceValue(locale)
}

private[almhirt] class ZeroTextResourceValue(override val locale: ULocale) extends BasicValueResourceValue with AlmFormatter {
  override def formatArgsIntoAt(appendTo: StringBuffer, pos: FieldPosition, args: Map[String, Any]): AlmValidation[StringBuffer] =
    scalaz.Success(appendTo)
  def formatable: AlmFormatter = this
}