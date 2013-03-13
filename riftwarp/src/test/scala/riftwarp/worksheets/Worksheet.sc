package riftwarp.worksheets

import scalaz.syntax.validation._
import almhirt.syntax.almvalidation._
import riftwarp._
import riftwarp.impl._


object Worksheet {
  val riftWarp = RiftWarp.concurrentWithDefaults  //> riftWarp  : riftwarp.RiftWarp = riftwarp.RiftWarp$$anon$1@2208f8b6
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

  val testObject = TestObjectA.pete               //> testObject  : riftwarp.TestObjectA = TestObjectA([B@59298a3b,[B@36325538,Pr
                                                  //| imitiveTypes(I am Pete,true,127,-237823,-278234263,265876257682376587365863
                                                  //| 876528756875682765252520577305007209857025728132213242,1.3675,1.36723223500
                                                  //| 05,23761247614876823746.23846749182408,2013-03-13T10:32:04.455+01:00,29cbe9
                                                  //| b7-62c3-4ef9-897c-9d0703802d27),PrimitiveListMAs(List(alpha, beta, gamma, d
                                                  //| elta),List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),List(1.0, 0.5, 0.2, 0.125),List(1
                                                  //| .333333, 1.33333335, 1.6666666, 1.6666667),List(2013-03-13T11:32:04.517+01:
                                                  //| 00, 2013-03-13T12:32:04.517+01:00, 2013-03-13T13:32:04.517+01:00, 2013-03-1
                                                  //| 3T14:32:04.517+01:00)),PrimitiveVectorMAs(Vector(alpha, beta, gamma, delta)
                                                  //| ,Vector(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),Vector(1.0, 0.5, 0.2, 0.125),Vector(
                                                  //| 1.333333, 1.33333335, 1.6666666, 1.6666667),Vector(2013-03-13T11:32:04.517+
                                                  //| 01:00, 2013-03-13T12:32:04.517+01:00, 2013-03-13T13:32:04.517+01:00, 2013-0
                                                  //| 3-13T14:32:04.517+01:00)),Some(PrimitiveSetMAs(Set(alpha, beta, gamma, delt
                                                  //| a),Set(5, 10, 1, 6, 9, 2, 7, 3, 8, 4),Set(1.0, 0.5, 0.2, 0.125),Set(1.33333
                                                  //| 3, 1.33333335, 1.6666666, 1.6666667),None)),PrimitiveIterableMAs(List(alpha
                                                  //| , beta, gamma, delta),List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),List(1.0, 0.5, 0.
                                                  //| 2, 0.125),List(1.333333, 1.33333335, 1.6666666, 1.6666667),List(2013-03-13T
                                                  //| 11:32:04.517+01:00, 2013-03-13T12:32:04.517+01:00, 2013-03-13T13:32:04.517+
                                                  //| 01:00, 2013-03-13T14:32:04.517+01:00)),ComplexMAs(List(TestAddress(Hamburg,
                                                  //| Am Hafen), TestAddress(New York,Broadway), TestAddress(Los Angeles ,Sunset 
                                                  //| Boulevard)),Vector(TestAddress(Hamburg,Am Hafen), TestAddress(New York,Broa
                                                  //| dway), TestAddress(Los Angeles ,Sunset Boulevard)),Set(TestAddress(Hamburg,
                                                  //| Am Hafen), TestAddress(New York,Broadway), TestAddress(Los Angeles ,Sunset 
                                                  //| Boulevard)),List(true, hello, 1, 2, 3.0, 3.0, TestAddress(Somewhere,here)))
                                                  //| ,PrimitiveMaps(Map(1 -> 10, 2 -> 20, 3 -> 30, 4 -> 40),Map(a -> 1, b -> 2, 
                                                  //| c -> 3),Map(cb240411-53d3-4edf-b063-5b544c59d1b1 -> 2013-03-13T10:32:04.533
                                                  //| +01:00, 94de2249-19f4-44b6-9b9f-74882c7b496c -> 2013-03-14T10:32:04.533+01:
                                                  //| 00, b0f22650-f291-42c9-a70c-e7e69a6ca758 -> 2013-03-15T10:32:04.533+01:00))
                                                  //| ,ComplexMaps(Map(0 -> TestAddress(Hamburg,Am Hafen), 1 -> TestAddress(New Y
                                                  //| ork,Broadway), 2 -> TestAddress(Los Angeles ,Sunset Boulevard)),Map(0 -> Te
                                                  //| stAddress(Hamburg,Am Hafen), 1 -> TestAddress(New York,Broadway), 2 -> Test
                                                  //| Address(Los Angeles ,Sunset Boulevard)),Map(x -> d1ef752c-a705-4582-b2cf-b1
                                                  //| 4500f79861, unspecifiedProblem -> almhirt.common.UnspecifiedProblem
                                                  //| Test
                                                  //| Category: SystemProblem
                                                  //| Severity: Major
                                                  //| Arguments: Map(arg1 -> 95)
                                                  //| , y -> 48529d37-c37a-4ebf-8529-96535461cf5f, 1 -> TestAddress(New York,Broa
                                                  //| dway), 0 -> TestAddress(Hamburg,Am Hafen), 2 -> TestAddress(Los Angeles ,Su
                                                  //| nset Boulevard), unknownType -> UnknownObject(1), z -> 2013-03-13T10:32:04.
                                                  //| 533+01:00)),Some(TestAddress(Berlin,At the wall 89)),Trees(<tree>,<tree>))
                                                  //| 

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
                                                  //| er":"RiftBlobRefByName","version":null},"name":"b7896d8f-6128-4e71-8c30-40c
                                                  //| 01a26fd73"},"primitiveTypes":{"riftdesc":{"identifier":"riftwarp.PrimitiveT
                                                  //| ypes","version":null},"str":"I am Pete","bool":true,"byte":127,"int":-23782
                                                  //| 3,"long":-278234263,"bigInt":"265876257682376587365863876528756875682765252
                                                  //| 520577305007209857025728132213242","float":1.3674999475479126,"double":1.36
                                                  //| 72322350005,"bigDec":"23761247614876823746.23846749182408","dateTime":"2013
                                                  //| -03-13T10:32:04.455+01:00","uuid":"29cbe9b7-62c3-4ef9-897c-9d0703802d27"},"
                                                  //| primitiveListMAs":{"riftdesc":{"identifier":"riftwarp.PrimitiveListMAs","ve
                                                  //| rsion":null},"listString":["alpha","beta","gamma","delta"],"listInt":[1,2,3
                                                  //| ,4,5,6,7,8,9,10],"listDouble":[1.0,0.5,0.2,0.125],"listBigDecimal":["1.3333
                                                  //| 33","1.33333335","1.6666666","1.6666667"],"listDateTime":["2013-03-13T11:32
                                                  //| :04.517+01:00","2013-03-13T12:32:04.517+01:00","2013-03-13T13:32:04.517+01:
                                                  //| 00","2013-03-13T14:32:04.517+01:00"]},"primitiveVectorMAs":{"riftdesc":{"id
                                                  //| entifier":"riftwarp.PrimitiveVectorMAs","version":null},"vectorString":["al
                                                  //| pha","beta","gamma","delta"],"vectorInt":[1,2,3,4,5,6,7,8,9,10],"vectorDoub
                                                  //| le":[1.0,0.5,0.2,0.125],"vectorBigDecimal":["1.333333","1.33333335","1.6666
                                                  //| 666","1.6666667"],"vectorDateTime":["2013-03-13T11:32:04.517+01:00","2013-0
                                                  //| 3-13T12:32:04.517+01:00","2013-03-13T13:32:04.517+01:00","2013-03-13T14:32:
                                                  //| 04.517+01:00"]},"primitiveSetMAs":{"riftdesc":{"identifier":"riftwarp.Primi
                                                  //| tiveSetMAs","version":null},"setString":["alpha","beta","gamma","delta"],"s
                                                  //| etInt":[5,10,1,6,9,2,7,3,8,4],"setDouble":[1.0,0.5,0.2,0.125],"setBigDecima
                                                  //| l":["1.333333","1.33333335","1.6666666","1.6666667"],"setDateTime":null},"p
                                                  //| rimitiveIterableMAs":{"riftdesc":{"identifier":"riftwarp.PrimitiveIterableM
                                                  //| As","version":null},"iterableString":["alpha","beta","gamma","delta"],"iter
                                                  //| ableInt":[1,2,3,4,5,6,7,8,9,10],"iterableDouble":[1.0,0.5,0.2,0.125],"itera
                                                  //| bleBigDecimal":["1.333333","1.33333335","1.6666666","1.6666667"],"iterableD
                                                  //| ateTime":["2013-03-13T11:32:04.517+01:00","2013-03-13T12:32:04.517+01:00","
                                                  //| 2013-03-13T13:32:04.517+01:00","2013-03-13T14:32:04.517+01:00"]},"complexMA
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
                                                  //| StringInt":[["a",1],["b",2],["c",3]],"mapUuidDateTime":[["cb240411-53d3-4ed
                                                  //| f-b063-5b544c59d1b1","2013-03-13T10:32:04.533+01:00"],["94de2249-19f4-44b6-
                                                  //| 9b9f-74882c7b496c","2013-03-14T10:32:04.533+01:00"],["b0f22650-f291-42c9-a7
                                                  //| 0c-e7e69a6ca758","2013-03-15T10:32:04.533+01:00"]]},"complexMaps":{"riftdes
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
                                                  //| oulevard"}]],"mapStringAnyWithUnknown":[["x","d1ef752c-a705-4582-b2cf-b1450
                                                  //| 0f79861"],["unspecifiedProblem",{"riftdesc":{"identifier":"almhirt.common.U
                                                  //| nspecifiedProblem","version":null},"message":"Test","severity":"Major","cat
                                                  //| egory":"SystemProblem","args":[["arg1",95]],"cause":null}],["y","48529d37-c
                                                  //| 37a-4ebf-8529-96535461cf5f"],["1",{"riftdesc":{"identifier":"riftwarp.TestA
                                                  //| ddress","version":null},"city":"New York","street":"Broadway"}],["0",{"rift
                                                  //| desc":{"identifier":"riftwarp.TestAddress","version":null},"city":"Hamburg"
                                                  //| ,"street":"Am Hafen"}],["2",{"riftdesc":{"identifier":"riftwarp.TestAddress
                                                  //| ","version":null},"city":"Los Angeles ","street":"Sunset Boulevard"}],["z",
                                                  //| "2013-03-13T10:32:04.533+01:00"]]},"addressOpt":{"riftdesc":{"identifier":"
                                                  //| riftwarp.TestAddress","version":null},"city":"Berlin","street":"At the wall
                                                  //|  89"},"trees":{"riftdesc":{"identifier":"riftwarp.Trees","version":null},"i
                                                  //| ntTree":[1,[[21,[[31,[]]]],[22,[]],[23,[[31,[]],[32,[[41,[]]]],[33,[]]]]]],
                                                  //| "addressTree":[{"riftdesc":{"identifier":"riftwarp.TestAddress","version":n
                                                  //| ull},"city":"Hamburg","street":"Am Hafen"},[[{"riftdesc":{"identifier":"rif
                                                  //| twarp.TestAddress","version":null},"city":"New York","street":"Broadway"},[
                                                  //| ]],[{"riftdesc":{"identifier":"riftwarp.TestAddress","version":null},"city"
                                                  //| :"Los Angeles ","street":"Sunset Boulevard"},[]]]]}}))

