package riftwarp.worksheets

import scalaz.syntax.validation._
import almhirt.syntax.almvalidation._
import riftwarp._
import riftwarp.impl._

object Worksheet {
  val riftWarp = RiftWarp.unsafeWithDefaults      //> riftWarp  : riftwarp.RiftWarp = riftwarp.RiftWarp$$anon$1@7d2452e8
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

  val testObject = TestObjectA.pete               //> testObject  : riftwarp.TestObjectA = TestObjectA([B@5fe0f2f6,[B@296f25a7,Pr
                                                  //| imitiveTypes(I am Pete,true,127,-237823,-278234263,265876257682376587365863
                                                  //| 876528756875682765252520577305007209857025728132213242,1.3672322,1.36723223
                                                  //| 50005,23761247614876823746.23846749182408,2012-12-10T19:57:45.093+01:00,4f2
                                                  //| 1048f-a8e1-4fd4-b6b2-5dfdf8035259),PrimitiveListMAs(List(alpha, beta, gamma
                                                  //| , delta),List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),List(1.0, 0.5, 0.2, 0.125),Lis
                                                  //| t(1.333333, 1.33333335, 1.6666666, 1.6666667),List(2012-12-10T20:57:45.135+
                                                  //| 01:00, 2012-12-10T21:57:45.136+01:00, 2012-12-10T22:57:45.136+01:00, 2012-1
                                                  //| 2-10T23:57:45.136+01:00)),PrimitiveVectorMAs(Vector(alpha, beta, gamma, del
                                                  //| ta),Vector(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),Vector(1.0, 0.5, 0.2, 0.125),Vect
                                                  //| or(1.333333, 1.33333335, 1.6666666, 1.6666667),Vector(2012-12-10T20:57:45.1
                                                  //| 36+01:00, 2012-12-10T21:57:45.136+01:00, 2012-12-10T22:57:45.136+01:00, 201
                                                  //| 2-12-10T23:57:45.136+01:00)),Some(PrimitiveSetMAs(Set(alpha, beta, gamma, d
                                                  //| elta),Set(5, 10, 1, 6, 9, 2, 7, 3, 8, 4),Set(1.0, 0.5, 0.2, 0.125),Set(1.33
                                                  //| 3333, 1.33333335, 1.6666666, 1.6666667),None)),PrimitiveIterableMAs(List(al
                                                  //| pha, beta, gamma, delta),List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),List(1.0, 0.5,
                                                  //|  0.2, 0.125),List(1.333333, 1.33333335, 1.6666666, 1.6666667),List(2012-12-
                                                  //| 10T20:57:45.153+01:00, 2012-12-10T21:57:45.153+01:00, 2012-12-10T22:57:45.1
                                                  //| 53+01:00, 2012-12-10T23:57:45.153+01:00)),ComplexMAs(List(TestAddress(Hambu
                                                  //| rg,Am Hafen), TestAddress(New York,Broadway), TestAddress(Los Angeles ,Suns
                                                  //| et Boulevard)),Vector(TestAddress(Hamburg,Am Hafen), TestAddress(New York,B
                                                  //| roadway), TestAddress(Los Angeles ,Sunset Boulevard)),Set(TestAddress(Hambu
                                                  //| rg,Am Hafen), TestAddress(New York,Broadway), TestAddress(Los Angeles ,Suns
                                                  //| et Boulevard)),List(true, hello, 1, 2, 3.0, 3.0, TestAddress(Somewhere,here
                                                  //| ))),Some(TestAddress(Berlin,At the wall 89)))
  val warpStreamV = riftWarp.prepareForWarp[DimensionCord](RiftJson())(testObject)
                                                  //> warpStreamV  : almhirt.common.package.AlmValidation[riftwarp.DimensionCord]
                                                  //|  = Success(DimensionCord({"riftwarptd":"riftwarp.TestObjectA","arrayByte":[
                                                  //| 126,-123,12,-45,-128],"blob":{"riftwarptd":"RiftBlobArrayValue","data":"AAA
                                                  //| AAAAGhQzTgHAAAAA="},"primitiveTypes":{"riftwarptd":"riftwarp.PrimitiveTypes
                                                  //| ","str":"I am Pete","bool":true,"byte":127,"int":-237823,"long":-278234263,
                                                  //| "bigInt":"26587625768237658736586387652875687568276525252057730500720985702
                                                  //| 5728132213242","float":1.3672322034835815,"double":1.3672322350005,"bigDec"
                                                  //| :"23761247614876823746.23846749182408","dateTime":"2012-12-10T19:57:45.093+
                                                  //| 01:00","uuid":"4f21048f-a8e1-4fd4-b6b2-5dfdf8035259"},"primitiveListMAs":{"
                                                  //| riftwarptd":"riftwarp.PrimitiveListMAs","listString":["alpha","beta","gamma
                                                  //| ","delta"],"listInt":[1,2,3,4,5,6,7,8,9,10],"listDouble":[1.0,0.5,0.2,0.125
                                                  //| ],"listBigDecimal":["1.333333","1.33333335","1.6666666","1.6666667"],"listD
                                                  //| ateTime":["2012-12-10T20:57:45.135+01:00","2012-12-10T21:57:45.136+01:00","
                                                  //| 2012-12-10T22:57:45.136+01:00","2012-12-10T23:57:45.136+01:00"]},"primitive
                                                  //| VectorMAs":{"riftwarptd":"riftwarp.PrimitiveVectorMAs","vectorString":["alp
                                                  //| ha","beta","gamma","delta"],"vectorInt":[1,2,3,4,5,6,7,8,9,10],"vectorDoubl
                                                  //| e":[1.0,0.5,0.2,0.125],"vectorBigDecimal":["1.333333","1.33333335","1.66666
                                                  //| 66","1.6666667"],"vectorDateTime":["2012-12-10T20:57:45.136+01:00","2012-12
                                                  //| -10T21:57:45.136+01:00","2012-12-10T22:57:45.136+01:00","2012-12-10T23:57:4
                                                  //| 5.136+01:00"]},"primitiveSetMAs":{"riftwarptd":"riftwarp.PrimitiveSetMAs","
                                                  //| setString":["alpha","beta","gamma","delta"],"setInt":[4,8,2,1,10,9,7,5,3,6]
                                                  //| ,"setDouble":[1.0,0.5,0.2,0.125],"setBigDecimal":["1.333333","1.33333335","
                                                  //| 1.6666666","1.6666667"],"setDateTime":null},"primitiveIterableMAs":{"riftwa
                                                  //| rptd":"riftwarp.PrimitiveIterableMAs","iterableString":["alpha","beta","gam
                                                  //| ma","delta"],"iterableInt":[1,2,3,4,5,6,7,8,9,10],"iterableDouble":[1.0,0.5
                                                  //| ,0.2,0.125],"iterableBigDecimal":["1.333333","1.33333335","1.6666666","1.66
                                                  //| 66667"],"iterableDateTime":["2012-12-10T20:57:45.153+01:00","2012-12-10T21:
                                                  //| 57:45.153+01:00","2012-12-10T22:57:45.153+01:00","2012-12-10T23:57:45.153+0
                                                  //| 1:00"]},"complexMAs":{"riftwarptd":"riftwarp.ComplexMAs","addresses1":[{"ri
                                                  //| ftwarptd":"riftwarp.TestAddress","city":"Hamburg","street":"Am Hafen"},{"ri
                                                  //| ftwarptd":"riftwarp.TestAddress","city":"New York","street":"Broadway"},{"r
                                                  //| iftwarptd":"riftwarp.TestAddress","city":"Los Angeles ","street":"Sunset Bo
                                                  //| ulevard"}],"addresses2":[{"riftwarptd":"riftwarp.TestAddress","city":"Hambu
                                                  //| rg","street":"Am Hafen"},{"riftwarptd":"riftwarp.TestAddress","city":"New Y
                                                  //| ork","street":"Broadway"},{"riftwarptd":"riftwarp.TestAddress","city":"Los 
                                                  //| Angeles ","street":"Sunset Boulevard"}],"addresses3":[{"riftwarptd":"riftwa
                                                  //| rp.TestAddress","city":"Hamburg","street":"Am Hafen"},{"riftwarptd":"riftwa
                                                  //| rp.TestAddress","city":"New York","street":"Broadway"},{"riftwarptd":"riftw
                                                  //| arp.TestAddress","city":"Los Angeles ","street":"Sunset Boulevard"}],"anyth
                                                  //| ing":[true,"hello",1,2,3.0,3.0,{"riftwarptd":"riftwarp.TestAddress","city":
                                                  //| "Somewhere","street":"here"}]},"addressOpt":{"riftwarptd":"riftwarp.TestAdd
                                                  //| ress","city":"Berlin","street":"At the wall 89"}}))
 
   
  val backFromWarpV = riftWarp.receiveFromWarp[DimensionCord, TestObjectA](RiftJson())(warpStreamV.forceResult)
                                                  //> backFromWarpV  : almhirt.common.package.AlmValidation[riftwarp.TestObjectA]
                                                  //|  = Success(TestObjectA([B@1de4eb5b,[B@2017b2b2,PrimitiveTypes(I am Pete,tru
                                                  //| e,127,-237823,-278234263,26587625768237658736586387652875687568276525252057
                                                  //| 7305007209857025728132213242,1.3672322,1.3672322350005,23761247614876823746
                                                  //| .23846749182408,2012-12-10T19:57:45.093+01:00,4f21048f-a8e1-4fd4-b6b2-5dfdf
                                                  //| 8035259),PrimitiveListMAs(List(alpha, beta, gamma, delta),List(1, 2, 3, 4, 
                                                  //| 5, 6, 7, 8, 9, 10),List(1.0, 0.5, 0.2, 0.125),List(1.333333, 1.33333335, 1.
                                                  //| 6666666, 1.6666667),List(2012-12-10T20:57:45.135+01:00, 2012-12-10T21:57:45
                                                  //| .136+01:00, 2012-12-10T22:57:45.136+01:00, 2012-12-10T23:57:45.136+01:00)),
                                                  //| PrimitiveVectorMAs(Vector(alpha, beta, gamma, delta),Vector(1, 2, 3, 4, 5, 
                                                  //| 6, 7, 8, 9, 10),Vector(1.0, 0.5, 0.2, 0.125),Vector(1.333333, 1.33333335, 1
                                                  //| .6666666, 1.6666667),Vector(2012-12-10T20:57:45.136+01:00, 2012-12-10T21:57
                                                  //| :45.136+01:00, 2012-12-10T22:57:45.136+01:00, 2012-12-10T23:57:45.136+01:00
                                                  //| )),Some(PrimitiveSetMAs(Set(alpha, beta, gamma, delta),Set(5, 10, 1, 6, 9, 
                                                  //| 2, 7, 3, 8, 4),Set(1.0, 0.5, 0.2, 0.125),Set(1.333333, 1.33333335, 1.666666
                                                  //| 6, 1.6666667),None)),PrimitiveIterableMAs(Set(alpha, beta, gamma, delta),Se
                                                  //| t(5, 10, 1, 6, 9, 2, 7, 3, 8, 4),Set(1.0, 0.5, 0.2, 0.125),Set(1.333333, 1.
                                                  //| 33333335, 1.6666666, 1.6666667),Set(2012-12-10T20:57:45.153+01:00, 2012-12-
                                                  //| 10T21:57:45.153+01:00, 2012-12-10T22:57:45.153+01:00, 2012-12-10T23:57:45.1
                                                  //| 53+01:00)),ComplexMAs(List(TestAddress(Hamburg,Am Hafen), TestAddress(New Y
                                                  //| ork,Broadway), TestAddress(Los Angeles ,Sunset Boulevard)),Vector(TestAddre
                                                  //| ss(Hamburg,Am Hafen), TestAddress(New York,Broadway), TestAddress(Los Angel
                                                  //| es ,Sunset Boulevard)),Set(TestAddress(Hamburg,Am Hafen), TestAddress(New Y
                                                  //| ork,Broadway), TestAddress(Los Angeles ,Sunset Boulevard)),List(true, hello
                                                  //| , 1.0, 2.0, 3.0, 3.0, Map(riftwarptd -> riftwarp.TestAddress, city -> Somew
                                                  //| here, street -> here))),Some(TestAddress(Berlin,At the wall 89))))
  

