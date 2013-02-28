package riftwarp.worksheets

import scalaz.syntax.validation._
import almhirt.syntax.almvalidation._
import riftwarp._
import riftwarp.impl._

object Worksheet {
  val riftWarp = RiftWarp.concurrentWithDefaults  //> riftWarp  : <error> = riftwarp.RiftWarp$$anon$1@36c5e73a
  riftWarp.barracks.addDecomposer(new TestObjectADecomposer())
                                                  //> res0: <error> = ()
  riftWarp.barracks.addRecomposer(new TestObjectARecomposer())
                                                  //> res1: <error> = ()
  riftWarp.barracks.addDecomposer(new TestAddressDecomposer())
                                                  //> res2: <error> = ()
  riftWarp.barracks.addRecomposer(new TestAddressRecomposer())
                                                  //> res3: <error> = ()

  riftWarp.barracks.addDecomposer(new PrimitiveTypesDecomposer())
                                                  //> res4: <error> = ()
  riftWarp.barracks.addRecomposer(new PrimitiveTypesRecomposer())
                                                  //> res5: <error> = ()
  riftWarp.barracks.addDecomposer(new PrimitiveListMAsDecomposer())
                                                  //> res6: <error> = ()
  riftWarp.barracks.addRecomposer(new PrimitiveListMAsRecomposer())
                                                  //> res7: <error> = ()
  riftWarp.barracks.addDecomposer(new PrimitiveVectorMAsDecomposer())
                                                  //> res8: <error> = ()
  riftWarp.barracks.addRecomposer(new PrimitiveVectorMAsRecomposer())
                                                  //> res9: <error> = ()
  riftWarp.barracks.addDecomposer(new PrimitiveSetMAsDecomposer())
                                                  //> res10: <error> = ()
  riftWarp.barracks.addRecomposer(new PrimitiveSetMAsRecomposer())
                                                  //> res11: <error> = ()
  riftWarp.barracks.addDecomposer(new PrimitiveIterableMAsDecomposer())
                                                  //> res12: <error> = ()
  riftWarp.barracks.addRecomposer(new PrimitiveIterableMAsRecomposer())
                                                  //> res13: <error> = ()
  riftWarp.barracks.addDecomposer(new ComplexMAsDecomposer())
                                                  //> res14: <error> = ()
  riftWarp.barracks.addRecomposer(new ComplexMAsRecomposer())
                                                  //> res15: <error> = ()
  riftWarp.barracks.addDecomposer(new PrimitiveMapsDecomposer())
                                                  //> res16: <error> = ()
  riftWarp.barracks.addRecomposer(new PrimitiveMapsRecomposer())
                                                  //> res17: <error> = ()
  riftWarp.barracks.addDecomposer(new ComplexMapsDecomposer())
                                                  //> res18: <error> = ()
  riftWarp.barracks.addRecomposer(new ComplexMapsRecomposer())
                                                  //> res19: <error> = ()



