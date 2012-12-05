package riftwarp.worksheets

import scalaz.syntax.validation._
import almhirt.syntax.almvalidation._
import riftwarp._
import riftwarp.impl._

object Worksheet {
  val riftWarp = RiftWarp.unsafeWithDefaults      //> riftWarp  : riftwarp.RiftWarp = riftwarp.RiftWarp$$anon$1@449b9ad
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


  val testObject = TestObjectA.pete               //> testObject  : riftwarp.TestObjectA = TestObjectA([B@3b3b23d6,[B@27add4fb,Pr
                                                  //| imitiveTypes(I am Pete,true,127,-237823,-278234263,265876257682376587365863
                                                  //| 876528756875682765252520577305007209857025728132213242,1.3672322,1.36723223
                                                  //| 50005,23761247614876823746.23846749182408,2012-12-05T17:57:26.255+01:00,09d
                                                  //| 6e7d3-b64c-4200-be7e-5a90917cf47a),PrimitiveListMAs(List(alpha, beta, gamma
                                                  //| , delta),List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),List(1.0, 0.5, 0.2, 0.125),Lis
                                                  //| t(1.333333, 1.33333335, 1.6666666, 1.6666667),List(2012-12-05T18:57:26.411+
                                                  //| 01:00, 2012-12-05T19:57:26.411+01:00, 2012-12-05T20:57:26.411+01:00, 2012-1
                                                  //| 2-05T21:57:26.411+01:00)),PrimitiveVectorMAs(Vector(alpha, beta, gamma, del
                                                  //| ta),Vector(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),Vector(1.0, 0.5, 0.2, 0.125),Vect
                                                  //| or(1.333333, 1.33333335, 1.6666666, 1.6666667),Vector(2012-12-05T18:57:26.4
                                                  //| 11+01:00, 2012-12-05T19:57:26.411+01:00, 2012-12-05T20:57:26.411+01:00, 201
                                                  //| 2-12-05T21:57:26.411+01:00)),Some(PrimitiveSetMAs(Set(alpha, beta, gamma, d
                                                  //| elta),Set(5, 10, 1, 6, 9, 2, 7, 3, 8, 4),Set(1.0, 0.5, 0.2, 0.125),Set(1.33
                                                  //| 3333, 1.33333335, 1.6666666, 1.6666667),None)),PrimitiveIterableMAs(List(al
                                                  //| pha, beta, gamma, delta),List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),List(1.0, 0.5,
                                                  //|  0.2, 0.125),List(1.333333, 1.33333335, 1.6666666, 1.6666667),List(2012-12-
                                                  //| 05T18:57:26.442+01:00, 2012-12-05T19:57:26.442+01:00, 2012-12-05T20:57:26.4
                                                  //| 42+01:00, 2012-12-05T21:57:26.442+01:00)),ComplexMAs(List(TestAddress(Hambu
                                                  //| rg,Am Hafen), TestAddress(New York,Broadway), TestAddress(Los Angeles ,Suns
                                                  //| et Boulevard)),Vector(TestAddress(Hamburg,Am Hafen), TestAddress(New York,B
                                                  //| roadway), TestAddress(Los Angeles ,Sunset Boulevard)),Set(TestAddress(Hambu
                                                  //| rg,Am Hafen), TestAddress(New York,Broadway), TestAddress(Los Angeles ,Suns
                                                  //| et Boulevard)),List(true, hello, 1, 2, 3.0, 3.0, TestAddress(Somewhere,here
                                                  //| ))),Some(TestAddress(Berlin,At the wall 89)))
  val warpStreamV = riftWarp.prepareForWarp[DimensionCord](RiftJson())(testObject)
                                                  //> warpStreamV  : almhirt.common.package.AlmValidation[riftwarp.DimensionCord]
                                                  //|  = Success(DimensionCord({"riftwarptd":"riftwarp.TestObjectA","arrayByte":[
                                                  //| 126,-123,12,-45,-128],"blob":"AAAAAAAGhQzTgHAAAAA=","primitiveTypes":{"rift
                                                  //| warptd":"riftwarp.PrimitiveTypes","str":"I am Pete","bool":true,"byte":127,
                                                  //| "int":-237823,"long":-278234263,"bigInt":"265876257682376587365863876528756
                                                  //| 875682765252520577305007209857025728132213242","float":1.3672322034835815,"
                                                  //| double":1.3672322350005,"bigDec":"23761247614876823746.23846749182408","dat
                                                  //| eTime":"2012-12-05T17:57:26.255+01:00","uuid":"09d6e7d3-b64c-4200-be7e-5a90
                                                  //| 917cf47a"},"primitiveListMAs":{"riftwarptd":"riftwarp.PrimitiveListMAs","li
                                                  //| stString":["alpha","beta","gamma","delta"],"listInt":[1,2,3,4,5,6,7,8,9,10]
                                                  //| ,"listDouble":[1.0,0.5,0.2,0.125],"listBigDecimal":["1.333333","1.33333335"
                                                  //| ,"1.6666666","1.6666667"],"listDateTime":["2012-12-05T18:57:26.411+01:00","
                                                  //| 2012-12-05T19:57:26.411+01:00","2012-12-05T20:57:26.411+01:00","2012-12-05T
                                                  //| 21:57:26.411+01:00"]},"primitiveVectorMAs":{"riftwarptd":"riftwarp.Primitiv
                                                  //| eVectorMAs","vectorString":["alpha","beta","gamma","delta"],"vectorInt":[1,
                                                  //| 2,3,4,5,6,7,8,9,10],"vectorDouble":[1.0,0.5,0.2,0.125],"vectorBigDecimal":[
                                                  //| "1.333333","1.33333335","1.6666666","1.6666667"],"vectorDateTime":["2012-12
                                                  //| -05T18:57:26.411+01:00","2012-12-05T19:57:26.411+01:00","2012-12-05T20:57:2
                                                  //| 6.411+01:00","2012-12-05T21:57:26.411+01:00"]},"primitiveSetMAs":{"riftwarp
                                                  //| td":"riftwarp.PrimitiveSetMAs","setString":["alpha","beta","gamma","delta"]
                                                  //| ,"setInt":[1,9,3,5,10,8,4,6,7,2],"setDouble":[1.0,0.5,0.2,0.125],"setBigDec
                                                  //| imal":["1.333333","1.33333335","1.6666666","1.6666667"],"setDateTime":null}
                                                  //| ,"primitiveIterableMAs":{"riftwarptd":"riftwarp.PrimitiveIterableMAs","iter
                                                  //| ableString":["alpha","beta","gamma","delta"],"iterableInt":[1,2,3,4,5,6,7,8
                                                  //| ,9,10],"iterableDouble":[1.0,0.5,0.2,0.125],"iterableBigDecimal":["1.333333
                                                  //| ","1.33333335","1.6666666","1.6666667"],"iterableDateTime":["2012-12-05T18:
                                                  //| 57:26.442+01:00","2012-12-05T19:57:26.442+01:00","2012-12-05T20:57:26.442+0
                                                  //| 1:00","2012-12-05T21:57:26.442+01:00"]},"complexMAs":{"riftwarptd":"riftwar
                                                  //| p.ComplexMAs","addresses1":[{"riftwarptd":"riftwarp.TestAddress","city":"Ha
                                                  //| mburg","street":"Am Hafen"},{"riftwarptd":"riftwarp.TestAddress","city":"Ne
                                                  //| w York","street":"Broadway"},{"riftwarptd":"riftwarp.TestAddress","city":"L
                                                  //| os Angeles ","street":"Sunset Boulevard"}],"addresses2":[{"riftwarptd":"rif
                                                  //| twarp.TestAddress","city":"Hamburg","street":"Am Hafen"},{"riftwarptd":"rif
                                                  //| twarp.TestAddress","city":"New York","street":"Broadway"},{"riftwarptd":"ri
                                                  //| ftwarp.TestAddress","city":"Los Angeles ","street":"Sunset Boulevard"}],"ad
                                                  //| dresses3":[{"riftwarptd":"riftwarp.TestAddress","city":"Hamburg","street":"
                                                  //| Am Hafen"},{"riftwarptd":"riftwarp.TestAddress","city":"New York","street":
                                                  //| "Broadway"},{"riftwarptd":"riftwarp.TestAddress","city":"Los Angeles ","str
                                                  //| eet":"Sunset Boulevard"}],"anything":[true,"hello",1,2,3.0,3.0,{"riftwarptd
                                                  //| ":"riftwarp.TestAddress","city":"Somewhere","street":"here"}]},"addressOpt"
                                                  //| :{"riftwarptd":"riftwarp.TestAddress","city":"Berlin","street":"At the wall
                                                  //|  89"}}))
   
 
   