  val backFromWarp = backFromWarpV.forceResult    //> backFromWarp  : riftwarp.TestObjectA = TestObjectA([B@1de4eb5b,[B@2017b2b2,
                                                  //| PrimitiveTypes(I am Pete,true,127,-237823,-278234263,2658762576823765873658
                                                  //| 63876528756875682765252520577305007209857025728132213242,1.3672322,1.367232
                                                  //| 2350005,23761247614876823746.23846749182408,2012-12-10T19:57:45.093+01:00,4
                                                  //| f21048f-a8e1-4fd4-b6b2-5dfdf8035259),PrimitiveListMAs(List(alpha, beta, gam
                                                  //| ma, delta),List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),List(1.0, 0.5, 0.2, 0.125),L
                                                  //| ist(1.333333, 1.33333335, 1.6666666, 1.6666667),List(2012-12-10T20:57:45.13
                                                  //| 5+01:00, 2012-12-10T21:57:45.136+01:00, 2012-12-10T22:57:45.136+01:00, 2012
                                                  //| -12-10T23:57:45.136+01:00)),PrimitiveVectorMAs(Vector(alpha, beta, gamma, d
                                                  //| elta),Vector(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),Vector(1.0, 0.5, 0.2, 0.125),Ve
                                                  //| ctor(1.333333, 1.33333335, 1.6666666, 1.6666667),Vector(2012-12-10T20:57:45
                                                  //| .136+01:00, 2012-12-10T21:57:45.136+01:00, 2012-12-10T22:57:45.136+01:00, 2
                                                  //| 012-12-10T23:57:45.136+01:00)),Some(PrimitiveSetMAs(Set(alpha, beta, gamma,
                                                  //|  delta),Set(5, 10, 1, 6, 9, 2, 7, 3, 8, 4),Set(1.0, 0.5, 0.2, 0.125),Set(1.
                                                  //| 333333, 1.33333335, 1.6666666, 1.6666667),None)),PrimitiveIterableMAs(Set(a
                                                  //| lpha, beta, gamma, delta),Set(5, 10, 1, 6, 9, 2, 7, 3, 8, 4),Set(1.0, 0.5, 
                                                  //| 0.2, 0.125),Set(1.333333, 1.33333335, 1.6666666, 1.6666667),Set(2012-12-10T
                                                  //| 20:57:45.153+01:00, 2012-12-10T21:57:45.153+01:00, 2012-12-10T22:57:45.153+
                                                  //| 01:00, 2012-12-10T23:57:45.153+01:00)),ComplexMAs(List(TestAddress(Hamburg,
                                                  //| Am Hafen), TestAddress(New York,Broadway), TestAddress(Los Angeles ,Sunset 
                                                  //| Boulevard)),Vector(TestAddress(Hamburg,Am Hafen), TestAddress(New York,Broa
                                                  //| dway), TestAddress(Los Angeles ,Sunset Boulevard)),Set(TestAddress(Hamburg,
                                                  //| Am Hafen), TestAddress(New York,Broadway), TestAddress(Los Angeles ,Sunset 
                                                  //| Boulevard)),List(true, hello, 1.0, 2.0, 3.0, 3.0, Map(riftwarptd -> riftwar
                                                  //| p.TestAddress, city -> Somewhere, street -> here))),Some(TestAddress(Berlin
                                                  //| ,At the wall 89)))

  testObject == backFromWarp                      //> res0: Boolean = false


  riftWarp.prepareForWarp[DimensionRawMap](RiftMap())(testObject).bind(warpStream =>
    riftWarp.receiveFromWarp[DimensionRawMap, TestObjectA](RiftMap())(warpStream)).map(rearrived =>
    rearrived == testObject)                      //> res1: scalaz.Validation[almhirt.common.Problem,Boolean] = Success(true)

  riftWarp.prepareForWarp[DimensionCord](RiftJson())(testObject).bind(warpStream =>
    riftWarp.receiveFromWarp[DimensionCord, TestObjectA](RiftJson())(warpStream)).map(rearrived =>
    rearrived == testObject)                      //> res2: scalaz.Validation[almhirt.common.Problem,Boolean] = Success(false)

}