package riftwarp.worksheets

import scalaz.syntax.validation._
import almhirt.syntax.almvalidation._
import riftwarp._
import riftwarp.impl._
import riftwarp.impl.rematerializers.simplema._

object Worksheet {
  val riftWarp = RiftWarp.unsafeWithDefaults      //> riftWarp  : riftwarp.RiftWarp = riftwarp.RiftWarp$$anon$49@60cb03c4
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
  riftWarp.barracks.addDecomposer(new ComplexMAsDecomposer())
  riftWarp.barracks.addRecomposer(new ComplexMAsRecomposer())

  val testObject = TestObjectA.pete               //> testObject  : riftwarp.TestObjectA = TestObjectA([B@6d011211,[B@614951ff,Pr
                                                  //| imitiveTypes(I am Pete,true,127,-237823,-278234263,265876257682376587365863
                                                  //| 876528756875682765252520577305007209857025728132213242,1.3672322,1.36723223
                                                  //| 50005,23761247614876823746.23846749182408,2012-11-28T13:42:49.809+01:00,e13
                                                  //| e73a4-6643-45a5-a0d2-0e8726651e62),PrimitiveListMAs(List(alpha, beta, gamma
                                                  //| , delta),List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),List(1.0, 0.5, 0.2, 0.125),Lis
                                                  //| t(1.333333, 1.33333335, 1.6666666, 1.6666667),List(2012-11-28T14:42:49.902+
                                                  //| 01:00, 2012-11-28T15:42:49.902+01:00, 2012-11-28T16:42:49.902+01:00, 2012-1
                                                  //| 1-28T17:42:49.902+01:00)),PrimitiveVectorMAs(Vector(alpha, beta, gamma, del
                                                  //| ta),Vector(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),Vector(1.0, 0.5, 0.2, 0.125),Vect
                                                  //| or(1.333333, 1.33333335, 1.6666666, 1.6666667),Vector(2012-11-28T14:42:49.9
                                                  //| 02+01:00, 2012-11-28T15:42:49.902+01:00, 2012-11-28T16:42:49.902+01:00, 201
                                                  //| 2-11-28T17:42:49.902+01:00)),PrimitiveSetMAs(Set(al"p"ha, beta, gamma, delt
                                                  //| a),Set(5, 10, 1, 6, 9, 2, 7, 3, 8, 4),Set(1.0, 0.5, 0.2, 0.125),Set(1.33333
                                                  //| 3, 1.33333335, 1.6666666, 1.6666667),Set(2012-11-28T14:42:49.918+01:00, 201
                                                  //| 2-11-28T15:42:49.918+01:00, 2012-11-28T16:42:49.918+01:00, 2012-11-28T17:42
                                                  //| :49.918+01:00)),PrimitiveIterableMAs(List(alpha, beta, gamma, delta),List(1
                                                  //| , 2, 3, 4, 5, 6, 7, 8, 9, 10),List(1.0, 0.5, 0.2, 0.125),List(1.333333, 1.3
                                                  //| 3333335, 1.6666666, 1.6666667),List(2012-11-28T14:42:49.918+01:00, 2012-11-
                                                  //| 28T15:42:49.918+01:00, 2012-11-28T16:42:49.918+01:00, 2012-11-28T17:42:49.9
                                                  //| 18+01:00)),ComplexMAs(List(TestAddress(Hamburg,Am Hafen), TestAddress(New Y
                                                  //| ork,Broadway), TestAddress(Los Angeles ,Sunset Boulevard)),Vector(TestAddre
                                                  //| ss(Hamburg,Am Hafen), TestAddress(New York,Broadway), TestAddress(Los Angel
                                                  //| es ,Sunset Boulevard)),Set(TestAddress(Hamburg,Am Hafen), TestAddress(New Y
                                                  //| ork,Broadway), TestAddress(Los Angeles ,Sunset Boulevard)),List(true, hello
                                                  //| , 1, 2, 3.0, 3.0, TestAddress(Somewhere,here))),Some(TestAddress(Berlin,At 
                                                  //| the wall 89)))
  val warpStreamV = riftWarp.prepareForWarp[DimensionCord](RiftJson())(testObject)
                                                  //> warpStreamV  : almhirt.common.package.AlmValidation[riftwarp.DimensionCord]
                                                  //|  = Success(DimensionCord({"riftwarptd":"riftwarp.TestObjectA","arrayByte":[
                                                  //| 126,-123,12,-45,-128],"blob":"AAAAAAAGhQzTgHAAAAA=","primitiveTypes":{"rift
                                                  //| warptd":"riftwarp.PrimitiveTypes","str":"I am Pete","bool":true,"byte":127,
                                                  //| "int":-237823,"long":-278234263,"bigInt":"265876257682376587365863876528756
                                                  //| 875682765252520577305007209857025728132213242","float":1.3672322034835815,"
                                                  //| double":1.3672322350005,"bigDec":"23761247614876823746.23846749182408","dat
                                                  //| eTime":"2012-11-28T13:42:49.809+01:00","uuid":"e13e73a4-6643-45a5-a0d2-0e87
                                                  //| 26651e62"},"primitiveListMAs":{"riftwarptd":"riftwarp.PrimitiveListMAs","li
                                                  //| stString":["alpha","beta","gamma","delta"],"listInt":[1,2,3,4,5,6,7,8,9,10]
                                                  //| ,"listDouble":[1.0,0.5,0.2,0.125],"listBigDecimal":["1.333333","1.33333335"
                                                  //| ,"1.6666666","1.6666667"],"listDateTime":["2012-11-28T14:42:49.902+01:00","
                                                  //| 2012-11-28T15:42:49.902+01:00","2012-11-28T16:42:49.902+01:00","2012-11-28T
                                                  //| 17:42:49.902+01:00"]},"primitiveVectorMAs":{"riftwarptd":"riftwarp.Primitiv
                                                  //| eVectorMAs","vectorString":["alpha","beta","gamma","delta"],"vectorInt":[1,
                                                  //| 2,3,4,5,6,7,8,9,10],"vectorDouble":[1.0,0.5,0.2,0.125],"vectorBigDecimal":[
                                                  //| "1.333333","1.33333335","1.6666666","1.6666667"],"vectorDateTime":["2012-11
                                                  //| -28T14:42:49.902+01:00","2012-11-28T15:42:49.902+01:00","2012-11-28T16:42:4
                                                  //| 9.902+01:00","2012-11-28T17:42:49.902+01:00"]},"primitiveSetMAs":{"riftwarp
                                                  //| td":"riftwarp.PrimitiveSetMAs","setString":["al"p"ha","beta","gamma","delta
                                                  //| "],"setInt":[2,5,7,9,3,6,10,4,1,8],"setDouble":[1.0,0.5,0.2,0.125],"setBigD
                                                  //| ecimal":["1.333333","1.33333335","1.6666666","1.6666667"],"setDateTime":["2
                                                  //| 012-11-28T14:42:49.918+01:00","2012-11-28T15:42:49.918+01:00","2012-11-28T1
                                                  //| 6:42:49.918+01:00","2012-11-28T17:42:49.918+01:00"]},"primitiveIterableMAs"
                                                  //| :{"riftwarptd":"riftwarp.PrimitiveIterableMAs","iterableString":["alpha","b
                                                  //| eta","gamma","delta"],"iterableInt":[1,2,3,4,5,6,7,8,9,10],"iterableDouble"
                                                  //| :[1.0,0.5,0.2,0.125],"iterableBigDecimal":["1.333333","1.33333335","1.66666
                                                  //| 66","1.6666667"],"iterableDateTime":["2012-11-28T14:42:49.918+01:00","2012-
                                                  //| 11-28T15:42:49.918+01:00","2012-11-28T16:42:49.918+01:00","2012-11-28T17:42
                                                  //| :49.918+01:00"]},"complexMAs":{"riftwarptd":"riftwarp.ComplexMAs","addresse
                                                  //| s1":[{"riftwarptd":"riftwarp.TestAddress","city":"Hamburg","street":"Am Haf
                                                  //| en"},{"riftwarptd":"riftwarp.TestAddress","city":"New York","street":"Broad
                                                  //| way"},{"riftwarptd":"riftwarp.TestAddress","city":"Los Angeles ","street":"
                                                  //| Sunset Boulevard"}],"addresses2":[{"riftwarptd":"riftwarp.TestAddress","cit
                                                  //| y":"Hamburg","street":"Am Hafen"},{"riftwarptd":"riftwarp.TestAddress","cit
                                                  //| y":"New York","street":"Broadway"},{"riftwarptd":"riftwarp.TestAddress","ci
                                                  //| ty":"Los Angeles ","street":"Sunset Boulevard"}],"addresses3":[{"riftwarptd
                                                  //| ":"riftwarp.TestAddress","city":"Hamburg","street":"Am Hafen"},{"riftwarptd
                                                  //| ":"riftwarp.TestAddress","city":"New York","street":"Broadway"},{"riftwarpt
                                                  //| d":"riftwarp.TestAddress","city":"Los Angeles ","street":"Sunset Boulevard"
                                                  //| }],"anything":[true,"hello",1,2,3.0,3.0,{"riftwarptd":"riftwarp.TestAddress
                                                  //| ","city":"Somewhere","street":"here"}]},"addressOpt":{"riftwarptd":"riftwar
                                                  //| p.TestAddress","city":"Berlin","street":"At the wall 89"}}))

