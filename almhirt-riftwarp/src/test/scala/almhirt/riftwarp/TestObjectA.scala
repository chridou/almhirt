package almhirt.riftwarp

import almhirt.common._
import almhirt.almvalidation.kit._
import scalaz._, Scalaz._
import almhirt.common.AlmValidation

case class TestObjectA(name: String, friend: Option[String], age: Int, address: Option[TestAddress]) extends HasDefaultTypeDescriptor

class TestObjectADecomposer extends Decomposer[TestObjectA] {
  val typeDescriptor = TypeDescriptor(classOf[TestObjectA])
  def decompose(what: TestObjectA)(implicit into: DematerializationFunnel): AlmValidation[DematerializationFunnel] = {
    into.addTypeDescriptor(typeDescriptor)
      .bind(_.addString("name", what.name))
      .bind(_.addOptionalString("friend", what.friend))
      .bind(_.addInt("age", what.age))
      .bind(_.addOptionalComplexType("address", what.address))
  }
}

class TestObjectARecomposer extends Recomposer[TestObjectA] {
  val typeDescriptor = TypeDescriptor(classOf[TestObjectA])
  def recompose(from: RematerializationArray): AlmValidation[TestObjectA] = {
    val name = from.getString("name").toAgg
    val friend = from.tryGetString("friend").toAgg
    val age = from.getInt("age").toAgg
    val address = from.tryGetComplexType("address").toAgg
    ( name |@| friend |@| age |@| address)(TestObjectA.apply)
  }
}

case class TestAddress(city: String, street: String) extends HasDefaultTypeDescriptor

class TestAddressDecomposer extends Decomposer[TestAddress] {
  val typeDescriptor = TypeDescriptor(classOf[TestAddress])
  def decompose(what: TestAddress)(implicit into: DematerializationFunnel): AlmValidation[DematerializationFunnel] = {
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
    ( city |@| street)(TestAddress.apply)
  }
}