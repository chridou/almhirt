package almhirt.problem

import almhirt.common._

trait ExceptionToProblem extends PartialFunction[Exception, SingleProblem] {
  def orElse(that: PartialFunction[Exception, SingleProblem]): ExceptionToProblem = {
    val fn = ((this: PartialFunction[Exception, SingleProblem]) orElse (that: PartialFunction[Exception, SingleProblem]))
    new ExceptionToProblem {
      override def apply(exn: Exception): SingleProblem = fn(exn)
      override def isDefinedAt(exn: Exception) = this.isDefinedAt(exn) || that.isDefinedAt(exn)
    }
  }
}

object CommonExceptionToProblem extends ExceptionToProblem {
  private val exnMappers = Map[Class[_ <: Exception], Exception => SingleProblem](
    (classOf[NoSuchElementException] -> (exn => problemtypes.NoSuchElementProblem(exn.getMessage, cause = Some(exn)))),
    (classOf[IndexOutOfBoundsException] -> (exn => problemtypes.IndexOutOfBoundsProblem(exn.getMessage, cause = Some(exn)))),
    (classOf[ClassCastException] -> (exn => problemtypes.InvalidCastProblem(exn.getMessage, cause = Some(exn)))),
    (classOf[java.util.concurrent.TimeoutException] -> (exn => problemtypes.OperationTimedOutProblem(exn.getMessage, cause = Some(exn)))))

  override def apply(exn: Exception) = exnMappers(exn.getClass())(exn)

  override def isDefinedAt(exn: Exception) = exnMappers.contains(exn.getClass())
}

object AnyExceptionToCaughtExceptionProblem extends ExceptionToProblem {
  override def apply(exn: Exception) = problemtypes.ExceptionCaughtProblem(exn)
  override def isDefinedAt(exn: Exception) = true
}