package riftwarp.worksheets

import scalaz.syntax.validation._
import almhirt.syntax.almvalidation._
import riftwarp._
import riftwarp.impl._

object Worksheet {
  val riftWarp = RiftWarp.concurrentWithDefaults  //> riftWarp  : riftwarp.RiftWarp = riftwarp.RiftWarp$$anon$1@42af94c4
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

  val testObject = TestObjectA.pete               //> testObject  : riftwarp.TestObjectA = TestObjectA([B@9be2c6f,[B@24573068,Pri
                                                  //| mitiveTypes(I am Pete,true,127,-237823,-278234263,2658762576823765873658638
                                                  //| 76528756875682765252520577305007209857025728132213242,1.3672322,1.367232235
                                                  //| 0005,23761247614876823746.23846749182408,2013-01-09T13:42:12.891+01:00,3e74
                                                  //| 60e4-a36a-47ea-8538-6da4ae7ab149),PrimitiveListMAs(List(alpha, beta, gamma,
                                                  //|  delta),List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),List(1.0, 0.5, 0.2, 0.125),List
                                                  //| (1.333333, 1.33333335, 1.6666666, 1.6666667),List(2013-01-09T14:42:12.969+0
                                                  //| 1:00, 2013-01-09T15:42:12.969+01:00, 2013-01-09T16:42:12.969+01:00, 2013-01
                                                  //| -09T17:42:12.969+01:00)),PrimitiveVectorMAs(Vector(alpha, beta, gamma, delt
                                                  //| a),Vector(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),Vector(1.0, 0.5, 0.2, 0.125),Vecto
                                                  //| r(1.333333, 1.33333335, 1.6666666, 1.6666667),Vector(2013-01-09T14:42:12.96
                                                  //| 9+01:00, 2013-01-09T15:42:12.969+01:00, 2013-01-09T16:42:12.969+01:00, 2013
                                                  //| -01-09T17:42:12.969+01:00)),Some(PrimitiveSetMAs(Set(alpha, beta, gamma, de
                                                  //| lta),Set(5, 10, 1, 6, 9, 2, 7, 3, 8, 4),Set(1.0, 0.5, 0.2, 0.125),Set(1.333
                                                  //| 333, 1.33333335, 1.6666666, 1.6666667),None)),PrimitiveIterableMAs(List(alp
                                                  //| ha, beta, gamma, delta),List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),List(1.0, 0.5, 
                                                  //| 0.2, 0.125),List(1.333333, 1.33333335, 1.6666666, 1.6666667),List(2013-01-0
                                                  //| 9T14:42:12.969+01:00, 2013-01-09T15:42:12.969+01:00, 2013-01-09T16:42:12.96
                                                  //| 9+01:00, 2013-01-09T17:42:12.969+01:00)),ComplexMAs(List(TestAddress(Hambur
                                                  //| g,Am Hafen), TestAddress(New York,Broadway), TestAddress(Los Angeles ,Sunse
                                                  //| t Boulevard)),Vector(TestAddress(Hamburg,Am Hafen), TestAddress(New York,Br
                                                  //| oadway), TestAddress(Los Angeles ,Sunset Boulevard)),Set(TestAddress(Hambur
                                                  //| g,Am Hafen), TestAddress(New York,Broadway), TestAddress(Los Angeles ,Sunse
                                                  //| t Boulevard)),List(true, hello, 1, 2, 3.0, 3.0, TestAddress(Somewhere,here)
                                                  //| )),PrimitiveMaps(Map(1 -> 10, 2 -> 20, 3 -> 30, 4 -> 40),Map(a -> 1, b -> 2
                                                  //| , c -> 3),Map(a0d71495-a24b-4dd0-87cb-084e34799d30 -> 2013-01-09T13:42:12.9
                                                  //| 69+01:00, 1c9ed91f-4202-4686-b920-653ffd0f9166 -> 2013-01-10T13:42:12.969+0
                                                  //| 1:00, aaff589f-6423-4088-a4b8-536b78718727 -> 2013-01-11T13:42:12.969+01:00
                                                  //| )),Some(TestAddress(Berlin,At the wall 89)))
 
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
                                                  //| cess(DimensionString({"riftwarptd":"riftwarp.TestObjectA","arrayByte":[126,
                                                  //| -123,12,-45,-128],"blob":{"riftwarptd":"RiftBlobRefByName","name":"2d1986ba
                                                  //| -b9f4-46d8-9cb2-20b8fdb57fc3"},"primitiveTypes":{"riftwarptd":"riftwarp.Pri
                                                  //| mitiveTypes","str":"I am Pete","bool":true,"byte":127,"int":-237823,"long":
                                                  //| -278234263,"bigInt":"265876257682376587365863876528756875682765252520577305
                                                  //| 007209857025728132213242","float":1.3672322034835815,"double":1.36723223500
                                                  //| 05,"bigDec":"23761247614876823746.23846749182408","dateTime":"2013-01-09T13
                                                  //| :42:12.891+01:00","uuid":"3e7460e4-a36a-47ea-8538-6da4ae7ab149"},"primitive
                                                  //| ListMAs":{"riftwarptd":"riftwarp.PrimitiveListMAs","listString":["alpha","b
                                                  //| eta","gamma","delta"],"listInt":[1,2,3,4,5,6,7,8,9,10],"listDouble":[1.0,0.
                                                  //| 5,0.2,0.125],"listBigDecimal":["1.333333","1.33333335","1.6666666","1.66666
                                                  //| 67"],"listDateTime":["2013-01-09T14:42:12.969+01:00","2013-01-09T15:42:12.9
                                                  //| 69+01:00","2013-01-09T16:42:12.969+01:00","2013-01-09T17:42:12.969+01:00"]}
                                                  //| ,"primitiveVectorMAs":{"riftwarptd":"riftwarp.PrimitiveVectorMAs","vectorSt
                                                  //| ring":["alpha","beta","gamma","delta"],"vectorInt":[1,2,3,4,5,6,7,8,9,10],"
                                                  //| vectorDouble":[1.0,0.5,0.2,0.125],"vectorBigDecimal":["1.333333","1.3333333
                                                  //| 5","1.6666666","1.6666667"],"vectorDateTime":["2013-01-09T14:42:12.969+01:0
                                                  //| 0","2013-01-09T15:42:12.969+01:00","2013-01-09T16:42:12.969+01:00","2013-01
                                                  //| -09T17:42:12.969+01:00"]},"primitiveSetMAs":{"riftwarptd":"riftwarp.Primiti
                                                  //| veSetMAs","setString":["alpha","beta","gamma","delta"],"setInt":[7,9,8,1,3,
                                                  //| 4,5,10,2,6],"setDouble":[1.0,0.5,0.2,0.125],"setBigDecimal":["1.333333","1.
                                                  //| 33333335","1.6666666","1.6666667"],"setDateTime":null},"primitiveIterableMA
                                                  //| s":{"riftwarptd":"riftwarp.PrimitiveIterableMAs","iterableString":["alpha",
                                                  //| "beta","gamma","delta"],"iterableInt":[1,2,3,4,5,6,7,8,9,10],"iterableDoubl
                                                  //| e":[1.0,0.5,0.2,0.125],"iterableBigDecimal":["1.333333","1.33333335","1.666
                                                  //| 6666","1.6666667"],"iterableDateTime":["2013-01-09T14:42:12.969+01:00","201
                                                  //| 3-01-09T15:42:12.969+01:00","2013-01-09T16:42:12.969+01:00","2013-01-09T17:
                                                  //| 42:12.969+01:00"]},"complexMAs":{"riftwarptd":"riftwarp.ComplexMAs","addres
                                                  //| ses1":[{"riftwarptd":"riftwarp.TestAddress","city":"Hamburg","street":"Am H
                                                  //| afen"},{"riftwarptd":"riftwarp.TestAddress","city":"New York","street":"Bro
                                                  //| adway"},{"riftwarptd":"riftwarp.TestAddress","city":"Los Angeles ","street"
                                                  //| :"Sunset Boulevard"}],"addresses2":[{"riftwarptd":"riftwarp.TestAddress","c
                                                  //| ity":"Hamburg","street":"Am Hafen"},{"riftwarptd":"riftwarp.TestAddress","c
                                                  //| ity":"New York","street":"Broadway"},{"riftwarptd":"riftwarp.TestAddress","
                                                  //| city":"Los Angeles ","street":"Sunset Boulevard"}],"addresses3":[{"riftwarp
                                                  //| td":"riftwarp.TestAddress","city":"Hamburg","street":"Am Hafen"},{"riftwarp
                                                  //| td":"riftwarp.TestAddress","city":"New York","street":"Broadway"},{"riftwar
                                                  //| ptd":"riftwarp.TestAddress","city":"Los Angeles ","street":"Sunset Boulevar
                                                  //| d"}],"anything":[true,"hello",1,2,3.0,3.0,{"riftwarptd":"riftwarp.TestAddre
                                                  //| ss","city":"Somewhere","street":"here"}]},"primitiveMaps":{"riftwarptd":"ri
                                                  //| ftwarp.PrimitiveMaps","mapIntInt":[{"k":1,"v":10},{"k":2,"v":20},{"k":3,"v"
                                                  //| :30},{"k":4,"v":40}],"mapStringInt":[{"k":"a","v":1},{"k":"b","v":2},{"k":"
                                                  //| c","v":3}],"mapUuidDateTime":[{"k":"a0d71495-a24b-4dd0-87cb-084e34799d30","
                                                  //| v":"2013-01-09T13:42:12.969+01:00"},{"k":"1c9ed91f-4202-4686-b920-653ffd0f9
                                                  //| 166","v":"2013-01-10T13:42:12.969+01:00"},{"k":"aaff589f-6423-4088-a4b8-536
                                                  //| b78718727","v":"2013-01-11T13:42:12.969+01:00"}]},"addressOpt":{"riftwarptd
                                                  //| ":"riftwarp.TestAddress","city":"Berlin","street":"At the wall 89"}}))
     
   
     
