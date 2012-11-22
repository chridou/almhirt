package almhirt.riftwarp.worksheets

import almhirt.syntax.almvalidation._
import almhirt.riftwarp._
import almhirt.riftwarp.impl._

object Worksheet {
  val riftWarp = RiftWarp.unsafeWithDefaults      //> riftWarp  : almhirt.riftwarp.RiftWarp = almhirt.riftwarp.RiftWarp$$anon$1@17
                                                  //| fe1feb
  riftWarp.barracks.addDecomposer(new TestObjectADecomposer())
  riftWarp.barracks.addDecomposer(new TestAddressDecomposer())
  riftWarp.barracks.addRecomposer(new TestObjectARecomposer())
  riftWarp.barracks.addRecomposer(new TestAddressRecomposer())



  val testObject = TestObjectA.pete               //> testObject  : almhirt.riftwarp.TestObjectA = TestObjectA(Pete,Some(Jim),true
                                                  //| ,47,12737823792992474737892456985496456847789872389723984,99283823727372382.
                                                  //| 62253651576457646,12.5,[B@23309e87,[B@4cf7c31d,Some(TestAddress(Berlin,An de
                                                  //| r Mauer 89)))
         
  val resV = riftWarp.prepareForWarp[DimensionCord,RiftJson](testObject)
                                                  //> resV  : almhirt.common.package.AlmValidation[almhirt.riftwarp.DimensionCord]
                                                  //|  = Success(DimensionCord({"riftwarptd":"almhirt.riftwarp.TestObjectA","name"
                                                  //| :"Pete","friend":"Jim","isMale":true,"age":47,"atoms":"127378237929924747378
                                                  //| 92456985496456847789872389723984","balance":"99283823727372382.6225365157645
                                                  //| 7646","size":12.5,"coins":[0,1,2,3,4,5,6,7,8,9,10,-1],"image":"FanpAAAAgIDq"
                                                  //| ,"address":{"riftwarptd":"almhirt.riftwarp.TestAddress","city":"Berlin","str
                                                  //| eet":"An der Mauer 89"}}))
  
  val warpStream = resV.forceResult               //> warpStream  : almhirt.riftwarp.DimensionCord = DimensionCord({"riftwarptd":"
                                                  //| almhirt.riftwarp.TestObjectA","name":"Pete","friend":"Jim","isMale":true,"ag
                                                  //| e":47,"atoms":"12737823792992474737892456985496456847789872389723984","balan
                                                  //| ce":"99283823727372382.62253651576457646","size":12.5,"coins":[0,1,2,3,4,5,6
                                                  //| ,7,8,9,10,-1],"image":"FanpAAAAgIDq","address":{"riftwarptd":"almhirt.riftwa
                                                  //| rp.TestAddress","city":"Berlin","street":"An der Mauer 89"}})

  val backFromWarpV = riftWarp.receiveFromWarp[DimensionCord, TestObjectA](RiftJson())(warpStream)
                                                  //> backFromWarpV  : almhirt.common.package.AlmValidation[almhirt.riftwarp.TestO
                                                  //| bjectA] = Success(TestObjectA(Pete,Some(Jim),true,47,12737823792992474737892
                                                  //| 456985496456847789872389723984,99283823727372382.62253651576457646,12.5,[B@3
                                                  //| 465b738,[B@1dfd868,Some(TestAddress(Berlin,An der Mauer 89))))
  
  val backFromWarp = backFromWarpV.forceResult    //> backFromWarp  : almhirt.riftwarp.TestObjectA = TestObjectA(Pete,Some(Jim),tr
                                                  //| ue,47,12737823792992474737892456985496456847789872389723984,9928382372737238
                                                  //| 2.62253651576457646,12.5,[B@3465b738,[B@1dfd868,Some(TestAddress(Berlin,An d
                                                  //| er Mauer 89)))
  
  testObject == backFromWarp                      //> res0: Boolean = false
  
  
  
  riftWarp.prepareForWarp[DimensionRawMap, RiftMap](testObject).bind(warpStream =>
    riftWarp.receiveFromWarp[DimensionRawMap, TestObjectA](RiftMap())(warpStream)).map(rearrived =>
      rearrived == testObject)                    //> res1: scalaz.Validation[almhirt.common.Problem,Boolean] = Success(true)


  riftWarp.prepareForWarp[DimensionCord,RiftJson](testObject).bind(warpStream =>
    riftWarp.receiveFromWarp[DimensionCord, TestObjectA](RiftJson())(warpStream)).map(rearrived =>
      rearrived == testObject)                    //> res2: scalaz.Validation[almhirt.common.Problem,Boolean] = Success(false)
      
}