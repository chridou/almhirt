package almhirt.riftwarp.worksheets

import almhirt.syntax.almvalidation._
import almhirt.riftwarp._
import almhirt.riftwarp.impl._


object Worksheet {
  val riftWarp = RiftWarp.unsafeWithDefaults      //> riftWarp  : almhirt.riftwarp.RiftWarp = almhirt.riftwarp.RiftWarp$$anon$1@16
                                                  //| f8f7db
  riftWarp.barracks.addDecomposer(new TestObjectADecomposer())
  riftWarp.barracks.addDecomposer(new TestAddressDecomposer())
  riftWarp.barracks.addRecomposer(new TestObjectARecomposer())
  riftWarp.barracks.addRecomposer(new TestAddressRecomposer())


  val testObject = new TestObjectA("Peter", Some("Paul"), 15, Some(TestAddress("Ldenscheid", """Gustav-"Adolf"-Strae""")))
                                                  //> testObject  : almhirt.riftwarp.TestObjectA = TestObjectA(Peter,Some(Paul),15
                                                  //| ,Some(TestAddress(Ldenscheid,Gustav-"Adolf"-Strae)))
   
  
  
      
  val resV = riftWarp.prepareForWarp[scalaz.Cord](RiftJson)(testObject)
                                                  //> resV  : almhirt.common.package.AlmValidation[scalaz.Cord] = Success({"riftwa
                                                  //| rptd":"almhirt.riftwarp.TestObjectA","name":"Peter","friend":"Paul","age":15
                                                  //| ,"address":{"riftwarptd":"almhirt.riftwarp.TestAddress","city":"Ldenscheid",
                                                  //| "street":"Gustav-"Adolf"-Strae"}})
  
  val warpStream = resV.forceResult               //> warpStream  : scalaz.Cord = {"riftwarptd":"almhirt.riftwarp.TestObjectA","na
                                                  //| me":"Peter","friend":"Paul","age":15,"address":{"riftwarptd":"almhirt.riftwa
                                                  //| rp.TestAddress","city":"Ldenscheid","street":"Gustav-"Adolf"-Strae"}}


  val backFromWarpV = riftWarp.receiveFromWarp[scalaz.Cord, TestObjectA](RiftJson)(warpStream)
                                                  //> backFromWarpV  : almhirt.common.package.AlmValidation[almhirt.riftwarp.TestO
                                                  //| bjectA] = Failure(almhirt.common.ParsingProblem
                                                  //| Could not parse JSON
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| Input:
                                                  //| {"riftwarptd":"almhirt.riftwarp.TestObjectA","name":"Peter","friend":"Paul",
                                                  //| "age":15,"address":{"riftwarptd":"almhirt.riftwarp.TestAddress","city":"Lden
                                                  //| scheid","street":"Gustav-"Adolf"-Strae"}}
                                                  //| )
  
  val backFromWarp = backFromWarpV.forceResult    //> almhirt.common.ResultForcedFromValidationException: A value has been forced 
                                                  //| from a failure: Could not parse JSON
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
                                                  //| mhirt.riftwarp.worksheets.Worksheet.scala:28)
                                                  //| 	at org.scalaide.worksheet.runtime.library.WorksheetSupport$$anonfun$$exe
                                                  //| cute$1.apply$mcV$sp(WorksheetSupport.scala:76)
                                                  //| 	at org.scalaide.worksheet.runtime.library.WorksheetSupport$.redirected(W
                                                  //| orksheetSu
                                                  //| Output exceeds cutoff limit.
  
  testObject == backFromWarp
  
 
  
  riftWarp.prepareForWarp[Map[String, Any]](RiftMap)(testObject).bind(warpStream =>
    riftWarp.receiveFromWarp[Map[String, Any], TestObjectA](RiftMap)(warpStream)).map(rearrived =>
      rearrived == testObject)


  riftWarp.prepareForWarp[Map[String, Any]](RiftJson)(testObject).bind(warpStream =>
    riftWarp.receiveFromWarp[Map[String, Any], TestObjectA](RiftJson)(warpStream)).map(rearrived =>
      rearrived == testObject)
      
}