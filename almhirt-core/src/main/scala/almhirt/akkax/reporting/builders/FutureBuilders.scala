package almhirt.akkax.reporting.builders

import scala.concurrent.ExecutionContext
import almhirt.common._
import almhirt.akkax.reporting._
import ezreps.ast._
import ezreps.util.EzValueConverter
import ezreps.EzReport

object FutureBuilders {
  private def fut2Field[T](label: String, f: AlmFuture[T])(implicit executor: ExecutionContext, converter: EzValueConverter[T]): AlmFuture[EzField] =
    f.mapOrRecover(
      map = v ⇒ EzField(label, converter.convert(v)),
      recover = p ⇒ EzField(label, EzError(p.message)))

  def build1[A](a: AlmFuture[A])(implicit executor: ExecutionContext, ca: EzValueConverter[A]): (String) ⇒ AlmFuture[Vector[EzField]] = {
    (labelA) ⇒
      fut2Field(labelA, a).map(Vector(_))
  }

  def build2[A, B](a: AlmFuture[A], b: AlmFuture[B])(implicit executor: ExecutionContext, ca: EzValueConverter[A], cb: EzValueConverter[B]): (String, String) ⇒ AlmFuture[Vector[EzField]] = {
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

  def build3[A, B, C](a: AlmFuture[A], b: AlmFuture[B], c: AlmFuture[C])(implicit executor: ExecutionContext, ca: EzValueConverter[A], cb: EzValueConverter[B], cc: EzValueConverter[C]): (String, String, String) ⇒ AlmFuture[Vector[EzField]] = {
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

  def build4[A, B, C, D](a: AlmFuture[A], b: AlmFuture[B], c: AlmFuture[C], d: AlmFuture[D])(implicit executor: ExecutionContext, ca: EzValueConverter[A], cb: EzValueConverter[B], cc: EzValueConverter[C], cd: EzValueConverter[D]): (String, String, String, String) ⇒ AlmFuture[Vector[EzField]] = {
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

  def build5[A, B, C, D, E](a: AlmFuture[A], b: AlmFuture[B], c: AlmFuture[C], d: AlmFuture[D], e: AlmFuture[E])(implicit executor: ExecutionContext, ca: EzValueConverter[A], cb: EzValueConverter[B], cc: EzValueConverter[C], cd: EzValueConverter[D], ce: EzValueConverter[E]): (String, String, String, String, String) ⇒ AlmFuture[Vector[EzField]] = {
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

  def build6[A, B, C, D, E, F](a: AlmFuture[A], b: AlmFuture[B], c: AlmFuture[C], d: AlmFuture[D], e: AlmFuture[E], f: AlmFuture[F])(implicit executor: ExecutionContext, ca: EzValueConverter[A], cb: EzValueConverter[B], cc: EzValueConverter[C], cd: EzValueConverter[D], ce: EzValueConverter[E], cf: EzValueConverter[F]): (String, String, String, String, String, String) ⇒ AlmFuture[Vector[EzField]] = {
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
