package riftwarp.worksheets

import scalaz.syntax.validation._
import almhirt.syntax.almvalidation._
import riftwarp._
import riftwarp.impl._

object Worksheet {
  val riftWarp = RiftWarp.concurrentWithDefaults  //> riftWarp  : riftwarp.RiftWarp = riftwarp.RiftWarp$$anon$1@d8cd562
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

  val testObject = TestObjectA.pete               //> testObject  : riftwarp.TestObjectA = TestObjectA([B@4bfbd4eb,[B@257b746,Pri
                                                  //| mitiveTypes(I am Pete,true,127,-237823,-278234263,2658762576823765873658638
                                                  //| 76528756875682765252520577305007209857025728132213242,1.3672322,1.367232235
                                                  //| 0005,23761247614876823746.23846749182408,2013-01-09T16:08:21.290+01:00,c942
                                                  //| fcac-24e5-4dc0-b115-8b7dbc727343),PrimitiveListMAs(List(alpha, beta, gamma,
                                                  //|  delta),List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),List(1.0, 0.5, 0.2, 0.125),List
                                                  //| (1.333333, 1.33333335, 1.6666666, 1.6666667),List(2013-01-09T17:08:21.384+0
                                                  //| 1:00, 2013-01-09T18:08:21.384+01:00, 2013-01-09T19:08:21.384+01:00, 2013-01
                                                  //| -09T20:08:21.384+01:00)),PrimitiveVectorMAs(Vector(alpha, beta, gamma, delt
                                                  //| a),Vector(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),Vector(1.0, 0.5, 0.2, 0.125),Vecto
                                                  //| r(1.333333, 1.33333335, 1.6666666, 1.6666667),Vector(2013-01-09T17:08:21.38
                                                  //| 4+01:00, 2013-01-09T18:08:21.384+01:00, 2013-01-09T19:08:21.384+01:00, 2013
                                                  //| -01-09T20:08:21.384+01:00)),Some(PrimitiveSetMAs(Set(alpha, beta, gamma, de
                                                  //| lta),Set(5, 10, 1, 6, 9, 2, 7, 3, 8, 4),Set(1.0, 0.5, 0.2, 0.125),Set(1.333
                                                  //| 333, 1.33333335, 1.6666666, 1.6666667),None)),PrimitiveIterableMAs(List(alp
                                                  //| ha, beta, gamma, delta),List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),List(1.0, 0.5, 
                                                  //| 0.2, 0.125),List(1.333333, 1.33333335, 1.6666666, 1.6666667),List(2013-01-0
                                                  //| 9T17:08:21.384+01:00, 2013-01-09T18:08:21.384+01:00, 2013-01-09T19:08:21.38
                                                  //| 4+01:00, 2013-01-09T20:08:21.384+01:00)),ComplexMAs(List(TestAddress(Hambur
                                                  //| g,Am Hafen), TestAddress(New York,Broadway), TestAddress(Los Angeles ,Sunse
                                                  //| t Boulevard)),Vector(TestAddress(Hamburg,Am Hafen), TestAddress(New York,Br
                                                  //| oadway), TestAddress(Los Angeles ,Sunset Boulevard)),Set(TestAddress(Hambur
                                                  //| g,Am Hafen), TestAddress(New York,Broadway), TestAddress(Los Angeles ,Sunse
                                                  //| t Boulevard)),List(true, hello, 1, 2, 3.0, 3.0, TestAddress(Somewhere,here)
                                                  //| )),PrimitiveMaps(Map(1 -> 10, 2 -> 20, 3 -> 30, 4 -> 40),Map(a -> 1, b -> 2
                                                  //| , c -> 3),Map(d5756846-723b-431d-a92b-15ba7b3d488e -> 2013-01-09T16:08:21.3
                                                  //| 84+01:00, f3132893-2005-4e8d-876d-a3ecae55a3bd -> 2013-01-10T16:08:21.384+0
                                                  //| 1:00, f434b7f6-b9df-41e3-940a-9b8faad61d0d -> 2013-01-11T16:08:21.384+01:00
                                                  //| )),ComplexMaps(Map(0 -> TestAddress(Hamburg,Am Hafen), 1 -> TestAddress(New
                                                  //|  York,Broadway), 2 -> TestAddress(Los Angeles ,Sunset Boulevard)),Map(0 -> 
                                                  //| TestAddress(Hamburg,Am Hafen), 1 -> TestAddress(New York,Broadway), 2 -> Te
                                                  //| stAddress(Los Angeles ,Sunset Boulevard)),Map(x -> 975e9d80-5760-4ca1-8c35-
                                                  //| 934549d3ddee, unspecifiedProblem -> almhirt.common.UnspecifiedProblem
                                                  //| Test
                                                  //| Category: SystemProblem
                                                  //| Severity: Major
                                                  //| Arguments: Map(arg1 -> 95)
                                                  //| , y -> 90d854a3-3e17-49de-a745-393261fffb11, 1 -> TestAddress(New York,Broa
                                                  //| dway), 0 -> TestAddress(Hamburg,Am Hafen), 2 -> TestAddress(Los Angeles ,Su
                                                  //| nset Boulevard), unknownType -> UnknownObject(1), z -> 2013-01-09T16:08:21.
                                                  //| 399+01:00)),Some(TestAddress(Berlin,At the wall 89)))
 
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
                                                  //| cess(DimensionString({"riftwarptd":"riftwarp.TestObjectA","arrayByte":[126,
                                                  //| -123,12,-45,-128],"blob":{"riftwarptd":"RiftBlobRefByName","name":"e17d8c38
                                                  //| -c7c9-441c-8e18-92710a15a5f4"},"primitiveTypes":{"riftwarptd":"riftwarp.Pri
                                                  //| mitiveTypes","str":"I am Pete","bool":true,"byte":127,"int":-237823,"long":
                                                  //| -278234263,"bigInt":"265876257682376587365863876528756875682765252520577305
                                                  //| 007209857025728132213242","float":1.3672322034835815,"double":1.36723223500
                                                  //| 05,"bigDec":"23761247614876823746.23846749182408","dateTime":"2013-01-09T16
                                                  //| :08:21.290+01:00","uuid":"c942fcac-24e5-4dc0-b115-8b7dbc727343"},"primitive
                                                  //| ListMAs":{"riftwarptd":"riftwarp.PrimitiveListMAs","listString":["alpha","b
                                                  //| eta","gamma","delta"],"listInt":[1,2,3,4,5,6,7,8,9,10],"listDouble":[1.0,0.
                                                  //| 5,0.2,0.125],"listBigDecimal":["1.333333","1.33333335","1.6666666","1.66666
                                                  //| 67"],"listDateTime":["2013-01-09T17:08:21.384+01:00","2013-01-09T18:08:21.3
                                                  //| 84+01:00","2013-01-09T19:08:21.384+01:00","2013-01-09T20:08:21.384+01:00"]}
                                                  //| ,"primitiveVectorMAs":{"riftwarptd":"riftwarp.PrimitiveVectorMAs","vectorSt
                                                  //| ring":["alpha","beta","gamma","delta"],"vectorInt":[1,2,3,4,5,6,7,8,9,10],"
                                                  //| vectorDouble":[1.0,0.5,0.2,0.125],"vectorBigDecimal":["1.333333","1.3333333
                                                  //| 5","1.6666666","1.6666667"],"vectorDateTime":["2013-01-09T17:08:21.384+01:0
                                                  //| 0","2013-01-09T18:08:21.384+01:00","2013-01-09T19:08:21.384+01:00","2013-01
                                                  //| -09T20:08:21.384+01:00"]},"primitiveSetMAs":{"riftwarptd":"riftwarp.Primiti
                                                  //| veSetMAs","setString":["alpha","beta","gamma","delta"],"setInt":[6,5,4,3,10
                                                  //| ,7,8,2,1,9],"setDouble":[1.0,0.5,0.2,0.125],"setBigDecimal":["1.333333","1.
                                                  //| 33333335","1.6666666","1.6666667"],"setDateTime":null},"primitiveIterableMA
                                                  //| s":{"riftwarptd":"riftwarp.PrimitiveIterableMAs","iterableString":["alpha",
                                                  //| "beta","gamma","delta"],"iterableInt":[1,2,3,4,5,6,7,8,9,10],"iterableDoubl
                                                  //| e":[1.0,0.5,0.2,0.125],"iterableBigDecimal":["1.333333","1.33333335","1.666
                                                  //| 6666","1.6666667"],"iterableDateTime":["2013-01-09T17:08:21.384+01:00","201
                                                  //| 3-01-09T18:08:21.384+01:00","2013-01-09T19:08:21.384+01:00","2013-01-09T20:
                                                  //| 08:21.384+01:00"]},"complexMAs":{"riftwarptd":"riftwarp.ComplexMAs","addres
                                                  //| ses1":[{"riftwarptd":"riftwarp.TestAddress","city":"Hamburg","street":"Am H
                                                  //| afen"},{"riftwarptd":"riftwarp.TestAddress","city":"New York","street":"Bro
                                                  //| adway"},{"riftwarptd":"riftwarp.TestAddress","city":"Los Angeles ","street"
                                                  //| :"Sunset Boulevard"}],"addresses2":[{"riftwarptd":"riftwarp.TestAddress","c
                                                  //| ity":"Hamburg","street":"Am Hafen"},{"riftwarptd":"riftwarp.TestAddress","c
                                                  //| ity":"New York","street":"Broadway"},{"riftwarptd":"riftwarp.TestAddress","
                                                  //| city":"Los Angeles ","street":"Sunset Boulevard"}],"addresses3":[{"riftwarp
                                                  //| td":"riftwarp.TestAddress","city":"Hamburg","street":"Am Hafen"},{"riftwarp
                                                  //| td":"riftwarp.TestAddress","city":"New York","street":"Broadway"},{"riftwar
                                                  //| ptd":"riftwarp.TestAddress","city":"Los Angeles ","street":"Sunset Boulevar
                                                  //| d"}],"anything":[true,"hello",1,2,3.0,3.0,{"riftwarptd":"riftwarp.TestAddre
                                                  //| ss","city":"Somewhere","street":"here"}]},"primitiveMaps":{"riftwarptd":"ri
                                                  //| ftwarp.PrimitiveMaps","mapIntInt":[{"k":1,"v":10},{"k":2,"v":20},{"k":3,"v"
                                                  //| :30},{"k":4,"v":40}],"mapStringInt":[{"k":"a","v":1},{"k":"b","v":2},{"k":"
                                                  //| c","v":3}],"mapUuidDateTime":[{"k":"d5756846-723b-431d-a92b-15ba7b3d488e","
                                                  //| v":"2013-01-09T16:08:21.384+01:00"},{"k":"f3132893-2005-4e8d-876d-a3ecae55a
                                                  //| 3bd","v":"2013-01-10T16:08:21.384+01:00"},{"k":"f434b7f6-b9df-41e3-940a-9b8
                                                  //| faad61d0d","v":"2013-01-11T16:08:21.384+01:00"}]},"complexMaps":{"riftwarpt
                                                  //| d":"riftwarp.ComplexMaps","mapIntTestAddress1":[{"k":0,"v":{"riftwarptd":"r
                                                  //| iftwarp.TestAddress","city":"Hamburg","street":"Am Hafen"}},{"k":1,"v":{"ri
                                                  //| ftwarptd":"riftwarp.TestAddress","city":"New York","street":"Broadway"}},{"
                                                  //| k":2,"v":{"riftwarptd":"riftwarp.TestAddress","city":"Los Angeles ","street
                                                  //| ":"Sunset Boulevard"}}],"mapIntAny":[{"k":0,"v":{"riftwarptd":"riftwarp.Tes
                                                  //| tAddress","city":"Hamburg","street":"Am Hafen"}},{"k":1,"v":{"riftwarptd":"
                                                  //| riftwarp.TestAddress","city":"New York","street":"Broadway"}},{"k":2,"v":{"
                                                  //| riftwarptd":"riftwarp.TestAddress","city":"Los Angeles ","street":"Sunset B
                                                  //| oulevard"}}],"mapStringAnyWithUnknown":[{"k":"x","v":"975e9d80-5760-4ca1-8c
                                                  //| 35-934549d3ddee"},{"k":"unspecifiedProblem","v":{"riftwarptd":"almhirt.comm
                                                  //| on.UnspecifiedProblem","message":"Test","severity":"Major","category":"Syst
                                                  //| emProblem","args":[{"k":"arg1","v":95}]}},{"k":"y","v":"90d854a3-3e17-49de-
                                                  //| a745-393261fffb11"},{"k":"1","v":{"riftwarptd":"riftwarp.TestAddress","city
                                                  //| ":"New York","street":"Broadway"}},{"k":"0","v":{"riftwarptd":"riftwarp.Tes
                                                  //| tAddress","city":"Hamburg","street":"Am Hafen"}},{"k":"2","v":{"riftwarptd"
                                                  //| :"riftwarp.TestAddress","city":"Los Angeles ","street":"Sunset Boulevard"}}
                                                  //| ,{"k":"z","v":"2013-01-09T16:08:21.399+01:00"}]},"addressOpt":{"riftwarptd"
                                                  //| :"riftwarp.TestAddress","city":"Berlin","street":"At the wall 89"}}))
 
      
  val backFromWarpV = riftWarp.receiveFromWarpWithBlobs[DimensionString, TestObjectA](blobFetch)(RiftJson())(warpStreamV.forceResult)
                                                  //> backFromWarpV  : almhirt.common.AlmValidation[riftwarp.TestObjectA] = Succe
                                                  //| ss(TestObjectA([B@668c640e,[B@257b746,PrimitiveTypes(I am Pete,true,127,-23
                                                  //| 7823,-278234263,26587625768237658736586387652875687568276525252057730500720
                                                  //| 9857025728132213242,1.3672322,1.3672322350005,23761247614876823746.23846749
                                                  //| 182408,2013-01-09T16:08:21.290+01:00,c942fcac-24e5-4dc0-b115-8b7dbc727343),
                                                  //| PrimitiveListMAs(List(alpha, beta, gamma, delta),List(1, 2, 3, 4, 5, 6, 7, 
                                                  //| 8, 9, 10),List(1.0, 0.5, 0.2, 0.125),List(1.333333, 1.33333335, 1.6666666, 
                                                  //| 1.6666667),List(2013-01-09T17:08:21.384+01:00, 2013-01-09T18:08:21.384+01:0
                                                  //| 0, 2013-01-09T19:08:21.384+01:00, 2013-01-09T20:08:21.384+01:00)),Primitive
                                                  //| VectorMAs(Vector(alpha, beta, gamma, delta),Vector(1, 2, 3, 4, 5, 6, 7, 8, 
                                                  //| 9, 10),Vector(1.0, 0.5, 0.2, 0.125),Vector(1.333333, 1.33333335, 1.6666666,
                                                  //|  1.6666667),Vector(2013-01-09T17:08:21.384+01:00, 2013-01-09T18:08:21.384+0
                                                  //| 1:00, 2013-01-09T19:08:21.384+01:00, 2013-01-09T20:08:21.384+01:00)),Some(P
                                                  //| rimitiveSetMAs(Set(alpha, beta, gamma, delta),Set(5, 10, 1, 6, 9, 2, 7, 3, 
                                                  //| 8, 4),Set(1.0, 0.5, 0.2, 0.125),Set(1.333333, 1.33333335, 1.6666666, 1.6666
                                                  //| 667),None)),PrimitiveIterableMAs(Set(alpha, beta, gamma, delta),Set(5, 10, 
                                                  //| 1, 6, 9, 2, 7, 3, 8, 4),Set(1.0, 0.5, 0.2, 0.125),Set(1.333333, 1.33333335,
                                                  //|  1.6666666, 1.6666667),Set(2013-01-09T17:08:21.384+01:00, 2013-01-09T18:08:
                                                  //| 21.384+01:00, 2013-01-09T19:08:21.384+01:00, 2013-01-09T20:08:21.384+01:00)
                                                  //| ),ComplexMAs(List(TestAddress(Hamburg,Am Hafen), TestAddress(New York,Broad
                                                  //| way), TestAddress(Los Angeles ,Sunset Boulevard)),Vector(TestAddress(Hambur
                                                  //| g,Am Hafen), TestAddress(New York,Broadway), TestAddress(Los Angeles ,Sunse
                                                  //| t Boulevard)),Set(TestAddress(Hamburg,Am Hafen), TestAddress(New York,Broad
                                                  //| way), TestAddress(Los Angeles ,Sunset Boulevard)),List(true, hello, 1.0, 2.
                                                  //| 0, 3.0, 3.0, TestAddress(Somewhere,here))),PrimitiveMaps(Map(1 -> 10, 2 -> 
                                                  //| 20, 3 -> 30, 4 -> 40),Map(a -> 1, b -> 2, c -> 3),Map(d5756846-723b-431d-a9
                                                  //| 2b-15ba7b3d488e -> 2013-01-09T16:08:21.384+01:00, f3132893-2005-4e8d-876d-a
                                                  //| 3ecae55a3bd -> 2013-01-10T16:08:21.384+01:00, f434b7f6-b9df-41e3-940a-9b8fa
                                                  //| ad61d0d -> 2013-01-11T16:08:21.384+01:00)),ComplexMaps(Map(0 -> TestAddress
                                                  //| (Hamburg,Am Hafen), 1 -> TestAddress(New York,Broadway), 2 -> TestAddress(L
                                                  //| os Angeles ,Sunset Boulevard)),Map(0 -> TestAddress(Hamburg,Am Hafen), 1 ->
                                                  //|  TestAddress(New York,Broadway), 2 -> TestAddress(Los Angeles ,Sunset Boule
                                                  //| vard)),Map(x -> 975e9d80-5760-4ca1-8c35-934549d3ddee, unspecifiedProblem ->
                                                  //|  almhirt.common.UnspecifiedProblem
                                                  //| Test
                                                  //| Category: SystemProblem
                                                  //| Severity: Major
                                                  //| Arguments: Map(arg1 -> 95.0)
                                                  //| , y -> 90d854a3-3e17-49de-a745-393261fffb11, 1 -> TestAddress(New York,Broa
                                                  //| dway), 0 -> TestAddress(Hamburg,Am Hafen), 2 -> TestAddress(Los Angeles ,Su
                                                  //| nset Boulevard), z -> 2013-01-09T16:08:21.399+01:00)),Some(TestAddress(Berl
                                                  //| in,At the wall 89))))
 
  
  
