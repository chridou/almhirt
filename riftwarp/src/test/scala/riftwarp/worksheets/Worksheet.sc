package riftwarp.worksheets

import scalaz.syntax.validation._
import almhirt.syntax.almvalidation._
import riftwarp._
import riftwarp.impl._
import riftwarp.impl.rematerializers.simplema._

object Worksheet {
  val riftWarp = RiftWarp.unsafeWithDefaults      //> riftWarp  : riftwarp.RiftWarp = riftwarp.RiftWarp$$anon$97@6cc06bf7
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

  val testObject = TestObjectA.pete               //> testObject  : riftwarp.TestObjectA = TestObjectA([B@74348aac,[B@257c5dce,Pr
                                                  //| imitiveTypes(I am Pete,true,127,-237823,-278234263,265876257682376587365863
                                                  //| 876528756875682765252520577305007209857025728132213242,1.3672322,1.36723223
                                                  //| 50005,23761247614876823746.23846749182408,2012-11-28T11:25:25.727+01:00,b59
                                                  //| 3dbc8-1b1e-4e36-922c-8c0943b23a3f),PrimitiveListMAs(List(alpha, beta, gamma
                                                  //| , delta),List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),List(1.0, 0.5, 0.2, 0.125),Lis
                                                  //| t(1.333333, 1.33333335, 1.6666666, 1.6666667),List(2012-11-28T12:25:25.821+
                                                  //| 01:00, 2012-11-28T13:25:25.821+01:00, 2012-11-28T14:25:25.821+01:00, 2012-1
                                                  //| 1-28T15:25:25.821+01:00)),PrimitiveVectorMAs(Vector(alpha, beta, gamma, del
                                                  //| ta),Vector(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),Vector(1.0, 0.5, 0.2, 0.125),Vect
                                                  //| or(1.333333, 1.33333335, 1.6666666, 1.6666667),Vector(2012-11-28T12:25:25.8
                                                  //| 21+01:00, 2012-11-28T13:25:25.821+01:00, 2012-11-28T14:25:25.821+01:00, 201
                                                  //| 2-11-28T15:25:25.821+01:00)),PrimitiveSetMAs(Set(alpha, beta, gamma, delta)
                                                  //| ,Set(5, 10, 1, 6, 9, 2, 7, 3, 8, 4),Set(1.0, 0.5, 0.2, 0.125),Set(1.333333,
                                                  //|  1.33333335, 1.6666666, 1.6666667),Set(2012-11-28T12:25:25.837+01:00, 2012-
                                                  //| 11-28T13:25:25.837+01:00, 2012-11-28T14:25:25.837+01:00, 2012-11-28T15:25:2
                                                  //| 5.837+01:00)),PrimitiveIterableMAs(List(alpha, beta, gamma, delta),List(1, 
                                                  //| 2, 3, 4, 5, 6, 7, 8, 9, 10),List(1.0, 0.5, 0.2, 0.125),List(1.333333, 1.333
                                                  //| 33335, 1.6666666, 1.6666667),List(2012-11-28T12:25:25.837+01:00, 2012-11-28
                                                  //| T13:25:25.837+01:00, 2012-11-28T14:25:25.837+01:00, 2012-11-28T15:25:25.837
                                                  //| +01:00)),ComplexMAs(List(TestAddress(Hamburg,Am Hafen), TestAddress(New Yor
                                                  //| k,Broadway), TestAddress(Los Angeles ,Sunset Boulevard)),Vector(TestAddress
                                                  //| (Hamburg,Am Hafen), TestAddress(New York,Broadway), TestAddress(Los Angeles
                                                  //|  ,Sunset Boulevard)),Set(TestAddress(Hamburg,Am Hafen), TestAddress(New Yor
                                                  //| k,Broadway), TestAddress(Los Angeles ,Sunset Boulevard))),Some(TestAddress(
                                                  //| Berlin,At the wall 89)))
  val warpStream = riftWarp.prepareForWarp[DimensionCord](RiftJson())(testObject).forceResult
                                                  //> warpStream  : riftwarp.DimensionCord = DimensionCord({"riftwarptd":"riftwar
                                                  //| p.TestObjectA","arrayByte":[126,-123,12,-45,-128],"blob":"AAAAAAAGhQzTgHAAA
                                                  //| AA=","primitiveTypes":{"riftwarptd":"riftwarp.PrimitiveTypes","str":"I am P
                                                  //| ete","bool":true,"byte":127,"int":-237823,"long":-278234263,"bigInt":"26587
                                                  //| 6257682376587365863876528756875682765252520577305007209857025728132213242",
                                                  //| "float":1.3672322034835815,"double":1.3672322350005,"bigDec":"2376124761487
                                                  //| 6823746.23846749182408","dateTime":"2012-11-28T11:25:25.727+01:00","uuid":"
                                                  //| b593dbc8-1b1e-4e36-922c-8c0943b23a3f"},"primitiveListMAs":{"riftwarptd":"ri
                                                  //| ftwarp.PrimitiveListMAs","listString":["alpha","beta","gamma","delta"],"lis
                                                  //| tInt":[1,2,3,4,5,6,7,8,9,10],"listDouble":[1.0,0.5,0.2,0.125],"listBigDecim
                                                  //| al":["1.333333","1.33333335","1.6666666","1.6666667"],"listDateTime":["2012
                                                  //| -11-28T12:25:25.821+01:00","2012-11-28T13:25:25.821+01:00","2012-11-28T14:2
                                                  //| 5:25.821+01:00","2012-11-28T15:25:25.821+01:00"]},"primitiveVectorMAs":{"ri
                                                  //| ftwarptd":"riftwarp.PrimitiveVectorMAs","vectorString":["alpha","beta","gam
                                                  //| ma","delta"],"vectorInt":[1,2,3,4,5,6,7,8,9,10],"vectorDouble":[1.0,0.5,0.2
                                                  //| ,0.125],"vectorBigDecimal":["1.333333","1.33333335","1.6666666","1.6666667"
                                                  //| ],"vectorDateTime":["2012-11-28T12:25:25.821+01:00","2012-11-28T13:25:25.82
                                                  //| 1+01:00","2012-11-28T14:25:25.821+01:00","2012-11-28T15:25:25.821+01:00"]},
                                                  //| "primitiveSetMAs":{"riftwarptd":"riftwarp.PrimitiveSetMAs","setString":["al
                                                  //| pha","beta","gamma","delta"],"setInt":[1,6,5,10,2,7,3,4,9,8],"setDouble":[1
                                                  //| .0,0.5,0.2,0.125],"setBigDecimal":["1.333333","1.33333335","1.6666666","1.6
                                                  //| 666667"],"setDateTime":["2012-11-28T12:25:25.837+01:00","2012-11-28T13:25:2
                                                  //| 5.837+01:00","2012-11-28T14:25:25.837+01:00","2012-11-28T15:25:25.837+01:00
                                                  //| "]},"primitiveIterableMAs":{"riftwarptd":"riftwarp.PrimitiveIterableMAs","i
                                                  //| terableString":["alpha","beta","gamma","delta"],"iterableInt":[1,2,3,4,5,6,
                                                  //| 7,8,9,10],"iterableDouble":[1.0,0.5,0.2,0.125],"iterableBigDecimal":["1.333
                                                  //| 333","1.33333335","1.6666666","1.6666667"],"iterableDateTime":["2012-11-28T
                                                  //| 12:25:25.837+01:00","2012-11-28T13:25:25.837+01:00","2012-11-28T14:25:25.83
                                                  //| 7+01:00","2012-11-28T15:25:25.837+01:00"]},"complexMAs":{"riftwarptd":"rift
                                                  //| warp.ComplexMAs","addresses1":[{"riftwarptd":"riftwarp.TestAddress","city":
                                                  //| "Hamburg","street":"Am Hafen"},{"riftwarptd":"riftwarp.TestAddress","city":
                                                  //| "New York","street":"Broadway"},{"riftwarptd":"riftwarp.TestAddress","city"
                                                  //| :"Los Angeles ","street":"Sunset Boulevard"}],"addresses2":[{"riftwarptd":"
                                                  //| riftwarp.TestAddress","city":"Hamburg","street":"Am Hafen"},{"riftwarptd":"
                                                  //| riftwarp.TestAddress","city":"New York","street":"Broadway"},{"riftwarptd":
                                                  //| "riftwarp.TestAddress","city":"Los Angeles ","street":"Sunset Boulevard"}],
                                                  //| "addresses3":[{"riftwarptd":"riftwarp.TestAddress","city":"Hamburg","street
                                                  //| ":"Am Hafen"},{"riftwarptd":"riftwarp.TestAddress","city":"New York","stree
                                                  //| t":"Broadway"},{"riftwarptd":"riftwarp.TestAddress","city":"Los Angeles ","
                                                  //| street":"Sunset Boulevard"}]},"addressOpt":{"riftwarptd":"riftwarp.TestAddr
                                                  //| ess","city":"Berlin","street":"At the wall 89"}})

