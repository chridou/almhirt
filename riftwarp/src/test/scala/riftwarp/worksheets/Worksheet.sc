package riftwarp.worksheets

import scalaz.syntax.validation._
import almhirt.syntax.almvalidation._
import riftwarp._
import riftwarp.impl._
import riftwarp.impl.rematerializers.simplema._

object Worksheet {
  val riftWarp = RiftWarp.unsafeWithDefaults      //> riftWarp  : riftwarp.RiftWarp = riftwarp.RiftWarp$$anon$97@4d8d042a
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

  val testObject = TestObjectA.pete               //> testObject  : riftwarp.TestObjectA = TestObjectA([B@45a86784,[B@33242ed6,Pr
                                                  //| imitiveTypes(I am Pete,true,127,-237823,-278234263,265876257682376587365863
                                                  //| 876528756875682765252520577305007209857025728132213242,1.3672322,1.36723223
                                                  //| 50005,23761247614876823746.23846749182408,2012-11-28T07:53:02.327+01:00,60a
                                                  //| bbe0a-1dc2-4bef-a7f8-df3aa657c1f4),PrimitiveListMAs(List(alpha, beta, gamma
                                                  //| , delta),List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),List(1.0, 0.5, 0.2, 0.125),Lis
                                                  //| t(1.333333, 1.33333335, 1.6666666, 1.6666667),List(2012-11-28T08:53:02.421+
                                                  //| 01:00, 2012-11-28T09:53:02.421+01:00, 2012-11-28T10:53:02.421+01:00, 2012-1
                                                  //| 1-28T11:53:02.421+01:00)),PrimitiveVectorMAs(Vector(alpha, beta, gamma, del
                                                  //| ta),Vector(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),Vector(1.0, 0.5, 0.2, 0.125),Vect
                                                  //| or(1.333333, 1.33333335, 1.6666666, 1.6666667),Vector(2012-11-28T08:53:02.4
                                                  //| 21+01:00, 2012-11-28T09:53:02.421+01:00, 2012-11-28T10:53:02.421+01:00, 201
                                                  //| 2-11-28T11:53:02.421+01:00)),PrimitiveSetMAs(Set(alpha, beta, gamma, delta)
                                                  //| ,Set(5, 10, 1, 6, 9, 2, 7, 3, 8, 4),Set(1.0, 0.5, 0.2, 0.125),Set(1.333333,
                                                  //|  1.33333335, 1.6666666, 1.6666667),Set(2012-11-28T08:53:02.452+01:00, 2012-
                                                  //| 11-28T09:53:02.452+01:00, 2012-11-28T10:53:02.452+01:00, 2012-11-28T11:53:0
                                                  //| 2.452+01:00)),PrimitiveIterableMAs(List(alpha, beta, gamma, delta),List(1, 
                                                  //| 2, 3, 4, 5, 6, 7, 8, 9, 10),List(1.0, 0.5, 0.2, 0.125),List(1.333333, 1.333
                                                  //| 33335, 1.6666666, 1.6666667),List(2012-11-28T08:53:02.452+01:00, 2012-11-28
                                                  //| T09:53:02.452+01:00, 2012-11-28T10:53:02.452+01:00, 2012-11-28T11:53:02.452
                                                  //| +01:00)),Some(TestAddress(Berlin,At the wall 89)))
  val warpStream = riftWarp.prepareForWarp[DimensionCord](RiftJson())(testObject).forceResult
                                                  //> warpStream  : riftwarp.DimensionCord = DimensionCord({"riftwarptd":"riftwar
                                                  //| p.TestObjectA","arrayByte":[126,-123,12,-45,-128],"blob":"AAAAAAAGhQzTgHAAA
                                                  //| AA=","primitiveTypes":{"riftwarptd":"riftwarp.PrimitiveTypes","str":"I am P
                                                  //| ete","bool":true,"byte":127,"int":-237823,"long":-278234263,"bigInt":"26587
                                                  //| 6257682376587365863876528756875682765252520577305007209857025728132213242",
                                                  //| "float":1.3672322034835815,"double":1.3672322350005,"bigDec":"2376124761487
                                                  //| 6823746.23846749182408","dateTime":"2012-11-28T07:53:02.327+01:00","uuid":"
                                                  //| 60abbe0a-1dc2-4bef-a7f8-df3aa657c1f4"},"primitiveListMAs":{"riftwarptd":"ri
                                                  //| ftwarp.PrimitiveListMAs","listString":["alpha","beta","gamma","delta"],"lis
                                                  //| tInt":[1,2,3,4,5,6,7,8,9,10],"listDouble":[1.0,0.5,0.2,0.125],"listBigDecim
                                                  //| al":["1.333333","1.33333335","1.6666666","1.6666667"],"listDateTime":["2012
                                                  //| -11-28T08:53:02.421+01:00","2012-11-28T09:53:02.421+01:00","2012-11-28T10:5
                                                  //| 3:02.421+01:00","2012-11-28T11:53:02.421+01:00"]},"primitiveVectorMAs":{"ri
                                                  //| ftwarptd":"riftwarp.PrimitiveVectorMAs","vectorString":["alpha","beta","gam
                                                  //| ma","delta"],"vectorInt":[1,2,3,4,5,6,7,8,9,10],"vectorDouble":[1.0,0.5,0.2
                                                  //| ,0.125],"vectorBigDecimal":["1.333333","1.33333335","1.6666666","1.6666667"
                                                  //| ],"vectorDateTime":["2012-11-28T08:53:02.421+01:00","2012-11-28T09:53:02.42
                                                  //| 1+01:00","2012-11-28T10:53:02.421+01:00","2012-11-28T11:53:02.421+01:00"]},
                                                  //| "primitiveSetMAs":{"riftwarptd":"riftwarp.PrimitiveSetMAs","setString":["al
                                                  //| pha","beta","gamma","delta"],"setInt":[8,9,10,4,7,5,1,6,3,2],"setDouble":[1
                                                  //| .0,0.5,0.2,0.125],"setBigDecimal":["1.333333","1.33333335","1.6666666","1.6
                                                  //| 666667"],"setDateTime":["2012-11-28T08:53:02.452+01:00","2012-11-28T09:53:0
                                                  //| 2.452+01:00","2012-11-28T10:53:02.452+01:00","2012-11-28T11:53:02.452+01:00
                                                  //| "]},"primitiveIterableMAs":{"riftwarptd":"riftwarp.PrimitiveIterableMAs","i
                                                  //| terableString":["alpha","beta","gamma","delta"],"iterableInt":[1,2,3,4,5,6,
                                                  //| 7,8,9,10],"iterableDouble":[1.0,0.5,0.2,0.125],"iterableBigDecimal":["1.333
                                                  //| 333","1.33333335","1.6666666","1.6666667"],"iterableDateTime":["2012-11-28T
                                                  //| 08:53:02.452+01:00","2012-11-28T09:53:02.452+01:00","2012-11-28T10:53:02.45
                                                  //| 2+01:00","2012-11-28T11:53:02.452+01:00"]},"addressOpt":{"riftwarptd":"rift
                                                  //| warp.TestAddress","city":"Berlin","street":"At the wall 89"}})

