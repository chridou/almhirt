package riftwarp.worksheets

import scalaz.syntax.validation._
import almhirt.syntax.almvalidation._
import riftwarp._
import riftwarp.impl._
import riftwarp.impl.rematerializers.simplema._

object Worksheet {
  val riftWarp = RiftWarp.unsafeWithDefaults      //> riftWarp  : riftwarp.RiftWarp = riftwarp.RiftWarp$$anon$97@2e3f8a3e
  riftWarp.barracks.addDecomposer(new TestObjectADecomposer())
  riftWarp.barracks.addRecomposer(new TestObjectARecomposer())
  riftWarp.barracks.addDecomposer(new TestAddressDecomposer())
  riftWarp.barracks.addRecomposer(new TestAddressRecomposer())

  riftWarp.barracks.addDecomposer(new PrimitiveTypesDecomposer())
  riftWarp.barracks.addRecomposer(new PrimitiveTypesRecomposer())
  riftWarp.barracks.addDecomposer(new PrimitiveListMAsDecomposer())
  riftWarp.barracks.addRecomposer(new PrimitiveListMAsRecomposer())
  riftWarp.barracks.addDecomposer(new PrimitiveVectorMAsDecomposer())
  riftWarp.barracks.addRecomposer(new PrimitiveVectorMAsRecomposer())
  riftWarp.barracks.addDecomposer(new PrimitiveSetMAsDecomposer())
  riftWarp.barracks.addRecomposer(new PrimitiveSetMAsRecomposer())
  riftWarp.barracks.addDecomposer(new PrimitiveIterableMAsDecomposer())
  riftWarp.barracks.addRecomposer(new PrimitiveIterableMAsRecomposer())

