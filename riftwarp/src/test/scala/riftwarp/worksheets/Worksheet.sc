package riftwarp.worksheets

import scalaz.syntax.validation._
import almhirt.syntax.almvalidation._
import riftwarp._
import riftwarp.impl._

object Worksheet {
  val riftWarp = RiftWarp.concurrentWithDefaults  //> riftWarp  : riftwarp.RiftWarp = riftwarp.RiftWarp$$anon$1@367cb9ff
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
  riftWarp.barracks.addDecomposer(new PrimitiveMapsDecomposer())
  riftWarp.barracks.addRecomposer(new PrimitiveMapsRecomposer())
  riftWarp.barracks.addDecomposer(new ComplexMapsDecomposer())
  riftWarp.barracks.addRecomposer(new ComplexMapsRecomposer())


  val testObject = TestObjectA.pete               //> testObject  : riftwarp.TestObjectA = TestObjectA([B@16cbfbed,[B@2a08904c,Pr
                                                  //| imitiveTypes(I am Pete,true,127,-237823,-278234263,265876257682376587365863
                                                  //| 876528756875682765252520577305007209857025728132213242,1.3672322,1.36723223
                                                  //| 50005,23761247614876823746.23846749182408,2013-01-21T10:23:47.161+01:00,68d
                                                  //| 0264b-d056-41ba-babe-3a2ad12c19d5),PrimitiveListMAs(List(alpha, beta, gamma
                                                  //| , delta),List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),List(1.0, 0.5, 0.2, 0.125),Lis
                                                  //| t(1.333333, 1.33333335, 1.6666666, 1.6666667),List(2013-01-21T11:23:47.251+
                                                  //| 01:00, 2013-01-21T12:23:47.251+01:00, 2013-01-21T13:23:47.251+01:00, 2013-0
                                                  //| 1-21T14:23:47.251+01:00)),PrimitiveVectorMAs(Vector(alpha, beta, gamma, del
                                                  //| ta),Vector(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),Vector(1.0, 0.5, 0.2, 0.125),Vect
                                                  //| or(1.333333, 1.33333335, 1.6666666, 1.6666667),Vector(2013-01-21T11:23:47.2
                                                  //| 51+01:00, 2013-01-21T12:23:47.251+01:00, 2013-01-21T13:23:47.251+01:00, 201
                                                  //| 3-01-21T14:23:47.251+01:00)),Some(PrimitiveSetMAs(Set(alpha, beta, gamma, d
                                                  //| elta),Set(5, 10, 1, 6, 9, 2, 7, 3, 8, 4),Set(1.0, 0.5, 0.2, 0.125),Set(1.33
                                                  //| 3333, 1.33333335, 1.6666666, 1.6666667),None)),PrimitiveIterableMAs(List(al
                                                  //| pha, beta, gamma, delta),List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),List(1.0, 0.5,
                                                  //|  0.2, 0.125),List(1.333333, 1.33333335, 1.6666666, 1.6666667),List(2013-01-
                                                  //| 21T11:23:47.261+01:00, 2013-01-21T12:23:47.261+01:00, 2013-01-21T13:23:47.2
                                                  //| 61+01:00, 2013-01-21T14:23:47.261+01:00)),ComplexMAs(List(TestAddress(Hambu
                                                  //| rg,Am Hafen), TestAddress(New York,Broadway), TestAddress(Los Angeles ,Suns
                                                  //| et Boulevard)),Vector(TestAddress(Hamburg,Am Hafen), TestAddress(New York,B
                                                  //| roadway), TestAddress(Los Angeles ,Sunset Boulevard)),Set(TestAddress(Hambu
                                                  //| rg,Am Hafen), TestAddress(New York,Broadway), TestAddress(Los Angeles ,Suns
                                                  //| et Boulevard)),List(true, hello, 1, 2, 3.0, 3.0, TestAddress(Somewhere,here
                                                  //| ))),PrimitiveMaps(Map(1 -> 10, 2 -> 20, 3 -> 30, 4 -> 40),Map(a -> 1, b -> 
                                                  //| 2, c -> 3),Map(d11497de-f512-45c3-b5b4-d935647a4f8c -> 2013-01-21T10:23:47.
                                                  //| 261+01:00, 1f43b794-6781-446f-bc72-cd93189d771d -> 2013-01-22T10:23:47.261+
                                                  //| 01:00, 52e01510-9555-4136-a0e6-d0c83870f134 -> 2013-01-23T10:23:47.261+01:0
                                                  //| 0)),ComplexMaps(Map(0 -> TestAddress(Hamburg,Am Hafen), 1 -> TestAddress(Ne
                                                  //| w York,Broadway), 2 -> TestAddress(Los Angeles ,Sunset Boulevard)),Map(0 ->
                                                  //|  TestAddress(Hamburg,Am Hafen), 1 -> TestAddress(New York,Broadway), 2 -> T
                                                  //| estAddress(Los Angeles ,Sunset Boulevard)),Map(x -> d1c5bdd2-a26c-4a55-ad9e
                                                  //| -42d15acc4405, unspecifiedProblem -> almhirt.common.UnspecifiedProblem
                                                  //| Test
                                                  //| Category: SystemProblem
                                                  //| Severity: Major
                                                  //| Arguments: Map(arg1 -> 95)
                                                  //| , y -> 68f40614-102b-4c12-9a6d-ea8636b0a1d4, 1 -> TestAddress(New York,Broa
                                                  //| dway), 0 -> TestAddress(Hamburg,Am Hafen), 2 -> TestAddress(Los Angeles ,Su
                                                  //| nset Boulevard), unknownType -> UnknownObject(1), z -> 2013-01-21T10:23:47.
                                                  //| 281+01:00)),Some(TestAddress(Berlin,At the wall 89)))
 
