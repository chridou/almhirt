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

import scalaz._
import scalaz.syntax.validation._
import org.joda.time.DateTime
import almhirt.common._

trait AlmValidationFunctions {
  def successAlm[T](x: T): AlmValidation[T] = x.success[Problem]
  
  def inTryCatch[T](a: => T): AlmValidation[T] = {
    try {
      a.success[Problem]
    } catch {
      case exn: Exception => launderException(exn).failure
    }
  }

  def inTryCatchM[T](a: => T)(message: String): AlmValidation[T] = {
    try {
      a.success[Problem]
    } catch {
      case exn: Exception => launderException(exn).withMessage(message).failure
    }
  }
  
  def computeSafely[T](a: => AlmValidation[T]): AlmValidation[T] = {
    try {
      a
    } catch {
      case exn: Exception => launderException(exn).failure
    }
  }
  
  def computeSafelyM[T](a: => AlmValidation[T])(message: String): AlmValidation[T] = {
    try {
      a
    } catch {
      case exn: Exception => launderException(exn).withMessage(message).failure
    }
  }

  def computeSafelyMM[T](a: => AlmValidation[T])(createMessage: Exception => String): AlmValidation[T] = {
    try {
      a
    } catch {
      case exn: Exception => launderException(exn).withMessage(createMessage(exn)).failure
    }
  }
  
  def mustBeTrue(cond: => Boolean, problem: => Problem): AlmValidation[Unit] =
    if(cond) ().success else problem.failure[Unit]
  
  def noneIsBadData[T](v: Option[T]): AlmValidation[T] =
    v match {
      case Some(that) => that.success[BadDataProblem]
      case None => BadDataProblem("A value was required but None was supplied.").failure[T]
    }
  
  def noneIsNotFound[T](v: Option[T]): AlmValidation[T] =
    v match {
      case Some(v) => v.success
      case None => NotFoundProblem("A value was expected but there was None").failure
    }

  def noneIsNoSuchElement[T](v: Option[T]): AlmValidation[T] =
    v match {
      case Some(v) => v.success
      case None => NoSuchElementProblem("A value was expected but there was None").failure
    }
  
  def getFromMap[K,V](key: K, map: Map[K,V], severity: Severity = NoProblem): Validation[KeyNotFoundProblem, V] = {
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