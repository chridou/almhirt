package riftwarp.worksheets

import scalaz.syntax.validation._
import almhirt.syntax.almvalidation._
import riftwarp._
import riftwarp.impl._

object Worksheet {
  val riftWarp = RiftWarp.concurrentWithDefaults  //> riftWarp  : riftwarp.RiftWarp = riftwarp.RiftWarp$$anon$1@517b46
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

  val testObject = TestObjectA.pete               //> testObject  : riftwarp.TestObjectA = TestObjectA([B@55ab936b,[B@735b81bd,Pr
                                                  //| imitiveTypes(I am Pete,true,127,-237823,-278234263,265876257682376587365863
                                                  //| 876528756875682765252520577305007209857025728132213242,1.3675,1.36723223500
                                                  //| 05,23761247614876823746.23846749182408,2013-02-26T09:44:13.896+01:00,81fb7d
                                                  //| 72-023d-405f-aac1-1c5fb00eda67),PrimitiveListMAs(List(alpha, beta, gamma, d
                                                  //| elta),List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),List(1.0, 0.5, 0.2, 0.125),List(1
                                                  //| .333333, 1.33333335, 1.6666666, 1.6666667),List(2013-02-26T10:44:13.959+01:
                                                  //| 00, 2013-02-26T11:44:13.959+01:00, 2013-02-26T12:44:13.959+01:00, 2013-02-2
                                                  //| 6T13:44:13.959+01:00)),PrimitiveVectorMAs(Vector(alpha, beta, gamma, delta)
                                                  //| ,Vector(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),Vector(1.0, 0.5, 0.2, 0.125),Vector(
                                                  //| 1.333333, 1.33333335, 1.6666666, 1.6666667),Vector(2013-02-26T10:44:13.959+
                                                  //| 01:00, 2013-02-26T11:44:13.959+01:00, 2013-02-26T12:44:13.959+01:00, 2013-0
                                                  //| 2-26T13:44:13.959+01:00)),Some(PrimitiveSetMAs(Set(alpha, beta, gamma, delt
                                                  //| a),Set(5, 10, 1, 6, 9, 2, 7, 3, 8, 4),Set(1.0, 0.5, 0.2, 0.125),Set(1.33333
                                                  //| 3, 1.33333335, 1.6666666, 1.6666667),None)),PrimitiveIterableMAs(List(alpha
                                                  //| , beta, gamma, delta),List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),List(1.0, 0.5, 0.
                                                  //| 2, 0.125),List(1.333333, 1.33333335, 1.6666666, 1.6666667),List(2013-02-26T
                                                  //| 10:44:13.959+01:00, 2013-02-26T11:44:13.959+01:00, 2013-02-26T12:44:13.959+
                                                  //| 01:00, 2013-02-26T13:44:13.959+01:00)),ComplexMAs(List(TestAddress(Hamburg,
                                                  //| Am Hafen), TestAddress(New York,Broadway), TestAddress(Los Angeles ,Sunset 
                                                  //| Boulevard)),Vector(TestAddress(Hamburg,Am Hafen), TestAddress(New York,Broa
                                                  //| dway), TestAddress(Los Angeles ,Sunset Boulevard)),Set(TestAddress(Hamburg,
                                                  //| Am Hafen), TestAddress(New York,Broadway), TestAddress(Los Angeles ,Sunset 
                                                  //| Boulevard)),List(true, hello, 1, 2, 3.0, 3.0, TestAddress(Somewhere,here)))
                                                  //| ,PrimitiveMaps(Map(1 -> 10, 2 -> 20, 3 -> 30, 4 -> 40),Map(a -> 1, b -> 2, 
                                                  //| c -> 3),Map(69716871-7ff7-401f-aac0-a2289061d6ed -> 2013-02-26T09:44:13.974
                                                  //| +01:00, 16f65a3e-4a85-47de-9ee7-217bd482ffa8 -> 2013-02-27T09:44:13.974+01:
                                                  //| 00, a8f53923-fd56-4822-acdc-b70cc2c84ce1 -> 2013-02-28T09:44:13.974+01:00))
                                                  //| ,ComplexMaps(Map(0 -> TestAddress(Hamburg,Am Hafen), 1 -> TestAddress(New Y
                                                  //| ork,Broadway), 2 -> TestAddress(Los Angeles ,Sunset Boulevard)),Map(0 -> Te
                                                  //| stAddress(Hamburg,Am Hafen), 1 -> TestAddress(New York,Broadway), 2 -> Test
                                                  //| Address(Los Angeles ,Sunset Boulevard)),Map(x -> fa9ba911-eed6-4b33-a765-c7
                                                  //| 431a82c324, unspecifiedProblem -> almhirt.common.UnspecifiedProblem
                                                  //| Test
                                                  //| Category: SystemProblem
                                                  //| Severity: Major
                                                  //| Arguments: Map(arg1 -> 95)
                                                  //| , y -> a1247455-54c5-4547-907d-df8da000a3d7, 1 -> TestAddress(New York,Broa
                                                  //| dway), 0 -> TestAddress(Hamburg,Am Hafen), 2 -> TestAddress(Los Angeles ,Su
                                                  //| nset Boulevard), unknownType -> UnknownObject(1), z -> 2013-02-26T09:44:13.
                                                  //| 974+01:00)),Some(TestAddress(Berlin,At the wall 89)))
 
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
                                                  //| er":"RiftBlobRefByName","version":null},"name":"5dd5a6c7-2cbf-4dcc-877d-9bd
                                                  //| 8e9ee4fc4"},"primitiveTypes":{"riftdesc":{"identifier":"riftwarp.PrimitiveT
                                                  //| ypes","version":null},"str":"I am Pete","bool":true,"byte":127,"int":-23782
                                                  //| 3,"long":-278234263,"bigInt":"265876257682376587365863876528756875682765252
                                                  //| 520577305007209857025728132213242","float":1.3674999475479126,"double":1.36
                                                  //| 72322350005,"bigDec":"23761247614876823746.23846749182408","dateTime":"2013
                                                  //| -02-26T09:44:13.896+01:00","uuid":"81fb7d72-023d-405f-aac1-1c5fb00eda67"},"
                                                  //| primitiveListMAs":{"riftdesc":{"identifier":"riftwarp.PrimitiveListMAs","ve
                                                  //| rsion":null},"listString":["alpha","beta","gamma","delta"],"listInt":[1,2,3
                                                  //| ,4,5,6,7,8,9,10],"listDouble":[1.0,0.5,0.2,0.125],"listBigDecimal":["1.3333
                                                  //| 33","1.33333335","1.6666666","1.6666667"],"listDateTime":["2013-02-26T10:44
                                                  //| :13.959+01:00","2013-02-26T11:44:13.959+01:00","2013-02-26T12:44:13.959+01:
                                                  //| 00","2013-02-26T13:44:13.959+01:00"]},"primitiveVectorMAs":{"riftdesc":{"id
                                                  //| entifier":"riftwarp.PrimitiveVectorMAs","version":null},"vectorString":["al
                                                  //| pha","beta","gamma","delta"],"vectorInt":[1,2,3,4,5,6,7,8,9,10],"vectorDoub
                                                  //| le":[1.0,0.5,0.2,0.125],"vectorBigDecimal":["1.333333","1.33333335","1.6666
                                                  //| 666","1.6666667"],"vectorDateTime":["2013-02-26T10:44:13.959+01:00","2013-0
                                                  //| 2-26T11:44:13.959+01:00","2013-02-26T12:44:13.959+01:00","2013-02-26T13:44:
                                                  //| 13.959+01:00"]},"primitiveSetMAs":{"riftdesc":{"identifier":"riftwarp.Primi
                                                  //| tiveSetMAs","version":null},"setString":["alpha","beta","gamma","delta"],"s
                                                  //| etInt":[5,10,1,6,9,2,7,3,8,4],"setDouble":[1.0,0.5,0.2,0.125],"setBigDecima
                                                  //| l":["1.333333","1.33333335","1.6666666","1.6666667"],"setDateTime":null},"p
                                                  //| rimitiveIterableMAs":{"riftdesc":{"identifier":"riftwarp.PrimitiveIterableM
                                                  //| As","version":null},"iterableString":["alpha","beta","gamma","delta"],"iter
                                                  //| ableInt":[1,2,3,4,5,6,7,8,9,10],"iterableDouble":[1.0,0.5,0.2,0.125],"itera
                                                  //| bleBigDecimal":["1.333333","1.33333335","1.6666666","1.6666667"],"iterableD
                                                  //| ateTime":["2013-02-26T10:44:13.959+01:00","2013-02-26T11:44:13.959+01:00","
                                                  //| 2013-02-26T12:44:13.959+01:00","2013-02-26T13:44:13.959+01:00"]},"complexMA
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
                                                  //| ,{"k":"c","v":3}],"mapUuidDateTime":[{"k":"69716871-7ff7-401f-aac0-a2289061
                                                  //| d6ed","v":"2013-02-26T09:44:13.974+01:00"},{"k":"16f65a3e-4a85-47de-9ee7-21
                                                  //| 7bd482ffa8","v":"2013-02-27T09:44:13.974+01:00"},{"k":"a8f53923-fd56-4822-a
                                                  //| cdc-b70cc2c84ce1","v":"2013-02-28T09:44:13.974+01:00"}]},"complexMaps":{"ri
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
                                                  //| gAnyWithUnknown":[{"k":"x","v":"fa9ba911-eed6-4b33-a765-c7431a82c324"},{"k"
                                                  //| :"unspecifiedProblem","v":{"riftdesc":{"identifier":"almhirt.common.Unspeci
                                                  //| fiedProblem","version":null},"message":"Test","severity":"Major","category"
                                                  //| :"SystemProblem","args":[{"k":"arg1","v":95}],"cause":null}},{"k":"y","v":"
                                                  //| a1247455-54c5-4547-907d-df8da000a3d7"},{"k":"1","v":{"riftdesc":{"identifie
                                                  //| r":"riftwarp.TestAddress","version":null},"city":"New York","street":"Broad
                                                  //| way"}},{"k":"0","v":{"riftdesc":{"identifier":"riftwarp.TestAddress","versi
                                                  //| on":null},"city":"Hamburg","street":"Am Hafen"}},{"k":"2","v":{"riftdesc":{
                                                  //| "identifier":"riftwarp.TestAddress","version":null},"city":"Los Angeles ","
                                                  //| street":"Sunset Boulevard"}},{"k":"z","v":"2013-02-26T09:44:13.974+01:00"}]
                                                  //| },"addressOpt":{"riftdesc":{"identifier":"riftwarp.TestAddress","version":n
                                                  //| ull},"city":"Berlin","street":"At the wall 89"}}))
                                       
    
  val backFromWarpV = riftWarp.receiveFromWarpWithBlobs[DimensionString, TestObjectA](blobFetch)(RiftJson())(warpStreamV.forceResult)
                                                  //> backFromWarpV  : almhirt.common.AlmValidation[riftwarp.TestObjectA] = Succe
                                                  //| ss(TestObjectA([B@7e2ef2be,[B@735b81bd,PrimitiveTypes(I am Pete,true,127,-2
                                                  //| 37823,-278234263,2658762576823765873658638765287568756827652525205773050072
                                                  //| 09857025728132213242,1.3675,1.3672322350005,23761247614876823746.2384674918
                                                  //| 2408,2013-02-26T09:44:13.896+01:00,81fb7d72-023d-405f-aac1-1c5fb00eda67),Pr
                                                  //| imitiveListMAs(List(alpha, beta, gamma, delta),List(1, 2, 3, 4, 5, 6, 7, 8,
                                                  //|  9, 10),List(1.0, 0.5, 0.2, 0.125),List(1.333333, 1.33333335, 1.6666666, 1.
                                                  //| 6666667),List(2013-02-26T10:44:13.959+01:00, 2013-02-26T11:44:13.959+01:00,
                                                  //|  2013-02-26T12:44:13.959+01:00, 2013-02-26T13:44:13.959+01:00)),PrimitiveVe
                                                  //| ctorMAs(Vector(alpha, beta, gamma, delta),Vector(1, 2, 3, 4, 5, 6, 7, 8, 9,
                                                  //|  10),Vector(1.0, 0.5, 0.2, 0.125),Vector(1.333333, 1.33333335, 1.6666666, 1
                                                  //| .6666667),Vector(2013-02-26T10:44:13.959+01:00, 2013-02-26T11:44:13.959+01:
                                                  //| 00, 2013-02-26T12:44:13.959+01:00, 2013-02-26T13:44:13.959+01:00)),Some(Pri
                                                  //| mitiveSetMAs(Set(alpha, beta, gamma, delta),Set(5, 10, 1, 6, 9, 2, 7, 3, 8,
                                                  //|  4),Set(1.0, 0.5, 0.2, 0.125),Set(1.333333, 1.33333335, 1.6666666, 1.666666
                                                  //| 7),None)),PrimitiveIterableMAs(Set(alpha, beta, gamma, delta),Set(5, 10, 1,
                                                  //|  6, 9, 2, 7, 3, 8, 4),Set(1.0, 0.5, 0.2, 0.125),Set(1.333333, 1.33333335, 1
                                                  //| .6666666, 1.6666667),Set(2013-02-26T10:44:13.959+01:00, 2013-02-26T11:44:13
                                                  //| .959+01:00, 2013-02-26T12:44:13.959+01:00, 2013-02-26T13:44:13.959+01:00)),
                                                  //| ComplexMAs(List(TestAddress(Hamburg,Am Hafen), TestAddress(New York,Broadwa
                                                  //| y), TestAddress(Los Angeles ,Sunset Boulevard)),Vector(TestAddress(Hamburg,
                                                  //| Am Hafen), TestAddress(New York,Broadway), TestAddress(Los Angeles ,Sunset 
                                                  //| Boulevard)),Set(TestAddress(Hamburg,Am Hafen), TestAddress(New York,Broadwa
                                                  //| y), TestAddress(Los Angeles ,Sunset Boulevard)),List(true, hello, 1.0, 2.0,
                                                  //|  3.0, 3.0, TestAddress(Somewhere,here))),PrimitiveMaps(Map(1 -> 10, 2 -> 20
                                                  //| , 3 -> 30, 4 -> 40),Map(a -> 1, b -> 2, c -> 3),Map(69716871-7ff7-401f-aac0
                                                  //| -a2289061d6ed -> 2013-02-26T09:44:13.974+01:00, 16f65a3e-4a85-47de-9ee7-217
                                                  //| bd482ffa8 -> 2013-02-27T09:44:13.974+01:00, a8f53923-fd56-4822-acdc-b70cc2c
                                                  //| 84ce1 -> 2013-02-28T09:44:13.974+01:00)),ComplexMaps(Map(0 -> TestAddress(H
                                                  //| amburg,Am Hafen), 1 -> TestAddress(New York,Broadway), 2 -> TestAddress(Los
                                                  //|  Angeles ,Sunset Boulevard)),Map(0 -> TestAddress(Hamburg,Am Hafen), 1 -> T
                                                  //| estAddress(New York,Broadway), 2 -> TestAddress(Los Angeles ,Sunset Bouleva
                                                  //| rd)),Map(x -> fa9ba911-eed6-4b33-a765-c7431a82c324, unspecifiedProblem -> a
                                                  //| lmhirt.common.UnspecifiedProblem
                                                  //| Test
                                                  //| Category: SystemProblem
                                                  //| Severity: Major
                                                  //| Arguments: Map(arg1 -> 95.0)
                                                  //| , y -> a1247455-54c5-4547-907d-df8da000a3d7, 1 -> TestAddress(New York,Broa
                                                  //| dway), 0 -> TestAddress(Hamburg,Am Hafen), 2 -> TestAddress(Los Angeles ,Su
                                                  //| nset Boulevard), z -> 2013-02-26T09:44:13.974+01:00)),Some(TestAddress(Berl
                                                  //| in,At the wall 89))))
 
