package riftwarp.worksheets

import scalaz.syntax.validation._
import almhirt.syntax.almvalidation._
import riftwarp._
import riftwarp.impl._

object Worksheet {
  val riftWarp = RiftWarp.concurrentWithDefaults  //> riftWarp  : riftwarp.RiftWarp = riftwarp.RiftWarp$$anon$1@101b57f7
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

  val testObject = TestObjectA.pete               //> testObject  : riftwarp.TestObjectA = TestObjectA([B@30b9cb99,[B@75e5c2cc,Pr
                                                  //| imitiveTypes(I am Pete,true,127,-237823,-278234263,265876257682376587365863
                                                  //| 876528756875682765252520577305007209857025728132213242,1.3672322,1.36723223
                                                  //| 50005,23761247614876823746.23846749182408,2012-12-28T19:33:29.429+01:00,26d
                                                  //| 2d30b-4620-441f-a465-5918110f7bb8),PrimitiveListMAs(List(alpha, beta, gamma
                                                  //| , delta),List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),List(1.0, 0.5, 0.2, 0.125),Lis
                                                  //| t(1.333333, 1.33333335, 1.6666666, 1.6666667),List(2012-12-28T20:33:29.488+
                                                  //| 01:00, 2012-12-28T21:33:29.489+01:00, 2012-12-28T22:33:29.489+01:00, 2012-1
                                                  //| 2-28T23:33:29.489+01:00)),PrimitiveVectorMAs(Vector(alpha, beta, gamma, del
                                                  //| ta),Vector(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),Vector(1.0, 0.5, 0.2, 0.125),Vect
                                                  //| or(1.333333, 1.33333335, 1.6666666, 1.6666667),Vector(2012-12-28T20:33:29.4
                                                  //| 90+01:00, 2012-12-28T21:33:29.490+01:00, 2012-12-28T22:33:29.490+01:00, 201
                                                  //| 2-12-28T23:33:29.490+01
                                                  //| Output exceeds cutoff limit.
 
 
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

 
  val warpStreamV = riftWarp.prepareForWarpWithBlobs[DimensionCord](blobDivert)(RiftJson())(testObject)
                                                  //> warpStreamV  : almhirt.common.AlmValidation[riftwarp.DimensionCord] = Succe
                                                  //| ss(DimensionCord({"riftwarptd":"riftwarp.TestObjectA","arrayByte":[126,-123
                                                  //| ,12,-45,-128],"blob":{"riftwarptd":"RiftBlobRefByName","name":"f9540b3e-8bf
                                                  //| 1-45e8-9704-502ecc6f6e59"},"primitiveTypes":{"riftwarptd":"riftwarp.Primiti
                                                  //| veTypes","str":"I am Pete","bool":true,"byte":127,"int":-237823,"long":-278
                                                  //| 234263,"bigInt":"2658762576823765873658638765287568756827652525205773050072
                                                  //| 09857025728132213242","float":1.3672322034835815,"double":1.3672322350005,"
                                                  //| bigDec":"23761247614876823746.23846749182408","dateTime":"2012-12-28T19:33:
                                                  //| 29.429+01:00","uuid":"26d2d30b-4620-441f-a465-5918110f7bb8"},"primitiveList
                                                  //| MAs":{"riftwarptd":"riftwarp.PrimitiveListMAs","listString":["alpha","beta"
                                                  //| ,"gamma","delta"],"listInt":[1,2,3,4,5,6,7,8,9,10],"listDouble":[1.0,0.5,0.
                                                  //| 2,0.125],"listBigDecimal":["1.333333","1.33333335","1.6666666","1.6666667"]
                                                  //| ,"listDateTime":["2012-
                                                  //| Output exceeds cutoff limit.
   
   
  val backFromWarpV = riftWarp.receiveFromWarpWithBlobs[DimensionCord, TestObjectA](blobFetch)(RiftJson())(warpStreamV.forceResult)
                                                  //> backFromWarpV  : almhirt.common.AlmValidation[riftwarp.TestObjectA] = Succe
                                                  //| ss(TestObjectA([B@4f0ef738,[B@75e5c2cc,PrimitiveTypes(I am Pete,true,127,-2
                                                  //| 37823,-278234263,2658762576823765873658638765287568756827652525205773050072
                                                  //| 09857025728132213242,1.3672322,1.3672322350005,23761247614876823746.2384674
                                                  //| 9182408,2012-12-28T19:33:29.429+01:00,26d2d30b-4620-441f-a465-5918110f7bb8)
                                                  //| ,PrimitiveListMAs(List(alpha, beta, gamma, delta),List(1, 2, 3, 4, 5, 6, 7,
                                                  //|  8, 9, 10),List(1.0, 0.5, 0.2, 0.125),List(1.333333, 1.33333335, 1.6666666,
                                                  //|  1.6666667),List(2012-12-28T20:33:29.488+01:00, 2012-12-28T21:33:29.489+01:
                                                  //| 00, 2012-12-28T22:33:29.489+01:00, 2012-12-28T23:33:29.489+01:00)),Primitiv
                                                  //| eVectorMAs(Vector(alpha, beta, gamma, delta),Vector(1, 2, 3, 4, 5, 6, 7, 8,
                                                  //|  9, 10),Vector(1.0, 0.5, 0.2, 0.125),Vector(1.333333, 1.33333335, 1.6666666
                                                  //| , 1.6666667),Vector(2012-12-28T20:33:29.490+01:00, 2012-12-28T21:33:29.490+
                                                  //| 01:00, 2012-12-28T22:33
                                                  //| Output exceeds cutoff limit.
  

