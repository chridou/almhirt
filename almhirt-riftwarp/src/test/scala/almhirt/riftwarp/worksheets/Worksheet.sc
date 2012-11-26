package almhirt.riftwarp.worksheets

import scalaz.syntax.validation._
import almhirt.syntax.almvalidation._
import almhirt.riftwarp._
import almhirt.riftwarp.impl._
import almhirt.riftwarp.impl.rematerializers.simplema._

object Worksheet {
  val riftWarp = RiftWarp.unsafeWithDefaults      //> riftWarp  : almhirt.riftwarp.RiftWarp = almhirt.riftwarp.RiftWarp$$anon$97@6
                                                  //| 01eb536
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


  val testObject = TestObjectA.pete               //> testObject  : almhirt.riftwarp.TestObjectA = TestObjectA([B@3d2f0499,[B@59d
                                                  //| 6f830,PrimitiveTypes(I am Pete,true,127,-237823,-278234263,2658762576823765
                                                  //| 87365863876528756875682765252520577305007209857025728132213242,1.3672322,1.
                                                  //| 3672322350005,23761247614876823746.23846749182408,2012-11-26T13:40:03.814+0
                                                  //| 1:00,e4d3af69-e48e-411c-b935-16e1a8840b09),PrimitiveListMAs(List(alpha, bet
                                                  //| a, gamma, delta),List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),List(1.0, 0.5, 0.2, 0.
                                                  //| 125),List(1.333333, 1.33333335, 1.6666666, 1.6666667),List(2012-11-26T14:40
                                                  //| :03.892+01:00, 2012-11-26T15:40:03.892+01:00, 2012-11-26T16:40:03.892+01:00
                                                  //| , 2012-11-26T17:40:03.892+01:00)),PrimitiveVectorMAs(Vector(alpha, beta, ga
                                                  //| mma, delta),Vector(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),Vector(1.0, 0.5, 0.2, 0.1
                                                  //| 25),Vector(1.333333, 1.33333335, 1.6666666, 1.6666667),Vector(2012-11-26T14
                                                  //| :40:03.892+01:00, 2012-11-26T15:40:03.892+01:00, 2012-11-26T16:40:03.892+01
                                                  //| :00, 2012-11-26T17:40:03.892+01:00)),PrimitiveSetMAs(Set(alpha, beta, gamma
                                                  //| , delta),Set(5, 10, 1, 6, 9, 2, 7, 3, 8, 4),Set(1.0, 0.5, 0.2, 0.125),Set(1
                                                  //| .333333, 1.33333335, 1.6666666, 1.6666667),Set(2012-11-26T14:40:03.908+01:0
                                                  //| 0, 2012-11-26T15:40:03.908+01:00, 2012-11-26T16:40:03.908+01:00, 2012-11-26
                                                  //| T17:40:03.908+01:00)),PrimitiveIterableMAs(List(alpha, beta, gamma, delta),
                                                  //| List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),List(1.0, 0.5, 0.2, 0.125),List(1.33333
                                                  //| 3, 1.33333335, 1.6666666, 1.6666667),List(2012-11-26T14:40:03.908+01:00, 20
                                                  //| 12-11-26T15:40:03.908+01:00, 2012-11-26T16:40:03.908+01:00, 2012-11-26T17:4
                                                  //| 0:03.908+01:00)),Some(TestAddress(Berlin,At the wall 89)))

