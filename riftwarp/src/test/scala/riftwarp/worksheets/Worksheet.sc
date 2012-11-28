package riftwarp.worksheets

import scalaz.syntax.validation._
import almhirt.syntax.almvalidation._
import riftwarp._
import riftwarp.impl._
import riftwarp.impl.rematerializers.simplema._

object Worksheet {
  val riftWarp = RiftWarp.unsafeWithDefaults      //> riftWarp  : riftwarp.RiftWarp = riftwarp.RiftWarp$$anon$97@187293dd
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


  val testObject = TestObjectA.pete               //> testObject  : riftwarp.TestObjectA = TestObjectA([B@51a7389f,[B@3976789a,Pr
                                                  //| imitiveTypes(I am Pete,true,127,-237823,-278234263,265876257682376587365863
                                                  //| 876528756875682765252520577305007209857025728132213242,1.3672322,1.36723223
                                                  //| 50005,23761247614876823746.23846749182408,2012-11-28T13:27:14.656+01:00,2ea
                                                  //| a2608-89f2-48dc-b9c8-eef515be0888),PrimitiveListMAs(List(alpha, beta, gamma
                                                  //| , delta),List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),List(1.0, 0.5, 0.2, 0.125),Lis
                                                  //| t(1.333333, 1.33333335, 1.6666666, 1.6666667),List(2012-11-28T14:27:14.749+
                                                  //| 01:00, 2012-11-28T15:27:14.749+01:00, 2012-11-28T16:27:14.749+01:00, 2012-1
                                                  //| 1-28T17:27:14.749+01:00)),PrimitiveVectorMAs(Vector(alpha, beta, gamma, del
                                                  //| ta),Vector(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),Vector(1.0, 0.5, 0.2, 0.125),Vect
                                                  //| or(1.333333, 1.33333335, 1.6666666, 1.6666667),Vector(2012-11-28T14:27:14.7
                                                  //| 49+01:00, 2012-11-28T15:27:14.749+01:00, 2012-11-28T16:27:14.749+01:00, 201
                                                  //| 2-11-28T17:27:14.749+01:00)),PrimitiveSetMAs(Set(alpha, beta, gamma, delta)
                                                  //| ,Set(5, 10, 1, 6, 9, 2, 7, 3, 8, 4),Set(1.0, 0.5, 0.2, 0.125),Set(1.333333,
                                                  //|  1.33333335, 1.6666666, 1.6666667),Set(2012-11-28T14:27:14.765+01:00, 2012-
                                                  //| 11-28T15:27:14.765+01:00, 2012-11-28T16:27:14.765+01:00, 2012-11-28T17:27:1
                                                  //| 4.765+01:00)),PrimitiveIterableMAs(List(alpha, beta, gamma, delta),List(1, 
                                                  //| 2, 3, 4, 5, 6, 7, 8, 9, 10),List(1.0, 0.5, 0.2, 0.125),List(1.333333, 1.333
                                                  //| 33335, 1.6666666, 1.6666667),List(2012-11-28T14:27:14.765+01:00, 2012-11-28
                                                  //| T15:27:14.765+01:00, 2012-11-28T16:27:14.765+01:00, 2012-11-28T17:27:14.765
                                                  //| +01:00)),ComplexMAs(List(TestAddress(Hamburg,Am Hafen), TestAddress(New Yor
                                                  //| k,Broadway), TestAddress(Los Angeles ,Sunset Boulevard)),Vector(TestAddress
                                                  //| (Hamburg,Am Hafen), TestAddress(New York,Broadway), TestAddress(Los Angeles
                                                  //|  ,Sunset Boulevard)),Set(TestAddress(Hamburg,Am Hafen), TestAddress(New Yor
                                                  //| k,Broadway), TestAddress(Los Angeles ,Sunset Boulevard)),List(true, hello, 
                                                  //| 1, 2, 3.0, 3.0, TestAddress(Somewhere,here))),Some(TestAddress(Berlin,At th
                                                  //| e wall 89)))
  val warpStreamV = riftWarp.prepareForWarp[DimensionCord](RiftJson())(testObject)
                                                  //> warpStreamV  : almhirt.common.package.AlmValidation[riftwarp.DimensionCord]
                                                  //|  = Success(DimensionCord({"riftwarptd":"riftwarp.TestObjectA","arrayByte":[
                                                  //| 126,-123,12,-45,-128],"blob":"AAAAAAAGhQzTgHAAAAA=","primitiveTypes":{"rift
                                                  //| warptd":"riftwarp.PrimitiveTypes","str":"I am Pete","bool":true,"byte":127,
                                                  //| "int":-237823,"long":-278234263,"bigInt":"265876257682376587365863876528756
                                                  //| 875682765252520577305007209857025728132213242","float":1.3672322034835815,"
                                                  //| double":1.3672322350005,"bigDec":"23761247614876823746.23846749182408","dat
                                                  //| eTime":"2012-11-28T13:27:14.656+01:00","uuid":"2eaa2608-89f2-48dc-b9c8-eef5
                                                  //| 15be0888"},"primitiveListMAs":{"riftwarptd":"riftwarp.PrimitiveListMAs","li
                                                  //| stString":["alpha","beta","gamma","delta"],"listInt":[1,2,3,4,5,6,7,8,9,10]
                                                  //| ,"listDouble":[1.0,0.5,0.2,0.125],"listBigDecimal":["1.333333","1.33333335"
                                                  //| ,"1.6666666","1.6666667"],"listDateTime":["2012-11-28T14:27:14.749+01:00","
                                                  //| 2012-11-28T15:27:14.749+01:00","2012-11-28T16:27:14.749+01:00","2012-11-28T
                                                  //| 17:27:14.749+01:00"]},"primitiveVectorMAs":{"riftwarptd":"riftwarp.Primitiv
                                                  //| eVectorMAs","vectorString":["alpha","beta","gamma","delta"],"vectorInt":[1,
                                                  //| 2,3,4,5,6,7,8,9,10],"vectorDouble":[1.0,0.5,0.2,0.125],"vectorBigDecimal":[
                                                  //| "1.333333","1.33333335","1.6666666","1.6666667"],"vectorDateTime":["2012-11
                                                  //| -28T14:27:14.749+01:00","2012-11-28T15:27:14.749+01:00","2012-11-28T16:27:1
                                                  //| 4.749+01:00","2012-11-28T17:27:14.749+01:00"]},"primitiveSetMAs":{"riftwarp
                                                  //| td":"riftwarp.PrimitiveSetMAs","setString":["alpha","beta","gamma","delta"]
                                                  //| ,"setInt":[1,3,6,4,2,7,5,10,8,9],"setDouble":[1.0,0.5,0.2,0.125],"setBigDec
                                                  //| imal":["1.333333","1.33333335","1.6666666","1.6666667"],"setDateTime":["201
                                                  //| 2-11-28T14:27:14.765+01:00","2012-11-28T15:27:14.765+01:00","2012-11-28T16:
                                                  //| 27:14.765+01:00","2012-11-28T17:27:14.765+01:00"]},"primitiveIterableMAs":{
                                                  //| "riftwarptd":"riftwarp.PrimitiveIterableMAs","iterableString":["alpha","bet
                                                  //| a","gamma","delta"],"iterableInt":[1,2,3,4,5,6,7,8,9,10],"iterableDouble":[
                                                  //| 1.0,0.5,0.2,0.125],"iterableBigDecimal":["1.333333","1.33333335","1.6666666
                                                  //| ","1.6666667"],"iterableDateTime":["2012-11-28T14:27:14.765+01:00","2012-11
                                                  //| -28T15:27:14.765+01:00","2012-11-28T16:27:14.765+01:00","2012-11-28T17:27:1
                                                  //| 4.765+01:00"]},"complexMAs":{"riftwarptd":"riftwarp.ComplexMAs","addresses1
                                                  //| ":[{"riftwarptd":"riftwarp.TestAddress","city":"Hamburg","street":"Am Hafen
                                                  //| "},{"riftwarptd":"riftwarp.TestAddress","city":"New York","street":"Broadwa
                                                  //| y"},{"riftwarptd":"riftwarp.TestAddress","city":"Los Angeles ","street":"Su
                                                  //| nset Boulevard"}],"addresses2":[{"riftwarptd":"riftwarp.TestAddress","city"
                                                  //| :"Hamburg","street":"Am Hafen"},{"riftwarptd":"riftwarp.TestAddress","city"
                                                  //| :"New York","street":"Broadway"},{"riftwarptd":"riftwarp.TestAddress","city
                                                  //| ":"Los Angeles ","street":"Sunset Boulevard"}],"addresses3":[{"riftwarptd":
                                                  //| "riftwarp.TestAddress","city":"Hamburg","street":"Am Hafen"},{"riftwarptd":
                                                  //| "riftwarp.TestAddress","city":"New York","street":"Broadway"},{"riftwarptd"
                                                  //| :"riftwarp.TestAddress","city":"Los Angeles ","street":"Sunset Boulevard"}]
                                                  //| ,"anything":[true,"hello",1,2,3.0,3.0,{"riftwarptd":"riftwarp.TestAddress",
                                                  //| "city":"Somewhere","street":"here"}]},"addressOpt":{"riftwarptd":"riftwarp.
                                                  //| TestAddress","city":"Berlin","street":"At the wall 89"}}))

