package riftwarp.worksheets

import scalaz.syntax.validation._
import almhirt.syntax.almvalidation._
import riftwarp._
import riftwarp.impl._

object Worksheet {
  val riftWarp = RiftWarp.unsafeWithDefaults      //> riftWarp  : riftwarp.RiftWarp = riftwarp.RiftWarp$$anon$1@5bbf3d87
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

  val testObject = TestObjectA.pete               //> testObject  : riftwarp.TestObjectA = TestObjectA([B@5fe0f2f6,[B@296f25a7,Pr
                                                  //| imitiveTypes(I am Pete,true,127,-237823,-278234263,265876257682376587365863
                                                  //| 876528756875682765252520577305007209857025728132213242,1.3672322,1.36723223
                                                  //| 50005,23761247614876823746.23846749182408,2012-12-10T20:47:07.019+01:00,baa
                                                  //| 00c3a-feb3-4e7c-9793-e7e6593e0d93),PrimitiveListMAs(List(alpha, beta, gamma
                                                  //| , delta),List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),List(1.0, 0.5, 0.2, 0.125),Lis
                                                  //| t(1.333333, 1.33333335, 1.6666666, 1.6666667),List(2012-12-10T21:47:07.064+
                                                  //| 01:00, 2012-12-10T22:47:07.064+01:00, 2012-12-10T23:47:07.064+01:00, 2012-1
                                                  //| 2-11T00:47:07.064+01:00)),PrimitiveVectorMAs(Vector(alpha, beta, gamma, del
                                                  //| ta),Vector(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),Vector(1.0, 0.5, 0.2, 0.125),Vect
                                                  //| or(1.333333, 1.33333335, 1.6666666, 1.6666667),Vector(2012-12-10T21:47:07.0
                                                  //| 65+01:00, 2012-12-10T22:47:07.065+01:00, 2012-12-10T23:47:07.065+01:00, 201
                                                  //| 2-12-11T00:47:07.065+01:00)),Some(PrimitiveSetMAs(Set(alpha, beta, gamma, d
                                                  //| elta),Set(5, 10, 1, 6, 9, 2, 7, 3, 8, 4),Set(1.0, 0.5, 0.2, 0.125),Set(1.33
                                                  //| 3333, 1.33333335, 1.6666666, 1.6666667),None)),PrimitiveIterableMAs(List(al
                                                  //| pha, beta, gamma, delta),List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),List(1.0, 0.5,
                                                  //|  0.2, 0.125),List(1.333333, 1.33333335, 1.6666666, 1.6666667),List(2012-12-
                                                  //| 10T21:47:07.082+01:00, 2012-12-10T22:47:07.082+01:00, 2012-12-10T23:47:07.0
                                                  //| 82+01:00, 2012-12-11T00:47:07.082+01:00)),ComplexMAs(List(TestAddress(Hambu
                                                  //| rg,Am Hafen), TestAddress(New York,Broadway), TestAddress(Los Angeles ,Suns
                                                  //| et Boulevard)),Vector(TestAddress(Hamburg,Am Hafen), TestAddress(New York,B
                                                  //| roadway), TestAddress(Los Angeles ,Sunset Boulevard)),Set(TestAddress(Hambu
                                                  //| rg,Am Hafen), TestAddress(New York,Broadway), TestAddress(Los Angeles ,Suns
                                                  //| et Boulevard)),List(true, hello, 1, 2, 3.0, 3.0, TestAddress(Somewhere,here
                                                  //| ))),Some(TestAddress(Berlin,At the wall 89)))
 
 
 	var blobdata = new scala.collection.mutable.HashMap[String, Array[Byte]]
                                                  //> blobdata  : scala.collection.mutable.HashMap[String,Array[Byte]] = Map()
	
	val blobDivert: BlobDivert = (arr, path) => {
	  val name = java.util.UUID.randomUUID().toString
		blobdata += (name -> arr)
		RiftBlobRefByName(name).success
	}                                         //> blobDivert  : (Array[Byte], List[String]) => almhirt.common.package.AlmVali
                                                  //| dation[riftwarp.RiftBlob] = <function2>
	
