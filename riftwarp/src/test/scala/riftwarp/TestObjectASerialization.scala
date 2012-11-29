package riftwarp

import almhirt.common._
import almhirt.almvalidation.kit._
import scalaz._, Scalaz._
import almhirt.common.AlmValidation
import java.util.UUID
import org.joda.time.DateTime

class TestObjectADecomposer extends Decomposer[TestObjectA] {
  val typeDescriptor = TypeDescriptor(classOf[TestObjectA])
  def decompose[TDimension <: RiftDimension](what: TestObjectA)(implicit into: Dematerializer[TDimension]): AlmValidation[Dematerializer[TDimension]] = {
    into.addTypeDescriptor(typeDescriptor)
      .bind(_.addByteArray("arrayByte", what.arrayByte))
      .bind(_.addBlob("blob", what.blob))
      .bind(_.addComplexType("primitiveTypes", what.primitiveTypes))
      .bind(_.addComplexType("primitiveListMAs", what.primitiveListMAs))
      .bind(_.addComplexType("primitiveVectorMAs", what.primitiveVectorMAs))
      .bind(_.addOptionalComplexType("primitiveSetMAs", what.primitiveSetMAs))
      .bind(_.addComplexType("primitiveIterableMAs", what.primitiveIterableMAs))
      .bind(_.addComplexType("complexMAs", what.complexMAs))
      .bind(_.addOptionalComplexType("addressOpt", what.addressOpt))
  }
}

class TestObjectARecomposer extends Recomposer[TestObjectA] {
  val typeDescriptor = TypeDescriptor(classOf[TestObjectA])
  def recompose(from: RematerializationArray): AlmValidation[TestObjectA] = {
    val arrayByte = from.getByteArray("arrayByte").toAgg
    val blob = from.getBlob("blob").toAgg
    val primitiveTypes = from.getComplexType[PrimitiveTypes]("primitiveTypes").toAgg
    val primitiveListMAs = from.getComplexType[PrimitiveListMAs]("primitiveListMAs").toAgg
    val primitiveVectorMAs = from.getComplexType[PrimitiveVectorMAs]("primitiveVectorMAs").toAgg
    val primitiveSetMAs = from.tryGetComplexType[PrimitiveSetMAs]("primitiveSetMAs").toAgg
    val primitiveIterableMAs = from.getComplexType[PrimitiveIterableMAs]("primitiveIterableMAs").toAgg
    val complexMAs = from.getComplexType[ComplexMAs]("complexMAs").toAgg
    val addressOpt = from.tryGetComplexType[TestAddress]("addressOpt").toAgg
    (arrayByte
      |@| blob
      |@| primitiveTypes
      |@| primitiveListMAs
      |@| primitiveVectorMAs
      |@| primitiveSetMAs
      |@| primitiveIterableMAs
      |@| complexMAs
      |@| addressOpt)(TestObjectA.apply)
  }
}

class PrimitiveTypesDecomposer extends Decomposer[PrimitiveTypes] {
  val typeDescriptor = TypeDescriptor(classOf[PrimitiveTypes])
  def decompose[TDimension <: RiftDimension](what: PrimitiveTypes)(implicit into: Dematerializer[TDimension]): AlmValidation[Dematerializer[TDimension]] = {
    into.addTypeDescriptor(typeDescriptor)
      .bind(_.addString("str", what.str))
      .bind(_.addBoolean("bool", what.bool))
      .bind(_.addByte("byte", what.byte))
      .bind(_.addInt("int", what.int))
      .bind(_.addLong("long", what.long))
      .bind(_.addBigInt("bigInt", what.bigInt))
      .bind(_.addFloat("float", what.float))
      .bind(_.addDouble("double", what.double))
      .bind(_.addBigDecimal("bigDec", what.bigDec))
      .bind(_.addDateTime("dateTime", what.dateTime))
      .bind(_.addUuid("uuid", what.uuid))
  }
}

