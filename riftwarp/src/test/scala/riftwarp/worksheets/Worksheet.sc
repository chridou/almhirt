package riftwarp.worksheets

import scalaz.syntax.validation._
import almhirt.syntax.almvalidation._
import riftwarp._
import riftwarp.impl._

object Worksheet {
  val riftWarp = RiftWarp.concurrentWithDefaults  //> riftWarp  : riftwarp.RiftWarp = riftwarp.RiftWarp$$anon$1@18c92ff9
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


  val testObject = TestObjectA.pete               //> testObject  : riftwarp.TestObjectA = TestObjectA([B@348f4ec,[B@2ac7b3a3,Pri
                                                  //| mitiveTypes(I am Pete,true,127,-237823,-278234263,2658762576823765873658638
                                                  //| 76528756875682765252520577305007209857025728132213242,1.3675,1.367232235000
                                                  //| 5,23761247614876823746.23846749182408,2013-03-11T15:45:07.710+01:00,b0fcf5b
                                                  //| c-685c-4601-a6a8-5d9f81714136),PrimitiveListMAs(List(alpha, beta, gamma, de
                                                  //| lta),List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),List(1.0, 0.5, 0.2, 0.125),List(1.
                                                  //| 333333, 1.33333335, 1.6666666, 1.6666667),List(2013-03-11T16:45:07.773+01:0
                                                  //| 0, 2013-03-11T17:45:07.773+01:00, 2013-03-11T18:45:07.773+01:00, 2013-03-11
                                                  //| T19:45:07.773+01:00)),PrimitiveVectorMAs(Vector(alpha, beta, gamma, delta),
                                                  //| Vector(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),Vector(1.0, 0.5, 0.2, 0.125),Vector(1
                                                  //| .333333, 1.33333335, 1.6666666, 1.6666667),Vector(2013-03-11T16:45:07.773+0
                                                  //| 1:00, 2013-03-11T17:45:07.773+01:00, 2013-03-11T18:45:07.773+01:00, 2013-03
                                                  //| -11T19:45:07.773+01:00)),Some(PrimitiveSetMAs(Set(alpha, beta, gamma, delta
                                                  //| ),Set(5, 10, 1, 6, 9, 2, 7, 3, 8, 4),Set(1.0, 0.5, 0.2, 0.125),Set(1.333333
                                                  //| , 1.33333335, 1.6666666, 1.6666667),None)),PrimitiveIterableMAs(List(alpha,
                                                  //|  beta, gamma, delta),List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),List(1.0, 0.5, 0.2
                                                  //| , 0.125),List(1.333333, 1.33333335, 1.6666666, 1.6666667),List(2013-03-11T1
                                                  //| 6:45:07.773+01:00, 2013-03-11T17:45:07.773+01:00, 2013-03-11T18:45:07.773+0
                                                  //| 1:00, 2013-03-11T19:45:07.773+01:00)),ComplexMAs(List(TestAddress(Hamburg,A
                                                  //| m Hafen), TestAddress(New York,Broadway), TestAddress(Los Angeles ,Sunset B
                                                  //| oulevard)),Vector(TestAddress(Hamburg,Am Hafen), TestAddress(New York,Broad
                                                  //| way), TestAddress(Los Angeles ,Sunset Boulevard)),Set(TestAddress(Hamburg,A
                                                  //| m Hafen), TestAddress(New York,Broadway), TestAddress(Los Angeles ,Sunset B
                                                  //| oulevard)),List(true, hello, 1, 2, 3.0, 3.0, TestAddress(Somewhere,here))),
                                                  //| PrimitiveMaps(Map(1 -> 10, 2 -> 20, 3 -> 30, 4 -> 40),Map(a -> 1, b -> 2, c
                                                  //|  -> 3),Map(17203ce5-7fdd-4acb-93f3-70fa66d5867a -> 2013-03-11T15:45:07.788+
                                                  //| 01:00, a1d82514-ebca-4c25-89be-9bff018bf4f3 -> 2013-03-12T15:45:07.788+01:0
                                                  //| 0, 5e5f5fc4-4966-49ef-be44-83e43e586aa8 -> 2013-03-13T15:45:07.788+01:00)),
                                                  //| ComplexMaps(Map(0 -> TestAddress(Hamburg,Am Hafen), 1 -> TestAddress(New Yo
                                                  //| rk,Broadway), 2 -> TestAddress(Los Angeles ,Sunset Boulevard)),Map(0 -> Tes
                                                  //| tAddress(Hamburg,Am Hafen), 1 -> TestAddress(New York,Broadway), 2 -> TestA
                                                  //| ddress(Los Angeles ,Sunset Boulevard)),Map(x -> 2faa22e0-c3e8-4b07-a1da-d84
                                                  //| 03a66a9b1, unspecifiedProblem -> almhirt.common.UnspecifiedProblem
                                                  //| Test
                                                  //| Category: SystemProblem
                                                  //| Severity: Major
                                                  //| Arguments: Map(arg1 -> 95)
                                                  //| , y -> 483f4ea6-5a15-4669-98aa-5f1a562dd8b5, 1 -> TestAddress(New York,Broa
                                                  //| dway), 0 -> TestAddress(Hamburg,Am Hafen), 2 -> TestAddress(Los Angeles ,Su
                                                  //| nset Boulevard), unknownType -> UnknownObject(1), z -> 2013-03-11T15:45:07.
                                                  //| 788+01:00)),Some(TestAddress(Berlin,At the wall 89)))
 
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
                                                  //| er":"RiftBlobRefByName","version":null},"name":"866d4506-a717-419d-93b0-b89
                                                  //| a93f5c4e1"},"primitiveTypes":{"riftdesc":{"identifier":"riftwarp.PrimitiveT
                                                  //| ypes","version":null},"str":"I am Pete","bool":true,"byte":127,"int":-23782
                                                  //| 3,"long":-278234263,"bigInt":"265876257682376587365863876528756875682765252
                                                  //| 520577305007209857025728132213242","float":1.3674999475479126,"double":1.36
                                                  //| 72322350005,"bigDec":"23761247614876823746.23846749182408","dateTime":"2013
                                                  //| -03-11T15:45:07.710+01:00","uuid":"b0fcf5bc-685c-4601-a6a8-5d9f81714136"},"
                                                  //| primitiveListMAs":{"riftdesc":{"identifier":"riftwarp.PrimitiveListMAs","ve
                                                  //| rsion":null},"listString":["alpha","beta","gamma","delta"],"listInt":[1,2,3
                                                  //| ,4,5,6,7,8,9,10],"listDouble":[1.0,0.5,0.2,0.125],"listBigDecimal":["1.3333
                                                  //| 33","1.33333335","1.6666666","1.6666667"],"listDateTime":["2013-03-11T16:45
                                                  //| :07.773+01:00","2013-03-11T17:45:07.773+01:00","2013-03-11T18:45:07.773+01:
                                                  //| 00","2013-03-11T19:45:07.773+01:00"]},"primitiveVectorMAs":{"riftdesc":{"id
                                                  //| entifier":"riftwarp.PrimitiveVectorMAs","version":null},"vectorString":["al
                                                  //| pha","beta","gamma","delta"],"vectorInt":[1,2,3,4,5,6,7,8,9,10],"vectorDoub
                                                  //| le":[1.0,0.5,0.2,0.125],"vectorBigDecimal":["1.333333","1.33333335","1.6666
                                                  //| 666","1.6666667"],"vectorDateTime":["2013-03-11T16:45:07.773+01:00","2013-0
                                                  //| 3-11T17:45:07.773+01:00","2013-03-11T18:45:07.773+01:00","2013-03-11T19:45:
                                                  //| 07.773+01:00"]},"primitiveSetMAs":{"riftdesc":{"identifier":"riftwarp.Primi
                                                  //| tiveSetMAs","version":null},"setString":["alpha","beta","gamma","delta"],"s
                                                  //| etInt":[5,10,1,6,9,2,7,3,8,4],"setDouble":[1.0,0.5,0.2,0.125],"setBigDecima
                                                  //| l":["1.333333","1.33333335","1.6666666","1.6666667"],"setDateTime":null},"p
                                                  //| rimitiveIterableMAs":{"riftdesc":{"identifier":"riftwarp.PrimitiveIterableM
                                                  //| As","version":null},"iterableString":["alpha","beta","gamma","delta"],"iter
                                                  //| ableInt":[1,2,3,4,5,6,7,8,9,10],"iterableDouble":[1.0,0.5,0.2,0.125],"itera
                                                  //| bleBigDecimal":["1.333333","1.33333335","1.6666666","1.6666667"],"iterableD
                                                  //| ateTime":["2013-03-11T16:45:07.773+01:00","2013-03-11T17:45:07.773+01:00","
                                                  //| 2013-03-11T18:45:07.773+01:00","2013-03-11T19:45:07.773+01:00"]},"complexMA
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
                                                  //| imitiveMaps","version":null},"mapIntInt":[[1,10],[2,20],[3,30],[4,40]],"map
                                                  //| StringInt":[["a",1],["b",2],["c",3]],"mapUuidDateTime":[["17203ce5-7fdd-4ac
                                                  //| b-93f3-70fa66d5867a","2013-03-11T15:45:07.788+01:00"],["a1d82514-ebca-4c25-
                                                  //| 89be-9bff018bf4f3","2013-03-12T15:45:07.788+01:00"],["5e5f5fc4-4966-49ef-be
                                                  //| 44-83e43e586aa8","2013-03-13T15:45:07.788+01:00"]]},"complexMaps":{"riftdes
                                                  //| c":{"identifier":"riftwarp.ComplexMaps","version":null},"mapIntTestAddress1
                                                  //| ":[[0,{"riftdesc":{"identifier":"riftwarp.TestAddress","version":null},"cit
                                                  //| y":"Hamburg","street":"Am Hafen"}],[1,{"riftdesc":{"identifier":"riftwarp.T
                                                  //| estAddress","version":null},"city":"New York","street":"Broadway"}],[2,{"ri
                                                  //| ftdesc":{"identifier":"riftwarp.TestAddress","version":null},"city":"Los An
                                                  //| geles ","street":"Sunset Boulevard"}]],"mapIntAny":[[0,{"riftdesc":{"identi
                                                  //| fier":"riftwarp.TestAddress","version":null},"city":"Hamburg","street":"Am 
                                                  //| Hafen"}],[1,{"riftdesc":{"identifier":"riftwarp.TestAddress","version":null
                                                  //| },"city":"New York","street":"Broadway"}],[2,{"riftdesc":{"identifier":"rif
                                                  //| twarp.TestAddress","version":null},"city":"Los Angeles ","street":"Sunset B
                                                  //| oulevard"}]],"mapStringAnyWithUnknown":[["x","2faa22e0-c3e8-4b07-a1da-d8403
                                                  //| a66a9b1"],["unspecifiedProblem",{"riftdesc":{"identifier":"almhirt.common.U
                                                  //| nspecifiedProblem","version":null},"message":"Test","severity":"Major","cat
                                                  //| egory":"SystemProblem","args":[["arg1",95]],"cause":null}],["y","483f4ea6-5
                                                  //| a15-4669-98aa-5f1a562dd8b5"],["1",{"riftdesc":{"identifier":"riftwarp.TestA
                                                  //| ddress","version":null},"city":"New York","street":"Broadway"}],["0",{"rift
                                                  //| desc":{"identifier":"riftwarp.TestAddress","version":null},"city":"Hamburg"
                                                  //| ,"street":"Am Hafen"}],["2",{"riftdesc":{"identifier":"riftwarp.TestAddress
                                                  //| ","version":null},"city":"Los Angeles ","street":"Sunset Boulevard"}],["z",
                                                  //| "2013-03-11T15:45:07.788+01:00"]]},"addressOpt":{"riftdesc":{"identifier":"
                                                  //| riftwarp.TestAddress","version":null},"city":"Berlin","street":"At the wall
                                                  //|  89"}}))
       
                        
                        