  val backFromWarpV = riftWarp.receiveFromWarpWithBlobs[DimensionString, TestObjectA](blobFetch)(RiftJson())(warpStreamV.forceResult)
                                                  //> backFromWarpV  : almhirt.common.AlmValidation[riftwarp.TestObjectA] = Succe
                                                  //| ss(TestObjectA([B@a746115,[B@24573068,PrimitiveTypes(I am Pete,true,127,-23
                                                  //| 7823,-278234263,26587625768237658736586387652875687568276525252057730500720
                                                  //| 9857025728132213242,1.3672322,1.3672322350005,23761247614876823746.23846749
                                                  //| 182408,2013-01-09T13:42:12.891+01:00,3e7460e4-a36a-47ea-8538-6da4ae7ab149),
                                                  //| PrimitiveListMAs(List(alpha, beta, gamma, delta),List(1, 2, 3, 4, 5, 6, 7, 
                                                  //| 8, 9, 10),List(1.0, 0.5, 0.2, 0.125),List(1.333333, 1.33333335, 1.6666666, 
                                                  //| 1.6666667),List(2013-01-09T14:42:12.969+01:00, 2013-01-09T15:42:12.969+01:0
                                                  //| 0, 2013-01-09T16:42:12.969+01:00, 2013-01-09T17:42:12.969+01:00)),Primitive
                                                  //| VectorMAs(Vector(alpha, beta, gamma, delta),Vector(1, 2, 3, 4, 5, 6, 7, 8, 
                                                  //| 9, 10),Vector(1.0, 0.5, 0.2, 0.125),Vector(1.333333, 1.33333335, 1.6666666,
                                                  //|  1.6666667),Vector(2013-01-09T14:42:12.969+01:00, 2013-01-09T15:42:12.969+0
                                                  //| 1:00, 2013-01-09T16:42:12.969+01:00, 2013-01-09T17:42:12.969+01:00)),Some(P
                                                  //| rimitiveSetMAs(Set(alpha, beta, gamma, delta),Set(5, 10, 1, 6, 9, 2, 7, 3, 
                                                  //| 8, 4),Set(1.0, 0.5, 0.2, 0.125),Set(1.333333, 1.33333335, 1.6666666, 1.6666
                                                  //| 667),None)),PrimitiveIterableMAs(Set(alpha, beta, gamma, delta),Set(5, 10, 
                                                  //| 1, 6, 9, 2, 7, 3, 8, 4),Set(1.0, 0.5, 0.2, 0.125),Set(1.333333, 1.33333335,
                                                  //|  1.6666666, 1.6666667),Set(2013-01-09T14:42:12.969+01:00, 2013-01-09T15:42:
                                                  //| 12.969+01:00, 2013-01-09T16:42:12.969+01:00, 2013-01-09T17:42:12.969+01:00)
                                                  //| ),ComplexMAs(List(TestAddress(Hamburg,Am Hafen), TestAddress(New York,Broad
                                                  //| way), TestAddress(Los Angeles ,Sunset Boulevard)),Vector(TestAddress(Hambur
                                                  //| g,Am Hafen), TestAddress(New York,Broadway), TestAddress(Los Angeles ,Sunse
                                                  //| t Boulevard)),Set(TestAddress(Hamburg,Am Hafen), TestAddress(New York,Broad
                                                  //| way), TestAddress(Los Angeles ,Sunset Boulevard)),List(true, hello, 1.0, 2.
                                                  //| 0, 3.0, 3.0, Map(riftwarptd -> riftwarp.TestAddress, city -> Somewhere, str
                                                  //| eet -> here))),PrimitiveMaps(Map(1 -> 10, 2 -> 20, 3 -> 30, 4 -> 40),Map(a 
                                                  //| -> 1, b -> 2, c -> 3),Map(a0d71495-a24b-4dd0-87cb-084e34799d30 -> 2013-01-0
                                                  //| 9T13:42:12.969+01:00, 1c9ed91f-4202-4686-b920-653ffd0f9166 -> 2013-01-10T13
                                                  //| :42:12.969+01:00, aaff589f-6423-4088-a4b8-536b78718727 -> 2013-01-11T13:42:
                                                  //| 12.969+01:00)),Some(TestAddress(Berlin,At the wall 89))))
  
