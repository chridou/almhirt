package riftwarp

import org.scalatest._
import org.scalatest.matchers.ShouldMatchers
import almhirt.syntax.almvalidation._
import riftwarp.inst._
import riftwarp.impl.rematerializers.FromStdLibJsonStringExtractor

class FromStdLibJsonStringExtractorSpecs extends WordSpec with ShouldMatchers {
  val riftWarp = RiftWarp.concurrentWithDefaults()
  implicit val hasDecomposers = riftWarp.barracks
  implicit val toolShed = riftWarp.toolShed

  val list0 = List.empty[Int]
  val list0Json = """{"list": []}"""
  val list1 = List(1, 2, 3, 4)
  val list1Json = """{"list": [1,2,3,4]}"""

  "FromStdLibJsonStringExtractor" when {
    "dematerializing an empty List of Integers" should {
      "extract the property" in {
        val list = FromStdLibJsonStringExtractor(list0Json).flatMap(extractor =>
          extractor.getManyPrimitives[List, Int]("list"))
        list.isSuccess should be(true)
      }
      "dematerialize the property correctly" in {
        val list = FromStdLibJsonStringExtractor(list0Json).flatMap(extractor =>
          extractor.getManyPrimitives[List, Int]("list")).forceResult
        list should equal(list0)
      }
    }
    "dematerializing a List of 4 Integers" should {
      "extract the property" in {
        val list = FromStdLibJsonStringExtractor(list1Json).flatMap(extractor =>
          extractor.getManyPrimitives[List, Int]("list"))
        list.isSuccess should be(true)
      }
      "dematerialize the property correctly" in {
        val list = FromStdLibJsonStringExtractor(list1Json).flatMap(extractor =>
          extractor.getManyPrimitives[List, Int]("list")).forceResult
        list should equal(list1)
      }
    }
  }
}