  val backFromWarpV = riftWarp.receiveFromWarp[DimensionCord, TestObjectA](RiftJson())(warpStream)
                                                  //> backFromWarpV  : almhirt.common.package.AlmValidation[riftwarp.TestObjectA]
                                                  //|  = Success(TestObjectA([B@7a4f8595,[B@7acb37ee,PrimitiveTypes(I am Pete,tru
                                                  //| e,127,-237823,-278234263,26587625768237658736586387652875687568276525252057
                                                  //| 7305007209857025728132213242,1.3672322,1.3672322350005,23761247614876823746
                                                  //| .23846749182408,2012-11-28T07:53:02.327+01:00,60abbe0a-1dc2-4bef-a7f8-df3aa
                                                  //| 657c1f4),PrimitiveListMAs(List(alpha, beta, gamma, delta),List(1, 2, 3, 4, 
                                                  //| 5, 6, 7, 8, 9, 10),List(1.0, 0.5, 0.2, 0.125),List(1.333333, 1.33333335, 1.
                                                  //| 6666666, 1.6666667),List(2012-11-28T08:53:02.421+01:00, 2012-11-28T09:53:02
                                                  //| .421+01:00, 2012-11-28T10:53:02.421+01:00, 2012-11-28T11:53:02.421+01:00)),
                                                  //| PrimitiveVectorMAs(Vector(alpha, beta, gamma, delta),Vector(1, 2, 3, 4, 5, 
                                                  //| 6, 7, 8, 9, 10),Vector(1.0, 0.5, 0.2, 0.125),Vector(1.333333, 1.33333335, 1
                                                  //| .6666666, 1.6666667),Vector(2012-11-28T08:53:02.421+01:00, 2012-11-28T09:53
                                                  //| :02.421+01:00, 2012-11-28T10:53:02.421+01:00, 2012-11-28T11:53:02.421+01:00
                                                  //| )),PrimitiveSetMAs(Set(alpha, beta, gamma, delta),Set(5, 10, 1, 6, 9, 2, 7,
                                                  //|  3, 8, 4),Set(1.0, 0.5, 0.2, 0.125),Set(1.333333, 1.33333335, 1.6666666, 1.
                                                  //| 6666667),Set(2012-11-28T08:53:02.452+01:00, 2012-11-28T09:53:02.452+01:00, 
                                                  //| 2012-11-28T10:53:02.452+01:00, 2012-11-28T11:53:02.452+01:00)),PrimitiveIte
                                                  //| rableMAs(Set(alpha, beta, gamma, delta),Set(5, 10, 1, 6, 9, 2, 7, 3, 8, 4),
                                                  //| Set(1.0, 0.5, 0.2, 0.125),Set(1.333333, 1.33333335, 1.6666666, 1.6666667),S
                                                  //| et(2012-11-28T08:53:02.452+01:00, 2012-11-28T09:53:02.452+01:00, 2012-11-28
                                                  //| T10:53:02.452+01:00, 2012-11-28T11:53:02.452+01:00)),Some(TestAddress(Berli
                                                  //| n,At the wall 89))))
  


