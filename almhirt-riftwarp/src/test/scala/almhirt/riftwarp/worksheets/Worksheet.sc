package almhirt.riftwarp.worksheets

import almhirt.syntax.almvalidation._
import almhirt.riftwarp._
import almhirt.riftwarp.impl._


object Worksheet {
  val riftWarp = RiftWarp.unsafeWithDefaults      //> riftWarp  : almhirt.riftwarp.RiftWarp = almhirt.riftwarp.RiftWarp$$anon$1@4a
                                                  //| d9cb27
  riftWarp.barracks.addDecomposer(new TestObjectADecomposer())
  riftWarp.barracks.addDecomposer(new TestAddressDecomposer())
  riftWarp.barracks.addRecomposer(new TestObjectARecomposer())
  riftWarp.barracks.addRecomposer(new TestAddressRecomposer())


  val testObject = new TestObjectA("Peter", Some("Paul"), 15, Some(TestAddress("Ldenscheid", "Gustav-Adolf-Strae")))
                                                  //> testObject  : almhirt.riftwarp.TestObjectA = TestObjectA(Peter,Some(Paul),15
                                                  //| ,Some(TestAddress(Ldenscheid,Gustav-Adolf-Strae)))
   
  
 
       
  val resV = riftWarp.prepareForWarp[scalaz.Cord](RiftJson)(testObject)
                                                  //> resV  : almhirt.common.package.AlmValidation[scalaz.Cord] = Success({"riftwa
                                                  //| rptd":"almhirt.riftwarp.TestObjectA","name":"Peter","friend":"Paul","age":15
                                                  //| ,"address":{"riftwarptd":"almhirt.riftwarp.TestAddress","city":"Ldenscheid",
                                                  //| "street":"Gustav-Adolf-Strae"}})
  
  val warpStream = resV.forceResult               //> warpStream  : scalaz.Cord = {"riftwarptd":"almhirt.riftwarp.TestObjectA","na
                                                  //| me":"Peter","friend":"Paul","age":15,"address":{"riftwarptd":"almhirt.riftwa
                                                  //| rp.TestAddress","city":"Ldenscheid","street":"Gustav-Adolf-Strae"}}


  val backFromWarpV = riftWarp.receiveFromWarp[scalaz.Cord, TestObjectA](RiftJson)(warpStream)
                                                  //> backFromWarpV  : almhirt.common.package.AlmValidation[almhirt.riftwarp.TestO
                                                  //| bjectA] = Success(TestObjectA(Peter,Some(Paul),15,Some(TestAddress(Ldenschei
                                                  //| d,Gustav-Adolf-Strae))))
  
  val backFromWarp = backFromWarpV.forceResult    //> backFromWarp  : almhirt.riftwarp.TestObjectA = TestObjectA(Peter,Some(Paul),
                                                  //| 15,Some(TestAddress(Ldenscheid,Gustav-Adolf-Strae)))
  
  testObject == backFromWarp                      //> res0: Boolean = true
  
 
  
  riftWarp.prepareForWarp[Map[String, Any]](RiftMap)(testObject).bind(warpStream =>
    riftWarp.receiveFromWarp[Map[String, Any], TestObjectA](RiftMap)(warpStream)).map(rearrived =>
      rearrived == testObject)                    //> res1: scalaz.Validation[almhirt.common.Problem,Boolean] = Success(true)
      
}