 	var blobdata = new scala.collection.mutable.HashMap[String, Array[Byte]]
                                                  //> blobdata  : scala.collection.mutable.HashMap[String,Array[Byte]] = Map()
	
	val blobDivert: BlobDivert = (arr, path) => {
	  val name = java.util.UUID.randomUUID().toString
		blobdata += (name -> arr)
		RiftBlobRefByName(name).success
	}                                         //> blobDivert  : (Array[Byte], riftwarp.RiftBlobIdentifier) => almhirt.common.
                                                  //| AlmValidation[riftwarp.RiftBlob] = <function2>
	
	val blobFetch: BlobFetch = blob =>
	  blob match {
	    case RiftBlobRefByName(name) => blobdata(name).success
	  }                                       //> blobFetch  : riftwarp.RiftBlob => almhirt.common.AlmValidation[Array[Byte]]
                                                  //|  = <function1>

  val warpStreamV = riftWarp.prepareForWarpWithBlobs[DimensionString](blobDivert)(RiftJson())(testObject)
                                                  //> warpStreamV  : almhirt.common.AlmValidation[riftwarp.DimensionString] = Suc
                                                  //| cess(DimensionString({"riftdesc":"riftwarp.TestObjectA","arrayByte":[126,-1
                                                  //| 23,12,-45,-128],"blob":{"riftdesc":"RiftBlobRefByName","name":"e15336d4-279
                                                  //| c-47d0-bc4b-561be4e9f177"},"primitiveTypes":{"riftdesc":"riftwarp.Primitive
                                                  //| Types","str":"I am Pete","bool":true,"byte":127,"int":-237823,"long":-27823
                                                  //| 4263,"bigInt":"265876257682376587365863876528756875682765252520577305007209
                                                  //| 857025728132213242","float":1.3672322034835815,"double":1.3672322350005,"bi
                                                  //| gDec":"23761247614876823746.23846749182408","dateTime":"2013-01-21T10:23:47
                                                  //| .161+01:00","uuid":"68d0264b-d056-41ba-babe-3a2ad12c19d5"},"primitiveListMA
                                                  //| s":{"riftdesc":"riftwarp.PrimitiveListMAs","listString":["alpha","beta","ga
                                                  //| mma","delta"],"listInt":[1,2,3,4,5,6,7,8,9,10],"listDouble":[1.0,0.5,0.2,0.
                                                  //| 125],"listBigDecimal":["1.333333","1.33333335","1.6666666","1.6666667"],"li
                                                  //| stDateTime":["2013-01-21T11:23:47.251+01:00","2013-01-21T12:23:47.251+01:00
                                                  //| ","2013-01-21T13:23:47.251+01:00","2013-01-21T14:23:47.251+01:00"]},"primit
                                                  //| iveVectorMAs":{"riftdesc":"riftwarp.PrimitiveVectorMAs","vectorString":["al
                                                  //| pha","beta","gamma","delta"],"vectorInt":[1,2,3,4,5,6,7,8,9,10],"vectorDoub
                                                  //| le":[1.0,0.5,0.2,0.125],"vectorBigDecimal":["1.333333","1.33333335","1.6666
                                                  //| 666","1.6666667"],"vectorDateTime":["2013-01-21T11:23:47.251+01:00","2013-0
                                                  //| 1-21T12:23:47.251+01:00","2013-01-21T13:23:47.251+01:00","2013-01-21T14:23:
                                                  //| 47.251+01:00"]},"primitiveSetMAs":{"riftdesc":"riftwarp.PrimitiveSetMAs","s
                                                  //| etString":["alpha","beta","gamma","delta"],"setInt":[6,8,3,2,9,5,10,1,7,4],
                                                  //| "setDouble":[1.0,0.5,0.2,0.125],"setBigDecimal":["1.333333","1.33333335","1
                                                  //| .6666666","1.6666667"],"setDateTime":null},"primitiveIterableMAs":{"riftdes
                                                  //| c":"riftwarp.PrimitiveIterableMAs","iterableString":["alpha","beta","gamma"
                                                  //| ,"delta"],"iterableInt":[1,2,3,4,5,6,7,8,9,10],"iterableDouble":[1.0,0.5,0.
                                                  //| 2,0.125],"iterableBigDecimal":["1.333333","1.33333335","1.6666666","1.66666
                                                  //| 67"],"iterableDateTime":["2013-01-21T11:23:47.261+01:00","2013-01-21T12:23:
                                                  //| 47.261+01:00","2013-01-21T13:23:47.261+01:00","2013-01-21T14:23:47.261+01:0
                                                  //| 0"]},"complexMAs":{"riftdesc":"riftwarp.ComplexMAs","addresses1":[{"riftdes
                                                  //| c":"riftwarp.TestAddress","city":"Hamburg","street":"Am Hafen"},{"riftdesc"
                                                  //| :"riftwarp.TestAddress","city":"New York","street":"Broadway"},{"riftdesc":
                                                  //| "riftwarp.TestAddress","city":"Los Angeles ","street":"Sunset Boulevard"}],
                                                  //| "addresses2":[{"riftdesc":"riftwarp.TestAddress","city":"Hamburg","street":
                                                  //| "Am Hafen"},{"riftdesc":"riftwarp.TestAddress","city":"New York","street":"
                                                  //| Broadway"},{"riftdesc":"riftwarp.TestAddress","city":"Los Angeles ","street
                                                  //| ":"Sunset Boulevard"}],"addresses3":[{"riftdesc":"riftwarp.TestAddress","ci
                                                  //| ty":"Hamburg","street":"Am Hafen"},{"riftdesc":"riftwarp.TestAddress","city
                                                  //| ":"New York","street":"Broadway"},{"riftdesc":"riftwarp.TestAddress","city"
                                                  //| :"Los Angeles ","street":"Sunset Boulevard"}],"anything":[true,"hello",1,2,
                                                  //| 3.0,3.0,{"riftdesc":"riftwarp.TestAddress","city":"Somewhere","street":"her
                                                  //| e"}]},"primitiveMaps":{"riftdesc":"riftwarp.PrimitiveMaps","mapIntInt":[{"k
                                                  //| ":1,"v":10},{"k":2,"v":20},{"k":3,"v":30},{"k":4,"v":40}],"mapStringInt":[{
                                                  //| "k":"a","v":1},{"k":"b","v":2},{"k":"c","v":3}],"mapUuidDateTime":[{"k":"d1
                                                  //| 1497de-f512-45c3-b5b4-d935647a4f8c","v":"2013-01-21T10:23:47.261+01:00"},{"
                                                  //| k":"1f43b794-6781-446f-bc72-cd93189d771d","v":"2013-01-22T10:23:47.261+01:0
                                                  //| 0"},{"k":"52e01510-9555-4136-a0e6-d0c83870f134","v":"2013-01-23T10:23:47.26
                                                  //| 1+01:00"}]},"complexMaps":{"riftdesc":"riftwarp.ComplexMaps","mapIntTestAdd
                                                  //| ress1":[{"k":0,"v":{"riftdesc":"riftwarp.TestAddress","city":"Hamburg","str
                                                  //| eet":"Am Hafen"}},{"k":1,"v":{"riftdesc":"riftwarp.TestAddress","city":"New
                                                  //|  York","street":"Broadway"}},{"k":2,"v":{"riftdesc":"riftwarp.TestAddress",
                                                  //| "city":"Los Angeles ","street":"Sunset Boulevard"}}],"mapIntAny":[{"k":0,"v
                                                  //| ":{"riftdesc":"riftwarp.TestAddress","city":"Hamburg","street":"Am Hafen"}}
                                                  //| ,{"k":1,"v":{"riftdesc":"riftwarp.TestAddress","city":"New York","street":"
                                                  //| Broadway"}},{"k":2,"v":{"riftdesc":"riftwarp.TestAddress","city":"Los Angel
                                                  //| es ","street":"Sunset Boulevard"}}],"mapStringAnyWithUnknown":[{"k":"x","v"
                                                  //| :"d1c5bdd2-a26c-4a55-ad9e-42d15acc4405"},{"k":"unspecifiedProblem","v":{"ri
                                                  //| ftdesc":"almhirt.common.UnspecifiedProblem","message":"Test","severity":"Ma
                                                  //| jor","category":"SystemProblem","args":[{"k":"arg1","v":95}]}},{"k":"y","v"
                                                  //| :"68f40614-102b-4c12-9a6d-ea8636b0a1d4"},{"k":"1","v":{"riftdesc":"riftwarp
                                                  //| .TestAddress","city":"New York","street":"Broadway"}},{"k":"0","v":{"riftde
                                                  //| sc":"riftwarp.TestAddress","city":"Hamburg","street":"Am Hafen"}},{"k":"2",
                                                  //| "v":{"riftdesc":"riftwarp.TestAddress","city":"Los Angeles ","street":"Suns
                                                  //| et Boulevard"}},{"k":"z","v":"2013-01-21T10:23:47.281+01:00"}]},"addressOpt
                                                  //| ":{"riftdesc":"riftwarp.TestAddress","city":"Berlin","street":"At the wall 
                                                  //| 89"}}))
 
      
  val backFromWarpV = riftWarp.receiveFromWarpWithBlobs[DimensionString, TestObjectA](blobFetch)(RiftJson())(warpStreamV.forceResult)
                                                  //> backFromWarpV  : almhirt.common.AlmValidation[riftwarp.TestObjectA] = Succe
                                                  //| ss(TestObjectA([B@56c562fa,[B@2a08904c,PrimitiveTypes(I am Pete,true,127,-2
                                                  //| 37823,-278234263,2658762576823765873658638765287568756827652525205773050072
                                                  //| 09857025728132213242,1.3672322,1.3672322350005,23761247614876823746.2384674
                                                  //| 9182408,2013-01-21T10:23:47.161+01:00,68d0264b-d056-41ba-babe-3a2ad12c19d5)
                                                  //| ,PrimitiveListMAs(List(alpha, beta, gamma, delta),List(1, 2, 3, 4, 5, 6, 7,
                                                  //|  8, 9, 10),List(1.0, 0.5, 0.2, 0.125),List(1.333333, 1.33333335, 1.6666666,
                                                  //|  1.6666667),List(2013-01-21T11:23:47.251+01:00, 2013-01-21T12:23:47.251+01:
                                                  //| 00, 2013-01-21T13:23:47.251+01:00, 2013-01-21T14:23:47.251+01:00)),Primitiv
                                                  //| eVectorMAs(Vector(alpha, beta, gamma, delta),Vector(1, 2, 3, 4, 5, 6, 7, 8,
                                                  //|  9, 10),Vector(1.0, 0.5, 0.2, 0.125),Vector(1.333333, 1.33333335, 1.6666666
                                                  //| , 1.6666667),Vector(2013-01-21T11:23:47.251+01:00, 2013-01-21T12:23:47.251+
                                                  //| 01:00, 2013-01-21T13:23:47.251+01:00, 2013-01-21T14:23:47.251+01:00)),Some(
                                                  //| PrimitiveSetMAs(Set(alpha, beta, gamma, delta),Set(5, 10, 1, 6, 9, 2, 7, 3,
                                                  //|  8, 4),Set(1.0, 0.5, 0.2, 0.125),Set(1.333333, 1.33333335, 1.6666666, 1.666
                                                  //| 6667),None)),PrimitiveIterableMAs(Set(alpha, beta, gamma, delta),Set(5, 10,
                                                  //|  1, 6, 9, 2, 7, 3, 8, 4),Set(1.0, 0.5, 0.2, 0.125),Set(1.333333, 1.33333335
                                                  //| , 1.6666666, 1.6666667),Set(2013-01-21T11:23:47.261+01:00, 2013-01-21T12:23
                                                  //| :47.261+01:00, 2013-01-21T13:23:47.261+01:00, 2013-01-21T14:23:47.261+01:00
                                                  //| )),ComplexMAs(List(TestAddress(Hamburg,Am Hafen), TestAddress(New York,Broa
                                                  //| dway), TestAddress(Los Angeles ,Sunset Boulevard)),Vector(TestAddress(Hambu
                                                  //| rg,Am Hafen), TestAddress(New York,Broadway), TestAddress(Los Angeles ,Suns
                                                  //| et Boulevard)),Set(TestAddress(Hamburg,Am Hafen), TestAddress(New York,Broa
                                                  //| dway), TestAddress(Los Angeles ,Sunset Boulevard)),List(true, hello, 1.0, 2
                                                  //| .0, 3.0, 3.0, TestAddress(Somewhere,here))),PrimitiveMaps(Map(1 -> 10, 2 ->
                                                  //|  20, 3 -> 30, 4 -> 40),Map(a -> 1, b -> 2, c -> 3),Map(d11497de-f512-45c3-b
                                                  //| 5b4-d935647a4f8c -> 2013-01-21T10:23:47.261+01:00, 1f43b794-6781-446f-bc72-
                                                  //| cd93189d771d -> 2013-01-22T10:23:47.261+01:00, 52e01510-9555-4136-a0e6-d0c8
                                                  //| 3870f134 -> 2013-01-23T10:23:47.261+01:00)),ComplexMaps(Map(0 -> TestAddres
                                                  //| s(Hamburg,Am Hafen), 1 -> TestAddress(New York,Broadway), 2 -> TestAddress(
                                                  //| Los Angeles ,Sunset Boulevard)),Map(0 -> TestAddress(Hamburg,Am Hafen), 1 -
                                                  //| > TestAddress(New York,Broadway), 2 -> TestAddress(Los Angeles ,Sunset Boul
                                                  //| evard)),Map(x -> d1c5bdd2-a26c-4a55-ad9e-42d15acc4405, unspecifiedProblem -
                                                  //| > almhirt.common.UnspecifiedProblem
                                                  //| Test
                                                  //| Category: SystemProblem
                                                  //| Severity: Major
                                                  //| Arguments: Map(arg1 -> 95.0)
                                                  //| , y -> 68f40614-102b-4c12-9a6d-ea8636b0a1d4, 1 -> TestAddress(New York,Broa
                                                  //| dway), 0 -> TestAddress(Hamburg,Am Hafen), 2 -> TestAddress(Los Angeles ,Su
                                                  //| nset Boulevard), z -> 2013-01-21T10:23:47.281+01:00)),Some(TestAddress(Berl
                                                  //| in,At the wall 89))))
 
  
  val backFromWarp = backFromWarpV.forceResult    //> backFromWarp  : riftwarp.TestObjectA = TestObjectA([B@56c562fa,[B@2a08904c,
                                                  //| PrimitiveTypes(I am Pete,true,127,-237823,-278234263,2658762576823765873658
                                                  //| 63876528756875682765252520577305007209857025728132213242,1.3672322,1.367232
                                                  //| 2350005,23761247614876823746.23846749182408,2013-01-21T10:23:47.161+01:00,6
                                                  //| 8d0264b-d056-41ba-babe-3a2ad12c19d5),PrimitiveListMAs(List(alpha, beta, gam
                                                  //| ma, delta),List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),List(1.0, 0.5, 0.2, 0.125),L
                                                  //| ist(1.333333, 1.33333335, 1.6666666, 1.6666667),List(2013-01-21T11:23:47.25
                                                  //| 1+01:00, 2013-01-21T12:23:47.251+01:00, 2013-01-21T13:23:47.251+01:00, 2013
                                                  //| -01-21T14:23:47.251+01:00)),PrimitiveVectorMAs(Vector(alpha, beta, gamma, d
                                                  //| elta),Vector(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),Vector(1.0, 0.5, 0.2, 0.125),Ve
                                                  //| ctor(1.333333, 1.33333335, 1.6666666, 1.6666667),Vector(2013-01-21T11:23:47
                                                  //| .251+01:00, 2013-01-21T12:23:47.251+01:00, 2013-01-21T13:23:47.251+01:00, 2
                                                  //| 013-01-21T14:23:47.251+01:00)),Some(PrimitiveSetMAs(Set(alpha, beta, gamma,
                                                  //|  delta),Set(5, 10, 1, 6, 9, 2, 7, 3, 8, 4),Set(1.0, 0.5, 0.2, 0.125),Set(1.
                                                  //| 333333, 1.33333335, 1.6666666, 1.6666667),None)),PrimitiveIterableMAs(Set(a
                                                  //| lpha, beta, gamma, delta),Set(5, 10, 1, 6, 9, 2, 7, 3, 8, 4),Set(1.0, 0.5, 
                                                  //| 0.2, 0.125),Set(1.333333, 1.33333335, 1.6666666, 1.6666667),Set(2013-01-21T
                                                  //| 11:23:47.261+01:00, 2013-01-21T12:23:47.261+01:00, 2013-01-21T13:23:47.261+
                                                  //| 01:00, 2013-01-21T14:23:47.261+01:00)),ComplexMAs(List(TestAddress(Hamburg,
                                                  //| Am Hafen), TestAddress(New York,Broadway), TestAddress(Los Angeles ,Sunset 
                                                  //| Boulevard)),Vector(TestAddress(Hamburg,Am Hafen), TestAddress(New York,Broa
                                                  //| dway), TestAddress(Los Angeles ,Sunset Boulevard)),Set(TestAddress(Hamburg,
                                                  //| Am Hafen), TestAddress(New York,Broadway), TestAddress(Los Angeles ,Sunset 
                                                  //| Boulevard)),List(true, hello, 1.0, 2.0, 3.0, 3.0, TestAddress(Somewhere,her
                                                  //| e))),PrimitiveMaps(Map(1 -> 10, 2 -> 20, 3 -> 30, 4 -> 40),Map(a -> 1, b ->
                                                  //|  2, c -> 3),Map(d11497de-f512-45c3-b5b4-d935647a4f8c -> 2013-01-21T10:23:47
                                                  //| .261+01:00, 1f43b794-6781-446f-bc72-cd93189d771d -> 2013-01-22T10:23:47.261
                                                  //| +01:00, 52e01510-9555-4136-a0e6-d0c83870f134 -> 2013-01-23T10:23:47.261+01:
                                                  //| 00)),ComplexMaps(Map(0 -> TestAddress(Hamburg,Am Hafen), 1 -> TestAddress(N
                                                  //| ew York,Broadway), 2 -> TestAddress(Los Angeles ,Sunset Boulevard)),Map(0 -
                                                  //| > TestAddress(Hamburg,Am Hafen), 1 -> TestAddress(New York,Broadway), 2 -> 
                                                  //| TestAddress(Los Angeles ,Sunset Boulevard)),Map(x -> d1c5bdd2-a26c-4a55-ad9
                                                  //| e-42d15acc4405, unspecifiedProblem -> almhirt.common.UnspecifiedProblem
                                                  //| Test
                                                  //| Category: SystemProblem
                                                  //| Severity: Major
                                                  //| Arguments: Map(arg1 -> 95.0)
                                                  //| , y -> 68f40614-102b-4c12-9a6d-ea8636b0a1d4, 1 -> TestAddress(New York,Broa
                                                  //| dway), 0 -> TestAddress(Hamburg,Am Hafen), 2 -> TestAddress(Los Angeles ,Su
                                                  //| nset Boulevard), z -> 2013-01-21T10:23:47.281+01:00)),Some(TestAddress(Berl
                                                  //| in,At the wall 89)))

  testObject == backFromWarp                      //> res0: Boolean = false


  riftWarp.prepareForWarp[DimensionCord](RiftJson())(testObject).flatMap(warpStream =>
    riftWarp.receiveFromWarp[DimensionCord, TestObjectA](RiftJson())(warpStream)).map(rearrived =>
    rearrived == testObject)                      //> res1: scalaz.Validation[almhirt.common.Problem,Boolean] = Success(false)


  riftWarp.prepareForWarp[DimensionRawMap](RiftMap())(testObject).flatMap(warpStream =>
    riftWarp.receiveFromWarp[DimensionRawMap, TestObjectA](RiftMap())(warpStream)).map(rearrived =>
    rearrived == testObject)                      //> res2: scalaz.Validation[almhirt.common.Problem,Boolean] = Failure(almhirt.c
                                                  //| ommon.AggregateProblem
                                                  //| One or more problems occured. See problems.
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| Aggregated problems:
                                                  //| Problem 0:
                                                  //| almhirt.common.KeyNotFoundProblem
                                                  //| No Decomposer found for  riftwarp.UnknownObject
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )


}