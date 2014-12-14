package almhirt.i18n

import scalaz.Validation.FlatMap._
import almhirt.common._
import almhirt.almvalidation.kit._
import org.scalatest._
import com.ibm.icu.util.ULocale
import MeasuredImplicits._
import com.ibm.icu.text.MessageFormat

class ResourcesAndKeysTests extends FunSuite with Matchers {
  //val resourcesWithFallbackAllowed = AlmResources.fromXmlInResources("localization", "test", getClass.getClassLoader, true).forceResult
  val resourcesWithoutFallback =
    try {
      AlmResources.fromXmlInResources("localization", "test", getClass.getClassLoader, false).forceResult
    } catch {
      case exn: ResultForcedFromValidationException ⇒
        info(exn.problem.toString)
        AlmResources.empty
      case scala.util.control.NonFatal(exn) ⇒
        throw exn
    }

  val key1 = ResourceKey("section_1", "group_1", "key_1")
  val key2 = ResourceKey("section_1", "group_1", "key_2")
  val group = ResourceGroup("section_1", "group_1")

  //  test("""must find 'en'""") {
  //    resourcesWithoutFallback.findTextResource(key1, new ULocale("en")).map(_.raw) should equal(Some("en"))
  //  }
  //
  //  test("""must find 'en-GB'""") {
  //    resourcesWithoutFallback.findTextResource(key1, new ULocale("en-GB")).map(_.raw) should equal(Some("en_GB"))
  //  }
  //
  //  test("""must find 'en_US'""") {
  //    resourcesWithoutFallback.findTextResource(key1, new ULocale("en_US")).map(_.raw) should equal(Some("en_US"))
  //  }
  //
  //  test("""must find 'de'""") {
  //    resourcesWithoutFallback.findTextResource(key1, new ULocale("de")).map(_.raw) should equal(Some("de"))
  //  }
  //
  //  test("""must find 'de-DE'""") {
  //    resourcesWithoutFallback.findTextResource(key1, new ULocale("de-DE")).map(_.raw) should equal(Some("de_DE"))
  //  }
  //
  //  test("""must find 'de-AT'""") {
  //    resourcesWithoutFallback.findTextResource(key1, new ULocale("de-AT")).map(_.raw) should equal(Some("de_AT"))
  //  }
  //
  //  test("""must find nothing for 'de-CH' when fallback is disabled.""") {
  //    resourcesWithoutFallback.findTextResource(key1, new ULocale("de-CH")).map(_.raw) should equal(None)
  //  }
  //
  //  test("""must find 'de' for 'de-CH' when fallback is enabled""") {
  //    resourcesWithFallbackAllowed.findTextResource(key1, new ULocale("de-CH")).map(_.raw) should equal(Some("de"))
  //  }

  test("""render 'de-AT' which is a plain string""") {
    resourcesWithoutFallback.formatable(key1, new ULocale("de-AT")).flatMap(_.render()) should equal(scalaz.Success("de_AT"))
  }

  test("""render a key with a nonformatted value(integer: 2)""") {
    resourcesWithoutFallback.formatable(key2, new ULocale("en")).flatMap(_.render()) should equal(scalaz.Success("en: 2"))
  }

  test("""render the length measured value 1.0m in "en".""") {
    val formatable = resourcesWithoutFallback.forceFormatable(group.withKey("length"), "en")
    info(formatable.forceRender("length" -> 1.0))
  }

  test("""render the length measured value 100000.0m in "en".""") {
    val formatable = resourcesWithoutFallback.forceFormatable(group.withKey("length"), "en")
    info(formatable.forceRender("length" -> 100000.0))
  }

  test("""render the length measured value 100000.0m in "en" with anglo american units.""") {
    val formatable = resourcesWithoutFallback.forceFormatable(group.withKey("length"), "en")
    info(formatable.forceRender("length" -> MeasuredValueArg.SiArg(100000.0, Some(UnitsOfMeasurementSystem.AngloAmerican))))
  }

  test("""render the length measured value 1.0m in "de".""") {
    val formatable = resourcesWithoutFallback.forceFormatable(group.withKey("length"), "de")
    info(formatable.forceRender("length" -> 1.0))
  }

  test("""render the length measured value 1000.0m in "de".""") {
    val formatable = resourcesWithoutFallback.forceFormatable(group.withKey("length"), "de")
    info(formatable.forceRender("length" -> 1000.0))
  }

  test("""render the length measured value 1000.0m in "de" with anglo american units.""") {
    val formatable = resourcesWithoutFallback.forceFormatable(group.withKey("length"), "de")
    info(formatable.forceRender("length" -> MeasuredValueArg.SiArg(1000.0, Some(UnitsOfMeasurementSystem.AngloAmerican))))
  }

  test("""render the boolean in en when true.""") {
    val formatable = resourcesWithoutFallback.forceFormatable(group.withKey("yesno"), "en")
    info(formatable.withUnnamedArg(true).forceRender)
  }

  test("""render the boolean in en when false.""") {
    val formatable = resourcesWithoutFallback.forceFormatable(group.withKey("yesno"), "en")
    info(formatable.withUnnamedArg(false).forceRender)
  }

  test("""select an existing text(alternative a).""") {
    val formatable = resourcesWithoutFallback.forceFormatable(group.withKey("select"), "en")
    info(formatable.withUnnamedArg("a").forceRender)
  }

  test("""select an existing text(alternative b).""") {
    val formatable = resourcesWithoutFallback.forceFormatable(group.withKey("select"), "en")
    info(formatable.withUnnamedArg("b").forceRender)
  }

  test("""select an existing text(non existing).""") {
    val formatable = resourcesWithoutFallback.forceFormatable(group.withKey("select"), "en")
    info(formatable.withUnnamedArg("c").forceRender)
  }
}
