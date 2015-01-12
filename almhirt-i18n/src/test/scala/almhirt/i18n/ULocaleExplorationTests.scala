package almhirt.i18n

import org.scalatest._
import com.ibm.icu.util.ULocale

class ULocaleExplorationTests extends FunSuite with Matchers {
  test("the base name for DE-DE is normalized to de_DE") {
    val loc = new ULocale("DE-DE")
    loc.getBaseName should equal("de_DE")
  }

  test("the language for DE-DE is Some(de)") {
    val loc = new ULocale("DE-DE")
    loc.language should equal(Some("de"))
  }

  test("the country for DE-DE is Some(DE)") {
    val loc = new ULocale("DE-de")
    loc.country should equal(Some("DE"))
  }

  test("the script for DE-DE is None") {
    val loc = new ULocale("DE-de")
    loc.script should equal(None)
  }

  test("the script for sR-LaTn_rS is Some(Latn)") {
    val loc = new ULocale("sR-LaTn_rS")
    loc.script should equal(Some("Latn"))
  }

  test("the display language for sR-LaTn_rS is") {
    val loc = new ULocale("sR-LaTn_rS")
    info(loc.getDisplayName(new ULocale("en-US")))
    info(loc.getDisplayCountry)
    info(loc.getDisplayVariant)
  }
  
  test("the display language for en-UK is") {
    val loc = new ULocale("en-UK")
    info(loc.getDisplayName(new ULocale("de-DE")))
    info(loc.getDisplayCountry)
    info(loc.getDisplayVariant)
  }
  
  test("the display language for de-DE is") {
    val loc = new ULocale("en-UK")
    info(loc.getDisplayName(new ULocale("de-DE")))
    info(loc.getDisplayCountry)
    info(loc.getDisplayVariant)
  }
}