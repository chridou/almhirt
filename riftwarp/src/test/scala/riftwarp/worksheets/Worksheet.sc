package riftwarp.worksheets

import scalaz.syntax.validation._
import almhirt.syntax.almvalidation._
import riftwarp._
import riftwarp.impl._

object Worksheet {
  val riftWarp = RiftWarp.concurrentWithDefaults  //> riftWarp  : riftwarp.RiftWarp = riftwarp.RiftWarp$$anon$1@61121eb9
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
  riftWarp.barracks.addDecomposer(new TreesDecomposer())
  riftWarp.barracks.addRecomposer(new TreesRecomposer())



  val testObject = TestObjectA.pete               //> testObject  : riftwarp.TestObjectA = TestObjectA([B@28c77d04,[B@40e0a686,Pr
                                                  //| imitiveTypes(I am Pete,true,127,-237823,-278234263,265876257682376587365863
                                                  //| 876528756875682765252520577305007209857025728132213242,1.3675,1.36723223500
                                                  //| 05,23761247614876823746.23846749182408,2013-03-12T21:11:40.513+01:00,eaa5fe
                                                  //| 34-cf50-47ef-9ab4-cc6d8322a93c),PrimitiveListMAs(List(alpha, beta, gamma, d
                                                  //| elta),List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),List(1.0, 0.5, 0.2, 0.125),List(1
                                                  //| .333333, 1.33333335, 1.6666666, 1.6666667),List(2013-03-12T22:11:40.563+01:
                                                  //| 00, 2013-03-12T23:11:40.563+01:00, 2013-03-13T00:11:40.563+01:00, 2013-03-1
                                                  //| 3T01:11:40.563+01:00)),PrimitiveVectorMAs(Vector(alpha, beta, gamma, delta)
                                                  //| ,Vector(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),Vector(1.0, 0.5, 0.2, 0.125),Vector(
                                                  //| 1.333333, 1.33333335, 1.6666666, 1.6666667),Vector(2013-03-12T22:11:40.564+
                                                  //| 01:00, 2013-03-12T23:11:40.564+01:00, 2013-03-13T00:11:40.564+01:00, 2013-0
                                                  //| 3-13T01:11:40.564+01:00)),Some(PrimitiveSetMAs(Set(alpha, beta, gamma, delt
                                                  //| a),Set(5, 10, 1, 6, 9, 2, 7, 3, 8, 4),Set(1.0, 0.5, 0.2, 0.125),Set(1.33333
                                                  //| 3, 1.33333335, 1.6666666, 1.6666667),None)),PrimitiveIterableMAs(List(alpha
                                                  //| , beta, gamma, delta),List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),List(1.0, 0.5, 0.
                                                  //| 2, 0.125),List(1.333333, 1.33333335, 1.6666666, 1.6666667),List(2013-03-12T
                                                  //| 22:11:40.565+01:00, 2013-03-12T23:11:40.565+01:00, 2013-03-13T00:11:40.565+
                                                  //| 01:00, 2013-03-13T01:11:40.565+01:00)),ComplexMAs(List(TestAddress(Hamburg,
                                                  //| Am Hafen), TestAddress(New York,Broadway), TestAddress(Los Angeles ,Sunset 
                                                  //| Boulevard)),Vector(TestAddress(Hamburg,Am Hafen), TestAddress(New York,Broa
                                                  //| dway), TestAddress(Los Angeles ,Sunset Boulevard)),Set(TestAddress(Hamburg,
                                                  //| Am Hafen), TestAddress(New York,Broadway), TestAddress(Los Angeles ,Sunset 
                                                  //| Boulevard)),List(true, hello, 1, 2, 3.0, 3.0, TestAddress(Somewhere,here)))
                                                  //| ,PrimitiveMaps(Map(1 -> 10, 2 -> 20, 3 -> 30, 4 -> 40),Map(a -> 1, b -> 2, 
                                                  //| c -> 3),Map(ce2206b0-aefe-4a9e-8c96-8958bf7b7815 -> 2013-03-12T21:11:40.570
                                                  //| +01:00, 3593bf33-05e7-4ff2-9747-ca35bac15c2c -> 2013-03-13T21:11:40.570+01:
                                                  //| 00, b2a35df5-487f-4511-aedb-4352b5ea8ac6 -> 2013-03-14T21:11:40.571+01:00))
                                                  //| ,ComplexMaps(Map(0 -> TestAddress(Hamburg,Am Hafen), 1 -> TestAddress(New Y
                                                  //| ork,Broadway), 2 -> TestAddress(Los Angeles ,Sunset Boulevard)),Map(0 -> Te
                                                  //| stAddress(Hamburg,Am Hafen), 1 -> TestAddress(New York,Broadway), 2 -> Test
                                                  //| Address(Los Angeles ,Sunset Boulevard)),Map(x -> 973d081d-f011-4810-bed7-98
                                                  //| 19de7be50e, unspecifiedProblem -> almhirt.common.UnspecifiedProblem
                                                  //| Test
                                                  //| Category: SystemProblem
                                                  //| Severity: Major
                                                  //| Arguments: Map(arg1 -> 95)
                                                  //| , y -> 3af37c44-39af-46e6-8243-9f6b196bee0a, 1 -> TestAddress(New York,Broa
                                                  //| dway), 0 -> TestAddress(Hamburg,Am Hafen), 2 -> TestAddress(Los Angeles ,Su
                                                  //| nset Boulevard), unknownType -> UnknownObject(1), z -> 2013-03-12T21:11:40.
                                                  //| 578+01:00)),Some(TestAddress(Berlin,At the wall 89)),Trees(<tree>,<tree>))

