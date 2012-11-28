package riftwarp.worksheets

import scalaz.syntax.validation._
import almhirt.syntax.almvalidation._
import riftwarp._
import riftwarp.impl._


object Worksheet {
  val riftWarp = RiftWarp.unsafeWithDefaults      //> riftWarp  : riftwarp.RiftWarp = riftwarp.RiftWarp$$anon$1@ed42d08
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

  val testObject = TestObjectA.pete               //> testObject  : riftwarp.TestObjectA = TestObjectA([B@5982bcde,[B@163dc0bb,Pr
                                                  //| imitiveTypes(I am Pete,true,127,-237823,-278234263,265876257682376587365863
                                                  //| 876528756875682765252520577305007209857025728132213242,1.3672322,1.36723223
                                                  //| 50005,23761247614876823746.23846749182408,2012-11-28T20:38:39.390+01:00,aae
                                                  //| a8f95-a8da-4957-a0d5-f15830774187),PrimitiveListMAs(List(alpha, beta, gamma
                                                  //| , delta),List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),List(1.0, 0.5, 0.2, 0.125),Lis
                                                  //| t(1.333333, 1.33333335, 1.6666666, 1.6666667),List(2012-11-28T21:38:39.433+
                                                  //| 01:00, 2012-11-28T22:38:39.434+01:00, 2012-11-28T23:38:39.434+01:00, 2012-1
                                                  //| 1-29T00:38:39.434+01:00)),PrimitiveVectorMAs(Vector(alpha, beta, gamma, del
                                                  //| ta),Vector(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),Vector(1.0, 0.5, 0.2, 0.125),Vect
                                                  //| or(1.333333, 1.33333335, 1.6666666, 1.6666667),Vector(2012-11-28T21:38:39.4
                                                  //| 35+01:00, 2012-11-28T22:38:39.435+01:00, 2012-11-28T23:38:39.435+01:00, 201
                                                  //| 2-11-29T00:38:39.435+01:00)),Some(PrimitiveSetMAs(Set(alpha, beta, gamma, d
                                                  //| elta),Set(5, 10, 1, 6, 9, 2, 7, 3, 8, 4),Set(1.0, 0.5, 0.2, 0.125),Set(1.33
                                                  //| 3333, 1.33333335, 1.6666666, 1.6666667),None)),PrimitiveIterableMAs(List(al
                                                  //| pha, beta, gamma, delta),List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),List(1.0, 0.5,
                                                  //|  0.2, 0.125),List(1.333333, 1.33333335, 1.6666666, 1.6666667),List(2012-11-
                                                  //| 28T21:38:39.451+01:00, 2012-11-28T22:38:39.451+01:00, 2012-11-28T23:38:39.4
                                                  //| 51+01:00, 2012-11-29T00:38:39.451+01:00)),ComplexMAs(List(TestAddress(Hambu
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
                                                  //| eTime":"2012-11-28T20:38:39.390+01:00","uuid":"aaea8f95-a8da-4957-a0d5-f158
                                                  //| 30774187"},"primitiveListMAs":{"riftwarptd":"riftwarp.PrimitiveListMAs","li
                                                  //| stString":["alpha","beta","gamma","delta"],"listInt":[1,2,3,4,5,6,7,8,9,10]
                                                  //| ,"listDouble":[1.0,0.5,0.2,0.125],"listBigDecimal":["1.333333","1.33333335"
                                                  //| ,"1.6666666","1.6666667"],"listDateTime":["2012-11-28T21:38:39.433+01:00","
                                                  //| 2012-11-28T22:38:39.434+01:00","2012-11-28T23:38:39.434+01:00","2012-11-29T
                                                  //| 00:38:39.434+01:00"]},"primitiveVectorMAs":{"riftwarptd":"riftwarp.Primitiv
                                                  //| eVectorMAs","vectorString":["alpha","beta","gamma","delta"],"vectorInt":[1,
                                                  //| 2,3,4,5,6,7,8,9,10],"vectorDouble":[1.0,0.5,0.2,0.125],"vectorBigDecimal":[
                                                  //| "1.333333","1.33333335","1.6666666","1.6666667"],"vectorDateTime":["2012-11
                                                  //| -28T21:38:39.435+01:00","2012-11-28T22:38:39.435+01:00","2012-11-28T23:38:3
                                                  //| 9.435+01:00","2012-11-29T00:38:39.435+01:00"]},"primitiveSetMAs":{"riftwarp
                                                  //| td":"riftwarp.PrimitiveSetMAs","setString":["alpha","beta","gamma","delta"]
                                                  //| ,"setInt":[8,7,4,3,2,1,10,6,9,5],"setDouble":[1.0,0.5,0.2,0.125],"setBigDec
                                                  //| imal":["1.333333","1.33333335","1.6666666","1.6666667"],"setDateTime":null}
                                                  //| ,"primitiveIterableMAs":{"riftwarptd":"riftwarp.PrimitiveIterableMAs","iter
                                                  //| ableString":["alpha","beta","gamma","delta"],"iterableInt":[1,2,3,4,5,6,7,8
                                                  //| ,9,10],"iterableDouble":[1.0,0.5,0.2,0.125],"iterableBigDecimal":["1.333333
                                                  //| ","1.33333335","1.6666666","1.6666667"],"iterableDateTime":["2012-11-28T21:
                                                  //| 38:39.451+01:00","2012-11-28T22:38:39.451+01:00","2012-11-28T23:38:39.451+0
                                                  //| 1:00","2012-11-29T00:38:39.451+01:00"]},"complexMAs":{"riftwarptd":"riftwar
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
                                                  //|  = Success(TestObjectA([B@33c13d0b,[B@538eb7b8,PrimitiveTypes(I am Pete,tru
                                                  //| e,127,-237823,-278234263,26587625768237658736586387652875687568276525252057
                                                  //| 7305007209857025728132213242,1.3672322,1.3672322350005,23761247614876823746
                                                  //| .23846749182408,2012-11-28T20:38:39.390+01:00,aaea8f95-a8da-4957-a0d5-f1583
                                                  //| 0774187),PrimitiveListMAs(List(alpha, beta, gamma, delta),List(1, 2, 3, 4, 
                                                  //| 5, 6, 7, 8, 9, 10),List(1.0, 0.5, 0.2, 0.125),List(1.333333, 1.33333335, 1.
                                                  //| 6666666, 1.6666667),List(2012-11-28T21:38:39.433+01:00, 2012-11-28T22:38:39
                                                  //| .434+01:00, 2012-11-28T23:38:39.434+01:00, 2012-11-29T00:38:39.434+01:00)),
                                                  //| PrimitiveVectorMAs(Vector(alpha, beta, gamma, delta),Vector(1, 2, 3, 4, 5, 
                                                  //| 6, 7, 8, 9, 10),Vector(1.0, 0.5, 0.2, 0.125),Vector(1.333333, 1.33333335, 1
                                                  //| .6666666, 1.6666667),Vector(2012-11-28T21:38:39.435+01:00, 2012-11-28T22:38
                                                  //| :39.435+01:00, 2012-11-28T23:38:39.435+01:00, 2012-11-29T00:38:39.435+01:00
                                                  //| )),Some(PrimitiveSetMAs(Set(alpha, beta, gamma, delta),Set(5, 10, 1, 6, 9, 
                                                  //| 2, 7, 3, 8, 4),Set(1.0, 0.5, 0.2, 0.125),Set(1.333333, 1.33333335, 1.666666
                                                  //| 6, 1.6666667),None)),PrimitiveIterableMAs(Set(alpha, beta, gamma, delta),Se
                                                  //| t(5, 10, 1, 6, 9, 2, 7, 3, 8, 4),Set(1.0, 0.5, 0.2, 0.125),Set(1.333333, 1.
                                                  //| 33333335, 1.6666666, 1.6666667),Set(2012-11-28T21:38:39.451+01:00, 2012-11-
                                                  //| 28T22:38:39.451+01:00, 2012-11-28T23:38:39.451+01:00, 2012-11-29T00:38:39.4
                                                  //| 51+01:00)),ComplexMAs(List(),Vector(),Set(),List()),Some(TestAddress(Berlin
                                                  //| ,At the wall 89))))
  

