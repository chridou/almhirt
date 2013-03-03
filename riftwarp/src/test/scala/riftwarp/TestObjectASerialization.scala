package riftwarp

import almhirt.common._
import almhirt.almvalidation.kit._
import scalaz._, Scalaz._
import almhirt.common.AlmValidation
import java.util.UUID
import org.joda.time.DateTime
import riftwarp.components._
import riftwarp.components._
import riftwarp.components._

class TestObjectADecomposer extends Decomposer[TestObjectA] {
  val riftDescriptor = RiftDescriptor(classOf[TestObjectA])
  val alternativeRiftDescriptors = Nil
  def decompose[TDimension <: RiftDimension](what: TestObjectA, into: WarpSequencer[TDimension]): AlmValidation[WarpSequencer[TDimension]] = {
    into
      .addRiftDescriptor(riftDescriptor)
      .addByteArray("arrayByte", what.arrayByte)
      .addBlob("blob", what.blob)
      .flatMap(_.addComplex("primitiveTypes", what.primitiveTypes, None))
      .flatMap(_.addComplex("primitiveListMAs", what.primitiveListMAs, None))
      .flatMap(_.addComplex("primitiveVectorMAs", what.primitiveVectorMAs, None))
      .flatMap(_.addOptionalComplex("primitiveSetMAs", what.primitiveSetMAs, None))
      .flatMap(_.addComplex("primitiveIterableMAs", what.primitiveIterableMAs, None))
      .flatMap(_.addComplex("complexMAs", what.complexMAs, None))
      .flatMap(_.addComplex("primitiveMaps", what.primitiveMaps, None))
      .flatMap(_.addComplex("complexMaps", what.complexMaps, None))
      .flatMap(_.addOptionalComplex("addressOpt", what.addressOpt, None))
  }
}

class TestObjectARecomposer extends Recomposer[TestObjectA] {
  val riftDescriptor = RiftDescriptor(classOf[TestObjectA])
  val alternativeRiftDescriptors = Nil
  def recompose(from: Rematerializer): AlmValidation[TestObjectA] = {
    for {
      arrayByte <- from.getByteArray("arrayByte")
      blob <- from.getBlob("blob")
      primitiveTypes <- from.getComplexType[PrimitiveTypes]("primitiveTypes")
      primitiveListMAs <- from.getComplexType[PrimitiveListMAs]("primitiveListMAs")
      primitiveVectorMAs <- from.getComplexType[PrimitiveVectorMAs]("primitiveVectorMAs")
      primitiveSetMAs <- from.tryGetComplexType[PrimitiveSetMAs]("primitiveSetMAs")
      primitiveIterableMAs <- from.getComplexType[PrimitiveIterableMAs]("primitiveIterableMAs")
      complexMAs <- from.getComplexType[ComplexMAs]("complexMAs")
      primitiveMaps <- from.getComplexType[PrimitiveMaps]("primitiveMaps")
      complexMaps <- from.getComplexType[ComplexMaps]("complexMaps")
      addressOpt <- from.tryGetComplexType[TestAddress]("addressOpt")
    } yield TestObjectA(arrayByte, blob, primitiveTypes, primitiveListMAs, primitiveVectorMAs, primitiveSetMAs, primitiveIterableMAs, complexMAs, primitiveMaps, complexMaps, addressOpt)
  }
}

class PrimitiveTypesDecomposer extends Decomposer[PrimitiveTypes] {
  val riftDescriptor = RiftDescriptor(classOf[PrimitiveTypes])
  val alternativeRiftDescriptors = Nil
  def decompose[TDimension <: RiftDimension](what: PrimitiveTypes, into: WarpSequencer[TDimension]): AlmValidation[WarpSequencer[TDimension]] = {
    into.addRiftDescriptor(riftDescriptor)
      .addString("str", what.str)
      .addBoolean("bool", what.bool)
      .addByte("byte", what.byte)
      .addInt("int", what.int)
      .addLong("long", what.long)
      .addBigInt("bigInt", what.bigInt)
      .addFloat("float", what.float)
      .addDouble("double", what.double)
      .addBigDecimal("bigDec", what.bigDec)
      .addDateTime("dateTime", what.dateTime)
      .addUuid("uuid", what.uuid).ok
  }
}

