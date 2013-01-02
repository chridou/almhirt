package riftwarp.worksheets

import scalaz.syntax.validation._
import almhirt.syntax.almvalidation._
import riftwarp._
import riftwarp.impl._

object Worksheet {
  val riftWarp = RiftWarp.concurrentWithDefaults  //> List(riftwarp.DimensionNiceCordToCord$@18ada729)
                                                  //| List(riftwarp.DimensionNiceStringToString$@6880d5d5)
                                                  //| List(riftwarp.DimensionConverterStringToXmlElem$@4657104d)
                                                  //| List(riftwarp.DimensionConverterCordToString$@52df888a, riftwarp.DimensionNi
                                                  //| ceStringToString$@6880d5d5)
                                                  //| List(riftwarp.DimensionConverterStringToCord$@87c06e0, riftwarp.DimensionNic
                                                  //| eCordToCord$@18ada729)
                                                  //| riftWarp  : riftwarp.RiftWarp = riftwarp.RiftWarp$$anon$1@229ec9cd
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

  val testObject = TestObjectA.pete               //> testObject  : riftwarp.TestObjectA = TestObjectA([B@713c3100,[B@26b53114,Pr
                                                  //| imitiveTypes(I am Pete,true,127,-237823,-278234263,265876257682376587365863
                                                  //| 876528756875682765252520577305007209857025728132213242,1.3672322,1.36723223
                                                  //| 50005,23761247614876823746.23846749182408,2013-01-02T14:34:27.496+01:00,f0c
                                                  //| 4ebcf-d02e-43ef-a5cd-95eac1b50837),PrimitiveListMAs(List(alpha, beta, gamma
                                                  //| , delta),List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),List(1.0, 0.5, 0.2, 0.125),Lis
                                                  //| t(1.333333, 1.33333335, 1.6666666, 1.6666667),List(2013-01-02T15:34:27.559+
                                                  //| 01:00, 2013-01-02T16:34:27.559+01:00, 2013-01-02T17:34:27.559+01:00, 2013-0
                                                  //| 1-02T18:34:27.559+01:00)),PrimitiveVectorMAs(Vector(alpha, beta, gamma, del
                                                  //| ta),Vector(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),Vector(1.0, 0.5, 0.2, 0.125),Vect
                                                  //| or(1.333333, 1.33333335, 1.6666666, 1.6666667),Vector(2013-01-02T15:34:27.5
                                                  //| 59+01:00, 2013-01-02T16:34:27.559+01:00, 2013-01-02T17:34:27.559+01:00, 201
                                                  //| 3-01-02T18:34:27.559+01:00)),Some(PrimitiveSetMAs(Set(alpha, beta, gamma, d
                                                  //| elta),Set(5, 10, 1, 6, 9, 2, 7, 3, 8, 4),Set(1.0, 0.5, 0.2, 0.125),Set(1.33
                                                  //| 3333, 1.33333335, 1.6666666, 1.6666667),None)),PrimitiveIterableMAs(List(al
                                                  //| pha, beta, gamma, delta),List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),List(1.0, 0.5,
                                                  //|  0.2, 0.125),List(1.333333, 1.33333335, 1.6666666, 1.6666667),List(2013-01-
                                                  //| 02T15:34:27.574+01:00, 2013-01-02T16:34:27.574+01:00, 2013-01-02T17:34:27.5
                                                  //| 74+01:00, 2013-01-02T18:34:27.574+01:00)),ComplexMAs(List(TestAddress(Hambu
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
                                                  //| AlmValidation[riftwarp.RiftBlob] = <function2>
	
	
	val blobFetch: BlobFetch = blob =>
	  blob match {
	    case RiftBlobRefByName(name) => blobdata(name).success
	  }                                       //> blobFetch  : riftwarp.RiftBlob => almhirt.common.AlmValidation[Array[Byte]]
                                                  //|  = <function1>

 
  val warpStreamV = riftWarp.prepareForWarpWithBlobs[DimensionString](blobDivert)(RiftJson())(testObject)
                                                  //> warpStreamV  : almhirt.common.AlmValidation[riftwarp.DimensionString] = Suc
                                                  //| cess(DimensionString({"riftwarptd":"riftwarp.TestObjectA","arrayByte":[126,
                                                  //| -123,12,-45,-128],"blob":{"riftwarptd":"RiftBlobRefByName","name":"f96df9ed
                                                  //| -216e-45a4-9ee4-3733b4866c02"},"primitiveTypes":{"riftwarptd":"riftwarp.Pri
                                                  //| mitiveTypes","str":"I am Pete","bool":true,"byte":127,"int":-237823,"long":
                                                  //| -278234263,"bigInt":"265876257682376587365863876528756875682765252520577305
                                                  //| 007209857025728132213242","float":1.3672322034835815,"double":1.36723223500
                                                  //| 05,"bigDec":"23761247614876823746.23846749182408","dateTime":"2013-01-02T14
                                                  //| :34:27.496+01:00","uuid":"f0c4ebcf-d02e-43ef-a5cd-95eac1b50837"},"primitive
                                                  //| ListMAs":{"riftwarptd":"riftwarp.PrimitiveListMAs","listString":["alpha","b
                                                  //| eta","gamma","delta"],"listInt":[1,2,3,4,5,6,7,8,9,10],"listDouble":[1.0,0.
                                                  //| 5,0.2,0.125],"listBigDecimal":["1.333333","1.33333335","1.6666666","1.66666
                                                  //| 67"],"listDateTime":["2013-01-02T15:34:27.559+01:00","2013-01-02T16:34:27.5
                                                  //| 59+01:00","2013-01-02T17:34:27.559+01:00","2013-01-02T18:34:27.559+01:00"]}
                                                  //| ,"primitiveVectorMAs":{"riftwarptd":"riftwarp.PrimitiveVectorMAs","vectorSt
                                                  //| ring":["alpha","beta","gamma","delta"],"vectorInt":[1,2,3,4,5,6,7,8,9,10],"
                                                  //| vectorDouble":[1.0,0.5,0.2,0.125],"vectorBigDecimal":["1.333333","1.3333333
                                                  //| 5","1.6666666","1.6666667"],"vectorDateTime":["2013-01-02T15:34:27.559+01:0
                                                  //| 0","2013-01-02T16:34:27.559+01:00","2013-01-02T17:34:27.559+01:00","2013-01
                                                  //| -02T18:34:27.559+01:00"]},"primitiveSetMAs":{"riftwarptd":"riftwarp.Primiti
                                                  //| veSetMAs","setString":["alpha","beta","gamma","delta"],"setInt":[4,2,8,5,7,
                                                  //| 9,6,1,10,3],"setDouble":[1.0,0.5,0.2,0.125],"setBigDecimal":["1.333333","1.
                                                  //| 33333335","1.6666666","1.6666667"],"setDateTime":null},"primitiveIterableMA
                                                  //| s":{"riftwarptd":"riftwarp.PrimitiveIterableMAs","iterableString":["alpha",
                                                  //| "beta","gamma","delta"],"iterableInt":[1,2,3,4,5,6,7,8,9,10],"iterableDoubl
                                                  //| e":[1.0,0.5,0.2,0.125],"iterableBigDecimal":["1.333333","1.33333335","1.666
                                                  //| 6666","1.6666667"],"iterableDateTime":["2013-01-02T15:34:27.574+01:00","201
                                                  //| 3-01-02T16:34:27.574+01:00","2013-01-02T17:34:27.574+01:00","2013-01-02T18:
                                                  //| 34:27.574+01:00"]},"complexMAs":{"riftwarptd":"riftwarp.ComplexMAs","addres
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
                                                  //| ss","city":"Somewhere","street":"here"}]},"addressOpt":{"riftwarptd":"riftw
                                                  //| arp.TestAddress","city":"Berlin","street":"At the wall 89"}}))
     
