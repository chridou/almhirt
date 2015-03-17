package almhirt.i18n

import scalaz.Validation.FlatMap._
import almhirt.common._
import com.ibm.icu.util.ULocale

trait ItemFormatter[T] {
  def appendToBuffer(what: T, locale: ULocale, uomSys: Option[UnitsOfMeasurementSystem], appendTo: StringBuffer)(implicit lookUp: ResourceLookup): AlmValidation[StringBuffer]

  final def appendToBuffer(what: T, locale: ULocale, appendTo: StringBuffer)(implicit lookUp: ResourceLookup): AlmValidation[StringBuffer] =
    appendToBuffer(what, locale, None, appendTo)
}