  val testObject = TestObjectA.pete               //> testObject  : riftwarp.TestObjectA = TestObjectA([B@255fcb67,[B@3422ea5c,Pr
                                                  //| imitiveTypes(I am Pete,true,127,-237823,-278234263,265876257682376587365863
                                                  //| 876528756875682765252520577305007209857025728132213242,1.3675,1.36723223500
                                                  //| 05,23761247614876823746.23846749182408,2013-02-28T09:47:15.208+01:00,02349c
                                                  //| ee-c8f1-4a31-b3e2-bbd9cc969a19),PrimitiveListMAs(List(alpha, beta, gamma, d
                                                  //| elta),List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),List(1.0, 0.5, 0.2, 0.125),List(1
                                                  //| .333333, 1.33333335, 1.6666666, 1.6666667),List(2013-02-28T10:47:15.286+01:
                                                  //| 00, 2013-02-28T11:47:15.286+01:00, 2013-02-28T12:47:15.286+01:00, 2013-02-2
                                                  //| 8T13:47:15.286+01:00)),PrimitiveVectorMAs(Vector(alpha, beta, gamma, delta)
                                                  //| ,Vector(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),Vector(1.0, 0.5, 0.2, 0.125),Vector(
                                                  //| 1.333333, 1.33333335, 1.6666666, 1.6666667),Vector(2013-02-28T10:47:15.286+
                                                  //| 01:00, 2013-02-28T11:47:15.286+01:00, 2013-02-28T12:47:15.286+01:00, 2013-0
                                                  //| 2-28T13:47:15.286+01:00)),Some(PrimitiveSetMAs(Set(alpha, beta, gamma, delt
                                                  //| a),Set(5, 10, 1, 6, 9, 2, 7, 3, 8, 4),Set(1.0, 0.5, 0.2, 0.125),Set(1.33333
                                                  //| 3, 1.33333335, 1.6666666, 1.6666667),None)),PrimitiveIterableMAs(List(alpha
                                                  //| , beta, gamma, delta),List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),List(1.0, 0.5, 0.
                                                  //| 2, 0.125),List(1.333333, 1.33333335, 1.6666666, 1.6666667),List(2013-02-28T
                                                  //| 10:47:15.302+01:00, 2013-02-28T11:47:15.302+01:00, 2013-02-28T12:47:15.302+
                                                  //| 01:00, 2013-02-28T13:47:15.302+01:00)),ComplexMAs(List(TestAddress(Hamburg,
                                                  //| Am Hafen), TestAddress(New York,Broadway), TestAddress(Los Angeles ,Sunset 
                                                  //| Boulevard)),Vector(TestAddress(Hamburg,Am Hafen), TestAddress(New York,Broa
                                                  //| dway), TestAddress(Los Angeles ,Sunset Boulevard)),Set(TestAddress(Hamburg,
                                                  //| Am Hafen), TestAddress(New York,Broadway), TestAddress(Los Angeles ,Sunset 
                                                  //| Boulevard)),List(true, hello, 1, 2, 3.0, 3.0, TestAddress(Somewhere,here)))
                                                  //| ,PrimitiveMaps(Map(1 -> 10, 2 -> 20, 3 -> 30, 4 -> 40),Map(a -> 1, b -> 2, 
                                                  //| c -> 3),Map(54ade8cb-4ca2-4daa-9495-5f59402b3532 -> 2013-02-28T09:47:15.302
                                                  //| +01:00, 2dea46c4-5fca-49ed-9a34-b8eb8d1b204d -> 2013-03-01T09:47:15.302+01:
                                                  //| 00, 16a35d78-1786-4c0d-bcc1-21761882b676 -> 2013-03-02T09:47:15.302+01:00))
                                                  //| ,ComplexMaps(Map(0 -> TestAddress(Hamburg,Am Hafen), 1 -> TestAddress(New Y
                                                  //| ork,Broadway), 2 -> TestAddress(Los Angeles ,Sunset Boulevard)),Map(0 -> Te
                                                  //| stAddress(Hamburg,Am Hafen), 1 -> TestAddress(New York,Broadway), 2 -> Test
                                                  //| Address(Los Angeles ,Sunset Boulevard)),Map(x -> 81202b44-81e0-4946-945c-68
                                                  //| ac3bb25fff, unspecifiedProblem -> almhirt.common.UnspecifiedProblem
                                                  //| Test
                                                  //| Category: SystemProblem
                                                  //| Severity: Major
                                                  //| Arguments: Map(arg1 -> 95)
                                                  //| , y -> 17be47e3-0930-4c56-9ee5-b969c47e414a, 1 -> TestAddress(New York,Broa
                                                  //| dway), 0 -> TestAddress(Hamburg,Am Hafen), 2 -> TestAddress(Los Angeles ,Su
                                                  //| nset Boulevard), unknownType -> UnknownObject(1), z -> 2013-02-28T09:47:15.
                                                  //| 317+01:00)),Some(TestAddress(Berlin,At the wall 89)))
 
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
                                                  //> warpStreamV  : <error> = Success(DimensionString({"riftdesc":{"identifier":
                                                  //| "riftwarp.TestObjectA","version":null},"arrayByte":[126,-123,12,-45,-128],"
                                                  //| blob":{"riftdesc":{"identifier":"RiftBlobRefByName","version":null},"name":
                                                  //| "958b3d4c-eef8-4a53-9227-37d60efd8e21"},"primitiveTypes":{"riftdesc":{"iden
                                                  //| tifier":"riftwarp.PrimitiveTypes","version":null},"str":"I am Pete","bool":
                                                  //| true,"byte":127,"int":-237823,"long":-278234263,"bigInt":"26587625768237658
                                                  //| 7365863876528756875682765252520577305007209857025728132213242","float":1.36
                                                  //| 74999475479126,"double":1.3672322350005,"bigDec":"23761247614876823746.2384
                                                  //| 6749182408","dateTime":"2013-02-28T09:47:15.208+01:00","uuid":"02349cee-c8f
                                                  //| 1-4a31-b3e2-bbd9cc969a19"},"primitiveListMAs":{"riftdesc":{"identifier":"ri
                                                  //| ftwarp.PrimitiveListMAs","version":null},"listString":["alpha","beta","gamm
                                                  //| a","delta"],"listInt":[1,2,3,4,5,6,7,8,9,10],"listDouble":[1.0,0.5,0.2,0.12
                                                  //| 5],"listBigDecimal":["1.333333","1.33333335","1.6666666","1.6666667"],"list
                                                  //| DateTime":["2013-02-28T10:47:15.286+01:00","2013-02-28T11:47:15.286+01:00",
                                                  //| "2013-02-28T12:47:15.286+01:00","2013-02-28T13:47:15.286+01:00"]},"primitiv
                                                  //| eVectorMAs":{"riftdesc":{"identifier":"riftwarp.PrimitiveVectorMAs","versio
                                                  //| n":null},"vectorString":["alpha","beta","gamma","delta"],"vectorInt":[1,2,3
                                                  //| ,4,5,6,7,8,9,10],"vectorDouble":[1.0,0.5,0.2,0.125],"vectorBigDecimal":["1.
                                                  //| 333333","1.33333335","1.6666666","1.6666667"],"vectorDateTime":["2013-02-28
                                                  //| T10:47:15.286+01:00","2013-02-28T11:47:15.286+01:00","2013-02-28T12:47:15.2
                                                  //| 86+01:00","2013-02-28T13:47:15.286+01:00"]},"primitiveSetMAs":{"riftdesc":{
                                                  //| "identifier":"riftwarp.PrimitiveSetMAs","version":null},"setString":["alpha
                                                  //| ","beta","gamma","delta"],"setInt":[5,10,1,6,9,2,7,3,8,4],"setDouble":[1.0,
                                                  //| 0.5,0.2,0.125],"setBigDecimal":["1.333333","1.33333335","1.6666666","1.6666
                                                  //| 667"],"setDateTime":null},"primitiveIterableMAs":{"riftdesc":{"identifier":
                                                  //| "riftwarp.PrimitiveIterableMAs","version":null},"iterableString":["alpha","
                                                  //| beta","gamma","delta"],"iterableInt":[1,2,3,4,5,6,7,8,9,10],"iterableDouble
                                                  //| ":[1.0,0.5,0.2,0.125],"iterableBigDecimal":["1.333333","1.33333335","1.6666
                                                  //| 666","1.6666667"],"iterableDateTime":["2013-02-28T10:47:15.302+01:00","2013
                                                  //| -02-28T11:47:15.302+01:00","2013-02-28T12:47:15.302+01:00","2013-02-28T13:4
                                                  //| 7:15.302+01:00"]},"complexMAs":{"riftdesc":{"identifier":"riftwarp.ComplexM
                                                  //| As","version":null},"addresses1":[{"riftdesc":{"identifier":"riftwarp.TestA
                                                  //| ddress","version":null},"city":"Hamburg","street":"Am Hafen"},{"riftdesc":{
                                                  //| "identifier":"riftwarp.TestAddress","version":null},"city":"New York","stre
                                                  //| et":"Broadway"},{"riftdesc":{"identifier":"riftwarp.TestAddress","version":
                                                  //| null},"city":"Los Angeles ","street":"Sunset Boulevard"}],"addresses2":[{"r
                                                  //| iftdesc":{"identifier":"riftwarp.TestAddress","version":null},"city":"Hambu
                                                  //| rg","street":"Am Hafen"},{"riftdesc":{"identifier":"riftwarp.TestAddress","
                                                  //| version":null},"city":"New York","street":"Broadway"},{"riftdesc":{"identif
                                                  //| ier":"riftwarp.TestAddress","version":null},"city":"Los Angeles ","street":
                                                  //| "Sunset Boulevard"}],"addresses3":[{"riftdesc":{"identifier":"riftwarp.Test
                                                  //| Address","version":null},"city":"Hamburg","street":"Am Hafen"},{"riftdesc":
                                                  //| {"identifier":"riftwarp.TestAddress","version":null},"city":"New York","str
                                                  //| eet":"Broadway"},{"riftdesc":{"identifier":"riftwarp.TestAddress","version"
                                                  //| :null},"city":"Los Angeles ","street":"Sunset Boulevard"}],"anything":[true
                                                  //| ,"hello",1,2,3.0,3.0,{"riftdesc":{"identifier":"riftwarp.TestAddress","vers
                                                  //| ion":null},"city":"Somewhere","street":"here"}]},"primitiveMaps":{"riftdesc
                                                  //| ":{"identifier":"riftwarp.PrimitiveMaps","version":null},"mapIntInt":[[1,10
                                                  //| ],[2,20],[3,30],[4,40]],"mapStringInt":[["a",1],["b",2],["c",3]],"mapUuidDa
                                                  //| teTime":[["54ade8cb-4ca2-4daa-9495-5f59402b3532","2013-02-28T09:47:15.302+0
                                                  //| 1:00"],["2dea46c4-5fca-49ed-9a34-b8eb8d1b204d","2013-03-01T09:47:15.302+01:
                                                  //| 00"],["16a35d78-1786-4c0d-bcc1-21761882b676","2013-03-02T09:47:15.302+01:00
                                                  //| "]]},"complexMaps":{"riftdesc":{"identifier":"riftwarp.ComplexMaps","versio
                                                  //| n":null},"mapIntTestAddress1":[[0,{"riftdesc":{"identifier":"riftwarp.TestA
                                                  //| ddress","version":null},"city":"Hamburg","street":"Am Hafen"}],[1,{"riftdes
                                                  //| c":{"identifier":"riftwarp.TestAddress","version":null},"city":"New York","
                                                  //| street":"Broadway"}],[2,{"riftdesc":{"identifier":"riftwarp.TestAddress","v
                                                  //| ersion":null},"city":"Los Angeles ","street":"Sunset Boulevard"}]],"mapIntA
                                                  //| ny":[[0,{"riftdesc":{"identifier":"riftwarp.TestAddress","version":null},"c
                                                  //| ity":"Hamburg","street":"Am Hafen"}],[1,{"riftdesc":{"identifier":"riftwarp
                                                  //| .TestAddress","version":null},"city":"New York","street":"Broadway"}],[2,{"
                                                  //| riftdesc":{"identifier":"riftwarp.TestAddress","version":null},"city":"Los 
                                                  //| Angeles ","street":"Sunset Boulevard"}]],"mapStringAnyWithUnknown":[["x","8
                                                  //| 1202b44-81e0-4946-945c-68ac3bb25fff"],["unspecifiedProblem",{"riftdesc":{"i
                                                  //| dentifier":"almhirt.common.UnspecifiedProblem","version":null},"message":"T
                                                  //| est","severity":"Major","category":"SystemProblem","args":[["arg1",95]],"ca
                                                  //| use":null}],["y","17be47e3-0930-4c56-9ee5-b969c47e414a"],["1",{"riftdesc":{
                                                  //| "identifier":"riftwarp.TestAddress","version":null},"city":"New York","stre
                                                  //| et":"Broadway"}],["0",{"riftdesc":{"identifier":"riftwarp.TestAddress","ver
                                                  //| sion":null},"city":"Hamburg","street":"Am Hafen"}],["2",{"riftdesc":{"ident
                                                  //| ifier":"riftwarp.TestAddress","version":null},"city":"Los Angeles ","street
                                                  //| ":"Sunset Boulevard"}],["z","2013-02-28T09:47:15.317+01:00"]]},"addressOpt"
                                                  //| :{"riftdesc":{"identifier":"riftwarp.TestAddress","version":null},"city":"B
                                                  //| erlin","street":"At the wall 89"}}))
                                       
