package riftwarp.worksheets

import scalaz.syntax.validation._
import almhirt.syntax.almvalidation._
import riftwarp._
import riftwarp.impl._

object Worksheet {
  val riftWarp = RiftWarp.concurrentWithDefaults  //> riftWarp  : riftwarp.RiftWarp = riftwarp.RiftWarp$$anon$1@24e0a659
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



  val testObject = TestObjectA.pete               //> testObject  : riftwarp.TestObjectA = TestObjectA([B@70b139c9,[B@348f4ec,Pri
                                                  //| mitiveTypes(I am Pete,true,127,-237823,-278234263,2658762576823765873658638
                                                  //| 76528756875682765252520577305007209857025728132213242,1.3675,1.367232235000
                                                  //| 5,23761247614876823746.23846749182408,2013-03-11T14:40:41.910+01:00,edad266
                                                  //| d-4a56-4815-8e3c-fdfa8ec3c2ab),PrimitiveListMAs(List(alpha, beta, gamma, de
                                                  //| lta),List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),List(1.0, 0.5, 0.2, 0.125),List(1.
                                                  //| 333333, 1.33333335, 1.6666666, 1.6666667),List(2013-03-11T15:40:41.988+01:0
                                                  //| 0, 2013-03-11T16:40:41.988+01:00, 2013-03-11T17:40:41.988+01:00, 2013-03-11
                                                  //| T18:40:41.988+01:00)),PrimitiveVectorMAs(Vector(alpha, beta, gamma, delta),
                                                  //| Vector(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),Vector(1.0, 0.5, 0.2, 0.125),Vector(1
                                                  //| .333333, 1.33333335, 1.6666666, 1.6666667),Vector(2013-03-11T15:40:41.988+0
                                                  //| 1:00, 2013-03-11T16:40:41.988+01:00, 2013-03-11T17:40:41.988+01:00, 2013-03
                                                  //| -11T18:40:41.988+01:00)),Some(PrimitiveSetMAs(Set(alpha, beta, gamma, delta
                                                  //| ),Set(5, 10, 1, 6, 9, 2, 7, 3, 8, 4),Set(1.0, 0.5, 0.2, 0.125),Set(1.333333
                                                  //| , 1.33333335, 1.6666666, 1.6666667),None)),PrimitiveIterableMAs(List(alpha,
                                                  //|  beta, gamma, delta),List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),List(1.0, 0.5, 0.2
                                                  //| , 0.125),List(1.333333, 1.33333335, 1.6666666, 1.6666667),List(2013-03-11T1
                                                  //| 5:40:41.988+01:00, 2013-03-11T16:40:41.988+01:00, 2013-03-11T17:40:41.988+0
                                                  //| 1:00, 2013-03-11T18:40:41.988+01:00)),ComplexMAs(List(TestAddress(Hamburg,A
                                                  //| m Hafen), TestAddress(New York,Broadway), TestAddress(Los Angeles ,Sunset B
                                                  //| oulevard)),Vector(TestAddress(Hamburg,Am Hafen), TestAddress(New York,Broad
                                                  //| way), TestAddress(Los Angeles ,Sunset Boulevard)),Set(TestAddress(Hamburg,A
                                                  //| m Hafen), TestAddress(New York,Broadway), TestAddress(Los Angeles ,Sunset B
                                                  //| oulevard)),List(true, hello, 1, 2, 3.0, 3.0, TestAddress(Somewhere,here))),
                                                  //| PrimitiveMaps(Map(1 -> 10, 2 -> 20, 3 -> 30, 4 -> 40),Map(a -> 1, b -> 2, c
                                                  //|  -> 3),Map(0c10a5c9-3c2a-47e0-9348-4dcd8cef2180 -> 2013-03-11T14:40:42.004+
                                                  //| 01:00, 7cf9d8f3-d3b9-48da-8646-9433e8d319c4 -> 2013-03-12T14:40:42.004+01:0
                                                  //| 0, 03ee24af-c84d-413b-8f43-be45a8dc24b0 -> 2013-03-13T14:40:42.004+01:00)),
                                                  //| ComplexMaps(Map(0 -> TestAddress(Hamburg,Am Hafen), 1 -> TestAddress(New Yo
                                                  //| rk,Broadway), 2 -> TestAddress(Los Angeles ,Sunset Boulevard)),Map(0 -> Tes
                                                  //| tAddress(Hamburg,Am Hafen), 1 -> TestAddress(New York,Broadway), 2 -> TestA
                                                  //| ddress(Los Angeles ,Sunset Boulevard)),Map(x -> f768b9d8-59ca-4c44-baaa-e9e
                                                  //| 0ec2f9222, unspecifiedProblem -> almhirt.common.UnspecifiedProblem
                                                  //| Test
                                                  //| Category: SystemProblem
                                                  //| Severity: Major
                                                  //| Arguments: Map(arg1 -> 95)
                                                  //| , y -> 312381a9-020e-4801-aab7-a3f96c572110, 1 -> TestAddress(New York,Broa
                                                  //| dway), 0 -> TestAddress(Hamburg,Am Hafen), 2 -> TestAddress(Los Angeles ,Su
                                                  //| nset Boulevard), unknownType -> UnknownObject(1), z -> 2013-03-11T14:40:42.
                                                  //| 020+01:00)),Some(TestAddress(Berlin,At the wall 89)))
 
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
                                                  //| er":"RiftBlobRefByName","version":null},"name":"e59bb0a6-aba0-4bad-951a-df9
                                                  //| 21e3711c0"},"primitiveTypes":{"riftdesc":{"identifier":"riftwarp.PrimitiveT
                                                  //| ypes","version":null},"str":"I am Pete","bool":true,"byte":127,"int":-23782
                                                  //| 3,"long":-278234263,"bigInt":"265876257682376587365863876528756875682765252
                                                  //| 520577305007209857025728132213242","float":1.3674999475479126,"double":1.36
                                                  //| 72322350005,"bigDec":"23761247614876823746.23846749182408","dateTime":"2013
                                                  //| -03-11T14:40:41.910+01:00","uuid":"edad266d-4a56-4815-8e3c-fdfa8ec3c2ab"},"
                                                  //| primitiveListMAs":{"riftdesc":{"identifier":"riftwarp.PrimitiveListMAs","ve
                                                  //| rsion":null},"listString":["alpha","beta","gamma","delta"],"listInt":[1,2,3
                                                  //| ,4,5,6,7,8,9,10],"listDouble":[1.0,0.5,0.2,0.125],"listBigDecimal":["1.3333
                                                  //| 33","1.33333335","1.6666666","1.6666667"],"listDateTime":["2013-03-11T15:40
                                                  //| :41.988+01:00","2013-03-11T16:40:41.988+01:00","2013-03-11T17:40:41.988+01:
                                                  //| 00","2013-03-11T18:40:41.988+01:00"]},"primitiveVectorMAs":{"riftdesc":{"id
                                                  //| entifier":"riftwarp.PrimitiveVectorMAs","version":null},"vectorString":["al
                                                  //| pha","beta","gamma","delta"],"vectorInt":[1,2,3,4,5,6,7,8,9,10],"vectorDoub
                                                  //| le":[1.0,0.5,0.2,0.125],"vectorBigDecimal":["1.333333","1.33333335","1.6666
                                                  //| 666","1.6666667"],"vectorDateTime":["2013-03-11T15:40:41.988+01:00","2013-0
                                                  //| 3-11T16:40:41.988+01:00","2013-03-11T17:40:41.988+01:00","2013-03-11T18:40:
                                                  //| 41.988+01:00"]},"primitiveSetMAs":{"riftdesc":{"identifier":"riftwarp.Primi
                                                  //| tiveSetMAs","version":null},"setString":["alpha","beta","gamma","delta"],"s
                                                  //| etInt":[5,10,1,6,9,2,7,3,8,4],"setDouble":[1.0,0.5,0.2,0.125],"setBigDecima
                                                  //| l":["1.333333","1.33333335","1.6666666","1.6666667"],"setDateTime":null},"p
                                                  //| rimitiveIterableMAs":{"riftdesc":{"identifier":"riftwarp.PrimitiveIterableM
                                                  //| As","version":null},"iterableString":["alpha","beta","gamma","delta"],"iter
                                                  //| ableInt":[1,2,3,4,5,6,7,8,9,10],"iterableDouble":[1.0,0.5,0.2,0.125],"itera
                                                  //| bleBigDecimal":["1.333333","1.33333335","1.6666666","1.6666667"],"iterableD
                                                  //| ateTime":["2013-03-11T15:40:41.988+01:00","2013-03-11T16:40:41.988+01:00","
                                                  //| 2013-03-11T17:40:41.988+01:00","2013-03-11T18:40:41.988+01:00"]},"complexMA
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
                                                  //| StringInt":[["a",1],["b",2],["c",3]],"mapUuidDateTime":[["0c10a5c9-3c2a-47e
                                                  //| 0-9348-4dcd8cef2180","2013-03-11T14:40:42.004+01:00"],["7cf9d8f3-d3b9-48da-
                                                  //| 8646-9433e8d319c4","2013-03-12T14:40:42.004+01:00"],["03ee24af-c84d-413b-8f
                                                  //| 43-be45a8dc24b0","2013-03-13T14:40:42.004+01:00"]]},"complexMaps":{"riftdes
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
                                                  //| oulevard"}]],"mapStringAnyWithUnknown":[["x","f768b9d8-59ca-4c44-baaa-e9e0e
                                                  //| c2f9222"],["unspecifiedProblem",{"riftdesc":{"identifier":"almhirt.common.U
                                                  //| nspecifiedProblem","version":null},"message":"Test","severity":"Major","cat
                                                  //| egory":"SystemProblem","args":[["arg1",95]],"cause":null}],["y","312381a9-0
                                                  //| 20e-4801-aab7-a3f96c572110"],["1",{"riftdesc":{"identifier":"riftwarp.TestA
                                                  //| ddress","version":null},"city":"New York","street":"Broadway"}],["0",{"rift
                                                  //| desc":{"identifier":"riftwarp.TestAddress","version":null},"city":"Hamburg"
                                                  //| ,"street":"Am Hafen"}],["2",{"riftdesc":{"identifier":"riftwarp.TestAddress
                                                  //| ","version":null},"city":"Los Angeles ","street":"Sunset Boulevard"}],["z",
                                                  //| "2013-03-11T14:40:42.020+01:00"]]},"addressOpt":{"riftdesc":{"identifier":"
                                                  //| riftwarp.TestAddress","version":null},"city":"Berlin","street":"At the wall
                                                  //|  89"}}))
       
                        
                        
