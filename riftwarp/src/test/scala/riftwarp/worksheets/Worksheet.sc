package riftwarp.worksheets

import scalaz.syntax.validation._
import almhirt.syntax.almvalidation._
import riftwarp._
import riftwarp.impl._

object Worksheet {
  val riftWarp = RiftWarp.concurrentWithDefaults  //> riftWarp  : riftwarp.RiftWarp = riftwarp.RiftWarp$$anon$1@406c9125
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


  val testObject = TestObjectA.pete               //> testObject  : riftwarp.TestObjectA = TestObjectA([B@64721088,[B@7c9b843c,Pr
                                                  //| imitiveTypes(I am Pete,true,127,-237823,-278234263,265876257682376587365863
                                                  //| 876528756875682765252520577305007209857025728132213242,1.3675,1.36723223500
                                                  //| 05,23761247614876823746.23846749182408,2013-03-11T19:08:14.310+01:00,4e128f
                                                  //| 4a-1af2-4049-b7d5-87d60031b514),PrimitiveListMAs(List(alpha, beta, gamma, d
                                                  //| elta),List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),List(1.0, 0.5, 0.2, 0.125),List(1
                                                  //| .333333, 1.33333335, 1.6666666, 1.6666667),List(2013-03-11T20:08:14.372+01:
                                                  //| 00, 2013-03-11T21:08:14.372+01:00, 2013-03-11T22:08:14.372+01:00, 2013-03-1
                                                  //| 1T23:08:14.372+01:00)),PrimitiveVectorMAs(Vector(alpha, beta, gamma, delta)
                                                  //| ,Vector(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),Vector(1.0, 0.5, 0.2, 0.125),Vector(
                                                  //| 1.333333, 1.33333335, 1.6666666, 1.6666667),Vector(2013-03-11T20:08:14.372+
                                                  //| 01:00, 2013-03-11T21:08:14.372+01:00, 2013-03-11T22:08:14.372+01:00, 2013-0
                                                  //| 3-11T23:08:14.372+01:00)),Some(PrimitiveSetMAs(Set(alpha, beta, gamma, delt
                                                  //| a),Set(5, 10, 1, 6, 9, 2, 7, 3, 8, 4),Set(1.0, 0.5, 0.2, 0.125),Set(1.33333
                                                  //| 3, 1.33333335, 1.6666666, 1.6666667),None)),PrimitiveIterableMAs(List(alpha
                                                  //| , beta, gamma, delta),List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),List(1.0, 0.5, 0.
                                                  //| 2, 0.125),List(1.333333, 1.33333335, 1.6666666, 1.6666667),List(2013-03-11T
                                                  //| 20:08:14.372+01:00, 2013-03-11T21:08:14.372+01:00, 2013-03-11T22:08:14.372+
                                                  //| 01:00, 2013-03-11T23:08:14.372+01:00)),ComplexMAs(List(TestAddress(Hamburg,
                                                  //| Am Hafen), TestAddress(New York,Broadway), TestAddress(Los Angeles ,Sunset 
                                                  //| Boulevard)),Vector(TestAddress(Hamburg,Am Hafen), TestAddress(New York,Broa
                                                  //| dway), TestAddress(Los Angeles ,Sunset Boulevard)),Set(TestAddress(Hamburg,
                                                  //| Am Hafen), TestAddress(New York,Broadway), TestAddress(Los Angeles ,Sunset 
                                                  //| Boulevard)),List(true, hello, 1, 2, 3.0, 3.0, TestAddress(Somewhere,here)))
                                                  //| ,PrimitiveMaps(Map(1 -> 10, 2 -> 20, 3 -> 30, 4 -> 40),Map(a -> 1, b -> 2, 
                                                  //| c -> 3),Map(c1ee896b-4197-4847-b2a7-cbd065bf3123 -> 2013-03-11T19:08:14.388
                                                  //| +01:00, ace1b713-2561-44a4-94c4-14e09f97187a -> 2013-03-12T19:08:14.388+01:
                                                  //| 00, 2ee7e309-a134-4da4-a2a7-a1d0c204ee53 -> 2013-03-13T19:08:14.388+01:00))
                                                  //| ,ComplexMaps(Map(0 -> TestAddress(Hamburg,Am Hafen), 1 -> TestAddress(New Y
                                                  //| ork,Broadway), 2 -> TestAddress(Los Angeles ,Sunset Boulevard)),Map(0 -> Te
                                                  //| stAddress(Hamburg,Am Hafen), 1 -> TestAddress(New York,Broadway), 2 -> Test
                                                  //| Address(Los Angeles ,Sunset Boulevard)),Map(x -> ffe5d6cc-3029-49a9-bb44-50
                                                  //| 4be8ca148c, unspecifiedProblem -> almhirt.common.UnspecifiedProblem
                                                  //| Test
                                                  //| Category: SystemProblem
                                                  //| Severity: Major
                                                  //| Arguments: Map(arg1 -> 95)
                                                  //| , y -> 05eef8f7-bd9a-4c64-b09a-51726682eb7b, 1 -> TestAddress(New York,Broa
                                                  //| dway), 0 -> TestAddress(Hamburg,Am Hafen), 2 -> TestAddress(Los Angeles ,Su
                                                  //| nset Boulevard), unknownType -> UnknownObject(1), z -> 2013-03-11T19:08:14.
                                                  //| 388+01:00)),Some(TestAddress(Berlin,At the wall 89)),Trees(<tree>,<tree>))
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
                                                  //| er":"RiftBlobRefByName","version":null},"name":"95c2822d-39ea-4c5b-b896-172
                                                  //| bc2368296"},"primitiveTypes":{"riftdesc":{"identifier":"riftwarp.PrimitiveT
                                                  //| ypes","version":null},"str":"I am Pete","bool":true,"byte":127,"int":-23782
                                                  //| 3,"long":-278234263,"bigInt":"265876257682376587365863876528756875682765252
                                                  //| 520577305007209857025728132213242","float":1.3674999475479126,"double":1.36
                                                  //| 72322350005,"bigDec":"23761247614876823746.23846749182408","dateTime":"2013
                                                  //| -03-11T19:08:14.310+01:00","uuid":"4e128f4a-1af2-4049-b7d5-87d60031b514"},"
                                                  //| primitiveListMAs":{"riftdesc":{"identifier":"riftwarp.PrimitiveListMAs","ve
                                                  //| rsion":null},"listString":["alpha","beta","gamma","delta"],"listInt":[1,2,3
                                                  //| ,4,5,6,7,8,9,10],"listDouble":[1.0,0.5,0.2,0.125],"listBigDecimal":["1.3333
                                                  //| 33","1.33333335","1.6666666","1.6666667"],"listDateTime":["2013-03-11T20:08
                                                  //| :14.372+01:00","2013-03-11T21:08:14.372+01:00","2013-03-11T22:08:14.372+01:
                                                  //| 00","2013-03-11T23:08:14.372+01:00"]},"primitiveVectorMAs":{"riftdesc":{"id
                                                  //| entifier":"riftwarp.PrimitiveVectorMAs","version":null},"vectorString":["al
                                                  //| pha","beta","gamma","delta"],"vectorInt":[1,2,3,4,5,6,7,8,9,10],"vectorDoub
                                                  //| le":[1.0,0.5,0.2,0.125],"vectorBigDecimal":["1.333333","1.33333335","1.6666
                                                  //| 666","1.6666667"],"vectorDateTime":["2013-03-11T20:08:14.372+01:00","2013-0
                                                  //| 3-11T21:08:14.372+01:00","2013-03-11T22:08:14.372+01:00","2013-03-11T23:08:
                                                  //| 14.372+01:00"]},"primitiveSetMAs":{"riftdesc":{"identifier":"riftwarp.Primi
                                                  //| tiveSetMAs","version":null},"setString":["alpha","beta","gamma","delta"],"s
                                                  //| etInt":[5,10,1,6,9,2,7,3,8,4],"setDouble":[1.0,0.5,0.2,0.125],"setBigDecima
                                                  //| l":["1.333333","1.33333335","1.6666666","1.6666667"],"setDateTime":null},"p
                                                  //| rimitiveIterableMAs":{"riftdesc":{"identifier":"riftwarp.PrimitiveIterableM
                                                  //| As","version":null},"iterableString":["alpha","beta","gamma","delta"],"iter
                                                  //| ableInt":[1,2,3,4,5,6,7,8,9,10],"iterableDouble":[1.0,0.5,0.2,0.125],"itera
                                                  //| bleBigDecimal":["1.333333","1.33333335","1.6666666","1.6666667"],"iterableD
                                                  //| ateTime":["2013-03-11T20:08:14.372+01:00","2013-03-11T21:08:14.372+01:00","
                                                  //| 2013-03-11T22:08:14.372+01:00","2013-03-11T23:08:14.372+01:00"]},"complexMA
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
                                                  //| StringInt":[["a",1],["b",2],["c",3]],"mapUuidDateTime":[["c1ee896b-4197-484
                                                  //| 7-b2a7-cbd065bf3123","2013-03-11T19:08:14.388+01:00"],["ace1b713-2561-44a4-
                                                  //| 94c4-14e09f97187a","2013-03-12T19:08:14.388+01:00"],["2ee7e309-a134-4da4-a2
                                                  //| a7-a1d0c204ee53","2013-03-13T19:08:14.388+01:00"]]},"complexMaps":{"riftdes
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
                                                  //| oulevard"}]],"mapStringAnyWithUnknown":[["x","ffe5d6cc-3029-49a9-bb44-504be
                                                  //| 8ca148c"],["unspecifiedProblem",{"riftdesc":{"identifier":"almhirt.common.U
                                                  //| nspecifiedProblem","version":null},"message":"Test","severity":"Major","cat
                                                  //| egory":"SystemProblem","args":[["arg1",95]],"cause":null}],["y","05eef8f7-b
                                                  //| d9a-4c64-b09a-51726682eb7b"],["1",{"riftdesc":{"identifier":"riftwarp.TestA
                                                  //| ddress","version":null},"city":"New York","street":"Broadway"}],["0",{"rift
                                                  //| desc":{"identifier":"riftwarp.TestAddress","version":null},"city":"Hamburg"
                                                  //| ,"street":"Am Hafen"}],["2",{"riftdesc":{"identifier":"riftwarp.TestAddress
                                                  //| ","version":null},"city":"Los Angeles ","street":"Sunset Boulevard"}],["z",
                                                  //| "2013-03-11T19:08:14.388+01:00"]]},"addressOpt":{"riftdesc":{"identifier":"
                                                  //| riftwarp.TestAddress","version":null},"city":"Berlin","street":"At the wall
                                                  //|  89"},"trees":{"riftdesc":{"identifier":"riftwarp.Trees","version":null},"i
                                                  //| ntTree":[1,[[21,[[31,[]]]],[22,[]],[23,[[31,[]],[32,[[41,[]]]],[33,[]]]]]],
                                                  //| "addressTree":[{"riftdesc":{"identifier":"riftwarp.TestAddress","version":n
                                                  //| ull},"city":"Hamburg","street":"Am Hafen"},[[{"riftdesc":{"identifier":"rif
                                                  //| twarp.TestAddress","version":null},"city":"New York","street":"Broadway"},[
                                                  //| ]],[{"riftdesc":{"identifier":"riftwarp.TestAddress","version":null},"city"
                                                  //| :"Los Angeles ","street":"Sunset Boulevard"},[]]]]}}))

