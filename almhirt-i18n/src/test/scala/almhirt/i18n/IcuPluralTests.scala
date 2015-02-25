package almhirt.i18n

import scalaz.Validation.FlatMap._
import almhirt.common._
import almhirt.almvalidation.kit._
import org.scalatest._
import com.ibm.icu.util.ULocale
import MeasuredImplicits._

class IcuPluralTests extends FunSuite with Matchers {
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

  val resourceKey = ResourceKey("section_1", "group_1", "plural_number_no_decimal_separator")

  test("""render the plural string "None" for value 0""") {
    val formatable = resourcesWithoutFallback.forceFormatter(resourceKey, "en")
    val res = formatable.forceFormat("amount" -> 0)
    res should equal("None")
  }

  test("""render the plural string "None" for value 0.0""") {
    val formatable = resourcesWithoutFallback.forceFormatter(resourceKey, "en")
    val res = formatable.forceFormat("amount" -> 0)
    res should equal("None")
  }

  test("""render the plural string "One" for value 1""") {
    val formatable = resourcesWithoutFallback.forceFormatter(resourceKey, "en")
    val res = formatable.forceFormat("amount" -> 1)
    res should equal("One")
  }

  test("""render the plural string "One" for value 1.0""") {
    val formatable = resourcesWithoutFallback.forceFormatter(resourceKey, "en")
    val res = formatable.forceFormat("amount" -> 1.0)
    res should equal("One")
  }

  test("""render the plural string "2 items" for value 2""") {
    val formatable = resourcesWithoutFallback.forceFormatter(resourceKey, "en")
    val res = formatable.forceFormat("amount" -> 2)
    res should equal("2 items")
  }

  test("""render the plural string "2 items" for value 2.0""") {
    val formatable = resourcesWithoutFallback.forceFormatter(resourceKey, "en")
    val res = formatable.forceFormat("amount" -> 2.0)
    res should equal("2 items")
  }

  test("""render the plural string "1000 items" for value 1000""") {
    val formatable = resourcesWithoutFallback.forceFormatter(resourceKey, "en")
    val res = formatable.forceFormat("amount" -> 1000)
    res should equal("1000 items")
  }

  test("""render the plural string  "1000 items" for value 1000.0""") {
    val formatable = resourcesWithoutFallback.forceFormatter(resourceKey, "en")
    val res = formatable.forceFormat("amount" -> 1000.0)
    res should equal("1000 items")
  }

}