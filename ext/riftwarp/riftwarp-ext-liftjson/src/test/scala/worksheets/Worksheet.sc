package worksheets

import scalaz.syntax.validation._
import almhirt.syntax.almvalidation._
import riftwarp.{RiftWarp, RiftJson, DimensionNiceString, DimensionString, DimensionCord }
import riftwarp.ext.liftjson._

object Worksheet {
  val riftWarp = RiftWarp.unsafeWithDefaults      //> riftWarp  : riftwarp.RiftWarp = riftwarp.RiftWarp$$anon$1@3882764b
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
  
  
  LiftJson.registerAsDefaults(riftWarp)           //> res0: almhirt.common.package.AlmValidation[riftwarp.RiftWarp] = Success(rif
                                                  //| twarp.RiftWarp$$anon$1@3882764b)
     
  val testObject = TestObjectA.pete               //> testObject  : riftwarp.ext.liftjson.TestObjectA = TestObjectA([B@426295eb,[
                                                  //| B@56609959,PrimitiveTypes(I am Pete,true,127,-237823,-278234263,26587625768
                                                  //| 2376587365863876528756875682765252520577305007209857025728132213242,1.36723
                                                  //| 22,1.3672322350005,23761247614876823746.23846749182408,2012-12-10T19:58:48.
                                                  //| 175+01:00,37520516-d0c4-4d3d-bf8a-fe253a92ad3d),PrimitiveListMAs(List(alpha
                                                  //| , beta, gamma, delta),List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),List(1.0, 0.5, 0.
                                                  //| 2, 0.125),List(1.333333, 1.33333335, 1.6666666, 1.6666667),List(2012-12-10T
                                                  //| 20:58:48.217+01:00, 2012-12-10T21:58:48.218+01:00, 2012-12-10T22:58:48.218+
                                                  //| 01:00, 2012-12-10T23:58:48.218+01:00)),PrimitiveVectorMAs(Vector(alpha, bet
                                                  //| a, gamma, delta),Vector(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),Vector(1.0, 0.5, 0.2
                                                  //| , 0.125),Vector(1.333333, 1.33333335, 1.6666666, 1.6666667),Vector(2012-12-
                                                  //| 10T20:58:48.218+01:00, 2012-12-10T21:58:48.218+01:00, 2012-12-10T22:58:48.2
                                                  //| 18+01:00, 2012-12-10T23:58:48.218+01:00)),Some(PrimitiveSetMAs(Set(alpha, b
                                                  //| eta, gamma, delta),Set(5, 10, 1, 6, 9, 2, 7, 3, 8, 4),Set(1.0, 0.5, 0.2, 0.
                                                  //| 125),Set(1.333333, 1.33333335, 1.6666666, 1.6666667),None)),PrimitiveIterab
                                                  //| leMAs(List(alpha, beta, gamma, delta),List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),L
                                                  //| ist(1.0, 0.5, 0.2, 0.125),List(1.333333, 1.33333335, 1.6666666, 1.6666667),
                                                  //| List(2012-12-10T20:58:48.235+01:00, 2012-12-10T21:58:48.235+01:00, 2012-12-
                                                  //| 10T22:58:48.235+01:00, 2012-12-10T23:58:48.235+01:00)),ComplexMAs(List(Test
                                                  //| Address(Hamburg,Am Hafen), TestAddress(New York,Broadway), TestAddress(Los 
                                                  //| Angeles ,Sunset Boulevard)),Vector(TestAddress(Hamburg,Am Hafen), TestAddre
                                                  //| ss(New York,Broadway), TestAddress(Los Angeles ,Sunset Boulevard)),Set(Test
                                                  //| Address(Hamburg,Am Hafen), TestAddress(New York,Broadway), TestAddress(Los 
                                                  //| Angeles ,Sunset Boulevard)),List(true, hello, 1, 2, 3.0, 3.0, TestAddress(S
                                                  //| omewhere,here))),Some(TestAddress(Berlin,At the wall 89)))
  val warpStreamV = riftWarp.prepareForWarp[DimensionNiceString](RiftJson())(testObject)
                                                  //> warpStreamV  : almhirt.common.package.AlmValidation[riftwarp.DimensionNiceS
                                                  //| tring] = Success(DimensionNiceString({
                                                  //|   "riftwarptd":"riftwarp.ext.liftjson.TestObjectA",
                                                  //|   "arrayByte":[126,-123,12,-45,-128],
                                                  //|   "blob":{
                                                  //|     "riftwarptd":"RiftBlobArrayValue",
                                                  //|     "data":"AAAAAAAGhQzTgHAAAAA="
                                                  //|   },
                                                  //|   "primitiveTypes":{
                                                  //|     "riftwarptd":"riftwarp.ext.liftjson.PrimitiveTypes",
                                                  //|     "str":"I am Pete",
                                                  //|     "bool":true,
                                                  //|     "byte":127,
                                                  //|     "int":-237823,
                                                  //|     "long":-278234263,
                                                  //|     "bigInt":"2658762576823765873658638765287568756827652525205773050072098
                                                  //| 57025728132213242",
                                                  //|     "float":1.3672322034835815,
                                                  //|     "double":1.3672322350005,
                                                  //|     "bigDec":"23761247614876823746.23846749182408",
                                                  //|     "dateTime":"2012-12-10T19:58:48.175+01:00",
                                                  //|     "uuid":"37520516-d0c4-4d3d-bf8a-fe253a92ad3d"
                                                  //|   },
                                                  //|   "primitiveListMAs":{
                                                  //|     "riftwarptd":"riftwarp.ext.liftjson.PrimitiveListMAs",
                                                  //|     "listString":["alpha","beta","gamma","delta"],
                                                  //|     "listInt":[1,2,3,4,5,6,7,8,9,10],
                                                  //|     "listDouble":[1.0,0.5,0.2,0.125],
                                                  //|     "listBigDecimal":["1.333333","1.33333335","1.6666666","1.6666667"],
                                                  //|     "listDateTime":["2012-12-10T20:58:48.217+01:00","2012-12-10T21:58:48.21
                                                  //| 8+01:00","2012-12-10T22:58:48.218+01:00","2012-12-10T23:58:48.218+01:00"]
                                                  //|   },
                                                  //|   "primitiveVectorMAs":{
                                                  //|     "riftwarptd":"riftwarp.ext.liftjson.PrimitiveVectorMAs",
                                                  //|     "vectorString":["alpha","beta","gamma","delta"],
                                                  //|     "vectorInt":[1,2,3,4,5,6,7,8,9,10],
                                                  //|     "vectorDouble":[1.0,0.5,0.2,0.125],
                                                  //|     "vectorBigDecimal":["1.333333","1.33333335","1.6666666","1.6666667"],
                                                  //|     "vectorDateTime":["2012-12-10T20:58:48.218+01:00","2012-12-10T21:58:48.
                                                  //| 218+01:00","2012-12-10T22:58:48.218+01:00","2012-12-10T23:58:48.218+01:00"]
                                                  //| 
                                                  //|   },
                                                  //|   "primitiveSetMAs":{
                                                  //|     "riftwarptd":"riftwarp.ext.liftjson.PrimitiveSetMAs",
                                                  //|     "setString":["alpha","beta","gamma","delta"],
                                                  //|     "setInt":[3,1,9,2,8,4,10,5,6,7],
                                                  //|     "setDouble":[1.0,0.5,0.2,0.125],
                                                  //|     "setBigDecimal":["1.333333","1.33333335","1.6666666","1.6666667"],
                                                  //|     "setDateTime":null
                                                  //|   },
                                                  //|   "primitiveIterableMAs":{
                                                  //|     "riftwarptd":"riftwarp.ext.liftjson.PrimitiveIterableMAs",
                                                  //|     "iterableString":["alpha","beta","gamma","delta"],
                                                  //|     "iterableInt":[1,2,3,4,5,6,7,8,9,10],
                                                  //|     "iterableDouble":[1.0,0.5,0.2,0.125],
                                                  //|     "iterableBigDecimal":["1.333333","1.33333335","1.6666666","1.6666667"],
                                                  //| 
                                                  //|     "iterableDateTime":["2012-12-10T20:58:48.235+01:00","2012-12-10T21:58:4
                                                  //| 8.235+01:00","2012-12-10T22:58:48.235+01:00","2012-12-10T23:58:48.235+01:00
                                                  //| "]
                                                  //|   },
                                                  //|   "complexMAs":{
                                                  //|     "riftwarptd":"riftwarp.ext.liftjson.ComplexMAs",
                                                  //|     "addresses1":[{
                                                  //|       "riftwarptd":"riftwarp.ext.liftjson.TestAddress",
                                                  //|       "city":"Hamburg",
                                                  //|       "street":"Am Hafen"
                                                  //|     },{
                                                  //|       "riftwarptd":"riftwarp.ext.liftjson.TestAddress",
                                                  //|       "city":"New York",
                                                  //|       "street":"Broadway"
                                                  //|     },{
                                                  //|       "riftwarptd":"riftwarp.ext.liftjson.TestAddress",
                                                  //|       "city":"Los Angeles ",
                                                  //|       "street":"Sunset Boulevard"
                                                  //|     }],
                                                  //|     "addresses2":[{
                                                  //|       "riftwarptd":"riftwarp.ext.liftjson.TestAddress",
                                                  //|       "city":"Hamburg",
                                                  //|       "street":"Am Hafen"
                                                  //|     },{
                                                  //|       "riftwarptd":"riftwarp.ext.liftjson.TestAddress",
                                                  //|       "city":"New York",
                                                  //|       "street":"Broadway"
                                                  //|     },{
                                                  //|       "riftwarptd":"riftwarp.ext.liftjson.TestAddress",
                                                  //|       "city":"Los Angeles ",
                                                  //|       "street":"Sunset Boulevard"
                                                  //|     }],
                                                  //|     "addresses3":[{
                                                  //|       "riftwarptd":"riftwarp.ext.liftjson.TestAddress",
                                                  //|       "city":"Hamburg",
                                                  //|       "street":"Am Hafen"
                                                  //|     },{
                                                  //|       "riftwarptd":"riftwarp.ext.liftjson.TestAddress",
                                                  //|       "city":"New York",
                                                  //|       "street":"Broadway"
                                                  //|     },{
                                                  //|       "riftwarptd":"riftwarp.ext.liftjson.TestAddress",
                                                  //|       "city":"Los Angeles ",
                                                  //|       "street":"Sunset Boulevard"
                                                  //|     }],
                                                  //|     "anything":[true,"hello",1,2,3.0,3.0,{
                                                  //|       "riftwarptd":"riftwarp.ext.liftjson.TestAddress",
                                                  //|       "city":"Somewhere",
                                                  //|       "street":"here"
                                                  //|     }]
                                                  //|   },
                                                  //|   "addressOpt":{
                                                  //|     "riftwarptd":"riftwarp.ext.liftjson.TestAddress",
                                                  //|     "city":"Berlin",
                                                  //|     "street":"At the wall 89"
                                                  //|   }
                                                  //| }))
  
   

   
  val backFromWarpV = riftWarp.receiveFromWarp[DimensionNiceString, TestObjectA](RiftJson())(warpStreamV.forceResult)
                                                  //> backFromWarpV  : almhirt.common.package.AlmValidation[riftwarp.ext.liftjson
                                                  //| .TestObjectA] = Success(TestObjectA([B@1f315415,[B@5f873eb2,PrimitiveTypes(
                                                  //| I am Pete,true,127,-237823,-278234263,2658762576823765873658638765287568756
                                                  //| 82765252520577305007209857025728132213242,1.3672322,1.3672322350005,2376124
                                                  //| 7614876823746.23846749182408,2012-12-10T19:58:48.175+01:00,37520516-d0c4-4d
                                                  //| 3d-bf8a-fe253a92ad3d),PrimitiveListMAs(List(alpha, beta, gamma, delta),List
                                                  //| (1, 2, 3, 4, 5, 6, 7, 8, 9, 10),List(1.0, 0.5, 0.2, 0.125),List(1.333333, 1
                                                  //| .33333335, 1.6666666, 1.6666667),List(2012-12-10T20:58:48.217+01:00, 2012-1
                                                  //| 2-10T21:58:48.218+01:00, 2012-12-10T22:58:48.218+01:00, 2012-12-10T23:58:48
                                                  //| .218+01:00)),PrimitiveVectorMAs(Vector(alpha, beta, gamma, delta),Vector(1,
                                                  //|  2, 3, 4, 5, 6, 7, 8, 9, 10),Vector(1.0, 0.5, 0.2, 0.125),Vector(1.333333, 
                                                  //| 1.33333335, 1.6666666, 1.6666667),Vector(2012-12-10T20:58:48.218+01:00, 201
                                                  //| 2-12-10T21:58:48.218+01:00, 2012-12-10T22:58:48.218+01:00, 2012-12-10T23:58
                                                  //| :48.218+01:00)),Some(PrimitiveSetMAs(Set(alpha, beta, gamma, delta),Set(5, 
                                                  //| 10, 1, 6, 9, 2, 7, 3, 8, 4),Set(1.0, 0.5, 0.2, 0.125),Set(1.333333, 1.33333
                                                  //| 335, 1.6666666, 1.6666667),None)),PrimitiveIterableMAs(Set(alpha, beta, gam
                                                  //| ma, delta),Set(5, 10, 1, 6, 9, 2, 7, 3, 8, 4),Set(1.0, 0.5, 0.2, 0.125),Set
                                                  //| (1.333333, 1.33333335, 1.6666666, 1.6666667),Set(2012-12-10T20:58:48.235+01
                                                  //| :00, 2012-12-10T21:58:48.235+01:00, 2012-12-10T22:58:48.235+01:00, 2012-12-
                                                  //| 10T23:58:48.235+01:00)),ComplexMAs(List(TestAddress(Hamburg,Am Hafen), Test
                                                  //| Address(New York,Broadway), TestAddress(Los Angeles ,Sunset Boulevard)),Vec
                                                  //| tor(TestAddress(Hamburg,Am Hafen), TestAddress(New York,Broadway), TestAddr
                                                  //| ess(Los Angeles ,Sunset Boulevard)),Set(TestAddress(Hamburg,Am Hafen), Test
                                                  //| Address(New York,Broadway), TestAddress(Los Angeles ,Sunset Boulevard)),Lis
                                                  //| t(true, hello, 1.0, 2.0, 3.0, 3.0, Map(riftwarptd -> riftwarp.ext.liftjson.
                                                  //| TestAddress, city -> Somewhere, street -> here))),Some(TestAddress(Berlin,A
                                                  //| t the wall 89))))
 
 
  val backFromWarp = backFromWarpV.forceResult    //> backFromWarp  : riftwarp.ext.liftjson.TestObjectA = TestObjectA([B@1f315415
                                                  //| ,[B@5f873eb2,PrimitiveTypes(I am Pete,true,127,-237823,-278234263,265876257
                                                  //| 682376587365863876528756875682765252520577305007209857025728132213242,1.367
                                                  //| 2322,1.3672322350005,23761247614876823746.23846749182408,2012-12-10T19:58:4
                                                  //| 8.175+01:00,37520516-d0c4-4d3d-bf8a-fe253a92ad3d),PrimitiveListMAs(List(alp
                                                  //| ha, beta, gamma, delta),List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),List(1.0, 0.5, 
                                                  //| 0.2, 0.125),List(1.333333, 1.33333335, 1.6666666, 1.6666667),List(2012-12-1
                                                  //| 0T20:58:48.217+01:00, 2012-12-10T21:58:48.218+01:00, 2012-12-10T22:58:48.21
                                                  //| 8+01:00, 2012-12-10T23:58:48.218+01:00)),PrimitiveVectorMAs(Vector(alpha, b
                                                  //| eta, gamma, delta),Vector(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),Vector(1.0, 0.5, 0
                                                  //| .2, 0.125),Vector(1.333333, 1.33333335, 1.6666666, 1.6666667),Vector(2012-1
                                                  //| 2-10T20:58:48.218+01:00, 2012-12-10T21:58:48.218+01:00, 2012-12-10T22:58:48
                                                  //| .218+01:00, 2012-12-10T23:58:48.218+01:00)),Some(PrimitiveSetMAs(Set(alpha,
                                                  //|  beta, gamma, delta),Set(5, 10, 1, 6, 9, 2, 7, 3, 8, 4),Set(1.0, 0.5, 0.2, 
                                                  //| 0.125),Set(1.333333, 1.33333335, 1.6666666, 1.6666667),None)),PrimitiveIter
                                                  //| ableMAs(Set(alpha, beta, gamma, delta),Set(5, 10, 1, 6, 9, 2, 7, 3, 8, 4),S
                                                  //| et(1.0, 0.5, 0.2, 0.125),Set(1.333333, 1.33333335, 1.6666666, 1.6666667),Se
                                                  //| t(2012-12-10T20:58:48.235+01:00, 2012-12-10T21:58:48.235+01:00, 2012-12-10T
                                                  //| 22:58:48.235+01:00, 2012-12-10T23:58:48.235+01:00)),ComplexMAs(List(TestAdd
                                                  //| ress(Hamburg,Am Hafen), TestAddress(New York,Broadway), TestAddress(Los Ang
                                                  //| eles ,Sunset Boulevard)),Vector(TestAddress(Hamburg,Am Hafen), TestAddress(
                                                  //| New York,Broadway), TestAddress(Los Angeles ,Sunset Boulevard)),Set(TestAdd
                                                  //| ress(Hamburg,Am Hafen), TestAddress(New York,Broadway), TestAddress(Los Ang
                                                  //| eles ,Sunset Boulevard)),List(true, hello, 1.0, 2.0, 3.0, 3.0, Map(riftwarp
                                                  //| td -> riftwarp.ext.liftjson.TestAddress, city -> Somewhere, street -> here)
                                                  //| )),Some(TestAddress(Berlin,At the wall 89)))

  testObject == backFromWarp                      //> res1: Boolean = false


  riftWarp.prepareForWarp[DimensionCord](RiftJson())(testObject).bind(warpStream =>
    riftWarp.receiveFromWarp[DimensionCord, TestObjectA](RiftJson())(warpStream)).map(rearrived =>
    rearrived == testObject)                      //> res2: scalaz.Validation[almhirt.common.Problem,Boolean] = Success(false)

}