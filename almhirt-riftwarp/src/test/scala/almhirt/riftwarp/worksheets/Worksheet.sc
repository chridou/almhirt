package almhirt.riftwarp.worksheets

import almhirt.syntax.almvalidation._
import almhirt.riftwarp._
import almhirt.riftwarp.impl._

object Worksheet {
  val riftWarp = RiftWarp.unsafeWithDefaults      //> riftWarp  : almhirt.riftwarp.RiftWarp = almhirt.riftwarp.RiftWarp$$anon$1@76
                                                  //| 653b8e
  riftWarp.barracks.addDecomposer(new TestObjectADecomposer())
  riftWarp.barracks.addDecomposer(new TestAddressDecomposer())
  riftWarp.barracks.addRecomposer(new TestObjectARecomposer())
  riftWarp.barracks.addRecomposer(new TestAddressRecomposer())

  val testObject = TestObjectA.pete               //> testObject  : almhirt.riftwarp.TestObjectA = TestObjectA(Pete,Some(Jim),true
                                                  //| ,47,12737823792992474737892456985496456847789872389723984,99283823727372382.
                                                  //| 62253651576457646,12.5,[B@2fb02c81,[B@5739e19c,Some(TestAddress(Berlin,An de
                                                  //| r Mauer 89)))
         
  val resV = riftWarp.prepareForWarp[scalaz.Cord](RiftJson)(testObject)
                                                  //> resV  : almhirt.common.package.AlmValidation[scalaz.Cord] = Success({"riftwa
                                                  //| rptd":"almhirt.riftwarp.TestObjectA","name":"Pete","friend":"Jim","isMale":t
                                                  //| rue,"age":47,"atoms":"12737823792992474737892456985496456847789872389723984"
                                                  //| ,"balance":"99283823727372382.62253651576457646","size":12.5,"coins":[0,1,2,
                                                  //| 3,4,5,6,7,8,9,10,-1],"image":"FanpAAAAgIDq","address":{"riftwarptd":"almhirt
                                                  //| .riftwarp.TestAddress","city":"Berlin","street":"An der Mauer 89"}})
  
  val warpStream = resV.forceResult               //> warpStream  : scalaz.Cord = {"riftwarptd":"almhirt.riftwarp.TestObjectA","na
                                                  //| me":"Pete","friend":"Jim","isMale":true,"age":47,"atoms":"127378237929924747
                                                  //| 37892456985496456847789872389723984","balance":"99283823727372382.6225365157
                                                  //| 6457646","size":12.5,"coins":[0,1,2,3,4,5,6,7,8,9,10,-1],"image":"FanpAAAAgI
                                                  //| Dq","address":{"riftwarptd":"almhirt.riftwarp.TestAddress","city":"Berlin","
                                                  //| street":"An der Mauer 89"}}

  val backFromWarpV = riftWarp.receiveFromWarp[scalaz.Cord, TestObjectA](RiftJson)(warpStream)
                                                  //> backFromWarpV  : almhirt.common.package.AlmValidation[almhirt.riftwarp.TestO
                                                  //| bjectA] = Success(TestObjectA(Pete,Some(Jim),true,47,12737823792992474737892
                                                  //| 456985496456847789872389723984,99283823727372382.62253651576457646,12.5,[B@7
                                                  //| 0808f4e,[B@8408396,Some(TestAddress(Berlin,An der Mauer 89))))
  
  val backFromWarp = backFromWarpV.forceResult    //> backFromWarp  : almhirt.riftwarp.TestObjectA = TestObjectA(Pete,Some(Jim),tr
                                                  //| ue,47,12737823792992474737892456985496456847789872389723984,9928382372737238
                                                  //| 2.62253651576457646,12.5,[B@70808f4e,[B@8408396,Some(TestAddress(Berlin,An d
                                                  //| er Mauer 89)))
  
  testObject == backFromWarp                      //> res0: Boolean = false
  
  
  
  riftWarp.prepareForWarp[Map[String, Any]](RiftMap)(testObject).bind(warpStream =>
    riftWarp.receiveFromWarp[Map[String, Any], TestObjectA](RiftMap)(warpStream)).map(rearrived =>
      rearrived == testObject)                    //> res1: scalaz.Validation[almhirt.common.Problem,Boolean] = Success(true)


  riftWarp.prepareForWarp[scalaz.Cord](RiftJson)(testObject).bind(warpStream =>
    riftWarp.receiveFromWarp[scalaz.Cord, TestObjectA](RiftJson)(warpStream)).map(rearrived =>
      rearrived == testObject)                    //> res2: scalaz.Validation[almhirt.common.Problem,Boolean] = Success(false)
      
}