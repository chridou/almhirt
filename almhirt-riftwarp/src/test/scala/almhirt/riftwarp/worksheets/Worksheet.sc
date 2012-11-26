package almhirt.riftwarp.worksheets

import almhirt.syntax.almvalidation._
import almhirt.riftwarp._
import almhirt.riftwarp.impl._

object Worksheet {
  val riftWarp = RiftWarp.unsafeWithDefaults      //> riftWarp  : almhirt.riftwarp.RiftWarp = almhirt.riftwarp.RiftWarp$$anon$97@3
                                                  //| ccc2187
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


  val testObject = TestObjectA.pete               //> testObject  : almhirt.riftwarp.TestObjectA = TestObjectA([B@442ac57c,[B@540
                                                  //| b72da,PrimitiveTypes(I am Pete,true,127,-237823,-278234263,2658762576823765
                                                  //| 87365863876528756875682765252520577305007209857025728132213242,1.3672322,1.
                                                  //| 3672322350005,23761247614876823746.23846749182408,2012-11-26T11:37:02.505+0
                                                  //| 1:00,0bfd0b98-cfaf-45de-a2c7-f87836397222),PrimitiveListMAs(List(alpha, bet
                                                  //| a, gamma, delta),List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),List(1.0, 0.5, 0.2, 0.
                                                  //| 125),List(1.333333, 1.33333335, 1.6666666, 1.6666667),List(2012-11-26T12:37
                                                  //| :02.583+01:00, 2012-11-26T13:37:02.583+01:00, 2012-11-26T14:37:02.583+01:00
                                                  //| , 2012-11-26T15:37:02.583+01:00)),PrimitiveVectorMAs(Vector(alpha, beta, ga
                                                  //| mma, delta),Vector(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),Vector(1.0, 0.5, 0.2, 0.1
                                                  //| 25),Vector(1.333333, 1.33333335, 1.6666666, 1.6666667),Vector(2012-11-26T12
                                                  //| :37:02.583+01:00, 2012-11-26T13:37:02.583+01:00, 2012-11-26T14:37:02.583+01
                                                  //| :00, 2012-11-26T15:37:02.583+01:00)),PrimitiveSetMAs(Set(alpha, beta, gamma
                                                  //| , delta),Set(5, 10, 1, 6, 9, 2, 7, 3, 8, 4),Set(1.0, 0.5, 0.2, 0.125),Set(1
                                                  //| .333333, 1.33333335, 1.6666666, 1.6666667),Set(2012-11-26T12:37:02.614+01:0
                                                  //| 0, 2012-11-26T13:37:02.614+01:00, 2012-11-26T14:37:02.614+01:00, 2012-11-26
                                                  //| T15:37:02.614+01:00)),PrimitiveIterableMAs(List(alpha, beta, gamma, delta),
                                                  //| List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),List(1.0, 0.5, 0.2, 0.125),List(1.33333
                                                  //| 3, 1.33333335, 1.6666666, 1.6666667),List(2012-11-26T12:37:02.614+01:00, 20
                                                  //| 12-11-26T13:37:02.614+01:00, 2012-11-26T14:37:02.614+01:00, 2012-11-26T15:3
                                                  //| 7:02.614+01:00)),Some(TestAddress(Berlin,At the wall 89)))
         
