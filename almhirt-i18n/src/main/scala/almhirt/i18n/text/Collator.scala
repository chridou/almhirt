package almhirt.i18n.text

import scalaz.Validation.FlatMap._
import almhirt.common._
import com.ibm.icu.util.ULocale
import com.ibm.icu.text.{ Collator ⇒ UCollator }
import almhirt.i18n.LocaleMagnet

sealed trait CollatorStrength {
  private[almhirt] def icuStrength: Int
}

object CollatorStrength {
  /**
   * Typically, this is used to denote differences between base characters (for example, "a" < "b").
   * It is the strongest difference.
   * For example, dictionaries are divided into different sections by base character.
   *
   */
  object Primary extends CollatorStrength {
    val icuStrength = UCollator.PRIMARY
  }
  /**
   * Accents in the characters are considered secondary differences (for example, "as" < "às" < "at").
   * Other differences between letters can also be considered secondary differences, depending on the language.
   * A secondary difference is ignored when there is a primary difference anywhere in the strings.
   */
  object Secondary extends CollatorStrength {
    val icuStrength = UCollator.SECONDARY
  }
  /**
   * Upper and lower case differences in characters are distinguished at tertiary strength (for example, "ao" < "Ao" < "aò").
   * In addition, a variant of a letter differs from the base form on the tertiary strength (such as "A" and "Ⓐ").
   * Another example is the difference between large and small Kana.
   * A tertiary difference is ignored when there is a primary or secondary difference anywhere in the strings.
   */
  object Tertiary extends CollatorStrength {
    val icuStrength = UCollator.TERTIARY
  }
  /**
   * When punctuation is ignored (see Ignoring Punctuations in the User Guide) at PRIMARY to TERTIARY strength,
   * an additional strength level can be used to distinguish words with and without punctuation (for example, "ab" < "a-b" < "aB").
   * This difference is ignored when there is a PRIMARY, SECONDARY or TERTIARY difference.
   * The QUATERNARY strength should only be used if ignoring punctuation is required.
   */
  object Quaternary extends CollatorStrength {
    val icuStrength = UCollator.QUATERNARY
  }
  /**
   * When all other strengths are equal, the IDENTICAL strength is used as a tiebreaker.
   * The Unicode code point values of the NFD form of each string are compared, just in case there is no difference.
   * For example, Hebrew cantellation marks are only distinguished at this strength. This strength should be used sparingly, as only code point value differences between two strings is an extremely rare occurrence.
   * Using this strength substantially decreases the performance for both comparison and collation key generation APIs.
   * This strength also increases the size of the collation key.
   */
  object Identical extends CollatorStrength {
    val icuStrength = UCollator.IDENTICAL
  }
}

trait Collator extends Ordering[String] {
  def originalLocale: ULocale
  def strength: CollatorStrength

  def sort[C <: scala.collection.SeqLike[String, C]](collection: C): C =
    collection.sorted(this)
}

object Collator {
  def apply[L: LocaleMagnet](locale: L, strength: Option[CollatorStrength]): Collator = {
    val uLoc = implicitly[LocaleMagnet[L]].toULocale(locale)
      val collator = UCollator.getInstance(uLoc)
      strength.foreach { str ⇒ collator.setStrength(str.icuStrength) }
      collator.freeze()
      new CollatorImpl(collator, uLoc)
  }
}

private[almhirt] final class CollatorImpl(underlying: UCollator, override val originalLocale: ULocale) extends Collator {
  override val strength: CollatorStrength =
    underlying.getStrength match {
      case UCollator.PRIMARY    ⇒ CollatorStrength.Primary
      case UCollator.SECONDARY  ⇒ CollatorStrength.Secondary
      case UCollator.TERTIARY   ⇒ CollatorStrength.Tertiary
      case UCollator.QUATERNARY ⇒ CollatorStrength.Quaternary
      case UCollator.IDENTICAL  ⇒ CollatorStrength.Identical
    }

  override def compare(a: String, b: String): Int =
    underlying.compare(a, b)
    

}