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
  val typeDescriptor = TypeDescriptor(classOf[TestObjectA])
  def decompose[TDimension <: RiftDimension](what: TestObjectA)(into: Dematerializer[TDimension]): AlmValidation[Dematerializer[TDimension]] = {
    into.addTypeDescriptor(typeDescriptor)
      .flatMap(_.addByteArray("arrayByte", what.arrayByte))
      .flatMap(_.addBlob("blob", what.blob))
      .flatMap(_.addComplexType("primitiveTypes", what.primitiveTypes))
      .flatMap(_.addComplexType("primitiveListMAs", what.primitiveListMAs))
      .flatMap(_.addComplexType("primitiveVectorMAs", what.primitiveVectorMAs))
      .flatMap(_.addOptionalComplexType("primitiveSetMAs", what.primitiveSetMAs))
      .flatMap(_.addComplexType("primitiveIterableMAs", what.primitiveIterableMAs))
      .flatMap(_.addComplexType("complexMAs", what.complexMAs))
      .flatMap(_.addComplexType("primitiveMaps", what.primitiveMaps))
      .flatMap(_.addComplexType("complexMaps", what.complexMaps))
      .flatMap(_.addOptionalComplexType("addressOpt", what.addressOpt))
  }
}

class TestObjectARecomposer extends Recomposer[TestObjectA] {
  val typeDescriptor = TypeDescriptor(classOf[TestObjectA])
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
    } yield TestObjectA(arrayByte,blob,primitiveTypes,primitiveListMAs,primitiveVectorMAs,primitiveSetMAs,primitiveIterableMAs,complexMAs,primitiveMaps, complexMaps,addressOpt)
  }
}

class PrimitiveTypesDecomposer extends Decomposer[PrimitiveTypes] {
  val typeDescriptor = TypeDescriptor(classOf[PrimitiveTypes])
  def decompose[TDimension <: RiftDimension](what: PrimitiveTypes)(into: Dematerializer[TDimension]): AlmValidation[Dematerializer[TDimension]] = {
    into.addTypeDescriptor(typeDescriptor)
      .flatMap(_.addString("str", what.str))
      .flatMap(_.addBoolean("bool", what.bool))
      .flatMap(_.addByte("byte", what.byte))
      .flatMap(_.addInt("int", what.int))
      .flatMap(_.addLong("long", what.long))
      .flatMap(_.addBigInt("bigInt", what.bigInt))
      .flatMap(_.addFloat("float", what.float))
      .flatMap(_.addDouble("double", what.double))
      .flatMap(_.addBigDecimal("bigDec", what.bigDec))
      .flatMap(_.addDateTime("dateTime", what.dateTime))
      .flatMap(_.addUuid("uuid", what.uuid))
  }
}

class PrimitiveTypesRecomposer extends Recomposer[PrimitiveTypes] {
  val typeDescriptor = TypeDescriptor(classOf[PrimitiveTypes])
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
  val typeDescriptor = TypeDescriptor(classOf[PrimitiveListMAs])
  def decompose[TDimension <: RiftDimension](what: PrimitiveListMAs)(into: Dematerializer[TDimension]): AlmValidation[Dematerializer[TDimension]] = {
    into.addTypeDescriptor(typeDescriptor)
      .flatMap(_.addPrimitiveMA("listString", what.listString))
      .flatMap(_.addPrimitiveMA("listInt", what.listInt))
      .flatMap(_.addPrimitiveMA("listDouble", what.listDouble))
      .flatMap(_.addPrimitiveMA("listBigDecimal", what.listBigDecimal))
      .flatMap(_.addPrimitiveMA("listDateTime", what.listDateTime))
  }
}

class PrimitiveListMAsRecomposer extends Recomposer[PrimitiveListMAs] {
  val typeDescriptor = TypeDescriptor(classOf[PrimitiveListMAs])
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
  val typeDescriptor = TypeDescriptor(classOf[PrimitiveVectorMAs])
  def decompose[TDimension <: RiftDimension](what: PrimitiveVectorMAs)(into: Dematerializer[TDimension]): AlmValidation[Dematerializer[TDimension]] = {
    into.addTypeDescriptor(typeDescriptor)
      .flatMap(_.addPrimitiveMA("vectorString", what.vectorString))
      .flatMap(_.addPrimitiveMA("vectorInt", what.vectorInt))
      .flatMap(_.addPrimitiveMA("vectorDouble", what.vectorDouble))
      .flatMap(_.addPrimitiveMA("vectorBigDecimal", what.vectorBigDecimal))
      .flatMap(_.addPrimitiveMA("vectorDateTime", what.vectorDateTime))
  }
}

class PrimitiveVectorMAsRecomposer extends Recomposer[PrimitiveVectorMAs] {
  val typeDescriptor = TypeDescriptor(classOf[PrimitiveVectorMAs])
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
  val typeDescriptor = TypeDescriptor(classOf[PrimitiveSetMAs])
  def decompose[TDimension <: RiftDimension](what: PrimitiveSetMAs)(into: Dematerializer[TDimension]): AlmValidation[Dematerializer[TDimension]] = {
    into.addTypeDescriptor(typeDescriptor)
      .flatMap(_.addPrimitiveMA("setString", what.setString))
      .flatMap(_.addPrimitiveMA("setInt", what.setInt))
      .flatMap(_.addPrimitiveMA("setDouble", what.setDouble))
      .flatMap(_.addPrimitiveMA("setBigDecimal", what.setBigDecimal))
      .flatMap(_.addOptionalPrimitiveMA("setDateTime", what.setDateTime))
  }
}

