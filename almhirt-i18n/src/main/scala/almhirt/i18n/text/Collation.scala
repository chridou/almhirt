package almhirt.i18n.text

import scalaz.Validation.FlatMap._
import almhirt.common._
import com.ibm.icu.text.{ Collator }
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
    val icuStrength = Collator.PRIMARY
  }
  /**
   * A secondary difference is ignored when there is a primary difference anywhere in the strings.
   * Other differences between letters can also be considered secondary differences, depending on the language.
   */
  object Secondary extends CollatorStrength {
    val icuStrength = Collator.SECONDARY
  }
  /**
   * When punctuation is ignored (see Ignoring Punctuations in the User Guide) at PRIMARY to TERTIARY strength,
   * an additional strength level can be used to distinguish words with and without punctuation (for example, "ab" < "a-b" < "aB").
   * This difference is ignored when there is a PRIMARY, SECONDARY or TERTIARY difference. The QUATERNARY strength should only be used if ignoring punctuation is required.
   */
  object Tertiary extends CollatorStrength {
    val icuStrength = Collator.TERTIARY
  }
  /**
   * This difference is ignored when there is a PRIMARY, SECONDARY or TERTIARY difference.
   * The QUATERNARY strength should only be used if ignoring punctuation is required.
   */
  object Quaternary extends CollatorStrength {
    val icuStrength = Collator.QUATERNARY
  }
  /**
   * When all other strengths are equal, the IDENTICAL strength is used as a tiebreaker.
   * The Unicode code point values of the NFD form of each string are compared, just in case there is no difference.
   * For example, Hebrew cantellation marks are only distinguished at this strength. This strength should be used sparingly, as only code point value differences between two strings is an extremely rare occurrence.
   * Using this strength substantially decreases the performance for both comparison and collation key generation APIs.
   * This strength also increases the size of the collation key.
   */
  object Identical extends CollatorStrength {
    val icuStrength = Collator.IDENTICAL
  }
}

object Collation {
  def collator[L: LocaleMagnet](locale: L, strength: Option[CollatorStrength]): AlmValidation[Collator] = {
    val uLoc = implicitly[LocaleMagnet[L]].toULocale(locale)
    inTryCatch {
      val collator = Collator.getInstance(uLoc)
      strength.foreach { str ⇒ collator.setStrength(str.icuStrength) }
      collator.freeze()
      collator
    }
  }

  def createOrdering[L: LocaleMagnet](locale: L, strength: Option[CollatorStrength]): AlmValidation[Ordering[String]] = {
    for {
      collator ← collator(locale, strength)
    } yield new Ordering[String] {
      override def compare(a: String, b: String): Int =
        collator.compare(a, b)
    }
  }

  def sort[L: LocaleMagnet](locale: L, items: Seq[String], strength: Option[CollatorStrength]): AlmValidation[Seq[String]] = {
    createOrdering(locale, strength).map { implicit x => items.sorted }
  }

}