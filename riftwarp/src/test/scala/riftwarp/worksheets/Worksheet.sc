package riftwarp.worksheets

import scalaz.syntax.validation._
import almhirt.syntax.almvalidation._
import riftwarp._
import riftwarp.impl._

object Worksheet {
  val riftWarp = RiftWarp.concurrentWithDefaults  //> riftWarp  : riftwarp.RiftWarp = riftwarp.RiftWarp$$anon$1@a40e6e2
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


  val testObject = TestObjectA.pete               //> testObject  : riftwarp.TestObjectA = TestObjectA([B@611007c2,[B@636d8356,Pr
                                                  //| imitiveTypes(I am Pete,true,127,-237823,-278234263,265876257682376587365863
                                                  //| 876528756875682765252520577305007209857025728132213242,1.3675,1.36723223500
                                                  //| 05,23761247614876823746.23846749182408,2013-01-31T08:43:33.473+01:00,55e975
                                                  //| 4b-d5e9-4e88-95a2-8fe2c87bbc46),PrimitiveListMAs(List(alpha, beta, gamma, d
                                                  //| elta),List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),List(1.0, 0.5, 0.2, 0.125),List(1
                                                  //| .333333, 1.33333335, 1.6666666, 1.6666667),List(2013-01-31T09:43:33.543+01:
                                                  //| 00, 2013-01-31T10:43:33.543+01:00, 2013-01-31T11:43:33.543+01:00, 2013-01-3
                                                  //| 1T12:43:33.543+01:00)),PrimitiveVectorMAs(Vector(alpha, beta, gamma, delta)
                                                  //| ,Vector(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),Vector(1.0, 0.5, 0.2, 0.125),Vector(
                                                  //| 1.333333, 1.33333335, 1.6666666, 1.6666667),Vector(2013-01-31T09:43:33.543+
                                                  //| 01:00, 2013-01-31T10:43:33.543+01:00, 2013-01-31T11:43:33.543+01:00, 2013-0
                                                  //| 1-31T12:43:33.543+01:00)),Some(PrimitiveSetMAs(Set(alpha, beta, gamma, delt
                                                  //| a),Set(5, 10, 1, 6, 9, 2, 7, 3, 8, 4),Set(1.0, 0.5, 0.2, 0.125),Set(1.33333
                                                  //| 3, 1.33333335, 1.6666666, 1.6666667),None)),PrimitiveIterableMAs(List(alpha
                                                  //| , beta, gamma, delta),List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),List(1.0, 0.5, 0.
                                                  //| 2, 0.125),List(1.333333, 1.33333335, 1.6666666, 1.6666667),List(2013-01-31T
                                                  //| 09:43:33.543+01:00, 2013-01-31T10:43:33.543+01:00, 2013-01-31T11:43:33.543+
                                                  //| 01:00, 2013-01-31T12:43:33.543+01:00)),ComplexMAs(List(TestAddress(Hamburg,
                                                  //| Am Hafen), TestAddress(New York,Broadway), TestAddress(Los Angeles ,Sunset 
                                                  //| Boulevard)),Vector(TestAddress(Hamburg,Am Hafen), TestAddress(New York,Broa
                                                  //| dway), TestAddress(Los Angeles ,Sunset Boulevard)),Set(TestAddress(Hamburg,
                                                  //| Am Hafen), TestAddress(New York,Broadway), TestAddress(Los Angeles ,Sunset 
                                                  //| Boulevard)),List(true, hello, 1, 2, 3.0, 3.0, TestAddress(Somewhere,here)))
                                                  //| ,PrimitiveMaps(Map(1 -> 10, 2 -> 20, 3 -> 30, 4 -> 40),Map(a -> 1, b -> 2, 
                                                  //| c -> 3),Map(8fde5495-e307-40b5-8bbc-e3903972d912 -> 2013-01-31T08:43:33.553
                                                  //| +01:00, a957279f-7e20-494a-9413-e2528c72e619 -> 2013-02-01T08:43:33.553+01:
                                                  //| 00, b50a5528-e0fc-4a85-b502-4d6e120a2a24 -> 2013-02-02T08:43:33.553+01:00))
                                                  //| ,ComplexMaps(Map(0 -> TestAddress(Hamburg,Am Hafen), 1 -> TestAddress(New Y
                                                  //| ork,Broadway), 2 -> TestAddress(Los Angeles ,Sunset Boulevard)),Map(0 -> Te
                                                  //| stAddress(Hamburg,Am Hafen), 1 -> TestAddress(New York,Broadway), 2 -> Test
                                                  //| Address(Los Angeles ,Sunset Boulevard)),Map(x -> bfb4673d-b8d6-474a-b295-15
                                                  //| b00e1dfbe1, unspecifiedProblem -> almhirt.common.UnspecifiedProblem
                                                  //| Test
                                                  //| Category: SystemProblem
                                                  //| Severity: Major
                                                  //| Arguments: Map(arg1 -> 95)
                                                  //| , y -> 240ff3f8-676f-4742-b8bf-2717ec394cb7, 1 -> TestAddress(New York,Broa
                                                  //| dway), 0 -> TestAddress(Hamburg,Am Hafen), 2 -> TestAddress(Los Angeles ,Su
                                                  //| nset Boulevard), unknownType -> UnknownObject(1), z -> 2013-01-31T08:43:33.
                                                  //| 563+01:00)),Some(TestAddress(Berlin,At the wall 89)))
 
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
                                                  //| cess(DimensionString({"riftdesc":"riftwarp.TestObjectA","arrayByte":[126,-1
                                                  //| 23,12,-45,-128],"blob":{"riftdesc":"RiftBlobRefByName","name":"f9d5fa08-d65
                                                  //| 7-4daf-94dc-0b4859ebf416"},"primitiveTypes":{"riftdesc":"riftwarp.Primitive
                                                  //| Types","str":"I am Pete","bool":true,"byte":127,"int":-237823,"long":-27823
                                                  //| 4263,"bigInt":"265876257682376587365863876528756875682765252520577305007209
                                                  //| 857025728132213242","float":1.3674999475479126,"double":1.3672322350005,"bi
                                                  //| gDec":"23761247614876823746.23846749182408","dateTime":"2013-01-31T08:43:33
                                                  //| .473+01:00","uuid":"55e9754b-d5e9-4e88-95a2-8fe2c87bbc46"},"primitiveListMA
                                                  //| s":{"riftdesc":"riftwarp.PrimitiveListMAs","listString":["alpha","beta","ga
                                                  //| mma","delta"],"listInt":[1,2,3,4,5,6,7,8,9,10],"listDouble":[1.0,0.5,0.2,0.
                                                  //| 125],"listBigDecimal":["1.333333","1.33333335","1.6666666","1.6666667"],"li
                                                  //| stDateTime":["2013-01-31T09:43:33.543+01:00","2013-01-31T10:43:33.543+01:00
                                                  //| ","2013-01-31T11:43:33.543+01:00","2013-01-31T12:43:33.543+01:00"]},"primit
                                                  //| iveVectorMAs":{"riftdesc":"riftwarp.PrimitiveVectorMAs","vectorString":["al
                                                  //| pha","beta","gamma","delta"],"vectorInt":[1,2,3,4,5,6,7,8,9,10],"vectorDoub
                                                  //| le":[1.0,0.5,0.2,0.125],"vectorBigDecimal":["1.333333","1.33333335","1.6666
                                                  //| 666","1.6666667"],"vectorDateTime":["2013-01-31T09:43:33.543+01:00","2013-0
                                                  //| 1-31T10:43:33.543+01:00","2013-01-31T11:43:33.543+01:00","2013-01-31T12:43:
                                                  //| 33.543+01:00"]},"primitiveSetMAs":{"riftdesc":"riftwarp.PrimitiveSetMAs","s
                                                  //| etString":["alpha","beta","gamma","delta"],"setInt":[1,5,2,7,9,6,4,8,10,3],
                                                  //| "setDouble":[1.0,0.5,0.2,0.125],"setBigDecimal":["1.333333","1.33333335","1
                                                  //| .6666666","1.6666667"],"setDateTime":null},"primitiveIterableMAs":{"riftdes
                                                  //| c":"riftwarp.PrimitiveIterableMAs","iterableString":["alpha","beta","gamma"
                                                  //| ,"delta"],"iterableInt":[1,2,3,4,5,6,7,8,9,10],"iterableDouble":[1.0,0.5,0.
                                                  //| 2,0.125],"iterableBigDecimal":["1.333333","1.33333335","1.6666666","1.66666
                                                  //| 67"],"iterableDateTime":["2013-01-31T09:43:33.543+01:00","2013-01-31T10:43:
                                                  //| 33.543+01:00","2013-01-31T11:43:33.543+01:00","2013-01-31T12:43:33.543+01:0
                                                  //| 0"]},"complexMAs":{"riftdesc":"riftwarp.ComplexMAs","addresses1":[{"riftdes
                                                  //| c":"riftwarp.TestAddress","city":"Hamburg","street":"Am Hafen"},{"riftdesc"
                                                  //| :"riftwarp.TestAddress","city":"New York","street":"Broadway"},{"riftdesc":
                                                  //| "riftwarp.TestAddress","city":"Los Angeles ","street":"Sunset Boulevard"}],
                                                  //| "addresses2":[{"riftdesc":"riftwarp.TestAddress","city":"Hamburg","street":
                                                  //| "Am Hafen"},{"riftdesc":"riftwarp.TestAddress","city":"New York","street":"
                                                  //| Broadway"},{"riftdesc":"riftwarp.TestAddress","city":"Los Angeles ","street
                                                  //| ":"Sunset Boulevard"}],"addresses3":[{"riftdesc":"riftwarp.TestAddress","ci
                                                  //| ty":"Hamburg","street":"Am Hafen"},{"riftdesc":"riftwarp.TestAddress","city
                                                  //| ":"New York","street":"Broadway"},{"riftdesc":"riftwarp.TestAddress","city"
                                                  //| :"Los Angeles ","street":"Sunset Boulevard"}],"anything":[true,"hello",1,2,
                                                  //| 3.0,3.0,{"riftdesc":"riftwarp.TestAddress","city":"Somewhere","street":"her
                                                  //| e"}]},"primitiveMaps":{"riftdesc":"riftwarp.PrimitiveMaps","mapIntInt":[{"k
                                                  //| ":1,"v":10},{"k":2,"v":20},{"k":3,"v":30},{"k":4,"v":40}],"mapStringInt":[{
                                                  //| "k":"a","v":1},{"k":"b","v":2},{"k":"c","v":3}],"mapUuidDateTime":[{"k":"8f
                                                  //| de5495-e307-40b5-8bbc-e3903972d912","v":"2013-01-31T08:43:33.553+01:00"},{"
                                                  //| k":"a957279f-7e20-494a-9413-e2528c72e619","v":"2013-02-01T08:43:33.553+01:0
                                                  //| 0"},{"k":"b50a5528-e0fc-4a85-b502-4d6e120a2a24","v":"2013-02-02T08:43:33.55
                                                  //| 3+01:00"}]},"complexMaps":{"riftdesc":"riftwarp.ComplexMaps","mapIntTestAdd
                                                  //| ress1":[{"k":0,"v":{"riftdesc":"riftwarp.TestAddress","city":"Hamburg","str
                                                  //| eet":"Am Hafen"}},{"k":1,"v":{"riftdesc":"riftwarp.TestAddress","city":"New
                                                  //|  York","street":"Broadway"}},{"k":2,"v":{"riftdesc":"riftwarp.TestAddress",
                                                  //| "city":"Los Angeles ","street":"Sunset Boulevard"}}],"mapIntAny":[{"k":0,"v
                                                  //| ":{"riftdesc":"riftwarp.TestAddress","city":"Hamburg","street":"Am Hafen"}}
                                                  //| ,{"k":1,"v":{"riftdesc":"riftwarp.TestAddress","city":"New York","street":"
                                                  //| Broadway"}},{"k":2,"v":{"riftdesc":"riftwarp.TestAddress","city":"Los Angel
                                                  //| es ","street":"Sunset Boulevard"}}],"mapStringAnyWithUnknown":[{"k":"x","v"
                                                  //| :"bfb4673d-b8d6-474a-b295-15b00e1dfbe1"},{"k":"unspecifiedProblem","v":{"ri
                                                  //| ftdesc":"almhirt.common.UnspecifiedProblem","message":"Test","severity":"Ma
                                                  //| jor","category":"SystemProblem","args":[{"k":"arg1","v":95}]}},{"k":"y","v"
                                                  //| :"240ff3f8-676f-4742-b8bf-2717ec394cb7"},{"k":"1","v":{"riftdesc":"riftwarp
                                                  //| .TestAddress","city":"New York","street":"Broadway"}},{"k":"0","v":{"riftde
                                                  //| sc":"riftwarp.TestAddress","city":"Hamburg","street":"Am Hafen"}},{"k":"2",
                                                  //| "v":{"riftdesc":"riftwarp.TestAddress","city":"Los Angeles ","street":"Suns
                                                  //| et Boulevard"}},{"k":"z","v":"2013-01-31T08:43:33.563+01:00"}]},"addressOpt
                                                  //| ":{"riftdesc":"riftwarp.TestAddress","city":"Berlin","street":"At the wall 
                                                  //| 89"}}))
 
      
  val backFromWarpV = riftWarp.receiveFromWarpWithBlobs[DimensionString, TestObjectA](blobFetch)(RiftJson())(warpStreamV.forceResult)
                                                  //> backFromWarpV  : almhirt.common.AlmValidation[riftwarp.TestObjectA] = Failu
                                                  //| re(almhirt.common.AggregateProblem
                                                  //| One or more problems
                                                  //| Category: SystemProblem
                                                  //| Severity: Major
                                                  //| Arguments: Map()
                                                  //| Aggregated problems:
                                                  //| Problem 0:
                                                  //| almhirt.common.UnspecifiedProblem
                                                  //| No converter found for M[_](scala.collection.immutable.List[_]) to N[_](jav
                                                  //| a.lang.Object[_])
                                                  //| Category: SystemProblem
                                                  //| Severity: Major
                                                  //| Arguments: Map()
                                                  //| Problem 1:
                                                  //| almhirt.common.UnspecifiedProblem
                                                  //| No converter found for M[_](scala.collection.immutable.List[_]) to N[_](jav
                                                  //| a.lang.Object[_])
                                                  //| Category: SystemProblem
                                                  //| Severity: Major
                                                  //| Arguments: Map()
                                                  //| Problem 2:
                                                  //| almhirt.common.UnspecifiedProblem
                                                  //| No converter found for M[_](scala.collection.immutable.List[_]) to N[_](jav
                                                  //| a.lang.Object[_])
                                                  //| Category: SystemProblem
                                                  //| Severity: Major
                                                  //| Arguments: Map()
                                                  //| Problem 3:
                                                  //| almhirt.common.UnspecifiedProblem
                                                  //| No converter found for M[_](scala.collection.immutable.List[_]) to N[_](jav
                                                  //| a.lang.Object[_])
                                                  //| Category: SystemProblem
                                                  //| Severity: Major
                                                  //| Arguments: Map()
                                                  //| Problem 4:
                                                  //| almhirt.common.UnspecifiedProblem
                                                  //| No converter found for M[_](scala.collection.immutable.List[_]) to N[_](jav
                                                  //| a.lang.Object[_])
                                                  //| Category: SystemProblem
                                                  //| Severity: Major
                                                  //| Arguments: Map()
                                                  //| )
 
  
  val backFromWarp = backFromWarpV.forceResult    //> almhirt.common.ResultForcedFromValidationException: A value has been forced
                                                  //|  from a failure: One or more problems
                                                  //| 	at almhirt.almvalidation.AlmValidationOps5$$anonfun$forceResult$1.apply(
                                                  //| AlmValidationOps.scala:118)
                                                  //| 	at almhirt.almvalidation.AlmValidationOps5$$anonfun$forceResult$1.apply(
                                                  //| AlmValidationOps.scala:118)
                                                  //| 	at scalaz.Validation$class.fold(Validation.scala:64)
                                                  //| 	at scalaz.Failure.fold(Validation.scala:316)
                                                  //| 	at almhirt.almvalidation.AlmValidationOps5$class.forceResult(AlmValidati
                                                  //| onOps.scala:118)
                                                  //| 	at almhirt.almvalidation.ToAlmValidationOps$$anon$6.forceResult(AlmValid
                                                  //| ationOps.scala:195)
                                                  //| 	at riftwarp.worksheets.Worksheet$$anonfun$main$1.apply$mcV$sp(riftwarp.w
                                                  //| orksheets.Worksheet.scala:55)
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


  riftWarp.prepareForWarp[DimensionRawMap](RiftMap())(testObject).flatMap(warpStream =>
    riftWarp.receiveFromWarp[DimensionRawMap, TestObjectA](RiftMap())(warpStream)).map(rearrived =>
    rearrived == testObject)


}