  val backFromWarpV = riftWarp.receiveFromWarp[DimensionCord, TestObjectA](RiftJson())(warpStreamV.forceResult)
                                                  //> backFromWarpV  : almhirt.common.package.AlmValidation[riftwarp.TestObjectA]
                                                  //|  = Success(TestObjectA([B@68d87ea,[B@35d61702,PrimitiveTypes(I am Pete,true
                                                  //| ,127,-237823,-278234263,265876257682376587365863876528756875682765252520577
                                                  //| 305007209857025728132213242,1.3672322,1.3672322350005,23761247614876823746.
                                                  //| 23846749182408,2012-12-05T17:57:26.255+01:00,09d6e7d3-b64c-4200-be7e-5a9091
                                                  //| 7cf47a),PrimitiveListMAs(List(alpha, beta, gamma, delta),List(1, 2, 3, 4, 5
                                                  //| , 6, 7, 8, 9, 10),List(1.0, 0.5, 0.2, 0.125),List(1.333333, 1.33333335, 1.6
                                                  //| 666666, 1.6666667),List(2012-12-05T18:57:26.411+01:00, 2012-12-05T19:57:26.
                                                  //| 411+01:00, 2012-12-05T20:57:26.411+01:00, 2012-12-05T21:57:26.411+01:00)),P
                                                  //| rimitiveVectorMAs(Vector(alpha, beta, gamma, delta),Vector(1, 2, 3, 4, 5, 6
                                                  //| , 7, 8, 9, 10),Vector(1.0, 0.5, 0.2, 0.125),Vector(1.333333, 1.33333335, 1.
                                                  //| 6666666, 1.6666667),Vector(2012-12-05T18:57:26.411+01:00, 2012-12-05T19:57:
                                                  //| 26.411+01:00, 2012-12-05T20:57:26.411+01:00, 2012-12-05T21:57:26.411+01:00)
                                                  //| ),Some(PrimitiveSetMAs(Set(alpha, beta, gamma, delta),Set(5, 10, 1, 6, 9, 2
                                                  //| , 7, 3, 8, 4),Set(1.0, 0.5, 0.2, 0.125),Set(1.333333, 1.33333335, 1.6666666
                                                  //| , 1.6666667),None)),PrimitiveIterableMAs(Set(alpha, beta, gamma, delta),Set
                                                  //| (5, 10, 1, 6, 9, 2, 7, 3, 8, 4),Set(1.0, 0.5, 0.2, 0.125),Set(1.333333, 1.3
                                                  //| 3333335, 1.6666666, 1.6666667),Set(2012-12-05T18:57:26.442+01:00, 2012-12-0
                                                  //| 5T19:57:26.442+01:00, 2012-12-05T20:57:26.442+01:00, 2012-12-05T21:57:26.44
                                                  //| 2+01:00)),ComplexMAs(List(TestAddress(Hamburg,Am Hafen), TestAddress(New Yo
                                                  //| rk,Broadway), TestAddress(Los Angeles ,Sunset Boulevard)),Vector(TestAddres
                                                  //| s(Hamburg,Am Hafen), TestAddress(New York,Broadway), TestAddress(Los Angele
                                                  //| s ,Sunset Boulevard)),Set(TestAddress(Hamburg,Am Hafen), TestAddress(New Yo
                                                  //| rk,Broadway), TestAddress(Los Angeles ,Sunset Boulevard)),List(true, hello,
                                                  //|  1.0, 2.0, 3.0, 3.0, Map(riftwarptd -> riftwarp.TestAddress, city -> Somewh
                                                  //| ere, street -> here))),Some(TestAddress(Berlin,At the wall 89))))
  