  val warpStream = riftWarp.prepareForWarp[DimensionCord](RiftJson())(testObject).forceResult
                                                  //> warpStream  : almhirt.riftwarp.DimensionCord = DimensionCord({"riftwarptd":
                                                  //| "almhirt.riftwarp.TestObjectA","arrayByte":[126,-123,12,-45,-128],"blob":"A
                                                  //| AAAAAAGhQzTgHAAAAA=","primitiveTypes":{"riftwarptd":"almhirt.riftwarp.Primi
                                                  //| tiveTypes","str":"I am Pete","bool":true,"byte":127,"int":-237823,"long":-2
                                                  //| 78234263,"bigInt":"26587625768237658736586387652875687568276525252057730500
                                                  //| 7209857025728132213242","float":1.3672322034835815,"double":1.3672322350005
                                                  //| ,"bigDec":"23761247614876823746.23846749182408","dateTime":"2012-11-26T13:4
                                                  //| 0:03.814+01:00","uuid":"e4d3af69-e48e-411c-b935-16e1a8840b09"},"primitiveLi
                                                  //| stMAs":{"riftwarptd":"almhirt.riftwarp.PrimitiveListMAs","listString":["alp
                                                  //| ha","beta","gamma","delta"],"listInt":[1,2,3,4,5,6,7,8,9,10],"listDouble":[
                                                  //| 1.0,0.5,0.2,0.125],"listBigDecimal":["1.333333","1.33333335","1.6666666","1
                                                  //| .6666667"],"listDateTime":["2012-11-26T14:40:03.892+01:00","2012-11-26T15:4
                                                  //| 0:03.892+01:00","2012-11-26T16:40:03.892+01:00","2012-11-26T17:40:03.892+01
                                                  //| :00"]},"primitiveVectorMAs":{"riftwarptd":"almhirt.riftwarp.PrimitiveVector
                                                  //| MAs","vectorString":["alpha","beta","gamma","delta"],"vectorInt":[1,2,3,4,5
                                                  //| ,6,7,8,9,10],"vectorDouble":[1.0,0.5,0.2,0.125],"vectorBigDecimal":["1.3333
                                                  //| 33","1.33333335","1.6666666","1.6666667"],"vectorDateTime":["2012-11-26T14:
                                                  //| 40:03.892+01:00","2012-11-26T15:40:03.892+01:00","2012-11-26T16:40:03.892+0
                                                  //| 1:00","2012-11-26T17:40:03.892+01:00"]},"primitiveSetMAs":{"riftwarptd":"al
                                                  //| mhirt.riftwarp.PrimitiveSetMAs","setString":["alpha","beta","gamma","delta"
                                                  //| ],"setInt":[5,10,1,6,9,2,7,3,8,4],"setDouble":[1.0,0.5,0.2,0.125],"setBigDe
                                                  //| cimal":["1.333333","1.33333335","1.6666666","1.6666667"],"setDateTime":["20
                                                  //| 12-11-26T14:40:03.908+01:00","2012-11-26T15:40:03.908+01:00","2012-11-26T16
                                                  //| :40:03.908+01:00","2012-11-26T17:40:03.908+01:00"]},"primitiveIterableMAs":
                                                  //| {"riftwarptd":"almhirt.riftwarp.PrimitiveIterableMAs","iterableString":["al
                                                  //| pha","beta","gamma","delta"],"iterableInt":[1,2,3,4,5,6,7,8,9,10],"iterable
                                                  //| Double":[1.0,0.5,0.2,0.125],"iterableBigDecimal":["1.333333","1.33333335","
                                                  //| 1.6666666","1.6666667"],"iterableDateTime":["2012-11-26T14:40:03.908+01:00"
                                                  //| ,"2012-11-26T15:40:03.908+01:00","2012-11-26T16:40:03.908+01:00","2012-11-2
                                                  //| 6T17:40:03.908+01:00"]},"addressOpt":{"riftwarptd":"almhirt.riftwarp.TestAd
                                                  //| dress","city":"Berlin","street":"At the wall 89"}})

  val backFromWarpV = riftWarp.receiveFromWarp[DimensionCord, TestObjectA](RiftJson())(warpStream)
                                                  //> backFromWarpV  : almhirt.common.package.AlmValidation[almhirt.riftwarp.Test
                                                  //| ObjectA] = Success(TestObjectA([B@44b6e204,[B@42b097da,PrimitiveTypes(I am 
                                                  //| Pete,true,127,-237823,-278234263,265876257682376587365863876528756875682765
                                                  //| 252520577305007209857025728132213242,1.3672322,1.3672322350005,237612476148
                                                  //| 76823746.23846749182408,2012-11-26T13:40:03.814+01:00,e4d3af69-e48e-411c-b9
                                                  //| 35-16e1a8840b09),PrimitiveListMAs(List(alpha, beta, gamma, delta),List(1, 2
                                                  //| , 3, 4, 5, 6, 7, 8, 9, 10),List(1.0, 0.5, 0.2, 0.125),List(1.333333, 1.3333
                                                  //| 3335, 1.6666666, 1.6666667),List(2012-11-26T14:40:03.892+01:00, 2012-11-26T
                                                  //| 15:40:03.892+01:00, 2012-11-26T16:40:03.892+01:00, 2012-11-26T17:40:03.892+
                                                  //| 01:00)),PrimitiveVectorMAs(Vector(alpha, beta, gamma, delta),Vector(1, 2, 3
                                                  //| , 4, 5, 6, 7, 8, 9, 10),Vector(1.0, 0.5, 0.2, 0.125),Vector(1.333333, 1.333
                                                  //| 33335, 1.6666666, 1.6666667),Vector(2012-11-26T14:40:03.892+01:00, 2012-11-
                                                  //| 26T15:40:03.892+01:00, 2012-11-26T16:40:03.892+01:00, 2012-11-26T17:40:03.8
                                                  //| 92+01:00)),PrimitiveSetMAs(Set(alpha, beta, gamma, delta),Set(5, 10, 1, 6, 
                                                  //| 9, 2, 7, 3, 8, 4),Set(1.0, 0.5, 0.2, 0.125),Set(1.333333, 1.33333335, 1.666
                                                  //| 6666, 1.6666667),Set(2012-11-26T14:40:03.908+01:00, 2012-11-26T15:40:03.908
                                                  //| +01:00, 2012-11-26T16:40:03.908+01:00, 2012-11-26T17:40:03.908+01:00)),Prim
                                                  //| itiveIterableMAs(Set(alpha, beta, gamma, delta),Set(5, 10, 1, 6, 9, 2, 7, 3
                                                  //| , 8, 4),Set(1.0, 0.5, 0.2, 0.125),Set(1.333333, 1.33333335, 1.6666666, 1.66
                                                  //| 66667),Set(2012-11-26T14:40:03.908+01:00, 2012-11-26T15:40:03.908+01:00, 20
                                                  //| 12-11-26T16:40:03.908+01:00, 2012-11-26T17:40:03.908+01:00)),Some(TestAddre
                                                  //| ss(Berlin,At the wall 89))))

