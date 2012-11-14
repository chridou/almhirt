package almhirt.riftwarp

case class TestObjectA(name: String, friend: Option[String], age: Int) extends HasDefaultTypeDescriptor

import almhirt.common._
import almhirt.almvalidation.kit._
import scalaz._, Scalaz._

class TestObjectADecomposer extends Decomposer[TestObjectA] {
  val typeDescriptor = TypeDescriptor(classOf[TestObjectA])
  def decompose(what: TestObjectA)(implicit into: DematerializationFunnel): AlmValidation[DematerializationFunnel] = {
    into.addTypeDescriptor(typeDescriptor)
      .bind(_.addString("name", what.name))
      .bind(_.addOptionalString("friend", what.friend))
      .bind(_.addInt("age", what.age))
  }
}

class TestObjectARecomposer extends Recomposer[TestObjectA] {
  val typeDescriptor = TypeDescriptor(classOf[TestObjectA])
  def recompose(from: RematerializationArray): AlmValidation[TestObjectA] = {
    val name = from.getString("name").toAgg
    val friend = from.tryGetString("friend").toAgg
    val age = from.getInt("age").toAgg
    ( name |@| friend |@| age)(TestObjectA.apply)
  }
}