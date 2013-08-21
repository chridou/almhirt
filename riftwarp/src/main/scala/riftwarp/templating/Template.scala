package riftwarp.templating

import shapeless._
import scalaz.syntax.validation._
import almhirt.common.AlmValidation
import riftwarp._

trait Template[L <: HList, U] { self =>
  final def apply(): L = hlist

  def ~[T <: TemplatePart[U]](other: T)(implicit prepender: Prepend[L, T :: HNil]): Template[prepender.Out, U] =
    new Template[prepender.Out, U] {
      val hlist = self.hlist :+ other
      val elemsGenerators = self.elemsGenerators :+ other.createWarpElement
    }

  def add[T <: TemplatePart[U]](other: T)(implicit prepender: Prepend[L, T :: HNil]): Template[prepender.Out, U] =
    self.~[T](other)

  def hlist: L

  def elemsGenerators: List[(U, WarpPackers) => AlmValidation[WarpElement]]

  def genElemsFun: (U, WarpPackers) => AlmValidation[Seq[WarpElement]] = {
    def gen(u: U, packers: WarpPackers, rest: List[(U, WarpPackers) => AlmValidation[WarpElement]], acc: AlmValidation[Seq[WarpElement]]): AlmValidation[Seq[WarpElement]] = {
      acc.fold(
        fail =>
          fail.failure,
        collected => {
          rest match {
            case Nil => acc
            case scala.collection.immutable.::(x, xs) =>
              x(u, packers).fold(
                fail =>
                  fail.failure,
                succ =>
                  gen(u, packers, xs, (collected :+ succ).success))
          }
        })
    }
    (u: U, p: WarpPackers) => gen(u, p, elemsGenerators, Seq.empty.success)
  }
  //def elemsFun: (U, WarpPackers) => AlmValidation[Vector[WarpElement]] 
  //  def x() {
  //    val x = hlist.map(evalPolyF)
  //  }
}

object Template {
  implicit class TemplateOps[L <: HList, U](self: Template[L, U]) {
    //    def eval(u: U) =
    //      self.
  }
}

object evalPolyF extends Poly1 {
  implicit def caseStringPart[U] = at[StringPart[U]](_.getValue)
  implicit def caseIntPart[U] = at[IntPart[U]](_.getValue)
}

