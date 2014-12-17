package almhirt.i18n

import scalaz.Validation.FlatMap._
import almhirt.common._
import com.ibm.icu.util.ULocale

trait ItemFormat[T] {
  def appendTo(what: T, locale: ULocale, appendTo: StringBuffer)(implicit lookUp: ResourceLookup): AlmValidation[StringBuffer]
}