  var blobdata = new scala.collection.mutable.HashMap[String, Array[Byte]]
                                                  //> blobdata  : scala.collection.mutable.HashMap[String,Array[Byte]] = Map()

  val blobDivert: BlobDivert = (arr, path) => {
    val name = java.util.UUID.randomUUID().toString
    blobdata += (name -> arr)
    RiftBlobRefByName(name).success
  }                                               //> blobDivert  : (Array[Byte], riftwarp.RiftBlobIdentifier) => almhirt.common.
                                                  //| AlmValidation[riftwarp.RiftBlob] = <function2>

  val blobFetch: BlobFetch = blob =>
    blob match {
      case RiftBlobRefByName(name) => blobdata(name).success
    }                                             //> blobFetch  : riftwarp.RiftBlob => almhirt.common.AlmValidation[Array[Byte]]
                                                  //|  = <function1>


  val warpStreamV = riftWarp.prepareForWarpWithBlobs[DimensionString](blobDivert)(RiftJson())(testObject)
                                                  //> warpStreamV  : almhirt.common.AlmValidation[riftwarp.DimensionString] = Suc
                                                  //| cess(DimensionString({"riftdesc":{"identifier":"riftwarp.TestObjectA","vers
                                                  //| ion":null},"arrayByte":[126,-123,12,-45,-128],"blob":{"riftdesc":{"identifi
                                                  //| er":"RiftBlobRefByName","version":null},"name":"a7292548-a6f3-41c6-b18e-d0a
                                                  //| c6f4b7a11"},"primitiveTypes":{"riftdesc":{"identifier":"riftwarp.PrimitiveT
                                                  //| ypes","version":null},"str":"I am Pete","bool":true,"byte":127,"int":-23782
                                                  //| 3,"long":-278234263,"bigInt":"265876257682376587365863876528756875682765252
                                                  //| 520577305007209857025728132213242","float":1.3674999475479126,"double":1.36
                                                  //| 72322350005,"bigDec":"23761247614876823746.23846749182408","dateTime":"2013
                                                  //| -03-12T21:11:40.513+01:00","uuid":"eaa5fe34-cf50-47ef-9ab4-cc6d8322a93c"},"
                                                  //| primitiveListMAs":{"riftdesc":{"identifier":"riftwarp.PrimitiveListMAs","ve
                                                  //| rsion":null},"listString":["alpha","beta","gamma","delta"],"listInt":[1,2,3
                                                  //| ,4,5,6,7,8,9,10],"listDouble":[1.0,0.5,0.2,0.125],"listBigDecimal":["1.3333
                                                  //| 33","1.33333335","1.6666666","1.6666667"],"listDateTime":["2013-03-12T22:11
                                                  //| :40.563+01:00","2013-03-12T23:11:40.563+01:00","2013-03-13T00:11:40.563+01:
                                                  //| 00","2013-03-13T01:11:40.563+01:00"]},"primitiveVectorMAs":{"riftdesc":{"id
                                                  //| entifier":"riftwarp.PrimitiveVectorMAs","version":null},"vectorString":["al
                                                  //| pha","beta","gamma","delta"],"vectorInt":[1,2,3,4,5,6,7,8,9,10],"vectorDoub
                                                  //| le":[1.0,0.5,0.2,0.125],"vectorBigDecimal":["1.333333","1.33333335","1.6666
                                                  //| 666","1.6666667"],"vectorDateTime":["2013-03-12T22:11:40.564+01:00","2013-0
                                                  //| 3-12T23:11:40.564+01:00","2013-03-13T00:11:40.564+01:00","2013-03-13T01:11:
                                                  //| 40.564+01:00"]},"primitiveSetMAs":{"riftdesc":{"identifier":"riftwarp.Primi
                                                  //| tiveSetMAs","version":null},"setString":["alpha","beta","gamma","delta"],"s
                                                  //| etInt":[5,10,1,6,9,2,7,3,8,4],"setDouble":[1.0,0.5,0.2,0.125],"setBigDecima
                                                  //| l":["1.333333","1.33333335","1.6666666","1.6666667"],"setDateTime":null},"p
                                                  //| rimitiveIterableMAs":{"riftdesc":{"identifier":"riftwarp.PrimitiveIterableM
                                                  //| As","version":null},"iterableString":["alpha","beta","gamma","delta"],"iter
                                                  //| ableInt":[1,2,3,4,5,6,7,8,9,10],"iterableDouble":[1.0,0.5,0.2,0.125],"itera
                                                  //| bleBigDecimal":["1.333333","1.33333335","1.6666666","1.6666667"],"iterableD
                                                  //| ateTime":["2013-03-12T22:11:40.565+01:00","2013-03-12T23:11:40.565+01:00","
                                                  //| 2013-03-13T00:11:40.565+01:00","2013-03-13T01:11:40.565+01:00"]},"complexMA
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
                                                  //| StringInt":[["a",1],["b",2],["c",3]],"mapUuidDateTime":[["ce2206b0-aefe-4a9
                                                  //| e-8c96-8958bf7b7815","2013-03-12T21:11:40.570+01:00"],["3593bf33-05e7-4ff2-
                                                  //| 9747-ca35bac15c2c","2013-03-13T21:11:40.570+01:00"],["b2a35df5-487f-4511-ae
                                                  //| db-4352b5ea8ac6","2013-03-14T21:11:40.571+01:00"]]},"complexMaps":{"riftdes
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
                                                  //| oulevard"}]],"mapStringAnyWithUnknown":[["x","973d081d-f011-4810-bed7-9819d
                                                  //| e7be50e"],["unspecifiedProblem",{"riftdesc":{"identifier":"almhirt.common.U
                                                  //| nspecifiedProblem","version":null},"message":"Test","severity":"Major","cat
                                                  //| egory":"SystemProblem","args":[["arg1",95]],"cause":null}],["y","3af37c44-3
                                                  //| 9af-46e6-8243-9f6b196bee0a"],["1",{"riftdesc":{"identifier":"riftwarp.TestA
                                                  //| ddress","version":null},"city":"New York","street":"Broadway"}],["0",{"rift
                                                  //| desc":{"identifier":"riftwarp.TestAddress","version":null},"city":"Hamburg"
                                                  //| ,"street":"Am Hafen"}],["2",{"riftdesc":{"identifier":"riftwarp.TestAddress
                                                  //| ","version":null},"city":"Los Angeles ","street":"Sunset Boulevard"}],["z",
                                                  //| "2013-03-12T21:11:40.578+01:00"]]},"addressOpt":{"riftdesc":{"identifier":"
                                                  //| riftwarp.TestAddress","version":null},"city":"Berlin","street":"At the wall
                                                  //|  89"},"trees":{"riftdesc":{"identifier":"riftwarp.Trees","version":null},"i
                                                  //| ntTree":[1,[[21,[[31,[]]]],[22,[]],[23,[[31,[]],[32,[[41,[]]]],[33,[]]]]]],
                                                  //| "addressTree":[{"riftdesc":{"identifier":"riftwarp.TestAddress","version":n
                                                  //| ull},"city":"Hamburg","street":"Am Hafen"},[[{"riftdesc":{"identifier":"rif
                                                  //| twarp.TestAddress","version":null},"city":"New York","street":"Broadway"},[
                                                  //| ]],[{"riftdesc":{"identifier":"riftwarp.TestAddress","version":null},"city"
                                                  //| :"Los Angeles ","street":"Sunset Boulevard"},[]]]]}}))

