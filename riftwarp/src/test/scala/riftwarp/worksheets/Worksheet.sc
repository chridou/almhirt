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
                                                  //| imitiveTypes(I am Pete and heres some invalid embedded xml: <h1>I am great!
                                                  //| </h1>. And heres some JSON: {"message: ["I am", "great"]},true,127,-237823,
                                                  //| -278234263,2658762576823765873658638765287568756827652525205773050072098570
                                                  //| 25728132213242,1.3675,1.3672322350005,23761247614876823746.23846749182408,2
                                                  //| 013-03-13T12:05:20.571+01:00,fa3ce73b-5a0f-46af-a3bf-6db860f32ad7),Primitiv
                                                  //| eListMAs(List(alpha, beta, gamma, delta),List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10
                                                  //| ),List(1.0, 0.5, 0.2, 0.125),List(1.333333, 1.33333335, 1.6666666, 1.666666
                                                  //| 7),List(2013-03-13T13:05:20.649+01:00, 2013-03-13T14:05:20.649+01:00, 2013-
                                                  //| 03-13T15:05:20.649+01:00, 2013-03-13T16:05:20.649+01:00)),PrimitiveVectorMA
                                                  //| s(Vector(alpha, beta, gamma, delta),Vector(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),V
                                                  //| ector(1.0, 0.5, 0.2, 0.125),Vector(1.333333, 1.33333335, 1.6666666, 1.66666
                                                  //| 67),Vector(2013-03-13T13:05:20.649+01:00, 2013-03-13T14:05:20.649+01:00, 20
                                                  //| 13-03-13T15:05:20.649+01:00, 2013-03-13T16:05:20.649+01:00)),Some(Primitive
                                                  //| SetMAs(Set(alpha, beta, gamma, delta),Set(5, 10, 1, 6, 9, 2, 7, 3, 8, 4),Se
                                                  //| t(1.0, 0.5, 0.2, 0.125),Set(1.333333, 1.33333335, 1.6666666, 1.6666667),Non
                                                  //| e)),PrimitiveIterableMAs(List(alpha, beta, gamma, delta),List(1, 2, 3, 4, 5
                                                  //| , 6, 7, 8, 9, 10),List(1.0, 0.5, 0.2, 0.125),List(1.333333, 1.33333335, 1.6
                                                  //| 666666, 1.6666667),List(2013-03-13T13:05:20.649+01:00, 2013-03-13T14:05:20.
                                                  //| 649+01:00, 2013-03-13T15:05:20.649+01:00, 2013-03-13T16:05:20.649+01:00)),C
                                                  //| omplexMAs(List(TestAddress(Hamburg,Am Hafen), TestAddress(New York,Broadway
                                                  //| ), TestAddress(Los Angeles ,Sunset Boulevard)),Vector(TestAddress(Hamburg,A
                                                  //| m Hafen), TestAddress(New York,Broadway), TestAddress(Los Angeles ,Sunset B
                                                  //| oulevard)),Set(TestAddress(Hamburg,Am Hafen), TestAddress(New York,Broadway
                                                  //| ), TestAddress(Los Angeles ,Sunset Boulevard)),List(true, hello, 1, 2, 3.0,
                                                  //|  3.0, TestAddress(Somewhere,here))),PrimitiveMaps(Map(1 -> 10, 2 -> 20, 3 -
                                                  //| > 30, 4 -> 40),Map(a -> 1, b -> 2, c -> 3),Map(1be3d9a0-f9bb-48ea-942d-08d2
                                                  //| 6c9fde80 -> 2013-03-13T12:05:20.649+01:00, eef59f32-2a35-4be3-9573-f66e9082
                                                  //| a03c -> 2013-03-14T12:05:20.649+01:00, c555d326-0a8b-461b-b77d-52f8104f779a
                                                  //|  -> 2013-03-15T12:05:20.649+01:00)),ComplexMaps(Map(0 -> TestAddress(Hambur
                                                  //| g,Am Hafen), 1 -> TestAddress(New York,Broadway), 2 -> TestAddress(Los Ange
                                                  //| les ,Sunset Boulevard)),Map(0 -> TestAddress(Hamburg,Am Hafen), 1 -> TestAd
                                                  //| dress(New York,Broadway), 2 -> TestAddress(Los Angeles ,Sunset Boulevard)),
                                                  //| Map(x -> b1a75d0b-9d95-468e-80f5-cb84989ba8ff, unspecifiedProblem -> almhir
                                                  //| t.common.UnspecifiedProblem
                                                  //| Test
                                                  //| Category: SystemProblem
                                                  //| Severity: Major
                                                  //| Arguments: Map(arg1 -> 95)
                                                  //| , y -> 2c990cc2-9eb1-40fe-9d61-8b32df162ea8, 1 -> TestAddress(New York,Broa
                                                  //| dway), 0 -> TestAddress(Hamburg,Am Hafen), 2 -> TestAddress(Los Angeles ,Su
                                                  //| nset Boulevard), unknownType -> UnknownObject(1), z -> 2013-03-13T12:05:20.
                                                  //| 665+01:00)),Some(TestAddress(Berlin,At the wall 89)),Trees(<tree>,<tree>))
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
                                                  //| er":"RiftBlobRefByName","version":null},"name":"1d9ec08b-bfa6-4ccb-ad1e-3fb
                                                  //| 17edeee08"},"primitiveTypes":{"riftdesc":{"identifier":"riftwarp.PrimitiveT
                                                  //| ypes","version":null},"str":"I am Pete and heres some invalid embedded xml:
                                                  //|  <h1>I am great!</h1>. And heres some JSON: {\"message: [\"I am\", \"great\
                                                  //| "]}","bool":true,"byte":127,"int":-237823,"long":-278234263,"bigInt":"26587
                                                  //| 6257682376587365863876528756875682765252520577305007209857025728132213242",
                                                  //| "float":1.3674999475479126,"double":1.3672322350005,"bigDec":"2376124761487
                                                  //| 6823746.23846749182408","dateTime":"2013-03-13T12:05:20.571+01:00","uuid":"
                                                  //| fa3ce73b-5a0f-46af-a3bf-6db860f32ad7"},"primitiveListMAs":{"riftdesc":{"ide
                                                  //| ntifier":"riftwarp.PrimitiveListMAs","version":null},"listString":["alpha",
                                                  //| "beta","gamma","delta"],"listInt":[1,2,3,4,5,6,7,8,9,10],"listDouble":[1.0,
                                                  //| 0.5,0.2,0.125],"listBigDecimal":["1.333333","1.33333335","1.6666666","1.666
                                                  //| 6667"],"listDateTime":["2013-03-13T13:05:20.649+01:00","2013-03-13T14:05:20
                                                  //| .649+01:00","2013-03-13T15:05:20.649+01:00","2013-03-13T16:05:20.649+01:00"
                                                  //| ]},"primitiveVectorMAs":{"riftdesc":{"identifier":"riftwarp.PrimitiveVector
                                                  //| MAs","version":null},"vectorString":["alpha","beta","gamma","delta"],"vecto
                                                  //| rInt":[1,2,3,4,5,6,7,8,9,10],"vectorDouble":[1.0,0.5,0.2,0.125],"vectorBigD
                                                  //| ecimal":["1.333333","1.33333335","1.6666666","1.6666667"],"vectorDateTime":
                                                  //| ["2013-03-13T13:05:20.649+01:00","2013-03-13T14:05:20.649+01:00","2013-03-1
                                                  //| 3T15:05:20.649+01:00","2013-03-13T16:05:20.649+01:00"]},"primitiveSetMAs":{
                                                  //| "riftdesc":{"identifier":"riftwarp.PrimitiveSetMAs","version":null},"setStr
                                                  //| ing":["alpha","beta","gamma","delta"],"setInt":[5,10,1,6,9,2,7,3,8,4],"setD
                                                  //| ouble":[1.0,0.5,0.2,0.125],"setBigDecimal":["1.333333","1.33333335","1.6666
                                                  //| 666","1.6666667"],"setDateTime":null},"primitiveIterableMAs":{"riftdesc":{"
                                                  //| identifier":"riftwarp.PrimitiveIterableMAs","version":null},"iterableString
                                                  //| ":["alpha","beta","gamma","delta"],"iterableInt":[1,2,3,4,5,6,7,8,9,10],"it
                                                  //| erableDouble":[1.0,0.5,0.2,0.125],"iterableBigDecimal":["1.333333","1.33333
                                                  //| 335","1.6666666","1.6666667"],"iterableDateTime":["2013-03-13T13:05:20.649+
                                                  //| 01:00","2013-03-13T14:05:20.649+01:00","2013-03-13T15:05:20.649+01:00","201
                                                  //| 3-03-13T16:05:20.649+01:00"]},"complexMAs":{"riftdesc":{"identifier":"riftw
                                                  //| arp.ComplexMAs","version":null},"addresses1":[{"riftdesc":{"identifier":"ri
                                                  //| ftwarp.TestAddress","version":null},"city":"Hamburg","street":"Am Hafen"},{
                                                  //| "riftdesc":{"identifier":"riftwarp.TestAddress","version":null},"city":"New
                                                  //|  York","street":"Broadway"},{"riftdesc":{"identifier":"riftwarp.TestAddress
                                                  //| ","version":null},"city":"Los Angeles ","street":"Sunset Boulevard"}],"addr
                                                  //| esses2":[{"riftdesc":{"identifier":"riftwarp.TestAddress","version":null},"
                                                  //| city":"Hamburg","street":"Am Hafen"},{"riftdesc":{"identifier":"riftwarp.Te
                                                  //| stAddress","version":null},"city":"New York","street":"Broadway"},{"riftdes
                                                  //| c":{"identifier":"riftwarp.TestAddress","version":null},"city":"Los Angeles
                                                  //|  ","street":"Sunset Boulevard"}],"addresses3":[{"riftdesc":{"identifier":"r
                                                  //| iftwarp.TestAddress","version":null},"city":"Hamburg","street":"Am Hafen"},
                                                  //| {"riftdesc":{"identifier":"riftwarp.TestAddress","version":null},"city":"Ne
                                                  //| w York","street":"Broadway"},{"riftdesc":{"identifier":"riftwarp.TestAddres
                                                  //| s","version":null},"city":"Los Angeles ","street":"Sunset Boulevard"}],"any
                                                  //| thing":[true,"hello",1,2,3.0,3.0,{"riftdesc":{"identifier":"riftwarp.TestAd
                                                  //| dress","version":null},"city":"Somewhere","street":"here"}]},"primitiveMaps
                                                  //| ":{"riftdesc":{"identifier":"riftwarp.PrimitiveMaps","version":null},"mapIn
                                                  //| tInt":[[1,10],[2,20],[3,30],[4,40]],"mapStringInt":[["a",1],["b",2],["c",3]
                                                  //| ],"mapUuidDateTime":[["1be3d9a0-f9bb-48ea-942d-08d26c9fde80","2013-03-13T12
                                                  //| :05:20.649+01:00"],["eef59f32-2a35-4be3-9573-f66e9082a03c","2013-03-14T12:0
                                                  //| 5:20.649+01:00"],["c555d326-0a8b-461b-b77d-52f8104f779a","2013-03-15T12:05:
                                                  //| 20.649+01:00"]]},"complexMaps":{"riftdesc":{"identifier":"riftwarp.ComplexM
                                                  //| aps","version":null},"mapIntTestAddress1":[[0,{"riftdesc":{"identifier":"ri
                                                  //| ftwarp.TestAddress","version":null},"city":"Hamburg","street":"Am Hafen"}],
                                                  //| [1,{"riftdesc":{"identifier":"riftwarp.TestAddress","version":null},"city":
                                                  //| "New York","street":"Broadway"}],[2,{"riftdesc":{"identifier":"riftwarp.Tes
                                                  //| tAddress","version":null},"city":"Los Angeles ","street":"Sunset Boulevard"
                                                  //| }]],"mapIntAny":[[0,{"riftdesc":{"identifier":"riftwarp.TestAddress","versi
                                                  //| on":null},"city":"Hamburg","street":"Am Hafen"}],[1,{"riftdesc":{"identifie
                                                  //| r":"riftwarp.TestAddress","version":null},"city":"New York","street":"Broad
                                                  //| way"}],[2,{"riftdesc":{"identifier":"riftwarp.TestAddress","version":null},
                                                  //| "city":"Los Angeles ","street":"Sunset Boulevard"}]],"mapStringAnyWithUnkno
                                                  //| wn":[["x","b1a75d0b-9d95-468e-80f5-cb84989ba8ff"],["unspecifiedProblem",{"r
                                                  //| iftdesc":{"identifier":"almhirt.common.UnspecifiedProblem","version":null},
                                                  //| "message":"Test","severity":"Major","category":"SystemProblem","args":[["ar
                                                  //| g1",95]],"cause":null}],["y","2c990cc2-9eb1-40fe-9d61-8b32df162ea8"],["1",{
                                                  //| "riftdesc":{"identifier":"riftwarp.TestAddress","version":null},"city":"New
                                                  //|  York","street":"Broadway"}],["0",{"riftdesc":{"identifier":"riftwarp.TestA
                                                  //| ddress","version":null},"city":"Hamburg","street":"Am Hafen"}],["2",{"riftd
                                                  //| esc":{"identifier":"riftwarp.TestAddress","version":null},"city":"Los Angel
                                                  //| es ","street":"Sunset Boulevard"}],["z","2013-03-13T12:05:20.665+01:00"]]},
                                                  //| "addressOpt":{"riftdesc":{"identifier":"riftwarp.TestAddress","version":nul
                                                  //| l},"city":"Berlin","street":"At the wall 89"},"trees":{"riftdesc":{"identif
                                                  //| ier":"riftwarp.Trees","version":null},"intTree":[1,[[21,[[31,[]]]],[22,[]],
                                                  //| [23,[[31,[]],[32,[[41,[]]]],[33,[]]]]]],"addressTree":[{"riftdesc":{"identi
                                                  //| fier":"riftwarp.TestAddress","version":null},"city":"Hamburg","street":"Am 
                                                  //| Hafen"},[[{"riftdesc":{"identifier":"riftwarp.TestAddress","version":null},
                                                  //| "city":"New York","street":"Broadway"},[]],[{"riftdesc":{"identifier":"rift
                                                  //| warp.TestAddress","version":null},"city":"Los Angeles ","street":"Sunset Bo
                                                  //| ulevard"},[]]]]}}))

  println(blobdata)                               //> Map(1d9ec08b-bfa6-4ccb-ad1e-3fb17edeee08 -> [B@36325538)

  val backFromWarpV = riftWarp.receiveFromWarpWithBlobs[DimensionString, TestObjectA](blobFetch)(RiftJson())(warpStreamV.forceResult)
                                                  //> backFromWarpV  : almhirt.common.AlmValidation[riftwarp.TestObjectA] = Succe
                                                  //| ss(TestObjectA([B@76f294c5,[B@36325538,PrimitiveTypes(I am Pete and heres s
                                                  //| ome invalid embedded xml: <h1>I am great!</h1>. And heres some JSON: {"mess
                                                  //| age: ["I am", "great"]},true,127,-237823,-278234263,26587625768237658736586
                                                  //| 3876528756875682765252520577305007209857025728132213242,1.3675,1.3672322350
                                                  //| 005,23761247614876823746.23846749182408,2013-03-13T12:05:20.571+01:00,fa3ce
                                                  //| 73b-5a0f-46af-a3bf-6db860f32ad7),PrimitiveListMAs(List(alpha, beta, gamma, 
                                                  //| delta),List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),List(1.0, 0.5, 0.2, 0.125),List(
                                                  //| 1.333333, 1.33333335, 1.6666666, 1.6666667),List(2013-03-13T13:05:20.649+01
                                                  //| :00, 2013-03-13T14:05:20.649+01:00, 2013-03-13T15:05:20.649+01:00, 2013-03-
                                                  //| 13T16:05:20.649+01:00)),PrimitiveVectorMAs(Vector(alpha, beta, gamma, delta
                                                  //| ),Vector(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),Vector(1.0, 0.5, 0.2, 0.125),Vector
                                                  //| (1.333333, 1.33333335, 1.6666666, 1.6666667),Vector(2013-03-13T13:05:20.649
                                                  //| +01:00, 2013-03-13T14:05:20.649+01:00, 2013-03-13T15:05:20.649+01:00, 2013-
                                                  //| 03-13T16:05:20.649+01:00)),Some(PrimitiveSetMAs(Set(alpha, beta, gamma, del
                                                  //| ta),Set(5, 10, 1, 6, 9, 2, 7, 3, 8, 4),Set(1.0, 0.5, 0.2, 0.125),Set(1.3333
                                                  //| 33, 1.33333335, 1.6666666, 1.6666667),None)),PrimitiveIterableMAs(Set(alpha
                                                  //| , beta, gamma, delta),Set(5, 10, 1, 6, 9, 2, 7, 3, 8, 4),Set(1.0, 0.5, 0.2,
                                                  //|  0.125),Set(1.333333, 1.33333335, 1.6666666, 1.6666667),Set(2013-03-13T13:0
                                                  //| 5:20.649+01:00, 2013-03-13T14:05:20.649+01:00, 2013-03-13T15:05:20.649+01:0
                                                  //| 0, 2013-03-13T16:05:20.649+01:00)),ComplexMAs(List(TestAddress(Hamburg,Am H
                                                  //| afen), TestAddress(New York,Broadway), TestAddress(Los Angeles ,Sunset Boul
                                                  //| evard)),Vector(TestAddress(Hamburg,Am Hafen), TestAddress(New York,Broadway
                                                  //| ), TestAddress(Los Angeles ,Sunset Boulevard)),Set(TestAddress(Hamburg,Am H
                                                  //| afen), TestAddress(New York,Broadway), TestAddress(Los Angeles ,Sunset Boul
                                                  //| evard)),List(true, hello, 1.0, 2.0, 3.0, 3.0, TestAddress(Somewhere,here)))
                                                  //| ,PrimitiveMaps(Map(1 -> 10, 2 -> 20, 3 -> 30, 4 -> 40),Map(a -> 1, b -> 2, 
                                                  //| c -> 3),Map(1be3d9a0-f9bb-48ea-942d-08d26c9fde80 -> 2013-03-13T12:05:20.649
                                                  //| +01:00, eef59f32-2a35-4be3-9573-f66e9082a03c -> 2013-03-14T12:05:20.649+01:
                                                  //| 00, c555d326-0a8b-461b-b77d-52f8104f779a -> 2013-03-15T12:05:20.649+01:00))
                                                  //| ,ComplexMaps(Map(0 -> TestAddress(Hamburg,Am Hafen), 1 -> TestAddress(New Y
                                                  //| ork,Broadway), 2 -> TestAddress(Los Angeles ,Sunset Boulevard)),Map(0 -> Te
                                                  //| stAddress(Hamburg,Am Hafen), 1 -> TestAddress(New York,Broadway), 2 -> Test
                                                  //| Address(Los Angeles ,Sunset Boulevard)),Map(x -> b1a75d0b-9d95-468e-80f5-cb
                                                  //| 84989ba8ff, unspecifiedProblem -> almhirt.common.UnspecifiedProblem
                                                  //| Test
                                                  //| Category: SystemProblem
                                                  //| Severity: Major
                                                  //| Arguments: Map(arg1 -> 95.0)
                                                  //| , y -> 2c990cc2-9eb1-40fe-9d61-8b32df162ea8, 1 -> TestAddress(New York,Broa
                                                  //| dway), 0 -> TestAddress(Hamburg,Am Hafen), 2 -> TestAddress(Los Angeles ,Su
                                                  //| nset Boulevard), z -> 2013-03-13T12:05:20.665+01:00)),Some(TestAddress(Berl
                                                  //| in,At the wall 89)),Trees(<tree>,<tree>)))

  val backFromWarp = backFromWarpV.forceResult    //> backFromWarp  : riftwarp.TestObjectA = TestObjectA([B@76f294c5,[B@36325538,
                                                  //| PrimitiveTypes(I am Pete and heres some invalid embedded xml: <h1>I am grea
                                                  //| t!</h1>. And heres some JSON: {"message: ["I am", "great"]},true,127,-23782
                                                  //| 3,-278234263,26587625768237658736586387652875687568276525252057730500720985
                                                  //| 7025728132213242,1.3675,1.3672322350005,23761247614876823746.23846749182408
                                                  //| ,2013-03-13T12:05:20.571+01:00,fa3ce73b-5a0f-46af-a3bf-6db860f32ad7),Primit
                                                  //| iveListMAs(List(alpha, beta, gamma, delta),List(1, 2, 3, 4, 5, 6, 7, 8, 9, 
                                                  //| 10),List(1.0, 0.5, 0.2, 0.125),List(1.333333, 1.33333335, 1.6666666, 1.6666
                                                  //| 667),List(2013-03-13T13:05:20.649+01:00, 2013-03-13T14:05:20.649+01:00, 201
                                                  //| 3-03-13T15:05:20.649+01:00, 2013-03-13T16:05:20.649+01:00)),PrimitiveVector
                                                  //| MAs(Vector(alpha, beta, gamma, delta),Vector(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                                                  //| ,Vector(1.0, 0.5, 0.2, 0.125),Vector(1.333333, 1.33333335, 1.6666666, 1.666
                                                  //| 6667),Vector(2013-03-13T13:05:20.649+01:00, 2013-03-13T14:05:20.649+01:00, 
                                                  //| 2013-03-13T15:05:20.649+01:00, 2013-03-13T16:05:20.649+01:00)),Some(Primiti
                                                  //| veSetMAs(Set(alpha, beta, gamma, delta),Set(5, 10, 1, 6, 9, 2, 7, 3, 8, 4),
                                                  //| Set(1.0, 0.5, 0.2, 0.125),Set(1.333333, 1.33333335, 1.6666666, 1.6666667),N
                                                  //| one)),PrimitiveIterableMAs(Set(alpha, beta, gamma, delta),Set(5, 10, 1, 6, 
                                                  //| 9, 2, 7, 3, 8, 4),Set(1.0, 0.5, 0.2, 0.125),Set(1.333333, 1.33333335, 1.666
                                                  //| 6666, 1.6666667),Set(2013-03-13T13:05:20.649+01:00, 2013-03-13T14:05:20.649
                                                  //| +01:00, 2013-03-13T15:05:20.649+01:00, 2013-03-13T16:05:20.649+01:00)),Comp
                                                  //| lexMAs(List(TestAddress(Hamburg,Am Hafen), TestAddress(New York,Broadway), 
                                                  //| TestAddress(Los Angeles ,Sunset Boulevard)),Vector(TestAddress(Hamburg,Am H
                                                  //| afen), TestAddress(New York,Broadway), TestAddress(Los Angeles ,Sunset Boul
                                                  //| evard)),Set(TestAddress(Hamburg,Am Hafen), TestAddress(New York,Broadway), 
                                                  //| TestAddress(Los Angeles ,Sunset Boulevard)),List(true, hello, 1.0, 2.0, 3.0
                                                  //| , 3.0, TestAddress(Somewhere,here))),PrimitiveMaps(Map(1 -> 10, 2 -> 20, 3 
                                                  //| -> 30, 4 -> 40),Map(a -> 1, b -> 2, c -> 3),Map(1be3d9a0-f9bb-48ea-942d-08d
                                                  //| 26c9fde80 -> 2013-03-13T12:05:20.649+01:00, eef59f32-2a35-4be3-9573-f66e908
                                                  //| 2a03c -> 2013-03-14T12:05:20.649+01:00, c555d326-0a8b-461b-b77d-52f8104f779
                                                  //| a -> 2013-03-15T12:05:20.649+01:00)),ComplexMaps(Map(0 -> TestAddress(Hambu
                                                  //| rg,Am Hafen), 1 -> TestAddress(New York,Broadway), 2 -> TestAddress(Los Ang
                                                  //| eles ,Sunset Boulevard)),Map(0 -> TestAddress(Hamburg,Am Hafen), 1 -> TestA
                                                  //| ddress(New York,Broadway), 2 -> TestAddress(Los Angeles ,Sunset Boulevard))
                                                  //| ,Map(x -> b1a75d0b-9d95-468e-80f5-cb84989ba8ff, unspecifiedProblem -> almhi
                                                  //| rt.common.UnspecifiedProblem
                                                  //| Test
                                                  //| Category: SystemProblem
                                                  //| Severity: Major
                                                  //| Arguments: Map(arg1 -> 95.0)
                                                  //| , y -> 2c990cc2-9eb1-40fe-9d61-8b32df162ea8, 1 -> TestAddress(New York,Broa
                                                  //| dway), 0 -> TestAddress(Hamburg,Am Hafen), 2 -> TestAddress(Los Angeles ,Su
                                                  //| nset Boulevard), z -> 2013-03-13T12:05:20.665+01:00)),Some(TestAddress(Berl
                                                  //| in,At the wall 89)),Trees(<tree>,<tree>))

  testObject == backFromWarp                      //> res0: Boolean = false

  testObject.primitiveTypes == backFromWarp.primitiveTypes
                                                  //> res1: Boolean = true
  
  
  
  val warpStreamXmlV = riftWarp.prepareForWarpWithBlobs[DimensionString](blobDivert)(RiftXml())(testObject)
                                                  //> warpStreamXmlV  : almhirt.common.AlmValidation[riftwarp.DimensionString] = 
                                                  //| Success(DimensionString(<TestObjectA type="riftwarp.TestObjectA" style="noi
                                                  //| sy"><arrayByte><bytes>126,-123,12,-45,-128</bytes></arrayByte><blob><RiftBl
                                                  //| obRefByName type="RiftBlobRefByName" style="noisy"><name><v type="String">4
                                                  //| 255f7bc-1b50-408c-8606-7bfab7ba825d</v></name></RiftBlobRefByName></blob><p
                                                  //| rimitiveTypes><PrimitiveTypes type="riftwarp.PrimitiveTypes" style="noisy">
                                                  //| <str><v type="String">I am Pete and heres some invalid embedded xml: &lt;h1
                                                  //| &gt;I am great!&lt;/h1&gt;. And heres some JSON: {&quot;message: [&quot;I a
                                                  //| m&quot;, &quot;great&quot;]}</v></str><bool><v type="Boolean">true</v></boo
                                                  //| l><byte><v type="Byte">127</v></byte><int><v type="Int">-237823</v></int><l
                                                  //| ong><v type="Long">-278234263</v></long><bigInt><v type="BigInt">2658762576
                                                  //| 82376587365863876528756875682765252520577305007209857025728132213242</v></b
                                                  //| igInt><float><v type="Float">1.3675</v></float><double><v type="Double">1.3
                                                  //| 672322350005</v></double><bigDec><v type="BigDecimal">23761247614876823746.
                                                  //| 23846749182408</v></bigDec><dateTime><v type="DateTime">2013-03-13T12:05:20
                                                  //| .571+01:00</v></dateTime><uuid><v type="Uuid">fa3ce73b-5a0f-46af-a3bf-6db86
                                                  //| 0f32ad7</v></uuid></PrimitiveTypes></primitiveTypes><primitiveListMAs><Prim
                                                  //| itiveListMAs type="riftwarp.PrimitiveListMAs" style="noisy"><listString><it
                                                  //| ems><v type="String">alpha</v><v type="String">beta</v><v type="String">gam
                                                  //| ma</v><v type="String">delta</v></items></listString><listInt><items><v typ
                                                  //| e="Int">1</v><v type="Int">2</v><v type="Int">3</v><v type="Int">4</v><v ty
                                                  //| pe="Int">5</v><v type="Int">6</v><v type="Int">7</v><v type="Int">8</v><v t
                                                  //| ype="Int">9</v><v type="Int">10</v></items></listInt><listDouble><items><v 
                                                  //| type="Double">1.0</v><v type="Double">0.5</v><v type="Double">0.2</v><v typ
                                                  //| e="Double">0.125</v></items></listDouble><listBigDecimal><items><v type="Bi
                                                  //| gDecimal">1.333333</v><v type="BigDecimal">1.33333335</v><v type="BigDecima
                                                  //| l">1.6666666</v><v type="BigDecimal">1.6666667</v></items></listBigDecimal>
                                                  //| <listDateTime><items><v type="DateTime">2013-03-13T13:05:20.649+01:00</v><v
                                                  //|  type="DateTime">2013-03-13T14:05:20.649+01:00</v><v type="DateTime">2013-0
                                                  //| 3-13T15:05:20.649+01:00</v><v type="DateTime">2013-03-13T16:05:20.649+01:00
                                                  //| </v></items></listDateTime></PrimitiveListMAs></primitiveListMAs><primitive
                                                  //| VectorMAs><PrimitiveVectorMAs type="riftwarp.PrimitiveVectorMAs" style="noi
                                                  //| sy"><vectorString><items><v type="String">alpha</v><v type="String">beta</v
                                                  //| ><v type="String">gamma</v><v type="String">delta</v></items></vectorString
                                                  //| ><vectorInt><items><v type="Int">1</v><v type="Int">2</v><v type="Int">3</v
                                                  //| ><v type="Int">4</v><v type="Int">5</v><v type="Int">6</v><v type="Int">7</
                                                  //| v><v type="Int">8</v><v type="Int">9</v><v type="Int">10</v></items></vecto
                                                  //| rInt><vectorDouble><items><v type="Double">1.0</v><v type="Double">0.5</v><
                                                  //| v type="Double">0.2</v><v type="Double">0.125</v></items></vectorDouble><ve
                                                  //| ctorBigDecimal><items><v type="BigDecimal">1.333333</v><v type="BigDecimal"
                                                  //| >1.33333335</v><v type="BigDecimal">1.6666666</v><v type="BigDecimal">1.666
                                                  //| 6667</v></items></vectorBigDecimal><vectorDateTime><items><v type="DateTime
                                                  //| ">2013-03-13T13:05:20.649+01:00</v><v type="DateTime">2013-03-13T14:05:20.6
                                                  //| 49+01:00</v><v type="DateTime">2013-03-13T15:05:20.649+01:00</v><v type="Da
                                                  //| teTime">2013-03-13T16:05:20.649+01:00</v></items></vectorDateTime></Primiti
                                                  //| veVectorMAs></primitiveVectorMAs><primitiveSetMAs><PrimitiveSetMAs type="ri
                                                  //| ftwarp.PrimitiveSetMAs" style="noisy"><setString><items><v type="String">al
                                                  //| pha</v><v type="String">beta</v><v type="String">gamma</v><v type="String">
                                                  //| delta</v></items></setString><setInt><items><v type="Int">5</v><v type="Int
                                                  //| ">10</v><v type="Int">1</v><v type="Int">6</v><v type="Int">9</v><v type="I
                                                  //| nt">2</v><v type="Int">7</v><v type="Int">3</v><v type="Int">8</v><v type="
                                                  //| Int">4</v></items></setInt><setDouble><items><v type="Double">1.0</v><v typ
                                                  //| e="Double">0.5</v><v type="Double">0.2</v><v type="Double">0.125</v></items
                                                  //| ></setDouble><setBigDecimal><items><v type="BigDecimal">1.333333</v><v type
                                                  //| ="BigDecimal">1.33333335</v><v type="BigDecimal">1.6666666</v><v type="BigD
                                                  //| ecimal">1.6666667</v></items></setBigDecimal></PrimitiveSetMAs></primitiveS
                                                  //| etMAs><primitiveIterableMAs><PrimitiveIterableMAs type="riftwarp.PrimitiveI
                                                  //| terableMAs" style="noisy"><iterableString><items><v type="String">alpha</v>
                                                  //| <v type="String">beta</v><v type="String">gamma</v><v type="String">delta</
                                                  //| v></items></iterableString><iterableInt><items><v type="Int">1</v><v type="
                                                  //| Int">2</v><v type="Int">3</v><v type="Int">4</v><v type="Int">5</v><v type=
                                                  //| "Int">6</v><v type="Int">7</v><v type="Int">8</v><v type="Int">9</v><v type
                                                  //| ="Int">10</v></items></iterableInt><iterableDouble><items><v type="Double">
                                                  //| 1.0</v><v type="Double">0.5</v><v type="Double">0.2</v><v type="Double">0.1
                                                  //| 25</v></items></iterableDouble><iterableBigDecimal><items><v type="BigDecim
                                                  //| al">1.333333</v><v type="BigDecimal">1.33333335</v><v type="BigDecimal">1.6
                                                  //| 666666</v><v type="BigDecimal">1.6666667</v></items></iterableBigDecimal><i
                                                  //| terableDateTime><items><v type="DateTime">2013-03-13T13:05:20.649+01:00</v>
                                                  //| <v type="DateTime">2013-03-13T14:05:20.649+01:00</v><v type="DateTime">2013
                                                  //| -03-13T15:05:20.649+01:00</v><v type="DateTime">2013-03-13T16:05:20.649+01:
                                                  //| 00</v></items></iterableDateTime></PrimitiveIterableMAs></primitiveIterable
                                                  //| MAs><complexMAs><ComplexMAs type="riftwarp.ComplexMAs" style="noisy"><addre
                                                  //| sses1><items><TestAddress type="riftwarp.TestAddress" style="noisy"><city><
                                                  //| v type="String">Hamburg</v></city><street><v type="String">Am Hafen</v></st
                                                  //| reet></TestAddress><TestAddress type="riftwarp.TestAddress" style="noisy"><
                                                  //| city><v type="String">New York</v></city><street><v type="String">Broadway<
                                                  //| /v></street></TestAddress><TestAddress type="riftwarp.TestAddress" style="n
                                                  //| oisy"><city><v type="String">Los Angeles </v></city><street><v type="String
                                                  //| ">Sunset Boulevard</v></street></TestAddress></items></addresses1><addresse
                                                  //| s2><items><TestAddress type="riftwarp.TestAddress" style="noisy"><city><v t
                                                  //| ype="String">Hamburg</v></city><street><v type="String">Am Hafen</v></stree
                                                  //| t></TestAddress><TestAddress type="riftwarp.TestAddress" style="noisy"><cit
                                                  //| y><v type="String">New York</v></city><street><v type="String">Broadway</v>
                                                  //| </street></TestAddress><TestAddress type="riftwarp.TestAddress" style="nois
                                                  //| y"><city><v type="String">Los Angeles </v></city><street><v type="String">S
                                                  //| unset Boulevard</v></street></TestAddress></items></addresses2><addresses3>
                                                  //| <items><TestAddress type="riftwarp.TestAddress" style="noisy"><city><v type
                                                  //| ="String">Hamburg</v></city><street><v type="String">Am Hafen</v></street><
                                                  //| /TestAddress><TestAddress type="riftwarp.TestAddress" style="noisy"><city><
                                                  //| v type="String">New York</v></city><street><v type="String">Broadway</v></s
                                                  //| treet></TestAddress><TestAddress type="riftwarp.TestAddress" style="noisy">
                                                  //| <city><v type="String">Los Angeles </v></city><street><v type="String">Suns
                                                  //| et Boulevard</v></street></TestAddress></items></addresses3><anything><item
                                                  //| s><v type="Boolean">true</v><v type="String">hello</v><v type="Int">1</v><v
                                                  //|  type="Long">2</v><v type="Double">3.0</v><v type="Float">3.0</v><TestAddre
                                                  //| ss type="riftwarp.TestAddress" style="noisy"><city><v type="String">Somewhe
                                                  //| re</v></city><street><v type="String">here</v></street></TestAddress></item
                                                  //| s></anything></ComplexMAs></complexMAs><primitiveMaps><PrimitiveMaps type="
                                                  //| riftwarp.PrimitiveMaps" style="noisy"><mapIntInt><items><items><v type="Int
                                                  //| ">1</v><v type="Int">10</v></items><items><v type="Int">2</v><v type="Int">
                                                  //| 20</v></items><items><v type="Int">3</v><v type="Int">30</v></items><items>
                                                  //| <v type="Int">4</v><v type="Int">40</v></items></items></mapIntInt><mapStri
                                                  //| ngInt><items><items><v type="String">a</v><v type="Int">1</v></items><items
                                                  //| ><v type="String">b</v><v type="Int">2</v></items><items><v type="String">c
                                                  //| </v><v type="Int">3</v></items></items></mapStringInt><mapUuidDateTime><ite
                                                  //| ms><items><v type="Uuid">1be3d9a0-f9bb-48ea-942d-08d26c9fde80</v><v type="D
                                                  //| ateTime">2013-03-13T12:05:20.649+01:00</v></items><items><v type="Uuid">eef
                                                  //| 59f32-2a35-4be3-9573-f66e9082a03c</v><v type="DateTime">2013-03-14T12:05:20
                                                  //| .649+01:00</v></items><items><v type="Uuid">c555d326-0a8b-461b-b77d-52f8104
                                                  //| f779a</v><v type="DateTime">2013-03-15T12:05:20.649+01:00</v></items></item
                                                  //| s></mapUuidDateTime></PrimitiveMaps></primitiveMaps><complexMaps><ComplexMa
                                                  //| ps type="riftwarp.ComplexMaps" style="noisy"><mapIntTestAddress1><items><it
                                                  //| ems><v type="Int">0</v><TestAddress type="riftwarp.TestAddress" style="nois
                                                  //| y"><city><v type="String">Hamburg</v></city><street><v type="String">Am Haf
                                                  //| en</v></street></TestAddress></items><items><v type="Int">1</v><TestAddress
                                                  //|  type="riftwarp.TestAddress" style="noisy"><city><v type="String">New York<
                                                  //| /v></city><street><v type="String">Broadway</v></street></TestAddress></ite
                                                  //| ms><items><v type="Int">2</v><TestAddress type="riftwarp.TestAddress" style
                                                  //| ="noisy"><city><v type="String">Los Angeles </v></city><street><v type="Str
                                                  //| ing">Sunset Boulevard</v></street></TestAddress></items></items></mapIntTes
                                                  //| tAddress1><mapIntAny><items><items><v type="Int">0</v><TestAddress type="ri
                                                  //| ftwarp.TestAddress" style="noisy"><city><v type="String">Hamburg</v></city>
                                                  //| <street><v type="String">Am Hafen</v></street></TestAddress></items><items>
                                                  //| <v type="Int">1</v><TestAddress type="riftwarp.TestAddress" style="noisy"><
                                                  //| city><v type="String">New York</v></city><street><v type="String">Broadway<
                                                  //| /v></street></TestAddress></items><items><v type="Int">2</v><TestAddress ty
                                                  //| pe="riftwarp.TestAddress" style="noisy"><city><v type="String">Los Angeles 
                                                  //| </v></city><street><v type="String">Sunset Boulevard</v></street></TestAddr
                                                  //| ess></items></items></mapIntAny><mapStringAnyWithUnknown><items><items><v t
                                                  //| ype="String">x</v><v type="Uuid">b1a75d0b-9d95-468e-80f5-cb84989ba8ff</v></
                                                  //| items><items><v type="String">unspecifiedProblem</v><UnspecifiedProblem typ
                                                  //| e="almhirt.common.UnspecifiedProblem" style="noisy"><message><v type="Strin
                                                  //| g">Test</v></message><severity><v type="String">Major</v></severity><catego
                                                  //| ry><v type="String">SystemProblem</v></category><args><items><items><v type
                                                  //| ="String">arg1</v><v type="Int">95</v></items></items></args></UnspecifiedP
                                                  //| roblem></items><items><v type="String">y</v><v type="Uuid">2c990cc2-9eb1-40
                                                  //| fe-9d61-8b32df162ea8</v></items><items><v type="String">1</v><TestAddress t
                                                  //| ype="riftwarp.TestAddress" style="noisy"><city><v type="String">New York</v
                                                  //| ></city><street><v type="String">Broadway</v></street></TestAddress></items
                                                  //| ><items><v type="String">0</v><TestAddress type="riftwarp.TestAddress" styl
                                                  //| e="noisy"><city><v type="String">Hamburg</v></city><street><v type="String"
                                                  //| >Am Hafen</v></street></TestAddress></items><items><v type="String">2</v><T
                                                  //| estAddress type="riftwarp.TestAddress" style="noisy"><city><v type="String"
                                                  //| >Los Angeles </v></city><street><v type="String">Sunset Boulevard</v></stre
                                                  //| et></TestAddress></items><items><v type="String">z</v><v type="DateTime">20
                                                  //| 13-03-13T12:05:20.665+01:00</v></items></items></mapStringAnyWithUnknown></
                                                  //| ComplexMaps></complexMaps><addressOpt><TestAddress type="riftwarp.TestAddre
                                                  //| ss" style="noisy"><city><v type="String">Berlin</v></city><street><v type="
                                                  //| String">At the wall 89</v></street></TestAddress></addressOpt><trees><Trees
                                                  //|  type="riftwarp.Trees" style="noisy"><intTree><items><v type="Int">1</v><it
                                                  //| ems><items><v type="Int">21</v><items><items><v type="Int">31</v><items></i
                                                  //| tems></items></items></items><items><v type="Int">22</v><items></items></it
                                                  //| ems><items><v type="Int">23</v><items><items><v type="Int">31</v><items></i
                                                  //| tems></items><items><v type="Int">32</v><items><items><v type="Int">41</v><
                                                  //| items></items></items></items></items><items><v type="Int">33</v><items></i
                                                  //| tems></items></items></items></items></items></intTree><addressTree><items>
                                                  //| <TestAddress type="riftwarp.TestAddress" style="noisy"><city><v type="Strin
                                                  //| g">Hamburg</v></city><street><v type="String">Am Hafen</v></street></TestAd
                                                  //| dress><items><items><TestAddress type="riftwarp.TestAddress" style="noisy">
                                                  //| <city><v type="String">New York</v></city><street><v type="String">Broadway
                                                  //| </v></street></TestAddress><items></items></items><items><TestAddress type=
                                                  //| "riftwarp.TestAddress" style="noisy"><city><v type="String">Los Angeles </v
                                                  //| ></city><street><v type="String">Sunset Boulevard</v></street></TestAddress
                                                  //| ><items></items></items></items></items></addressTree></Trees></trees></Tes
                                                  //| tObjectA>))



 val backFromWarpXmlV = riftWarp.receiveFromWarpWithBlobs[DimensionString, TestObjectA](blobFetch)(RiftXml())(warpStreamXmlV.forceResult)
                                                  //> backFromWarpXmlV  : almhirt.common.AlmValidation[riftwarp.TestObjectA] = Su
                                                  //| ccess(TestObjectA([B@46e1e0c8,[B@36325538,PrimitiveTypes(I am Pete and here
                                                  //| s some invalid embedded xml: <h1>I am great!</h1>. And heres some JSON: {"m
                                                  //| essage: ["I am", "great"]},true,127,-237823,-278234263,26587625768237658736
                                                  //| 5863876528756875682765252520577305007209857025728132213242,1.3675,1.3672322
                                                  //| 350005,23761247614876823746.23846749182408,2013-03-13T12:05:20.571+01:00,fa
                                                  //| 3ce73b-5a0f-46af-a3bf-6db860f32ad7),PrimitiveListMAs(List(alpha, beta, gamm
                                                  //| a, delta),List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),List(1.0, 0.5, 0.2, 0.125),Li
                                                  //| st(1.333333, 1.33333335, 1.6666666, 1.6666667),List(2013-03-13T13:05:20.649
                                                  //| +01:00, 2013-03-13T14:05:20.649+01:00, 2013-03-13T15:05:20.649+01:00, 2013-
                                                  //| 03-13T16:05:20.649+01:00)),PrimitiveVectorMAs(Vector(alpha, beta, gamma, de
                                                  //| lta),Vector(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),Vector(1.0, 0.5, 0.2, 0.125),Vec
                                                  //| tor(1.333333, 1.33333335, 1.6666666, 1.6666667),Vector(2013-03-13T13:05:20.
                                                  //| 649+01:00, 2013-03-13T14:05:20.649+01:00, 2013-03-13T15:05:20.649+01:00, 20
                                                  //| 13-03-13T16:05:20.649+01:00)),Some(PrimitiveSetMAs(Set(alpha, beta, gamma, 
                                                  //| delta),Set(5, 10, 1, 6, 9, 2, 7, 3, 8, 4),Set(1.0, 0.5, 0.2, 0.125),Set(1.3
                                                  //| 33333, 1.33333335, 1.6666666, 1.6666667),None)),PrimitiveIterableMAs(Set(al
                                                  //| pha, beta, gamma, delta),Set(5, 10, 1, 6, 9, 2, 7, 3, 8, 4),Set(1.0, 0.5, 0
                                                  //| .2, 0.125),Set(1.333333, 1.33333335, 1.6666666, 1.6666667),Set(2013-03-13T1
                                                  //| 3:05:20.649+01:00, 2013-03-13T14:05:20.649+01:00, 2013-03-13T15:05:20.649+0
                                                  //| 1:00, 2013-03-13T16:05:20.649+01:00)),ComplexMAs(List(TestAddress(Hamburg,A
                                                  //| m Hafen), TestAddress(New York,Broadway), TestAddress(Los Angeles ,Sunset B
                                                  //| oulevard)),Vector(TestAddress(Hamburg,Am Hafen), TestAddress(New York,Broad
                                                  //| way), TestAddress(Los Angeles ,Sunset Boulevard)),Set(TestAddress(Hamburg,A
                                                  //| m Hafen), TestAddress(New York,Broadway), TestAddress(Los Angeles ,Sunset B
                                                  //| oulevard)),List(true, hello, 1, 2, 3.0, 3.0, TestAddress(Somewhere,here))),
                                                  //| PrimitiveMaps(Map(1 -> 10, 2 -> 20, 3 -> 30, 4 -> 40),Map(a -> 1, b -> 2, c
                                                  //|  -> 3),Map(1be3d9a0-f9bb-48ea-942d-08d26c9fde80 -> 2013-03-13T12:05:20.649+
                                                  //| 01:00, eef59f32-2a35-4be3-9573-f66e9082a03c -> 2013-03-14T12:05:20.649+01:0
                                                  //| 0, c555d326-0a8b-461b-b77d-52f8104f779a -> 2013-03-15T12:05:20.649+01:00)),
                                                  //| ComplexMaps(Map(0 -> TestAddress(Hamburg,Am Hafen), 1 -> TestAddress(New Yo
                                                  //| rk,Broadway), 2 -> TestAddress(Los Angeles ,Sunset Boulevard)),Map(0 -> Tes
                                                  //| tAddress(Hamburg,Am Hafen), 1 -> TestAddress(New York,Broadway), 2 -> TestA
                                                  //| ddress(Los Angeles ,Sunset Boulevard)),Map(x -> b1a75d0b-9d95-468e-80f5-cb8
                                                  //| 4989ba8ff, unspecifiedProblem -> almhirt.common.UnspecifiedProblem
                                                  //| Test
                                                  //| Category: SystemProblem
                                                  //| Severity: Major
                                                  //| Arguments: Map(arg1 -> 95)
                                                  //| , y -> 2c990cc2-9eb1-40fe-9d61-8b32df162ea8, 1 -> TestAddress(New York,Broa
                                                  //| dway), 0 -> TestAddress(Hamburg,Am Hafen), 2 -> TestAddress(Los Angeles ,Su
                                                  //| nset Boulevard), z -> 2013-03-13T12:05:20.665+01:00)),Some(TestAddress(Berl
                                                  //| in,At the wall 89)),Trees(<tree>,<tree>)))

  println(blobdata)                               //> Map(4255f7bc-1b50-408c-8606-7bfab7ba825d -> [B@36325538, 1d9ec08b-bfa6-4ccb
                                                  //| -ad1e-3fb17edeee08 -> [B@36325538)


  riftWarp.prepareForWarp[DimensionCord](RiftJson())(testObject).flatMap(warpStream =>
    riftWarp.receiveFromWarp[DimensionCord, TestObjectA](RiftJson())(warpStream)).map(rearrived =>
    rearrived == testObject)                      //> res2: scalaz.Validation[almhirt.common.Problem,Boolean] = Success(false)

  //  riftWarp.prepareForWarp[DimensionRawMap](RiftMap())(testObject).flatMap(warpStream =>
  //    riftWarp.receiveFromWarp[DimensionRawMap, TestObjectA](RiftMap())(warpStream)).map(rearrived =>
  //    rearrived == testObject)

}