class PrimitiveTypesRecomposer extends Recomposer[PrimitiveTypes] {
  val typeDescriptor = TypeDescriptor(classOf[PrimitiveTypes])
  def recompose(from: RematerializationArray): AlmValidation[PrimitiveTypes] = {
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
  def decompose[TDimension <: RiftDimension](what: PrimitiveListMAs)(implicit into: Dematerializer[TDimension]): AlmValidation[Dematerializer[TDimension]] = {
    into.addTypeDescriptor(typeDescriptor)
      .bind(_.addPrimitiveMA("listString", what.listString))
      .bind(_.addPrimitiveMA("listInt", what.listInt))
      .bind(_.addPrimitiveMA("listDouble", what.listDouble))
      .bind(_.addPrimitiveMA("listBigDecimal", what.listBigDecimal))
      .bind(_.addPrimitiveMA("listDateTime", what.listDateTime))
  }
}

class PrimitiveListMAsRecomposer extends Recomposer[PrimitiveListMAs] {
  val typeDescriptor = TypeDescriptor(classOf[PrimitiveListMAs])
  def recompose(from: RematerializationArray): AlmValidation[PrimitiveListMAs] = {
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
  def decompose[TDimension <: RiftDimension](what: PrimitiveVectorMAs)(implicit into: Dematerializer[TDimension]): AlmValidation[Dematerializer[TDimension]] = {
    into.addTypeDescriptor(typeDescriptor)
      .bind(_.addPrimitiveMA("vectorString", what.vectorString))
      .bind(_.addPrimitiveMA("vectorInt", what.vectorInt))
      .bind(_.addPrimitiveMA("vectorDouble", what.vectorDouble))
      .bind(_.addPrimitiveMA("vectorBigDecimal", what.vectorBigDecimal))
      .bind(_.addPrimitiveMA("vectorDateTime", what.vectorDateTime))
  }
}

class PrimitiveVectorMAsRecomposer extends Recomposer[PrimitiveVectorMAs] {
  val typeDescriptor = TypeDescriptor(classOf[PrimitiveVectorMAs])
  def recompose(from: RematerializationArray): AlmValidation[PrimitiveVectorMAs] = {

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
  def decompose[TDimension <: RiftDimension](what: PrimitiveSetMAs)(implicit into: Dematerializer[TDimension]): AlmValidation[Dematerializer[TDimension]] = {
    into.addTypeDescriptor(typeDescriptor)
      .bind(_.addPrimitiveMA("setString", what.setString))
      .bind(_.addPrimitiveMA("setInt", what.setInt))
      .bind(_.addPrimitiveMA("setDouble", what.setDouble))
      .bind(_.addPrimitiveMA("setBigDecimal", what.setBigDecimal))
      .bind(_.addOptionalPrimitiveMA("setDateTime", what.setDateTime))
  }
}

class PrimitiveSetMAsRecomposer extends Recomposer[PrimitiveSetMAs] {
  val typeDescriptor = TypeDescriptor(classOf[PrimitiveSetMAs])
  def recompose(from: RematerializationArray): AlmValidation[PrimitiveSetMAs] = {

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
  def decompose[TDimension <: RiftDimension](what: PrimitiveIterableMAs)(implicit into: Dematerializer[TDimension]): AlmValidation[Dematerializer[TDimension]] = {
    into.addTypeDescriptor(typeDescriptor)

      .bind(_.addPrimitiveMA("iterableString", what.iterableString))
      .bind(_.addPrimitiveMA("iterableInt", what.iterableInt))
      .bind(_.addPrimitiveMA("iterableDouble", what.iterableDouble))
      .bind(_.addPrimitiveMA("iterableBigDecimal", what.iterableBigDecimal))
      .bind(_.addPrimitiveMA("iterableDateTime", what.iterableDateTime))
  }
}

class PrimitiveIterableMAsRecomposer extends Recomposer[PrimitiveIterableMAs] {
  val typeDescriptor = TypeDescriptor(classOf[PrimitiveIterableMAs])
  def recompose(from: RematerializationArray): AlmValidation[PrimitiveIterableMAs] = {

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
  def decompose[TDimension <: RiftDimension](what: ComplexMAs)(implicit into: Dematerializer[TDimension]): AlmValidation[Dematerializer[TDimension]] = {
    into.addTypeDescriptor(typeDescriptor)
      .bind(_.addComplexMA(new TestAddressDecomposer())("addresses1", what.addresses1))
      .bind(_.addComplexMAFixed("addresses2", what.addresses2))
      .bind(_.addComplexMALoose("addresses3", what.addresses3))
      .bind(_.addMA("anything", what.anything))
  }
}

class ComplexMAsRecomposer extends Recomposer[ComplexMAs] {
  val typeDescriptor = TypeDescriptor(classOf[ComplexMAs])
  def recompose(from: RematerializationArray): AlmValidation[ComplexMAs] = {
    val addresses1 = from.getComplexMALoose[List, TestAddress]("addresses1").toAgg
    val addresses2 = from.getComplexMALoose[Vector, TestAddress]("addresses2").toAgg
    val addresses3 = from.getComplexMALoose[Set, TestAddress]("addresses3").toAgg
   //(addresses1).map(ComplexMAs(List.empty))
    (addresses1
      |@| addresses2
      |@| addresses3)(ComplexMAs(_, _, _, Iterable.empty))
  }
}

class TestAddressDecomposer extends Decomposer[TestAddress] {
  val typeDescriptor = TypeDescriptor(classOf[TestAddress])
  def decompose[TDimension <: RiftDimension](what: TestAddress)(implicit into: Dematerializer[TDimension]): AlmValidation[Dematerializer[TDimension]] = {
    into.addTypeDescriptor(typeDescriptor)
      .bind(_.addString("city", what.city))
      .bind(_.addString("street", what.street))
  }
}

class TestAddressRecomposer extends Recomposer[TestAddress] {
  val typeDescriptor = TypeDescriptor(classOf[TestAddress])
  def recompose(from: RematerializationArray): AlmValidation[TestAddress] = {
    val city = from.getString("city").toAgg
    val street = from.getString("street").toAgg
    (city |@| street)(TestAddress.apply)
  }
}