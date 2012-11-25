package almhirt.riftwarp.worksheets

import almhirt.syntax.almvalidation._
import almhirt.riftwarp._
import almhirt.riftwarp.impl._

object Worksheet {
  val riftWarp = RiftWarp.unsafeWithDefaults      //> riftWarp  : almhirt.riftwarp.RiftWarp = almhirt.riftwarp.RiftWarp$$anon$97@5
                                                  //| 954864a
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

  val testObject = TestObjectA.pete               //> testObject  : almhirt.riftwarp.TestObjectA = TestObjectA([B@2cba5bdb,[B@7ee
                                                  //| 49dcd,PrimitiveTypes(I am Pete,true,127,-237823,-278234263,2658762576823765
                                                  //| 87365863876528756875682765252520577305007209857025728132213242,1.3672322,1.
                                                  //| 3672322350005,23761247614876823746.23846749182408,2012-11-25T18:35:56.155+0
                                                  //| 1:00,fb2f5249-86f5-4c27-950c-f0eaa388bb03),PrimitiveListMAs(List(alpha, bet
                                                  //| a, gamma, delta),List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),List(1.0, 0.5, 0.2, 0.
                                                  //| 125),List(1.333333, 1.33333335, 1.6666666, 1.6666667),List(2012-11-25T19:35
                                                  //| :56.202+01:00, 2012-11-25T20:35:56.203+01:00, 2012-11-25T21:35:56.203+01:00
                                                  //| , 2012-11-25T22:35:56.203+01:00)),PrimitiveVectorMAs(Vector(alpha, beta, ga
                                                  //| mma, delta),Vector(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),Vector(1.0, 0.5, 0.2, 0.1
                                                  //| 25),Vector(1.333333, 1.33333335, 1.6666666, 1.6666667),Vector(2012-11-25T19
                                                  //| :35:56.204+01:00, 2012-11-25T20:35:56.204+01:00, 2012-11-25T21:35:56.204+01
                                                  //| :00, 2012-11-25T22:35:56.204+01:00)),PrimitiveSetMAs(Set(alpha, beta, gamma
                                                  //| , delta),Set(5, 10, 1, 6, 9, 2, 7, 3, 8, 4),Set(1.0, 0.5, 0.2, 0.125),Set(1
                                                  //| .333333, 1.33333335, 1.6666666, 1.6666667),Set(2012-11-25T19:35:56.219+01:0
                                                  //| 0, 2012-11-25T20:35:56.219+01:00, 2012-11-25T21:35:56.219+01:00, 2012-11-25
                                                  //| T22:35:56.219+01:00)),PrimitiveIterableMAs(List(alpha, beta, gamma, delta),
                                                  //| List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),List(1.0, 0.5, 0.2, 0.125),List(1.33333
                                                  //| 3, 1.33333335, 1.6666666, 1.6666667),List(2012-11-25T19:35:56.219+01:00, 20
                                                  //| 12-11-25T20:35:56.219+01:00, 2012-11-25T21:35:56.219+01:00, 2012-11-25T22:3
                                                  //| 5:56.219+01:00)),Some(TestAddress(Berlin,At the wall 89)))
         
