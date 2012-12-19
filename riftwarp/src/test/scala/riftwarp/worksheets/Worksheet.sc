package riftwarp.worksheets

import scalaz.syntax.validation._
import almhirt.syntax.almvalidation._
import riftwarp._
import riftwarp.impl._

object Worksheet {
  val riftWarp = RiftWarp.concurrentWithDefaults  //> riftWarp  : riftwarp.RiftWarp = riftwarp.RiftWarp$$anon$1@2353f67e
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

  val testObject = TestObjectA.pete               //> testObject  : riftwarp.TestObjectA = TestObjectA([B@5de9ac4,[B@5982bcde,Pri
                                                  //| mitiveTypes(I am Pete,true,127,-237823,-278234263,2658762576823765873658638
                                                  //| 76528756875682765252520577305007209857025728132213242,1.3672322,1.367232235
                                                  //| 0005,23761247614876823746.23846749182408,2012-12-19T21:28:57.257+01:00,8f9c
                                                  //| e1a0-be58-4a7d-91f7-466135be3428),PrimitiveListMAs(List(alpha, beta, gamma,
                                                  //|  delta),List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),List(1.0, 0.5, 0.2, 0.125),List
                                                  //| (1.333333, 1.33333335, 1.6666666, 1.6666667),List(2012-12-19T22:28:57.306+0
                                                  //| 1:00, 2012-12-19T23:28:57.307+01:00, 2012-12-20T00:28:57.307+01:00, 2012-12
                                                  //| -20T01:28:57.307+01:00)),PrimitiveVectorMAs(Vector(alpha, beta, gamma, delt
                                                  //| a),Vector(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),Vector(1.0, 0.5, 0.2, 0.125),Vecto
                                                  //| r(1.333333, 1.33333335, 1.6666666, 1.6666667),Vector(2012-12-19T22:28:57.30
                                                  //| 8+01:00, 2012-12-19T23:28:57.308+01:00, 2012-12-20T00:28:57.308+01:00, 2012
                                                  //| -12-20T01:28:57.308+01:
                                                  //| Output exceeds cutoff limit.
 
 
 	var blobdata = new scala.collection.mutable.HashMap[String, Array[Byte]]
                                                  //> blobdata  : scala.collection.mutable.HashMap[String,Array[Byte]] = Map()
	
	val blobDivert: BlobDivert = (arr, path) => {
	  val name = java.util.UUID.randomUUID().toString
		blobdata += (name -> arr)
		RiftBlobRefByName(name).success
	}                                         //> blobDivert  : (Array[Byte], riftwarp.RiftBlobIdentifier) => almhirt.common.
                                                  //| package.AlmValidation[riftwarp.RiftBlob] = <function2>
	