  val testObject = TestObjectA.pete               //> testObject  : riftwarp.TestObjectA = TestObjectA([B@773b0c5b,[B@45a86784,Pr
                                                  //| imitiveTypes(I am Pete,true,127,-237823,-278234263,265876257682376587365863
                                                  //| 876528756875682765252520577305007209857025728132213242,1.3672322,1.36723223
                                                  //| 50005,23761247614876823746.23846749182408,2012-11-28T07:26:41.625+01:00,0b2
                                                  //| 62a5c-ccc6-49e1-8a29-ad18cd31df0f),PrimitiveListMAs(List(alpha, beta, gamma
                                                  //| , delta),List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),List(1.0, 0.5, 0.2, 0.125),Lis
                                                  //| t(1.333333, 1.33333335, 1.6666666, 1.6666667),List(2012-11-28T08:26:41.703+
                                                  //| 01:00, 2012-11-28T09:26:41.703+01:00, 2012-11-28T10:26:41.703+01:00, 2012-1
                                                  //| 1-28T11:26:41.703+01:00)),PrimitiveVectorMAs(Vector(alpha, beta, gamma, del
                                                  //| ta),Vector(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),Vector(1.0, 0.5, 0.2, 0.125),Vect
                                                  //| or(1.333333, 1.33333335, 1.6666666, 1.6666667),Vector(2012-11-28T08:26:41.7
                                                  //| 03+01:00, 2012-11-28T09:26:41.703+01:00, 2012-11-28T10:26:41.703+01:00, 201
                                                  //| 2-11-28T11:26:41.703+01:00)),PrimitiveSetMAs(Set(alpha, beta, gamma, delta)
                                                  //| ,Set(5, 10, 1, 6, 9, 2, 7, 3, 8, 4),Set(1.0, 0.5, 0.2, 0.125),Set(1.333333,
                                                  //|  1.33333335, 1.6666666, 1.6666667),Set(2012-11-28T08:26:41.718+01:00, 2012-
                                                  //| 11-28T09:26:41.718+01:00, 2012-11-28T10:26:41.718+01:00, 2012-11-28T11:26:4
                                                  //| 1.718+01:00)),PrimitiveIterableMAs(List(alpha, beta, gamma, delta),List(1, 
                                                  //| 2, 3, 4, 5, 6, 7, 8, 9, 10),List(1.0, 0.5, 0.2, 0.125),List(1.333333, 1.333
                                                  //| 33335, 1.6666666, 1.6666667),List(2012-11-28T08:26:41.718+01:00, 2012-11-28
                                                  //| T09:26:41.718+01:00, 2012-11-28T10:26:41.718+01:00, 2012-11-28T11:26:41.718
                                                  //| +01:00)),Some(TestAddress(Berlin,At the wall 89)))
  val warpStream = riftWarp.prepareForWarp[DimensionCord](RiftJson())(testObject).forceResult
                                                  //> warpStream  : riftwarp.DimensionCord = DimensionCord({"riftwarptd":"riftwar
                                                  //| p.TestObjectA","arrayByte":[126,-123,12,-45,-128],"blob":"AAAAAAAGhQzTgHAAA
                                                  //| AA=","primitiveTypes":{"riftwarptd":"riftwarp.PrimitiveTypes","str":"I am P
                                                  //| ete","bool":true,"byte":127,"int":-237823,"long":-278234263,"bigInt":""2658
                                                  //| 76257682376587365863876528756875682765252520577305007209857025728132213242"
                                                  //| ","float":1.3672322034835815,"double":1.3672322350005,"bigDec":""2376124761
                                                  //| 4876823746.23846749182408"","dateTime":"2012-11-28T07:26:41.625+01:00","uui
                                                  //| d":""0b262a5c-ccc6-49e1-8a29-ad18cd31df0f""},"primitiveListMAs":{"riftwarpt
                                                  //| d":"riftwarp.PrimitiveListMAs",alpha,beta,gamma,delta,1,2,3,4,5,6,7,8,9,10,
                                                  //| 1.0,0.5,0.2,0.125,"1.333333","1.33333335","1.6666666","1.6666667","2012-11-
                                                  //| 28T08:26:41.703+01:00","2012-11-28T09:26:41.703+01:00","2012-11-28T10:26:41
                                                  //| .703+01:00","2012-11-28T11:26:41.703+01:00"},"primitiveVectorMAs":{"riftwar
                                                  //| ptd":"riftwarp.PrimitiveVectorMAs",alpha,beta,gamma,delta,1,2,3,4,5,6,7,8,9
                                                  //| ,10,1.0,0.5,0.2,0.125,"1.333333","1.33333335","1.6666666","1.6666667","2012
                                                  //| -11-28T08:26:41.703+01:00","2012-11-28T09:26:41.703+01:00","2012-11-28T10:2
                                                  //| 6:41.703+01:00","2012-11-28T11:26:41.703+01:00"},"primitiveSetMAs":{"riftwa
                                                  //| rptd":"riftwarp.PrimitiveSetMAs",alpha,beta,gamma,delta,7,3,10,6,2,5,8,9,1,
                                                  //| 4,1.0,0.5,0.2,0.125,"1.333333","1.33333335","1.6666666","1.6666667","2012-1
                                                  //| 1-28T08:26:41.718+01:00","2012-11-28T09:26:41.718+01:00","2012-11-28T10:26:
                                                  //| 41.718+01:00","2012-11-28T11:26:41.718+01:00"},"primitiveIterableMAs":{"rif
                                                  //| twarptd":"riftwarp.PrimitiveIterableMAs",alpha,beta,gamma,delta,1,2,3,4,5,6
                                                  //| ,7,8,9,10,1.0,0.5,0.2,0.125,"1.333333","1.33333335","1.6666666","1.6666667"
                                                  //| ,"2012-11-28T08:26:41.718+01:00","2012-11-28T09:26:41.718+01:00","2012-11-2
                                                  //| 8T10:26:41.718+01:00","2012-11-28T11:26:41.718+01:00"},"addressOpt":{"riftw
                                                  //| arptd":"riftwarp.TestAddress","city":"Berlin","street":"At the wall 89"}})
                                                  //| 