	val blobFetch: BlobFetch = blob =>
	  blob match {
	    case RiftBlobRefByName(name) => blobdata(name).success
	  }                                       //> blobFetch  : riftwarp.RiftBlob => almhirt.common.package.AlmValidation[Arra
                                                  //| y[Byte]] = <function1>

 
  val warpStreamV = riftWarp.prepareForWarpWithBlobs[DimensionCord](blobDivert)(RiftJson())(testObject)
                                                  //> warpStreamV  : almhirt.common.package.AlmValidation[riftwarp.DimensionCord]
                                                  //|  = Success(DimensionCord({"riftwarptd":"riftwarp.TestObjectA","arrayByte":[
                                                  //| 126,-123,12,-45,-128],"blob":{"riftwarptd":"RiftBlobRefByName","name":"864e
                                                  //| 8aac-82d7-4c88-b878-eff9d7a33a8b"},"primitiveTypes":{"riftwarptd":"riftwarp
                                                  //| .PrimitiveTypes","str":"I am Pete","bool":true,"byte":127,"int":-237823,"lo
                                                  //| ng":-278234263,"bigInt":"26587625768237658736586387652875687568276525252057
                                                  //| 7305007209857025728132213242","float":1.3672322034835815,"double":1.3672322
                                                  //| 350005,"bigDec":"23761247614876823746.23846749182408","dateTime":"2012-12-1
                                                  //| 0T20:47:07.019+01:00","uuid":"baa00c3a-feb3-4e7c-9793-e7e6593e0d93"},"primi
                                                  //| tiveListMAs":{"riftwarptd":"riftwarp.PrimitiveListMAs","listString":["alpha
                                                  //| ","beta","gamma","delta"],"listInt":[1,2,3,4,5,6,7,8,9,10],"listDouble":[1.
                                                  //| 0,0.5,0.2,0.125],"listBigDecimal":["1.333333","1.33333335","1.6666666","1.6
                                                  //| 666667"],"listDateTime":["2012-12-10T21:47:07.064+01:00","2012-12-10T22:47:
                                                  //| 07.064+01:00","2012-12-10T23:47:07.064+01:00","2012-12-11T00:47:07.064+01:0
                                                  //| 0"]},"primitiveVectorMAs":{"riftwarptd":"riftwarp.PrimitiveVectorMAs","vect
                                                  //| orString":["alpha","beta","gamma","delta"],"vectorInt":[1,2,3,4,5,6,7,8,9,1
                                                  //| 0],"vectorDouble":[1.0,0.5,0.2,0.125],"vectorBigDecimal":["1.333333","1.333
                                                  //| 33335","1.6666666","1.6666667"],"vectorDateTime":["2012-12-10T21:47:07.065+
                                                  //| 01:00","2012-12-10T22:47:07.065+01:00","2012-12-10T23:47:07.065+01:00","201
                                                  //| 2-12-11T00:47:07.065+01:00"]},"primitiveSetMAs":{"riftwarptd":"riftwarp.Pri
                                                  //| mitiveSetMAs","setString":["alpha","beta","gamma","delta"],"setInt":[7,10,5
                                                  //| ,2,6,1,4,3,8,9],"setDouble":[1.0,0.5,0.2,0.125],"setBigDecimal":["1.333333"
                                                  //| ,"1.33333335","1.6666666","1.6666667"],"setDateTime":null},"primitiveIterab
                                                  //| leMAs":{"riftwarptd":"riftwarp.PrimitiveIterableMAs","iterableString":["alp
                                                  //| ha","beta","gamma","delta"],"iterableInt":[1,2,3,4,5,6,7,8,9,10],"iterableD
                                                  //| ouble":[1.0,0.5,0.2,0.125],"iterableBigDecimal":["1.333333","1.33333335","1
                                                  //| .6666666","1.6666667"],"iterableDateTime":["2012-12-10T21:47:07.082+01:00",
                                                  //| "2012-12-10T22:47:07.082+01:00","2012-12-10T23:47:07.082+01:00","2012-12-11
                                                  //| T00:47:07.082+01:00"]},"complexMAs":{"riftwarptd":"riftwarp.ComplexMAs","ad
                                                  //| dresses1":[{"riftwarptd":"riftwarp.TestAddress","city":"Hamburg","street":"
                                                  //| Am Hafen"},{"riftwarptd":"riftwarp.TestAddress","city":"New York","street":
                                                  //| "Broadway"},{"riftwarptd":"riftwarp.TestAddress","city":"Los Angeles ","str
                                                  //| eet":"Sunset Boulevard"}],"addresses2":[{"riftwarptd":"riftwarp.TestAddress
                                                  //| ","city":"Hamburg","street":"Am Hafen"},{"riftwarptd":"riftwarp.TestAddress
                                                  //| ","city":"New York","street":"Broadway"},{"riftwarptd":"riftwarp.TestAddres
                                                  //| s","city":"Los Angeles ","street":"Sunset Boulevard"}],"addresses3":[{"rift
                                                  //| warptd":"riftwarp.TestAddress","city":"Hamburg","street":"Am Hafen"},{"rift
                                                  //| warptd":"riftwarp.TestAddress","city":"New York","street":"Broadway"},{"rif
                                                  //| twarptd":"riftwarp.TestAddress","city":"Los Angeles ","street":"Sunset Boul
                                                  //| evard"}],"anything":[true,"hello",1,2,3.0,3.0,{"riftwarptd":"riftwarp.TestA
                                                  //| ddress","city":"Somewhere","street":"here"}]},"addressOpt":{"riftwarptd":"r
                                                  //| iftwarp.TestAddress","city":"Berlin","street":"At the wall 89"}}))
 
   
  val backFromWarpV = riftWarp.receiveFromWarpWithBlobs[DimensionCord, TestObjectA](blobFetch)(RiftJson())(warpStreamV.forceResult)
                                                  //> backFromWarpV  : almhirt.common.package.AlmValidation[riftwarp.TestObjectA]
                                                  //|  = Success(TestObjectA([B@25aa0a15,[B@296f25a7,PrimitiveTypes(I am Pete,tru
                                                  //| e,127,-237823,-278234263,26587625768237658736586387652875687568276525252057
                                                  //| 7305007209857025728132213242,1.3672322,1.3672322350005,23761247614876823746
                                                  //| .23846749182408,2012-12-10T20:47:07.019+01:00,baa00c3a-feb3-4e7c-9793-e7e65
                                                  //| 93e0d93),PrimitiveListMAs(List(alpha, beta, gamma, delta),List(1, 2, 3, 4, 
                                                  //| 5, 6, 7, 8, 9, 10),List(1.0, 0.5, 0.2, 0.125),List(1.333333, 1.33333335, 1.
                                                  //| 6666666, 1.6666667),List(2012-12-10T21:47:07.064+01:00, 2012-12-10T22:47:07
                                                  //| .064+01:00, 2012-12-10T23:47:07.064+01:00, 2012-12-11T00:47:07.064+01:00)),
                                                  //| PrimitiveVectorMAs(Vector(alpha, beta, gamma, delta),Vector(1, 2, 3, 4, 5, 
                                                  //| 6, 7, 8, 9, 10),Vector(1.0, 0.5, 0.2, 0.125),Vector(1.333333, 1.33333335, 1
                                                  //| .6666666, 1.6666667),Vector(2012-12-10T21:47:07.065+01:00, 2012-12-10T22:47
                                                  //| :07.065+01:00, 2012-12-10T23:47:07.065+01:00, 2012-12-11T00:47:07.065+01:00
                                                  //| )),Some(PrimitiveSetMAs(Set(alpha, beta, gamma, delta),Set(5, 10, 1, 6, 9, 
                                                  //| 2, 7, 3, 8, 4),Set(1.0, 0.5, 0.2, 0.125),Set(1.333333, 1.33333335, 1.666666
                                                  //| 6, 1.6666667),None)),PrimitiveIterableMAs(Set(alpha, beta, gamma, delta),Se
                                                  //| t(5, 10, 1, 6, 9, 2, 7, 3, 8, 4),Set(1.0, 0.5, 0.2, 0.125),Set(1.333333, 1.
                                                  //| 33333335, 1.6666666, 1.6666667),Set(2012-12-10T21:47:07.082+01:00, 2012-12-
                                                  //| 10T22:47:07.082+01:00, 2012-12-10T23:47:07.082+01:00, 2012-12-11T00:47:07.0
                                                  //| 82+01:00)),ComplexMAs(List(TestAddress(Hamburg,Am Hafen), TestAddress(New Y
                                                  //| ork,Broadway), TestAddress(Los Angeles ,Sunset Boulevard)),Vector(TestAddre
                                                  //| ss(Hamburg,Am Hafen), TestAddress(New York,Broadway), TestAddress(Los Angel
                                                  //| es ,Sunset Boulevard)),Set(TestAddress(Hamburg,Am Hafen), TestAddress(New Y
                                                  //| ork,Broadway), TestAddress(Los Angeles ,Sunset Boulevard)),List(true, hello
                                                  //| , 1.0, 2.0, 3.0, 3.0, Map(riftwarptd -> riftwarp.TestAddress, city -> Somew
                                                  //| here, street -> here))),Some(TestAddress(Berlin,At the wall 89))))
  