  val backFromWarp = backFromWarpV.forceResult    //> backFromWarp  : riftwarp.TestObjectA = TestObjectA([B@a746115,[B@24573068,P
                                                  //| rimitiveTypes(I am Pete,true,127,-237823,-278234263,26587625768237658736586
                                                  //| 3876528756875682765252520577305007209857025728132213242,1.3672322,1.3672322
                                                  //| 350005,23761247614876823746.23846749182408,2013-01-09T13:42:12.891+01:00,3e
                                                  //| 7460e4-a36a-47ea-8538-6da4ae7ab149),PrimitiveListMAs(List(alpha, beta, gamm
                                                  //| a, delta),List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),List(1.0, 0.5, 0.2, 0.125),Li
                                                  //| st(1.333333, 1.33333335, 1.6666666, 1.6666667),List(2013-01-09T14:42:12.969
                                                  //| +01:00, 2013-01-09T15:42:12.969+01:00, 2013-01-09T16:42:12.969+01:00, 2013-
                                                  //| 01-09T17:42:12.969+01:00)),PrimitiveVectorMAs(Vector(alpha, beta, gamma, de
                                                  //| lta),Vector(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),Vector(1.0, 0.5, 0.2, 0.125),Vec
                                                  //| tor(1.333333, 1.33333335, 1.6666666, 1.6666667),Vector(2013-01-09T14:42:12.
                                                  //| 969+01:00, 2013-01-09T15:42:12.969+01:00, 2013-01-09T16:42:12.969+01:00, 20
                                                  //| 13-01-09T17:42:12.969+01:00)),Some(PrimitiveSetMAs(Set(alpha, beta, gamma, 
                                                  //| delta),Set(5, 10, 1, 6, 9, 2, 7, 3, 8, 4),Set(1.0, 0.5, 0.2, 0.125),Set(1.3
                                                  //| 33333, 1.33333335, 1.6666666, 1.6666667),None)),PrimitiveIterableMAs(Set(al
                                                  //| pha, beta, gamma, delta),Set(5, 10, 1, 6, 9, 2, 7, 3, 8, 4),Set(1.0, 0.5, 0
                                                  //| .2, 0.125),Set(1.333333, 1.33333335, 1.6666666, 1.6666667),Set(2013-01-09T1
                                                  //| 4:42:12.969+01:00, 2013-01-09T15:42:12.969+01:00, 2013-01-09T16:42:12.969+0
                                                  //| 1:00, 2013-01-09T17:42:12.969+01:00)),ComplexMAs(List(TestAddress(Hamburg,A
                                                  //| m Hafen), TestAddress(New York,Broadway), TestAddress(Los Angeles ,Sunset B
                                                  //| oulevard)),Vector(TestAddress(Hamburg,Am Hafen), TestAddress(New York,Broad
                                                  //| way), TestAddress(Los Angeles ,Sunset Boulevard)),Set(TestAddress(Hamburg,A
                                                  //| m Hafen), TestAddress(New York,Broadway), TestAddress(Los Angeles ,Sunset B
                                                  //| oulevard)),List(true, hello, 1.0, 2.0, 3.0, 3.0, Map(riftwarptd -> riftwarp
                                                  //| .TestAddress, city -> Somewhere, street -> here))),PrimitiveMaps(Map(1 -> 1
                                                  //| 0, 2 -> 20, 3 -> 30, 4 -> 40),Map(a -> 1, b -> 2, c -> 3),Map(a0d71495-a24b
                                                  //| -4dd0-87cb-084e34799d30 -> 2013-01-09T13:42:12.969+01:00, 1c9ed91f-4202-468
                                                  //| 6-b920-653ffd0f9166 -> 2013-01-10T13:42:12.969+01:00, aaff589f-6423-4088-a4
                                                  //| b8-536b78718727 -> 2013-01-11T13:42:12.969+01:00)),Some(TestAddress(Berlin,
                                                  //| At the wall 89)))

  testObject == backFromWarp                      //> res0: Boolean = false




  riftWarp.prepareForWarp[DimensionRawMap](RiftMap())(testObject).flatMap(warpStream =>
    riftWarp.receiveFromWarp[DimensionRawMap, TestObjectA](RiftMap())(warpStream)).map(rearrived =>
    rearrived == testObject)                      //> res1: scalaz.Validation[almhirt.common.Problem,Boolean] = Success(true)

  riftWarp.prepareForWarp[DimensionCord](RiftJson())(testObject).flatMap(warpStream =>
    riftWarp.receiveFromWarp[DimensionCord, TestObjectA](RiftJson())(warpStream)).map(rearrived =>
    rearrived == testObject)                      //> res2: scalaz.Validation[almhirt.common.Problem,Boolean] = Success(false)

}