class PrimitiveSetMAsRecomposer extends Recomposer[PrimitiveSetMAs] {
  val typeDescriptor = TypeDescriptor(classOf[PrimitiveSetMAs])
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
  val typeDescriptor = TypeDescriptor(classOf[PrimitiveIterableMAs])
  def decompose[TDimension <: RiftDimension](what: PrimitiveIterableMAs)(into: Dematerializer[TDimension]): AlmValidation[Dematerializer[TDimension]] = {
    into.addTypeDescriptor(typeDescriptor)
      .flatMap(_.addPrimitiveMA("iterableString", what.iterableString))
      .flatMap(_.addPrimitiveMA("iterableInt", what.iterableInt))
      .flatMap(_.addPrimitiveMA("iterableDouble", what.iterableDouble))
      .flatMap(_.addPrimitiveMA("iterableBigDecimal", what.iterableBigDecimal))
      .flatMap(_.addPrimitiveMA("iterableDateTime", what.iterableDateTime))
  }
}

class PrimitiveIterableMAsRecomposer extends Recomposer[PrimitiveIterableMAs] {
  val typeDescriptor = TypeDescriptor(classOf[PrimitiveIterableMAs])
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
  val typeDescriptor = TypeDescriptor(classOf[ComplexMAs])
  def decompose[TDimension <: RiftDimension](what: ComplexMAs)(into: Dematerializer[TDimension]): AlmValidation[Dematerializer[TDimension]] = {
    into.addTypeDescriptor(typeDescriptor)
      .flatMap(_.addComplexMA(new TestAddressDecomposer())("addresses1", what.addresses1))
      .flatMap(_.addComplexMAFixed("addresses2", what.addresses2))
      .flatMap(_.addComplexMALoose("addresses3", what.addresses3))
      .flatMap(_.addMA("anything", what.anything))
  }
}

class ComplexMAsRecomposer extends Recomposer[ComplexMAs] {
  val typeDescriptor = TypeDescriptor(classOf[ComplexMAs])
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
  val typeDescriptor = TypeDescriptor(classOf[PrimitiveMaps])
  def decompose[TDimension <: RiftDimension](what: PrimitiveMaps)(into: Dematerializer[TDimension]): AlmValidation[Dematerializer[TDimension]] = {
    into.addTypeDescriptor(typeDescriptor)
      .flatMap(_.addPrimitiveMap("mapIntInt", what.mapIntInt))
      .flatMap(_.addPrimitiveMap("mapStringInt", what.mapStringInt))
      .flatMap(_.addPrimitiveMap("mapUuidDateTime", what.mapUuidDateTime))
  }
}

class PrimitiveMapsRecomposer extends Recomposer[PrimitiveMaps] {
  val typeDescriptor = TypeDescriptor(classOf[PrimitiveMaps])
  def recompose(from: Rematerializer): AlmValidation[PrimitiveMaps] = {
    val mapIntInt = from.getPrimitiveMap[Int, Int]("mapIntInt").toAgg
    val mapStringInt = from.getPrimitiveMap[String, Int]("mapStringInt").toAgg
    val mapUuidDateTime = from.getPrimitiveMap[UUID, DateTime]("mapUuidDateTime").toAgg
    (mapIntInt |@| mapStringInt |@| mapUuidDateTime)(PrimitiveMaps.apply)
  }
}
 
class ComplexMapsDecomposer extends Decomposer[ComplexMaps] {
  val typeDescriptor = TypeDescriptor(classOf[ComplexMaps])
  def decompose[TDimension <: RiftDimension](what: ComplexMaps)(into: Dematerializer[TDimension]): AlmValidation[Dematerializer[TDimension]] = {
    into.addTypeDescriptor(typeDescriptor)
      .flatMap(_.addComplexMapFixed("mapIntTestAddress1", what.mapIntTestAddress1))
      .flatMap(_.addMap("mapIntAny", what.mapIntAny))
      .flatMap(_.addMapSkippingUnknownValues("mapStringAnyWithUnknown", what.mapStringAnyWithUnknown))
  }
}

class ComplexMapsRecomposer extends Recomposer[ComplexMaps] {
  val typeDescriptor = TypeDescriptor(classOf[ComplexMaps])
  def recompose(from: Rematerializer): AlmValidation[ComplexMaps] = {
    val mapIntTestAddress1 = from.getComplexMapFixed[Int, TestAddress]("mapIntTestAddress1").toAgg
    val mapIntAny = from.getComplexMapLoose[Int, AnyRef]("mapIntAny").toAgg
    val mapStringAnyWithUnknown = from.getMap[String, Any]("mapStringAnyWithUnknown").toAgg
    (mapIntTestAddress1 |@| mapIntAny |@| mapStringAnyWithUnknown)(ComplexMaps.apply)
  }
}
class TestAddressDecomposer extends Decomposer[TestAddress] {
  val typeDescriptor = TypeDescriptor(classOf[TestAddress])
  def decompose[TDimension <: RiftDimension](what: TestAddress)(into: Dematerializer[TDimension]): AlmValidation[Dematerializer[TDimension]] = {
    into.addTypeDescriptor(typeDescriptor)
      .flatMap(_.addString("city", what.city))
      .flatMap(_.addString("street", what.street))
  }
}

class TestAddressRecomposer extends Recomposer[TestAddress] {
  val typeDescriptor = TypeDescriptor(classOf[TestAddress])
  def recompose(from: Rematerializer): AlmValidation[TestAddress] = {
    val city = from.getString("city").toAgg
    val street = from.getString("street").toAgg
    (city |@| street)(TestAddress.apply)
  }
}