  val backFromWarpV = riftWarp.receiveFromWarpWithBlobs[DimensionString, TestObjectA](blobFetch)(RiftJson())(warpStreamV.forceResult)
                                                  //> backFromWarpV  : <error> = Success(TestObjectA([B@28f593b6,[B@3422ea5c,Prim
                                                  //| itiveTypes(I am Pete,true,127,-237823,-278234263,26587625768237658736586387
                                                  //| 6528756875682765252520577305007209857025728132213242,1.3675,1.3672322350005
                                                  //| ,23761247614876823746.23846749182408,2013-02-28T09:47:15.208+01:00,02349cee
                                                  //| -c8f1-4a31-b3e2-bbd9cc969a19),PrimitiveListMAs(List(alpha, beta, gamma, del
                                                  //| ta),List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),List(1.0, 0.5, 0.2, 0.125),List(1.3
                                                  //| 33333, 1.33333335, 1.6666666, 1.6666667),List(2013-02-28T10:47:15.286+01:00
                                                  //| , 2013-02-28T11:47:15.286+01:00, 2013-02-28T12:47:15.286+01:00, 2013-02-28T
                                                  //| 13:47:15.286+01:00)),PrimitiveVectorMAs(Vector(alpha, beta, gamma, delta),V
                                                  //| ector(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),Vector(1.0, 0.5, 0.2, 0.125),Vector(1.
                                                  //| 333333, 1.33333335, 1.6666666, 1.6666667),Vector(2013-02-28T10:47:15.286+01
                                                  //| :00, 2013-02-28T11:47:15.286+01:00, 2013-02-28T12:47:15.286+01:00, 2013-02-
                                                  //| 28T13:47:15.286+01:00)),Some(PrimitiveSetMAs(Set(alpha, beta, gamma, delta)
                                                  //| ,Set(5, 10, 1, 6, 9, 2, 7, 3, 8, 4),Set(1.0, 0.5, 0.2, 0.125),Set(1.333333,
                                                  //|  1.33333335, 1.6666666, 1.6666667),None)),PrimitiveIterableMAs(Set(alpha, b
                                                  //| eta, gamma, delta),Set(5, 10, 1, 6, 9, 2, 7, 3, 8, 4),Set(1.0, 0.5, 0.2, 0.
                                                  //| 125),Set(1.333333, 1.33333335, 1.6666666, 1.6666667),Set(2013-02-28T10:47:1
                                                  //| 5.302+01:00, 2013-02-28T11:47:15.302+01:00, 2013-02-28T12:47:15.302+01:00, 
                                                  //| 2013-02-28T13:47:15.302+01:00)),ComplexMAs(List(TestAddress(Hamburg,Am Hafe
                                                  //| n), TestAddress(New York,Broadway), TestAddress(Los Angeles ,Sunset Bouleva
                                                  //| rd)),Vector(TestAddress(Hamburg,Am Hafen), TestAddress(New York,Broadway), 
                                                  //| TestAddress(Los Angeles ,Sunset Boulevard)),Set(TestAddress(Hamburg,Am Hafe
                                                  //| n), TestAddress(New York,Broadway), TestAddress(Los Angeles ,Sunset Bouleva
                                                  //| rd)),List(true, hello, 1.0, 2.0, 3.0, 3.0, TestAddress(Somewhere,here))),Pr
                                                  //| imitiveMaps(Map(1 -> 10, 2 -> 20, 3 -> 30, 4 -> 40),Map(a -> 1, b -> 2, c -
                                                  //| > 3),Map(54ade8cb-4ca2-4daa-9495-5f59402b3532 -> 2013-02-28T09:47:15.302+01
                                                  //| :00, 2dea46c4-5fca-49ed-9a34-b8eb8d1b204d -> 2013-03-01T09:47:15.302+01:00,
                                                  //|  16a35d78-1786-4c0d-bcc1-21761882b676 -> 2013-03-02T09:47:15.302+01:00)),Co
                                                  //| mplexMaps(Map(0 -> TestAddress(Hamburg,Am Hafen), 1 -> TestAddress(New York
                                                  //| ,Broadway), 2 -> TestAddress(Los Angeles ,Sunset Boulevard)),Map(0 -> TestA
                                                  //| ddress(Hamburg,Am Hafen), 1 -> TestAddress(New York,Broadway), 2 -> TestAdd
                                                  //| ress(Los Angeles ,Sunset Boulevard)),Map(x -> 81202b44-81e0-4946-945c-68ac3
                                                  //| bb25fff, unspecifiedProblem -> almhirt.common.UnspecifiedProblem
                                                  //| Test
                                                  //| Category: SystemProblem
                                                  //| Severity: Major
                                                  //| Arguments: Map(arg1 -> 95.0)
                                                  //| , y -> 17be47e3-0930-4c56-9ee5-b969c47e414a, 1 -> TestAddress(New York,Broa
                                                  //| dway), 0 -> TestAddress(Hamburg,Am Hafen), 2 -> TestAddress(Los Angeles ,Su
                                                  //| nset Boulevard), z -> 2013-02-28T09:47:15.317+01:00)),Some(TestAddress(Berl
                                                  //| in,At the wall 89))))
 
