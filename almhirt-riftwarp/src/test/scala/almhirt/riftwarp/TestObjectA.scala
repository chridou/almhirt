package almhirt.riftwarp

case class TestObjectA(name: String, friend: Option[String], age: Int) extends HasDefaultDescriptor

import almhirt.common._
class TestObjectADecomposer extends Decomposer[TestObjectA] {
  val typeDescriptor = TypeDescriptor(classOf[TestObjectA])
  def decompose(what: TestObjectA)(implicit into: Dematerializer): AlmValidation[Dematerializer] = {
    into.addTypeDescriptor(typeDescriptor)
      .bind(_.addString("name", what.name))
      .bind(_.addString("friend", what.friend))
      .bind(_.addInt("age", what.age))
  }
}

//class TestObjectARecomposer extends Recomposer[TestObjectA] {
//  val typeDescriptor = classOf[TestObjectA].getName()
//  def recompose(what: TestObjectA)(implicit into: Dematerializer): AlmValidation[Dematerializer] = {
//    into.addTypeDescriptor(typeDescriptor)
//      .bind(_.addString("name", what.name))
//      .bind(_.addString("friend", what.friend))
//      .bind(_.addInt("age", what.age))
//  }
//}