  val resV = riftWarp.prepareForWarp[RiftJson, DimensionCord](testObject)
                                                  //> resV  : almhirt.common.package.AlmValidation[almhirt.riftwarp.DimensionCord
                                                  //| ] = Success(DimensionCord({"riftwarptd":"almhirt.riftwarp.TestObjectA","arr
                                                  //| ayByte":[126,-123,12,-45,-128],"blob":"AAAAAAAGhQzTgHAAAAA=","primitiveType
                                                  //| s":{"riftwarptd":"almhirt.riftwarp.PrimitiveTypes","str":"I am Pete","bool"
                                                  //| :true,"byte":127,"int":-237823,"long":-278234263,"bigInt":"2658762576823765
                                                  //| 87365863876528756875682765252520577305007209857025728132213242","float":1.3
                                                  //| 672322034835815,"double":1.3672322350005,"bigDec":"23761247614876823746.238
                                                  //| 46749182408","dateTime":"2012-11-25T18:35:56.155+01:00","uuid":"fb2f5249-86
                                                  //| f5-4c27-950c-f0eaa388bb03"},"primitiveListMAs":{"riftwarptd":"almhirt.riftw
                                                  //| arp.PrimitiveListMAs","listString":["alpha","beta","gamma","delta"],"listIn
                                                  //| t":[1,2,3,4,5,6,7,8,9,10],"listDouble":[1.0,0.5,0.2,0.125],"listBigDecimal"
                                                  //| :["1.333333","1.33333335","1.6666666","1.6666667"],"listDateTime":["2012-11
                                                  //| -25T19:35:56.202+01:00","2012-11-25T20:35:56.203+01:00","2012-11-25T21:35:5
                                                  //| 6.203+01:00","2012-11-25T22:35:56.203+01:00"]},"primitiveVectorMAs":{"riftw
                                                  //| arptd":"almhirt.riftwarp.PrimitiveVectorMAs","vectorString":["alpha","beta"
                                                  //| ,"gamma","delta"],"vectorInt":[1,2,3,4,5,6,7,8,9,10],"vectorDouble":[1.0,0.
                                                  //| 5,0.2,0.125],"vectorBigDecimal":["1.333333","1.33333335","1.6666666","1.666
                                                  //| 6667"],"vectorDateTime":["2012-11-25T19:35:56.204+01:00","2012-11-25T20:35:
                                                  //| 56.204+01:00","2012-11-25T21:35:56.204+01:00","2012-11-25T22:35:56.204+01:0
                                                  //| 0"]},"primitiveSetMAs":{"riftwarptd":"almhirt.riftwarp.PrimitiveSetMAs","se
                                                  //| tString":["alpha","beta","gamma","delta"],"setInt":[5,10,1,6,9,2,7,3,8,4],"
                                                  //| setDouble":[1.0,0.5,0.2,0.125],"setBigDecimal":["1.333333","1.33333335","1.
                                                  //| 6666666","1.6666667"],"setDateTime":["2012-11-25T19:35:56.219+01:00","2012-
                                                  //| 11-25T20:35:56.219+01:00","2012-11-25T21:35:56.219+01:00","2012-11-25T22:35
                                                  //| :56.219+01:00"]},"primitiveIterableMAs":{"riftwarptd":"almhirt.riftwarp.Pri
                                                  //| mitiveIterableMAs","iterableString":["alpha","beta","gamma","delta"],"itera
                                                  //| bleInt":[1,2,3,4,5,6,7,8,9,10],"iterableDouble":[1.0,0.5,0.2,0.125],"iterab
                                                  //| leBigDecimal":["1.333333","1.33333335","1.6666666","1.6666667"],"iterableDa
                                                  //| teTime":["2012-11-25T19:35:56.219+01:00","2012-11-25T20:35:56.219+01:00","2
                                                  //| 012-11-25T21:35:56.219+01:00","2012-11-25T22:35:56.219+01:00"]},"addressOpt
                                                  //| ":{"riftwarptd":"almhirt.riftwarp.TestAddress","city":"Berlin","street":"At
                                                  //|  the wall 89"}}))
  