  println(blobdata)                               //> Map(95c2822d-39ea-4c5b-b896-172bc2368296 -> [B@7c9b843c)

  val backFromWarpV = riftWarp.receiveFromWarpWithBlobs[DimensionString, TestObjectA](blobFetch)(RiftJson())(warpStreamV.forceResult)
                                                  //> backFromWarpV  : almhirt.common.AlmValidation[riftwarp.TestObjectA] = Succe
                                                  //| ss(TestObjectA([B@d27b1b3,[B@7c9b843c,PrimitiveTypes(I am Pete,true,127,-23
                                                  //| 7823,-278234263,26587625768237658736586387652875687568276525252057730500720
                                                  //| 9857025728132213242,1.3675,1.3672322350005,23761247614876823746.23846749182
                                                  //| 408,2013-03-11T19:08:14.310+01:00,4e128f4a-1af2-4049-b7d5-87d60031b514),Pri
                                                  //| mitiveListMAs(List(alpha, beta, gamma, delta),List(1, 2, 3, 4, 5, 6, 7, 8, 
                                                  //| 9, 10),List(1.0, 0.5, 0.2, 0.125),List(1.333333, 1.33333335, 1.6666666, 1.6
                                                  //| 666667),List(2013-03-11T20:08:14.372+01:00, 2013-03-11T21:08:14.372+01:00, 
                                                  //| 2013-03-11T22:08:14.372+01:00, 2013-03-11T23:08:14.372+01:00)),PrimitiveVec
                                                  //| torMAs(Vector(alpha, beta, gamma, delta),Vector(1, 2, 3, 4, 5, 6, 7, 8, 9, 
                                                  //| 10),Vector(1.0, 0.5, 0.2, 0.125),Vector(1.333333, 1.33333335, 1.6666666, 1.
                                                  //| 6666667),Vector(2013-03-11T20:08:14.372+01:00, 2013-03-11T21:08:14.372+01:0
                                                  //| 0, 2013-03-11T22:08:14.372+01:00, 2013-03-11T23:08:14.372+01:00)),Some(Prim
                                                  //| itiveSetMAs(Set(alpha, beta, gamma, delta),Set(5, 10, 1, 6, 9, 2, 7, 3, 8, 
                                                  //| 4),Set(1.0, 0.5, 0.2, 0.125),Set(1.333333, 1.33333335, 1.6666666, 1.6666667
                                                  //| ),None)),PrimitiveIterableMAs(Set(alpha, beta, gamma, delta),Set(5, 10, 1, 
                                                  //| 6, 9, 2, 7, 3, 8, 4),Set(1.0, 0.5, 0.2, 0.125),Set(1.333333, 1.33333335, 1.
                                                  //| 6666666, 1.6666667),Set(2013-03-11T20:08:14.372+01:00, 2013-03-11T21:08:14.
                                                  //| 372+01:00, 2013-03-11T22:08:14.372+01:00, 2013-03-11T23:08:14.372+01:00)),C
                                                  //| omplexMAs(List(TestAddress(Hamburg,Am Hafen), TestAddress(New York,Broadway
                                                  //| ), TestAddress(Los Angeles ,Sunset Boulevard)),Vector(TestAddress(Hamburg,A
                                                  //| m Hafen), TestAddress(New York,Broadway), TestAddress(Los Angeles ,Sunset B
                                                  //| oulevard)),Set(TestAddress(Hamburg,Am Hafen), TestAddress(New York,Broadway
                                                  //| ), TestAddress(Los Angeles ,Sunset Boulevard)),List(true, hello, 1.0, 2.0, 
                                                  //| 3.0, 3.0, TestAddress(Somewhere,here))),PrimitiveMaps(Map(1 -> 10, 2 -> 20,
                                                  //|  3 -> 30, 4 -> 40),Map(a -> 1, b -> 2, c -> 3),Map(c1ee896b-4197-4847-b2a7-
                                                  //| cbd065bf3123 -> 2013-03-11T19:08:14.388+01:00, ace1b713-2561-44a4-94c4-14e0
                                                  //| 9f97187a -> 2013-03-12T19:08:14.388+01:00, 2ee7e309-a134-4da4-a2a7-a1d0c204
                                                  //| ee53 -> 2013-03-13T19:08:14.388+01:00)),ComplexMaps(Map(0 -> TestAddress(Ha
                                                  //| mburg,Am Hafen), 1 -> TestAddress(New York,Broadway), 2 -> TestAddress(Los 
                                                  //| Angeles ,Sunset Boulevard)),Map(0 -> TestAddress(Hamburg,Am Hafen), 1 -> Te
                                                  //| stAddress(New York,Broadway), 2 -> TestAddress(Los Angeles ,Sunset Boulevar
                                                  //| d)),Map(x -> ffe5d6cc-3029-49a9-bb44-504be8ca148c, unspecifiedProblem -> al
                                                  //| mhirt.common.UnspecifiedProblem
                                                  //| Test
                                                  //| Category: SystemProblem
                                                  //| Severity: Major
                                                  //| Arguments: Map(arg1 -> 95.0)
                                                  //| , y -> 05eef8f7-bd9a-4c64-b09a-51726682eb7b, 1 -> TestAddress(New York,Broa
                                                  //| dway), 0 -> TestAddress(Hamburg,Am Hafen), 2 -> TestAddress(Los Angeles ,Su
                                                  //| nset Boulevard), z -> 2013-03-11T19:08:14.388+01:00)),Some(TestAddress(Berl
                                                  //| in,At the wall 89)),Trees(<tree>,<tree>)))

