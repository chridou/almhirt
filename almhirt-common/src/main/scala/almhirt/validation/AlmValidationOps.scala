package almhirt.validation

import scalaz.{Validation, Success, Failure, ValidationNEL}
import scalaz.syntax.validation._
import org.joda.time.DateTime
import almhirt.validation.Problem._

trait AlmValidationOps {
  import Problem._  
    
  def successAlm[T](x: T): AlmValidation[T] = x.success[Problem]
  
  def inTryCatch[T](a: => T, defaultProblemType: Problem = defaultProblem): AlmValidation[T] = {
    try {
      a.success[Problem]
    } catch {
      case err => defaultProblemType.withMessage(err.getMessage).withException(err).failure[T]
    }
  }
  
  def computeSafely[T](a: => AlmValidation[T], defaultProblemType: Problem = defaultProblem): AlmValidation[T] = {
    try {
      a
    } catch {
      case err => defaultProblemType.withMessage(err.getMessage).withException(err).failure[T]
    }
  }
  
  def mustBeTrue(cond: => Boolean, problem: => Problem): AlmValidation[Unit] =
    if(cond) ().success else problem.failure[Unit]
  
  def noneIsBadData[T](v: Option[T], message: String = "No value supplied", key: String = "unknown"): AlmValidationSBD[T] =
    v match {
      case Some(v) => v.success[SingleBadDataProblem]
      case None => SingleBadDataProblem(message, key = key).failure[T]
    }
  
  def noneIsNotFound[T](v: Option[T], message: String = "Not found"): AlmValidation[T] =
    v match {
      case Some(v) => v.success[NotFoundProblem]
      case None => NotFoundProblem(message).failure[T]
    }
  
  def tryGetFromMap[K,V](key: K, map: Map[K,V], severity: Severity = NoProblem): Validation[KeyNotFoundProblem, V] = {
    map.get(key) match {
      case Some(v) => v.success
      case None => KeyNotFoundProblem("Could not find a value for key '%s'".format(key)).failure
    }
  }
  
  def tryApply[K,V](x: K, f: K => Option[V], severity: Severity = NoProblem): Validation[KeyNotFoundProblem, V] = {
    f(x) match {
      case Some(v) => v.success
      case None => KeyNotFoundProblem("Could not find a value for '%s'".format(x)).failure
    }
  }

}