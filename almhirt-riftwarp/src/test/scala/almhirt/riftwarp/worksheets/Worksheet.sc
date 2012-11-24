package almhirt.riftwarp.worksheets

import almhirt.syntax.almvalidation._
import almhirt.riftwarp._
import almhirt.riftwarp.impl._

object Worksheet {
  val riftWarp = RiftWarp.unsafeWithDefaults      //> riftWarp  : almhirt.riftwarp.RiftWarp = almhirt.riftwarp.RiftWarp$$anon$49@9
                                                  //| d7fbfb
  riftWarp.barracks.addDecomposer(new TestObjectADecomposer())
  riftWarp.barracks.addDecomposer(new PrimitiveMAsDecomposer())
  riftWarp.barracks.addDecomposer(new TestAddressDecomposer())
  riftWarp.barracks.addRecomposer(new TestObjectARecomposer())
  riftWarp.barracks.addRecomposer(new TestAddressRecomposer())


  val testObject = TestObjectA.pete               //> testObject  : almhirt.riftwarp.TestObjectA = TestObjectA(I am Pete,Some(I am
                                                  //|  Henry, too),true,127,-237823,-278234263,26587625768237658736586387652875687
                                                  //| 5682765252520577305007209857025728132213242,1.3672322,1.3672322350005,237612
                                                  //| 47614876823746.23846749182408,2012-11-24T15:26:25.522+01:00,ec4bf700-9a08-40
                                                  //| 33-97bc-a6a7f2d618e9,[B@dcb52ae,[B@1fa12495,PrimitiveMAs(List(alpha, beta, g
                                                  //| amma, delta),List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),List(1.0, 0.5, 0.2, 0.125),
                                                  //| List(1.333333, 1.33333335, 1.6666666, 1.6666667),List(2012-11-24T16:26:25.56
                                                  //| 3+01:00, 2012-11-24T17:26:25.564+01:00, 2012-11-24T18:26:25.564+01:00, 2012-
                                                  //| 11-24T19:26:25.564+01:00),Vector(alpha, beta, gamma, delta),Vector(1, 2, 3, 
                                                  //| 4, 5, 6, 7, 8, 9, 10),Vector(1.0, 0.5, 0.2, 0.125),Vector(1.333333, 1.333333
                                                  //| 35, 1.6666666, 1.6666667),Vector(2012-11-24T16:26:25.565+01:00, 2012-11-24T1
                                                  //| 7:26:25.565+01:00, 2012-11-24T18:26:25.565+01:00, 2012-11-24T19:26:25.565+01
                                                  //| :00),Set(alpha, beta, gamma, delta),Set(5, 10, 1, 6, 9, 2, 7, 3, 8, 4),Set(1
                                                  //| .0, 0.5, 0.2, 0.125),Set(1.333333, 1.33333335, 1.6666666, 1.6666667),Set(201
                                                  //| 2-11-24T16:26:25.579+01:00, 2012-11-24T17:26:25.579+01:00, 2012-11-24T18:26:
                                                  //| 25.579+01:00, 2012-11-24T19:26:25.579+01:00),List(alpha, beta, gamma, delta)
                                                  //| ,List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),List(1.0, 0.5, 0.2, 0.125),List(1.33333
                                                  //| 3, 1.33333335, 1.6666666, 1.6666667),List(2012-11-24T16:26:25.580+01:00, 201
                                                  //| 2-11-24T17:26:25.580+01:00, 2012-11-24T18:26:25.580+01:00, 2012-11-24T19:26:
                                                  //| 25.580+01:00)),Some(TestAddress(Berlin,At the wall 89)))
         