  val resV = riftWarp.prepareForWarp[DimensionCord](RiftJson())(testObject)
                                                  //> resV  : almhirt.common.package.AlmValidation[almhirt.riftwarp.DimensionCord
                                                  //| ] = Success(DimensionCord({"riftwarptd":"almhirt.riftwarp.TestObjectA","arr
                                                  //| ayByte":[126,-123,12,-45,-128],"blob":"AAAAAAAGhQzTgHAAAAA=","primitiveType
                                                  //| s":{"riftwarptd":"almhirt.riftwarp.PrimitiveTypes","str":"I am Pete","bool"
                                                  //| :true,"byte":127,"int":-237823,"long":-278234263,"bigInt":"2658762576823765
                                                  //| 87365863876528756875682765252520577305007209857025728132213242","float":1.3
                                                  //| 672322034835815,"double":1.3672322350005,"bigDec":"23761247614876823746.238
                                                  //| 46749182408","dateTime":"2012-11-26T11:37:02.505+01:00","uuid":"0bfd0b98-cf
                                                  //| af-45de-a2c7-f87836397222"},"primitiveListMAs":{"riftwarptd":"almhirt.riftw
                                                  //| arp.PrimitiveListMAs","listString":["alpha","beta","gamma","delta"],"listIn
                                                  //| t":[1,2,3,4,5,6,7,8,9,10],"listDouble":[1.0,0.5,0.2,0.125],"listBigDecimal"
                                                  //| :["1.333333","1.33333335","1.6666666","1.6666667"],"listDateTime":["2012-11
                                                  //| -26T12:37:02.583+01:00","2012-11-26T13:37:02.583+01:00","2012-11-26T14:37:0
                                                  //| 2.583+01:00","2012-11-26T15:37:02.583+01:00"]},"primitiveVectorMAs":{"riftw
                                                  //| arptd":"almhirt.riftwarp.PrimitiveVectorMAs","vectorString":["alpha","beta"
                                                  //| ,"gamma","delta"],"vectorInt":[1,2,3,4,5,6,7,8,9,10],"vectorDouble":[1.0,0.
                                                  //| 5,0.2,0.125],"vectorBigDecimal":["1.333333","1.33333335","1.6666666","1.666
                                                  //| 6667"],"vectorDateTime":["2012-11-26T12:37:02.583+01:00","2012-11-26T13:37:
                                                  //| 02.583+01:00","2012-11-26T14:37:02.583+01:00","2012-11-26T15:37:02.583+01:0
                                                  //| 0"]},"primitiveSetMAs":{"riftwarptd":"almhirt.riftwarp.PrimitiveSetMAs","se
                                                  //| tString":["alpha","beta","gamma","delta"],"setInt":[5,10,1,6,9,2,7,3,8,4],"
                                                  //| setDouble":[1.0,0.5,0.2,0.125],"setBigDecimal":["1.333333","1.33333335","1.
                                                  //| 6666666","1.6666667"],"setDateTime":["2012-11-26T12:37:02.614+01:00","2012-
                                                  //| 11-26T13:37:02.614+01:00","2012-11-26T14:37:02.614+01:00","2012-11-26T15:37
                                                  //| :02.614+01:00"]},"primitiveIterableMAs":{"riftwarptd":"almhirt.riftwarp.Pri
                                                  //| mitiveIterableMAs","iterableString":["alpha","beta","gamma","delta"],"itera
                                                  //| bleInt":[1,2,3,4,5,6,7,8,9,10],"iterableDouble":[1.0,0.5,0.2,0.125],"iterab
                                                  //| leBigDecimal":["1.333333","1.33333335","1.6666666","1.6666667"],"iterableDa
                                                  //| teTime":["2012-11-26T12:37:02.614+01:00","2012-11-26T13:37:02.614+01:00","2
                                                  //| 012-11-26T14:37:02.614+01:00","2012-11-26T15:37:02.614+01:00"]},"addressOpt
                                                  //| ":{"riftwarptd":"almhirt.riftwarp.TestAddress","city":"Berlin","street":"At
                                                  //|  the wall 89"}}))
  
  
  val warpStream = resV.forceResult               //> warpStream  : almhirt.riftwarp.DimensionCord = DimensionCord({"riftwarptd":
                                                  //| "almhirt.riftwarp.TestObjectA","arrayByte":[126,-123,12,-45,-128],"blob":"A
                                                  //| AAAAAAGhQzTgHAAAAA=","primitiveTypes":{"riftwarptd":"almhirt.riftwarp.Primi
                                                  //| tiveTypes","str":"I am Pete","bool":true,"byte":127,"int":-237823,"long":-2
                                                  //| 78234263,"bigInt":"26587625768237658736586387652875687568276525252057730500
                                                  //| 7209857025728132213242","float":1.3672322034835815,"double":1.3672322350005
                                                  //| ,"bigDec":"23761247614876823746.23846749182408","dateTime":"2012-11-26T11:3
                                                  //| 7:02.505+01:00","uuid":"0bfd0b98-cfaf-45de-a2c7-f87836397222"},"primitiveLi
                                                  //| stMAs":{"riftwarptd":"almhirt.riftwarp.PrimitiveListMAs","listString":["alp
                                                  //| ha","beta","gamma","delta"],"listInt":[1,2,3,4,5,6,7,8,9,10],"listDouble":[
                                                  //| 1.0,0.5,0.2,0.125],"listBigDecimal":["1.333333","1.33333335","1.6666666","1
                                                  //| .6666667"],"listDateTime":["2012-11-26T12:37:02.583+01:00","2012-11-26T13:3
                                                  //| 7:02.583+01:00","2012-11-26T14:37:02.583+01:00","2012-11-26T15:37:02.583+01
                                                  //| :00"]},"primitiveVectorMAs":{"riftwarptd":"almhirt.riftwarp.PrimitiveVector
                                                  //| MAs","vectorString":["alpha","beta","gamma","delta"],"vectorInt":[1,2,3,4,5
                                                  //| ,6,7,8,9,10],"vectorDouble":[1.0,0.5,0.2,0.125],"vectorBigDecimal":["1.3333
                                                  //| 33","1.33333335","1.6666666","1.6666667"],"vectorDateTime":["2012-11-26T12:
                                                  //| 37:02.583+01:00","2012-11-26T13:37:02.583+01:00","2012-11-26T14:37:02.583+0
                                                  //| 1:00","2012-11-26T15:37:02.583+01:00"]},"primitiveSetMAs":{"riftwarptd":"al
                                                  //| mhirt.riftwarp.PrimitiveSetMAs","setString":["alpha","beta","gamma","delta"
                                                  //| ],"setInt":[5,10,1,6,9,2,7,3,8,4],"setDouble":[1.0,0.5,0.2,0.125],"setBigDe
                                                  //| cimal":["1.333333","1.33333335","1.6666666","1.6666667"],"setDateTime":["20
                                                  //| 12-11-26T12:37:02.614+01:00","2012-11-26T13:37:02.614+01:00","2012-11-26T14
                                                  //| :37:02.614+01:00","2012-11-26T15:37:02.614+01:00"]},"primitiveIterableMAs":
                                                  //| {"riftwarptd":"almhirt.riftwarp.PrimitiveIterableMAs","iterableString":["al
                                                  //| pha","beta","gamma","delta"],"iterableInt":[1,2,3,4,5,6,7,8,9,10],"iterable
                                                  //| Double":[1.0,0.5,0.2,0.125],"iterableBigDecimal":["1.333333","1.33333335","
                                                  //| 1.6666666","1.6666667"],"iterableDateTime":["2012-11-26T12:37:02.614+01:00"
                                                  //| ,"2012-11-26T13:37:02.614+01:00","2012-11-26T14:37:02.614+01:00","2012-11-2
                                                  //| 6T15:37:02.614+01:00"]},"addressOpt":{"riftwarptd":"almhirt.riftwarp.TestAd
                                                  //| dress","city":"Berlin","street":"At the wall 89"}})