  println(blobdata)                               //> Map(866d4506-a717-419d-93b0-b89a93f5c4e1 -> [B@2ac7b3a3)
                                       
  val backFromWarpV = riftWarp.receiveFromWarpWithBlobs[DimensionString, TestObjectA](blobFetch)(RiftJson())(warpStreamV.forceResult)
                                                  //> backFromWarpV  : almhirt.common.AlmValidation[riftwarp.TestObjectA] = Succe
                                                  //| ss(TestObjectA([B@7f938cc2,[B@2ac7b3a3,PrimitiveTypes(I am Pete,true,127,-2
                                                  //| 37823,-278234263,2658762576823765873658638765287568756827652525205773050072
                                                  //| 09857025728132213242,1.3675,1.3672322350005,23761247614876823746.2384674918
                                                  //| 2408,2013-03-11T15:45:07.710+01:00,b0fcf5bc-685c-4601-a6a8-5d9f81714136),Pr
                                                  //| imitiveListMAs(List(alpha, beta, gamma, delta),List(1, 2, 3, 4, 5, 6, 7, 8,
                                                  //|  9, 10),List(1.0, 0.5, 0.2, 0.125),List(1.333333, 1.33333335, 1.6666666, 1.
                                                  //| 6666667),List(2013-03-11T16:45:07.773+01:00, 2013-03-11T17:45:07.773+01:00,
                                                  //|  2013-03-11T18:45:07.773+01:00, 2013-03-11T19:45:07.773+01:00)),PrimitiveVe
                                                  //| ctorMAs(Vector(alpha, beta, gamma, delta),Vector(1, 2, 3, 4, 5, 6, 7, 8, 9,
                                                  //|  10),Vector(1.0, 0.5, 0.2, 0.125),Vector(1.333333, 1.33333335, 1.6666666, 1
                                                  //| .6666667),Vector(2013-03-11T16:45:07.773+01:00, 2013-03-11T17:45:07.773+01:
                                                  //| 00, 2013-03-11T18:45:07.773+01:00, 2013-03-11T19:45:07.773+01:00)),Some(Pri
                                                  //| mitiveSetMAs(Set(alpha, beta, gamma, delta),Set(5, 10, 1, 6, 9, 2, 7, 3, 8,
                                                  //|  4),Set(1.0, 0.5, 0.2, 0.125),Set(1.333333, 1.33333335, 1.6666666, 1.666666
                                                  //| 7),None)),PrimitiveIterableMAs(Set(alpha, beta, gamma, delta),Set(5, 10, 1,
                                                  //|  6, 9, 2, 7, 3, 8, 4),Set(1.0, 0.5, 0.2, 0.125),Set(1.333333, 1.33333335, 1
                                                  //| .6666666, 1.6666667),Set(2013-03-11T16:45:07.773+01:00, 2013-03-11T17:45:07
                                                  //| .773+01:00, 2013-03-11T18:45:07.773+01:00, 2013-03-11T19:45:07.773+01:00)),
                                                  //| ComplexMAs(List(TestAddress(Hamburg,Am Hafen), TestAddress(New York,Broadwa
                                                  //| y), TestAddress(Los Angeles ,Sunset Boulevard)),Vector(TestAddress(Hamburg,
                                                  //| Am Hafen), TestAddress(New York,Broadway), TestAddress(Los Angeles ,Sunset 
                                                  //| Boulevard)),Set(TestAddress(Hamburg,Am Hafen), TestAddress(New York,Broadwa
                                                  //| y), TestAddress(Los Angeles ,Sunset Boulevard)),List(true, hello, 1.0, 2.0,
                                                  //|  3.0, 3.0, TestAddress(Somewhere,here))),PrimitiveMaps(Map(1 -> 10, 2 -> 20
                                                  //| , 3 -> 30, 4 -> 40),Map(a -> 1, b -> 2, c -> 3),Map(17203ce5-7fdd-4acb-93f3
                                                  //| -70fa66d5867a -> 2013-03-11T15:45:07.788+01:00, a1d82514-ebca-4c25-89be-9bf
                                                  //| f018bf4f3 -> 2013-03-12T15:45:07.788+01:00, 5e5f5fc4-4966-49ef-be44-83e43e5
                                                  //| 86aa8 -> 2013-03-13T15:45:07.788+01:00)),ComplexMaps(Map(0 -> TestAddress(H
                                                  //| amburg,Am Hafen), 1 -> TestAddress(New York,Broadway), 2 -> TestAddress(Los
                                                  //|  Angeles ,Sunset Boulevard)),Map(0 -> TestAddress(Hamburg,Am Hafen), 1 -> T
                                                  //| estAddress(New York,Broadway), 2 -> TestAddress(Los Angeles ,Sunset Bouleva
                                                  //| rd)),Map(x -> 2faa22e0-c3e8-4b07-a1da-d8403a66a9b1, unspecifiedProblem -> a
                                                  //| lmhirt.common.UnspecifiedProblem
                                                  //| Test
                                                  //| Category: SystemProblem
                                                  //| Severity: Major
                                                  //| Arguments: Map(arg1 -> 95.0)
                                                  //| , y -> 483f4ea6-5a15-4669-98aa-5f1a562dd8b5, 1 -> TestAddress(New York,Broa
                                                  //| dway), 0 -> TestAddress(Hamburg,Am Hafen), 2 -> TestAddress(Los Angeles ,Su
                                                  //| nset Boulevard), z -> 2013-03-11T15:45:07.788+01:00)),Some(TestAddress(Berl
                                                  //| in,At the wall 89))))
 