  val backFromWarp = backFromWarpV.forceResult    //> backFromWarp  : <error> = TestObjectA([B@28f593b6,[B@3422ea5c,PrimitiveType
                                                  //| s(I am Pete,true,127,-237823,-278234263,26587625768237658736586387652875687
                                                  //| 5682765252520577305007209857025728132213242,1.3675,1.3672322350005,23761247
                                                  //| 614876823746.23846749182408,2013-02-28T09:47:15.208+01:00,02349cee-c8f1-4a3
                                                  //| 1-b3e2-bbd9cc969a19),PrimitiveListMAs(List(alpha, beta, gamma, delta),List(
                                                  //| 1, 2, 3, 4, 5, 6, 7, 8, 9, 10),List(1.0, 0.5, 0.2, 0.125),List(1.333333, 1.
                                                  //| 33333335, 1.6666666, 1.6666667),List(2013-02-28T10:47:15.286+01:00, 2013-02
                                                  //| -28T11:47:15.286+01:00, 2013-02-28T12:47:15.286+01:00, 2013-02-28T13:47:15.
                                                  //| 286+01:00)),PrimitiveVectorMAs(Vector(alpha, beta, gamma, delta),Vector(1, 
                                                  //| 2, 3, 4, 5, 6, 7, 8, 9, 10),Vector(1.0, 0.5, 0.2, 0.125),Vector(1.333333, 1
                                                  //| .33333335, 1.6666666, 1.6666667),Vector(2013-02-28T10:47:15.286+01:00, 2013
                                                  //| -02-28T11:47:15.286+01:00, 2013-02-28T12:47:15.286+01:00, 2013-02-28T13:47:
                                                  //| 15.286+01:00)),Some(PrimitiveSetMAs(Set(alpha, beta, gamma, delta),Set(5, 1
                                                  //| 0, 1, 6, 9, 2, 7, 3, 8, 4),Set(1.0, 0.5, 0.2, 0.125),Set(1.333333, 1.333333
                                                  //| 35, 1.6666666, 1.6666667),None)),PrimitiveIterableMAs(Set(alpha, beta, gamm
                                                  //| a, delta),Set(5, 10, 1, 6, 9, 2, 7, 3, 8, 4),Set(1.0, 0.5, 0.2, 0.125),Set(
                                                  //| 1.333333, 1.33333335, 1.6666666, 1.6666667),Set(2013-02-28T10:47:15.302+01:
                                                  //| 00, 2013-02-28T11:47:15.302+01:00, 2013-02-28T12:47:15.302+01:00, 2013-02-2
                                                  //| 8T13:47:15.302+01:00)),ComplexMAs(List(TestAddress(Hamburg,Am Hafen), TestA
                                                  //| ddress(New York,Broadway), TestAddress(Los Angeles ,Sunset Boulevard)),Vect
                                                  //| or(TestAddress(Hamburg,Am Hafen), TestAddress(New York,Broadway), TestAddre
                                                  //| ss(Los Angeles ,Sunset Boulevard)),Set(TestAddress(Hamburg,Am Hafen), TestA
                                                  //| ddress(New York,Broadway), TestAddress(Los Angeles ,Sunset Boulevard)),List
                                                  //| (true, hello, 1.0, 2.0, 3.0, 3.0, TestAddress(Somewhere,here))),PrimitiveMa
                                                  //| ps(Map(1 -> 10, 2 -> 20, 3 -> 30, 4 -> 40),Map(a -> 1, b -> 2, c -> 3),Map(
                                                  //| 54ade8cb-4ca2-4daa-9495-5f59402b3532 -> 2013-02-28T09:47:15.302+01:00, 2dea
                                                  //| 46c4-5fca-49ed-9a34-b8eb8d1b204d -> 2013-03-01T09:47:15.302+01:00, 16a35d78
                                                  //| -1786-4c0d-bcc1-21761882b676 -> 2013-03-02T09:47:15.302+01:00)),ComplexMaps
                                                  //| (Map(0 -> TestAddress(Hamburg,Am Hafen), 1 -> TestAddress(New York,Broadway
                                                  //| ), 2 -> TestAddress(Los Angeles ,Sunset Boulevard)),Map(0 -> TestAddress(Ha
                                                  //| mburg,Am Hafen), 1 -> TestAddress(New York,Broadway), 2 -> TestAddress(Los 
                                                  //| Angeles ,Sunset Boulevard)),Map(x -> 81202b44-81e0-4946-945c-68ac3bb25fff, 
                                                  //| unspecifiedProblem -> almhirt.common.UnspecifiedProblem
                                                  //| Test
                                                  //| Category: SystemProblem
                                                  //| Severity: Major
                                                  //| Arguments: Map(arg1 -> 95.0)
                                                  //| , y -> 17be47e3-0930-4c56-9ee5-b969c47e414a, 1 -> TestAddress(New York,Broa
                                                  //| dway), 0 -> TestAddress(Hamburg,Am Hafen), 2 -> TestAddress(Los Angeles ,Su
                                                  //| nset Boulevard), z -> 2013-02-28T09:47:15.317+01:00)),Some(TestAddress(Berl
                                                  //| in,At the wall 89)))

  testObject == backFromWarp                      //> res20: Boolean = false


  testObject.primitiveTypes == backFromWarp.primitiveTypes
                                                  //> res21: Boolean = true
  
  riftWarp.prepareForWarp[DimensionCord](RiftJson())(testObject).flatMap(warpStream =>
    riftWarp.receiveFromWarp[DimensionCord, TestObjectA](RiftJson())(warpStream)).map(rearrived =>
    rearrived == testObject)                      //> res22: <error> = Success(false)


//  riftWarp.prepareForWarp[DimensionRawMap](RiftMap())(testObject).flatMap(warpStream =>
//    riftWarp.receiveFromWarp[DimensionRawMap, TestObjectA](RiftMap())(warpStream)).map(rearrived =>
//    rearrived == testObject)


}