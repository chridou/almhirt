package almhirt.i18n

import scalaz.Validation.FlatMap._
import almhirt.common._
import almhirt.almvalidation.kit._
import org.scalatest._
import com.ibm.icu.util.ULocale

class RangeSelectionOfManyTests extends FunSpec with Matchers {
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

  val selectionOfManyKey = ResourceKey("section_2", "group_1", "selection-of-many-1")

  describe("A RangeSelectionOfMany-Formatter") {
    describe("when the overall number of items is zero") {
      describe("and no other parameter is set") {
        it("should return the correct text") {
          val formatable = resourcesWithoutFallback.forceFormatter(selectionOfManyKey, "en")
          val res = formatable.forceFormat("all" -> 0)
          info(res)
          res should equal("Nothing to select")
        }
      }
      describe("and lower-index is set") {
        it("should return the correct text") {
          val formatable = resourcesWithoutFallback.forceFormatter(selectionOfManyKey, "en")
          val res = formatable.forceFormat("all" -> 0, "lower-index" -> 0)
          info(res)
          res should equal("Nothing to select")
        }
      }
      describe("and upper-index is set") {
        it("should return the correct text") {
          val formatable = resourcesWithoutFallback.forceFormatter(selectionOfManyKey, "en")
          val res = formatable.forceFormat("all" -> 0, "upper-index" -> 1)
          info(res)
          res should equal("Nothing to select")
        }
      }
      describe("and lower-index and upper-index are set") {
        it("should return the correct text") {
          val formatable = resourcesWithoutFallback.forceFormatter(selectionOfManyKey, "en")
          val res = formatable.forceFormat("all" -> 0, "lower-index" -> 0, "upper-index" -> 1)
          info(res)
          res should equal("Nothing to select")
        }
      }
    }
    describe("when the overall number of items is 1") {
      describe("and lower-index=0 and upper-index=0 ") {
        it("should return the correct text") {
          val formatable = resourcesWithoutFallback.forceFormatter(selectionOfManyKey, "en")
          val res = formatable.forceFormat("all" -> 1, "lower-index" -> 0, "upper-index" -> 0)
          info(res)
          res should equal("Item 0 from one item selected")
        }
      }
      describe("and lower-index=1 and upper-index=1 ") {
        it("should return the correct text") {
          val formatable = resourcesWithoutFallback.forceFormatter(selectionOfManyKey, "en")
          val res = formatable.forceFormat("all" -> 1, "lower-index" -> 1, "upper-index" -> 1)
          info(res)
          res should equal("Item 1 from one item selected")
        }
      }
      describe("and lower-index=0 and upper-index=1 ") {
        it("should return the correct text") {
          val formatable = resourcesWithoutFallback.forceFormatter(selectionOfManyKey, "en")
          val res = formatable.forceFormat("all" -> 1, "lower-index" -> 0, "upper-index" -> 1)
          info(res)
          res should equal("Items 0 to 1 from one item selected")
        }
      }
      describe("and lower-index=1 and upper-index=3 ") {
        it("should return the correct text") {
          val formatable = resourcesWithoutFallback.forceFormatter(selectionOfManyKey, "en")
          val res = formatable.forceFormat("all" -> 1, "lower-index" -> 1, "upper-index" -> 3)
          info(res)
          res should equal("Items 1 to 3 from one item selected")
        }
      }
      describe("and lower-index=3 and upper-index=1 ") {
        it("should return the correct text") {
          val formatable = resourcesWithoutFallback.forceFormatter(selectionOfManyKey, "en")
          val res = formatable.forceFormat("all" -> 1, "lower-index" -> 3, "upper-index" -> 1)
          info(res)
          res should equal("No item from one item selected")
        }
      }
      describe("and lower-index=3") {
        it("should return the correct text") {
          val formatable = resourcesWithoutFallback.forceFormatter(selectionOfManyKey, "en")
          val res = formatable.forceFormat("all" -> 1, "lower-index" -> 3)
          info(res)
          res should equal("No item from one item selected")
        }
      }
      describe("and upper-index=3") {
        it("should return the correct text") {
          val formatable = resourcesWithoutFallback.forceFormatter(selectionOfManyKey, "en")
          val res = formatable.forceFormat("all" -> 1, "upper-index" -> 3)
          info(res)
          res should equal("No item from one item selected")
        }
      }
      it("should return the correct text") {
        val formatable = resourcesWithoutFallback.forceFormatter(selectionOfManyKey, "en")
        val res = formatable.forceFormat("all" -> 1)
        info(res)
        res should equal("No item from one item selected")
      }
    }
    describe("when the overall number of items is 2") {
      describe("and lower-index=0 and upper-index=0 ") {
        it("should return the correct text") {
          val formatable = resourcesWithoutFallback.forceFormatter(selectionOfManyKey, "en")
          val res = formatable.forceFormat("all" -> 1, "lower-index" -> 0, "upper-index" -> 0)
          info(res)
          res should equal("Item 0 from 2 items selected")
        }
      }
      describe("and lower-index=1 and upper-index=1 ") {
        it("should return the correct text") {
          val formatable = resourcesWithoutFallback.forceFormatter(selectionOfManyKey, "en")
          val res = formatable.forceFormat("all" -> 1, "lower-index" -> 1, "upper-index" -> 1)
          info(res)
          res should equal("Item 1 from 2 items selected")
        }
      }
      describe("and lower-index=0 and upper-index=1 ") {
        it("should return the correct text") {
          val formatable = resourcesWithoutFallback.forceFormatter(selectionOfManyKey, "en")
          val res = formatable.forceFormat("all" -> 1, "lower-index" -> 0, "upper-index" -> 1)
          info(res)
          res should equal("Items 0 to 1 from 2 items selected")
        }
      }
      describe("and lower-index=1 and upper-index=3 ") {
        it("should return the correct text") {
          val formatable = resourcesWithoutFallback.forceFormatter(selectionOfManyKey, "en")
          val res = formatable.forceFormat("all" -> 1, "lower-index" -> 1, "upper-index" -> 3)
          info(res)
          res should equal("Items 1 to 3 from 2 items selected")
        }
      }
      describe("and lower-index=3 and upper-index=1 ") {
        it("should return the correct text") {
          val formatable = resourcesWithoutFallback.forceFormatter(selectionOfManyKey, "en")
          val res = formatable.forceFormat("all" -> 1, "lower-index" -> 3, "upper-index" -> 1)
          info(res)
          res should equal("No item from 2 items selected")
        }
      }
      describe("and lower-index=3") {
        it("should return the correct text") {
          val formatable = resourcesWithoutFallback.forceFormatter(selectionOfManyKey, "en")
          val res = formatable.forceFormat("all" -> 1, "lower-index" -> 3)
          info(res)
          res should equal("No item from 2 items selected")
        }
      }
      describe("and upper-index=3") {
        it("should return the correct text") {
          val formatable = resourcesWithoutFallback.forceFormatter(selectionOfManyKey, "en")
          val res = formatable.forceFormat("all" -> 1, "upper-index" -> 3)
          info(res)
          res should equal("No item from 2 items selected")
        }
      }
      it("should return the correct text") {
        val formatable = resourcesWithoutFallback.forceFormatter(selectionOfManyKey, "en")
        val res = formatable.forceFormat("all" -> 1)
        info(res)
        res should equal("No item from 2 items selected")
      }
    }
  }
}