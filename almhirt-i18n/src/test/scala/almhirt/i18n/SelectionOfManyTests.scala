package almhirt.i18n

import scalaz.Validation.FlatMap._
import almhirt.common._
import almhirt.almvalidation.kit._
import org.scalatest._
import com.ibm.icu.util.ULocale

class SelectionOfManyTests extends FunSpec with Matchers {
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

  describe("A RangeSelectionOfMany-Formatter") {
    describe("when range-selection and amount-selection are present") {
      val selectionOfManyKey = ResourceKey("section_2", "group_1", "selection-of-many-all")
      describe("when the overall number of items is zero") {
        describe("and no other parameter is set") {
          it("should return the correct text") {
            val formatable = resourcesWithoutFallback.forceFormatter(selectionOfManyKey, "en")
            val res = formatable.forceFormat("all_items_count" -> 0)
            info(res)
            res should equal("Nothing to select")
          }
        }
        describe("and lower_index is set") {
          it("should return the correct text") {
            val formatable = resourcesWithoutFallback.forceFormatter(selectionOfManyKey, "en")
            val res = formatable.forceFormat("all_items_count" -> 0, "lower_index" -> 0)
            info(res)
            res should equal("Nothing to select")
          }
        }
        describe("and selection_size is set") {
          it("should return the correct text") {
            val formatable = resourcesWithoutFallback.forceFormatter(selectionOfManyKey, "en")
            val res = formatable.forceFormat("all_items_count" -> 0, "selection_size" -> 1)
            info(res)
            res should equal("Nothing to select")
          }
        }
        describe("and lower_index and selection_size are set") {
          it("should return the correct text") {
            val formatable = resourcesWithoutFallback.forceFormatter(selectionOfManyKey, "en")
            val res = formatable.forceFormat("all_items_count" -> 0, "lower_index" -> 0, "selection_size" -> 1)
            info(res)
            res should equal("Nothing to select")
          }
        }
      }
      describe("when the overall number of items is 1") {
        describe("and no other parameter is set") {
          it("should return the correct text") {
            val formatable = resourcesWithoutFallback.forceFormatter(selectionOfManyKey, "en")
            val res = formatable.forceFormat("all_items_count" -> 1)
            info(res)
            res should equal("Nothing from one item selected")
          }
        }
        describe("and lower_index=0 and selection_size=0 ") {
          it("should return the correct text") {
            val formatable = resourcesWithoutFallback.forceFormatter(selectionOfManyKey, "en")
            val res = formatable.forceFormat("all_items_count" -> 1, "lower_index" -> 0, "selection_size" -> 0)
            info(res)
            res should equal("Nothing from one item selected")
          }
        }
        describe("and lower_index=1 and selection_size=0 ") {
          it("should return the correct text") {
            val formatable = resourcesWithoutFallback.forceFormatter(selectionOfManyKey, "en")
            val res = formatable.forceFormat("all_items_count" -> 1, "lower_index" -> 1, "selection_size" -> 0)
            info(res)
            res should equal("Nothing from one item selected")
          }
        }
        describe("and lower_index=1") {
          it("should return the correct text") {
            val formatable = resourcesWithoutFallback.forceFormatter(selectionOfManyKey, "en")
            val res = formatable.forceFormat("all_items_count" -> 1, "lower_index" -> 1)
            info(res)
            res should equal("ItemR 1 from one item selected")
          }
        }
        describe("and lower_index=1 and selection_size=1 ") {
          it("should return the correct text") {
            val formatable = resourcesWithoutFallback.forceFormatter(selectionOfManyKey, "en")
            val res = formatable.forceFormat("all_items_count" -> 1, "lower_index" -> 1, "selection_size" -> 1)
            info(res)
            res should equal("ItemR 1 from one item selected")
          }
        }
        describe("and lower_index=1 and selection_size=2 ") {
          it("should return the correct text") {
            val formatable = resourcesWithoutFallback.forceFormatter(selectionOfManyKey, "en")
            val res = formatable.forceFormat("all_items_count" -> 1, "lower_index" -> 1, "selection_size" -> 2)
            info(res)
            res should equal("ItemsR 1 to 2 from one item selected")
          }
        }

        describe("and selection_size=0 ") {
          it("should return the correct text") {
            val formatable = resourcesWithoutFallback.forceFormatter(selectionOfManyKey, "en")
            val res = formatable.forceFormat("all_items_count" -> 1, "selection_size" -> 0)
            info(res)
            res should equal("Nothing from one item selected")
          }
        }
        describe("and selection_size=1") {
          it("should return the correct text") {
            val formatable = resourcesWithoutFallback.forceFormatter(selectionOfManyKey, "en")
            val res = formatable.forceFormat("all_items_count" -> 1, "selection_size" -> 1)
            info(res)
            res should equal("One itemA from one item selected")
          }
        }
        describe("and selection_size=2") {
          it("should return the correct text") {
            val formatable = resourcesWithoutFallback.forceFormatter(selectionOfManyKey, "en")
            val res = formatable.forceFormat("all_items_count" -> 1, "selection_size" -> 2)
            info(res)
            res should equal("2 itemsA from one item selected")
          }
        }
      }
      describe("when the overall number of items is 2") {
        describe("and no other parameter is set") {
          it("should return the correct text") {
            val formatable = resourcesWithoutFallback.forceFormatter(selectionOfManyKey, "en")
            val res = formatable.forceFormat("all_items_count" -> 2)
            info(res)
            res should equal("Nothing from 2 items selected")
          }
        }
        describe("and lower_index=0 and selection_size=0 ") {
          it("should return the correct text") {
            val formatable = resourcesWithoutFallback.forceFormatter(selectionOfManyKey, "en")
            val res = formatable.forceFormat("all_items_count" -> 2, "lower_index" -> 0, "selection_size" -> 0)
            info(res)
            res should equal("Nothing from 2 items selected")
          }
        }
        describe("and lower_index=1 and selection_size=0 ") {
          it("should return the correct text") {
            val formatable = resourcesWithoutFallback.forceFormatter(selectionOfManyKey, "en")
            val res = formatable.forceFormat("all_items_count" -> 2, "lower_index" -> 1, "selection_size" -> 0)
            info(res)
            res should equal("Nothing from 2 items selected")
          }
        }
        describe("and lower_index=1 and selection_size=1 ") {
          it("should return the correct text") {
            val formatable = resourcesWithoutFallback.forceFormatter(selectionOfManyKey, "en")
            val res = formatable.forceFormat("all_items_count" -> 2, "lower_index" -> 1, "selection_size" -> 1)
            info(res)
            res should equal("ItemR 1 from 2 items selected")
          }
        }
        describe("and lower_index=1") {
          it("should return the correct text") {
            val formatable = resourcesWithoutFallback.forceFormatter(selectionOfManyKey, "en")
            val res = formatable.forceFormat("all_items_count" -> 2, "lower_index" -> 1)
            info(res)
            res should equal("ItemR 1 from 2 items selected")
          }
        }
        describe("and lower_index=1 and selection_size=2 ") {
          it("should return the correct text") {
            val formatable = resourcesWithoutFallback.forceFormatter(selectionOfManyKey, "en")
            val res = formatable.forceFormat("all_items_count" -> 2, "lower_index" -> 1, "selection_size" -> 2)
            info(res)
            res should equal("ItemsR 1 to 2 from 2 items selected")
          }
        }

        describe("and selection_size=0 ") {
          it("should return the correct text") {
            val formatable = resourcesWithoutFallback.forceFormatter(selectionOfManyKey, "en")
            val res = formatable.forceFormat("all_items_count" -> 2, "selection_size" -> 0)
            info(res)
            res should equal("Nothing from 2 items selected")
          }
        }
        describe("and selection_size=1") {
          it("should return the correct text") {
            val formatable = resourcesWithoutFallback.forceFormatter(selectionOfManyKey, "en")
            val res = formatable.forceFormat("all_items_count" -> 2, "selection_size" -> 1)
            info(res)
            res should equal("One itemA from 2 items selected")
          }
        }
        describe("and selection_size=2") {
          it("should return the correct text") {
            val formatable = resourcesWithoutFallback.forceFormatter(selectionOfManyKey, "en")
            val res = formatable.forceFormat("all_items_count" -> 2, "selection_size" -> 2)
            info(res)
            res should equal("2 itemsA from 2 items selected")
          }
        }
        describe("and lower_index=1 and upper_index=1") {
          it("should return the correct text") {
            val formatable = resourcesWithoutFallback.forceFormatter(selectionOfManyKey, "en")
            val res = formatable.forceFormat("all_items_count" -> 2, "lower_index" -> 1, "upper_index" -> 1)
            info(res)
            res should equal("ItemR 1 from 2 items selected")
          }
        }
        describe("and lower_index=1 and upper_index=2") {
          it("should return the correct text") {
            val formatable = resourcesWithoutFallback.forceFormatter(selectionOfManyKey, "en")
            val res = formatable.forceFormat("all_items_count" -> 2, "lower_index" -> 1, "upper_index" -> 2)
            info(res)
            res should equal("ItemsR 1 to 2 from 2 items selected")
          }
        }
        describe("and lower_index=2 and upper_index=1 selection_size=1(uses lower_index and selection_size))") {
          it("should return the correct text") {
            val formatable = resourcesWithoutFallback.forceFormatter(selectionOfManyKey, "en")
            val res = formatable.forceFormat("all_items_count" -> 2, "lower_index" -> 2, "upper_index" -> 1, "selection_size" -> 1)
            info(res)
            res should equal("ItemR 2 from 2 items selected")
          }
        }
        describe("and lower_index=2 and upper_index=1 and ") {
          it("should return the correct text") {
            val formatable = resourcesWithoutFallback.forceFormatter(selectionOfManyKey, "en")
            val res = formatable.forceFormat("all_items_count" -> 2, "lower_index" -> 2, "upper_index" -> 1)
            info(res)
            res should equal("Nothing from 2 items selected")
          }
        }
        describe("and selection_size=1 and upper_index=2") {
          it("should return the correct text") {
            val formatable = resourcesWithoutFallback.forceFormatter(selectionOfManyKey, "en")
            val res = formatable.forceFormat("all_items_count" -> 2, "selection_size" -> 1, "upper_index" -> 2)
            info(res)
            res should equal("ItemR 2 from 2 items selected")
          }
        }
        describe("and selection_size=2 and upper_index=2") {
          it("should return the correct text") {
            val formatable = resourcesWithoutFallback.forceFormatter(selectionOfManyKey, "en")
            val res = formatable.forceFormat("all_items_count" -> 2, "selection_size" -> 2, "upper_index" -> 2)
            info(res)
            res should equal("ItemsR 1 to 2 from 2 items selected")
          }
        }
      }
    }
    describe("when only range-selection is present") {
      val selectionOfManyKey = ResourceKey("section_2", "group_1", "selection-of-many-range-only")
      describe("when the overall number of items is 2") {
        describe("and no other parameter is set") {
          it("should return the correct text") {
            val formatable = resourcesWithoutFallback.forceFormatter(selectionOfManyKey, "en")
            val res = formatable.forceFormat("all_items_count" -> 2)
            info(res)
            res should equal("Nothing from 2 items selected")
          }
        }
        describe("and lower_index=0 and selection_size=0 ") {
          it("should return the correct text") {
            val formatable = resourcesWithoutFallback.forceFormatter(selectionOfManyKey, "en")
            val res = formatable.forceFormat("all_items_count" -> 2, "lower_index" -> 0, "selection_size" -> 0)
            info(res)
            res should equal("Nothing from 2 items selected")
          }
        }
        describe("and lower_index=1 and selection_size=0 ") {
          it("should return the correct text") {
            val formatable = resourcesWithoutFallback.forceFormatter(selectionOfManyKey, "en")
            val res = formatable.forceFormat("all_items_count" -> 2, "lower_index" -> 1, "selection_size" -> 0)
            info(res)
            res should equal("Nothing from 2 items selected")
          }
        }
        describe("and lower_index=1 and selection_size=1 ") {
          it("should return the correct text") {
            val formatable = resourcesWithoutFallback.forceFormatter(selectionOfManyKey, "en")
            val res = formatable.forceFormat("all_items_count" -> 2, "lower_index" -> 1, "selection_size" -> 1)
            info(res)
            res should equal("ItemR 1 from 2 items selected")
          }
        }
        describe("and lower_index=1") {
          it("should return the correct text") {
            val formatable = resourcesWithoutFallback.forceFormatter(selectionOfManyKey, "en")
            val res = formatable.forceFormat("all_items_count" -> 2, "lower_index" -> 1)
            info(res)
            res should equal("ItemR 1 from 2 items selected")
          }
        }
        describe("and lower_index=1 and selection_size=2 ") {
          it("should return the correct text") {
            val formatable = resourcesWithoutFallback.forceFormatter(selectionOfManyKey, "en")
            val res = formatable.forceFormat("all_items_count" -> 2, "lower_index" -> 1, "selection_size" -> 2)
            info(res)
            res should equal("ItemsR 1 to 2 from 2 items selected")
          }
        }

        describe("and selection_size=0 ") {
          it("should return the correct text") {
            val formatable = resourcesWithoutFallback.forceFormatter(selectionOfManyKey, "en")
            val res = formatable.forceFormat("all_items_count" -> 2, "selection_size" -> 0)
            info(res)
            res should equal("Nothing from 2 items selected")
          }
        }
        describe("and selection_size=1") {
          it("should fail") {
            val formatable = resourcesWithoutFallback.forceFormatter(selectionOfManyKey, "en")
            val res = formatable.format("all_items_count" -> 2, "selection_size" -> 1)
            res.isFailure should equal(true)
          }
        }
        describe("and selection_size=2") {
          it("should fail") {
            val formatable = resourcesWithoutFallback.forceFormatter(selectionOfManyKey, "en")
            val res = formatable.format("all_items_count" -> 2, "selection_size" -> 2)
            res.isFailure should equal(true)
          }
        }
      }
    }
    describe("when only amount-selection is present") {
      val selectionOfManyKey = ResourceKey("section_2", "group_1", "selection-of-many-amount-only")
      describe("when the overall number of items is 2") {
        describe("and no other parameter is set") {
          it("should return the correct text") {
            val formatable = resourcesWithoutFallback.forceFormatter(selectionOfManyKey, "en")
            val res = formatable.forceFormat("all_items_count" -> 2)
            info(res)
            res should equal("Nothing from 2 items selected")
          }
        }
        describe("and lower_index=0 and selection_size=0 ") {
          it("should return the correct text") {
            val formatable = resourcesWithoutFallback.forceFormatter(selectionOfManyKey, "en")
            val res = formatable.forceFormat("all_items_count" -> 2, "lower_index" -> 0, "selection_size" -> 0)
            info(res)
            res should equal("Nothing from 2 items selected")
          }
        }
        describe("and lower_index=1 and selection_size=0 ") {
          it("should return the correct text") {
            val formatable = resourcesWithoutFallback.forceFormatter(selectionOfManyKey, "en")
            val res = formatable.forceFormat("all_items_count" -> 2, "lower_index" -> 1, "selection_size" -> 0)
            info(res)
            res should equal("Nothing from 2 items selected")
          }
        }
        describe("and lower_index=1 and selection_size=1 ") {
          it("should return the correct text") {
            val formatable = resourcesWithoutFallback.forceFormatter(selectionOfManyKey, "en")
            val res = formatable.forceFormat("all_items_count" -> 2, "lower_index" -> 1, "selection_size" -> 1)
            info(res)
            res should equal("One itemA from 2 items selected")
          }
        }
        describe("and lower_index=1") {
          it("should return the correct text") {
            val formatable = resourcesWithoutFallback.forceFormatter(selectionOfManyKey, "en")
            val res = formatable.forceFormat("all_items_count" -> 2, "lower_index" -> 1)
            info(res)
            res should equal("One itemA from 2 items selected")
          }
        }
        describe("and lower_index=1 and selection_size=2 ") {
          it("should return the correct text") {
            val formatable = resourcesWithoutFallback.forceFormatter(selectionOfManyKey, "en")
            val res = formatable.forceFormat("all_items_count" -> 2, "lower_index" -> 1, "selection_size" -> 2)
            info(res)
            res should equal("2 itemsA from 2 items selected")
          }
        }

        describe("and selection_size=0 ") {
          it("should return the correct text") {
            val formatable = resourcesWithoutFallback.forceFormatter(selectionOfManyKey, "en")
            val res = formatable.forceFormat("all_items_count" -> 2, "selection_size" -> 0)
            info(res)
            res should equal("Nothing from 2 items selected")
          }
        }
        describe("and selection_size=1") {
          it("should return the correct text") {
            val formatable = resourcesWithoutFallback.forceFormatter(selectionOfManyKey, "en")
            val res = formatable.forceFormat("all_items_count" -> 2, "selection_size" -> 1)
            info(res)
            res should equal("One itemA from 2 items selected")
          }
        }
        describe("and selection_size=2") {
          it("should return the correct text") {
            val formatable = resourcesWithoutFallback.forceFormatter(selectionOfManyKey, "en")
            val res = formatable.forceFormat("all_items_count" -> 2, "selection_size" -> 2)
            info(res)
            res should equal("2 itemsA from 2 items selected")
          }
        }
      }
    }
  }
}