class PrimitiveTypesRecomposer extends Recomposer[PrimitiveTypes] {
  val riftDescriptor = RiftDescriptor(classOf[PrimitiveTypes])
  val alternativeRiftDescriptors = Nil
  def recompose(from: Rematerializer): AlmValidation[PrimitiveTypes] = {
    val str = from.getString("str").toAgg
    val bool = from.getBoolean("bool").toAgg
    val byte = from.getByte("byte").toAgg
    val int = from.getInt("int").toAgg
    val long = from.getLong("long").toAgg
    val bigInt = from.getBigInt("bigInt").toAgg
    val float = from.getFloat("float").toAgg
    val double = from.getDouble("double").toAgg
    val bigDec = from.getBigDecimal("bigDec").toAgg
    val dateTime = from.getDateTime("dateTime").toAgg
    val uuid = from.getUuid("uuid").toAgg
    (str
      |@| bool
      |@| byte
      |@| int
      |@| long
      |@| bigInt
      |@| float
      |@| double
      |@| bigDec
      |@| dateTime
      |@| uuid)(PrimitiveTypes.apply)
  }
}

class PrimitiveListMAsDecomposer extends Decomposer[PrimitiveListMAs] {
  val riftDescriptor = RiftDescriptor(classOf[PrimitiveListMAs])
  val alternativeRiftDescriptors = Nil
  def decompose[TDimension <: RiftDimension](what: PrimitiveListMAs, into: WarpSequencer[TDimension]): AlmValidation[WarpSequencer[TDimension]] = {
    into
      .addRiftDescriptor(riftDescriptor)
      .addIterableOfPrimitives("listString", what.listString)
      .flatMap(_.addIterableOfPrimitives("listInt", what.listInt))
      .flatMap(_.addIterableOfPrimitives("listDouble", what.listDouble))
      .flatMap(_.addIterableOfPrimitives("listBigDecimal", what.listBigDecimal))
      .flatMap(_.addIterableOfPrimitives("listDateTime", what.listDateTime))
  }
}

class PrimitiveListMAsRecomposer extends Recomposer[PrimitiveListMAs] {
  val riftDescriptor = RiftDescriptor(classOf[PrimitiveListMAs])
  val alternativeRiftDescriptors = Nil
  def recompose(from: Rematerializer): AlmValidation[PrimitiveListMAs] = {
    val listString = from.getPrimitiveMA[List, String]("listString").toAgg
    val listInt = from.getPrimitiveMA[List, Int]("listInt").toAgg
    val listDouble = from.getPrimitiveMA[List, Double]("listDouble").toAgg
    val listBigDecimal = from.getPrimitiveMA[List, BigDecimal]("listBigDecimal").toAgg
    val listDateTime = from.getPrimitiveMA[List, DateTime]("listDateTime").toAgg
    (listString
      |@| listInt
      |@| listDouble
      |@| listBigDecimal
      |@| listDateTime)(PrimitiveListMAs.apply)
  }
}

class PrimitiveVectorMAsDecomposer extends Decomposer[PrimitiveVectorMAs] {
  val riftDescriptor = RiftDescriptor(classOf[PrimitiveVectorMAs])
  val alternativeRiftDescriptors = Nil
  def decompose[TDimension <: RiftDimension](what: PrimitiveVectorMAs, into: WarpSequencer[TDimension]): AlmValidation[WarpSequencer[TDimension]] = {
    into.addRiftDescriptor(riftDescriptor)
      .addIterableOfPrimitives("vectorString", what.vectorString)
      .flatMap(_.addIterableOfPrimitives("vectorInt", what.vectorInt))
      .flatMap(_.addIterableOfPrimitives("vectorDouble", what.vectorDouble))
      .flatMap(_.addIterableOfPrimitives("vectorBigDecimal", what.vectorBigDecimal))
      .flatMap(_.addIterableOfPrimitives("vectorDateTime", what.vectorDateTime))
  }
}

class PrimitiveVectorMAsRecomposer extends Recomposer[PrimitiveVectorMAs] {
  val riftDescriptor = RiftDescriptor(classOf[PrimitiveVectorMAs])
  val alternativeRiftDescriptors = Nil
  def recompose(from: Rematerializer): AlmValidation[PrimitiveVectorMAs] = {

    val vectorString = from.getPrimitiveMA[Vector, String]("vectorString").toAgg
    val vectorInt = from.getPrimitiveMA[Vector, Int]("vectorInt").toAgg
    val vectorDouble = from.getPrimitiveMA[Vector, Double]("vectorDouble").toAgg
    val vectorBigDecimal = from.getPrimitiveMA[Vector, BigDecimal]("vectorBigDecimal").toAgg
    val vectorDateTime = from.getPrimitiveMA[Vector, DateTime]("vectorDateTime").toAgg
    (vectorString
      |@| vectorInt
      |@| vectorDouble
      |@| vectorBigDecimal
      |@| vectorDateTime)(PrimitiveVectorMAs.apply)
  }
}

