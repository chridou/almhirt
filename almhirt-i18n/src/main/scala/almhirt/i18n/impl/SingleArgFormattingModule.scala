package almhirt.i18n.impl

import java.text.FieldPosition
import almhirt.common._
import com.ibm.icu.util.ULocale

private[almhirt] trait SingleArgFormattingModule {
  def locale: ULocale
  def argname: String
  def formatIntoBuffer(appendTo: StringBuffer, pos: FieldPosition, arg: Any): AlmValidation[StringBuffer]
}