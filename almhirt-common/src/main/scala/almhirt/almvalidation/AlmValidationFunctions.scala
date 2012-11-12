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
package almhirt.almvalidation

import scalaz.{Validation, Success, Failure, ValidationNEL}
import scalaz.syntax.validation._
import org.joda.time.DateTime
import almhirt.common._

trait AlmValidationFunctions {
  import almhirt.problem.ProblemDefaults._
  
  def successAlm[T](x: T): AlmValidation[T] = x.success[Problem]
  
  def inTryCatch[T](a: => T, defaultProblemType: Problem = defaultProblem): AlmValidation[T] = {
    try {
      a.success[Problem]
    } catch {
      case err => defaultProblemType.withMessage(err.getMessage).withCause(CauseIsThrowable(err)).failure[T]
    }
  }
  
  def computeSafely[T](a: => AlmValidation[T], defaultProblemType: Problem = defaultProblem): AlmValidation[T] = {
    try {
      a
    } catch {
      case err => defaultProblemType.withMessage(err.getMessage).withCause(CauseIsThrowable(err)).failure[T]
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