  val backFromWarp = backFromWarpV.forceResult    //> backFromWarp  : riftwarp.TestObjectA = TestObjectA([B@7a4f8595,[B@7acb37ee,
                                                  //| PrimitiveTypes(I am Pete,true,127,-237823,-278234263,2658762576823765873658
                                                  //| 63876528756875682765252520577305007209857025728132213242,1.3672322,1.367232
                                                  //| 2350005,23761247614876823746.23846749182408,2012-11-28T07:53:02.327+01:00,6
                                                  //| 0abbe0a-1dc2-4bef-a7f8-df3aa657c1f4),PrimitiveListMAs(List(alpha, beta, gam
                                                  //| ma, delta),List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),List(1.0, 0.5, 0.2, 0.125),L
                                                  //| ist(1.333333, 1.33333335, 1.6666666, 1.6666667),List(2012-11-28T08:53:02.42
                                                  //| 1+01:00, 2012-11-28T09:53:02.421+01:00, 2012-11-28T10:53:02.421+01:00, 2012
                                                  //| -11-28T11:53:02.421+01:00)),PrimitiveVectorMAs(Vector(alpha, beta, gamma, d
                                                  //| elta),Vector(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),Vector(1.0, 0.5, 0.2, 0.125),Ve
                                                  //| ctor(1.333333, 1.33333335, 1.6666666, 1.6666667),Vector(2012-11-28T08:53:02
                                                  //| .421+01:00, 2012-11-28T09:53:02.421+01:00, 2012-11-28T10:53:02.421+01:00, 2
                                                  //| 012-11-28T11:53:02.421+01:00)),PrimitiveSetMAs(Set(alpha, beta, gamma, delt
                                                  //| a),Set(5, 10, 1, 6, 9, 2, 7, 3, 8, 4),Set(1.0, 0.5, 0.2, 0.125),Set(1.33333
                                                  //| 3, 1.33333335, 1.6666666, 1.6666667),Set(2012-11-28T08:53:02.452+01:00, 201
                                                  //| 2-11-28T09:53:02.452+01:00, 2012-11-28T10:53:02.452+01:00, 2012-11-28T11:53
                                                  //| :02.452+01:00)),PrimitiveIterableMAs(Set(alpha, beta, gamma, delta),Set(5, 
                                                  //| 10, 1, 6, 9, 2, 7, 3, 8, 4),Set(1.0, 0.5, 0.2, 0.125),Set(1.333333, 1.33333
                                                  //| 335, 1.6666666, 1.6666667),Set(2012-11-28T08:53:02.452+01:00, 2012-11-28T09
                                                  //| :53:02.452+01:00, 2012-11-28T10:53:02.452+01:00, 2012-11-28T11:53:02.452+01
                                                  //| :00)),Some(TestAddress(Berlin,At the wall 89)))