  val backFromWarp = backFromWarpV.forceResult    //> backFromWarp  : riftwarp.TestObjectA = TestObjectA([B@7f938cc2,[B@2ac7b3a3,
                                                  //| PrimitiveTypes(I am Pete,true,127,-237823,-278234263,2658762576823765873658
                                                  //| 63876528756875682765252520577305007209857025728132213242,1.3675,1.367232235
                                                  //| 0005,23761247614876823746.23846749182408,2013-03-11T15:45:07.710+01:00,b0fc
                                                  //| f5bc-685c-4601-a6a8-5d9f81714136),PrimitiveListMAs(List(alpha, beta, gamma,
                                                  //|  delta),List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),List(1.0, 0.5, 0.2, 0.125),List
                                                  //| (1.333333, 1.33333335, 1.6666666, 1.6666667),List(2013-03-11T16:45:07.773+0
                                                  //| 1:00, 2013-03-11T17:45:07.773+01:00, 2013-03-11T18:45:07.773+01:00, 2013-03
                                                  //| -11T19:45:07.773+01:00)),PrimitiveVectorMAs(Vector(alpha, beta, gamma, delt
                                                  //| a),Vector(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),Vector(1.0, 0.5, 0.2, 0.125),Vecto
                                                  //| r(1.333333, 1.33333335, 1.6666666, 1.6666667),Vector(2013-03-11T16:45:07.77
                                                  //| 3+01:00, 2013-03-11T17:45:07.773+01:00, 2013-03-11T18:45:07.773+01:00, 2013
                                                  //| -03-11T19:45:07.773+01:00)),Some(PrimitiveSetMAs(Set(alpha, beta, gamma, de
                                                  //| lta),Set(5, 10, 1, 6, 9, 2, 7, 3, 8, 4),Set(1.0, 0.5, 0.2, 0.125),Set(1.333
                                                  //| 333, 1.33333335, 1.6666666, 1.6666667),None)),PrimitiveIterableMAs(Set(alph
                                                  //| a, beta, gamma, delta),Set(5, 10, 1, 6, 9, 2, 7, 3, 8, 4),Set(1.0, 0.5, 0.2
                                                  //| , 0.125),Set(1.333333, 1.33333335, 1.6666666, 1.6666667),Set(2013-03-11T16:
                                                  //| 45:07.773+01:00, 2013-03-11T17:45:07.773+01:00, 2013-03-11T18:45:07.773+01:
                                                  //| 00, 2013-03-11T19:45:07.773+01:00)),ComplexMAs(List(TestAddress(Hamburg,Am 
                                                  //| Hafen), TestAddress(New York,Broadway), TestAddress(Los Angeles ,Sunset Bou
                                                  //| levard)),Vector(TestAddress(Hamburg,Am Hafen), TestAddress(New York,Broadwa
                                                  //| y), TestAddress(Los Angeles ,Sunset Boulevard)),Set(TestAddress(Hamburg,Am 
                                                  //| Hafen), TestAddress(New York,Broadway), TestAddress(Los Angeles ,Sunset Bou
                                                  //| levard)),List(true, hello, 1.0, 2.0, 3.0, 3.0, TestAddress(Somewhere,here))
                                                  //| ),PrimitiveMaps(Map(1 -> 10, 2 -> 20, 3 -> 30, 4 -> 40),Map(a -> 1, b -> 2,
                                                  //|  c -> 3),Map(17203ce5-7fdd-4acb-93f3-70fa66d5867a -> 2013-03-11T15:45:07.78
                                                  //| 8+01:00, a1d82514-ebca-4c25-89be-9bff018bf4f3 -> 2013-03-12T15:45:07.788+01
                                                  //| :00, 5e5f5fc4-4966-49ef-be44-83e43e586aa8 -> 2013-03-13T15:45:07.788+01:00)
                                                  //| ),ComplexMaps(Map(0 -> TestAddress(Hamburg,Am Hafen), 1 -> TestAddress(New 
                                                  //| York,Broadway), 2 -> TestAddress(Los Angeles ,Sunset Boulevard)),Map(0 -> T
                                                  //| estAddress(Hamburg,Am Hafen), 1 -> TestAddress(New York,Broadway), 2 -> Tes
                                                  //| tAddress(Los Angeles ,Sunset Boulevard)),Map(x -> 2faa22e0-c3e8-4b07-a1da-d
                                                  //| 8403a66a9b1, unspecifiedProblem -> almhirt.common.UnspecifiedProblem
                                                  //| Test
                                                  //| Category: SystemProblem
                                                  //| Severity: Major
                                                  //| Arguments: Map(arg1 -> 95.0)
                                                  //| , y -> 483f4ea6-5a15-4669-98aa-5f1a562dd8b5, 1 -> TestAddress(New York,Broa
                                                  //| dway), 0 -> TestAddress(Hamburg,Am Hafen), 2 -> TestAddress(Los Angeles ,Su
                                                  //| nset Boulevard), z -> 2013-03-11T15:45:07.788+01:00)),Some(TestAddress(Berl
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