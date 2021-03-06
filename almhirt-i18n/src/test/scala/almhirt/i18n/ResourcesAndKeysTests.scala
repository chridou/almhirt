package almhirt.i18n

import scalaz.Validation.FlatMap._
import almhirt.common._
import almhirt.almvalidation.kit._
import org.scalatest._
import com.ibm.icu.util.ULocale
import MeasuredImplicits._

class ResourcesAndKeysTests extends FunSuite with Matchers {
  //val resourcesWithFallbackAllowed = AlmResources.fromXmlInResources("localization", "test", getClass.getClassLoader, true).forceResult
  val resourcesWithoutFallback =
    try {
      AlmResources.fromXmlInResources("localization", "test", getClass.getClassLoader, true, false, true).forceResult
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

  test("""render 'de-AT' which is a plain string""") {
    resourcesWithoutFallback.getFormatter(key1, new ULocale("de-AT")).flatMap(_.format()) should equal(scalaz.Success("de_AT"))
  }

  test("""render a key with a nonformatted value(integer: 2)""") {
    resourcesWithoutFallback.getFormatter(key2, new ULocale("en")).flatMap(_.formatValues(2)) should equal(scalaz.Success("en: 2"))
  }

  test("""render the length measured value 1.0m in "en".""") {
    val formatable = resourcesWithoutFallback.forceFormatter(group.withKey("length"), "en")
    info(formatable.forceFormat("length" -> 1.0))
  }

  test("""render the length measured value 100000.0m in "en".""") {
    val formatable = resourcesWithoutFallback.forceFormatter(group.withKey("length"), "en")
    info(formatable.forceFormat("length" -> 100000.0))
  }

  test("""render the length measured value 100000.0m in "en" with anglo american units.""") {
    val formatable = resourcesWithoutFallback.getMeasureFormatter(group.withKey("length"), "en").forceResult
    info(formatable.formatMeasure(100000.0.meter, Some(UnitsOfMeasurementSystem.US)).forceResult)
  }

  test("""render the length measured value 1.0m in "de".""") {
    val formatable = resourcesWithoutFallback.getMeasureFormatter(group.withKey("length"), "de").forceResult
    info(formatable.forceFormat("length" -> 1.0))
  }

  test("""render the length measured value 1000.0m in "de".""") {
    val formatable = resourcesWithoutFallback.getMeasureFormatter(group.withKey("length"), "de").forceResult
    info(formatable.forceFormat("length" -> 1000.0))
  }

  test("""render the length measured value 1000.0m in "de" with anglo american units.""") {
    val formatable = resourcesWithoutFallback.getMeasureFormatter(group.withKey("length"), "de").forceResult
    info(formatable.formatMeasure(1000.0.meter, Some(UnitsOfMeasurementSystem.US)).forceResult)
  }

  test("""render the length measured range 1000.0m - 2km in "de" with anglo american units.""") {
    val formatable = resourcesWithoutFallback.getMeasureFormatter(group.withKey("length"), "de").forceResult
    info(formatable.formatMeasureRange(1000.0.meter, 2.0.kilometer, Some(UnitsOfMeasurementSystem.US)).forceResult)
  }

  test("""render a number without a style in en.""") {
    val formatable = resourcesWithoutFallback.forceFormatter(group.withKey("number"), "en")
    info(formatable.forceFormatValues(12345.67))
  }

  test("""render a number explicitly without a style in en.""") {
    val formatable = resourcesWithoutFallback.forceFormatter(group.withKey("number-nostyle"), "en")
    info(formatable.forceFormatValues(12345.67))
  }

  test("""render a number with style integer in en.""") {
    val formatable = resourcesWithoutFallback.forceFormatter(group.withKey("number-integer"), "en")
    info(formatable.forceFormatValues(12345.67))
  }

  test("""render a number with style scientific in en.""") {
    val formatable = resourcesWithoutFallback.forceFormatter(group.withKey("number-scientific"), "en")
    info(formatable.forceFormatValues(12345.67))
  }

  test("""render a number with style percentage in en.""") {
    val formatable = resourcesWithoutFallback.forceFormatter(group.withKey("number-percentage"), "en")
    info(formatable.forceFormatValues(12345.67))
  }

  test("""render the boolean in en when true.""") {
    val formatable = resourcesWithoutFallback.forceFormatter(group.withKey("yesno"), "en")
    formatable.forceFormatValues(true) should equal("Yes!")
  }

  test("""render the boolean in en when false.""") {
    val formatable = resourcesWithoutFallback.forceFormatter(group.withKey("yesno"), "en")
    formatable.forceFormatValues(false) should equal("No!")
  }

  test("""render the boolean in en via the lookups extension methods.""") {
    import ResourceLookup.UnsafeFormattingImplicits._
    resourcesWithoutFallback.forceFormatValues(group.withKey("yesno"), "en", true) should equal("Yes!")
  }

  test("""select an existing text(alternative a).""") {
    val formatable = resourcesWithoutFallback.forceFormatter(group.withKey("select"), "en")
    info(formatable.forceFormatValues("a"))
  }

  test("""select an existing text(alternative b).""") {
    val formatable = resourcesWithoutFallback.forceFormatter(group.withKey("select"), "en")
    info(formatable.forceFormatValues("b"))
  }

  test("""select an existing text(non existing).""") {
    val formatable = resourcesWithoutFallback.forceFormatter(group.withKey("select"), "en")
    info(formatable.forceFormatValues("c"))
  }
}
