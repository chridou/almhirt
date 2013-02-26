package riftwarp.worksheets

import scalaz.syntax.validation._
import almhirt.syntax.almvalidation._
import riftwarp._
import riftwarp.impl._

object Worksheet {
  val riftWarp = RiftWarp.concurrentWithDefaults  //> riftWarp  : riftwarp.RiftWarp = riftwarp.RiftWarp$$anon$1@6d70c116
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



  val testObject = TestObjectA.pete               //> testObject  : riftwarp.TestObjectA = TestObjectA([B@77958338,[B@78da0edd,Pr
                                                  //| imitiveTypes(I am Pete,true,127,-237823,-278234263,265876257682376587365863
                                                  //| 876528756875682765252520577305007209857025728132213242,1.3675,1.36723223500
                                                  //| 05,23761247614876823746.23846749182408,2013-02-26T10:24:13.116+01:00,d09512
                                                  //| c0-ef3d-4968-bbf6-94123c523535),PrimitiveListMAs(List(alpha, beta, gamma, d
                                                  //| elta),List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),List(1.0, 0.5, 0.2, 0.125),List(1
                                                  //| .333333, 1.33333335, 1.6666666, 1.6666667),List(2013-02-26T11:24:13.178+01:
                                                  //| 00, 2013-02-26T12:24:13.178+01:00, 2013-02-26T13:24:13.178+01:00, 2013-02-2
                                                  //| 6T14:24:13.178+01:00)),PrimitiveVectorMAs(Vector(alpha, beta, gamma, delta)
                                                  //| ,Vector(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),Vector(1.0, 0.5, 0.2, 0.125),Vector(
                                                  //| 1.333333, 1.33333335, 1.6666666, 1.6666667),Vector(2013-02-26T11:24:13.178+
                                                  //| 01:00, 2013-02-26T12:24:13.178+01:00, 2013-02-26T13:24:13.178+01:00, 2013-0
                                                  //| 2-26T14:24:13.178+01:00)),Some(PrimitiveSetMAs(Set(alpha, beta, gamma, delt
                                                  //| a),Set(5, 10, 1, 6, 9, 2, 7, 3, 8, 4),Set(1.0, 0.5, 0.2, 0.125),Set(1.33333
                                                  //| 3, 1.33333335, 1.6666666, 1.6666667),None)),PrimitiveIterableMAs(List(alpha
                                                  //| , beta, gamma, delta),List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),List(1.0, 0.5, 0.
                                                  //| 2, 0.125),List(1.333333, 1.33333335, 1.6666666, 1.6666667),List(2013-02-26T
                                                  //| 11:24:13.178+01:00, 2013-02-26T12:24:13.178+01:00, 2013-02-26T13:24:13.178+
                                                  //| 01:00, 2013-02-26T14:24:13.178+01:00)),ComplexMAs(List(TestAddress(Hamburg,
                                                  //| Am Hafen), TestAddress(New York,Broadway), TestAddress(Los Angeles ,Sunset 
                                                  //| Boulevard)),Vector(TestAddress(Hamburg,Am Hafen), TestAddress(New York,Broa
                                                  //| dway), TestAddress(Los Angeles ,Sunset Boulevard)),Set(TestAddress(Hamburg,
                                                  //| Am Hafen), TestAddress(New York,Broadway), TestAddress(Los Angeles ,Sunset 
                                                  //| Boulevard)),List(true, hello, 1, 2, 3.0, 3.0, TestAddress(Somewhere,here)))
                                                  //| ,PrimitiveMaps(Map(1 -> 10, 2 -> 20, 3 -> 30, 4 -> 40),Map(a -> 1, b -> 2, 
                                                  //| c -> 3),Map(c6c0c1b8-2e6e-43e3-88bf-9aa66b270877 -> 2013-02-26T10:24:13.194
                                                  //| +01:00, dd53e139-8595-4d08-874a-5e87cee42ed4 -> 2013-02-27T10:24:13.194+01:
                                                  //| 00, b58cf6d8-2022-447f-a4fe-5474df57b5c4 -> 2013-02-28T10:24:13.194+01:00))
                                                  //| ,ComplexMaps(Map(0 -> TestAddress(Hamburg,Am Hafen), 1 -> TestAddress(New Y
                                                  //| ork,Broadway), 2 -> TestAddress(Los Angeles ,Sunset Boulevard)),Map(0 -> Te
                                                  //| stAddress(Hamburg,Am Hafen), 1 -> TestAddress(New York,Broadway), 2 -> Test
                                                  //| Address(Los Angeles ,Sunset Boulevard)),Map(x -> f8a6a42d-a76d-48a9-8226-84
                                                  //| bd33a51587, unspecifiedProblem -> almhirt.common.UnspecifiedProblem
                                                  //| Test
                                                  //| Category: SystemProblem
                                                  //| Severity: Major
                                                  //| Arguments: Map(arg1 -> 95)
                                                  //| , y -> aea0cf6e-cef0-40ac-9527-518257529473, 1 -> TestAddress(New York,Broa
                                                  //| dway), 0 -> TestAddress(Hamburg,Am Hafen), 2 -> TestAddress(Los Angeles ,Su
                                                  //| nset Boulevard), unknownType -> UnknownObject(1), z -> 2013-02-26T10:24:13.
                                                  //| 209+01:00)),Some(TestAddress(Berlin,At the wall 89)))
 
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
                                                  //| cess(DimensionString({"riftdesc":{"identifier":"riftwarp.TestObjectA","vers
                                                  //| ion":null},"arrayByte":[126,-123,12,-45,-128],"blob":{"riftdesc":{"identifi
                                                  //| er":"RiftBlobRefByName","version":null},"name":"511d4cd4-427b-4792-9c61-b7f
                                                  //| 5d45804fa"},"primitiveTypes":{"riftdesc":{"identifier":"riftwarp.PrimitiveT
                                                  //| ypes","version":null},"str":"I am Pete","bool":true,"byte":127,"int":-23782
                                                  //| 3,"long":-278234263,"bigInt":"265876257682376587365863876528756875682765252
                                                  //| 520577305007209857025728132213242","float":1.3674999475479126,"double":1.36
                                                  //| 72322350005,"bigDec":"23761247614876823746.23846749182408","dateTime":"2013
                                                  //| -02-26T10:24:13.116+01:00","uuid":"d09512c0-ef3d-4968-bbf6-94123c523535"},"
                                                  //| primitiveListMAs":{"riftdesc":{"identifier":"riftwarp.PrimitiveListMAs","ve
                                                  //| rsion":null},"listString":["alpha","beta","gamma","delta"],"listInt":[1,2,3
                                                  //| ,4,5,6,7,8,9,10],"listDouble":[1.0,0.5,0.2,0.125],"listBigDecimal":["1.3333
                                                  //| 33","1.33333335","1.6666666","1.6666667"],"listDateTime":["2013-02-26T11:24
                                                  //| :13.178+01:00","2013-02-26T12:24:13.178+01:00","2013-02-26T13:24:13.178+01:
                                                  //| 00","2013-02-26T14:24:13.178+01:00"]},"primitiveVectorMAs":{"riftdesc":{"id
                                                  //| entifier":"riftwarp.PrimitiveVectorMAs","version":null},"vectorString":["al
                                                  //| pha","beta","gamma","delta"],"vectorInt":[1,2,3,4,5,6,7,8,9,10],"vectorDoub
                                                  //| le":[1.0,0.5,0.2,0.125],"vectorBigDecimal":["1.333333","1.33333335","1.6666
                                                  //| 666","1.6666667"],"vectorDateTime":["2013-02-26T11:24:13.178+01:00","2013-0
                                                  //| 2-26T12:24:13.178+01:00","2013-02-26T13:24:13.178+01:00","2013-02-26T14:24:
                                                  //| 13.178+01:00"]},"primitiveSetMAs":{"riftdesc":{"identifier":"riftwarp.Primi
                                                  //| tiveSetMAs","version":null},"setString":["alpha","beta","gamma","delta"],"s
                                                  //| etInt":[5,10,1,6,9,2,7,3,8,4],"setDouble":[1.0,0.5,0.2,0.125],"setBigDecima
                                                  //| l":["1.333333","1.33333335","1.6666666","1.6666667"],"setDateTime":null},"p
                                                  //| rimitiveIterableMAs":{"riftdesc":{"identifier":"riftwarp.PrimitiveIterableM
                                                  //| As","version":null},"iterableString":["alpha","beta","gamma","delta"],"iter
                                                  //| ableInt":[1,2,3,4,5,6,7,8,9,10],"iterableDouble":[1.0,0.5,0.2,0.125],"itera
                                                  //| bleBigDecimal":["1.333333","1.33333335","1.6666666","1.6666667"],"iterableD
                                                  //| ateTime":["2013-02-26T11:24:13.178+01:00","2013-02-26T12:24:13.178+01:00","
                                                  //| 2013-02-26T13:24:13.178+01:00","2013-02-26T14:24:13.178+01:00"]},"complexMA
                                                  //| s":{"riftdesc":{"identifier":"riftwarp.ComplexMAs","version":null},"address
                                                  //| es1":[{"riftdesc":{"identifier":"riftwarp.TestAddress","version":null},"cit
                                                  //| y":"Hamburg","street":"Am Hafen"},{"riftdesc":{"identifier":"riftwarp.TestA
                                                  //| ddress","version":null},"city":"New York","street":"Broadway"},{"riftdesc":
                                                  //| {"identifier":"riftwarp.TestAddress","version":null},"city":"Los Angeles ",
                                                  //| "street":"Sunset Boulevard"}],"addresses2":[{"riftdesc":{"identifier":"rift
                                                  //| warp.TestAddress","version":null},"city":"Hamburg","street":"Am Hafen"},{"r
                                                  //| iftdesc":{"identifier":"riftwarp.TestAddress","version":null},"city":"New Y
                                                  //| ork","street":"Broadway"},{"riftdesc":{"identifier":"riftwarp.TestAddress",
                                                  //| "version":null},"city":"Los Angeles ","street":"Sunset Boulevard"}],"addres
                                                  //| ses3":[{"riftdesc":{"identifier":"riftwarp.TestAddress","version":null},"ci
                                                  //| ty":"Hamburg","street":"Am Hafen"},{"riftdesc":{"identifier":"riftwarp.Test
                                                  //| Address","version":null},"city":"New York","street":"Broadway"},{"riftdesc"
                                                  //| :{"identifier":"riftwarp.TestAddress","version":null},"city":"Los Angeles "
                                                  //| ,"street":"Sunset Boulevard"}],"anything":[true,"hello",1,2,3.0,3.0,{"riftd
                                                  //| esc":{"identifier":"riftwarp.TestAddress","version":null},"city":"Somewhere
                                                  //| ","street":"here"}]},"primitiveMaps":{"riftdesc":{"identifier":"riftwarp.Pr
                                                  //| imitiveMaps","version":null},"mapIntInt":[{"k":1,"v":10},{"k":2,"v":20},{"k
                                                  //| ":3,"v":30},{"k":4,"v":40}],"mapStringInt":[{"k":"a","v":1},{"k":"b","v":2}
                                                  //| ,{"k":"c","v":3}],"mapUuidDateTime":[{"k":"c6c0c1b8-2e6e-43e3-88bf-9aa66b27
                                                  //| 0877","v":"2013-02-26T10:24:13.194+01:00"},{"k":"dd53e139-8595-4d08-874a-5e
                                                  //| 87cee42ed4","v":"2013-02-27T10:24:13.194+01:00"},{"k":"b58cf6d8-2022-447f-a
                                                  //| 4fe-5474df57b5c4","v":"2013-02-28T10:24:13.194+01:00"}]},"complexMaps":{"ri
                                                  //| ftdesc":{"identifier":"riftwarp.ComplexMaps","version":null},"mapIntTestAdd
                                                  //| ress1":[{"k":0,"v":{"riftdesc":{"identifier":"riftwarp.TestAddress","versio
                                                  //| n":null},"city":"Hamburg","street":"Am Hafen"}},{"k":1,"v":{"riftdesc":{"id
                                                  //| entifier":"riftwarp.TestAddress","version":null},"city":"New York","street"
                                                  //| :"Broadway"}},{"k":2,"v":{"riftdesc":{"identifier":"riftwarp.TestAddress","
                                                  //| version":null},"city":"Los Angeles ","street":"Sunset Boulevard"}}],"mapInt
                                                  //| Any":[{"k":0,"v":{"riftdesc":{"identifier":"riftwarp.TestAddress","version"
                                                  //| :null},"city":"Hamburg","street":"Am Hafen"}},{"k":1,"v":{"riftdesc":{"iden
                                                  //| tifier":"riftwarp.TestAddress","version":null},"city":"New York","street":"
                                                  //| Broadway"}},{"k":2,"v":{"riftdesc":{"identifier":"riftwarp.TestAddress","ve
                                                  //| rsion":null},"city":"Los Angeles ","street":"Sunset Boulevard"}}],"mapStrin
                                                  //| gAnyWithUnknown":[{"k":"x","v":"f8a6a42d-a76d-48a9-8226-84bd33a51587"},{"k"
                                                  //| :"unspecifiedProblem","v":{"riftdesc":{"identifier":"almhirt.common.Unspeci
                                                  //| fiedProblem","version":null},"message":"Test","severity":"Major","category"
                                                  //| :"SystemProblem","args":[{"k":"arg1","v":95}],"cause":null}},{"k":"y","v":"
                                                  //| aea0cf6e-cef0-40ac-9527-518257529473"},{"k":"1","v":{"riftdesc":{"identifie
                                                  //| r":"riftwarp.TestAddress","version":null},"city":"New York","street":"Broad
                                                  //| way"}},{"k":"0","v":{"riftdesc":{"identifier":"riftwarp.TestAddress","versi
                                                  //| on":null},"city":"Hamburg","street":"Am Hafen"}},{"k":"2","v":{"riftdesc":{
                                                  //| "identifier":"riftwarp.TestAddress","version":null},"city":"Los Angeles ","
                                                  //| street":"Sunset Boulevard"}},{"k":"z","v":"2013-02-26T10:24:13.209+01:00"}]
                                                  //| },"addressOpt":{"riftdesc":{"identifier":"riftwarp.TestAddress","version":n
                                                  //| ull},"city":"Berlin","street":"At the wall 89"}}))
                                       