  val backFromWarp = backFromWarpV.forceResult    //> backFromWarp  : riftwarp.TestObjectA = TestObjectA([B@33c13d0b,[B@538eb7b8,
                                                  //| PrimitiveTypes(I am Pete,true,127,-237823,-278234263,2658762576823765873658
                                                  //| 63876528756875682765252520577305007209857025728132213242,1.3672322,1.367232
                                                  //| 2350005,23761247614876823746.23846749182408,2012-11-28T20:38:39.390+01:00,a
                                                  //| aea8f95-a8da-4957-a0d5-f15830774187),PrimitiveListMAs(List(alpha, beta, gam
                                                  //| ma, delta),List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),List(1.0, 0.5, 0.2, 0.125),L
                                                  //| ist(1.333333, 1.33333335, 1.6666666, 1.6666667),List(2012-11-28T21:38:39.43
                                                  //| 3+01:00, 2012-11-28T22:38:39.434+01:00, 2012-11-28T23:38:39.434+01:00, 2012
                                                  //| -11-29T00:38:39.434+01:00)),PrimitiveVectorMAs(Vector(alpha, beta, gamma, d
                                                  //| elta),Vector(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),Vector(1.0, 0.5, 0.2, 0.125),Ve
                                                  //| ctor(1.333333, 1.33333335, 1.6666666, 1.6666667),Vector(2012-11-28T21:38:39
                                                  //| .435+01:00, 2012-11-28T22:38:39.435+01:00, 2012-11-28T23:38:39.435+01:00, 2
                                                  //| 012-11-29T00:38:39.435+01:00)),Some(PrimitiveSetMAs(Set(alpha, beta, gamma,
                                                  //|  delta),Set(5, 10, 1, 6, 9, 2, 7, 3, 8, 4),Set(1.0, 0.5, 0.2, 0.125),Set(1.
                                                  //| 333333, 1.33333335, 1.6666666, 1.6666667),None)),PrimitiveIterableMAs(Set(a
                                                  //| lpha, beta, gamma, delta),Set(5, 10, 1, 6, 9, 2, 7, 3, 8, 4),Set(1.0, 0.5, 
                                                  //| 0.2, 0.125),Set(1.333333, 1.33333335, 1.6666666, 1.6666667),Set(2012-11-28T
                                                  //| 21:38:39.451+01:00, 2012-11-28T22:38:39.451+01:00, 2012-11-28T23:38:39.451+
                                                  //| 01:00, 2012-11-29T00:38:39.451+01:00)),ComplexMAs(List(),Vector(),Set(),Lis
                                                  //| t()),Some(TestAddress(Berlin,At the wall 89)))

  testObject == backFromWarp                      //> res0: Boolean = false

  riftWarp.prepareForWarp[DimensionRawMap](RiftMap())(testObject).bind(warpStream =>
    riftWarp.receiveFromWarp[DimensionRawMap, TestObjectA](RiftMap())(warpStream)).map(rearrived =>
    rearrived == testObject)                      //> res1: scalaz.Validation[almhirt.common.Problem,Boolean] = Success(false)


  riftWarp.prepareForWarp[DimensionCord](RiftJson())(testObject).bind(warpStream =>
    riftWarp.receiveFromWarp[DimensionCord, TestObjectA](RiftJson())(warpStream)).map(rearrived =>
    rearrived == testObject)                      //> res2: scalaz.Validation[almhirt.common.Problem,Boolean] = Success(false)

}