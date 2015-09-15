package almhirt.akkax.reporting

import almhirt.common._

object ValidationBuilders {
  private def vali2Field[T](label: String, v: AlmValidation[T])(implicit converter: RValueConverter[T]): AST.RField =
    AST.RField(label, v.fold(
      fail ⇒ AST.RError(fail.message),
      success ⇒ converter.convert(success)))

  def build1[A](a: AlmValidation[A])(implicit ca: RValueConverter[A]): (String) ⇒ ReportFields = {
    (labelA) ⇒
      Vector(vali2Field(labelA, a))
  }

  def build2[A, B](a: AlmValidation[A], b: AlmValidation[B])(implicit ca: RValueConverter[A], cb: RValueConverter[B]): (String, String) ⇒ ReportFields = {
    (labelA, labelB) ⇒
      Vector(vali2Field(labelA, a), vali2Field(labelB, b))
  }

  def build3[A, B, C](a: AlmValidation[A], b: AlmValidation[B], c: AlmValidation[C])(implicit ca: RValueConverter[A], cb: RValueConverter[B], cc: RValueConverter[C]): (String, String, String) ⇒ ReportFields = {
    (labelA, labelB, labelC) ⇒
      Vector(vali2Field(labelA, a), vali2Field(labelB, b), vali2Field(labelC, c))
  }

  def build4[A, B, C, D](a: AlmValidation[A], b: AlmValidation[B], c: AlmValidation[C], d: AlmValidation[D])(implicit ca: RValueConverter[A], cb: RValueConverter[B], cc: RValueConverter[C], ccd: RValueConverter[D]): (String, String, String, String) ⇒ ReportFields = {
    (labelA, labelB, labelC, labelD) ⇒
      Vector(vali2Field(labelA, a), vali2Field(labelB, b), vali2Field(labelC, c), vali2Field(labelD, d))
  }

  def build5[A, B, C, D, E](a: AlmValidation[A], b: AlmValidation[B], c: AlmValidation[C], d: AlmValidation[D], e: AlmValidation[E])(implicit ca: RValueConverter[A], cb: RValueConverter[B], cc: RValueConverter[C], cd: RValueConverter[D], ce: RValueConverter[E]): (String, String, String, String, String) ⇒ ReportFields = {
    (labelA, labelB, labelC, labelD, labelE) ⇒
      Vector(vali2Field(labelA, a), vali2Field(labelB, b), vali2Field(labelC, c), vali2Field(labelD, d), vali2Field(labelE, e))
  }

  def build6[A, B, C, D, E, F](a: AlmValidation[A], b: AlmValidation[B], c: AlmValidation[C], d: AlmValidation[D], e: AlmValidation[E], f: AlmValidation[F])(implicit ca: RValueConverter[A], cb: RValueConverter[B], cc: RValueConverter[C], cd: RValueConverter[D], ce: RValueConverter[E], cf: RValueConverter[F]): (String, String, String, String, String, String) ⇒ ReportFields = {
    (labelA, labelB, labelC, labelD, labelE, labelF) ⇒
      Vector(vali2Field(labelA, a), vali2Field(labelB, b), vali2Field(labelC, c), vali2Field(labelD, d), vali2Field(labelE, e), vali2Field(labelF, f))
  }

}