  val warpStream = resV.forceResult               //> warpStream  : almhirt.riftwarp.DimensionCord = DimensionCord({"riftwarptd":
                                                  //| "almhirt.riftwarp.TestObjectA","arrayByte":[126,-123,12,-45,-128],"blob":"A
                                                  //| AAAAAAGhQzTgHAAAAA=","primitiveTypes":{"riftwarptd":"almhirt.riftwarp.Primi
                                                  //| tiveTypes","str":"I am Pete","bool":true,"byte":127,"int":-237823,"long":-2
                                                  //| 78234263,"bigInt":"26587625768237658736586387652875687568276525252057730500
                                                  //| 7209857025728132213242","float":1.3672322034835815,"double":1.3672322350005
                                                  //| ,"bigDec":"23761247614876823746.23846749182408","dateTime":"2012-11-25T18:3
                                                  //| 5:56.155+01:00","uuid":"fb2f5249-86f5-4c27-950c-f0eaa388bb03"},"primitiveLi
                                                  //| stMAs":{"riftwarptd":"almhirt.riftwarp.PrimitiveListMAs","listString":["alp
                                                  //| ha","beta","gamma","delta"],"listInt":[1,2,3,4,5,6,7,8,9,10],"listDouble":[
                                                  //| 1.0,0.5,0.2,0.125],"listBigDecimal":["1.333333","1.33333335","1.6666666","1
                                                  //| .6666667"],"listDateTime":["2012-11-25T19:35:56.202+01:00","2012-11-25T20:3
                                                  //| 5:56.203+01:00","2012-11-25T21:35:56.203+01:00","2012-11-25T22:35:56.203+01
                                                  //| :00"]},"primitiveVectorMAs":{"riftwarptd":"almhirt.riftwarp.PrimitiveVector
                                                  //| MAs","vectorString":["alpha","beta","gamma","delta"],"vectorInt":[1,2,3,4,5
                                                  //| ,6,7,8,9,10],"vectorDouble":[1.0,0.5,0.2,0.125],"vectorBigDecimal":["1.3333
                                                  //| 33","1.33333335","1.6666666","1.6666667"],"vectorDateTime":["2012-11-25T19:
                                                  //| 35:56.204+01:00","2012-11-25T20:35:56.204+01:00","2012-11-25T21:35:56.204+0
                                                  //| 1:00","2012-11-25T22:35:56.204+01:00"]},"primitiveSetMAs":{"riftwarptd":"al
                                                  //| mhirt.riftwarp.PrimitiveSetMAs","setString":["alpha","beta","gamma","delta"
                                                  //| ],"setInt":[5,10,1,6,9,2,7,3,8,4],"setDouble":[1.0,0.5,0.2,0.125],"setBigDe
                                                  //| cimal":["1.333333","1.33333335","1.6666666","1.6666667"],"setDateTime":["20
                                                  //| 12-11-25T19:35:56.219+01:00","2012-11-25T20:35:56.219+01:00","2012-11-25T21
                                                  //| :35:56.219+01:00","2012-11-25T22:35:56.219+01:00"]},"primitiveIterableMAs":
                                                  //| {"riftwarptd":"almhirt.riftwarp.PrimitiveIterableMAs","iterableString":["al
                                                  //| pha","beta","gamma","delta"],"iterableInt":[1,2,3,4,5,6,7,8,9,10],"iterable
                                                  //| Double":[1.0,0.5,0.2,0.125],"iterableBigDecimal":["1.333333","1.33333335","
                                                  //| 1.6666666","1.6666667"],"iterableDateTime":["2012-11-25T19:35:56.219+01:00"
                                                  //| ,"2012-11-25T20:35:56.219+01:00","2012-11-25T21:35:56.219+01:00","2012-11-2
                                                  //| 5T22:35:56.219+01:00"]},"addressOpt":{"riftwarptd":"almhirt.riftwarp.TestAd
                                                  //| dress","city":"Berlin","street":"At the wall 89"}})

  val backFromWarpV = riftWarp.receiveFromWarp[DimensionCord, RiftJson, TestObjectA](warpStream)
                                                  //> backFromWarpV  : almhirt.common.package.AlmValidation[almhirt.riftwarp.Test
                                                  //| ObjectA] = Success(TestObjectA([B@4bd27069,[B@64d22462,PrimitiveTypes(I am 
                                                  //| Pete,true,127,-237823,-278234263,265876257682376587365863876528756875682765
                                                  //| 252520577305007209857025728132213242,1.3672322,1.3672322350005,237612476148
                                                  //| 76823746.23846749182408,2012-11-25T18:35:56.155+01:00,fb2f5249-86f5-4c27-95
                                                  //| 0c-f0eaa388bb03),PrimitiveListMAs(List(alpha, beta, gamma, delta),List(1, 2
                                                  //| , 3, 4, 5, 6, 7, 8, 9, 10),List(1.0, 0.5, 0.2, 0.125),List(1.333333, 1.3333
                                                  //| 3335, 1.6666666, 1.6666667),List(2012-11-25T19:35:56.202+01:00, 2012-11-25T
                                                  //| 20:35:56.203+01:00, 2012-11-25T21:35:56.203+01:00, 2012-11-25T22:35:56.203+
                                                  //| 01:00)),PrimitiveVectorMAs(Vector(alpha, beta, gamma, delta),Vector(1, 2, 3
                                                  //| , 4, 5, 6, 7, 8, 9, 10),Vector(1.0, 0.5, 0.2, 0.125),Vector(1.333333, 1.333
                                                  //| 33335, 1.6666666, 1.6666667),Vector(2012-11-25T19:35:56.204+01:00, 2012-11-
                                                  //| 25T20:35:56.204+01:00, 2012-11-25T21:35:56.204+01:00, 2012-11-25T22:35:56.2
                                                  //| 04+01:00)),PrimitiveSetMAs(Set(alpha, beta, gamma, delta),Set(5, 10, 1, 6, 
                                                  //| 9, 2, 7, 3, 8, 4),Set(1.0, 0.5, 0.2, 0.125),Set(1.333333, 1.33333335, 1.666
                                                  //| 6666, 1.6666667),Set(2012-11-25T19:35:56.219+01:00, 2012-11-25T20:35:56.219
                                                  //| +01:00, 2012-11-25T21:35:56.219+01:00, 2012-11-25T22:35:56.219+01:00)),Prim
                                                  //| itiveIterableMAs(Set(alpha, beta, gamma, delta),Set(5, 10, 1, 6, 9, 2, 7, 3
                                                  //| , 8, 4),Set(1.0, 0.5, 0.2, 0.125),Set(1.333333, 1.33333335, 1.6666666, 1.66
                                                  //| 66667),Set(2012-11-25T19:35:56.219+01:00, 2012-11-25T20:35:56.219+01:00, 20
                                                  //| 12-11-25T21:35:56.219+01:00, 2012-11-25T22:35:56.219+01:00)),Some(TestAddre
                                                  //| ss(Berlin,At the wall 89))))
  