  val backFromWarpV = riftWarp.receiveFromWarp[DimensionCord, TestObjectA](RiftJson())(warpStream)
                                                  //> backFromWarpV  : almhirt.common.package.AlmValidation[riftwarp.TestObjectA]
                                                  //|  = Success(TestObjectA([B@7671eb24,[B@3104c53c,PrimitiveTypes(I am Pete,tru
                                                  //| e,127,-237823,-278234263,26587625768237658736586387652875687568276525252057
                                                  //| 7305007209857025728132213242,1.3672322,1.3672322350005,23761247614876823746
                                                  //| .23846749182408,2012-11-28T11:25:25.727+01:00,b593dbc8-1b1e-4e36-922c-8c094
                                                  //| 3b23a3f),PrimitiveListMAs(List(alpha, beta, gamma, delta),List(1, 2, 3, 4, 
                                                  //| 5, 6, 7, 8, 9, 10),List(1.0, 0.5, 0.2, 0.125),List(1.333333, 1.33333335, 1.
                                                  //| 6666666, 1.6666667),List(2012-11-28T12:25:25.821+01:00, 2012-11-28T13:25:25
                                                  //| .821+01:00, 2012-11-28T14:25:25.821+01:00, 2012-11-28T15:25:25.821+01:00)),
                                                  //| PrimitiveVectorMAs(Vector(alpha, beta, gamma, delta),Vector(1, 2, 3, 4, 5, 
                                                  //| 6, 7, 8, 9, 10),Vector(1.0, 0.5, 0.2, 0.125),Vector(1.333333, 1.33333335, 1
                                                  //| .6666666, 1.6666667),Vector(2012-11-28T12:25:25.821+01:00, 2012-11-28T13:25
                                                  //| :25.821+01:00, 2012-11-28T14:25:25.821+01:00, 2012-11-28T15:25:25.821+01:00
                                                  //| )),PrimitiveSetMAs(Set(alpha, beta, gamma, delta),Set(5, 10, 1, 6, 9, 2, 7,
                                                  //|  3, 8, 4),Set(1.0, 0.5, 0.2, 0.125),Set(1.333333, 1.33333335, 1.6666666, 1.
                                                  //| 6666667),Set(2012-11-28T12:25:25.837+01:00, 2012-11-28T13:25:25.837+01:00, 
                                                  //| 2012-11-28T14:25:25.837+01:00, 2012-11-28T15:25:25.837+01:00)),PrimitiveIte
                                                  //| rableMAs(Set(alpha, beta, gamma, delta),Set(5, 10, 1, 6, 9, 2, 7, 3, 8, 4),
                                                  //| Set(1.0, 0.5, 0.2, 0.125),Set(1.333333, 1.33333335, 1.6666666, 1.6666667),S
                                                  //| et(2012-11-28T12:25:25.837+01:00, 2012-11-28T13:25:25.837+01:00, 2012-11-28
                                                  //| T14:25:25.837+01:00, 2012-11-28T15:25:25.837+01:00)),ComplexMAs(List(),Vect
                                                  //| or(),Set()),Some(TestAddress(Berlin,At the wall 89))))
  


