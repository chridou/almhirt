package riftwarp.worksheets

import scalaz.syntax.validation._
import almhirt.syntax.almvalidation._
import riftwarp._
import riftwarp.impl._

object Worksheet {
  val riftWarp = RiftWarp.concurrentWithDefaults  //> riftWarp  : riftwarp.RiftWarp = riftwarp.RiftWarp$$anon$1@59d57c39
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

  val testObject = TestObjectA.pete               //> testObject  : riftwarp.TestObjectA = TestObjectA([B@5aabb9e4,[B@4039563d,Pr
                                                  //| imitiveTypes(I am Pete,true,127,-237823,-278234263,265876257682376587365863
                                                  //| 876528756875682765252520577305007209857025728132213242,1.3672322,1.36723223
                                                  //| 50005,23761247614876823746.23846749182408,2013-01-01T17:15:36.844+01:00,438
                                                  //| e9398-1ac8-475e-aac8-d41d9e9b19a9),PrimitiveListMAs(List(alpha, beta, gamma
                                                  //| , delta),List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),List(1.0, 0.5, 0.2, 0.125),Lis
                                                  //| t(1.333333, 1.33333335, 1.6666666, 1.6666667),List(2013-01-01T18:15:36.894+
                                                  //| 01:00, 2013-01-01T19:15:36.894+01:00, 2013-01-01T20:15:36.894+01:00, 2013-0
                                                  //| 1-01T21:15:36.894+01:00)),PrimitiveVectorMAs(Vector(alpha, beta, gamma, del
                                                  //| ta),Vector(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),Vector(1.0, 0.5, 0.2, 0.125),Vect
                                                  //| or(1.333333, 1.33333335, 1.6666666, 1.6666667),Vector(2013-01-01T18:15:36.8
                                                  //| 95+01:00, 2013-01-01T19:15:36.895+01:00, 2013-01-01T20:15:36.895+01:00, 201
                                                  //| 3-01-01T21:15:36.895+01:00)),Some(PrimitiveSetMAs(Set(alpha, beta, gamma, d
                                                  //| elta),Set(5, 10, 1, 6, 9, 2, 7, 3, 8, 4),Set(1.0, 0.5, 0.2, 0.125),Set(1.33
                                                  //| 3333, 1.33333335, 1.6666666, 1.6666667),None)),PrimitiveIterableMAs(List(al
                                                  //| pha, beta, gamma, delta),List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),List(1.0, 0.5,
                                                  //|  0.2, 0.125),List(1.333333, 1.33333335, 1.6666666, 1.6666667),List(2013-01-
                                                  //| 01T18:15:36.896+01:00, 2013-01-01T19:15:36.896+01:00, 2013-01-01T20:15:36.8
                                                  //| 96+01:00, 2013-01-01T21:15:36.896+01:00)),ComplexMAs(List(TestAddress(Hambu
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

 
  val warpStreamV = riftWarp.prepareForWarpWithBlobs[DimensionString](blobDivert)(RiftXml())(testObject)
                                                  //> warpStreamV  : almhirt.common.AlmValidation[riftwarp.DimensionString] = Suc
                                                  //| cess(DimensionString(<TestObjectA typedescriptor="riftwarp.TestObjectA"><ar
                                                  //| rayByte>126,-123,12,-45,-128</arrayByte><blob><RiftBlobRefByName typedescri
                                                  //| ptor="RiftBlobRefByName"><name>027ecebb-8d4b-4fc1-ae20-9eb0c17eb1c6</name><
                                                  //| /RiftBlobRefByName></blob><primitiveTypes><PrimitiveTypes typedescriptor="r
                                                  //| iftwarp.PrimitiveTypes"><str>I am Pete</str><bool>true</bool><byte>127</byt
                                                  //| e><int>-237823</int><long>-278234263</long><bigInt>265876257682376587365863
                                                  //| 876528756875682765252520577305007209857025728132213242</bigInt><float>1.367
                                                  //| 2322</float><double>1.3672322350005</double><bigDec>23761247614876823746.23
                                                  //| 846749182408</bigDec><dateTime>2013-01-01T17:15:36.844+01:00</dateTime><uui
                                                  //| d>438e9398-1ac8-475e-aac8-d41d9e9b19a9</uuid></PrimitiveTypes></primitiveTy
                                                  //| pes><primitiveListMAs><PrimitiveListMAs typedescriptor="riftwarp.PrimitiveL
                                                  //| istMAs"><listString><Collection><String>alpha</String><String>beta</String>
                                                  //| <String>gamma</String><String>delta</String></Collection></listString><list
                                                  //| Int><Collection><Int>1</Int><Int>2</Int><Int>3</Int><Int>4</Int><Int>5</Int
                                                  //| ><Int>6</Int><Int>7</Int><Int>8</Int><Int>9</Int><Int>10</Int></Collection>
                                                  //| </listInt><listDouble><Collection><Double>1.0</Double><Double>0.5</Double><
                                                  //| Double>0.2</Double><Double>0.125</Double></Collection></listDouble><listBig
                                                  //| Decimal><Collection><BigDecimal>1.333333</BigDecimal><BigDecimal>1.33333335
                                                  //| </BigDecimal><BigDecimal>1.6666666</BigDecimal><BigDecimal>1.6666667</BigDe
                                                  //| cimal></Collection></listBigDecimal><listDateTime><Collection><DateTime>201
                                                  //| 3-01-01T18:15:36.894+01:00</DateTime><DateTime>2013-01-01T19:15:36.894+01:0
                                                  //| 0</DateTime><DateTime>2013-01-01T20:15:36.894+01:00</DateTime><DateTime>201
                                                  //| 3-01-01T21:15:36.894+01:00</DateTime></Collection></listDateTime></Primitiv
                                                  //| eListMAs></primitiveListMAs><primitiveVectorMAs><PrimitiveVectorMAs typedes
                                                  //| criptor="riftwarp.PrimitiveVectorMAs"><vectorString><Collection><String>alp
                                                  //| ha</String><String>beta</String><String>gamma</String><String>delta</String
                                                  //| ></Collection></vectorString><vectorInt><Collection><Int>1</Int><Int>2</Int
                                                  //| ><Int>3</Int><Int>4</Int><Int>5</Int><Int>6</Int><Int>7</Int><Int>8</Int><I
                                                  //| nt>9</Int><Int>10</Int></Collection></vectorInt><vectorDouble><Collection><
                                                  //| Double>1.0</Double><Double>0.5</Double><Double>0.2</Double><Double>0.125</D
                                                  //| ouble></Collection></vectorDouble><vectorBigDecimal><Collection><BigDecimal
                                                  //| >1.333333</BigDecimal><BigDecimal>1.33333335</BigDecimal><BigDecimal>1.6666
                                                  //| 666</BigDecimal><BigDecimal>1.6666667</BigDecimal></Collection></vectorBigD
                                                  //| ecimal><vectorDateTime><Collection><DateTime>2013-01-01T18:15:36.895+01:00<
                                                  //| /DateTime><DateTime>2013-01-01T19:15:36.895+01:00</DateTime><DateTime>2013-
                                                  //| 01-01T20:15:36.895+01:00</DateTime><DateTime>2013-01-01T21:15:36.895+01:00<
                                                  //| /DateTime></Collection></vectorDateTime></PrimitiveVectorMAs></primitiveVec
                                                  //| torMAs><primitiveSetMAs><PrimitiveSetMAs typedescriptor="riftwarp.Primitive
                                                  //| SetMAs"><setString><Collection><String>alpha</String><String>beta</String><
                                                  //| String>gamma</String><String>delta</String></Collection></setString><setInt
                                                  //| ><Collection><Int>6</Int><Int>4</Int><Int>7</Int><Int>9</Int><Int>8</Int><I
                                                  //| nt>3</Int><Int>5</Int><Int>1</Int><Int>10</Int><Int>2</Int></Collection></s
                                                  //| etInt><setDouble><Collection><Double>1.0</Double><Double>0.5</Double><Doubl
                                                  //| e>0.2</Double><Double>0.125</Double></Collection></setDouble><setBigDecimal
                                                  //| ><Collection><BigDecimal>1.333333</BigDecimal><BigDecimal>1.33333335</BigDe
                                                  //| cimal><BigDecimal>1.6666666</BigDecimal><BigDecimal>1.6666667</BigDecimal><
                                                  //| /Collection></setBigDecimal></PrimitiveSetMAs></primitiveSetMAs><primitiveI
                                                  //| terableMAs><PrimitiveIterableMAs typedescriptor="riftwarp.PrimitiveIterable
                                                  //| MAs"><iterableString><Collection><String>alpha</String><String>beta</String
                                                  //| ><String>gamma</String><String>delta</String></Collection></iterableString>
                                                  //| <iterableInt><Collection><Int>1</Int><Int>2</Int><Int>3</Int><Int>4</Int><I
                                                  //| nt>5</Int><Int>6</Int><Int>7</Int><Int>8</Int><Int>9</Int><Int>10</Int></Co
                                                  //| llection></iterableInt><iterableDouble><Collection><Double>1.0</Double><Dou
                                                  //| ble>0.5</Double><Double>0.2</Double><Double>0.125</Double></Collection></it
                                                  //| erableDouble><iterableBigDecimal><Collection><BigDecimal>1.333333</BigDecim
                                                  //| al><BigDecimal>1.33333335</BigDecimal><BigDecimal>1.6666666</BigDecimal><Bi
                                                  //| gDecimal>1.6666667</BigDecimal></Collection></iterableBigDecimal><iterableD
                                                  //| ateTime><Collection><DateTime>2013-01-01T18:15:36.896+01:00</DateTime><Date
                                                  //| Time>2013-01-01T19:15:36.896+01:00</DateTime><DateTime>2013-01-01T20:15:36.
                                                  //| 896+01:00</DateTime><DateTime>2013-01-01T21:15:36.896+01:00</DateTime></Col
                                                  //| lection></iterableDateTime></PrimitiveIterableMAs></primitiveIterableMAs><c
                                                  //| omplexMAs><ComplexMAs typedescriptor="riftwarp.ComplexMAs"><addresses1><Col
                                                  //| lection><TestAddress typedescriptor="riftwarp.TestAddress"><city>Hamburg</c
                                                  //| ity><street>Am Hafen</street></TestAddress><TestAddress typedescriptor="rif
                                                  //| twarp.TestAddress"><city>New York</city><street>Broadway</street></TestAddr
                                                  //| ess><TestAddress typedescriptor="riftwarp.TestAddress"><city>Los Angeles </
                                                  //| city><street>Sunset Boulevard</street></TestAddress></Collection></addresse
                                                  //| s1><addresses2><Collection><TestAddress typedescriptor="riftwarp.TestAddres
                                                  //| s"><city>Hamburg</city><street>Am Hafen</street></TestAddress><TestAddress 
                                                  //| typedescriptor="riftwarp.TestAddress"><city>New York</city><street>Broadway
                                                  //| </street></TestAddress><TestAddress typedescriptor="riftwarp.TestAddress"><
                                                  //| city>Los Angeles </city><street>Sunset Boulevard</street></TestAddress></Co
                                                  //| llection></addresses2><addresses3><Collection><TestAddress typedescriptor="
                                                  //| riftwarp.TestAddress"><city>Hamburg</city><street>Am Hafen</street></TestAd
                                                  //| dress><TestAddress typedescriptor="riftwarp.TestAddress"><city>New York</ci
                                                  //| ty><street>Broadway</street></TestAddress><TestAddress typedescriptor="rift
                                                  //| warp.TestAddress"><city>Los Angeles </city><street>Sunset Boulevard</street
                                                  //| ></TestAddress></Collection></addresses3><anything><Collection><Boolean>tru
                                                  //| e</Boolean><String>hello</String><Int>1</Int><Long>2</Long><Double>3.0</Dou
                                                  //| ble><Float>3.0</Float><TestAddress typedescriptor="riftwarp.TestAddress"><c
                                                  //| ity>Somewhere</city><street>here</street></TestAddress></Collection></anyth
                                                  //| ing></ComplexMAs></complexMAs><addressOpt><TestAddress typedescriptor="rift
                                                  //| warp.TestAddress"><city>Berlin</city><street>At the wall 89</street></TestA
                                                  //| ddress></addressOpt></TestObjectA>))
     
