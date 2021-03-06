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
import scalaz.syntax.validation._
import scala.concurrent._
import almhirt.problem._

/** Classes and traits needed at other places*/
package object common extends ops.DeadlineExt with ops.FiniteDurationExt {
  type AlmValidation[+α] = scalaz.Validation[almhirt.problem.Problem, α]
  type AlmValidationAP[+α] = scalaz.Validation[almhirt.problem.AggregatedProblem, α]

  type Problem = almhirt.problem.Problem
  type SingleProblem = almhirt.problem.SingleProblem
  type AggregatedProblem = almhirt.problem.AggregatedProblem

  implicit def stdF2AlmF[T](f: Future[T])(implicit execCtx: ExecutionContext): AlmFuture[T] =
    new AlmFuture[T](f.map(scalaz.Success(_)))

  implicit def almF2StdF[T](f: AlmFuture[T])(implicit execCtx: ExecutionContext): Future[T] = f.std

  implicit def ProblemEqual[T <: Problem]: scalaz.Equal[T] = new scalaz.Equal[T] { def equal(p1: T, p2: T): Boolean = p1 == p2 }

  def launderException(exn: Throwable): SingleProblem = (CommonExceptionToProblem orElse (AnyExceptionToCaughtExceptionProblem))(exn)

  def handleThrowable(throwable: Throwable): Problem =
    throwable match {
      case NonFatal(exn) ⇒ launderException(exn)
    }

  implicit object DateTimeOrdering extends Ordering[java.time.ZonedDateTime] {
    def compare(a: java.time.ZonedDateTime, b: java.time.ZonedDateTime) = a.compareTo(b)
  }

  implicit object LocalDateTimeOrdering extends Ordering[java.time.LocalDateTime] {
    def compare(a: java.time.LocalDateTime, b: java.time.LocalDateTime) = a.compareTo(b)
  }

  implicit class AlmRichString(self: String) {
    def ellipse(maxLength: Int, suffix: String = "..."): String = {
      if (self.length > maxLength) {
        self.take(maxLength - suffix.length) + suffix
      } else {
        self
      }
    }
  }
  
  /** Evaluate an unsafe expression in a safe way */
  def inTryCatch[T](a: ⇒ T): AlmValidation[T] = {
    try {
      a.success[Problem]
    } catch {
      case scala.util.control.NonFatal(exn) ⇒ launderException(exn).failure
    }
  }

  /** Evaluate an unsafe expression in a safe way */
  def inTryCatchM[T](a: ⇒ T)(message: String): AlmValidation[T] = {
    try {
      a.success[Problem]
    } catch {
      case scala.util.control.NonFatal(exn) ⇒ launderException(exn).withMessage(message).failure
    }
  }

  /** Evaluate an unsafe expression in a safe way */
  def inTryCatchMM[T](a: ⇒ T)(createMessage: Throwable ⇒ String): AlmValidation[T] = {
    try {
      a.success[Problem]
    } catch {
      case scala.util.control.NonFatal(exn) ⇒ launderException(exn).withMessage(createMessage(exn)).failure
    }
  }

  /** Evaluate an unsafe expression that looks safe but might escalate or which you just don't trust */
  def unsafe[T](a: ⇒ AlmValidation[T]): AlmValidation[T] = {
    try {
      a
    } catch {
      case scala.util.control.NonFatal(exn) ⇒ launderException(exn).failure
    }
  }

 
  /** Evaluate an unsafe expression that looks safe but might escalate or which you just don't trust */
  def unsafeM[T](a: ⇒ AlmValidation[T])(message: String): AlmValidation[T] = {
    try {
      a
    } catch {
      case scala.util.control.NonFatal(exn) ⇒ launderException(exn).withMessage(message).failure
    }
  }

  /** Evaluate an unsafe expression that looks safe but might escalate or which you just don't trust */
  def unsafeMM[T](a: ⇒ AlmValidation[T])(createMessage: Throwable ⇒ String): AlmValidation[T] = {
    try {
      a
    } catch {
      case scala.util.control.NonFatal(exn) ⇒ launderException(exn).withMessage(createMessage(exn)).failure
    }
  }
  

  val skip = TraverseWindow.skipStart

  val CriticalSeverity = almhirt.problem.Critical
  val MajorSeverity = almhirt.problem.Major
  val MinorSeverity = almhirt.problem.Minor

  val UnspecifiedProblem = almhirt.problem.problemtypes.UnspecifiedProblem
  val MultipleProblems = almhirt.problem.problemtypes.MultipleProblems
  val ExceptionCaughtProblem = almhirt.problem.problemtypes.ExceptionCaughtProblem
  val RegistrationProblem = almhirt.problem.problemtypes.RegistrationProblem
  val ServiceNotFoundProblem = almhirt.problem.problemtypes.ServiceNotFoundProblem
  val ServiceNotAvailableProblem = almhirt.problem.problemtypes.ServiceNotAvailableProblem
  val ServiceBusyProblem = almhirt.problem.problemtypes.ServiceBusyProblem
  val ServiceBrokenProblem = almhirt.problem.problemtypes.ServiceBrokenProblem
  val ServiceShutDownProblem = almhirt.problem.problemtypes.ServiceShutDownProblem
  val ServiceNotReadyProblem = almhirt.problem.problemtypes.ServiceNotReadyProblem
  val NoTimelyResponseFromServiceProblem = almhirt.problem.problemtypes.NoTimelyResponseFromServiceProblem
  val DependencyNotFoundProblem = almhirt.problem.problemtypes.DependencyNotFoundProblem
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
  val VersionConflictProblem = almhirt.problem.problemtypes.VersionConflictProblem
  val ParsingProblem = almhirt.problem.problemtypes.ParsingProblem
  val BadDataProblem = almhirt.problem.problemtypes.BadDataProblem
  val CollisionProblem = almhirt.problem.problemtypes.CollisionProblem
  val NotAuthorizedProblem = almhirt.problem.problemtypes.NotAuthorizedProblem
  val NotAuthenticatedProblem = almhirt.problem.problemtypes.NotAuthenticatedProblem
  val AlreadyExistsProblem = almhirt.problem.problemtypes.AlreadyExistsProblem
  val OperationCancelledProblem = almhirt.problem.problemtypes.OperationCancelledProblem
  val BusinessRuleViolatedProblem = almhirt.problem.problemtypes.BusinessRuleViolatedProblem
  val LocaleNotSupportedProblem = almhirt.problem.problemtypes.LocaleNotSupportedProblem
  val ResourceNotFoundProblem = almhirt.problem.problemtypes.ResourceNotFoundProblem
  val NoSuchElementProblem = almhirt.problem.problemtypes.NoSuchElementProblem
  val TooMuchDataProblem = almhirt.problem.problemtypes.TooMuchDataProblem
  val CommandExecutionFailedProblem = almhirt.problem.problemtypes.CommandExecutionFailedProblem
  val CircuitOpenProblem = almhirt.problem.problemtypes.CircuitOpenProblem
  val ConfigurationProblem = almhirt.problem.problemtypes.ConfigurationProblem
}