  val backFromWarpV = riftWarp.receiveFromWarpWithBlobs[DimensionString, TestObjectA](blobFetch)(RiftJson())(warpStreamV.forceResult)
                                                  //> backFromWarpV  : almhirt.common.AlmValidation[riftwarp.TestObjectA] = Succe
                                                  //| ss(TestObjectA([B@2556af33,[B@26b53114,PrimitiveTypes(I am Pete,true,127,-2
                                                  //| 37823,-278234263,2658762576823765873658638765287568756827652525205773050072
                                                  //| 09857025728132213242,1.3672322,1.3672322350005,23761247614876823746.2384674
                                                  //| 9182408,2013-01-02T14:34:27.496+01:00,f0c4ebcf-d02e-43ef-a5cd-95eac1b50837)
                                                  //| ,PrimitiveListMAs(List(alpha, beta, gamma, delta),List(1, 2, 3, 4, 5, 6, 7,
                                                  //|  8, 9, 10),List(1.0, 0.5, 0.2, 0.125),List(1.333333, 1.33333335, 1.6666666,
                                                  //|  1.6666667),List(2013-01-02T15:34:27.559+01:00, 2013-01-02T16:34:27.559+01:
                                                  //| 00, 2013-01-02T17:34:27.559+01:00, 2013-01-02T18:34:27.559+01:00)),Primitiv
                                                  //| eVectorMAs(Vector(alpha, beta, gamma, delta),Vector(1, 2, 3, 4, 5, 6, 7, 8,
                                                  //|  9, 10),Vector(1.0, 0.5, 0.2, 0.125),Vector(1.333333, 1.33333335, 1.6666666
                                                  //| , 1.6666667),Vector(2013-01-02T15:34:27.559+01:00, 2013-01-02T16:34:27.559+
                                                  //| 01:00, 2013-01-02T17:34:27.559+01:00, 2013-01-02T18:34:27.559+01:00)),Some(
                                                  //| PrimitiveSetMAs(Set(alpha, beta, gamma, delta),Set(5, 10, 1, 6, 9, 2, 7, 3,
                                                  //|  8, 4),Set(1.0, 0.5, 0.2, 0.125),Set(1.333333, 1.33333335, 1.6666666, 1.666
                                                  //| 6667),None)),PrimitiveIterableMAs(Set(alpha, beta, gamma, delta),Set(5, 10,
                                                  //|  1, 6, 9, 2, 7, 3, 8, 4),Set(1.0, 0.5, 0.2, 0.125),Set(1.333333, 1.33333335
                                                  //| , 1.6666666, 1.6666667),Set(2013-01-02T15:34:27.574+01:00, 2013-01-02T16:34
                                                  //| :27.574+01:00, 2013-01-02T17:34:27.574+01:00, 2013-01-02T18:34:27.574+01:00
                                                  //| )),ComplexMAs(List(TestAddress(Hamburg,Am Hafen), TestAddress(New York,Broa
                                                  //| dway), TestAddress(Los Angeles ,Sunset Boulevard)),Vector(TestAddress(Hambu
                                                  //| rg,Am Hafen), TestAddress(New York,Broadway), TestAddress(Los Angeles ,Suns
                                                  //| et Boulevard)),Set(TestAddress(Hamburg,Am Hafen), TestAddress(New York,Broa
                                                  //| dway), TestAddress(Los Angeles ,Sunset Boulevard)),List(true, hello, 1.0, 2
                                                  //| .0, 3.0, 3.0, Map(riftwarptd -> riftwarp.TestAddress, city -> Somewhere, st
                                                  //| reet -> here))),Some(TestAddress(Berlin,At the wall 89))))
  