  val backFromWarp = backFromWarpV.forceResult    //> backFromWarp  : almhirt.riftwarp.TestObjectA = TestObjectA([B@44b6e204,[B@4
                                                  //| 2b097da,PrimitiveTypes(I am Pete,true,127,-237823,-278234263,26587625768237
                                                  //| 6587365863876528756875682765252520577305007209857025728132213242,1.3672322,
                                                  //| 1.3672322350005,23761247614876823746.23846749182408,2012-11-26T13:40:03.814
                                                  //| +01:00,e4d3af69-e48e-411c-b935-16e1a8840b09),PrimitiveListMAs(List(alpha, b
                                                  //| eta, gamma, delta),List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),List(1.0, 0.5, 0.2, 
                                                  //| 0.125),List(1.333333, 1.33333335, 1.6666666, 1.6666667),List(2012-11-26T14:
                                                  //| 40:03.892+01:00, 2012-11-26T15:40:03.892+01:00, 2012-11-26T16:40:03.892+01:
                                                  //| 00, 2012-11-26T17:40:03.892+01:00)),PrimitiveVectorMAs(Vector(alpha, beta, 
                                                  //| gamma, delta),Vector(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),Vector(1.0, 0.5, 0.2, 0
                                                  //| .125),Vector(1.333333, 1.33333335, 1.6666666, 1.6666667),Vector(2012-11-26T
                                                  //| 14:40:03.892+01:00, 2012-11-26T15:40:03.892+01:00, 2012-11-26T16:40:03.892+
                                                  //| 01:00, 2012-11-26T17:40:03.892+01:00)),PrimitiveSetMAs(Set(alpha, beta, gam
                                                  //| ma, delta),Set(5, 10, 1, 6, 9, 2, 7, 3, 8, 4),Set(1.0, 0.5, 0.2, 0.125),Set
                                                  //| (1.333333, 1.33333335, 1.6666666, 1.6666667),Set(2012-11-26T14:40:03.908+01
                                                  //| :00, 2012-11-26T15:40:03.908+01:00, 2012-11-26T16:40:03.908+01:00, 2012-11-
                                                  //| 26T17:40:03.908+01:00)),PrimitiveIterableMAs(Set(alpha, beta, gamma, delta)
                                                  //| ,Set(5, 10, 1, 6, 9, 2, 7, 3, 8, 4),Set(1.0, 0.5, 0.2, 0.125),Set(1.333333,
                                                  //|  1.33333335, 1.6666666, 1.6666667),Set(2012-11-26T14:40:03.908+01:00, 2012-
                                                  //| 11-26T15:40:03.908+01:00, 2012-11-26T16:40:03.908+01:00, 2012-11-26T17:40:0
                                                  //| 3.908+01:00)),Some(TestAddress(Berlin,At the wall 89)))

  testObject == backFromWarp                      //> res0: Boolean = false

  riftWarp.prepareForWarp[DimensionRawMap](RiftMap())(testObject).bind(warpStream =>
    riftWarp.receiveFromWarp[DimensionRawMap, TestObjectA](RiftMap())(warpStream)).map(rearrived =>
    rearrived == testObject)                      //> res1: scalaz.Validation[almhirt.common.Problem,Boolean] = Success(true)


  riftWarp.prepareForWarp[DimensionCord](RiftJson())(testObject).bind(warpStream =>
    riftWarp.receiveFromWarp[DimensionCord, TestObjectA](RiftJson())(warpStream)).map(rearrived =>
    rearrived == testObject)                      //> res2: scalaz.Validation[almhirt.common.Problem,Boolean] = Success(false)

}