  val backFromWarpV = riftWarp.receiveFromWarpWithBlobs[DimensionString, TestObjectA](blobFetch)(RiftJson())(warpStreamV.forceResult)
                                                  //> backFromWarpV  : almhirt.common.AlmValidation[riftwarp.TestObjectA] = Succe
                                                  //| ss(TestObjectA([B@a5571f9,[B@78da0edd,PrimitiveTypes(I am Pete,true,127,-23
                                                  //| 7823,-278234263,26587625768237658736586387652875687568276525252057730500720
                                                  //| 9857025728132213242,1.3675,1.3672322350005,23761247614876823746.23846749182
                                                  //| 408,2013-02-26T10:24:13.116+01:00,d09512c0-ef3d-4968-bbf6-94123c523535),Pri
                                                  //| mitiveListMAs(List(alpha, beta, gamma, delta),List(1, 2, 3, 4, 5, 6, 7, 8, 
                                                  //| 9, 10),List(1.0, 0.5, 0.2, 0.125),List(1.333333, 1.33333335, 1.6666666, 1.6
                                                  //| 666667),List(2013-02-26T11:24:13.178+01:00, 2013-02-26T12:24:13.178+01:00, 
                                                  //| 2013-02-26T13:24:13.178+01:00, 2013-02-26T14:24:13.178+01:00)),PrimitiveVec
                                                  //| torMAs(Vector(alpha, beta, gamma, delta),Vector(1, 2, 3, 4, 5, 6, 7, 8, 9, 
                                                  //| 10),Vector(1.0, 0.5, 0.2, 0.125),Vector(1.333333, 1.33333335, 1.6666666, 1.
                                                  //| 6666667),Vector(2013-02-26T11:24:13.178+01:00, 2013-02-26T12:24:13.178+01:0
                                                  //| 0, 2013-02-26T13:24:13.178+01:00, 2013-02-26T14:24:13.178+01:00)),Some(Prim
                                                  //| itiveSetMAs(Set(alpha, beta, gamma, delta),Set(5, 10, 1, 6, 9, 2, 7, 3, 8, 
                                                  //| 4),Set(1.0, 0.5, 0.2, 0.125),Set(1.333333, 1.33333335, 1.6666666, 1.6666667
                                                  //| ),None)),PrimitiveIterableMAs(Set(alpha, beta, gamma, delta),Set(5, 10, 1, 
                                                  //| 6, 9, 2, 7, 3, 8, 4),Set(1.0, 0.5, 0.2, 0.125),Set(1.333333, 1.33333335, 1.
                                                  //| 6666666, 1.6666667),Set(2013-02-26T11:24:13.178+01:00, 2013-02-26T12:24:13.
                                                  //| 178+01:00, 2013-02-26T13:24:13.178+01:00, 2013-02-26T14:24:13.178+01:00)),C
                                                  //| omplexMAs(List(TestAddress(Hamburg,Am Hafen), TestAddress(New York,Broadway
                                                  //| ), TestAddress(Los Angeles ,Sunset Boulevard)),Vector(TestAddress(Hamburg,A
                                                  //| m Hafen), TestAddress(New York,Broadway), TestAddress(Los Angeles ,Sunset B
                                                  //| oulevard)),Set(TestAddress(Hamburg,Am Hafen), TestAddress(New York,Broadway
                                                  //| ), TestAddress(Los Angeles ,Sunset Boulevard)),List(true, hello, 1.0, 2.0, 
                                                  //| 3.0, 3.0, TestAddress(Somewhere,here))),PrimitiveMaps(Map(1 -> 10, 2 -> 20,
                                                  //|  3 -> 30, 4 -> 40),Map(a -> 1, b -> 2, c -> 3),Map(c6c0c1b8-2e6e-43e3-88bf-
                                                  //| 9aa66b270877 -> 2013-02-26T10:24:13.194+01:00, dd53e139-8595-4d08-874a-5e87
                                                  //| cee42ed4 -> 2013-02-27T10:24:13.194+01:00, b58cf6d8-2022-447f-a4fe-5474df57
                                                  //| b5c4 -> 2013-02-28T10:24:13.194+01:00)),ComplexMaps(Map(0 -> TestAddress(Ha
                                                  //| mburg,Am Hafen), 1 -> TestAddress(New York,Broadway), 2 -> TestAddress(Los 
                                                  //| Angeles ,Sunset Boulevard)),Map(0 -> TestAddress(Hamburg,Am Hafen), 1 -> Te
                                                  //| stAddress(New York,Broadway), 2 -> TestAddress(Los Angeles ,Sunset Boulevar
                                                  //| d)),Map(x -> f8a6a42d-a76d-48a9-8226-84bd33a51587, unspecifiedProblem -> al
                                                  //| mhirt.common.UnspecifiedProblem
                                                  //| Test
                                                  //| Category: SystemProblem
                                                  //| Severity: Major
                                                  //| Arguments: Map(arg1 -> 95.0)
                                                  //| , y -> aea0cf6e-cef0-40ac-9527-518257529473, 1 -> TestAddress(New York,Broa
                                                  //| dway), 0 -> TestAddress(Hamburg,Am Hafen), 2 -> TestAddress(Los Angeles ,Su
                                                  //| nset Boulevard), z -> 2013-02-26T10:24:13.209+01:00)),Some(TestAddress(Berl
                                                  //| in,At the wall 89))))
 