  val backFromWarp = backFromWarpV.forceResult    //> backFromWarp  : riftwarp.TestObjectA = TestObjectA([B@68d87ea,[B@35d61702,P
                                                  //| rimitiveTypes(I am Pete,true,127,-237823,-278234263,26587625768237658736586
                                                  //| 3876528756875682765252520577305007209857025728132213242,1.3672322,1.3672322
                                                  //| 350005,23761247614876823746.23846749182408,2012-12-05T17:57:26.255+01:00,09
                                                  //| d6e7d3-b64c-4200-be7e-5a90917cf47a),PrimitiveListMAs(List(alpha, beta, gamm
                                                  //| a, delta),List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),List(1.0, 0.5, 0.2, 0.125),Li
                                                  //| st(1.333333, 1.33333335, 1.6666666, 1.6666667),List(2012-12-05T18:57:26.411
                                                  //| +01:00, 2012-12-05T19:57:26.411+01:00, 2012-12-05T20:57:26.411+01:00, 2012-
                                                  //| 12-05T21:57:26.411+01:00)),PrimitiveVectorMAs(Vector(alpha, beta, gamma, de
                                                  //| lta),Vector(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),Vector(1.0, 0.5, 0.2, 0.125),Vec
                                                  //| tor(1.333333, 1.33333335, 1.6666666, 1.6666667),Vector(2012-12-05T18:57:26.
                                                  //| 411+01:00, 2012-12-05T19:57:26.411+01:00, 2012-12-05T20:57:26.411+01:00, 20
                                                  //| 12-12-05T21:57:26.411+01:00)),Some(PrimitiveSetMAs(Set(alpha, beta, gamma, 
                                                  //| delta),Set(5, 10, 1, 6, 9, 2, 7, 3, 8, 4),Set(1.0, 0.5, 0.2, 0.125),Set(1.3
                                                  //| 33333, 1.33333335, 1.6666666, 1.6666667),None)),PrimitiveIterableMAs(Set(al
                                                  //| pha, beta, gamma, delta),Set(5, 10, 1, 6, 9, 2, 7, 3, 8, 4),Set(1.0, 0.5, 0
                                                  //| .2, 0.125),Set(1.333333, 1.33333335, 1.6666666, 1.6666667),Set(2012-12-05T1
                                                  //| 8:57:26.442+01:00, 2012-12-05T19:57:26.442+01:00, 2012-12-05T20:57:26.442+0
                                                  //| 1:00, 2012-12-05T21:57:26.442+01:00)),ComplexMAs(List(TestAddress(Hamburg,A
                                                  //| m Hafen), TestAddress(New York,Broadway), TestAddress(Los Angeles ,Sunset B
                                                  //| oulevard)),Vector(TestAddress(Hamburg,Am Hafen), TestAddress(New York,Broad
                                                  //| way), TestAddress(Los Angeles ,Sunset Boulevard)),Set(TestAddress(Hamburg,A
                                                  //| m Hafen), TestAddress(New York,Broadway), TestAddress(Los Angeles ,Sunset B
                                                  //| oulevard)),List(true, hello, 1.0, 2.0, 3.0, 3.0, Map(riftwarptd -> riftwarp
                                                  //| .TestAddress, city -> Somewhere, street -> here))),Some(TestAddress(Berlin,
                                                  //| At the wall 89)))

  testObject == backFromWarp                      //> res0: Boolean = false



  riftWarp.prepareForWarp[DimensionRawMap](RiftMap())(testObject).bind(warpStream =>
    riftWarp.receiveFromWarp[DimensionRawMap, TestObjectA](RiftMap())(warpStream)).map(rearrived =>
    rearrived == testObject)                      //> res1: scalaz.Validation[almhirt.common.Problem,Boolean] = Success(true)

  riftWarp.prepareForWarp[DimensionCord](RiftJson())(testObject).bind(warpStream =>
    riftWarp.receiveFromWarp[DimensionCord, TestObjectA](RiftJson())(warpStream)).map(rearrived =>
    rearrived == testObject)                      //> res2: scalaz.Validation[almhirt.common.Problem,Boolean] = Success(false)

}