  testObject == backFromWarp                      //> res0: Boolean = false

  riftWarp.prepareForWarp[DimensionRawMap](RiftMap())(testObject).bind(warpStream =>
    riftWarp.receiveFromWarp[DimensionRawMap, TestObjectA](RiftMap())(warpStream)).map(rearrived =>
    rearrived == testObject)                      //> java.lang.RuntimeException: 
                                                  //| 	at scala.sys.package$.error(package.scala:27)
                                                  //| 	at riftwarp.impl.dematerializers.ToMapDematerializer.addPrimitiveMA(ToRa
                                                  //| wMapDematerializer.scala:50)
                                                  //| 	at riftwarp.PrimitiveListMAsDecomposer$$anonfun$decompose$20.apply(TestO
                                                  //| bjectASerialization.scala:97)
                                                  //| 	at riftwarp.PrimitiveListMAsDecomposer$$anonfun$decompose$20.apply(TestO
                                                  //| bjectASerialization.scala:97)
                                                  //| 	at scalaz.Validation$class.bind(Validation.scala:137)
                                                  //| 	at scalaz.Success.bind(Validation.scala:304)
                                                  //| 	at riftwarp.PrimitiveListMAsDecomposer.decompose(TestObjectASerializatio
                                                  //| n.scala:97)
                                                  //| 	at riftwarp.PrimitiveListMAsDecomposer.decompose(TestObjectASerializatio
                                                  //| n.scala:93)
                                                  //| 	at riftwarp.impl.dematerializers.ToMapDematerializer.addComplexType(ToRa
                                                  //| wMapDematerializer.scala:37)
                                                  //| 	at riftwarp.impl.dematerializers.ToMapDematerializer.addComplexType(ToRa
                                                  //| wMapDematerializer.scala:44)
                                                  //| 	at riftwarp.TestObjectADecomposer$$anonfun$decompose$4.apply(TestObjectA
                                                  //| Serialization.scala:17)
                                                  //| 	at riftwarp.TestObjectADecomposer$$anonfun$decompose$4.apply(TestObjectA
                                                  //| Serialization.scala:17)
                                                  //| 	at scalaz.Validation$class.bind(Validation.scala:137)
                                                  //| 	at scalaz.Success.bind(Validation.scala:304)
                                                  //| 	at riftwarp.TestObjectADecomposer.decompose(TestObjectASerialization.sca
                                                  //| la:17)
                                                  //| 	at riftwarp.TestObjectADecomposer.decompose(TestObjectASerialization.sca
                                                  //| la:10)
                                                  //| 	at riftwarp.Decomposer$class.decomposeRaw(Decomposer.scala:13)
                                                  //| 	at riftwarp.TestObjectADecomposer.decomposeRaw(TestObjectASerialization.
                                                  //| scala:10)
                                                  //| 	at riftwarp.RiftWarp$$anonfun$prepareForWarp$1.apply(RiftWarp.scala:26)
                                                  //| 
                                                  //| 	at riftwarp.RiftWarp$$anonfun$prepareForWarp$1.apply(RiftWarp.scala:23)
                                                  //| 
                                                  //| 	at scalaz.Validation$class.bind(Validation.scala:137)
                                                  //| 	at scalaz.Success.bind(Validation.scala:304)
                                                  //| 	at riftwarp.RiftWarp$class.prepareForWarp(RiftWarp.scala:23)
                                                  //| 	at riftwarp.RiftWarp$$anon$97.prepareForWarp(RiftWarp.scala:57)
                                                  //| 	at riftwarp.worksheets.Worksheet$$anonfun$main$1.apply$mcV$sp(riftwarp.w
                                                  //| orksheets.Worksheet.scala:39)
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


  riftWarp.prepareForWarp[DimensionCord](RiftJson())(testObject).bind(warpStream =>
    riftWarp.receiveFromWarp[DimensionCord, TestObjectA](RiftJson())(warpStream)).map(rearrived =>
    rearrived == testObject)

}