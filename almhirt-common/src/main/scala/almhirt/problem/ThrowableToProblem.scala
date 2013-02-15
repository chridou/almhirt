package almhirt.problem

import almhirt.common._

trait ExceptionToProblem extends PartialFunction[Exception, Problem] {
  def orElse(that: PartialFunction[Exception, Problem]): ExceptionToProblem = {
    val fn = ((this: PartialFunction[Exception, Problem]) orElse (that: PartialFunction[Exception, Problem]))
    new ExceptionToProblem {
      override def apply(exn: Exception): Problem = fn(exn)
      override def isDefinedAt(exn: Exception) = this.isDefinedAt(exn) || that.isDefinedAt(exn)
    }
  }
}

object CommonExceptionToProblem extends ExceptionToProblem {
  private val exnMappers = Map[Class[_ <: Exception], Exception => Problem](
    (classOf[NoSuchElementException] -> (exn => NoSuchElementProblem(exn.getMessage, cause = Some(exn)))),
    (classOf[IndexOutOfBoundsException] -> (exn => IndexOutOfBoundsProblem(exn.getMessage, cause = Some(exn)))),
    (classOf[ClassCastException] -> (exn => InvalidCastProblem(exn.getMessage, cause = Some(exn)))),
    (classOf[java.util.concurrent.TimeoutException] -> (exn => OperationTimedOutProblem(exn.getMessage, cause = Some(exn)))))

  override def apply(exn: Exception) = exnMappers(exn.getClass())(exn)

  override def isDefinedAt(exn: Exception) = exnMappers.contains(exn.getClass())
}

object AnyExceptionToCaughtExceptionProblem extends ExceptionToProblem {
  override def apply(exn: Exception) = ExceptionCaughtProblem(exn.getMessage(), cause = Some(exn))
  override def isDefinedAt(exn: Exception) = true
}