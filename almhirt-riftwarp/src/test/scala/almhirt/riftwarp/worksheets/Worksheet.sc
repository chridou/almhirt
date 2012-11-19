package almhirt.riftwarp.worksheets

import almhirt.syntax.almvalidation._
import almhirt.riftwarp._
import almhirt.riftwarp.impl._

object Worksheet {
  val riftWarp = RiftWarp.unsafeWithDefaults
  riftWarp.barracks.addDecomposer(new TestObjectADecomposer())
  riftWarp.barracks.addDecomposer(new TestAddressDecomposer())
  riftWarp.barracks.addRecomposer(new TestObjectARecomposer())
  riftWarp.barracks.addRecomposer(new TestAddressRecomposer())

  val testObject = TestObjectA.pete
         
  val resV = riftWarp.prepareForWarp[scalaz.Cord](RiftJson)(testObject)
  
  val warpStream = resV.forceResult

  val backFromWarpV = riftWarp.receiveFromWarp[scalaz.Cord, TestObjectA](RiftJson)(warpStream)
  
  val backFromWarp = backFromWarpV.forceResult
  
  testObject == backFromWarp
  
  
  riftWarp.prepareForWarp[Map[String, Any]](RiftMap)(testObject).bind(warpStream =>
    riftWarp.receiveFromWarp[Map[String, Any], TestObjectA](RiftMap)(warpStream)).map(rearrived =>
      rearrived == testObject)


  riftWarp.prepareForWarp[scalaz.Cord](RiftJson)(testObject).bind(warpStream =>
    riftWarp.receiveFromWarp[scalaz.Cord, TestObjectA](RiftJson)(warpStream)).map(rearrived =>
      rearrived == testObject)
      
}