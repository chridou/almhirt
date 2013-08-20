package riftwarp.templating

import shapeless._

trait TemplatePart[U] { self =>
  type Repr
  def label: String
  def getValue: U => Repr
}

object TemplatePart {
  def combine[L <: TemplatePart[U], R <: TemplatePart[U], U](l: L, r: R): Template[L :: R :: HNil, U] =
    new Template[L :: R :: HNil, U] {
      val hlist = l :: r :: HNil
    }
    
}

trait TemplateCombinator[T <: TemplatePart[U], U] { self : T =>
  def ~[V <: TemplatePart[U]](other: V): Template[T :: V :: HNil, U] =
    TemplatePart.combine(self, other)
  def combine[V <: TemplatePart[U]](other: V): Template[T :: V :: HNil, U] = 
    this.~[V](other)
}

trait StringPart[U] extends TemplatePart[U] with TemplateCombinator[StringPart[U], U] { self =>
  type Repr = String
  def getValue: U => String
}

object StringPart {
  def apply[U](theLabel: String, getter: U => String) = 
    new StringPart[U] {
	  val label = theLabel
	  val getValue = getter
  }
}

trait IntPart[U] extends TemplatePart[U] with TemplateCombinator[IntPart[U], U] { self =>
  type Repr = Int
  def getValue: U => Int
}

object IntPart {
  def apply[U](theLabel: String, getter: U => Int) = 
    new IntPart[U] {
	  val label = theLabel
	  val getValue = getter
  }
}