  val backFromWarpV = riftWarp.receiveFromWarp[DimensionCord, TestObjectA](RiftJson())(warpStreamV.forceResult)
                                                  //> backFromWarpV  : almhirt.common.package.AlmValidation[riftwarp.TestObjectA]
                                                  //|  = Success(TestObjectA([B@63f8b70e,[B@5dba2b68,PrimitiveTypes(I am Pete,tru
                                                  //| e,127,-237823,-278234263,26587625768237658736586387652875687568276525252057
                                                  //| 7305007209857025728132213242,1.3672322,1.3672322350005,23761247614876823746
                                                  //| .23846749182408,2012-11-28T13:27:14.656+01:00,2eaa2608-89f2-48dc-b9c8-eef51
                                                  //| 5be0888),PrimitiveListMAs(List(alpha, beta, gamma, delta),List(1, 2, 3, 4, 
                                                  //| 5, 6, 7, 8, 9, 10),List(1.0, 0.5, 0.2, 0.125),List(1.333333, 1.33333335, 1.
                                                  //| 6666666, 1.6666667),List(2012-11-28T14:27:14.749+01:00, 2012-11-28T15:27:14
                                                  //| .749+01:00, 2012-11-28T16:27:14.749+01:00, 2012-11-28T17:27:14.749+01:00)),
                                                  //| PrimitiveVectorMAs(Vector(alpha, beta, gamma, delta),Vector(1, 2, 3, 4, 5, 
                                                  //| 6, 7, 8, 9, 10),Vector(1.0, 0.5, 0.2, 0.125),Vector(1.333333, 1.33333335, 1
                                                  //| .6666666, 1.6666667),Vector(2012-11-28T14:27:14.749+01:00, 2012-11-28T15:27
                                                  //| :14.749+01:00, 2012-11-28T16:27:14.749+01:00, 2012-11-28T17:27:14.749+01:00
                                                  //| )),PrimitiveSetMAs(Set(alpha, beta, gamma, delta),Set(5, 10, 1, 6, 9, 2, 7,
                                                  //|  3, 8, 4),Set(1.0, 0.5, 0.2, 0.125),Set(1.333333, 1.33333335, 1.6666666, 1.
                                                  //| 6666667),Set(2012-11-28T14:27:14.765+01:00, 2012-11-28T15:27:14.765+01:00, 
                                                  //| 2012-11-28T16:27:14.765+01:00, 2012-11-28T17:27:14.765+01:00)),PrimitiveIte
                                                  //| rableMAs(Set(alpha, beta, gamma, delta),Set(5, 10, 1, 6, 9, 2, 7, 3, 8, 4),
                                                  //| Set(1.0, 0.5, 0.2, 0.125),Set(1.333333, 1.33333335, 1.6666666, 1.6666667),S
                                                  //| et(2012-11-28T14:27:14.765+01:00, 2012-11-28T15:27:14.765+01:00, 2012-11-28
                                                  //| T16:27:14.765+01:00, 2012-11-28T17:27:14.765+01:00)),ComplexMAs(List(),Vect
                                                  //| or(),Set(),List()),Some(TestAddress(Berlin,At the wall 89))))
  

