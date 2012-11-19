package almhirt.riftwarp

import almhirt.common._
import almhirt.almvalidation.kit._
import scalaz._, Scalaz._
import almhirt.common.AlmValidation

case class TestObjectA(
  name: String,
  friend: Option[String],
  isMale: Boolean,
  age: Int,
  atoms: BigInt,
  balance: BigDecimal,
  size: Double,
  coins: Array[Byte],
  image: Array[Byte],
  address: Option[TestAddress]) extends HasDefaultTypeDescriptor

object TestObjectA {
  val pete: TestObjectA =
    TestObjectA(
      "Pete",
      Some("Jim"),
      true,
      47,
      BigInt("12737823792992474737892456985496456847789872389723984"),
      BigDecimal("99283823727372382.62253651576457645725428449249274974734798749465573"),
      12.5,
      Array[Byte](0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 255.toByte),
      Array[Byte](21, 169.toByte, 233.toByte, 0, 0, 0, 128.toByte, 128.toByte, 234.toByte),
      Some(TestAddress("Berlin", "An der Mauer 89")))
}

class TestObjectADecomposer extends Decomposer[TestObjectA] {
  val typeDescriptor = TypeDescriptor(classOf[TestObjectA])
  def decompose(what: TestObjectA)(implicit into: DematerializationFunnel): AlmValidation[DematerializationFunnel] = {
    into.addTypeDescriptor(typeDescriptor)
      .bind(_.addString("name", what.name))
      .bind(_.addOptionalString("friend", what.friend))
      .bind(_.addBoolean("isMale", what.isMale))
      .bind(_.addInt("age", what.age))
      .bind(_.addBigInt("atoms", what.atoms))
      .bind(_.addBigDecimal("balance", what.balance))
      .bind(_.addDouble("size", what.size))
      .bind(_.addByteArray("coins", what.coins))
      .bind(_.addBlob("image", what.image))
      .bind(_.addOptionalComplexType("address", what.address))
  }
}

class TestObjectARecomposer extends Recomposer[TestObjectA] {
  val typeDescriptor = TypeDescriptor(classOf[TestObjectA])
  def recompose(from: RematerializationArray): AlmValidation[TestObjectA] = {
    val name = from.getString("name").toAgg
    val friend = from.tryGetString("friend").toAgg
    val isMale = from.getBoolean("isMale").toAgg
    val age = from.getInt("age").toAgg
    val atoms = from.getBigInt("atoms").toAgg
    val balance = from.getBigDecimal("balance").toAgg
    val size = from.getDouble("size").toAgg
    val coins = from.getByteArray("coins").toAgg
    val image = from.getBlob("image").toAgg
    val address = from.tryGetComplexType("address").toAgg
    (name |@| friend |@| isMale |@| age |@| atoms |@| balance |@| size |@| coins |@| image |@| address)(TestObjectA.apply)
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
    (city |@| street)(TestAddress.apply)
  }
}