  println(blobdata)                               //> Map(a7292548-a6f3-41c6-b18e-d0ac6f4b7a11 -> [B@40e0a686)

  val backFromWarpV = riftWarp.receiveFromWarpWithBlobs[DimensionString, TestObjectA](blobFetch)(RiftJson())(warpStreamV.forceResult)
                                                  //> backFromWarpV  : almhirt.common.AlmValidation[riftwarp.TestObjectA] = Succe
                                                  //| ss(TestObjectA([B@23bcfa7b,[B@40e0a686,PrimitiveTypes(I am Pete,true,127,-2
                                                  //| 37823,-278234263,2658762576823765873658638765287568756827652525205773050072
                                                  //| 09857025728132213242,1.3675,1.3672322350005,23761247614876823746.2384674918
                                                  //| 2408,2013-03-12T21:11:40.513+01:00,eaa5fe34-cf50-47ef-9ab4-cc6d8322a93c),Pr
                                                  //| imitiveListMAs(List(alpha, beta, gamma, delta),List(1, 2, 3, 4, 5, 6, 7, 8,
                                                  //|  9, 10),List(1.0, 0.5, 0.2, 0.125),List(1.333333, 1.33333335, 1.6666666, 1.
                                                  //| 6666667),List(2013-03-12T22:11:40.563+01:00, 2013-03-12T23:11:40.563+01:00,
                                                  //|  2013-03-13T00:11:40.563+01:00, 2013-03-13T01:11:40.563+01:00)),PrimitiveVe
                                                  //| ctorMAs(Vector(alpha, beta, gamma, delta),Vector(1, 2, 3, 4, 5, 6, 7, 8, 9,
                                                  //|  10),Vector(1.0, 0.5, 0.2, 0.125),Vector(1.333333, 1.33333335, 1.6666666, 1
                                                  //| .6666667),Vector(2013-03-12T22:11:40.564+01:00, 2013-03-12T23:11:40.564+01:
                                                  //| 00, 2013-03-13T00:11:40.564+01:00, 2013-03-13T01:11:40.564+01:00)),Some(Pri
                                                  //| mitiveSetMAs(Set(alpha, beta, gamma, delta),Set(5, 10, 1, 6, 9, 2, 7, 3, 8,
                                                  //|  4),Set(1.0, 0.5, 0.2, 0.125),Set(1.333333, 1.33333335, 1.6666666, 1.666666
                                                  //| 7),None)),PrimitiveIterableMAs(Set(alpha, beta, gamma, delta),Set(5, 10, 1,
                                                  //|  6, 9, 2, 7, 3, 8, 4),Set(1.0, 0.5, 0.2, 0.125),Set(1.333333, 1.33333335, 1
                                                  //| .6666666, 1.6666667),Set(2013-03-12T22:11:40.565+01:00, 2013-03-12T23:11:40
                                                  //| .565+01:00, 2013-03-13T00:11:40.565+01:00, 2013-03-13T01:11:40.565+01:00)),
                                                  //| ComplexMAs(List(TestAddress(Hamburg,Am Hafen), TestAddress(New York,Broadwa
                                                  //| y), TestAddress(Los Angeles ,Sunset Boulevard)),Vector(TestAddress(Hamburg,
                                                  //| Am Hafen), TestAddress(New York,Broadway), TestAddress(Los Angeles ,Sunset 
                                                  //| Boulevard)),Set(TestAddress(Hamburg,Am Hafen), TestAddress(New York,Broadwa
                                                  //| y), TestAddress(Los Angeles ,Sunset Boulevard)),List(true, hello, 1.0, 2.0,
                                                  //|  3.0, 3.0, TestAddress(Somewhere,here))),PrimitiveMaps(Map(1 -> 10, 2 -> 20
                                                  //| , 3 -> 30, 4 -> 40),Map(a -> 1, b -> 2, c -> 3),Map(ce2206b0-aefe-4a9e-8c96
                                                  //| -8958bf7b7815 -> 2013-03-12T21:11:40.570+01:00, 3593bf33-05e7-4ff2-9747-ca3
                                                  //| 5bac15c2c -> 2013-03-13T21:11:40.570+01:00, b2a35df5-487f-4511-aedb-4352b5e
                                                  //| a8ac6 -> 2013-03-14T21:11:40.571+01:00)),ComplexMaps(Map(0 -> TestAddress(H
                                                  //| amburg,Am Hafen), 1 -> TestAddress(New York,Broadway), 2 -> TestAddress(Los
                                                  //|  Angeles ,Sunset Boulevard)),Map(0 -> TestAddress(Hamburg,Am Hafen), 1 -> T
                                                  //| estAddress(New York,Broadway), 2 -> TestAddress(Los Angeles ,Sunset Bouleva
                                                  //| rd)),Map(x -> 973d081d-f011-4810-bed7-9819de7be50e, unspecifiedProblem -> a
                                                  //| lmhirt.common.UnspecifiedProblem
                                                  //| Test
                                                  //| Category: SystemProblem
                                                  //| Severity: Major
                                                  //| Arguments: Map(arg1 -> 95.0)
                                                  //| , y -> 3af37c44-39af-46e6-8243-9f6b196bee0a, 1 -> TestAddress(New York,Broa
                                                  //| dway), 0 -> TestAddress(Hamburg,Am Hafen), 2 -> TestAddress(Los Angeles ,Su
                                                  //| nset Boulevard), z -> 2013-03-12T21:11:40.578+01:00)),Some(TestAddress(Berl
                                                  //| in,At the wall 89)),Trees(<tree>,<tree>)))

