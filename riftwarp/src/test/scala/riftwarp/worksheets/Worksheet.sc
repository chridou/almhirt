package riftwarp.worksheets

import scalaz.syntax.validation._
import almhirt.syntax.almvalidation._
import riftwarp._
import riftwarp.impl._

object Worksheet {
  val riftWarp = RiftWarp.concurrentWithDefaults  //> riftWarp  : riftwarp.RiftWarp = riftwarp.RiftWarp$$anon$1@778b8315
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


  val testObject = TestObjectA.pete               //> testObject  : riftwarp.TestObjectA = TestObjectA([B@5081876b,[B@67e1ab17,Pr
                                                  //| imitiveTypes(I am Pete,true,127,-237823,-278234263,265876257682376587365863
                                                  //| 876528756875682765252520577305007209857025728132213242,1.3675,1.36723223500
                                                  //| 05,23761247614876823746.23846749182408,2013-03-03T19:26:12.832+01:00,c3aa49
                                                  //| ff-1336-44f3-9edb-9f61ae5450e2),PrimitiveListMAs(List(alpha, beta, gamma, d
                                                  //| elta),List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),List(1.0, 0.5, 0.2, 0.125),List(1
                                                  //| .333333, 1.33333335, 1.6666666, 1.6666667),List(2013-03-03T20:26:12.878+01:
                                                  //| 00, 2013-03-03T21:26:12.878+01:00, 2013-03-03T22:26:12.878+01:00, 2013-03-0
                                                  //| 3T23:26:12.878+01:00)),PrimitiveVectorMAs(Vector(alpha, beta, gamma, delta)
                                                  //| ,Vector(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),Vector(1.0, 0.5, 0.2, 0.125),Vector(
                                                  //| 1.333333, 1.33333335, 1.6666666, 1.6666667),Vector(2013-03-03T20:26:12.879+
                                                  //| 01:00, 2013-03-03T21:26:12.879+01:00, 2013-03-03T22:26:12.879+01:00, 2013-0
                                                  //| 3-03T23:26:12.879+01:00)),Some(PrimitiveSetMAs(Set(alpha, beta, gamma, delt
                                                  //| a),Set(5, 10, 1, 6, 9, 2, 7, 3, 8, 4),Set(1.0, 0.5, 0.2, 0.125),Set(1.33333
                                                  //| 3, 1.33333335, 1.6666666, 1.6666667),None)),PrimitiveIterableMAs(List(alpha
                                                  //| , beta, gamma, delta),List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),List(1.0, 0.5, 0.
                                                  //| 2, 0.125),List(1.333333, 1.33333335, 1.6666666, 1.6666667),List(2013-03-03T
                                                  //| 20:26:12.880+01:00, 2013-03-03T21:26:12.880+01:00, 2013-03-03T22:26:12.880+
                                                  //| 01:00, 2013-03-03T23:26:12.880+01:00)),ComplexMAs(List(TestAddress(Hamburg,
                                                  //| Am Hafen), TestAddress(New York,Broadway), TestAddress(Los Angeles ,Sunset 
                                                  //| Boulevard)),Vector(TestAddress(Hamburg,Am Hafen), TestAddress(New York,Broa
                                                  //| dway), TestAddress(Los Angeles ,Sunset Boulevard)),Set(TestAddress(Hamburg,
                                                  //| Am Hafen), TestAddress(New York,Broadway), TestAddress(Los Angeles ,Sunset 
                                                  //| Boulevard)),List(true, hello, 1, 2, 3.0, 3.0, TestAddress(Somewhere,here)))
                                                  //| ,PrimitiveMaps(Map(1 -> 10, 2 -> 20, 3 -> 30, 4 -> 40),Map(a -> 1, b -> 2, 
                                                  //| c -> 3),Map(8e671be4-2b2d-4e39-8eed-14e5bf02b793 -> 2013-03-03T19:26:12.886
                                                  //| +01:00, 947ea48d-fbbb-4ac9-9d94-cd415a3c0cd8 -> 2013-03-04T19:26:12.886+01:
                                                  //| 00, 046cd3b9-28d9-492f-8e30-16823ea9b8b2 -> 2013-03-05T19:26:12.886+01:00))
                                                  //| ,ComplexMaps(Map(0 -> TestAddress(Hamburg,Am Hafen), 1 -> TestAddress(New Y
                                                  //| ork,Broadway), 2 -> TestAddress(Los Angeles ,Sunset Boulevard)),Map(0 -> Te
                                                  //| stAddress(Hamburg,Am Hafen), 1 -> TestAddress(New York,Broadway), 2 -> Test
                                                  //| Address(Los Angeles ,Sunset Boulevard)),Map(x -> bec1bd4d-2801-4e58-bb30-fd
                                                  //| 6d038fa40d, unspecifiedProblem -> almhirt.common.UnspecifiedProblem
                                                  //| Test
                                                  //| Category: SystemProblem
                                                  //| Severity: Major
                                                  //| Arguments: Map(arg1 -> 95)
                                                  //| , y -> dfb7d77e-56a3-4f74-b064-44307efec863, 1 -> TestAddress(New York,Broa
                                                  //| dway), 0 -> TestAddress(Hamburg,Am Hafen), 2 -> TestAddress(Los Angeles ,Su
                                                  //| nset Boulevard), unknownType -> UnknownObject(1), z -> 2013-03-03T19:26:12.
                                                  //| 894+01:00)),Some(TestAddress(Berlin,At the wall 89)))
 
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
                                                  //| cess(DimensionString({"riftdesc":{"identifier":"RiftBlobRefByName","version
                                                  //| ":null},"name":"5114b2e2-7c37-4b82-ab5b-82d585e7aba5","primitiveTypes":{"ri
                                                  //| ftdesc":{"identifier":"riftwarp.PrimitiveTypes","version":null},"str":"I am
                                                  //|  Pete","bool":true,"byte":127,"int":-237823,"long":-278234263,"bigInt":"265
                                                  //| 876257682376587365863876528756875682765252520577305007209857025728132213242
                                                  //| ","float":1.3674999475479126,"double":1.3672322350005,"bigDec":"23761247614
                                                  //| 876823746.23846749182408","dateTime":"2013-03-03T19:26:12.832+01:00","uuid"
                                                  //| :"c3aa49ff-1336-44f3-9edb-9f61ae5450e2"},"primitiveListMAs":{"riftdesc":{"i
                                                  //| dentifier":"riftwarp.PrimitiveListMAs","version":null},"listString":["alpha
                                                  //| ","beta","gamma","delta"],"listInt":[1,2,3,4,5,6,7,8,9,10],"listDouble":[1.
                                                  //| 0,0.5,0.2,0.125],"listBigDecimal":["1.333333","1.33333335","1.6666666","1.6
                                                  //| 666667"],"listDateTime":["2013-03-03T20:26:12.878+01:00","2013-03-03T21:26:
                                                  //| 12.878+01:00","2013-03-03T22:26:12.878+01:00","2013-03-03T23:26:12.878+01:0
                                                  //| 0"]},"primitiveVectorMAs":{"riftdesc":{"identifier":"riftwarp.PrimitiveVect
                                                  //| orMAs","version":null},"vectorString":["alpha","beta","gamma","delta"],"vec
                                                  //| torInt":[1,2,3,4,5,6,7,8,9,10],"vectorDouble":[1.0,0.5,0.2,0.125],"vectorBi
                                                  //| gDecimal":["1.333333","1.33333335","1.6666666","1.6666667"],"vectorDateTime
                                                  //| ":["2013-03-03T20:26:12.879+01:00","2013-03-03T21:26:12.879+01:00","2013-03
                                                  //| -03T22:26:12.879+01:00","2013-03-03T23:26:12.879+01:00"]},"primitiveSetMAs"
                                                  //| :{"riftdesc":{"identifier":"riftwarp.PrimitiveSetMAs","version":null},"setS
                                                  //| tring":["alpha","beta","gamma","delta"],"setInt":[5,10,1,6,9,2,7,3,8,4],"se
                                                  //| tDouble":[1.0,0.5,0.2,0.125],"setBigDecimal":["1.333333","1.33333335","1.66
                                                  //| 66666","1.6666667"],"setDateTime":null},"primitiveIterableMAs":{"riftdesc":
                                                  //| {"identifier":"riftwarp.PrimitiveIterableMAs","version":null},"iterableStri
                                                  //| ng":["alpha","beta","gamma","delta"],"iterableInt":[1,2,3,4,5,6,7,8,9,10],"
                                                  //| iterableDouble":[1.0,0.5,0.2,0.125],"iterableBigDecimal":["1.333333","1.333
                                                  //| 33335","1.6666666","1.6666667"],"iterableDateTime":["2013-03-03T20:26:12.88
                                                  //| 0+01:00","2013-03-03T21:26:12.880+01:00","2013-03-03T22:26:12.880+01:00","2
                                                  //| 013-03-03T23:26:12.880+01:00"]},"complexMAs":{"riftdesc":{"identifier":"rif
                                                  //| twarp.ComplexMAs","version":null},"addresses1":[{"riftdesc":{"identifier":"
                                                  //| riftwarp.TestAddress","version":null},"city":"Hamburg","street":"Am Hafen"}
                                                  //| ,{"riftdesc":{"identifier":"riftwarp.TestAddress","version":null},"city":"N
                                                  //| ew York","street":"Broadway"},{"riftdesc":{"identifier":"riftwarp.TestAddre
                                                  //| ss","version":null},"city":"Los Angeles ","street":"Sunset Boulevard"}],"ad
                                                  //| dresses2":[{"riftdesc":{"identifier":"riftwarp.TestAddress","version":null}
                                                  //| ,"city":"Hamburg","street":"Am Hafen"},{"riftdesc":{"identifier":"riftwarp.
                                                  //| TestAddress","version":null},"city":"New York","street":"Broadway"},{"riftd
                                                  //| esc":{"identifier":"riftwarp.TestAddress","version":null},"city":"Los Angel
                                                  //| es ","street":"Sunset Boulevard"}],"addresses3":[{"riftdesc":{"identifier":
                                                  //| "riftwarp.TestAddress","version":null},"city":"Hamburg","street":"Am Hafen"
                                                  //| },{"riftdesc":{"identifier":"riftwarp.TestAddress","version":null},"city":"
                                                  //| New York","street":"Broadway"},{"riftdesc":{"identifier":"riftwarp.TestAddr
                                                  //| ess","version":null},"city":"Los Angeles ","street":"Sunset Boulevard"}],"a
                                                  //| nything":[true,"hello",1,2,3.0,3.0,{"riftdesc":{"identifier":"riftwarp.Test
                                                  //| Address","version":null},"city":"Somewhere","street":"here"}]},"primitiveMa
                                                  //| ps":{"riftdesc":{"identifier":"riftwarp.PrimitiveMaps","version":null},"map
                                                  //| IntInt":[[1,10],[2,20],[3,30],[4,40]],"mapStringInt":[["a",1],["b",2],["c",
                                                  //| 3]],"mapUuidDateTime":[["8e671be4-2b2d-4e39-8eed-14e5bf02b793","2013-03-03T
                                                  //| 19:26:12.886+01:00"],["947ea48d-fbbb-4ac9-9d94-cd415a3c0cd8","2013-03-04T19
                                                  //| :26:12.886+01:00"],["046cd3b9-28d9-492f-8e30-16823ea9b8b2","2013-03-05T19:2
                                                  //| 6:12.886+01:00"]]},"complexMaps":{"riftdesc":{"identifier":"riftwarp.Comple
                                                  //| xMaps","version":null},"mapIntTestAddress1":[[0,{"riftdesc":{"identifier":"
                                                  //| riftwarp.TestAddress","version":null},"city":"Hamburg","street":"Am Hafen"}
                                                  //| ],[1,{"riftdesc":{"identifier":"riftwarp.TestAddress","version":null},"city
                                                  //| ":"New York","street":"Broadway"}],[2,{"riftdesc":{"identifier":"riftwarp.T
                                                  //| estAddress","version":null},"city":"Los Angeles ","street":"Sunset Boulevar
                                                  //| d"}]],"mapIntAny":[[0,{"riftdesc":{"identifier":"riftwarp.TestAddress","ver
                                                  //| sion":null},"city":"Hamburg","street":"Am Hafen"}],[1,{"riftdesc":{"identif
                                                  //| ier":"riftwarp.TestAddress","version":null},"city":"New York","street":"Bro
                                                  //| adway"}],[2,{"riftdesc":{"identifier":"riftwarp.TestAddress","version":null
                                                  //| },"city":"Los Angeles ","street":"Sunset Boulevard"}]],"mapStringAnyWithUnk
                                                  //| nown":[["x","bec1bd4d-2801-4e58-bb30-fd6d038fa40d"],["unspecifiedProblem",{
                                                  //| "riftdesc":{"identifier":"almhirt.common.UnspecifiedProblem","version":null
                                                  //| },"message":"Test","severity":"Major","category":"SystemProblem","args":[["
                                                  //| arg1",95]],"cause":null}],["y","dfb7d77e-56a3-4f74-b064-44307efec863"],["1"
                                                  //| ,{"riftdesc":{"identifier":"riftwarp.TestAddress","version":null},"city":"N
                                                  //| ew York","street":"Broadway"}],["0",{"riftdesc":{"identifier":"riftwarp.Tes
                                                  //| tAddress","version":null},"city":"Hamburg","street":"Am Hafen"}],["2",{"rif
                                                  //| tdesc":{"identifier":"riftwarp.TestAddress","version":null},"city":"Los Ang
                                                  //| eles ","street":"Sunset Boulevard"}],["z","2013-03-03T19:26:12.894+01:00"]]
                                                  //| },"addressOpt":{"riftdesc":{"identifier":"riftwarp.TestAddress","version":n
                                                  //| ull},"city":"Berlin","street":"At the wall 89"}}))
       
                                       
  val backFromWarpV = riftWarp.receiveFromWarpWithBlobs[DimensionString, TestObjectA](blobFetch)(RiftJson())(warpStreamV.forceResult)
                                                  //> backFromWarpV  : almhirt.common.AlmValidation[riftwarp.TestObjectA] = Failu
                                                  //| re(almhirt.common.UnspecifiedProblem
                                                  //| No recomposer found for RiftDescriptor 'RiftDescriptor(RiftBlobRefByName;no
                                                  //|  version)')
                                                  //| Category: SystemProblem
                                                  //| Severity: Major
                                                  //| Arguments: Map()
                                                  //| )
 
