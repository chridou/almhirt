package almhirt.problem

import almhirt.common._

trait ThrowableToProblem extends PartialFunction[Throwable, Problem] {
  def orElse(that: PartialFunction[Throwable, Problem]): ThrowableToProblem = {
    val fn = ((this: PartialFunction[Throwable, Problem]) orElse (that: PartialFunction[Throwable, Problem]))
    new ThrowableToProblem {
      override def apply(exn: Throwable): Problem = fn(exn)
      override def isDefinedAt(exn: Throwable) = this.isDefinedAt(exn) || that.isDefinedAt(exn)
    }
  }
}

object CommonThrowableToProblem extends ThrowableToProblem {
  private val exnMappers = Map[Class[_ <: Throwable], Throwable => Problem](
    (classOf[NoSuchElementException] -> (exn => NoSuchElementProblem(exn.getMessage, cause = Some(exn)))),
    (classOf[IndexOutOfBoundsException] -> (exn => IndexOutOfBoundsProblem(exn.getMessage, cause = Some(exn)))),
    (classOf[java.util.concurrent.TimeoutException] -> (exn => OperationTimedOutProblem(exn.getMessage, cause = Some(exn)))))

  override def apply(exn: Throwable) = exnMappers(exn.getClass())(exn)

  override def isDefinedAt(exn: Throwable) = exnMappers.contains(exn.getClass())
}

object AllThrowablesToCaughtExceptionProblem extends ThrowableToProblem {
  override def apply(exn: Throwable) = ExceptionCaughtProblem(exn.getMessage(), cause = Some(exn))
  override def isDefinedAt(exn: Throwable) = true
}