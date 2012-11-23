package almhirt.riftwarp.worksheets

import almhirt.syntax.almvalidation._
import almhirt.riftwarp._
import almhirt.riftwarp.impl._

object Worksheet {
  val riftWarp = RiftWarp.unsafeWithDefaults      //> riftWarp  : almhirt.riftwarp.RiftWarp = almhirt.riftwarp.RiftWarp$$anon$1@6d
                                                  //| e120da
  riftWarp.barracks.addDecomposer(new TestObjectADecomposer())
  riftWarp.barracks.addDecomposer(new TestAddressDecomposer())
  riftWarp.barracks.addRecomposer(new TestObjectARecomposer())
  riftWarp.barracks.addRecomposer(new TestAddressRecomposer())



  val testObject = TestObjectA.pete               //> testObject  : almhirt.riftwarp.TestObjectA = TestObjectA(Pete,Some(Jim),true
                                                  //| ,47,12737823792992474737892456985496456847789872389723984,99283823727372382.
                                                  //| 62253651576457646,12.5,[B@336d026a,[B@39b99786,List(4, 6, 8, 10, 12, 20),Lis
                                                  //| t(aaa, bbb, ccc),Some(TestAddress(Berlin,An der Mauer 89)))
         
  val resV = riftWarp.prepareForWarp[RiftJson, DimensionCord](testObject)
                                                  //> resV  : almhirt.common.package.AlmValidation[almhirt.riftwarp.DimensionCord]
                                                  //|  = Failure(almhirt.common.UnspecifiedProblem
                                                  //| No primitive dematerializer found for M[A](scala.collection.immutable.List, 
                                                  //| java.lang.String) found for ident 'words'
                                                  //| Category: SystemProblem
                                                  //| Severity: Major
                                                  //| Arguments: Map()
                                                  //| )
  
  val warpStream = resV.forceResult               //> almhirt.common.ResultForcedFromValidationException: A value has been forced 
                                                  //| from a failure: No primitive dematerializer found for M[A](scala.collection.
                                                  //| immutable.List, java.lang.String) found for ident 'words'
                                                  //| 	at almhirt.almvalidation.AlmValidationOps5$$anonfun$forceResult$1.apply(
                                                  //| AlmValidationOps.scala:121)
                                                  //| 	at almhirt.almvalidation.AlmValidationOps5$$anonfun$forceResult$1.apply(
                                                  //| AlmValidationOps.scala:121)
                                                  //| 	at scalaz.Validation$class.fold(Validation.scala:64)
                                                  //| 	at scalaz.Failure.fold(Validation.scala:305)
                                                  //| 	at almhirt.almvalidation.AlmValidationOps5$class.forceResult(AlmValidati
                                                  //| onOps.scala:121)
                                                  //| 	at almhirt.almvalidation.ToAlmValidationOps$$anon$6.forceResult(AlmValid
                                                  //| ationOps.scala:195)
                                                  //| 	at almhirt.riftwarp.worksheets.Worksheet$$anonfun$main$1.apply$mcV$sp(al
                                                  //| mhirt.riftwarp.worksheets.Worksheet.scala:20)
                                                  //| 	at org.scalaide.worksheet.runtime.library.WorksheetSupport$$anonfun$$exe
                                                  //| cute$1.apply$mcV$sp(Worksheet
                                                  //| Output exceeds cutoff limit.

  val backFromWarpV = riftWarp.receiveFromWarp[DimensionCord, TestObjectA](RiftJson())(warpStream)
  
  val backFromWarp = backFromWarpV.forceResult
  
  testObject == backFromWarp
  
  
  
  riftWarp.prepareForWarp[RiftMap, DimensionRawMap](testObject).bind(warpStream =>
    riftWarp.receiveFromWarp[DimensionRawMap, TestObjectA](RiftMap())(warpStream)).map(rearrived =>
      rearrived == testObject)


  riftWarp.prepareForWarp[RiftJson, DimensionCord](testObject).bind(warpStream =>
    riftWarp.receiveFromWarp[DimensionCord, TestObjectA](RiftJson())(warpStream)).map(rearrived =>
      rearrived == testObject)
      
}