  val resV = riftWarp.prepareForWarp[RiftJson, DimensionCord](testObject)
                                                  //> resV  : almhirt.common.package.AlmValidation[almhirt.riftwarp.DimensionCord]
                                                  //|  = Success(DimensionCord({"riftwarptd":"almhirt.riftwarp.TestObjectA","str":
                                                  //| "I am Pete","strOpt":"I am Henry, too","bool":true,"byte":127,"int":-237823,
                                                  //| "long":-278234263,"bigIng":"265876257682376587365863876528756875682765252520
                                                  //| 577305007209857025728132213242","float":1.3672322034835815,"double":1.367232
                                                  //| 2350005,"bigDec":"23761247614876823746.23846749182408","dateTime":"2012-11-2
                                                  //| 4T15:26:25.522+01:00","uuid":"ec4bf700-9a08-4033-97bc-a6a7f2d618e9","arrayBy
                                                  //| te":[126,-123,12,-45,-128],"blob":"AAAAAAAGhQzTgHAAAAA=","primitiveMAs":{"ri
                                                  //| ftwarptd":"almhirt.riftwarp.PrimitiveMAs","listString":["alpha","beta","gamm
                                                  //| a","delta"],"listInt":[1,2,3,4,5,6,7,8,9,10],"listDouble":[1.0,0.5,0.2,0.125
                                                  //| ],"listBigDecimal":["1.333333","1.33333335","1.6666666","1.6666667"],"listDa
                                                  //| teTime":["2012-11-24T16:26:25.563+01:00","2012-11-24T17:26:25.564+01:00","20
                                                  //| 12-11-24T18:26:25.564+01:00","2012-11-24T19:26:25.564+01:00"],"vectorString"
                                                  //| :["alpha","beta","gamma","delta"],"vectorInt":[1,2,3,4,5,6,7,8,9,10],"vector
                                                  //| Double":[1.0,0.5,0.2,0.125],"vectorBigDecimal":["1.333333","1.33333335","1.6
                                                  //| 666666","1.6666667"],"vectorDateTime":["2012-11-24T16:26:25.565+01:00","2012
                                                  //| -11-24T17:26:25.565+01:00","2012-11-24T18:26:25.565+01:00","2012-11-24T19:26
                                                  //| :25.565+01:00"],"setString":["alpha","beta","gamma","delta"],"setInt":[5,10,
                                                  //| 1,6,9,2,7,3,8,4],"setDouble":[1.0,0.5,0.2,0.125],"setBigDecimal":["1.333333"
                                                  //| ,"1.33333335","1.6666666","1.6666667"],"setDateTime":["2012-11-24T16:26:25.5
                                                  //| 79+01:00","2012-11-24T17:26:25.579+01:00","2012-11-24T18:26:25.579+01:00","2
                                                  //| 012-11-24T19:26:25.579+01:00"],"iterableString":["alpha","beta","gamma","del
                                                  //| ta"],"iterableInt":[1,2,3,4,5,6,7,8,9,10],"iterableDouble":[1.0,0.5,0.2,0.12
                                                  //| 5],"iterableBigDecimal":["1.333333","1.33333335","1.6666666","1.6666667"],"i
                                                  //| terableDateTime":["2012-11-24T16:26:25.580+01:00","2012-11-24T17:26:25.580+0
                                                  //| 1:00","2012-11-24T18:26:25.580+01:00","2012-11-24T19:26:25.580+01:00"]},"add
                                                  //| ressOpt":{"riftwarptd":"almhirt.riftwarp.TestAddress","city":"Berlin","stree
                                                  //| t":"At the wall 89"}}))
  