  val backFromWarp = backFromWarpV.forceResult    //> backFromWarp  : riftwarp.TestObjectA = TestObjectA([B@4f0ef738,[B@75e5c2cc,
                                                  //| PrimitiveTypes(I am Pete,true,127,-237823,-278234263,2658762576823765873658
                                                  //| 63876528756875682765252520577305007209857025728132213242,1.3672322,1.367232
                                                  //| 2350005,23761247614876823746.23846749182408,2012-12-28T19:33:29.429+01:00,2
                                                  //| 6d2d30b-4620-441f-a465-5918110f7bb8),PrimitiveListMAs(List(alpha, beta, gam
                                                  //| ma, delta),List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),List(1.0, 0.5, 0.2, 0.125),L
                                                  //| ist(1.333333, 1.33333335, 1.6666666, 1.6666667),List(2012-12-28T20:33:29.48
                                                  //| 8+01:00, 2012-12-28T21:33:29.489+01:00, 2012-12-28T22:33:29.489+01:00, 2012
                                                  //| -12-28T23:33:29.489+01:00)),PrimitiveVectorMAs(Vector(alpha, beta, gamma, d
                                                  //| elta),Vector(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),Vector(1.0, 0.5, 0.2, 0.125),Ve
                                                  //| ctor(1.333333, 1.33333335, 1.6666666, 1.6666667),Vector(2012-12-28T20:33:29
                                                  //| .490+01:00, 2012-12-28T21:33:29.490+01:00, 2012-12-28T22:33:29.490+01:00, 2
                                                  //| 012-12-28T23:33:29.490+
                                                  //| Output exceeds cutoff limit.

  testObject == backFromWarp                      //> res0: Boolean = false


  riftWarp.prepareForWarp[DimensionRawMap](RiftMap())(testObject).flatMap(warpStream =>
    riftWarp.receiveFromWarp[DimensionRawMap, TestObjectA](RiftMap())(warpStream)).map(rearrived =>
    rearrived == testObject)                      //> res1: scalaz.Validation[almhirt.common.Problem,Boolean] = Success(true)

  riftWarp.prepareForWarp[DimensionCord](RiftJson())(testObject).flatMap(warpStream =>
    riftWarp.receiveFromWarp[DimensionCord, TestObjectA](RiftJson())(warpStream)).map(rearrived =>
    rearrived == testObject)                      //> res2: scalaz.Validation[almhirt.common.Problem,Boolean] = Success(false)

}