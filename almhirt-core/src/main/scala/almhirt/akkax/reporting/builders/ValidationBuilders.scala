package almhirt.akkax.reporting.builders

import almhirt.common._
import almhirt.akkax.reporting._
import ezreps.ast._
import ezreps.util.EzValueConverter
import ezreps.EzReport

object ValidationBuilders {
  private def vali2Field[T](label: String, v: AlmValidation[T])(implicit converter: EzValueConverter[T]): EzField =
    EzField(label, v.fold(
      fail ⇒ EzError(fail.message),
      success ⇒ converter.convert(success)))

  def build1[A](a: AlmValidation[A])(implicit ca: EzValueConverter[A]): (String) ⇒ Vector[EzField] = {
    (labelA) ⇒
      Vector(vali2Field(labelA, a))
  }

  def build2[A, B](a: AlmValidation[A], b: AlmValidation[B])(implicit ca: EzValueConverter[A], cb: EzValueConverter[B]): (String, String) ⇒ Vector[EzField] = {
    (labelA, labelB) ⇒
      Vector(vali2Field(labelA, a), vali2Field(labelB, b))
  }

  def build3[A, B, C](a: AlmValidation[A], b: AlmValidation[B], c: AlmValidation[C])(implicit ca: EzValueConverter[A], cb: EzValueConverter[B], cc: EzValueConverter[C]): (String, String, String) ⇒ Vector[EzField] = {
    (labelA, labelB, labelC) ⇒
      Vector(vali2Field(labelA, a), vali2Field(labelB, b), vali2Field(labelC, c))
  }

  def build4[A, B, C, D](a: AlmValidation[A], b: AlmValidation[B], c: AlmValidation[C], d: AlmValidation[D])(implicit ca: EzValueConverter[A], cb: EzValueConverter[B], cc: EzValueConverter[C], ccd: EzValueConverter[D]): (String, String, String, String) ⇒ Vector[EzField] = {
    (labelA, labelB, labelC, labelD) ⇒
      Vector(vali2Field(labelA, a), vali2Field(labelB, b), vali2Field(labelC, c), vali2Field(labelD, d))
  }

  def build5[A, B, C, D, E](a: AlmValidation[A], b: AlmValidation[B], c: AlmValidation[C], d: AlmValidation[D], e: AlmValidation[E])(implicit ca: EzValueConverter[A], cb: EzValueConverter[B], cc: EzValueConverter[C], cd: EzValueConverter[D], ce: EzValueConverter[E]): (String, String, String, String, String) ⇒ Vector[EzField] = {
    (labelA, labelB, labelC, labelD, labelE) ⇒
      Vector(vali2Field(labelA, a), vali2Field(labelB, b), vali2Field(labelC, c), vali2Field(labelD, d), vali2Field(labelE, e))
  }

  def build6[A, B, C, D, E, F](a: AlmValidation[A], b: AlmValidation[B], c: AlmValidation[C], d: AlmValidation[D], e: AlmValidation[E], f: AlmValidation[F])(implicit ca: EzValueConverter[A], cb: EzValueConverter[B], cc: EzValueConverter[C], cd: EzValueConverter[D], ce: EzValueConverter[E], cf: EzValueConverter[F]): (String, String, String, String, String, String) ⇒ Vector[EzField] = {
    (labelA, labelB, labelC, labelD, labelE, labelF) ⇒
      Vector(vali2Field(labelA, a), vali2Field(labelB, b), vali2Field(labelC, c), vali2Field(labelD, d), vali2Field(labelE, e), vali2Field(labelF, f))
  }

}