  println(blobdata)                               //> Map(b7896d8f-6128-4e71-8c30-40c01a26fd73 -> [B@36325538)

  val backFromWarpV = riftWarp.receiveFromWarpWithBlobs[DimensionString, TestObjectA](blobFetch)(RiftJson())(warpStreamV.forceResult)
                                                  //> backFromWarpV  : almhirt.common.AlmValidation[riftwarp.TestObjectA] = Succe
                                                  //| ss(TestObjectA([B@2c661664,[B@36325538,PrimitiveTypes(I am Pete,true,127,-2
                                                  //| 37823,-278234263,2658762576823765873658638765287568756827652525205773050072
                                                  //| 09857025728132213242,1.3675,1.3672322350005,23761247614876823746.2384674918
                                                  //| 2408,2013-03-13T10:32:04.455+01:00,29cbe9b7-62c3-4ef9-897c-9d0703802d27),Pr
                                                  //| imitiveListMAs(List(alpha, beta, gamma, delta),List(1, 2, 3, 4, 5, 6, 7, 8,
                                                  //|  9, 10),List(1.0, 0.5, 0.2, 0.125),List(1.333333, 1.33333335, 1.6666666, 1.
                                                  //| 6666667),List(2013-03-13T11:32:04.517+01:00, 2013-03-13T12:32:04.517+01:00,
                                                  //|  2013-03-13T13:32:04.517+01:00, 2013-03-13T14:32:04.517+01:00)),PrimitiveVe
                                                  //| ctorMAs(Vector(alpha, beta, gamma, delta),Vector(1, 2, 3, 4, 5, 6, 7, 8, 9,
                                                  //|  10),Vector(1.0, 0.5, 0.2, 0.125),Vector(1.333333, 1.33333335, 1.6666666, 1
                                                  //| .6666667),Vector(2013-03-13T11:32:04.517+01:00, 2013-03-13T12:32:04.517+01:
                                                  //| 00, 2013-03-13T13:32:04.517+01:00, 2013-03-13T14:32:04.517+01:00)),Some(Pri
                                                  //| mitiveSetMAs(Set(alpha, beta, gamma, delta),Set(5, 10, 1, 6, 9, 2, 7, 3, 8,
                                                  //|  4),Set(1.0, 0.5, 0.2, 0.125),Set(1.333333, 1.33333335, 1.6666666, 1.666666
                                                  //| 7),None)),PrimitiveIterableMAs(Set(alpha, beta, gamma, delta),Set(5, 10, 1,
                                                  //|  6, 9, 2, 7, 3, 8, 4),Set(1.0, 0.5, 0.2, 0.125),Set(1.333333, 1.33333335, 1
                                                  //| .6666666, 1.6666667),Set(2013-03-13T11:32:04.517+01:00, 2013-03-13T12:32:04
                                                  //| .517+01:00, 2013-03-13T13:32:04.517+01:00, 2013-03-13T14:32:04.517+01:00)),
                                                  //| ComplexMAs(List(TestAddress(Hamburg,Am Hafen), TestAddress(New York,Broadwa
                                                  //| y), TestAddress(Los Angeles ,Sunset Boulevard)),Vector(TestAddress(Hamburg,
                                                  //| Am Hafen), TestAddress(New York,Broadway), TestAddress(Los Angeles ,Sunset 
                                                  //| Boulevard)),Set(TestAddress(Hamburg,Am Hafen), TestAddress(New York,Broadwa
                                                  //| y), TestAddress(Los Angeles ,Sunset Boulevard)),List(true, hello, 1.0, 2.0,
                                                  //|  3.0, 3.0, TestAddress(Somewhere,here))),PrimitiveMaps(Map(1 -> 10, 2 -> 20
                                                  //| , 3 -> 30, 4 -> 40),Map(a -> 1, b -> 2, c -> 3),Map(cb240411-53d3-4edf-b063
                                                  //| -5b544c59d1b1 -> 2013-03-13T10:32:04.533+01:00, 94de2249-19f4-44b6-9b9f-748
                                                  //| 82c7b496c -> 2013-03-14T10:32:04.533+01:00, b0f22650-f291-42c9-a70c-e7e69a6
                                                  //| ca758 -> 2013-03-15T10:32:04.533+01:00)),ComplexMaps(Map(0 -> TestAddress(H
                                                  //| amburg,Am Hafen), 1 -> TestAddress(New York,Broadway), 2 -> TestAddress(Los
                                                  //|  Angeles ,Sunset Boulevard)),Map(0 -> TestAddress(Hamburg,Am Hafen), 1 -> T
                                                  //| estAddress(New York,Broadway), 2 -> TestAddress(Los Angeles ,Sunset Bouleva
                                                  //| rd)),Map(x -> d1ef752c-a705-4582-b2cf-b14500f79861, unspecifiedProblem -> a
                                                  //| lmhirt.common.UnspecifiedProblem
                                                  //| Test
                                                  //| Category: SystemProblem
                                                  //| Severity: Major
                                                  //| Arguments: Map(arg1 -> 95.0)
                                                  //| , y -> 48529d37-c37a-4ebf-8529-96535461cf5f, 1 -> TestAddress(New York,Broa
                                                  //| dway), 0 -> TestAddress(Hamburg,Am Hafen), 2 -> TestAddress(Los Angeles ,Su
                                                  //| nset Boulevard), z -> 2013-03-13T10:32:04.533+01:00)),Some(TestAddress(Berl
                                                  //| in,At the wall 89)),Trees(<tree>,<tree>)))