  println(blobdata)                               //> Map(e59bb0a6-aba0-4bad-951a-df921e3711c0 -> [B@348f4ec)
                                       
  val backFromWarpV = riftWarp.receiveFromWarpWithBlobs[DimensionString, TestObjectA](blobFetch)(RiftJson())(warpStreamV.forceResult)
                                                  //> backFromWarpV  : almhirt.common.AlmValidation[riftwarp.TestObjectA] = Failu
                                                  //| re(almhirt.common.UnspecifiedProblem
                                                  //| No recomposer found for RiftDescriptor 'RiftDescriptor(riftwarp.TestObjectA
                                                  //| ;0)')
                                                  //| Category: SystemProblem
                                                  //| Severity: Major
                                                  //| Arguments: Map()
                                                  //| )
 
  val backFromWarp = backFromWarpV.forceResult    //> almhirt.common.ResultForcedFromValidationException: A value has been forced
                                                  //|  from a failure: No recomposer found for RiftDescriptor 'RiftDescriptor(rif
                                                  //| twarp.TestObjectA;0)')
                                                  //| 	at almhirt.almvalidation.AlmValidationOps5$$anonfun$forceResult$1.apply(
                                                  //| AlmValidationOps.scala:123)
                                                  //| 	at almhirt.almvalidation.AlmValidationOps5$$anonfun$forceResult$1.apply(
                                                  //| AlmValidationOps.scala:123)
                                                  //| 	at scalaz.Validation$class.fold(Validation.scala:64)
                                                  //| 	at scalaz.Failure.fold(Validation.scala:330)
                                                  //| 	at almhirt.almvalidation.AlmValidationOps5$class.forceResult(AlmValidati
                                                  //| onOps.scala:123)
                                                  //| 	at almhirt.almvalidation.ToAlmValidationOps$$anon$6.forceResult(AlmValid
                                                  //| ationOps.scala:205)
                                                  //| 	at riftwarp.worksheets.Worksheet$$anonfun$main$1.apply$mcV$sp(riftwarp.w
                                                  //| orksheets.Worksheet.scala:57)
                                                  //| 	at org.scalaide.worksheet.runtime.library.WorksheetSupport$$anonfun$$exe
                                                  //| cute$1.apply$mcV$sp(WorksheetSupport.scala:76)
                                                  //| 	at org.scalaide.worksheet.runtime.library.WorksheetSupport$.redirected(W
                                                  //| orksheetSupport.scala:65)
                                                  //| 	at org.scalaide.worksheet.runtime.library.WorksheetSupport$.$execute(Wor
                                                  //| ksheetSupport.scala:75)
                                                  //| 	at riftwarp.worksheets.Worksheet$.main(riftwarp.worksheets.Worksheet.sca
                                                  //| la:8)
                                                  //| 	at riftwarp.worksheets.Worksheet.main(riftwarp.worksheets.Worksheet.scal
                                                  //| a)

  testObject == backFromWarp


  testObject.primitiveTypes == backFromWarp.primitiveTypes
  
  riftWarp.prepareForWarp[DimensionCord](RiftJson())(testObject).flatMap(warpStream =>
    riftWarp.receiveFromWarp[DimensionCord, TestObjectA](RiftJson())(warpStream)).map(rearrived =>
    rearrived == testObject)


//  riftWarp.prepareForWarp[DimensionRawMap](RiftMap())(testObject).flatMap(warpStream =>
//    riftWarp.receiveFromWarp[DimensionRawMap, TestObjectA](RiftMap())(warpStream)).map(rearrived =>
//    rearrived == testObject)


}