package almhirt.i18n

import com.ibm.icu.util.ULocale

trait LocaleMagnet[L] {
  def toULocale(what: L): ULocale
}

