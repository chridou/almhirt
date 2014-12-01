package almhirt.i18n

import almhirt.almvalidation.kit._
import org.scalatest._
import com.ibm.icu.util.ULocale

class ResourcesAndKeysTests extends FunSuite with Matchers {
  val resourcesWithFallbackAllowed = AlmResources.fromXmlInResources("localization", "test", getClass.getClassLoader, true).forceResult
  val resourcesWithoutFallback = AlmResources.fromXmlInResources("localization", "test", getClass.getClassLoader, false).forceResult
  
  val key1 = ResourceKey("section_1", "group_1", "key_1")

  test("""must find 'en'""") {
    resourcesWithoutFallback.findResource(key1, new ULocale("en")) should equal(Some("en"))
  }

  test("""must find 'en-GB'""") {
    resourcesWithoutFallback.findResource(key1, new ULocale("en-GB")) should equal(Some("en_GB"))
  }

  test("""must find 'en_US'""") {
    resourcesWithoutFallback.findResource(key1, new ULocale("en_US")) should equal(Some("en_US"))
  }

  test("""must find 'de'""") {
    resourcesWithoutFallback.findResource(key1, new ULocale("de")) should equal(Some("de"))
  }

  test("""must find 'de-DE'""") {
    resourcesWithoutFallback.findResource(key1, new ULocale("de-DE")) should equal(Some("de_DE"))
  }

  test("""must find 'de-AT'""") {
    resourcesWithoutFallback.findResource(key1, new ULocale("de-AT")) should equal(Some("de_AT"))
  }

  test("""must find nothing for 'de-CH' when fallback is disabled.""") {
    resourcesWithoutFallback.findResource(key1, new ULocale("de-CH")) should equal(None)
  }

  test("""must find 'de' for 'de-CH' when fallback is enabled""") {
    resourcesWithFallbackAllowed.findResource(key1, new ULocale("de-CH")) should equal(Some("de"))
  }
  
}
