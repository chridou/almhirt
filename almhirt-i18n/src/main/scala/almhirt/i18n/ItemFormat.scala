package almhirt.i18n

import scalaz.Validation.FlatMap._
import almhirt.common._
import com.ibm.icu.util.ULocale

trait ItemFormat[T] {
  def appendTo(what: T, locale: ULocale, appendTo: StringBuffer)(implicit lookUp: ResourceLookup): AlmValidation[StringBuffer]

  def createKey(what: T): ResourceKey

  protected def withFormatable(what: T, locale: ULocale, lookup: ResourceLookup)(f: (T, Formatable) ⇒ AlmValidation[StringBuffer]): AlmValidation[StringBuffer] =
    for {
      formatable ← lookup.formatable(createKey(what), locale)
      rendered ← f(what, formatable)
    } yield rendered
}