  val backFromWarp = backFromWarpV.forceResult    //> backFromWarp  : riftwarp.TestObjectA = TestObjectA([B@a5571f9,[B@78da0edd,P
                                                  //| rimitiveTypes(I am Pete,true,127,-237823,-278234263,26587625768237658736586
                                                  //| 3876528756875682765252520577305007209857025728132213242,1.3675,1.3672322350
                                                  //| 005,23761247614876823746.23846749182408,2013-02-26T10:24:13.116+01:00,d0951
                                                  //| 2c0-ef3d-4968-bbf6-94123c523535),PrimitiveListMAs(List(alpha, beta, gamma, 
                                                  //| delta),List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),List(1.0, 0.5, 0.2, 0.125),List(
                                                  //| 1.333333, 1.33333335, 1.6666666, 1.6666667),List(2013-02-26T11:24:13.178+01
                                                  //| :00, 2013-02-26T12:24:13.178+01:00, 2013-02-26T13:24:13.178+01:00, 2013-02-
                                                  //| 26T14:24:13.178+01:00)),PrimitiveVectorMAs(Vector(alpha, beta, gamma, delta
                                                  //| ),Vector(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),Vector(1.0, 0.5, 0.2, 0.125),Vector
                                                  //| (1.333333, 1.33333335, 1.6666666, 1.6666667),Vector(2013-02-26T11:24:13.178
                                                  //| +01:00, 2013-02-26T12:24:13.178+01:00, 2013-02-26T13:24:13.178+01:00, 2013-
                                                  //| 02-26T14:24:13.178+01:00)),Some(PrimitiveSetMAs(Set(alpha, beta, gamma, del
                                                  //| ta),Set(5, 10, 1, 6, 9, 2, 7, 3, 8, 4),Set(1.0, 0.5, 0.2, 0.125),Set(1.3333
                                                  //| 33, 1.33333335, 1.6666666, 1.6666667),None)),PrimitiveIterableMAs(Set(alpha
                                                  //| , beta, gamma, delta),Set(5, 10, 1, 6, 9, 2, 7, 3, 8, 4),Set(1.0, 0.5, 0.2,
                                                  //|  0.125),Set(1.333333, 1.33333335, 1.6666666, 1.6666667),Set(2013-02-26T11:2
                                                  //| 4:13.178+01:00, 2013-02-26T12:24:13.178+01:00, 2013-02-26T13:24:13.178+01:0
                                                  //| 0, 2013-02-26T14:24:13.178+01:00)),ComplexMAs(List(TestAddress(Hamburg,Am H
                                                  //| afen), TestAddress(New York,Broadway), TestAddress(Los Angeles ,Sunset Boul
                                                  //| evard)),Vector(TestAddress(Hamburg,Am Hafen), TestAddress(New York,Broadway
                                                  //| ), TestAddress(Los Angeles ,Sunset Boulevard)),Set(TestAddress(Hamburg,Am H
                                                  //| afen), TestAddress(New York,Broadway), TestAddress(Los Angeles ,Sunset Boul
                                                  //| evard)),List(true, hello, 1.0, 2.0, 3.0, 3.0, TestAddress(Somewhere,here)))
                                                  //| ,PrimitiveMaps(Map(1 -> 10, 2 -> 20, 3 -> 30, 4 -> 40),Map(a -> 1, b -> 2, 
                                                  //| c -> 3),Map(c6c0c1b8-2e6e-43e3-88bf-9aa66b270877 -> 2013-02-26T10:24:13.194
                                                  //| +01:00, dd53e139-8595-4d08-874a-5e87cee42ed4 -> 2013-02-27T10:24:13.194+01:
                                                  //| 00, b58cf6d8-2022-447f-a4fe-5474df57b5c4 -> 2013-02-28T10:24:13.194+01:00))
                                                  //| ,ComplexMaps(Map(0 -> TestAddress(Hamburg,Am Hafen), 1 -> TestAddress(New Y
                                                  //| ork,Broadway), 2 -> TestAddress(Los Angeles ,Sunset Boulevard)),Map(0 -> Te
                                                  //| stAddress(Hamburg,Am Hafen), 1 -> TestAddress(New York,Broadway), 2 -> Test
                                                  //| Address(Los Angeles ,Sunset Boulevard)),Map(x -> f8a6a42d-a76d-48a9-8226-84
                                                  //| bd33a51587, unspecifiedProblem -> almhirt.common.UnspecifiedProblem
                                                  //| Test
                                                  //| Category: SystemProblem
                                                  //| Severity: Major
                                                  //| Arguments: Map(arg1 -> 95.0)
                                                  //| , y -> aea0cf6e-cef0-40ac-9527-518257529473, 1 -> TestAddress(New York,Broa
                                                  //| dway), 0 -> TestAddress(Hamburg,Am Hafen), 2 -> TestAddress(Los Angeles ,Su
                                                  //| nset Boulevard), z -> 2013-02-26T10:24:13.209+01:00)),Some(TestAddress(Berl
                                                  //| in,At the wall 89)))

  testObject == backFromWarp                      //> res0: Boolean = false


  testObject.primitiveTypes == backFromWarp.primitiveTypes
                                                  //> res1: Boolean = true
  
  riftWarp.prepareForWarp[DimensionCord](RiftJson())(testObject).flatMap(warpStream =>
    riftWarp.receiveFromWarp[DimensionCord, TestObjectA](RiftJson())(warpStream)).map(rearrived =>
    rearrived == testObject)                      //> res2: scalaz.Validation[almhirt.common.Problem,Boolean] = Success(false)


//  riftWarp.prepareForWarp[DimensionRawMap](RiftMap())(testObject).flatMap(warpStream =>
//    riftWarp.receiveFromWarp[DimensionRawMap, TestObjectA](RiftMap())(warpStream)).map(rearrived =>
//    rearrived == testObject)


}