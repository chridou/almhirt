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
package almhirt

import language.implicitConversions
import scala.util.control.NonFatal
import scalaz.syntax.validation
import scala.concurrent.ExecutionContext
import almhirt.problem._

/** Classes and traits needed at other places*/
package object common {
  type AlmValidation[+α] = scalaz.Validation[almhirt.problem.Problem, α]
  type AlmValidationAP[+α] = scalaz.Validation[almhirt.problem.AggregateProblem, α]

  type Problem = almhirt.problem.Problem
  type SingleProblem = almhirt.problem.SingleProblem
  type AggregateProblem = almhirt.problem.AggregateProblem

  implicit def ProblemEqual[T <: Problem]: scalaz.Equal[T] = new scalaz.Equal[T] { def equal(p1: T, p2: T): Boolean = p1 == p2 }

  def launderException(exn: Throwable): SingleProblem = (CommonExceptionToProblem orElse (AnyExceptionToCaughtExceptionProblem))(exn)
  def handleThrowable(throwable: Throwable): Problem =
    throwable match {
      case NonFatal(exn) => launderException(exn)
    }

  implicit object DateTimeOrdering extends Ordering[org.joda.time.DateTime] {
    def compare(a: org.joda.time.DateTime, b: org.joda.time.DateTime) = a.compareTo(b)
  }

  implicit object LocalDateTimeOrdering extends Ordering[org.joda.time.LocalDateTime] {
    def compare(a: org.joda.time.LocalDateTime, b: org.joda.time.LocalDateTime) = a.compareTo(b)
  }

  import scala.concurrent.duration._
  implicit class DeadlineOps(self: Deadline) {
    def lap: FiniteDuration = Deadline.now - self
  }
  
  object Severity {
    val Critical = almhirt.problem.Critical
    val Major = almhirt.problem.Major
    val Minor = almhirt.problem.Minor
    val NoProblem = almhirt.problem.NoProblem
  }

  val UnspecifiedProblem = almhirt.problem.problemtypes.UnspecifiedProblem
  val MultipleProblems = almhirt.problem.problemtypes.MultipleProblems
  val ExceptionCaughtProblem = almhirt.problem.problemtypes.ExceptionCaughtProblem
  val RegistrationProblem = almhirt.problem.problemtypes.RegistrationProblem
  val ServiceNotFoundProblem = almhirt.problem.problemtypes.ServiceNotFoundProblem
  val NoConnectionProblem = almhirt.problem.problemtypes.NoConnectionProblem
  val OperationTimedOutProblem = almhirt.problem.problemtypes.OperationTimedOutProblem
  val OperationAbortedProblem = almhirt.problem.problemtypes.OperationAbortedProblem
  val IllegalOperationProblem = almhirt.problem.problemtypes.IllegalOperationProblem
  val OperationNotSupportedProblem = almhirt.problem.problemtypes.OperationNotSupportedProblem
  val ArgumentProblem = almhirt.problem.problemtypes.ArgumentProblem
  val EmptyCollectionProblem = almhirt.problem.problemtypes.EmptyCollectionProblem
  val MandatoryDataProblem = almhirt.problem.problemtypes.MandatoryDataProblem
  val InvalidCastProblem = almhirt.problem.problemtypes.InvalidCastProblem
  val PersistenceProblem = almhirt.problem.problemtypes.PersistenceProblem
  val MappingProblem = almhirt.problem.problemtypes.MappingProblem
  val SerializationProblem = almhirt.problem.problemtypes.SerializationProblem
  val StartupProblem = almhirt.problem.problemtypes.StartupProblem
  val IndexOutOfBoundsProblem = almhirt.problem.problemtypes.IndexOutOfBoundsProblem
  val NotFoundProblem = almhirt.problem.problemtypes.NotFoundProblem
  val ConstraintViolatedProblem = almhirt.problem.problemtypes.ConstraintViolatedProblem
  val ParsingProblem = almhirt.problem.problemtypes.ParsingProblem
  val BadDataProblem = almhirt.problem.problemtypes.BadDataProblem
  val CollisionProblem = almhirt.problem.problemtypes.CollisionProblem
  val NotAuthorizedProblem = almhirt.problem.problemtypes.NotAuthorizedProblem
  val NotAuthenticatedProblem = almhirt.problem.problemtypes.NotAuthenticatedProblem
  val AlreadyExistsProblem = almhirt.problem.problemtypes.AlreadyExistsProblem
  val OperationCancelledProblem = almhirt.problem.problemtypes.OperationCancelledProblem
  val BusinessRuleViolatedProblem = almhirt.problem.problemtypes.BusinessRuleViolatedProblem
  val LocaleNotSupportedProblem = almhirt.problem.problemtypes.LocaleNotSupportedProblem
  val NoSuchElementProblem = almhirt.problem.problemtypes.NoSuchElementProblem

}