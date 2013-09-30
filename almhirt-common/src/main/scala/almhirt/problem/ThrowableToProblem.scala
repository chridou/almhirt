package almhirt.problem

import almhirt.common._

trait ExceptionToProblem extends PartialFunction[Throwable, SingleProblem] {
  def orElse(that: PartialFunction[Throwable, SingleProblem]): ExceptionToProblem = {
    val fn = ((this: PartialFunction[Throwable, SingleProblem]) orElse (that: PartialFunction[Throwable, SingleProblem]))
    new ExceptionToProblem {
      override def apply(exn: Throwable): SingleProblem = fn(exn)
      override def isDefinedAt(exn: Throwable) = this.isDefinedAt(exn) || that.isDefinedAt(exn)
    }
  }
}

object CommonExceptionToProblem extends ExceptionToProblem {
  private val exnMappers = Map[Class[_ <: Throwable], Throwable => SingleProblem](
    (classOf[NoSuchElementException] -> (exn => problemtypes.NoSuchElementProblem(exn.getMessage, cause = Some(exn)))),
    (classOf[IndexOutOfBoundsException] -> (exn => problemtypes.IndexOutOfBoundsProblem(exn.getMessage, cause = Some(exn)))),
    (classOf[ClassCastException] -> (exn => problemtypes.InvalidCastProblem(exn.getMessage, cause = Some(exn)))),
    (classOf[java.util.concurrent.TimeoutException] -> (exn => problemtypes.OperationTimedOutProblem(exn.getMessage, cause = Some(exn)))))

  override def apply(exn: Throwable) = exnMappers(exn.getClass())(exn)

  override def isDefinedAt(exn: Throwable) = exnMappers.contains(exn.getClass())
}

object AnyExceptionToCaughtExceptionProblem extends ExceptionToProblem {
  override def apply(exn: Throwable) =
    if (exn.getClass.getName() == "akka.pattern.AskTimeoutException")
      problemtypes.OperationTimedOutProblem(s"""Asking an actor timed out: "${exn.getMessage}"""", cause = Some(exn))
    else if (exn.isInstanceOf[java.util.concurrent.TimeoutException])
      problemtypes.OperationTimedOutProblem(exn.getMessage, cause = Some(exn))
    else
      problemtypes.ExceptionCaughtProblem(exn)
  override def isDefinedAt(exn: Throwable) = true
}