package almhirt.riftwarp.worksheets

import almhirt.syntax.almvalidation._
import almhirt.riftwarp._
import almhirt.riftwarp.impl._

object Worksheet {
  val riftWarp = RiftWarp.unsafeWithDefaults      //> riftWarp  : almhirt.riftwarp.RiftWarp = almhirt.riftwarp.RiftWarp$$anon$1@1a
                                                  //| 84da23
  riftWarp.barracks.addDecomposer(new TestObjectADecomposer())
  riftWarp.barracks.addRecomposer(new TestObjectARecomposer())

  val testObject = new TestObjectA("Peter", Some("Paul"), 15)
                                                  //> testObject  : almhirt.riftwarp.TestObjectA = TestObjectA(Peter,Some(Paul),15
                                                  //| )
 
      
  val resV = riftWarp.prepareForWarp[Map[String, Any]](RiftMap)(testObject)
                                                  //> resV  : almhirt.common.package.AlmValidation[Map[String,Any]] = Success(Map(
                                                  //| typedescriptor -> almhirt.riftwarp.TestObjectA, name -> Peter, friend -> Pau
                                                  //| l, age -> 15))
  
  val warpStream = resV.forceResult               //> warpStream  : Map[String,Any] = Map(typedescriptor -> almhirt.riftwarp.TestO
                                                  //| bjectA, name -> Peter, friend -> Paul, age -> 15)


  val backFromWarpV = riftWarp.receiveFromWarp[Map[String, Any], TestObjectA](RiftMap)(warpStream)
                                                  //> backFromWarpV  : almhirt.common.package.AlmValidation[almhirt.riftwarp.TestO
                                                  //| bjectA] = Success(TestObjectA(Peter,Some(Paul),15))
  
  val backFromWarp = backFromWarpV.forceResult    //> backFromWarp  : almhirt.riftwarp.TestObjectA = TestObjectA(Peter,Some(Paul),
                                                  //| 15)
  
  testObject == backFromWarp                      //> res0: Boolean = true
  
  
  riftWarp.prepareForWarp[Map[String, Any]](RiftMap)(testObject).bind(warpStream =>
    riftWarp.receiveFromWarp[Map[String, Any], TestObjectA](RiftMap)(warpStream)).map(rearrived =>
      rearrived == testObject)                    //> res1: scalaz.Validation[almhirt.common.Problem,Boolean] = Success(true)
}