  val backFromWarp = backFromWarpV.forceResult    //> backFromWarp  : riftwarp.TestObjectA = TestObjectA([B@2c661664,[B@36325538,
                                                  //| PrimitiveTypes(I am Pete,true,127,-237823,-278234263,2658762576823765873658
                                                  //| 63876528756875682765252520577305007209857025728132213242,1.3675,1.367232235
                                                  //| 0005,23761247614876823746.23846749182408,2013-03-13T10:32:04.455+01:00,29cb
                                                  //| e9b7-62c3-4ef9-897c-9d0703802d27),PrimitiveListMAs(List(alpha, beta, gamma,
                                                  //|  delta),List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),List(1.0, 0.5, 0.2, 0.125),List
                                                  //| (1.333333, 1.33333335, 1.6666666, 1.6666667),List(2013-03-13T11:32:04.517+0
                                                  //| 1:00, 2013-03-13T12:32:04.517+01:00, 2013-03-13T13:32:04.517+01:00, 2013-03
                                                  //| -13T14:32:04.517+01:00)),PrimitiveVectorMAs(Vector(alpha, beta, gamma, delt
                                                  //| a),Vector(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),Vector(1.0, 0.5, 0.2, 0.125),Vecto
                                                  //| r(1.333333, 1.33333335, 1.6666666, 1.6666667),Vector(2013-03-13T11:32:04.51
                                                  //| 7+01:00, 2013-03-13T12:32:04.517+01:00, 2013-03-13T13:32:04.517+01:00, 2013
                                                  //| -03-13T14:32:04.517+01:00)),Some(PrimitiveSetMAs(Set(alpha, beta, gamma, de
                                                  //| lta),Set(5, 10, 1, 6, 9, 2, 7, 3, 8, 4),Set(1.0, 0.5, 0.2, 0.125),Set(1.333
                                                  //| 333, 1.33333335, 1.6666666, 1.6666667),None)),PrimitiveIterableMAs(Set(alph
                                                  //| a, beta, gamma, delta),Set(5, 10, 1, 6, 9, 2, 7, 3, 8, 4),Set(1.0, 0.5, 0.2
                                                  //| , 0.125),Set(1.333333, 1.33333335, 1.6666666, 1.6666667),Set(2013-03-13T11:
                                                  //| 32:04.517+01:00, 2013-03-13T12:32:04.517+01:00, 2013-03-13T13:32:04.517+01:
                                                  //| 00, 2013-03-13T14:32:04.517+01:00)),ComplexMAs(List(TestAddress(Hamburg,Am 
                                                  //| Hafen), TestAddress(New York,Broadway), TestAddress(Los Angeles ,Sunset Bou
                                                  //| levard)),Vector(TestAddress(Hamburg,Am Hafen), TestAddress(New York,Broadwa
                                                  //| y), TestAddress(Los Angeles ,Sunset Boulevard)),Set(TestAddress(Hamburg,Am 
                                                  //| Hafen), TestAddress(New York,Broadway), TestAddress(Los Angeles ,Sunset Bou
                                                  //| levard)),List(true, hello, 1.0, 2.0, 3.0, 3.0, TestAddress(Somewhere,here))
                                                  //| ),PrimitiveMaps(Map(1 -> 10, 2 -> 20, 3 -> 30, 4 -> 40),Map(a -> 1, b -> 2,
                                                  //|  c -> 3),Map(cb240411-53d3-4edf-b063-5b544c59d1b1 -> 2013-03-13T10:32:04.53
                                                  //| 3+01:00, 94de2249-19f4-44b6-9b9f-74882c7b496c -> 2013-03-14T10:32:04.533+01
                                                  //| :00, b0f22650-f291-42c9-a70c-e7e69a6ca758 -> 2013-03-15T10:32:04.533+01:00)
                                                  //| ),ComplexMaps(Map(0 -> TestAddress(Hamburg,Am Hafen), 1 -> TestAddress(New 
                                                  //| York,Broadway), 2 -> TestAddress(Los Angeles ,Sunset Boulevard)),Map(0 -> T
                                                  //| estAddress(Hamburg,Am Hafen), 1 -> TestAddress(New York,Broadway), 2 -> Tes
                                                  //| tAddress(Los Angeles ,Sunset Boulevard)),Map(x -> d1ef752c-a705-4582-b2cf-b
                                                  //| 14500f79861, unspecifiedProblem -> almhirt.common.UnspecifiedProblem
                                                  //| Test
                                                  //| Category: SystemProblem
                                                  //| Severity: Major
                                                  //| Arguments: Map(arg1 -> 95.0)
                                                  //| , y -> 48529d37-c37a-4ebf-8529-96535461cf5f, 1 -> TestAddress(New York,Broa
                                                  //| dway), 0 -> TestAddress(Hamburg,Am Hafen), 2 -> TestAddress(Los Angeles ,Su
                                                  //| nset Boulevard), z -> 2013-03-13T10:32:04.533+01:00)),Some(TestAddress(Berl
                                                  //| in,At the wall 89)),Trees(<tree>,<tree>))