  val backFromWarpV = riftWarp.receiveFromWarp[DimensionCord, TestObjectA](RiftJson())(warpStream)
                                                  //> java.lang.AbstractMethodError: almhirt.riftwarp.RiftWarp$$anon$61.remateria
                                                  //| lize(Lalmhirt/riftwarp/RiftDimension;)Lscalaz/Validation;
                                                  //| 	at almhirt.riftwarp.impl.rematerializers.FromJsonMapRematerializationArr
                                                  //| ay$$anonfun$tryGetPrimitiveMA$1.apply(FromJsonMapRematerializationArray.sca
                                                  //| la:77)
                                                  //| 	at almhirt.riftwarp.impl.rematerializers.FromJsonMapRematerializationArr
                                                  //| ay$$anonfun$tryGetPrimitiveMA$1.apply(FromJsonMapRematerializationArray.sca
                                                  //| la:74)
                                                  //| 	at scalaz.Validation$class.bind(Validation.scala:137)
                                                  //| 	at scalaz.Success.bind(Validation.scala:304)
                                                  //| 	at almhirt.riftwarp.impl.rematerializers.FromJsonMapRematerializationArr
                                                  //| ay.tryGetPrimitiveMA(FromJsonMapRematerializationArray.scala:74)
                                                  //| 	at almhirt.riftwarp.RematiarializationArrayBasedOnOptionGetters$class.ge
                                                  //| tPrimitiveMA(RematerializationArray.scala:86)
                                                  //| 	at almhirt.riftwarp.impl.rematerializers.FromJsonMapRematerializationArr
                                                  //| ay.getPrimitiveMA(FromJsonMapRematerializationArray.scala:10)
                                                  //| 	at almhirt.riftwarp.PrimitiveListMAsRecomposer.recompose(TestObjectASeri
                                                  //| alization.scala:108)
                                                  //| 	at almhirt.riftwarp.impl.rematerializers.FromJsonMapRematerializationArr
                                                  //| ay$$anonfun$tryGetComplexType$3$$anonfun$apply$24$$anonfun$apply$26.apply(F
                                                  //| romJsonMapRematerializationArray.scala:61)
                                                  //| 	at almhirt.riftwarp.impl.rematerializers.FromJsonMapRematerializationArr
                                                  //| ay$$anonfun$tryGetComplexType$3$$anonfun$apply$24$$anonfun$apply$26.apply(F
                                                  //| romJsonMapRematerializationArray.scala:59)
                                                  //| 	at scalaz.Validation$class.bind(Validation.scala:137)
                                                  //| 	at scalaz.Success.bind(Validation.scala:304)
                                                  //| 	at almhirt.riftwarp.impl.rematerializers.FromJsonMapRematerializationArr
                                                  //| ay$$anonfun$tryGetComplexType$3$$anonfun$apply$24.apply(FromJsonMapRemateri
                                                  //| alizationArray.scala:59)
                                                  //| 	at almhirt.riftwarp.impl.rematerializers.FromJsonMapRematerializationArr
                                                  //| ay$$anonfun$tryGetComplexType$3$$anonfun$apply$24.apply(FromJsonMapRemateri
                                                  //| alizationArray.scala:55)
                                                  //| 	at scalaz.Validation$class.bind(Validation.scala:137)
                                                  //| 	at scalaz.Success.bind(Validation.scala:304)
                                                  //| 	at almhirt.riftwarp.impl.rematerializers.FromJsonMapRematerializationArr
                                                  //| ay$$anonfun$tryGetComplexType$3.apply(FromJsonMapRematerializationArray.sca
                                                  //| la:55)
                                                  //| 	at almhirt.riftwarp.impl.rematerializers.FromJsonMapRematerializationArr
                                                  //| ay$$anonfun$tryGetComplexType$3.apply(FromJsonMapRematerializationArray.sca
                                                  //| la:54)
                                                  //| 	at scalaz.Validation$class.bind(Validation.scala:137)
                                                  //| 	at scalaz.Success.bind(Validation.scala:304)
                                                  //| 	at almhirt.riftwarp.impl.rematerializers.FromJsonMapRematerializationArr
                                                  //| ay.tryGetComplexType(FromJsonMapRematerializationArray.scala:54)
                                                  //| 	at almhirt.riftwarp.RematiarializationArrayBasedOnOptionGetters$class.ge
                                                  //| tComplexType(RematerializationArray.scala:84)
                                                  //| 	at almhirt.riftwarp.impl.rematerializers.FromJsonMapRematerializationArr
                                                  //| ay.getComplexType(FromJsonMapRematerializationArray.scala:10)
                                                  //| 	at almhirt.riftwarp.TestObjectARecomposer.recompose(TestObjectASerializa
                                                  //| tion.scala:31)
                                                  //| 	at almhirt.riftwarp.Recomposer$class.recomposeRaw(Recomposer.scala:12)
                                                  //| 	at almhirt.riftwarp.TestObjectARecomposer.recomposeRaw(TestObjectASerial
                                                  //| ization.scala:25)
                                                  //| 	at almhirt.riftwarp.RiftWarp$$anonfun$receiveFromWarp$1$$anonfun$apply$4
                                                  //| .apply(RiftWarp.scala:44)
                                                  //| 	at almhirt.riftwarp.RiftWarp$$anonfun$receiveFromWarp$1$$anonfun$apply$4
                                                  //| .apply(RiftWarp.scala:40)
                                                  //| 	at scalaz.Validation$class.bind(Validation.scala:137)
                                                  //| 	at scalaz.Success.bind(Validation.scala:304)
                                                  //| 	at almhirt.riftwarp.RiftWarp$$anonfun$receiveFromWarp$1.apply(RiftWarp.s
                                                  //| cala:40)
                                                  //| 	at almhirt.riftwarp.RiftWarp$$anonfun$receiveFromWarp$1.apply(RiftWarp.s
                                                  //| cala:38)
                                                  //| 	at scalaz.Validation$class.bind(Validation.scala:137)
                                                  //| 	at scalaz.Success.bind(Validation.scala:304)
                                                  //| 	at almhirt.riftwarp.RiftWarp$class.receiveFromWarp(RiftWarp.scala:38)
                                                  //| 	at almhirt.riftwarp.RiftWarp$$anon$97.receiveFromWarp(RiftWarp.scala:57)
                                                  //| 
                                                  //| 	at almhirt.riftwarp.worksheets.Worksheet$$anonfun$main$1.apply$mcV$sp(al
                                                  //| mhirt.riftwarp.worksheets.Worksheet.scala:33)
                                                  //| 	at org.scalaide.worksheet.runtime.library.WorksheetSupport$$anonfun$$exe
                                                  //| cute$1.apply$mcV$sp(WorksheetSupport.scala:76)
                                                  //| 	at org.scalaide.worksheet.runtime.library.WorksheetSupport$.redirected(W
                                                  //| orksheetSupport.scala:65)
                                                  //| 	at org.scalaide.worksheet.runtime.library.WorksheetSupport$.$execute(Wor
                                                  //| ksheetSupport.scala:75)
                                                  //| 	at almhirt.riftwarp.worksheets.Worksheet$.main(almhirt.riftwarp.workshee
                                                  //| ts.Worksheet.scala:7)
                                                  //| 	at almhirt.riftwarp.worksheets.Worksheet.main(almhirt.riftwarp.worksheet
                                                  //| s.Worksheet.scala)
  
  val backFromWarp = backFromWarpV.forceResult
  
  testObject == backFromWarp
  
  
  
  riftWarp.prepareForWarp[DimensionRawMap](RiftMap())(testObject).bind(warpStream =>
    riftWarp.receiveFromWarp[DimensionRawMap, TestObjectA](RiftMap())(warpStream)).map(rearrived =>
      rearrived == testObject)


  riftWarp.prepareForWarp[DimensionCord](RiftJson())(testObject).bind(warpStream =>
    riftWarp.receiveFromWarp[DimensionCord, TestObjectA](RiftJson())(warpStream)).map(rearrived =>
      rearrived == testObject)
      
}