  val backFromWarp = backFromWarpV.forceResult    //> backFromWarp  : riftwarp.TestObjectA = TestObjectA([B@668c640e,[B@257b746,P
                                                  //| rimitiveTypes(I am Pete,true,127,-237823,-278234263,26587625768237658736586
                                                  //| 3876528756875682765252520577305007209857025728132213242,1.3672322,1.3672322
                                                  //| 350005,23761247614876823746.23846749182408,2013-01-09T16:08:21.290+01:00,c9
                                                  //| 42fcac-24e5-4dc0-b115-8b7dbc727343),PrimitiveListMAs(List(alpha, beta, gamm
                                                  //| a, delta),List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),List(1.0, 0.5, 0.2, 0.125),Li
                                                  //| st(1.333333, 1.33333335, 1.6666666, 1.6666667),List(2013-01-09T17:08:21.384
                                                  //| +01:00, 2013-01-09T18:08:21.384+01:00, 2013-01-09T19:08:21.384+01:00, 2013-
                                                  //| 01-09T20:08:21.384+01:00)),PrimitiveVectorMAs(Vector(alpha, beta, gamma, de
                                                  //| lta),Vector(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),Vector(1.0, 0.5, 0.2, 0.125),Vec
                                                  //| tor(1.333333, 1.33333335, 1.6666666, 1.6666667),Vector(2013-01-09T17:08:21.
                                                  //| 384+01:00, 2013-01-09T18:08:21.384+01:00, 2013-01-09T19:08:21.384+01:00, 20
                                                  //| 13-01-09T20:08:21.384+01:00)),Some(PrimitiveSetMAs(Set(alpha, beta, gamma, 
                                                  //| delta),Set(5, 10, 1, 6, 9, 2, 7, 3, 8, 4),Set(1.0, 0.5, 0.2, 0.125),Set(1.3
                                                  //| 33333, 1.33333335, 1.6666666, 1.6666667),None)),PrimitiveIterableMAs(Set(al
                                                  //| pha, beta, gamma, delta),Set(5, 10, 1, 6, 9, 2, 7, 3, 8, 4),Set(1.0, 0.5, 0
                                                  //| .2, 0.125),Set(1.333333, 1.33333335, 1.6666666, 1.6666667),Set(2013-01-09T1
                                                  //| 7:08:21.384+01:00, 2013-01-09T18:08:21.384+01:00, 2013-01-09T19:08:21.384+0
                                                  //| 1:00, 2013-01-09T20:08:21.384+01:00)),ComplexMAs(List(TestAddress(Hamburg,A
                                                  //| m Hafen), TestAddress(New York,Broadway), TestAddress(Los Angeles ,Sunset B
                                                  //| oulevard)),Vector(TestAddress(Hamburg,Am Hafen), TestAddress(New York,Broad
                                                  //| way), TestAddress(Los Angeles ,Sunset Boulevard)),Set(TestAddress(Hamburg,A
                                                  //| m Hafen), TestAddress(New York,Broadway), TestAddress(Los Angeles ,Sunset B
                                                  //| oulevard)),List(true, hello, 1.0, 2.0, 3.0, 3.0, TestAddress(Somewhere,here
                                                  //| ))),PrimitiveMaps(Map(1 -> 10, 2 -> 20, 3 -> 30, 4 -> 40),Map(a -> 1, b -> 
                                                  //| 2, c -> 3),Map(d5756846-723b-431d-a92b-15ba7b3d488e -> 2013-01-09T16:08:21.
                                                  //| 384+01:00, f3132893-2005-4e8d-876d-a3ecae55a3bd -> 2013-01-10T16:08:21.384+
                                                  //| 01:00, f434b7f6-b9df-41e3-940a-9b8faad61d0d -> 2013-01-11T16:08:21.384+01:0
                                                  //| 0)),ComplexMaps(Map(0 -> TestAddress(Hamburg,Am Hafen), 1 -> TestAddress(Ne
                                                  //| w York,Broadway), 2 -> TestAddress(Los Angeles ,Sunset Boulevard)),Map(0 ->
                                                  //|  TestAddress(Hamburg,Am Hafen), 1 -> TestAddress(New York,Broadway), 2 -> T
                                                  //| estAddress(Los Angeles ,Sunset Boulevard)),Map(x -> 975e9d80-5760-4ca1-8c35
                                                  //| -934549d3ddee, unspecifiedProblem -> almhirt.common.UnspecifiedProblem
                                                  //| Test
                                                  //| Category: SystemProblem
                                                  //| Severity: Major
                                                  //| Arguments: Map(arg1 -> 95.0)
                                                  //| , y -> 90d854a3-3e17-49de-a745-393261fffb11, 1 -> TestAddress(New York,Broa
                                                  //| dway), 0 -> TestAddress(Hamburg,Am Hafen), 2 -> TestAddress(Los Angeles ,Su
                                                  //| nset Boulevard), z -> 2013-01-09T16:08:21.399+01:00)),Some(TestAddress(Berl
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
                                                  //| Category: SystemProblem
                                                  //| Severity: Major
                                                  //| Arguments: Map()
                                                  //| Aggregated problems:
                                                  //| Problem 0:
                                                  //| almhirt.common.UnspecifiedProblem
                                                  //| No decomposer found for type 'riftwarp.UnknownObject')
                                                  //| Category: SystemProblem
                                                  //| Severity: Major
                                                  //| Arguments: Map()
                                                  //| )


}