  testObject == backFromWarp                      //> res0: Boolean = false

  testObject.primitiveTypes == backFromWarp.primitiveTypes
                                                  //> res1: Boolean = true
  
  
  
  val warpStreamXmlV = riftWarp.prepareForWarpWithBlobs[DimensionString](blobDivert)(RiftXml())(testObject)
                                                  //> warpStreamXmlV  : almhirt.common.AlmValidation[riftwarp.DimensionString] = 
                                                  //| Success(DimensionString(<TestObjectA type="riftwarp.TestObjectA" style="noi
                                                  //| sy"><arrayByte><bytes>126,-123,12,-45,-128</bytes></arrayByte><blob><RiftBl
                                                  //| obRefByName type="RiftBlobRefByName" style="noisy"><name><v type="String">d
                                                  //| 4f67527-65e8-4368-aa31-69336549ef13</v></name></RiftBlobRefByName></blob><p
                                                  //| rimitiveTypes><PrimitiveTypes type="riftwarp.PrimitiveTypes" style="noisy">
                                                  //| <str><v type="String">I am Pete</v></str><bool><v type="Boolean">true</v></
                                                  //| bool><byte><v type="Byte">127</v></byte><int><v type="Int">-237823</v></int
                                                  //| ><long><v type="Long">-278234263</v></long><bigInt><v type="BigInt">2658762
                                                  //| 57682376587365863876528756875682765252520577305007209857025728132213242</v>
                                                  //| </bigInt><float><v type="Float">1.3675</v></float><double><v type="Double">
                                                  //| 1.3672322350005</v></double><bigDec><v type="BigDecimal">237612476148768237
                                                  //| 46.23846749182408</v></bigDec><dateTime><v type="DateTime">2013-03-13T10:32
                                                  //| :04.455+01:00</v></dateTime><uuid><v type="Uuid">29cbe9b7-62c3-4ef9-897c-9d
                                                  //| 0703802d27</v></uuid></PrimitiveTypes></primitiveTypes><primitiveListMAs><P
                                                  //| rimitiveListMAs type="riftwarp.PrimitiveListMAs" style="noisy"><listString>
                                                  //| <items><v type="String">alpha</v><v type="String">beta</v><v type="String">
                                                  //| gamma</v><v type="String">delta</v></items></listString><listInt><items><v 
                                                  //| type="Int">1</v><v type="Int">2</v><v type="Int">3</v><v type="Int">4</v><v
                                                  //|  type="Int">5</v><v type="Int">6</v><v type="Int">7</v><v type="Int">8</v><
                                                  //| v type="Int">9</v><v type="Int">10</v></items></listInt><listDouble><items>
                                                  //| <v type="Double">1.0</v><v type="Double">0.5</v><v type="Double">0.2</v><v 
                                                  //| type="Double">0.125</v></items></listDouble><listBigDecimal><items><v type=
                                                  //| "BigDecimal">1.333333</v><v type="BigDecimal">1.33333335</v><v type="BigDec
                                                  //| imal">1.6666666</v><v type="BigDecimal">1.6666667</v></items></listBigDecim
                                                  //| al><listDateTime><items><v type="DateTime">2013-03-13T11:32:04.517+01:00</v
                                                  //| ><v type="DateTime">2013-03-13T12:32:04.517+01:00</v><v type="DateTime">201
                                                  //| 3-03-13T13:32:04.517+01:00</v><v type="DateTime">2013-03-13T14:32:04.517+01
                                                  //| :00</v></items></listDateTime></PrimitiveListMAs></primitiveListMAs><primit
                                                  //| iveVectorMAs><PrimitiveVectorMAs type="riftwarp.PrimitiveVectorMAs" style="
                                                  //| noisy"><vectorString><items><v type="String">alpha</v><v type="String">beta
                                                  //| </v><v type="String">gamma</v><v type="String">delta</v></items></vectorStr
                                                  //| ing><vectorInt><items><v type="Int">1</v><v type="Int">2</v><v type="Int">3
                                                  //| </v><v type="Int">4</v><v type="Int">5</v><v type="Int">6</v><v type="Int">
                                                  //| 7</v><v type="Int">8</v><v type="Int">9</v><v type="Int">10</v></items></ve
                                                  //| ctorInt><vectorDouble><items><v type="Double">1.0</v><v type="Double">0.5</
                                                  //| v><v type="Double">0.2</v><v type="Double">0.125</v></items></vectorDouble>
                                                  //| <vectorBigDecimal><items><v type="BigDecimal">1.333333</v><v type="BigDecim
                                                  //| al">1.33333335</v><v type="BigDecimal">1.6666666</v><v type="BigDecimal">1.
                                                  //| 6666667</v></items></vectorBigDecimal><vectorDateTime><items><v type="DateT
                                                  //| ime">2013-03-13T11:32:04.517+01:00</v><v type="DateTime">2013-03-13T12:32:0
                                                  //| 4.517+01:00</v><v type="DateTime">2013-03-13T13:32:04.517+01:00</v><v type=
                                                  //| "DateTime">2013-03-13T14:32:04.517+01:00</v></items></vectorDateTime></Prim
                                                  //| itiveVectorMAs></primitiveVectorMAs><primitiveSetMAs><PrimitiveSetMAs type=
                                                  //| "riftwarp.PrimitiveSetMAs" style="noisy"><setString><items><v type="String"
                                                  //| >alpha</v><v type="String">beta</v><v type="String">gamma</v><v type="Strin
                                                  //| g">delta</v></items></setString><setInt><items><v type="Int">5</v><v type="
                                                  //| Int">10</v><v type="Int">1</v><v type="Int">6</v><v type="Int">9</v><v type
                                                  //| ="Int">2</v><v type="Int">7</v><v type="Int">3</v><v type="Int">8</v><v typ
                                                  //| e="Int">4</v></items></setInt><setDouble><items><v type="Double">1.0</v><v 
                                                  //| type="Double">0.5</v><v type="Double">0.2</v><v type="Double">0.125</v></it
                                                  //| ems></setDouble><setBigDecimal><items><v type="BigDecimal">1.333333</v><v t
                                                  //| ype="BigDecimal">1.33333335</v><v type="BigDecimal">1.6666666</v><v type="B
                                                  //| igDecimal">1.6666667</v></items></setBigDecimal></PrimitiveSetMAs></primiti
                                                  //| veSetMAs><primitiveIterableMAs><PrimitiveIterableMAs type="riftwarp.Primiti
                                                  //| veIterableMAs" style="noisy"><iterableString><items><v type="String">alpha<
                                                  //| /v><v type="String">beta</v><v type="String">gamma</v><v type="String">delt
                                                  //| a</v></items></iterableString><iterableInt><items><v type="Int">1</v><v typ
                                                  //| e="Int">2</v><v type="Int">3</v><v type="Int">4</v><v type="Int">5</v><v ty
                                                  //| pe="Int">6</v><v type="Int">7</v><v type="Int">8</v><v type="Int">9</v><v t
                                                  //| ype="Int">10</v></items></iterableInt><iterableDouble><items><v type="Doubl
                                                  //| e">1.0</v><v type="Double">0.5</v><v type="Double">0.2</v><v type="Double">
                                                  //| 0.125</v></items></iterableDouble><iterableBigDecimal><items><v type="BigDe
                                                  //| cimal">1.333333</v><v type="BigDecimal">1.33333335</v><v type="BigDecimal">
                                                  //| 1.6666666</v><v type="BigDecimal">1.6666667</v></items></iterableBigDecimal
                                                  //| ><iterableDateTime><items><v type="DateTime">2013-03-13T11:32:04.517+01:00<
                                                  //| /v><v type="DateTime">2013-03-13T12:32:04.517+01:00</v><v type="DateTime">2
                                                  //| 013-03-13T13:32:04.517+01:00</v><v type="DateTime">2013-03-13T14:32:04.517+
                                                  //| 01:00</v></items></iterableDateTime></PrimitiveIterableMAs></primitiveItera
                                                  //| bleMAs><complexMAs><ComplexMAs type="riftwarp.ComplexMAs" style="noisy"><ad
                                                  //| dresses1><items><TestAddress type="riftwarp.TestAddress" style="noisy"><cit
                                                  //| y><v type="String">Hamburg</v></city><street><v type="String">Am Hafen</v><
                                                  //| /street></TestAddress><TestAddress type="riftwarp.TestAddress" style="noisy
                                                  //| "><city><v type="String">New York</v></city><street><v type="String">Broadw
                                                  //| ay</v></street></TestAddress><TestAddress type="riftwarp.TestAddress" style
                                                  //| ="noisy"><city><v type="String">Los Angeles </v></city><street><v type="Str
                                                  //| ing">Sunset Boulevard</v></street></TestAddress></items></addresses1><addre
                                                  //| sses2><items><TestAddress type="riftwarp.TestAddress" style="noisy"><city><
                                                  //| v type="String">Hamburg</v></city><street><v type="String">Am Hafen</v></st
                                                  //| reet></TestAddress><TestAddress type="riftwarp.TestAddress" style="noisy"><
                                                  //| city><v type="String">New York</v></city><street><v type="String">Broadway<
                                                  //| /v></street></TestAddress><TestAddress type="riftwarp.TestAddress" style="n
                                                  //| oisy"><city><v type="String">Los Angeles </v></city><street><v type="String
                                                  //| ">Sunset Boulevard</v></street></TestAddress></items></addresses2><addresse
                                                  //| s3><items><TestAddress type="riftwarp.TestAddress" style="noisy"><city><v t
                                                  //| ype="String">Hamburg</v></city><street><v type="String">Am Hafen</v></stree
                                                  //| t></TestAddress><TestAddress type="riftwarp.TestAddress" style="noisy"><cit
                                                  //| y><v type="String">New York</v></city><street><v type="String">Broadway</v>
                                                  //| </street></TestAddress><TestAddress type="riftwarp.TestAddress" style="nois
                                                  //| y"><city><v type="String">Los Angeles </v></city><street><v type="String">S
                                                  //| unset Boulevard</v></street></TestAddress></items></addresses3><anything><i
                                                  //| tems><v type="Boolean">true</v><v type="String">hello</v><v type="Int">1</v
                                                  //| ><v type="Long">2</v><v type="Double">3.0</v><v type="Float">3.0</v><TestAd
                                                  //| dress type="riftwarp.TestAddress" style="noisy"><city><v type="String">Some
                                                  //| where</v></city><street><v type="String">here</v></street></TestAddress></i
                                                  //| tems></anything></ComplexMAs></complexMAs><primitiveMaps><PrimitiveMaps typ
                                                  //| e="riftwarp.PrimitiveMaps" style="noisy"><mapIntInt><items><items><v type="
                                                  //| Int">1</v><v type="Int">10</v></items><items><v type="Int">2</v><v type="In
                                                  //| t">20</v></items><items><v type="Int">3</v><v type="Int">30</v></items><ite
                                                  //| ms><v type="Int">4</v><v type="Int">40</v></items></items></mapIntInt><mapS
                                                  //| tringInt><items><items><v type="String">a</v><v type="Int">1</v></items><it
                                                  //| ems><v type="String">b</v><v type="Int">2</v></items><items><v type="String
                                                  //| ">c</v><v type="Int">3</v></items></items></mapStringInt><mapUuidDateTime><
                                                  //| items><items><v type="Uuid">cb240411-53d3-4edf-b063-5b544c59d1b1</v><v type
                                                  //| ="DateTime">2013-03-13T10:32:04.533+01:00</v></items><items><v type="Uuid">
                                                  //| 94de2249-19f4-44b6-9b9f-74882c7b496c</v><v type="DateTime">2013-03-14T10:32
                                                  //| :04.533+01:00</v></items><items><v type="Uuid">b0f22650-f291-42c9-a70c-e7e6
                                                  //| 9a6ca758</v><v type="DateTime">2013-03-15T10:32:04.533+01:00</v></items></i
                                                  //| tems></mapUuidDateTime></PrimitiveMaps></primitiveMaps><complexMaps><Comple
                                                  //| xMaps type="riftwarp.ComplexMaps" style="noisy"><mapIntTestAddress1><items>
                                                  //| <items><v type="Int">0</v><TestAddress type="riftwarp.TestAddress" style="n
                                                  //| oisy"><city><v type="String">Hamburg</v></city><street><v type="String">Am 
                                                  //| Hafen</v></street></TestAddress></items><items><v type="Int">1</v><TestAddr
                                                  //| ess type="riftwarp.TestAddress" style="noisy"><city><v type="String">New Yo
                                                  //| rk</v></city><street><v type="String">Broadway</v></street></TestAddress></
                                                  //| items><items><v type="Int">2</v><TestAddress type="riftwarp.TestAddress" st
                                                  //| yle="noisy"><city><v type="String">Los Angeles </v></city><street><v type="
                                                  //| String">Sunset Boulevard</v></street></TestAddress></items></items></mapInt
                                                  //| TestAddress1><mapIntAny><items><items><v type="Int">0</v><TestAddress type=
                                                  //| "riftwarp.TestAddress" style="noisy"><city><v type="String">Hamburg</v></ci
                                                  //| ty><street><v type="String">Am Hafen</v></street></TestAddress></items><ite
                                                  //| ms><v type="Int">1</v><TestAddress type="riftwarp.TestAddress" style="noisy
                                                  //| "><city><v type="String">New York</v></city><street><v type="String">Broadw
                                                  //| ay</v></street></TestAddress></items><items><v type="Int">2</v><TestAddress
                                                  //|  type="riftwarp.TestAddress" style="noisy"><city><v type="String">Los Angel
                                                  //| es </v></city><street><v type="String">Sunset Boulevard</v></street></TestA
                                                  //| ddress></items></items></mapIntAny><mapStringAnyWithUnknown><items><items><
                                                  //| v type="String">x</v><v type="Uuid">d1ef752c-a705-4582-b2cf-b14500f79861</v
                                                  //| ></items><items><v type="String">unspecifiedProblem</v><UnspecifiedProblem 
                                                  //| type="almhirt.common.UnspecifiedProblem" style="noisy"><message><v type="St
                                                  //| ring">Test</v></message><severity><v type="String">Major</v></severity><cat
                                                  //| egory><v type="String">SystemProblem</v></category><args><items><items><v t
                                                  //| ype="String">arg1</v><v type="Int">95</v></items></items></args></Unspecifi
                                                  //| edProblem></items><items><v type="String">y</v><v type="Uuid">48529d37-c37a
                                                  //| -4ebf-8529-96535461cf5f</v></items><items><v type="String">1</v><TestAddres
                                                  //| s type="riftwarp.TestAddress" style="noisy"><city><v type="String">New York
                                                  //| </v></city><street><v type="String">Broadway</v></street></TestAddress></it
                                                  //| ems><items><v type="String">0</v><TestAddress type="riftwarp.TestAddress" s
                                                  //| tyle="noisy"><city><v type="String">Hamburg</v></city><street><v type="Stri
                                                  //| ng">Am Hafen</v></street></TestAddress></items><items><v type="String">2</v
                                                  //| ><TestAddress type="riftwarp.TestAddress" style="noisy"><city><v type="Stri
                                                  //| ng">Los Angeles </v></city><street><v type="String">Sunset Boulevard</v></s
                                                  //| treet></TestAddress></items><items><v type="String">z</v><v type="DateTime"
                                                  //| >2013-03-13T10:32:04.533+01:00</v></items></items></mapStringAnyWithUnknown
                                                  //| ></ComplexMaps></complexMaps><addressOpt><TestAddress type="riftwarp.TestAd
                                                  //| dress" style="noisy"><city><v type="String">Berlin</v></city><street><v typ
                                                  //| e="String">At the wall 89</v></street></TestAddress></addressOpt><trees><Tr
                                                  //| ees type="riftwarp.Trees" style="noisy"><intTree><items><v type="Int">1</v>
                                                  //| <items><items><v type="Int">21</v><items><items><v type="Int">31</v><items>
                                                  //| </items></items></items></items><items><v type="Int">22</v><items></items><
                                                  //| /items><items><v type="Int">23</v><items><items><v type="Int">31</v><items>
                                                  //| </items></items><items><v type="Int">32</v><items><items><v type="Int">41</
                                                  //| v><items></items></items></items></items><items><v type="Int">33</v><items>
                                                  //| </items></items></items></items></items></items></intTree><addressTree><ite
                                                  //| ms><TestAddress type="riftwarp.TestAddress" style="noisy"><city><v type="St
                                                  //| ring">Hamburg</v></city><street><v type="String">Am Hafen</v></street></Tes
                                                  //| tAddress><items><items><TestAddress type="riftwarp.TestAddress" style="nois
                                                  //| y"><city><v type="String">New York</v></city><street><v type="String">Broad
                                                  //| way</v></street></TestAddress><items></items></items><items><TestAddress ty
                                                  //| pe="riftwarp.TestAddress" style="noisy"><city><v type="String">Los Angeles 
                                                  //| </v></city><street><v type="String">Sunset Boulevard</v></street></TestAddr
                                                  //| ess><items></items></items></items></items></addressTree></Trees></trees></
                                                  //| TestObjectA>))