  val backFromWarp = backFromWarpV.forceResult    //> almhirt.common.ResultForcedFromValidationException: A value has been forced
                                                  //|  from a failure: No recomposer found for RiftDescriptor 'RiftDescriptor(Rif
                                                  //| tBlobRefByName;no version)')
                                                  //| 	at almhirt.almvalidation.AlmValidationOps5$$anonfun$forceResult$1.apply(
                                                  //| AlmValidationOps.scala:125)
                                                  //| 	at almhirt.almvalidation.AlmValidationOps5$$anonfun$forceResult$1.apply(
                                                  //| AlmValidationOps.scala:125)
                                                  //| 	at scalaz.Validation$class.fold(Validation.scala:64)
                                                  //| 	at scalaz.Failure.fold(Validation.scala:316)
                                                  //| 	at almhirt.almvalidation.AlmValidationOps5$class.forceResult(AlmValidati
                                                  //| onOps.scala:125)
                                                  //| 	at almhirt.almvalidation.ToAlmValidationOps$$anon$6.forceResult(AlmValid
                                                  //| ationOps.scala:207)
                                                  //| 	at riftwarp.worksheets.Worksheet$$anonfun$main$1.apply$mcV$sp(riftwarp.w
                                                  //| orksheets.Worksheet.scala:53)
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