  val backFromWarp = backFromWarpV.forceResult    //> backFromWarp  : riftwarp.TestObjectA = TestObjectA([B@7e2ef2be,[B@735b81bd,
                                                  //| PrimitiveTypes(I am Pete,true,127,-237823,-278234263,2658762576823765873658
                                                  //| 63876528756875682765252520577305007209857025728132213242,1.3675,1.367232235
                                                  //| 0005,23761247614876823746.23846749182408,2013-02-26T09:44:13.896+01:00,81fb
                                                  //| 7d72-023d-405f-aac1-1c5fb00eda67),PrimitiveListMAs(List(alpha, beta, gamma,
                                                  //|  delta),List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),List(1.0, 0.5, 0.2, 0.125),List
                                                  //| (1.333333, 1.33333335, 1.6666666, 1.6666667),List(2013-02-26T10:44:13.959+0
                                                  //| 1:00, 2013-02-26T11:44:13.959+01:00, 2013-02-26T12:44:13.959+01:00, 2013-02
                                                  //| -26T13:44:13.959+01:00)),PrimitiveVectorMAs(Vector(alpha, beta, gamma, delt
                                                  //| a),Vector(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),Vector(1.0, 0.5, 0.2, 0.125),Vecto
                                                  //| r(1.333333, 1.33333335, 1.6666666, 1.6666667),Vector(2013-02-26T10:44:13.95
                                                  //| 9+01:00, 2013-02-26T11:44:13.959+01:00, 2013-02-26T12:44:13.959+01:00, 2013
                                                  //| -02-26T13:44:13.959+01:00)),Some(PrimitiveSetMAs(Set(alpha, beta, gamma, de
                                                  //| lta),Set(5, 10, 1, 6, 9, 2, 7, 3, 8, 4),Set(1.0, 0.5, 0.2, 0.125),Set(1.333
                                                  //| 333, 1.33333335, 1.6666666, 1.6666667),None)),PrimitiveIterableMAs(Set(alph
                                                  //| a, beta, gamma, delta),Set(5, 10, 1, 6, 9, 2, 7, 3, 8, 4),Set(1.0, 0.5, 0.2
                                                  //| , 0.125),Set(1.333333, 1.33333335, 1.6666666, 1.6666667),Set(2013-02-26T10:
                                                  //| 44:13.959+01:00, 2013-02-26T11:44:13.959+01:00, 2013-02-26T12:44:13.959+01:
                                                  //| 00, 2013-02-26T13:44:13.959+01:00)),ComplexMAs(List(TestAddress(Hamburg,Am 
                                                  //| Hafen), TestAddress(New York,Broadway), TestAddress(Los Angeles ,Sunset Bou
                                                  //| levard)),Vector(TestAddress(Hamburg,Am Hafen), TestAddress(New York,Broadwa
                                                  //| y), TestAddress(Los Angeles ,Sunset Boulevard)),Set(TestAddress(Hamburg,Am 
                                                  //| Hafen), TestAddress(New York,Broadway), TestAddress(Los Angeles ,Sunset Bou
                                                  //| levard)),List(true, hello, 1.0, 2.0, 3.0, 3.0, TestAddress(Somewhere,here))
                                                  //| ),PrimitiveMaps(Map(1 -> 10, 2 -> 20, 3 -> 30, 4 -> 40),Map(a -> 1, b -> 2,
                                                  //|  c -> 3),Map(69716871-7ff7-401f-aac0-a2289061d6ed -> 2013-02-26T09:44:13.97
                                                  //| 4+01:00, 16f65a3e-4a85-47de-9ee7-217bd482ffa8 -> 2013-02-27T09:44:13.974+01
                                                  //| :00, a8f53923-fd56-4822-acdc-b70cc2c84ce1 -> 2013-02-28T09:44:13.974+01:00)
                                                  //| ),ComplexMaps(Map(0 -> TestAddress(Hamburg,Am Hafen), 1 -> TestAddress(New 
                                                  //| York,Broadway), 2 -> TestAddress(Los Angeles ,Sunset Boulevard)),Map(0 -> T
                                                  //| estAddress(Hamburg,Am Hafen), 1 -> TestAddress(New York,Broadway), 2 -> Tes
                                                  //| tAddress(Los Angeles ,Sunset Boulevard)),Map(x -> fa9ba911-eed6-4b33-a765-c
                                                  //| 7431a82c324, unspecifiedProblem -> almhirt.common.UnspecifiedProblem
                                                  //| Test
                                                  //| Category: SystemProblem
                                                  //| Severity: Major
                                                  //| Arguments: Map(arg1 -> 95.0)
                                                  //| , y -> a1247455-54c5-4547-907d-df8da000a3d7, 1 -> TestAddress(New York,Broa
                                                  //| dway), 0 -> TestAddress(Hamburg,Am Hafen), 2 -> TestAddress(Los Angeles ,Su
                                                  //| nset Boulevard), z -> 2013-02-26T09:44:13.974+01:00)),Some(TestAddress(Berl
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