  val backFromWarp = backFromWarpV.forceResult    //> backFromWarp  : riftwarp.TestObjectA = TestObjectA([B@d27b1b3,[B@7c9b843c,P
                                                  //| rimitiveTypes(I am Pete,true,127,-237823,-278234263,26587625768237658736586
                                                  //| 3876528756875682765252520577305007209857025728132213242,1.3675,1.3672322350
                                                  //| 005,23761247614876823746.23846749182408,2013-03-11T19:08:14.310+01:00,4e128
                                                  //| f4a-1af2-4049-b7d5-87d60031b514),PrimitiveListMAs(List(alpha, beta, gamma, 
                                                  //| delta),List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),List(1.0, 0.5, 0.2, 0.125),List(
                                                  //| 1.333333, 1.33333335, 1.6666666, 1.6666667),List(2013-03-11T20:08:14.372+01
                                                  //| :00, 2013-03-11T21:08:14.372+01:00, 2013-03-11T22:08:14.372+01:00, 2013-03-
                                                  //| 11T23:08:14.372+01:00)),PrimitiveVectorMAs(Vector(alpha, beta, gamma, delta
                                                  //| ),Vector(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),Vector(1.0, 0.5, 0.2, 0.125),Vector
                                                  //| (1.333333, 1.33333335, 1.6666666, 1.6666667),Vector(2013-03-11T20:08:14.372
                                                  //| +01:00, 2013-03-11T21:08:14.372+01:00, 2013-03-11T22:08:14.372+01:00, 2013-
                                                  //| 03-11T23:08:14.372+01:00)),Some(PrimitiveSetMAs(Set(alpha, beta, gamma, del
                                                  //| ta),Set(5, 10, 1, 6, 9, 2, 7, 3, 8, 4),Set(1.0, 0.5, 0.2, 0.125),Set(1.3333
                                                  //| 33, 1.33333335, 1.6666666, 1.6666667),None)),PrimitiveIterableMAs(Set(alpha
                                                  //| , beta, gamma, delta),Set(5, 10, 1, 6, 9, 2, 7, 3, 8, 4),Set(1.0, 0.5, 0.2,
                                                  //|  0.125),Set(1.333333, 1.33333335, 1.6666666, 1.6666667),Set(2013-03-11T20:0
                                                  //| 8:14.372+01:00, 2013-03-11T21:08:14.372+01:00, 2013-03-11T22:08:14.372+01:0
                                                  //| 0, 2013-03-11T23:08:14.372+01:00)),ComplexMAs(List(TestAddress(Hamburg,Am H
                                                  //| afen), TestAddress(New York,Broadway), TestAddress(Los Angeles ,Sunset Boul
                                                  //| evard)),Vector(TestAddress(Hamburg,Am Hafen), TestAddress(New York,Broadway
                                                  //| ), TestAddress(Los Angeles ,Sunset Boulevard)),Set(TestAddress(Hamburg,Am H
                                                  //| afen), TestAddress(New York,Broadway), TestAddress(Los Angeles ,Sunset Boul
                                                  //| evard)),List(true, hello, 1.0, 2.0, 3.0, 3.0, TestAddress(Somewhere,here)))
                                                  //| ,PrimitiveMaps(Map(1 -> 10, 2 -> 20, 3 -> 30, 4 -> 40),Map(a -> 1, b -> 2, 
                                                  //| c -> 3),Map(c1ee896b-4197-4847-b2a7-cbd065bf3123 -> 2013-03-11T19:08:14.388
                                                  //| +01:00, ace1b713-2561-44a4-94c4-14e09f97187a -> 2013-03-12T19:08:14.388+01:
                                                  //| 00, 2ee7e309-a134-4da4-a2a7-a1d0c204ee53 -> 2013-03-13T19:08:14.388+01:00))
                                                  //| ,ComplexMaps(Map(0 -> TestAddress(Hamburg,Am Hafen), 1 -> TestAddress(New Y
                                                  //| ork,Broadway), 2 -> TestAddress(Los Angeles ,Sunset Boulevard)),Map(0 -> Te
                                                  //| stAddress(Hamburg,Am Hafen), 1 -> TestAddress(New York,Broadway), 2 -> Test
                                                  //| Address(Los Angeles ,Sunset Boulevard)),Map(x -> ffe5d6cc-3029-49a9-bb44-50
                                                  //| 4be8ca148c, unspecifiedProblem -> almhirt.common.UnspecifiedProblem
                                                  //| Test
                                                  //| Category: SystemProblem
                                                  //| Severity: Major
                                                  //| Arguments: Map(arg1 -> 95.0)
                                                  //| , y -> 05eef8f7-bd9a-4c64-b09a-51726682eb7b, 1 -> TestAddress(New York,Broa
                                                  //| dway), 0 -> TestAddress(Hamburg,Am Hafen), 2 -> TestAddress(Los Angeles ,Su
                                                  //| nset Boulevard), z -> 2013-03-11T19:08:14.388+01:00)),Some(TestAddress(Berl
                                                  //| in,At the wall 89)),Trees(<tree>,<tree>))

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