  val warpStream = resV.forceResult               //> warpStream  : almhirt.riftwarp.DimensionCord = DimensionCord({"riftwarptd":"
                                                  //| almhirt.riftwarp.TestObjectA","str":"I am Pete","strOpt":"I am Henry, too","
                                                  //| bool":true,"byte":127,"int":-237823,"long":-278234263,"bigIng":"265876257682
                                                  //| 376587365863876528756875682765252520577305007209857025728132213242","float":
                                                  //| 1.3672322034835815,"double":1.3672322350005,"bigDec":"23761247614876823746.2
                                                  //| 3846749182408","dateTime":"2012-11-24T15:26:25.522+01:00","uuid":"ec4bf700-9
                                                  //| a08-4033-97bc-a6a7f2d618e9","arrayByte":[126,-123,12,-45,-128],"blob":"AAAAA
                                                  //| AAGhQzTgHAAAAA=","primitiveMAs":{"riftwarptd":"almhirt.riftwarp.PrimitiveMAs
                                                  //| ","listString":["alpha","beta","gamma","delta"],"listInt":[1,2,3,4,5,6,7,8,9
                                                  //| ,10],"listDouble":[1.0,0.5,0.2,0.125],"listBigDecimal":["1.333333","1.333333
                                                  //| 35","1.6666666","1.6666667"],"listDateTime":["2012-11-24T16:26:25.563+01:00"
                                                  //| ,"2012-11-24T17:26:25.564+01:00","2012-11-24T18:26:25.564+01:00","2012-11-24
                                                  //| T19:26:25.564+01:00"],"vectorString":["alpha","beta","gamma","delta"],"vecto
                                                  //| rInt":[1,2,3,4,5,6,7,8,9,10],"vectorDouble":[1.0,0.5,0.2,0.125],"vectorBigDe
                                                  //| cimal":["1.333333","1.33333335","1.6666666","1.6666667"],"vectorDateTime":["
                                                  //| 2012-11-24T16:26:25.565+01:00","2012-11-24T17:26:25.565+01:00","2012-11-24T1
                                                  //| 8:26:25.565+01:00","2012-11-24T19:26:25.565+01:00"],"setString":["alpha","be
                                                  //| ta","gamma","delta"],"setInt":[5,10,1,6,9,2,7,3,8,4],"setDouble":[1.0,0.5,0.
                                                  //| 2,0.125],"setBigDecimal":["1.333333","1.33333335","1.6666666","1.6666667"],"
                                                  //| setDateTime":["2012-11-24T16:26:25.579+01:00","2012-11-24T17:26:25.579+01:00
                                                  //| ","2012-11-24T18:26:25.579+01:00","2012-11-24T19:26:25.579+01:00"],"iterable
                                                  //| String":["alpha","beta","gamma","delta"],"iterableInt":[1,2,3,4,5,6,7,8,9,10
                                                  //| ],"iterableDouble":[1.0,0.5,0.2,0.125],"iterableBigDecimal":["1.333333","1.3
                                                  //| 3333335","1.6666666","1.6666667"],"iterableDateTime":["2012-11-24T16:26:25.5
                                                  //| 80+01:00","2012-11-24T17:26:25.580+01:00","2012-11-24T18:26:25.580+01:00","2
                                                  //| 012-11-24T19:26:25.580+01:00"]},"addressOpt":{"riftwarptd":"almhirt.riftwarp
                                                  //| .TestAddress","city":"Berlin","street":"At the wall 89"}})

  val backFromWarpV = riftWarp.receiveFromWarp[DimensionCord, TestObjectA](RiftJson())(warpStream)
                                                  //> backFromWarpV  : almhirt.common.package.AlmValidation[almhirt.riftwarp.TestO
                                                  //| bjectA] = Failure(almhirt.common.UnspecifiedProblem
                                                  //| Not implemented
                                                  //| Category: SystemProblem
                                                  //| Severity: Major
                                                  //| Arguments: Map()
                                                  //| )
  
  val backFromWarp = backFromWarpV.forceResult    //> almhirt.common.ResultForcedFromValidationException: A value has been forced 
                                                  //| from a failure: Not implemented
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
                                                  //| mhirt.riftwarp.worksheets.Worksheet.scala:24)
                                                  //| 	at org.scalaide.worksheet.runtime.library.WorksheetSupport$$anonfun$$exe
                                                  //| cute$1.apply$mcV$sp(WorksheetSupport.scala:76)
                                                  //| 	at org.scalaide.worksheet.runtime.library.WorksheetSupport$.redirected(W
                                                  //| orksheetSupport.scala:65)
                                                  //| 	at org.scalaide.worksheet.runtime.library.WorksheetSupport$.$execute(Wor
                                                  //| ksheetSupport.scala:75)
                                                  //| 	at almhirt.riftwarp.worksheets.Worksheet$.main(almhirt.riftwarp.workshee
                                                  //| ts.Worksheet.scala:7)
                                                  //| 	at almhirt.riftwarp.worksheets.Worksheet.main(almhirt.riftwarp.worksheet
                                                  //| s.Worksheet.scala)
  
  testObject == backFromWarp
  
  
  
  riftWarp.prepareForWarp[RiftMap, DimensionRawMap](testObject).bind(warpStream =>
    riftWarp.receiveFromWarp[DimensionRawMap, TestObjectA](RiftMap())(warpStream)).map(rearrived =>
      rearrived == testObject)


  riftWarp.prepareForWarp[RiftJson, DimensionCord](testObject).bind(warpStream =>
    riftWarp.receiveFromWarp[DimensionCord, TestObjectA](RiftJson())(warpStream)).map(rearrived =>
      rearrived == testObject)
      
}