class PrimitiveSetMAsDecomposer extends Decomposer[PrimitiveSetMAs] {
  val riftDescriptor = RiftDescriptor(classOf[PrimitiveSetMAs])
  val alternativeRiftDescriptors = Nil
  def decompose[TDimension <: RiftDimension](what: PrimitiveSetMAs, into: WarpSequencer[TDimension]): AlmValidation[WarpSequencer[TDimension]] = {
    into.addRiftDescriptor(riftDescriptor)
      .addIterableOfPrimitives("setString", what.setString)
      .flatMap(_.addIterableOfPrimitives("setInt", what.setInt))
      .flatMap(_.addIterableOfPrimitives("setDouble", what.setDouble))
      .flatMap(_.addIterableOfPrimitives("setBigDecimal", what.setBigDecimal))
      .flatMap(_.addOptionalIterableOfPrimitives("setDateTime", what.setDateTime))
  }
}

class PrimitiveSetMAsRecomposer extends Recomposer[PrimitiveSetMAs] {
  val riftDescriptor = RiftDescriptor(classOf[PrimitiveSetMAs])
  val alternativeRiftDescriptors = Nil
  def recompose(from: Rematerializer): AlmValidation[PrimitiveSetMAs] = {

    val setString = from.getPrimitiveMA[Set, String]("setString").toAgg
    val setInt = from.getPrimitiveMA[Set, Int]("setInt").toAgg
    val setDouble = from.getPrimitiveMA[Set, Double]("setDouble").toAgg
    val setBigDecimal = from.getPrimitiveMA[Set, BigDecimal]("setBigDecimal").toAgg
    val setDateTime = from.tryGetPrimitiveMA[Set, DateTime]("setDateTime").toAgg
    (setString
      |@| setInt
      |@| setDouble
      |@| setBigDecimal
      |@| setDateTime)(PrimitiveSetMAs.apply)
  }
}

class PrimitiveIterableMAsDecomposer extends Decomposer[PrimitiveIterableMAs] {
  val riftDescriptor = RiftDescriptor(classOf[PrimitiveIterableMAs])
  val alternativeRiftDescriptors = Nil
  def decompose[TDimension <: RiftDimension](what: PrimitiveIterableMAs, into: WarpSequencer[TDimension]): AlmValidation[WarpSequencer[TDimension]] = {
    into.addRiftDescriptor(riftDescriptor)
      .addIterableOfPrimitives("iterableString", what.iterableString)
      .flatMap(_.addIterableOfPrimitives("iterableInt", what.iterableInt))
      .flatMap(_.addIterableOfPrimitives("iterableDouble", what.iterableDouble))
      .flatMap(_.addIterableOfPrimitives("iterableBigDecimal", what.iterableBigDecimal))
      .flatMap(_.addIterableOfPrimitives("iterableDateTime", what.iterableDateTime))
  }
}

class PrimitiveIterableMAsRecomposer extends Recomposer[PrimitiveIterableMAs] {
  val riftDescriptor = RiftDescriptor(classOf[PrimitiveIterableMAs])
  val alternativeRiftDescriptors = Nil
  def recompose(from: Rematerializer): AlmValidation[PrimitiveIterableMAs] = {

    val iterableString = from.getPrimitiveMA[Set, String]("iterableString").toAgg
    val iterableInt = from.getPrimitiveMA[Set, Int]("iterableInt").toAgg
    val iterableDouble = from.getPrimitiveMA[Set, Double]("iterableDouble").toAgg
    val iterableBigDecimal = from.getPrimitiveMA[Set, BigDecimal]("iterableBigDecimal").toAgg
    val iterableDateTime = from.getPrimitiveMA[Set, DateTime]("iterableDateTime").toAgg
    (iterableString
      |@| iterableInt
      |@| iterableDouble
      |@| iterableBigDecimal
      |@| iterableDateTime)(PrimitiveIterableMAs.apply)
  }
}

class ComplexMAsDecomposer extends Decomposer[ComplexMAs] {
  val riftDescriptor = RiftDescriptor(classOf[ComplexMAs])
  val alternativeRiftDescriptors = Nil
  def decompose[TDimension <: RiftDimension](what: ComplexMAs, into: WarpSequencer[TDimension]): AlmValidation[WarpSequencer[TDimension]] = {
    into.addRiftDescriptor(riftDescriptor)
      .addIterableAllWith("addresses1", what.addresses1, new TestAddressDecomposer())
      .flatMap(_.addIterableStrict("addresses2", what.addresses2, None))
      .flatMap(_.addIterableOfComplex("addresses3", what.addresses3, None))
      .flatMap(_.addIterable("anything", what.anything, None))
  }
}

