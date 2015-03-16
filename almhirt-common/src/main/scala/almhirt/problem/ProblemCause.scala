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
import almhirt.common.EscalatedProblemException

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

  implicit class ProblemCauseOps(val self: ProblemCause) extends AnyVal {
    def toProblem: Problem =
      self match {
        case CauseIsProblem(p)                           ⇒ p
        case CauseIsThrowable(HasAThrowable(exn))        ⇒ almhirt.common.ExceptionCaughtProblem(exn)
        case CauseIsThrowable(d: HasAThrowableDescribed) ⇒ almhirt.common.UnspecifiedProblem(s"There was a description of an exception:\n$d")
      }

    def toThrowable: Throwable =
      self match {
        case CauseIsProblem(p)                           ⇒ new EscalatedProblemException(p)
        case CauseIsThrowable(HasAThrowable(exn))        ⇒ exn
        case CauseIsThrowable(d: HasAThrowableDescribed) ⇒ new Exception(s"There was a description of an exception:\n$d")
      }
    
    def unwrap(recursively: Boolean = false): ProblemCause =
      self match {
        case CauseIsProblem(problemtypes.ExceptionCaughtProblem(ContainsThrowable(throwable))) ⇒
          val r = CauseIsThrowable(HasAThrowable(throwable))
          if (recursively)
            r.unwrap(recursively)
          else
            r
        case CauseIsThrowable(HasAThrowable(almhirt.common.EscalatedProblemException(p))) ⇒
          val r = CauseIsProblem(p)
          if (recursively)
            r.unwrap(recursively)
          else
            r
        case x ⇒ x
      }

    def mapProblem(m: Problem ⇒ Problem): ProblemCause =
      self match {
        case CauseIsProblem(p) ⇒ CauseIsProblem(m(p))
        case x                 ⇒ x
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


