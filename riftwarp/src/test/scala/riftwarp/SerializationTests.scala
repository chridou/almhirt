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
    rw.barracks.addRecomposer(new TreesRecomposer())
    rw
  }

  val testObject = TestObjectA.pete

  test("RiftWarp must deserialize to Json and the reserialize without error") {
    val warpStream = riftWarp.prepareForWarp[DimensionString](RiftJson())(testObject).forceResult
    val backFromWarpV = riftWarp.receiveFromWarp[DimensionString, TestObjectA](RiftJson())(warpStream)

    backFromWarpV.isSuccess must be(true)
  }

  test("RiftWarp must deserialize to Xml and the reserialize without error") {
    val warpStream = riftWarp.prepareForWarp[DimensionString](RiftXml())(testObject).forceResult
    val backFromWarpV = riftWarp.receiveFromWarp[DimensionString, TestObjectA](RiftXml())(warpStream)

    backFromWarpV.isSuccess must be(true)
  }
  
  test("RiftWarp must deserialize the testObject's complex collections to Xml and the reserialize without error") {
    val warpStream = riftWarp.prepareForWarp[DimensionString](RiftXml())(testObject.complexMAs).forceResult
    val backFromWarpV = riftWarp.receiveFromWarp[DimensionString, ComplexMAs](RiftXml())(warpStream)

    println(backFromWarpV)
    
    backFromWarpV.isSuccess must be(true)
  }
  
//  test("It must deserialize the trees to Json and the reserialize without error") {
//    val warpStream = riftWarp.prepareForWarp[DimensionString](RiftJson())(Trees()).forceResult
//    val backFromWarp = riftWarp.receiveFromWarp[DimensionString, Trees](RiftJson())(warpStream).forceResult
////    import scalaz.std.AllInstances._
////    println(backFromWarpV.forceResult.intTree.drawTree)
//    
//    (backFromWarp.intTree == Trees().intTree) must be(true)
//  }
}