  val backFromWarp = backFromWarpV.forceResult    //> backFromWarp  : riftwarp.TestObjectA = TestObjectA([B@23bcfa7b,[B@40e0a686,
                                                  //| PrimitiveTypes(I am Pete,true,127,-237823,-278234263,2658762576823765873658
                                                  //| 63876528756875682765252520577305007209857025728132213242,1.3675,1.367232235
                                                  //| 0005,23761247614876823746.23846749182408,2013-03-12T21:11:40.513+01:00,eaa5
                                                  //| fe34-cf50-47ef-9ab4-cc6d8322a93c),PrimitiveListMAs(List(alpha, beta, gamma,
                                                  //|  delta),List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),List(1.0, 0.5, 0.2, 0.125),List
                                                  //| (1.333333, 1.33333335, 1.6666666, 1.6666667),List(2013-03-12T22:11:40.563+0
                                                  //| 1:00, 2013-03-12T23:11:40.563+01:00, 2013-03-13T00:11:40.563+01:00, 2013-03
                                                  //| -13T01:11:40.563+01:00)),PrimitiveVectorMAs(Vector(alpha, beta, gamma, delt
                                                  //| a),Vector(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),Vector(1.0, 0.5, 0.2, 0.125),Vecto
                                                  //| r(1.333333, 1.33333335, 1.6666666, 1.6666667),Vector(2013-03-12T22:11:40.56
                                                  //| 4+01:00, 2013-03-12T23:11:40.564+01:00, 2013-03-13T00:11:40.564+01:00, 2013
                                                  //| -03-13T01:11:40.564+01:00)),Some(PrimitiveSetMAs(Set(alpha, beta, gamma, de
                                                  //| lta),Set(5, 10, 1, 6, 9, 2, 7, 3, 8, 4),Set(1.0, 0.5, 0.2, 0.125),Set(1.333
                                                  //| 333, 1.33333335, 1.6666666, 1.6666667),None)),PrimitiveIterableMAs(Set(alph
                                                  //| a, beta, gamma, delta),Set(5, 10, 1, 6, 9, 2, 7, 3, 8, 4),Set(1.0, 0.5, 0.2
                                                  //| , 0.125),Set(1.333333, 1.33333335, 1.6666666, 1.6666667),Set(2013-03-12T22:
                                                  //| 11:40.565+01:00, 2013-03-12T23:11:40.565+01:00, 2013-03-13T00:11:40.565+01:
                                                  //| 00, 2013-03-13T01:11:40.565+01:00)),ComplexMAs(List(TestAddress(Hamburg,Am 
                                                  //| Hafen), TestAddress(New York,Broadway), TestAddress(Los Angeles ,Sunset Bou
                                                  //| levard)),Vector(TestAddress(Hamburg,Am Hafen), TestAddress(New York,Broadwa
                                                  //| y), TestAddress(Los Angeles ,Sunset Boulevard)),Set(TestAddress(Hamburg,Am 
                                                  //| Hafen), TestAddress(New York,Broadway), TestAddress(Los Angeles ,Sunset Bou
                                                  //| levard)),List(true, hello, 1.0, 2.0, 3.0, 3.0, TestAddress(Somewhere,here))
                                                  //| ),PrimitiveMaps(Map(1 -> 10, 2 -> 20, 3 -> 30, 4 -> 40),Map(a -> 1, b -> 2,
                                                  //|  c -> 3),Map(ce2206b0-aefe-4a9e-8c96-8958bf7b7815 -> 2013-03-12T21:11:40.57
                                                  //| 0+01:00, 3593bf33-05e7-4ff2-9747-ca35bac15c2c -> 2013-03-13T21:11:40.570+01
                                                  //| :00, b2a35df5-487f-4511-aedb-4352b5ea8ac6 -> 2013-03-14T21:11:40.571+01:00)
                                                  //| ),ComplexMaps(Map(0 -> TestAddress(Hamburg,Am Hafen), 1 -> TestAddress(New 
                                                  //| York,Broadway), 2 -> TestAddress(Los Angeles ,Sunset Boulevard)),Map(0 -> T
                                                  //| estAddress(Hamburg,Am Hafen), 1 -> TestAddress(New York,Broadway), 2 -> Tes
                                                  //| tAddress(Los Angeles ,Sunset Boulevard)),Map(x -> 973d081d-f011-4810-bed7-9
                                                  //| 819de7be50e, unspecifiedProblem -> almhirt.common.UnspecifiedProblem
                                                  //| Test
                                                  //| Category: SystemProblem
                                                  //| Severity: Major
                                                  //| Arguments: Map(arg1 -> 95.0)
                                                  //| , y -> 3af37c44-39af-46e6-8243-9f6b196bee0a, 1 -> TestAddress(New York,Broa
                                                  //| dway), 0 -> TestAddress(Hamburg,Am Hafen), 2 -> TestAddress(Los Angeles ,Su
                                                  //| nset Boulevard), z -> 2013-03-12T21:11:40.578+01:00)),Some(TestAddress(Berl
                                                  //| in,At the wall 89)),Trees(<tree>,<tree>))