  val backFromWarpV = riftWarp.receiveFromWarp[DimensionCord, TestObjectA](RiftJson())(warpStreamV.forceResult)
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
                                                  //| 234263,"bigInt":"2658762576823765873658638765287568756827652525205773050072
                                                  //| 09857025728132213242","float":1.3672322034835815,"double":1.3672322350005,"
                                                  //| bigDec":"23761247614876823746.23846749182408","dateTime":"2012-11-28T13:42:
                                                  //| 49.809+01:00","uuid":"e13e73a4-6643-45a5-a0d2-0e8726651e62"},"primitiveList
                                                  //| MAs":{"riftwarptd":"riftwarp.PrimitiveListMAs","listString":["alpha","beta"
                                                  //| ,"gamma","delta"],"listInt":[1,2,3,4,5,6,7,8,9,10],"listDouble":[1.0,0.5,0.
                                                  //| 2,0.125],"listBigDecimal":["1.333333","1.33333335","1.6666666","1.6666667"]
                                                  //| ,"listDateTime":["2012-11-28T14:42:49.902+01:00","2012-11-28T15:42:49.902+0
                                                  //| 1:00","2012-11-28T16:42:49.902+01:00","2012-11-28T17:42:49.902+01:00"]},"pr
                                                  //| imitiveVectorMAs":{"riftwarptd":"riftwarp.PrimitiveVectorMAs","vectorString
                                                  //| ":["alpha","beta","gamma","delta"],"vectorInt":[1,2,3,4,5,6,7,8,9,10],"vect
                                                  //| orDouble":[1.0,0.5,0.2,0.125],"vectorBigDecimal":["1.333333","1.33333335","
                                                  //| 1.6666666","1.6666667"],"vectorDateTime":["2012-11-28T14:42:49.902+01:00","
                                                  //| 2012-11-28T15:42:49.902+01:00","2012-11-28T16:42:49.902+01:00","2012-11-28T
                                                  //| 17:42:49.902+01:00"]},"primitiveSetMAs":{"riftwarptd":"riftwarp.PrimitiveSe
                                                  //| tMAs","setString":["al"p"ha","beta","gamma","delta"],"setInt":[2,5,7,9,3,6,
                                                  //| 10,4,1,8],"setDouble":[1.0,0.5,0.2,0.125],"setBigDecimal":["1.333333","1.33
                                                  //| 333335","1.6666666","1.6666667"],"setDateTime":["2012-11-28T14:42:49.918+01
                                                  //| :00","2012-11-28T15:42:49.918+01:00","2012-11-28T16:42:49.918+01:00","2012-
                                                  //| 11-28T17:42:49.918+01:00"]},"primitiveIterableMAs":{"riftwarptd":"riftwarp.
                                                  //| PrimitiveIterableMAs","iterableString":["alpha","beta","gamma","delta"],"it
                                                  //| erableInt":[1,2,3,4,5,6,7,8,9,10],"iterableDouble":[1.0,0.5,0.2,0.125],"ite
                                                  //| rableBigDecimal":["1.333333","1.33333335","1.6666666","1.6666667"],"iterabl
                                                  //| eDateTime":["2012-11-28T14:42:49.918+01:00","2012-11-28T15:42:49.918+01:00"
                                                  //| ,"2012-11-28T16:42:49.918+01:00","2012-11-28T17:42:49.918+01:00"]},"complex
                                                  //| MAs":{"riftwarptd":"riftwarp.ComplexMAs","addresses1":[{"riftwarptd":"riftw
                                                  //| arp.TestAddress","city":"Hamburg","street":"Am Hafen"},{"riftwarptd":"riftw
                                                  //| arp.TestAddress","city":"New York","street":"Broadway"},{"riftwarptd":"rift
                                                  //| warp.TestAddress","city":"Los Angeles ","street":"Sunset Boulevard"}],"addr
                                                  //| esses2":[{"riftwarptd":"riftwarp.TestAddress","city":"Hamburg","street":"Am
                                                  //|  Hafen"},{"riftwarptd":"riftwarp.TestAddress","city":"New York","street":"B
                                                  //| roadway"},{"riftwarptd":"riftwarp.TestAddress","city":"Los Angeles ","stree
                                                  //| t":"Sunset Boulevard"}],"addresses3":[{"riftwarptd":"riftwarp.TestAddress",
                                                  //| "city":"Hamburg","street":"Am Hafen"},{"riftwarptd":"riftwarp.TestAddress",
                                                  //| "city":"New York","street":"Broadway"},{"riftwarptd":"riftwarp.TestAddress"
                                                  //| ,"city":"Los Angeles ","street":"Sunset Boulevard"}],"anything":[true,"hell
                                                  //| o",1,2,3.0,3.0,{"riftwarptd":"riftwarp.TestAddress","city":"Somewhere","str
                                                  //| eet":"here"}]},"addressOpt":{"riftwarptd":"riftwarp.TestAddress","city":"Be
                                                  //| rlin","street":"At the wall 89"}}
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
                                                  //| orksheets.Worksheet.scala:35)
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