  val backFromWarpV = riftWarp.receiveFromWarp[DimensionCord, TestObjectA](RiftJson())(warpStream)
                                                  //> backFromWarpV  : almhirt.common.package.AlmValidation[riftwarp.TestObjectA]
                                                  //|  = Failure(almhirt.common.ParsingProblem
                                                  //| Could not parse JSON
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| Input:
                                                  //| {"riftwarptd":"riftwarp.TestObjectA","arrayByte":[126,-123,12,-45,-128],"bl
                                                  //| ob":"AAAAAAAGhQzTgHAAAAA=","primitiveTypes":{"riftwarptd":"riftwarp.Primiti
                                                  //| veTypes","str":"I am Pete","bool":true,"byte":127,"int":-237823,"long":-278
                                                  //| 234263,"bigInt":""265876257682376587365863876528756875682765252520577305007
                                                  //| 209857025728132213242"","float":1.3672322034835815,"double":1.3672322350005
                                                  //| ,"bigDec":""23761247614876823746.23846749182408"","dateTime":"2012-11-28T07
                                                  //| :26:41.625+01:00","uuid":""0b262a5c-ccc6-49e1-8a29-ad18cd31df0f""},"primiti
                                                  //| veListMAs":{"riftwarptd":"riftwarp.PrimitiveListMAs",alpha,beta,gamma,delta
                                                  //| ,1,2,3,4,5,6,7,8,9,10,1.0,0.5,0.2,0.125,"1.333333","1.33333335","1.6666666"
                                                  //| ,"1.6666667","2012-11-28T08:26:41.703+01:00","2012-11-28T09:26:41.703+01:00
                                                  //| ","2012-11-28T10:26:41.703+01:00","2012-11-28T11:26:41.703+01:00"},"primiti
                                                  //| veVectorMAs":{"riftwarptd":"riftwarp.PrimitiveVectorMAs",alpha,beta,gamma,d
                                                  //| elta,1,2,3,4,5,6,7,8,9,10,1.0,0.5,0.2,0.125,"1.333333","1.33333335","1.6666
                                                  //| 666","1.6666667","2012-11-28T08:26:41.703+01:00","2012-11-28T09:26:41.703+0
                                                  //| 1:00","2012-11-28T10:26:41.703+01:00","2012-11-28T11:26:41.703+01:00"},"pri
                                                  //| mitiveSetMAs":{"riftwarptd":"riftwarp.PrimitiveSetMAs",alpha,beta,gamma,del
                                                  //| ta,7,3,10,6,2,5,8,9,1,4,1.0,0.5,0.2,0.125,"1.333333","1.33333335","1.666666
                                                  //| 6","1.6666667","2012-11-28T08:26:41.718+01:00","2012-11-28T09:26:41.718+01:
                                                  //| 00","2012-11-28T10:26:41.718+01:00","2012-11-28T11:26:41.718+01:00"},"primi
                                                  //| tiveIterableMAs":{"riftwarptd":"riftwarp.PrimitiveIterableMAs",alpha,beta,g
                                                  //| amma,delta,1,2,3,4,5,6,7,8,9,10,1.0,0.5,0.2,0.125,"1.333333","1.33333335","
                                                  //| 1.6666666","1.6666667","2012-11-28T08:26:41.718+01:00","2012-11-28T09:26:41
                                                  //| .718+01:00","2012-11-28T10:26:41.718+01:00","2012-11-28T11:26:41.718+01:00"
                                                  //| },"addressOpt":{"riftwarptd":"riftwarp.TestAddress","city":"Berlin","street
                                                  //| ":"At the wall 89"}}
                                                  //| )

  val backFromWarp = backFromWarpV.forceResult    //> almhirt.common.ResultForcedFromValidationException: A value has been forced
                                                  //|  from a failure: Could not parse JSON
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
                                                  //| 	at riftwarp.worksheets.Worksheet$$anonfun$main$1.apply$mcV$sp(riftwarp.w
                                                  //| orksheets.Worksheet.scala:32)
                                                  //| 	at org.scalaide.worksheet.runtime.library.WorksheetSupport$$anonfun$$exe
                                                  //| cute$1.apply$mcV$sp(WorksheetSupport.scala:76)
                                                  //| 	at org.scalaide.worksheet.runtime.library.WorksheetSupport$.redirected(W
                                                  //| orksheetSupport.scala:65)
                                                  //| 	at org.scalaide.worksheet.runtime.library.WorksheetSupport$.$execute(Wor
                                                  //| ksheetSupport.scala:75)
                                                  //| 	at riftwarp.worksheets.Worksheet$.main(riftwarp.worksheets.Worksheet.sca
                                                  //| la:9)
                                                  //| 	at riftwarp.worksheets.Worksheet.main(riftwarp.worksheets.Worksheet.scal
                                                  //| a)

  testObject == backFromWarp

  riftWarp.prepareForWarp[DimensionRawMap](RiftMap())(testObject).bind(warpStream =>
    riftWarp.receiveFromWarp[DimensionRawMap, TestObjectA](RiftMap())(warpStream)).map(rearrived =>
    rearrived == testObject)


  riftWarp.prepareForWarp[DimensionCord](RiftJson())(testObject).bind(warpStream =>
    riftWarp.receiveFromWarp[DimensionCord, TestObjectA](RiftJson())(warpStream)).map(rearrived =>
    rearrived == testObject)

}