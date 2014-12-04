package almhirt.i18n

import scalaz.Validation.FlatMap._
import almhirt.common._
import com.ibm.icu.util.ULocale

trait ItemFormat[T] {
  def prepare(what: T, locale: ULocale, lookUp: ResourceLookup): AlmValidation[CanRenderToString]

  def createKey(what: T): ResourceKey

  protected def withFormatable(what: T, locale: ULocale, lookup: ResourceLookup)(f: (T, Formatable) ⇒ AlmValidation[CanRenderToString]): AlmValidation[CanRenderToString] =
    for {
      formatable ← lookup.formatable(createKey(what), locale)
      renderable ← f(what, formatable)
    } yield renderable
}