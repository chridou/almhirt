package riftwarp

import org.scalatest._
import org.scalatest.matchers.MustMatchers
import almhirt.almvalidation.kit._

class SerializationTests extends FunSuite with MustMatchers {
  val riftWarp = {
    val rw = RiftWarp.concurrentWithDefaults()
    rw.barracks.addDecomposer(new TestObjectADecomposer())
    rw.barracks.addRecomposer(new TestObjectARecomposer())
    rw.barracks.addDecomposer(new TestAddressDecomposer())
    rw.barracks.addRecomposer(new TestAddressRecomposer())

    rw.barracks.addDecomposer(new PrimitiveTypesDecomposer())
    rw.barracks.addRecomposer(new PrimitiveTypesRecomposer())
    rw.barracks.addDecomposer(new PrimitiveListMAsDecomposer())
    rw.barracks.addRecomposer(new PrimitiveListMAsRecomposer())
    rw.barracks.addDecomposer(new PrimitiveVectorMAsDecomposer())
    rw.barracks.addRecomposer(new PrimitiveVectorMAsRecomposer())
    rw.barracks.addDecomposer(new PrimitiveSetMAsDecomposer())
    rw.barracks.addRecomposer(new PrimitiveSetMAsRecomposer())
    rw.barracks.addDecomposer(new PrimitiveIterableMAsDecomposer())
    rw.barracks.addRecomposer(new PrimitiveIterableMAsRecomposer())
    rw.barracks.addDecomposer(new ComplexMAsDecomposer())
    rw.barracks.addRecomposer(new ComplexMAsRecomposer())
    rw.barracks.addDecomposer(new PrimitiveMapsDecomposer())
    rw.barracks.addRecomposer(new PrimitiveMapsRecomposer())
    rw.barracks.addDecomposer(new ComplexMapsDecomposer())
    rw.barracks.addRecomposer(new ComplexMapsRecomposer())
    rw.barracks.addDecomposer(new TreesDecomposer())
    rw
  }

  val testObject = TestObjectA.pete
//  test("Serialize the Testobject to JSON 1000 times") {
//    val res =
//      for (i <- 1 to 1000) yield riftWarp.prepareForWarp[DimensionCord](RiftJson())(testObject).isSuccess
//    assert(res.forall(_ == true))
//  }

  test("It must deserialize to Json and the reserialize without error") {
    val warpStream = riftWarp.prepareForWarp[DimensionString](RiftJson())(testObject).forceResult
    val backFromWarpV = riftWarp.receiveFromWarp[DimensionString, TestObjectA](RiftJson())(warpStream)

    backFromWarpV.isSuccess must be(true)
  }
}