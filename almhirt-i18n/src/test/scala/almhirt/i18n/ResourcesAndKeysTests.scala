package almhirt.i18n

import scalaz.Validation.FlatMap._
import almhirt.almvalidation.kit._
import org.scalatest._
import com.ibm.icu.util.ULocale
import MeasuredImplicits._

class ResourcesAndKeysTests extends FunSuite with Matchers {
  val resourcesWithFallbackAllowed = AlmResources.fromXmlInResources("localization", "test", getClass.getClassLoader, true).forceResult
  val resourcesWithoutFallback = AlmResources.fromXmlInResources("localization", "test", getClass.getClassLoader, false).forceResult
  
  val key1 = ResourceKey("section_1", "group_1", "key_1")
  val key2 = ResourceKey("section_1", "group_1", "key_2")

  test("""must find 'en'""") {
    resourcesWithoutFallback.findResource(key1, new ULocale("en")).map(_.raw) should equal(Some("en"))
  }

  test("""must find 'en-GB'""") {
    resourcesWithoutFallback.findResource(key1, new ULocale("en-GB")).map(_.raw) should equal(Some("en_GB"))
  }

  test("""must find 'en_US'""") {
    resourcesWithoutFallback.findResource(key1, new ULocale("en_US")).map(_.raw) should equal(Some("en_US"))
  }

  test("""must find 'de'""") {
    resourcesWithoutFallback.findResource(key1, new ULocale("de")).map(_.raw) should equal(Some("de"))
  }

  test("""must find 'de-DE'""") {
    resourcesWithoutFallback.findResource(key1, new ULocale("de-DE")).map(_.raw) should equal(Some("de_DE"))
  }

  test("""must find 'de-AT'""") {
    resourcesWithoutFallback.findResource(key1, new ULocale("de-AT")).map(_.raw) should equal(Some("de_AT"))
  }

  test("""must find nothing for 'de-CH' when fallback is disabled.""") {
    resourcesWithoutFallback.findResource(key1, new ULocale("de-CH")).map(_.raw) should equal(None)
  }

  test("""must find 'de' for 'de-CH' when fallback is enabled""") {
    resourcesWithFallbackAllowed.findResource(key1, new ULocale("de-CH")).map(_.raw) should equal(Some("de"))
  }
 
  test("""render 'de-AT' which is a plain string""") {
    resourcesWithoutFallback.icuFormattable(key1, new ULocale("de-AT")).flatMap(_.render).toOption should equal(Some("de_AT"))
  }
 
  test("""render a key with a nonformatted value(integer: 2)""") {
    resourcesWithoutFallback.icuFormattable(key2, new ULocale("en")).flatMap(_.withRawArg("1" -> 2).render).toOption should equal(Some("en: 2"))
  }

  test("""render a key with a measured value(2m as 2.0 length-meter) en""") {
    resourcesWithoutFallback.icuFormattable(key2, new ULocale("en")).flatMap(_.withMeasuredValue("1" -> 2.meter).render).toOption should equal(Some("en: 2.0 length-meter"))
  }

  test("""render a key with a measured value(2m as 2.0 length-meter) de-DE""") {
    resourcesWithoutFallback.icuFormattable(key2, new ULocale("de-DE")).flatMap(_.withMeasuredValue("1" -> 2.meter).render).toOption should equal(Some("en: 2.0 length-meter"))
  }
  
}
