package almhirt.localization

import scalaz.syntax.validation._
import almhirt.common._

trait SupportedLocales {
  def supportedLocales: Seq[String]
  def isSupported(locale: String): Boolean = supportedLocales.contains(locale)
  def supported(locale: String): AlmValidation[String]

}

object SupportedLocales {
  def apply(): SupportedLocales = new SupportedLocales {
    val supportedLocales: Seq[String] = Seq.empty
    override def isSupported(locale: String): Boolean = false
    override def supported(locale: String): AlmValidation[String] = LocaleNotSupportedProblem(s""""$locale" is not supported. This provider is empty and supports no locale at all.""").failure
  }

  def apply(theSupportedLocales: Seq[String], convertToTwoLetter: Boolean): SupportedLocales = {
    def toValid(loc: String): String =
      if (convertToTwoLetter)
        loc.take(2)
      else
        loc

    val supportedSet = theSupportedLocales.toSet
    new SupportedLocales {
      override val supportedLocales: Seq[String] = theSupportedLocales
      override def isSupported(locale: String): Boolean = supportedSet(toValid(locale))
      override def supported(locale: String): AlmValidation[String] = {
        if (isSupported(locale))
          toValid(locale).success
        else {
          val args = Map("unsupported-locale" → locale, "supported-locales" → supportedLocales.toList)
          LocaleNotSupportedProblem(s""""$locale" is not supported. The supported locales are [${supportedLocales.mkString(" ,")}].""").failure
        }
      }
    }
  }
}