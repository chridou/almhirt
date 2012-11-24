package almhirt.riftwarp


import almhirt.common._
import almhirt.almvalidation.kit._
import scalaz._, Scalaz._
import almhirt.common.AlmValidation
import java.util.UUID
import org.joda.time.DateTime

class TestObjectADecomposer extends Decomposer[TestObjectA] {
  val typeDescriptor = TypeDescriptor(classOf[TestObjectA])
  def decompose[TChannel <: RiftChannelDescriptor, TDimension <: RiftTypedDimension[_]](what: TestObjectA)(implicit into: Dematerializer[TChannel, TDimension]): AlmValidation[Dematerializer[TChannel, TDimension]] = {
    into.addTypeDescriptor(typeDescriptor)
      .bind(_.addString("str", what.str))
      .bind(_.addOptionalString("strOpt", what.strOpt))
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
      .bind(_.addByteArray("arrayByte", what.arrayByte))
      .bind(_.addBlob("blob", what.blob))
      .bind(_.addComplexType("primitiveMAs", what.primitiveMAs))
      .bind(_.addOptionalComplexType("addressOpt", what.addressOpt))
  }
}

class TestObjectARecomposer extends Recomposer[TestObjectA] {
  val typeDescriptor = TypeDescriptor(classOf[TestObjectA])
  def recompose(from: RematerializationArray): AlmValidation[TestObjectA] = {
    UnspecifiedProblem("Not implemented").failure
  }
}

class PrimitiveMAsDecomposer extends Decomposer[PrimitiveMAs] {
  val typeDescriptor = TypeDescriptor(classOf[PrimitiveMAs])
  def decompose[TChannel <: RiftChannelDescriptor, TDimension <: RiftTypedDimension[_]](what: PrimitiveMAs)(implicit into: Dematerializer[TChannel, TDimension]): AlmValidation[Dematerializer[TChannel, TDimension]] = {
    into.addTypeDescriptor(typeDescriptor)
      .bind(_.addPrimitiveMA("listString", what.listString))
      .bind(_.addPrimitiveMA("listInt", what.listInt))
      .bind(_.addPrimitiveMA("listDouble", what.listDouble))
      .bind(_.addPrimitiveMA("listBigDecimal", what.listBigDecimal))
      .bind(_.addPrimitiveMA("listDateTime", what.listDateTime))

      .bind(_.addPrimitiveMA("vectorString", what.vectorString))
      .bind(_.addPrimitiveMA("vectorInt", what.vectorInt))
      .bind(_.addPrimitiveMA("vectorDouble", what.vectorDouble))
      .bind(_.addPrimitiveMA("vectorBigDecimal", what.vectorBigDecimal))
      .bind(_.addPrimitiveMA("vectorDateTime", what.vectorDateTime))

      .bind(_.addPrimitiveMA("setString", what.setString))
      .bind(_.addPrimitiveMA("setInt", what.setInt))
      .bind(_.addPrimitiveMA("setDouble", what.setDouble))
      .bind(_.addPrimitiveMA("setBigDecimal", what.setBigDecimal))
      .bind(_.addPrimitiveMA("setDateTime", what.setDateTime))

      .bind(_.addPrimitiveMA("iterableString", what.iterableString))
      .bind(_.addPrimitiveMA("iterableInt", what.iterableInt))
      .bind(_.addPrimitiveMA("iterableDouble", what.iterableDouble))
      .bind(_.addPrimitiveMA("iterableBigDecimal", what.iterableBigDecimal))
      .bind(_.addPrimitiveMA("iterableDateTime", what.iterableDateTime))
  }
}


case class TestAddress(city: String, street: String) extends HasDefaultTypeDescriptor

class TestAddressDecomposer extends Decomposer[TestAddress] {
  val typeDescriptor = TypeDescriptor(classOf[TestAddress])
  def decompose[TChannel <: RiftChannelDescriptor, TDimension <: RiftTypedDimension[_]](what: TestAddress)(implicit into: Dematerializer[TChannel, TDimension]): AlmValidation[Dematerializer[TChannel, TDimension]] = {
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