	val blobFetch: BlobFetch = blob =>
	  blob match {
	    case RiftBlobRefByName(name) => blobdata(name).success
	  }                                       //> blobFetch  : riftwarp.RiftBlob => almhirt.common.package.AlmValidation[Arra
                                                  //| y[Byte]] = <function1>

 
  val warpStreamV = riftWarp.prepareForWarpWithBlobs[DimensionCord](blobDivert)(RiftJson())(testObject)
                                                  //> warpStreamV  : almhirt.common.package.AlmValidation[riftwarp.DimensionCord]
                                                  //|  = Success(DimensionCord({"riftwarptd":"riftwarp.TestObjectA","arrayByte":[
                                                  //| 126,-123,12,-45,-128],"blob":{"riftwarptd":"RiftBlobRefByName","name":"2bc4
                                                  //| 376b-9bfb-4bca-a50c-b17e6e9953e0"},"primitiveTypes":{"riftwarptd":"riftwarp
                                                  //| .PrimitiveTypes","str":"I am Pete","bool":true,"byte":127,"int":-237823,"lo
                                                  //| ng":-278234263,"bigInt":"26587625768237658736586387652875687568276525252057
                                                  //| 7305007209857025728132213242","float":1.3672322034835815,"double":1.3672322
                                                  //| 350005,"bigDec":"23761247614876823746.23846749182408","dateTime":"2012-12-1
                                                  //| 9T21:28:57.257+01:00","uuid":"8f9ce1a0-be58-4a7d-91f7-466135be3428"},"primi
                                                  //| tiveListMAs":{"riftwarptd":"riftwarp.PrimitiveListMAs","listString":["alpha
                                                  //| ","beta","gamma","delta"],"listInt":[1,2,3,4,5,6,7,8,9,10],"listDouble":[1.
                                                  //| 0,0.5,0.2,0.125],"listBigDecimal":["1.333333","1.33333335","1.6666666","1.6
                                                  //| 666667"],"listDateTime"
                                                  //| Output exceeds cutoff limit.
 
   
  val backFromWarpV = riftWarp.receiveFromWarpWithBlobs[DimensionCord, TestObjectA](blobFetch)(RiftJson())(warpStreamV.forceResult)
                                                  //> backFromWarpV  : almhirt.common.package.AlmValidation[riftwarp.TestObjectA]
                                                  //|  = Success(TestObjectA([B@4adf2940,[B@5982bcde,PrimitiveTypes(I am Pete,tru
                                                  //| e,127,-237823,-278234263,26587625768237658736586387652875687568276525252057
                                                  //| 7305007209857025728132213242,1.3672322,1.3672322350005,23761247614876823746
                                                  //| .23846749182408,2012-12-19T21:28:57.257+01:00,8f9ce1a0-be58-4a7d-91f7-46613
                                                  //| 5be3428),PrimitiveListMAs(List(alpha, beta, gamma, delta),List(1, 2, 3, 4, 
                                                  //| 5, 6, 7, 8, 9, 10),List(1.0, 0.5, 0.2, 0.125),List(1.333333, 1.33333335, 1.
                                                  //| 6666666, 1.6666667),List(2012-12-19T22:28:57.306+01:00, 2012-12-19T23:28:57
                                                  //| .307+01:00, 2012-12-20T00:28:57.307+01:00, 2012-12-20T01:28:57.307+01:00)),
                                                  //| PrimitiveVectorMAs(Vector(alpha, beta, gamma, delta),Vector(1, 2, 3, 4, 5, 
                                                  //| 6, 7, 8, 9, 10),Vector(1.0, 0.5, 0.2, 0.125),Vector(1.333333, 1.33333335, 1
                                                  //| .6666666, 1.6666667),Vector(2012-12-19T22:28:57.308+01:00, 2012-12-19T23:28
                                                  //| :57.308+01:00, 2012-12-
                                                  //| Output exceeds cutoff limit.
  

  val backFromWarp = backFromWarpV.forceResult    //> backFromWarp  : riftwarp.TestObjectA = TestObjectA([B@4adf2940,[B@5982bcde,
                                                  //| PrimitiveTypes(I am Pete,true,127,-237823,-278234263,2658762576823765873658
                                                  //| 63876528756875682765252520577305007209857025728132213242,1.3672322,1.367232
                                                  //| 2350005,23761247614876823746.23846749182408,2012-12-19T21:28:57.257+01:00,8
                                                  //| f9ce1a0-be58-4a7d-91f7-466135be3428),PrimitiveListMAs(List(alpha, beta, gam
                                                  //| ma, delta),List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),List(1.0, 0.5, 0.2, 0.125),L
                                                  //| ist(1.333333, 1.33333335, 1.6666666, 1.6666667),List(2012-12-19T22:28:57.30
                                                  //| 6+01:00, 2012-12-19T23:28:57.307+01:00, 2012-12-20T00:28:57.307+01:00, 2012
                                                  //| -12-20T01:28:57.307+01:00)),PrimitiveVectorMAs(Vector(alpha, beta, gamma, d
                                                  //| elta),Vector(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),Vector(1.0, 0.5, 0.2, 0.125),Ve
                                                  //| ctor(1.333333, 1.33333335, 1.6666666, 1.6666667),Vector(2012-12-19T22:28:57
                                                  //| .308+01:00, 2012-12-19T23:28:57.308+01:00, 2012-12-20T00:28:57.308+01:00, 2
                                                  //| 012-12-20T01:28:57.308+
                                                  //| Output exceeds cutoff limit.

  testObject == backFromWarp                      //> res0: Boolean = false


  riftWarp.prepareForWarp[DimensionRawMap](RiftMap())(testObject).bind(warpStream =>
    riftWarp.receiveFromWarp[DimensionRawMap, TestObjectA](RiftMap())(warpStream)).map(rearrived =>
    rearrived == testObject)                      //> res1: scalaz.Validation[almhirt.common.Problem,Boolean] = Success(true)

  riftWarp.prepareForWarp[DimensionCord](RiftJson())(testObject).bind(warpStream =>
    riftWarp.receiveFromWarp[DimensionCord, TestObjectA](RiftJson())(warpStream)).map(rearrived =>
    rearrived == testObject)                      //> res2: scalaz.Validation[almhirt.common.Problem,Boolean] = Success(false)

}