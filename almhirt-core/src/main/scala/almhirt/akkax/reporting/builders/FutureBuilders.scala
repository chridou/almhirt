package almhirt.akkax.reporting.builders

import scala.concurrent.ExecutionContext
import almhirt.common._
import almhirt.akkax.reporting._
import scala.Vector

object FutureBuilders {
  private def fut2Field[T](label: String, f: AlmFuture[T])(implicit executor: ExecutionContext, converter: RValueConverter[T]): AlmFuture[AST.RField] =
    f.mapOrRecover(
      map = v ⇒ AST.RField(label, converter.convert(v)),
      recover = p ⇒ AST.RField(label, AST.RError(p.message)))

  def build1[A](a: AlmFuture[A])(implicit executor: ExecutionContext, ca: RValueConverter[A]): (String) ⇒ AlmFuture[ReportFields] = {
    (labelA) ⇒
      fut2Field(labelA, a).map(Vector(_))
  }

  def build2[A, B](a: AlmFuture[A], b: AlmFuture[B])(implicit executor: ExecutionContext, ca: RValueConverter[A], cb: RValueConverter[B]): (String, String) ⇒ AlmFuture[ReportFields] = {
    (labelA, labelB) ⇒
      {
        val fa = fut2Field(labelA, a)
        val fb = fut2Field(labelB, b)
        for {
          ra ← fa
          rb ← fb
        } yield Vector(ra, rb)
      }
  }

  def build3[A, B, C](a: AlmFuture[A], b: AlmFuture[B], c: AlmFuture[C])(implicit executor: ExecutionContext, ca: RValueConverter[A], cb: RValueConverter[B], cc: RValueConverter[C]): (String, String, String) ⇒ AlmFuture[ReportFields] = {
    (labelA, labelB, labelC) ⇒
      {
        val fa = fut2Field(labelA, a)
        val fb = fut2Field(labelB, b)
        val fc = fut2Field(labelC, c)
        for {
          ra ← fa
          rb ← fb
          rc ← fc
        } yield Vector(ra, rb, rc)
      }
  }

  def build4[A, B, C, D](a: AlmFuture[A], b: AlmFuture[B], c: AlmFuture[C], d: AlmFuture[D])(implicit executor: ExecutionContext, ca: RValueConverter[A], cb: RValueConverter[B], cc: RValueConverter[C], cd: RValueConverter[D]): (String, String, String, String) ⇒ AlmFuture[ReportFields] = {
    (labelA, labelB, labelC, labelD) ⇒
      {
        val fa = fut2Field(labelA, a)
        val fb = fut2Field(labelB, b)
        val fc = fut2Field(labelC, c)
        val fd = fut2Field(labelD, d)
        for {
          ra ← fa
          rb ← fb
          rc ← fc
          rd ← fd
        } yield Vector(ra, rb, rc, rd)
      }
  }

  def build5[A, B, C, D, E](a: AlmFuture[A], b: AlmFuture[B], c: AlmFuture[C], d: AlmFuture[D], e: AlmFuture[E])(implicit executor: ExecutionContext, ca: RValueConverter[A], cb: RValueConverter[B], cc: RValueConverter[C], cd: RValueConverter[D], ce: RValueConverter[E]): (String, String, String, String, String) ⇒ AlmFuture[ReportFields] = {
    (labelA, labelB, labelC, labelD, labelE) ⇒
      {
        val fa = fut2Field(labelA, a)
        val fb = fut2Field(labelB, b)
        val fc = fut2Field(labelC, c)
        val fd = fut2Field(labelD, d)
        val fe = fut2Field(labelE, e)
        for {
          ra ← fa
          rb ← fb
          rc ← fc
          rd ← fd
          re ← fe
        } yield Vector(ra, rb, rc, rd, re)
      }
  }

  def build6[A, B, C, D, E, F](a: AlmFuture[A], b: AlmFuture[B], c: AlmFuture[C], d: AlmFuture[D], e: AlmFuture[E], f: AlmFuture[F])(implicit executor: ExecutionContext, ca: RValueConverter[A], cb: RValueConverter[B], cc: RValueConverter[C], cd: RValueConverter[D], ce: RValueConverter[E], cf: RValueConverter[F]): (String, String, String, String, String, String) ⇒ AlmFuture[ReportFields] = {
    (labelA, labelB, labelC, labelD, labelE, labelF) ⇒
      {
        val fa = fut2Field(labelA, a)
        val fb = fut2Field(labelB, b)
        val fc = fut2Field(labelC, c)
        val fd = fut2Field(labelD, d)
        val fe = fut2Field(labelE, e)
        val ff = fut2Field(labelF, f)
        for {
          ra ← fa
          rb ← fb
          rc ← fc
          rd ← fd
          re ← fe
          rf ← ff
        } yield Vector(ra, rb, rc, rd, re, rf)
      }
  }

}