  val backFromWarp = backFromWarpV.forceResult    //> backFromWarp  : riftwarp.TestObjectA = TestObjectA([B@63f8b70e,[B@5dba2b68,
                                                  //| PrimitiveTypes(I am Pete,true,127,-237823,-278234263,2658762576823765873658
                                                  //| 63876528756875682765252520577305007209857025728132213242,1.3672322,1.367232
                                                  //| 2350005,23761247614876823746.23846749182408,2012-11-28T13:27:14.656+01:00,2
                                                  //| eaa2608-89f2-48dc-b9c8-eef515be0888),PrimitiveListMAs(List(alpha, beta, gam
                                                  //| ma, delta),List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),List(1.0, 0.5, 0.2, 0.125),L
                                                  //| ist(1.333333, 1.33333335, 1.6666666, 1.6666667),List(2012-11-28T14:27:14.74
                                                  //| 9+01:00, 2012-11-28T15:27:14.749+01:00, 2012-11-28T16:27:14.749+01:00, 2012
                                                  //| -11-28T17:27:14.749+01:00)),PrimitiveVectorMAs(Vector(alpha, beta, gamma, d
                                                  //| elta),Vector(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),Vector(1.0, 0.5, 0.2, 0.125),Ve
                                                  //| ctor(1.333333, 1.33333335, 1.6666666, 1.6666667),Vector(2012-11-28T14:27:14
                                                  //| .749+01:00, 2012-11-28T15:27:14.749+01:00, 2012-11-28T16:27:14.749+01:00, 2
                                                  //| 012-11-28T17:27:14.749+01:00)),PrimitiveSetMAs(Set(alpha, beta, gamma, delt
                                                  //| a),Set(5, 10, 1, 6, 9, 2, 7, 3, 8, 4),Set(1.0, 0.5, 0.2, 0.125),Set(1.33333
                                                  //| 3, 1.33333335, 1.6666666, 1.6666667),Set(2012-11-28T14:27:14.765+01:00, 201
                                                  //| 2-11-28T15:27:14.765+01:00, 2012-11-28T16:27:14.765+01:00, 2012-11-28T17:27
                                                  //| :14.765+01:00)),PrimitiveIterableMAs(Set(alpha, beta, gamma, delta),Set(5, 
                                                  //| 10, 1, 6, 9, 2, 7, 3, 8, 4),Set(1.0, 0.5, 0.2, 0.125),Set(1.333333, 1.33333
                                                  //| 335, 1.6666666, 1.6666667),Set(2012-11-28T14:27:14.765+01:00, 2012-11-28T15
                                                  //| :27:14.765+01:00, 2012-11-28T16:27:14.765+01:00, 2012-11-28T17:27:14.765+01
                                                  //| :00)),ComplexMAs(List(),Vector(),Set(),List()),Some(TestAddress(Berlin,At t
                                                  //| he wall 89)))

  testObject == backFromWarp                      //> res0: Boolean = false

  riftWarp.prepareForWarp[DimensionRawMap](RiftMap())(testObject).bind(warpStream =>
    riftWarp.receiveFromWarp[DimensionRawMap, TestObjectA](RiftMap())(warpStream)).map(rearrived =>
    rearrived == testObject)                      //> res1: scalaz.Validation[almhirt.common.Problem,Boolean] = Success(false)


  riftWarp.prepareForWarp[DimensionCord](RiftJson())(testObject).bind(warpStream =>
    riftWarp.receiveFromWarp[DimensionCord, TestObjectA](RiftJson())(warpStream)).map(rearrived =>
    rearrived == testObject)                      //> res2: scalaz.Validation[almhirt.common.Problem,Boolean] = Success(false)

}