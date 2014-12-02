package almhirt.i18n

import almhirt.common._
import com.ibm.icu.util.ULocale

trait ItemFormatter[T] {
  def prepare(what: T, locale: ULocale, lookUp: ResourceLookup): AlmValidation[Formatable]
}