  val backFromWarpV = riftWarp.receiveFromWarpWithBlobs[DimensionString, TestObjectA](blobFetch)(RiftXml())(warpStreamV.forceResult)
                                                  //> backFromWarpV  : almhirt.common.AlmValidation[riftwarp.TestObjectA] = Failu
                                                  //| re(almhirt.common.AggregateProblem
                                                  //| One or more problems occured. See problems.
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| Aggregated problems:
                                                  //| Problem 0:
                                                  //| almhirt.common.KeyNotFoundProblem
                                                  //| Nothing found for 'name'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map(key -> name)
                                                  //| )
  
  val backFromWarp = backFromWarpV.forceResult    //> almhirt.common.ResultForcedFromValidationException: A value has been forced
                                                  //|  from a failure: One or more problems occured. See problems.
                                                  //| 	at almhirt.almvalidation.AlmValidationOps5$$anonfun$forceResult$1.apply(
                                                  //| AlmValidationOps.scala:118)
                                                  //| 	at almhirt.almvalidation.AlmValidationOps5$$anonfun$forceResult$1.apply(
                                                  //| AlmValidationOps.scala:118)
                                                  //| 	at scalaz.Validation$class.fold(Validation.scala:64)
                                                  //| 	at scalaz.Failure.fold(Validation.scala:316)
                                                  //| 	at almhirt.almvalidation.AlmValidationOps5$class.forceResult(AlmValidati
                                                  //| onOps.scala:118)
                                                  //| 	at almhirt.almvalidation.ToAlmValidationOps$$anon$6.forceResult(AlmValid
                                                  //| ationOps.scala:183)
                                                  //| 	at riftwarp.worksheets.Worksheet$$anonfun$main$1.apply$mcV$sp(riftwarp.w
                                                  //| orksheets.Worksheet.scala:51)
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


  riftWarp.prepareForWarp[DimensionRawMap](RiftMap())(testObject).flatMap(warpStream =>
    riftWarp.receiveFromWarp[DimensionRawMap, TestObjectA](RiftMap())(warpStream)).map(rearrived =>
    rearrived == testObject)

  riftWarp.prepareForWarp[DimensionCord](RiftJson())(testObject).flatMap(warpStream =>
    riftWarp.receiveFromWarp[DimensionCord, TestObjectA](RiftJson())(warpStream)).map(rearrived =>
    rearrived == testObject)

}