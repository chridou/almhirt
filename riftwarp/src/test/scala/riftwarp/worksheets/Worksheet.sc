package riftwarp.worksheets

import scalaz.syntax.validation._
import almhirt.syntax.almvalidation._
import riftwarp._
import riftwarp.impl._

object Worksheet {
  val riftWarp = RiftWarp.concurrentWithDefaults  //> riftWarp  : <error> = riftwarp.RiftWarp$$anon$1@6a8c436b
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

  val testObject = TestObjectA.pete               //> testObject  : riftwarp.TestObjectA = TestObjectA([B@4e513d61,[B@3794d372,Pr
                                                  //| imitiveTypes(I am Pete,true,127,-237823,-278234263,265876257682376587365863
                                                  //| 876528756875682765252520577305007209857025728132213242,1.3672322,1.36723223
                                                  //| 50005,23761247614876823746.23846749182408,2012-12-20T19:02:46.210+01:00,757
                                                  //| a2823-d99a-4c55-aebf-a5840ba0adc3),PrimitiveListMAs(List(alpha, beta, gamma
                                                  //| , delta),List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),List(1.0, 0.5, 0.2, 0.125),Lis
                                                  //| t(1.333333, 1.33333335, 1.6666666, 1.6666667),List(2012-12-20T20:02:46.252+
                                                  //| 01:00, 2012-12-20T21:02:46.253+01:00, 2012-12-20T22:02:46.253+01:00, 2012-1
                                                  //| 2-20T23:02:46.253+01:00)),PrimitiveVectorMAs(Vector(alpha, beta, gamma, del
                                                  //| ta),Vector(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),Vector(1.0, 0.5, 0.2, 0.125),Vect
                                                  //| or(1.333333, 1.33333335, 1.6666666, 1.6666667),Vector(2012-12-20T20:02:46.2
                                                  //| 53+01:00, 2012-12-20T21:02:46.253+01:00, 2012-12-20T22:02:46.253+01:00, 201
                                                  //| 2-12-20T23:02:46.253+01:00)),Some(PrimitiveSetMAs(Set(alpha, beta, gamma, d
                                                  //| elta),Set(5, 10, 1, 6, 9, 2, 7, 3, 8, 4),Set(1.0, 0.5, 0.2, 0.125),Set(1.33
                                                  //| 3333, 1.33333335, 1.6666666, 1.6666667),None)),PrimitiveIterableMAs(List(al
                                                  //| pha, beta, gamma, delta),List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),List(1.0, 0.5,
                                                  //|  0.2, 0.125),List(1.333333, 1.33333335, 1.6666666, 1.6666667),List(2012-12-
                                                  //| 20T20:02:46.271+01:00, 2012-12-20T21:02:46.271+01:00, 2012-12-20T22:02:46.2
                                                  //| 71+01:00, 2012-12-20T23:02:46.271+01:00)),ComplexMAs(List(TestAddress(Hambu
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
	}                                         //> blobDivert  : (Array[Byte], riftwarp.RiftBlobIdentifier) => almhirt.common.
                                                  //| package.AlmValidation[riftwarp.RiftBlob] = <function2>
	
	val blobFetch: BlobFetch = blob =>
	  blob match {
	    case RiftBlobRefByName(name) => blobdata(name).success
	  }                                       //> blobFetch  : riftwarp.RiftBlob => almhirt.common.package.AlmValidation[Arra
                                                  //| y[Byte]] = <function1>

 
  val warpStreamV = riftWarp.prepareForWarpWithBlobs[DimensionCord](blobDivert)(RiftJson())(testObject)
                                                  //> warpStreamV  : <error> = Success(DimensionCord({"riftwarptd":"riftwarp.Test
                                                  //| ObjectA","arrayByte":[126,-123,12,-45,-128],"blob":{"riftwarptd":"RiftBlobR
                                                  //| efByName","name":"c2defc11-2b2a-4bd1-a8de-9836dc8a6111"},"primitiveTypes":{
                                                  //| "riftwarptd":"riftwarp.PrimitiveTypes","str":"I am Pete","bool":true,"byte"
                                                  //| :127,"int":-237823,"long":-278234263,"bigInt":"2658762576823765873658638765
                                                  //| 28756875682765252520577305007209857025728132213242","float":1.3672322034835
                                                  //| 815,"double":1.3672322350005,"bigDec":"23761247614876823746.23846749182408"
                                                  //| ,"dateTime":"2012-12-20T19:02:46.210+01:00","uuid":"757a2823-d99a-4c55-aebf
                                                  //| -a5840ba0adc3"},"primitiveListMAs":{"riftwarptd":"riftwarp.PrimitiveListMAs
                                                  //| ","listString":["alpha","beta","gamma","delta"],"listInt":[1,2,3,4,5,6,7,8,
                                                  //| 9,10],"listDouble":[1.0,0.5,0.2,0.125],"listBigDecimal":["1.333333","1.3333
                                                  //| 3335","1.6666666","1.6666667"],"listDateTime":["2012-12-20T20:02:46.252+01:
                                                  //| 00","2012-12-20T21:02:46.253+01:00","2012-12-20T22:02:46.253+01:00","2012-1
                                                  //| 2-20T23:02:46.253+01:00"]},"primitiveVectorMAs":{"riftwarptd":"riftwarp.Pri
                                                  //| mitiveVectorMAs","vectorString":["alpha","beta","gamma","delta"],"vectorInt
                                                  //| ":[1,2,3,4,5,6,7,8,9,10],"vectorDouble":[1.0,0.5,0.2,0.125],"vectorBigDecim
                                                  //| al":["1.333333","1.33333335","1.6666666","1.6666667"],"vectorDateTime":["20
                                                  //| 12-12-20T20:02:46.253+01:00","2012-12-20T21:02:46.253+01:00","2012-12-20T22
                                                  //| :02:46.253+01:00","2012-12-20T23:02:46.253+01:00"]},"primitiveSetMAs":{"rif
                                                  //| twarptd":"riftwarp.PrimitiveSetMAs","setString":["alpha","beta","gamma","de
                                                  //| lta"],"setInt":[2,8,4,7,9,10,3,5,6,1],"setDouble":[1.0,0.5,0.2,0.125],"setB
                                                  //| igDecimal":["1.333333","1.33333335","1.6666666","1.6666667"],"setDateTime":
                                                  //| null},"primitiveIterableMAs":{"riftwarptd":"riftwarp.PrimitiveIterableMAs",
                                                  //| "iterableString":["alpha","beta","gamma","delta"],"iterableInt":[1,2,3,4,5,
                                                  //| 6,7,8,9,10],"iterableDouble":[1.0,0.5,0.2,0.125],"iterableBigDecimal":["1.3
                                                  //| 33333","1.33333335","1.6666666","1.6666667"],"iterableDateTime":["2012-12-2
                                                  //| 0T20:02:46.271+01:00","2012-12-20T21:02:46.271+01:00","2012-12-20T22:02:46.
                                                  //| 271+01:00","2012-12-20T23:02:46.271+01:00"]},"complexMAs":{"riftwarptd":"ri
                                                  //| ftwarp.ComplexMAs","addresses1":[{"riftwarptd":"riftwarp.TestAddress","city
                                                  //| ":"Hamburg","street":"Am Hafen"},{"riftwarptd":"riftwarp.TestAddress","city
                                                  //| ":"New York","street":"Broadway"},{"riftwarptd":"riftwarp.TestAddress","cit
                                                  //| y":"Los Angeles ","street":"Sunset Boulevard"}],"addresses2":[{"riftwarptd"
                                                  //| :"riftwarp.TestAddress","city":"Hamburg","street":"Am Hafen"},{"riftwarptd"
                                                  //| :"riftwarp.TestAddress","city":"New York","street":"Broadway"},{"riftwarptd
                                                  //| ":"riftwarp.TestAddress","city":"Los Angeles ","street":"Sunset Boulevard"}
                                                  //| ],"addresses3":[{"riftwarptd":"riftwarp.TestAddress","city":"Hamburg","stre
                                                  //| et":"Am Hafen"},{"riftwarptd":"riftwarp.TestAddress","city":"New York","str
                                                  //| eet":"Broadway"},{"riftwarptd":"riftwarp.TestAddress","city":"Los Angeles "
                                                  //| ,"street":"Sunset Boulevard"}],"anything":[true,"hello",1,2,3.0,3.0,{"riftw
                                                  //| arptd":"riftwarp.TestAddress","city":"Somewhere","street":"here"}]},"addres
                                                  //| sOpt":{"riftwarptd":"riftwarp.TestAddress","city":"Berlin","street":"At the
                                                  //|  wall 89"}}))
   
   
  val backFromWarpV = riftWarp.receiveFromWarpWithBlobs[DimensionCord, TestObjectA](blobFetch)(RiftJson())(warpStreamV.forceResult)
                                                  //> backFromWarpV  : <error> = Success(TestObjectA([B@10d4f27,[B@3794d372,Primi
                                                  //| tiveTypes(I am Pete,true,127,-237823,-278234263,265876257682376587365863876
                                                  //| 528756875682765252520577305007209857025728132213242,1.3672322,1.36723223500
                                                  //| 05,23761247614876823746.23846749182408,2012-12-20T19:02:46.210+01:00,757a28
                                                  //| 23-d99a-4c55-aebf-a5840ba0adc3),PrimitiveListMAs(List(alpha, beta, gamma, d
                                                  //| elta),List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),List(1.0, 0.5, 0.2, 0.125),List(1
                                                  //| .333333, 1.33333335, 1.6666666, 1.6666667),List(2012-12-20T20:02:46.252+01:
                                                  //| 00, 2012-12-20T21:02:46.253+01:00, 2012-12-20T22:02:46.253+01:00, 2012-12-2
                                                  //| 0T23:02:46.253+01:00)),PrimitiveVectorMAs(Vector(alpha, beta, gamma, delta)
                                                  //| ,Vector(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),Vector(1.0, 0.5, 0.2, 0.125),Vector(
                                                  //| 1.333333, 1.33333335, 1.6666666, 1.6666667),Vector(2012-12-20T20:02:46.253+
                                                  //| 01:00, 2012-12-20T21:02:46.253+01:00, 2012-12-20T22:02:46.253+01:00, 2012-1
                                                  //| 2-20T23:02:46.253+01:00)),Some(PrimitiveSetMAs(Set(alpha, beta, gamma, delt
                                                  //| a),Set(5, 10, 1, 6, 9, 2, 7, 3, 8, 4),Set(1.0, 0.5, 0.2, 0.125),Set(1.33333
                                                  //| 3, 1.33333335, 1.6666666, 1.6666667),None)),PrimitiveIterableMAs(Set(alpha,
                                                  //|  beta, gamma, delta),Set(5, 10, 1, 6, 9, 2, 7, 3, 8, 4),Set(1.0, 0.5, 0.2, 
                                                  //| 0.125),Set(1.333333, 1.33333335, 1.6666666, 1.6666667),Set(2012-12-20T20:02
                                                  //| :46.271+01:00, 2012-12-20T21:02:46.271+01:00, 2012-12-20T22:02:46.271+01:00
                                                  //| , 2012-12-20T23:02:46.271+01:00)),ComplexMAs(List(TestAddress(Hamburg,Am Ha
                                                  //| fen), TestAddress(New York,Broadway), TestAddress(Los Angeles ,Sunset Boule
                                                  //| vard)),Vector(TestAddress(Hamburg,Am Hafen), TestAddress(New York,Broadway)
                                                  //| , TestAddress(Los Angeles ,Sunset Boulevard)),Set(TestAddress(Hamburg,Am Ha
                                                  //| fen), TestAddress(New York,Broadway), TestAddress(Los Angeles ,Sunset Boule
                                                  //| vard)),List(true, hello, 1.0, 2.0, 3.0, 3.0, Map(riftwarptd -> riftwarp.Tes
                                                  //| tAddress, city -> Somewhere, street -> here))),Some(TestAddress(Berlin,At t
                                                  //| he wall 89))))
  

  val backFromWarp = backFromWarpV.forceResult    //> backFromWarp  : <error> = TestObjectA([B@10d4f27,[B@3794d372,PrimitiveTypes
                                                  //| (I am Pete,true,127,-237823,-278234263,265876257682376587365863876528756875
                                                  //| 682765252520577305007209857025728132213242,1.3672322,1.3672322350005,237612
                                                  //| 47614876823746.23846749182408,2012-12-20T19:02:46.210+01:00,757a2823-d99a-4
                                                  //| c55-aebf-a5840ba0adc3),PrimitiveListMAs(List(alpha, beta, gamma, delta),Lis
                                                  //| t(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),List(1.0, 0.5, 0.2, 0.125),List(1.333333, 
                                                  //| 1.33333335, 1.6666666, 1.6666667),List(2012-12-20T20:02:46.252+01:00, 2012-
                                                  //| 12-20T21:02:46.253+01:00, 2012-12-20T22:02:46.253+01:00, 2012-12-20T23:02:4
                                                  //| 6.253+01:00)),PrimitiveVectorMAs(Vector(alpha, beta, gamma, delta),Vector(1
                                                  //| , 2, 3, 4, 5, 6, 7, 8, 9, 10),Vector(1.0, 0.5, 0.2, 0.125),Vector(1.333333,
                                                  //|  1.33333335, 1.6666666, 1.6666667),Vector(2012-12-20T20:02:46.253+01:00, 20
                                                  //| 12-12-20T21:02:46.253+01:00, 2012-12-20T22:02:46.253+01:00, 2012-12-20T23:0
                                                  //| 2:46.253+01:00)),Some(PrimitiveSetMAs(Set(alpha, beta, gamma, delta),Set(5,
                                                  //|  10, 1, 6, 9, 2, 7, 3, 8, 4),Set(1.0, 0.5, 0.2, 0.125),Set(1.333333, 1.3333
                                                  //| 3335, 1.6666666, 1.6666667),None)),PrimitiveIterableMAs(Set(alpha, beta, ga
                                                  //| mma, delta),Set(5, 10, 1, 6, 9, 2, 7, 3, 8, 4),Set(1.0, 0.5, 0.2, 0.125),Se
                                                  //| t(1.333333, 1.33333335, 1.6666666, 1.6666667),Set(2012-12-20T20:02:46.271+0
                                                  //| 1:00, 2012-12-20T21:02:46.271+01:00, 2012-12-20T22:02:46.271+01:00, 2012-12
                                                  //| -20T23:02:46.271+01:00)),ComplexMAs(List(TestAddress(Hamburg,Am Hafen), Tes
                                                  //| tAddress(New York,Broadway), TestAddress(Los Angeles ,Sunset Boulevard)),Ve
                                                  //| ctor(TestAddress(Hamburg,Am Hafen), TestAddress(New York,Broadway), TestAdd
                                                  //| ress(Los Angeles ,Sunset Boulevard)),Set(TestAddress(Hamburg,Am Hafen), Tes
                                                  //| tAddress(New York,Broadway), TestAddress(Los Angeles ,Sunset Boulevard)),Li
                                                  //| st(true, hello, 1.0, 2.0, 3.0, 3.0, Map(riftwarptd -> riftwarp.TestAddress,
                                                  //|  city -> Somewhere, street -> here))),Some(TestAddress(Berlin,At the wall 8
                                                  //| 9)))

  testObject == backFromWarp                      //> res16: Boolean = false


  riftWarp.prepareForWarp[DimensionRawMap](RiftMap())(testObject).bind(warpStream =>
    riftWarp.receiveFromWarp[DimensionRawMap, TestObjectA](RiftMap())(warpStream)).map(rearrived =>
    rearrived == testObject)                      //> res17: <error> = Success(true)

  riftWarp.prepareForWarp[DimensionCord](RiftJson())(testObject).bind(warpStream =>
    riftWarp.receiveFromWarp[DimensionCord, TestObjectA](RiftJson())(warpStream)).map(rearrived =>
    rearrived == testObject)                      //> res18: <error> = Success(false)

}