  testObject == backFromWarp                      //> res0: Boolean = false

  testObject.primitiveTypes == backFromWarp.primitiveTypes
                                                  //> res1: Boolean = true
  
  
  
  val warpStreamVXml = riftWarp.prepareForWarpWithBlobs[DimensionNiceString](blobDivert)(RiftXml())(testObject)
                                                  //> warpStreamVXml  : almhirt.common.AlmValidation[riftwarp.DimensionNiceString
                                                  //| ] = Success(DimensionNiceString(<TestObjectA type="riftwarp.TestObjectA" st
                                                  //| yle="noisy"><arrayByte><bytes>126,-123,12,-45,-128</bytes></arrayByte><blob
                                                  //| ><RiftBlobRefByName type="RiftBlobRefByName" style="noisy"><name><v type="S
                                                  //| tring">5ab3698a-b9c8-4deb-9368-34221531450b</v></name></RiftBlobRefByName><
                                                  //| /blob><primitiveTypes><PrimitiveTypes type="riftwarp.PrimitiveTypes" style=
                                                  //| "noisy"><str><v type="String">I am Pete</v></str><bool><v type="Boolean">tr
                                                  //| ue</v></bool><byte><v type="Byte">127</v></byte><int><v type="Int">-237823<
                                                  //| /v></int><long><v type="Long">-278234263</v></long><bigInt><v type="BigInt"
                                                  //| >26587625768237658736586387652875687568276525252057730500720985702572813221
                                                  //| 3242</v></bigInt><float><v type="Float">1.3675</v></float><double><v type="
                                                  //| Double">1.3672322350005</v></double><bigDec><v type="BigDecimal">2376124761
                                                  //| 4876823746.23846749182408</v></bigDec><dateTime><v type="DateTime">2013-03-
                                                  //| 12T21:11:40.513+01:00</v></dateTime><uuid><v type="Uuid">eaa5fe34-cf50-47ef
                                                  //| -9ab4-cc6d8322a93c</v></uuid></PrimitiveTypes></primitiveTypes><primitiveLi
                                                  //| stMAs><PrimitiveListMAs type="riftwarp.PrimitiveListMAs" style="noisy"><lis
                                                  //| tString><items><v type="String">alpha</v><v type="String">beta</v><v type="
                                                  //| String">gamma</v><v type="String">delta</v></items></listString><listInt><i
                                                  //| tems><v type="Int">1</v><v type="Int">2</v><v type="Int">3</v><v type="Int"
                                                  //| >4</v><v type="Int">5</v><v type="Int">6</v><v type="Int">7</v><v type="Int
                                                  //| ">8</v><v type="Int">9</v><v type="Int">10</v></items></listInt><listDouble
                                                  //| ><items><v type="Double">1.0</v><v type="Double">0.5</v><v type="Double">0.
                                                  //| 2</v><v type="Double">0.125</v></items></listDouble><listBigDecimal><items>
                                                  //| <v type="BigDecimal">1.333333</v><v type="BigDecimal">1.33333335</v><v type
                                                  //| ="BigDecimal">1.6666666</v><v type="BigDecimal">1.6666667</v></items></list
                                                  //| BigDecimal><listDateTime><items><v type="DateTime">2013-03-12T22:11:40.563+
                                                  //| 01:00</v><v type="DateTime">2013-03-12T23:11:40.563+01:00</v><v type="DateT
                                                  //| ime">2013-03-13T00:11:40.563+01:00</v><v type="DateTime">2013-03-13T01:11:4
                                                  //| 0.563+01:00</v></items></listDateTime></PrimitiveListMAs></primitiveListMAs
                                                  //| ><primitiveVectorMAs><PrimitiveVectorMAs type="riftwarp.PrimitiveVectorMAs"
                                                  //|  style="noisy"><vectorString><items><v type="String">alpha</v><v type="Stri
                                                  //| ng">beta</v><v type="String">gamma</v><v type="String">delta</v></items></v
                                                  //| ectorString><vectorInt><items><v type="Int">1</v><v type="Int">2</v><v type
                                                  //| ="Int">3</v><v type="Int">4</v><v type="Int">5</v><v type="Int">6</v><v typ
                                                  //| e="Int">7</v><v type="Int">8</v><v type="Int">9</v><v type="Int">10</v></it
                                                  //| ems></vectorInt><vectorDouble><items><v type="Double">1.0</v><v type="Doubl
                                                  //| e">0.5</v><v type="Double">0.2</v><v type="Double">0.125</v></items></vecto
                                                  //| rDouble><vectorBigDecimal><items><v type="BigDecimal">1.333333</v><v type="
                                                  //| BigDecimal">1.33333335</v><v type="BigDecimal">1.6666666</v><v type="BigDec
                                                  //| imal">1.6666667</v></items></vectorBigDecimal><vectorDateTime><items><v typ
                                                  //| e="DateTime">2013-03-12T22:11:40.564+01:00</v><v type="DateTime">2013-03-12
                                                  //| T23:11:40.564+01:00</v><v type="DateTime">2013-03-13T00:11:40.564+01:00</v>
                                                  //| <v type="DateTime">2013-03-13T01:11:40.564+01:00</v></items></vectorDateTim
                                                  //| e></PrimitiveVectorMAs></primitiveVectorMAs><primitiveSetMAs><PrimitiveSetM
                                                  //| As type="riftwarp.PrimitiveSetMAs" style="noisy"><setString><items><v type=
                                                  //| "String">alpha</v><v type="String">beta</v><v type="String">gamma</v><v typ
                                                  //| e="String">delta</v></items></setString><setInt><items><v type="Int">5</v><
                                                  //| v type="Int">10</v><v type="Int">1</v><v type="Int">6</v><v type="Int">9</v
                                                  //| ><v type="Int">2</v><v type="Int">7</v><v type="Int">3</v><v type="Int">8</
                                                  //| v><v type="Int">4</v></items></setInt><setDouble><items><v type="Double">1.
                                                  //| 0</v><v type="Double">0.5</v><v type="Double">0.2</v><v type="Double">0.125
                                                  //| </v></items></setDouble><setBigDecimal><items><v type="BigDecimal">1.333333
                                                  //| </v><v type="BigDecimal">1.33333335</v><v type="BigDecimal">1.6666666</v><v
                                                  //|  type="BigDecimal">1.6666667</v></items></setBigDecimal></PrimitiveSetMAs><
                                                  //| /primitiveSetMAs><primitiveIterableMAs><PrimitiveIterableMAs type="riftwarp
                                                  //| .PrimitiveIterableMAs" style="noisy"><iterableString><items><v type="String
                                                  //| ">alpha</v><v type="String">beta</v><v type="String">gamma</v><v type="Stri
                                                  //| ng">delta</v></items></iterableString><iterableInt><items><v type="Int">1</
                                                  //| v><v type="Int">2</v><v type="Int">3</v><v type="Int">4</v><v type="Int">5<
                                                  //| /v><v type="Int">6</v><v type="Int">7</v><v type="Int">8</v><v type="Int">9
                                                  //| </v><v type="Int">10</v></items></iterableInt><iterableDouble><items><v typ
                                                  //| e="Double">1.0</v><v type="Double">0.5</v><v type="Double">0.2</v><v type="
                                                  //| Double">0.125</v></items></iterableDouble><iterableBigDecimal><items><v typ
                                                  //| e="BigDecimal">1.333333</v><v type="BigDecimal">1.33333335</v><v type="BigD
                                                  //| ecimal">1.6666666</v><v type="BigDecimal">1.6666667</v></items></iterableBi
                                                  //| gDecimal><iterableDateTime><items><v type="DateTime">2013-03-12T22:11:40.56
                                                  //| 5+01:00</v><v type="DateTime">2013-03-12T23:11:40.565+01:00</v><v type="Dat
                                                  //| eTime">2013-03-13T00:11:40.565+01:00</v><v type="DateTime">2013-03-13T01:11
                                                  //| :40.565+01:00</v></items></iterableDateTime></PrimitiveIterableMAs></primit
                                                  //| iveIterableMAs><complexMAs><ComplexMAs type="riftwarp.ComplexMAs" style="no
                                                  //| isy"><addresses1><items><TestAddress type="riftwarp.TestAddress" style="noi
                                                  //| sy"><city><v type="String">Hamburg</v></city><street><v type="String">Am Ha
                                                  //| fen</v></street></TestAddress><TestAddress type="riftwarp.TestAddress" styl
                                                  //| e="noisy"><city><v type="String">New York</v></city><street><v type="String
                                                  //| ">Broadway</v></street></TestAddress><TestAddress type="riftwarp.TestAddres
                                                  //| s" style="noisy"><city><v type="String">Los Angeles </v></city><street><v t
                                                  //| ype="String">Sunset Boulevard</v></street></TestAddress></items></addresses
                                                  //| 1><addresses2><items><TestAddress type="riftwarp.TestAddress" style="noisy"
                                                  //| ><city><v type="String">Hamburg</v></city><street><v type="String">Am Hafen
                                                  //| </v></street></TestAddress><TestAddress type="riftwarp.TestAddress" style="
                                                  //| noisy"><city><v type="String">New York</v></city><street><v type="String">B
                                                  //| roadway</v></street></TestAddress><TestAddress type="riftwarp.TestAddress" 
                                                  //| style="noisy"><city><v type="String">Los Angeles </v></city><street><v type
                                                  //| ="String">Sunset Boulevard</v></street></TestAddress></items></addresses2><
                                                  //| addresses3><items><TestAddress type="riftwarp.TestAddress" style="noisy"><c
                                                  //| ity><v type="String">Hamburg</v></city><street><v type="String">Am Hafen</v
                                                  //| ></street></TestAddress><TestAddress type="riftwarp.TestAddress" style="noi
                                                  //| sy"><city><v type="String">New York</v></city><street><v type="String">Broa
                                                  //| dway</v></street></TestAddress><TestAddress type="riftwarp.TestAddress" sty
                                                  //| le="noisy"><city><v type="String">Los Angeles </v></city><street><v type="S
                                                  //| tring">Sunset Boulevard</v></street></TestAddress></items></addresses3><any
                                                  //| thing><items><v type="Boolean">true</v><v type="String">hello</v><v type="I
                                                  //| nt">1</v><v type="Long">2</v><v type="Double">3.0</v><v type="Float">3.0</v
                                                  //| ><TestAddress type="riftwarp.TestAddress" style="noisy"><city><v type="Stri
                                                  //| ng">Somewhere</v></city><street><v type="String">here</v></street></TestAdd
                                                  //| ress></items></anything></ComplexMAs></complexMAs><primitiveMaps><Primitive
                                                  //| Maps type="riftwarp.PrimitiveMaps" style="noisy"><mapIntInt><items><items><
                                                  //| v type="Int">1</v><v type="Int">10</v></items><items><v type="Int">2</v><v 
                                                  //| type="Int">20</v></items><items><v type="Int">3</v><v type="Int">30</v></it
                                                  //| ems><items><v type="Int">4</v><v type="Int">40</v></items></items></mapIntI
                                                  //| nt><mapStringInt><items><items><v type="String">a</v><v type="Int">1</v></i
                                                  //| tems><items><v type="String">b</v><v type="Int">2</v></items><items><v type
                                                  //| ="String">c</v><v type="Int">3</v></items></items></mapStringInt><mapUuidDa
                                                  //| teTime><items><items><v type="Uuid">ce2206b0-aefe-4a9e-8c96-8958bf7b7815</v
                                                  //| ><v type="DateTime">2013-03-12T21:11:40.570+01:00</v></items><items><v type
                                                  //| ="Uuid">3593bf33-05e7-4ff2-9747-ca35bac15c2c</v><v type="DateTime">2013-03-
                                                  //| 13T21:11:40.570+01:00</v></items><items><v type="Uuid">b2a35df5-487f-4511-a
                                                  //| edb-4352b5ea8ac6</v><v type="DateTime">2013-03-14T21:11:40.571+01:00</v></i
                                                  //| tems></items></mapUuidDateTime></PrimitiveMaps></primitiveMaps><complexMaps
                                                  //| ><ComplexMaps type="riftwarp.ComplexMaps" style="noisy"><mapIntTestAddress1
                                                  //| ><items><items><v type="Int">0</v><TestAddress type="riftwarp.TestAddress" 
                                                  //| style="noisy"><city><v type="String">Hamburg</v></city><street><v type="Str
                                                  //| ing">Am Hafen</v></street></TestAddress></items><items><v type="Int">1</v><
                                                  //| TestAddress type="riftwarp.TestAddress" style="noisy"><city><v type="String
                                                  //| ">New York</v></city><street><v type="String">Broadway</v></street></TestAd
                                                  //| dress></items><items><v type="Int">2</v><TestAddress type="riftwarp.TestAdd
                                                  //| ress" style="noisy"><city><v type="String">Los Angeles </v></city><street><
                                                  //| v type="String">Sunset Boulevard</v></street></TestAddress></items></items>
                                                  //| </mapIntTestAddress1><mapIntAny><items><items><v type="Int">0</v><TestAddre
                                                  //| ss type="riftwarp.TestAddress" style="noisy"><city><v type="String">Hamburg
                                                  //| </v></city><street><v type="String">Am Hafen</v></street></TestAddress></it
                                                  //| ems><items><v type="Int">1</v><TestAddress type="riftwarp.TestAddress" styl
                                                  //| e="noisy"><city><v type="String">New York</v></city><street><v type="String
                                                  //| ">Broadway</v></street></TestAddress></items><items><v type="Int">2</v><Tes
                                                  //| tAddress type="riftwarp.TestAddress" style="noisy"><city><v type="String">L
                                                  //| os Angeles </v></city><street><v type="String">Sunset Boulevard</v></street
                                                  //| ></TestAddress></items></items></mapIntAny><mapStringAnyWithUnknown><items>
                                                  //| <items><v type="String">x</v><v type="Uuid">973d081d-f011-4810-bed7-9819de7
                                                  //| be50e</v></items><items><v type="String">unspecifiedProblem</v><Unspecified
                                                  //| Problem type="almhirt.common.UnspecifiedProblem" style="noisy"><message><v 
                                                  //| type="String">Test</v></message><severity><v type="String">Major</v></sever
                                                  //| ity><category><v type="String">SystemProblem</v></category><args><items><it
                                                  //| ems><v type="String">arg1</v><v type="Int">95</v></items></items></args></U
                                                  //| nspecifiedProblem></items><items><v type="String">y</v><v type="Uuid">3af37
                                                  //| c44-39af-46e6-8243-9f6b196bee0a</v></items><items><v type="String">1</v><Te
                                                  //| stAddress type="riftwarp.TestAddress" style="noisy"><city><v type="String">
                                                  //| New York</v></city><street><v type="String">Broadway</v></street></TestAddr
                                                  //| ess></items><items><v type="String">0</v><TestAddress type="riftwarp.TestAd
                                                  //| dress" style="noisy"><city><v type="String">Hamburg</v></city><street><v ty
                                                  //| pe="String">Am Hafen</v></street></TestAddress></items><items><v type="Stri
                                                  //| ng">2</v><TestAddress type="riftwarp.TestAddress" style="noisy"><city><v ty
                                                  //| pe="String">Los Angeles </v></city><street><v type="String">Sunset Boulevar
                                                  //| d</v></street></TestAddress></items><items><v type="String">z</v><v type="D
                                                  //| ateTime">2013-03-12T21:11:40.578+01:00</v></items></items></mapStringAnyWit
                                                  //| hUnknown></ComplexMaps></complexMaps><addressOpt><TestAddress type="riftwar
                                                  //| p.TestAddress" style="noisy"><city><v type="String">Berlin</v></city><stree
                                                  //| t><v type="String">At the wall 89</v></street></TestAddress></addressOpt><t
                                                  //| rees><Trees type="riftwarp.Trees" style="noisy"><intTree><items><v type="In
                                                  //| t">1</v><items><items><v type="Int">21</v><items><items><v type="Int">31</v
                                                  //| ><items></items></items></items></items><items><v type="Int">22</v><items><
                                                  //| /items></items><items><v type="Int">23</v><items><items><v type="Int">31</v
                                                  //| ><items></items></items><items><v type="Int">32</v><items><items><v type="I
                                                  //| nt">41</v><items></items></items></items></items><items><v type="Int">33</v
                                                  //| ><items></items></items></items></items></items></items></intTree><addressT
                                                  //| ree><items><TestAddress type="riftwarp.TestAddress" style="noisy"><city><v 
                                                  //| type="String">Hamburg</v></city><street><v type="String">Am Hafen</v></stre
                                                  //| et></TestAddress><items><items><TestAddress type="riftwarp.TestAddress" sty
                                                  //| le="noisy"><city><v type="String">New York</v></city><street><v type="Strin
                                                  //| g">Broadway</v></street></TestAddress><items></items></items><items><TestAd
                                                  //| dress type="riftwarp.TestAddress" style="noisy"><city><v type="String">Los 
                                                  //| Angeles </v></city><street><v type="String">Sunset Boulevard</v></street></
                                                  //| TestAddress><items></items></items></items></items></addressTree></Trees></
                                                  //| trees></TestObjectA>))

  println(blobdata)                               //> Map(a7292548-a6f3-41c6-b18e-d0ac6f4b7a11 -> [B@40e0a686, 5ab3698a-b9c8-4deb
                                                  //| -9368-34221531450b -> [B@40e0a686)

  riftWarp.prepareForWarp[DimensionCord](RiftJson())(testObject).flatMap(warpStream =>
    riftWarp.receiveFromWarp[DimensionCord, TestObjectA](RiftJson())(warpStream)).map(rearrived =>
    rearrived == testObject)                      //> res2: scalaz.Validation[almhirt.common.Problem,Boolean] = Success(false)

  //  riftWarp.prepareForWarp[DimensionRawMap](RiftMap())(testObject).flatMap(warpStream =>
  //    riftWarp.receiveFromWarp[DimensionRawMap, TestObjectA](RiftMap())(warpStream)).map(rearrived =>
  //    rearrived == testObject)

}