  val backFromWarp = backFromWarpV.forceResult    //> backFromWarp  : riftwarp.TestObjectA = TestObjectA([B@2556af33,[B@26b53114,
                                                  //| PrimitiveTypes(I am Pete,true,127,-237823,-278234263,2658762576823765873658
                                                  //| 63876528756875682765252520577305007209857025728132213242,1.3672322,1.367232
                                                  //| 2350005,23761247614876823746.23846749182408,2013-01-02T14:34:27.496+01:00,f
                                                  //| 0c4ebcf-d02e-43ef-a5cd-95eac1b50837),PrimitiveListMAs(List(alpha, beta, gam
                                                  //| ma, delta),List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),List(1.0, 0.5, 0.2, 0.125),L
                                                  //| ist(1.333333, 1.33333335, 1.6666666, 1.6666667),List(2013-01-02T15:34:27.55
                                                  //| 9+01:00, 2013-01-02T16:34:27.559+01:00, 2013-01-02T17:34:27.559+01:00, 2013
                                                  //| -01-02T18:34:27.559+01:00)),PrimitiveVectorMAs(Vector(alpha, beta, gamma, d
                                                  //| elta),Vector(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),Vector(1.0, 0.5, 0.2, 0.125),Ve
                                                  //| ctor(1.333333, 1.33333335, 1.6666666, 1.6666667),Vector(2013-01-02T15:34:27
                                                  //| .559+01:00, 2013-01-02T16:34:27.559+01:00, 2013-01-02T17:34:27.559+01:00, 2
                                                  //| 013-01-02T18:34:27.559+01:00)),Some(PrimitiveSetMAs(Set(alpha, beta, gamma,
                                                  //|  delta),Set(5, 10, 1, 6, 9, 2, 7, 3, 8, 4),Set(1.0, 0.5, 0.2, 0.125),Set(1.
                                                  //| 333333, 1.33333335, 1.6666666, 1.6666667),None)),PrimitiveIterableMAs(Set(a
                                                  //| lpha, beta, gamma, delta),Set(5, 10, 1, 6, 9, 2, 7, 3, 8, 4),Set(1.0, 0.5, 
                                                  //| 0.2, 0.125),Set(1.333333, 1.33333335, 1.6666666, 1.6666667),Set(2013-01-02T
                                                  //| 15:34:27.574+01:00, 2013-01-02T16:34:27.574+01:00, 2013-01-02T17:34:27.574+
                                                  //| 01:00, 2013-01-02T18:34:27.574+01:00)),ComplexMAs(List(TestAddress(Hamburg,
                                                  //| Am Hafen), TestAddress(New York,Broadway), TestAddress(Los Angeles ,Sunset 
                                                  //| Boulevard)),Vector(TestAddress(Hamburg,Am Hafen), TestAddress(New York,Broa
                                                  //| dway), TestAddress(Los Angeles ,Sunset Boulevard)),Set(TestAddress(Hamburg,
                                                  //| Am Hafen), TestAddress(New York,Broadway), TestAddress(Los Angeles ,Sunset 
                                                  //| Boulevard)),List(true, hello, 1.0, 2.0, 3.0, 3.0, Map(riftwarptd -> riftwar
                                                  //| p.TestAddress, city -> Somewhere, street -> here))),Some(TestAddress(Berlin
                                                  //| ,At the wall 89)))

  testObject == backFromWarp                      //> res0: Boolean = false


  riftWarp.prepareForWarp[DimensionRawMap](RiftMap())(testObject).flatMap(warpStream =>
    riftWarp.receiveFromWarp[DimensionRawMap, TestObjectA](RiftMap())(warpStream)).map(rearrived =>
    rearrived == testObject)                      //> res1: scalaz.Validation[almhirt.common.Problem,Boolean] = Success(true)

  riftWarp.prepareForWarp[DimensionCord](RiftJson())(testObject).flatMap(warpStream =>
    riftWarp.receiveFromWarp[DimensionCord, TestObjectA](RiftJson())(warpStream)).map(rearrived =>
    rearrived == testObject)                      //> res2: scalaz.Validation[almhirt.common.Problem,Boolean] = Success(false)

}