 val backFromWarpXmlV = riftWarp.receiveFromWarpWithBlobs[DimensionString, TestObjectA](blobFetch)(RiftXml())(warpStreamXmlV.forceResult)
                                                  //> backFromWarpXmlV  : almhirt.common.AlmValidation[riftwarp.TestObjectA] = Su
                                                  //| ccess(TestObjectA([B@3d0508c5,[B@36325538,PrimitiveTypes(I am Pete,true,127
                                                  //| ,-237823,-278234263,2658762576823765873658638765287568756827652525205773050
                                                  //| 07209857025728132213242,1.3675,1.3672322350005,23761247614876823746.2384674
                                                  //| 9182408,2013-03-13T10:32:04.455+01:00,29cbe9b7-62c3-4ef9-897c-9d0703802d27)
                                                  //| ,PrimitiveListMAs(List(alpha, beta, gamma, delta),List(1, 2, 3, 4, 5, 6, 7,
                                                  //|  8, 9, 10),List(1.0, 0.5, 0.2, 0.125),List(1.333333, 1.33333335, 1.6666666,
                                                  //|  1.6666667),List(2013-03-13T11:32:04.517+01:00, 2013-03-13T12:32:04.517+01:
                                                  //| 00, 2013-03-13T13:32:04.517+01:00, 2013-03-13T14:32:04.517+01:00)),Primitiv
                                                  //| eVectorMAs(Vector(alpha, beta, gamma, delta),Vector(1, 2, 3, 4, 5, 6, 7, 8,
                                                  //|  9, 10),Vector(1.0, 0.5, 0.2, 0.125),Vector(1.333333, 1.33333335, 1.6666666
                                                  //| , 1.6666667),Vector(2013-03-13T11:32:04.517+01:00, 2013-03-13T12:32:04.517+
                                                  //| 01:00, 2013-03-13T13:32:04.517+01:00, 2013-03-13T14:32:04.517+01:00)),Some(
                                                  //| PrimitiveSetMAs(Set(alpha, beta, gamma, delta),Set(5, 10, 1, 6, 9, 2, 7, 3,
                                                  //|  8, 4),Set(1.0, 0.5, 0.2, 0.125),Set(1.333333, 1.33333335, 1.6666666, 1.666
                                                  //| 6667),None)),PrimitiveIterableMAs(Set(alpha, beta, gamma, delta),Set(5, 10,
                                                  //|  1, 6, 9, 2, 7, 3, 8, 4),Set(1.0, 0.5, 0.2, 0.125),Set(1.333333, 1.33333335
                                                  //| , 1.6666666, 1.6666667),Set(2013-03-13T11:32:04.517+01:00, 2013-03-13T12:32
                                                  //| :04.517+01:00, 2013-03-13T13:32:04.517+01:00, 2013-03-13T14:32:04.517+01:00
                                                  //| )),ComplexMAs(List(TestAddress(Hamburg,Am Hafen), TestAddress(New York,Broa
                                                  //| dway), TestAddress(Los Angeles ,Sunset Boulevard)),Vector(TestAddress(Hambu
                                                  //| rg,Am Hafen), TestAddress(New York,Broadway), TestAddress(Los Angeles ,Suns
                                                  //| et Boulevard)),Set(TestAddress(Hamburg,Am Hafen), TestAddress(New York,Broa
                                                  //| dway), TestAddress(Los Angeles ,Sunset Boulevard)),List(true, hello, 1, 2, 
                                                  //| 3.0, 3.0, TestAddress(Somewhere,here))),PrimitiveMaps(Map(1 -> 10, 2 -> 20,
                                                  //|  3 -> 30, 4 -> 40),Map(a -> 1, b -> 2, c -> 3),Map(cb240411-53d3-4edf-b063-
                                                  //| 5b544c59d1b1 -> 2013-03-13T10:32:04.533+01:00, 94de2249-19f4-44b6-9b9f-7488
                                                  //| 2c7b496c -> 2013-03-14T10:32:04.533+01:00, b0f22650-f291-42c9-a70c-e7e69a6c
                                                  //| a758 -> 2013-03-15T10:32:04.533+01:00)),ComplexMaps(Map(0 -> TestAddress(Ha
                                                  //| mburg,Am Hafen), 1 -> TestAddress(New York,Broadway), 2 -> TestAddress(Los 
                                                  //| Angeles ,Sunset Boulevard)),Map(0 -> TestAddress(Hamburg,Am Hafen), 1 -> Te
                                                  //| stAddress(New York,Broadway), 2 -> TestAddress(Los Angeles ,Sunset Boulevar
                                                  //| d)),Map(x -> d1ef752c-a705-4582-b2cf-b14500f79861, unspecifiedProblem -> al
                                                  //| mhirt.common.UnspecifiedProblem
                                                  //| Test
                                                  //| Category: SystemProblem
                                                  //| Severity: Major
                                                  //| Arguments: Map(arg1 -> 95)
                                                  //| , y -> 48529d37-c37a-4ebf-8529-96535461cf5f, 1 -> TestAddress(New York,Broa
                                                  //| dway), 0 -> TestAddress(Hamburg,Am Hafen), 2 -> TestAddress(Los Angeles ,Su
                                                  //| nset Boulevard), z -> 2013-03-13T10:32:04.533+01:00)),Some(TestAddress(Berl
                                                  //| in,At the wall 89)),Trees(<tree>,<tree>)))

  println(blobdata)                               //> Map(b7896d8f-6128-4e71-8c30-40c01a26fd73 -> [B@36325538, d4f67527-65e8-4368
                                                  //| -aa31-69336549ef13 -> [B@36325538)


  riftWarp.prepareForWarp[DimensionCord](RiftJson())(testObject).flatMap(warpStream =>
    riftWarp.receiveFromWarp[DimensionCord, TestObjectA](RiftJson())(warpStream)).map(rearrived =>
    rearrived == testObject)                      //> res2: scalaz.Validation[almhirt.common.Problem,Boolean] = Success(false)

  //  riftWarp.prepareForWarp[DimensionRawMap](RiftMap())(testObject).flatMap(warpStream =>
  //    riftWarp.receiveFromWarp[DimensionRawMap, TestObjectA](RiftMap())(warpStream)).map(rearrived =>
  //    rearrived == testObject)

}