  val backFromWarp = backFromWarpV.forceResult    //> backFromWarp  : riftwarp.TestObjectA = TestObjectA([B@7671eb24,[B@3104c53c,
                                                  //| PrimitiveTypes(I am Pete,true,127,-237823,-278234263,2658762576823765873658
                                                  //| 63876528756875682765252520577305007209857025728132213242,1.3672322,1.367232
                                                  //| 2350005,23761247614876823746.23846749182408,2012-11-28T11:25:25.727+01:00,b
                                                  //| 593dbc8-1b1e-4e36-922c-8c0943b23a3f),PrimitiveListMAs(List(alpha, beta, gam
                                                  //| ma, delta),List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),List(1.0, 0.5, 0.2, 0.125),L
                                                  //| ist(1.333333, 1.33333335, 1.6666666, 1.6666667),List(2012-11-28T12:25:25.82
                                                  //| 1+01:00, 2012-11-28T13:25:25.821+01:00, 2012-11-28T14:25:25.821+01:00, 2012
                                                  //| -11-28T15:25:25.821+01:00)),PrimitiveVectorMAs(Vector(alpha, beta, gamma, d
                                                  //| elta),Vector(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),Vector(1.0, 0.5, 0.2, 0.125),Ve
                                                  //| ctor(1.333333, 1.33333335, 1.6666666, 1.6666667),Vector(2012-11-28T12:25:25
                                                  //| .821+01:00, 2012-11-28T13:25:25.821+01:00, 2012-11-28T14:25:25.821+01:00, 2
                                                  //| 012-11-28T15:25:25.821+01:00)),PrimitiveSetMAs(Set(alpha, beta, gamma, delt
                                                  //| a),Set(5, 10, 1, 6, 9, 2, 7, 3, 8, 4),Set(1.0, 0.5, 0.2, 0.125),Set(1.33333
                                                  //| 3, 1.33333335, 1.6666666, 1.6666667),Set(2012-11-28T12:25:25.837+01:00, 201
                                                  //| 2-11-28T13:25:25.837+01:00, 2012-11-28T14:25:25.837+01:00, 2012-11-28T15:25
                                                  //| :25.837+01:00)),PrimitiveIterableMAs(Set(alpha, beta, gamma, delta),Set(5, 
                                                  //| 10, 1, 6, 9, 2, 7, 3, 8, 4),Set(1.0, 0.5, 0.2, 0.125),Set(1.333333, 1.33333
                                                  //| 335, 1.6666666, 1.6666667),Set(2012-11-28T12:25:25.837+01:00, 2012-11-28T13
                                                  //| :25:25.837+01:00, 2012-11-28T14:25:25.837+01:00, 2012-11-28T15:25:25.837+01
                                                  //| :00)),ComplexMAs(List(),Vector(),Set()),Some(TestAddress(Berlin,At the wall
                                                  //|  89)))

  testObject == backFromWarp                      //> res0: Boolean = false

  riftWarp.prepareForWarp[DimensionRawMap](RiftMap())(testObject).bind(warpStream =>
    riftWarp.receiveFromWarp[DimensionRawMap, TestObjectA](RiftMap())(warpStream)).map(rearrived =>
    rearrived == testObject)                      //> res1: scalaz.Validation[almhirt.common.Problem,Boolean] = Success(false)


  riftWarp.prepareForWarp[DimensionCord](RiftJson())(testObject).bind(warpStream =>
    riftWarp.receiveFromWarp[DimensionCord, TestObjectA](RiftJson())(warpStream)).map(rearrived =>
    rearrived == testObject)                      //> res2: scalaz.Validation[almhirt.common.Problem,Boolean] = Success(false)

}