/* Copyright 2012 Christian Douven

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package almhirt.problem

import scala.language.implicitConversions

sealed trait ProblemCause
case class CauseIsProblem(problem: Problem) extends ProblemCause
case class CauseIsThrowable(representation: ThrowableRepresentation) extends ProblemCause

sealed trait ThrowableRepresentation
case class HasAThrowable(exn: Throwable) extends ThrowableRepresentation {
  def toDescription: HasAThrowableDescribed = HasAThrowableDescribed(exn)
}

case class HasAThrowableDescribed(classname: String, message: String, stacktrace: String, cause: Option[HasAThrowableDescribed]) extends ThrowableRepresentation

object HasAThrowableDescribed {
  def apply(exn: Throwable): HasAThrowableDescribed =
    HasAThrowableDescribed(
      exn.getClass().getName(),
      exn.getMessage(),
      exn.getStackTrace.mkString("\n"),
      if (exn.getCause() != null) Some(HasAThrowableDescribed(exn.getCause())) else None)
}

object ProblemCause {
  def apply(exn: Throwable): ProblemCause =
    CauseIsThrowable(HasAThrowable(exn))

  def apply(problem: Problem): ProblemCause =
    CauseIsProblem(problem)

  implicit def throwable2ProblemCause(exn: Throwable): ProblemCause = ProblemCause(exn)
  implicit def prob2ProblemCause(problem: Problem): ProblemCause = ProblemCause(problem)

  implicit class ProblemCauseOps(self: ProblemCause) {
    def toProblem: Problem =
      self match {
        case CauseIsProblem(p) => p
        case CauseIsThrowable(HasAThrowable(exn)) => almhirt.common.ExceptionCaughtProblem(exn)
        case CauseIsThrowable(d: HasAThrowableDescribed) => almhirt.common.UnspecifiedProblem(s"There was a description of an exception:\n$d")
      }
  }
}

object ContainsThrowable {
  def unapply(p: Problem): Option[Throwable] =
    p match {
      case IsSingleProblem(sp) ⇒
        sp.cause match {
          case Some(CauseIsThrowable(HasAThrowable(throwable))) ⇒
            Some(throwable)
          case _ ⇒ None
        }
      case _ ⇒ None
    }
}


