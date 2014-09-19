//package riftwarp.templating
//
//import scalaz.syntax.validation._
//import shapeless._
//import riftwarp._
//import almhirt.common._
//
//trait TemplatePart[U] { self ⇒
//  type TagetType
//  def label: String
//  def getValue: U ⇒ TagetType
//  def createWarpElement : (U, WarpPackers) ⇒ AlmValidation[WarpElement]
//}
//
//object TemplatePart {
//  def combine[L <: TemplatePart[U], R <: TemplatePart[U], U](l: L, r: R): Template[L :: R :: HNil, U] =
//    new Template[L :: R :: HNil, U] {
//      val hlist = l :: r :: HNil
//      val elemsGenerators = List(l.createWarpElement, r.createWarpElement)
//    }
//    
//}
//
//trait TemplateCombinator[T <: TemplatePart[U], U] { self : T ⇒
//  def ~[V <: TemplatePart[U]](other: V): Template[T :: V :: HNil, U] =
//    TemplatePart.combine(self, other)
//    
//  def combine[V <: TemplatePart[U]](other: V): Template[T :: V :: HNil, U] = 
//    this.~[V](other)
//    
// // def template: Template[T :: HNil, U] =  
//  def unary_! =  
//    new Template[T :: HNil, U] {
//      val hlist = self :: HNil
//      val elemsGenerators = List(self.createWarpElement)
//    }
//
//}
//
//class StringPart[U]private (val label: String, val getValue: U ⇒ String) extends TemplatePart[U] with TemplateCombinator[StringPart[U], U] {
//  type TagetType = String
//  override def createWarpElement = (u: U, packers: WarpPackers) ⇒ WarpElement(label, Some(WarpString(getValue(u)))).success
//}
//
//object StringPart {
//  def apply[U](theLabel: String, getter: U ⇒ String) = 
//    new StringPart[U](theLabel, getter)
//}
//
//class IntPart[U]private (val label: String, val getValue: U ⇒ Int) extends TemplatePart[U] with TemplateCombinator[IntPart[U], U] {
//  type TagetType = Int
//  override def createWarpElement = (u: U, packers: WarpPackers) ⇒ WarpElement(label, Some(WarpInt(getValue(u)))).success
//}
//
//object IntPart {
//  def apply[U](theLabel: String, getter: U ⇒ Int) = 
//    new IntPart[U](theLabel, getter)
//}
