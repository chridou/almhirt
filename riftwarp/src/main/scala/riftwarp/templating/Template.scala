package riftwarp.templating

import shapeless._

trait Template[L <: HList, U] { self =>
  final def apply: L = hlist

  def ~[T <: TemplatePart[U], U](other: T)(implicit prepender: Prepend[L, T :: HNil]): Template[prepender.Out, U] =
    new Template[prepender.Out, U] {
      val hlist = self.hlist :+ other
    }

  def add[T <: TemplatePart[U], U](other: T)(implicit prepender: Prepend[L, T :: HNil]): Template[prepender.Out, U] =
    self.~[T, U](other)

  def hlist: L
  //def list = hlist.toList[TemplatePart[U]]
}

object Template {
  implicit class TemplateOps[L <: HList, U](self: Template[L, U]) {
    //    def eval(u: U) =
    //      self.hlist.foldLeft(HNil)(op)
  }
}

object eval extends Poly1 {
  implicit def caseStringPart[U] = at[StringPart[U]](_.getValue)
  implicit def caseIntPart[U] = at[IntPart[U]](_.getValue)
}