  val backFromWarp = backFromWarpV.forceResult    //> backFromWarp  : riftwarp.TestObjectA = TestObjectA([B@25aa0a15,[B@296f25a7,
                                                  //| PrimitiveTypes(I am Pete,true,127,-237823,-278234263,2658762576823765873658
                                                  //| 63876528756875682765252520577305007209857025728132213242,1.3672322,1.367232
                                                  //| 2350005,23761247614876823746.23846749182408,2012-12-10T20:47:07.019+01:00,b
                                                  //| aa00c3a-feb3-4e7c-9793-e7e6593e0d93),PrimitiveListMAs(List(alpha, beta, gam
                                                  //| ma, delta),List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),List(1.0, 0.5, 0.2, 0.125),L
                                                  //| ist(1.333333, 1.33333335, 1.6666666, 1.6666667),List(2012-12-10T21:47:07.06
                                                  //| 4+01:00, 2012-12-10T22:47:07.064+01:00, 2012-12-10T23:47:07.064+01:00, 2012
                                                  //| -12-11T00:47:07.064+01:00)),PrimitiveVectorMAs(Vector(alpha, beta, gamma, d
                                                  //| elta),Vector(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),Vector(1.0, 0.5, 0.2, 0.125),Ve
                                                  //| ctor(1.333333, 1.33333335, 1.6666666, 1.6666667),Vector(2012-12-10T21:47:07
                                                  //| .065+01:00, 2012-12-10T22:47:07.065+01:00, 2012-12-10T23:47:07.065+01:00, 2
                                                  //| 012-12-11T00:47:07.065+01:00)),Some(PrimitiveSetMAs(Set(alpha, beta, gamma,
                                                  //|  delta),Set(5, 10, 1, 6, 9, 2, 7, 3, 8, 4),Set(1.0, 0.5, 0.2, 0.125),Set(1.
                                                  //| 333333, 1.33333335, 1.6666666, 1.6666667),None)),PrimitiveIterableMAs(Set(a
                                                  //| lpha, beta, gamma, delta),Set(5, 10, 1, 6, 9, 2, 7, 3, 8, 4),Set(1.0, 0.5, 
                                                  //| 0.2, 0.125),Set(1.333333, 1.33333335, 1.6666666, 1.6666667),Set(2012-12-10T
                                                  //| 21:47:07.082+01:00, 2012-12-10T22:47:07.082+01:00, 2012-12-10T23:47:07.082+
                                                  //| 01:00, 2012-12-11T00:47:07.082+01:00)),ComplexMAs(List(TestAddress(Hamburg,
                                                  //| Am Hafen), TestAddress(New York,Broadway), TestAddress(Los Angeles ,Sunset 
                                                  //| Boulevard)),Vector(TestAddress(Hamburg,Am Hafen), TestAddress(New York,Broa
                                                  //| dway), TestAddress(Los Angeles ,Sunset Boulevard)),Set(TestAddress(Hamburg,
                                                  //| Am Hafen), TestAddress(New York,Broadway), TestAddress(Los Angeles ,Sunset 
                                                  //| Boulevard)),List(true, hello, 1.0, 2.0, 3.0, 3.0, Map(riftwarptd -> riftwar
                                                  //| p.TestAddress, city -> Somewhere, street -> here))),Some(TestAddress(Berlin
                                                  //| ,At the wall 89)))

  testObject == backFromWarp                      //> res0: Boolean = false


  riftWarp.prepareForWarp[DimensionRawMap](RiftMap())(testObject).bind(warpStream =>
    riftWarp.receiveFromWarp[DimensionRawMap, TestObjectA](RiftMap())(warpStream)).map(rearrived =>
    rearrived == testObject)                      //> res1: scalaz.Validation[almhirt.common.Problem,Boolean] = Success(true)

  riftWarp.prepareForWarp[DimensionCord](RiftJson())(testObject).bind(warpStream =>
    riftWarp.receiveFromWarp[DimensionCord, TestObjectA](RiftJson())(warpStream)).map(rearrived =>
    rearrived == testObject)                      //> res2: scalaz.Validation[almhirt.common.Problem,Boolean] = Success(false)

}