  val backFromWarp = backFromWarpV.forceResult    //> backFromWarp  : almhirt.riftwarp.TestObjectA = TestObjectA([B@4bd27069,[B@6
                                                  //| 4d22462,PrimitiveTypes(I am Pete,true,127,-237823,-278234263,26587625768237
                                                  //| 6587365863876528756875682765252520577305007209857025728132213242,1.3672322,
                                                  //| 1.3672322350005,23761247614876823746.23846749182408,2012-11-25T18:35:56.155
                                                  //| +01:00,fb2f5249-86f5-4c27-950c-f0eaa388bb03),PrimitiveListMAs(List(alpha, b
                                                  //| eta, gamma, delta),List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),List(1.0, 0.5, 0.2, 
                                                  //| 0.125),List(1.333333, 1.33333335, 1.6666666, 1.6666667),List(2012-11-25T19:
                                                  //| 35:56.202+01:00, 2012-11-25T20:35:56.203+01:00, 2012-11-25T21:35:56.203+01:
                                                  //| 00, 2012-11-25T22:35:56.203+01:00)),PrimitiveVectorMAs(Vector(alpha, beta, 
                                                  //| gamma, delta),Vector(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),Vector(1.0, 0.5, 0.2, 0
                                                  //| .125),Vector(1.333333, 1.33333335, 1.6666666, 1.6666667),Vector(2012-11-25T
                                                  //| 19:35:56.204+01:00, 2012-11-25T20:35:56.204+01:00, 2012-11-25T21:35:56.204+
                                                  //| 01:00, 2012-11-25T22:35:56.204+01:00)),PrimitiveSetMAs(Set(alpha, beta, gam
                                                  //| ma, delta),Set(5, 10, 1, 6, 9, 2, 7, 3, 8, 4),Set(1.0, 0.5, 0.2, 0.125),Set
                                                  //| (1.333333, 1.33333335, 1.6666666, 1.6666667),Set(2012-11-25T19:35:56.219+01
                                                  //| :00, 2012-11-25T20:35:56.219+01:00, 2012-11-25T21:35:56.219+01:00, 2012-11-
                                                  //| 25T22:35:56.219+01:00)),PrimitiveIterableMAs(Set(alpha, beta, gamma, delta)
                                                  //| ,Set(5, 10, 1, 6, 9, 2, 7, 3, 8, 4),Set(1.0, 0.5, 0.2, 0.125),Set(1.333333,
                                                  //|  1.33333335, 1.6666666, 1.6666667),Set(2012-11-25T19:35:56.219+01:00, 2012-
                                                  //| 11-25T20:35:56.219+01:00, 2012-11-25T21:35:56.219+01:00, 2012-11-25T22:35:5
                                                  //| 6.219+01:00)),Some(TestAddress(Berlin,At the wall 89)))
  
  testObject == backFromWarp                      //> res0: Boolean = false
  
  
  
  riftWarp.prepareForWarp[RiftMap, DimensionRawMap](testObject).bind(warpStream =>
    riftWarp.receiveFromWarp[DimensionRawMap, RiftMap, TestObjectA](warpStream)).map(rearrived =>
      rearrived == testObject)                    //> res1: scalaz.Validation[almhirt.common.Problem,Boolean] = Success(true)


  riftWarp.prepareForWarp[RiftJson, DimensionCord](testObject).bind(warpStream =>
    riftWarp.receiveFromWarp[DimensionCord, RiftJson, TestObjectA](warpStream)).map(rearrived =>
      rearrived == testObject)                    //> res2: scalaz.Validation[almhirt.common.Problem,Boolean] = Success(false)
      
}