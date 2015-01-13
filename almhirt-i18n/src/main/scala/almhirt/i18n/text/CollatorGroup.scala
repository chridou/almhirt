package almhirt.i18n.text

import almhirt.common._
import com.ibm.icu.util.ULocale
import almhirt.i18n.LocaleMagnet

/**
 * Access collator functions by locale.
 * Acts as a cache of precreated [[Collator]]s but can also create new ones on demand.
 */
trait Collators {
  def strength: Option[CollatorStrength]

  def originalLocales: Set[ULocale]

  def get[L: LocaleMagnet](locale: L): AlmValidation[Collator]

  final def getOrCreate[L: LocaleMagnet](locale: L): Collator =
    get(locale) match {
      case scalaz.Success(collator) ⇒ collator
      case scalaz.Failure(_)        ⇒ Collator(locale, strength)
    }

  final def find[L: LocaleMagnet](locale: L): Option[Collator] =
    get(locale).toOption

  final def sort[C <: scala.collection.SeqLike[String, C], L: LocaleMagnet](locale: L, collection: C): C =
    collection.sorted(getOrCreate(locale))
}

object Collators {
  def apply[L: LocaleMagnet](sharedStrength: Option[CollatorStrength]): Collators =
    apply(Seq.empty, sharedStrength)

  def apply[L: LocaleMagnet](locales: Seq[L], sharedStrength: Option[CollatorStrength]): Collators = {
    val magnet = implicitly[LocaleMagnet[L]]
    val mapped = locales.map(magnet.toULocale(_)).map(uLoc ⇒ (uLoc, Collator(uLoc, sharedStrength))).toMap

    new Collators {
      override val strength = sharedStrength
      override val originalLocales: Set[ULocale] = mapped.keySet
      override def get[L: LocaleMagnet](locale: L): AlmValidation[Collator] = {
        val magnet = implicitly[LocaleMagnet[L]]
        val uLoc = magnet.toULocale(locale)
        mapped get (uLoc) match {
          case Some(collator) ⇒ scalaz.Success(collator)
          case None           ⇒ scalaz.Failure(NotFoundProblem(s"""No collator found for locale "${uLoc.toLanguageTag()}"."""))
        }
      }
    }
  }
}