class ComplexMAsRecomposer extends Recomposer[ComplexMAs] {
  val riftDescriptor = RiftDescriptor(classOf[ComplexMAs])
  val alternativeRiftDescriptors = Nil
  def recompose(from: Rematerializer): AlmValidation[ComplexMAs] = {
    val addresses1 = from.getComplexMALoose[List, TestAddress]("addresses1").toAgg
    val addresses2 = from.getComplexMALoose[Vector, TestAddress]("addresses2").toAgg
    val addresses3 = from.getComplexMALoose[Set, TestAddress]("addresses3").toAgg
    val anything = from.getMA[Iterable, Any]("anything").toAgg
    (addresses1
      |@| addresses2
      |@| addresses3
      |@| anything)(ComplexMAs(_, _, _, _))
  }
}

class PrimitiveMapsDecomposer extends Decomposer[PrimitiveMaps] {
  val riftDescriptor = RiftDescriptor(classOf[PrimitiveMaps])
  val alternativeRiftDescriptors = Nil
  def decompose[TDimension <: RiftDimension](what: PrimitiveMaps, into: WarpSequencer[TDimension]): AlmValidation[WarpSequencer[TDimension]] = {
    into.addRiftDescriptor(riftDescriptor)
      .addMapOfPrimitives("mapIntInt", what.mapIntInt)
      .flatMap(_.addMapOfPrimitives("mapStringInt", what.mapStringInt))
      .flatMap(_.addMapOfPrimitives("mapUuidDateTime", what.mapUuidDateTime))
  }
}

class PrimitiveMapsRecomposer extends Recomposer[PrimitiveMaps] {
  val riftDescriptor = RiftDescriptor(classOf[PrimitiveMaps])
  val alternativeRiftDescriptors = Nil
  def recompose(from: Rematerializer): AlmValidation[PrimitiveMaps] = {
    val mapIntInt = from.getPrimitiveMap[Int, Int]("mapIntInt").toAgg
    val mapStringInt = from.getPrimitiveMap[String, Int]("mapStringInt").toAgg
    val mapUuidDateTime = from.getPrimitiveMap[UUID, DateTime]("mapUuidDateTime").toAgg
    (mapIntInt |@| mapStringInt |@| mapUuidDateTime)(PrimitiveMaps.apply)
  }
}

class ComplexMapsDecomposer extends Decomposer[ComplexMaps] {
  val riftDescriptor = RiftDescriptor(classOf[ComplexMaps])
  val alternativeRiftDescriptors = Nil
  def decompose[TDimension <: RiftDimension](what: ComplexMaps, into: WarpSequencer[TDimension]): AlmValidation[WarpSequencer[TDimension]] = {
    into.addRiftDescriptor(riftDescriptor)
      .addMapOfComplex("mapIntTestAddress1", what.mapIntTestAddress1, None)
      .flatMap(_.addMap("mapIntAny", what.mapIntAny, None))
      .flatMap(_.addMapLiberate("mapStringAnyWithUnknown", what.mapStringAnyWithUnknown, None))
  }
}

class ComplexMapsRecomposer extends Recomposer[ComplexMaps] {
  val riftDescriptor = RiftDescriptor(classOf[ComplexMaps])
  val alternativeRiftDescriptors = Nil
  def recompose(from: Rematerializer): AlmValidation[ComplexMaps] = {
    val mapIntTestAddress1 = from.getComplexMapFixed[Int, TestAddress]("mapIntTestAddress1").toAgg
    val mapIntAny = from.getComplexMapLoose[Int, AnyRef]("mapIntAny").toAgg
    val mapStringAnyWithUnknown = from.getMap[String, Any]("mapStringAnyWithUnknown").toAgg
    (mapIntTestAddress1 |@| mapIntAny |@| mapStringAnyWithUnknown)(ComplexMaps.apply)
  }
}
class TestAddressDecomposer extends Decomposer[TestAddress] {
  val riftDescriptor = RiftDescriptor(classOf[TestAddress])
  val alternativeRiftDescriptors = Nil
  def decompose[TDimension <: RiftDimension](what: TestAddress, into: WarpSequencer[TDimension]): AlmValidation[WarpSequencer[TDimension]] = {
    into.addRiftDescriptor(riftDescriptor)
      .addString("city", what.city)
      .addString("street", what.street).ok
  }
}

class TestAddressRecomposer extends Recomposer[TestAddress] {
  val riftDescriptor = RiftDescriptor(classOf[TestAddress])
  val alternativeRiftDescriptors = Nil
  def recompose(from: Rematerializer): AlmValidation[TestAddress] = {
    val city = from.getString("city").toAgg
    val street = from.getString("street").toAgg
    (city |@| street)(TestAddress.apply)
  }
}