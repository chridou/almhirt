package almhirt.riftwarp.worksheets

import almhirt.syntax.almvalidation._
import almhirt.riftwarp._
import almhirt.riftwarp.impl._


object Worksheet {
  val riftWarp = RiftWarp.unsafeWithDefaults      //> riftWarp  : almhirt.riftwarp.RiftWarp = almhirt.riftwarp.RiftWarp$$anon$1@2f
                                                  //| 92d8d4
  riftWarp.barracks.addDecomposer(new TestObjectADecomposer())
  riftWarp.barracks.addDecomposer(new TestAddressDecomposer())
  riftWarp.barracks.addRecomposer(new TestObjectARecomposer())
  riftWarp.barracks.addRecomposer(new TestAddressRecomposer())


  val testObject = new TestObjectA("Peter", Some("Paul"), 15, Some(TestAddress("Ldenscheid", "Gustav-Adolf-Strae")))
                                                  //> testObject  : almhirt.riftwarp.TestObjectA = TestObjectA(Peter,Some(Paul),15
                                                  //| ,Some(TestAddress(Ldenscheid,Gustav-Adolf-Strae)))
       
  val resV = riftWarp.prepareForWarp[Map[String, Any]](RiftMap)(testObject)
                                                  //> resV  : almhirt.common.package.AlmValidation[Map[String,Any]] = Success(Map(
                                                  //| name -> Peter, friend -> Paul, age -> 15, address -> Map(typedescriptor -> a
                                                  //| lmhirt.riftwarp.TestAddress, city -> Ldenscheid, street -> Gustav-Adolf-Stra
                                                  //| e), typedescriptor -> almhirt.riftwarp.TestObjectA))
  
  val warpStream = resV.forceResult               //> warpStream  : Map[String,Any] = Map(name -> Peter, friend -> Paul, age -> 15
                                                  //| , address -> Map(typedescriptor -> almhirt.riftwarp.TestAddress, city -> Lde
                                                  //| nscheid, street -> Gustav-Adolf-Strae), typedescriptor -> almhirt.riftwarp.T
                                                  //| estObjectA)



  val backFromWarpV = riftWarp.receiveFromWarp[Map[String, Any], TestObjectA](RiftMap)(warpStream)
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