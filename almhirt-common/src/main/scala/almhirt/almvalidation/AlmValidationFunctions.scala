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

  /** Evaluate an unsafe expression in a safe way */
  def inTryCatch[T](a: => T): AlmValidation[T] = {
    try {
      a.success[Problem]
    } catch {
      case scala.util.control.NonFatal(exn) => launderException(exn).failure
    }
  }

  /** Evaluate an unsafe expression in a safe way */
  def inTryCatchM[T](a: => T)(message: String): AlmValidation[T] = {
    try {
      a.success[Problem]
    } catch {
      case scala.util.control.NonFatal(exn) => launderException(exn).withMessage(message).failure
    }
  }

  /** Evaluate an unsafe expression in a safe way */
  def inTryCatchMM[T](a: => T)(createMessage: Throwable => String): AlmValidation[T] = {
    try {
      a.success[Problem]
    } catch {
      case scala.util.control.NonFatal(exn) => launderException(exn).withMessage(createMessage(exn)).failure
    }
  }

  /** Evaluate an unsafe expression that looks safe but might escalate or which you just don't trust */
  def unsafe[T](a: => AlmValidation[T]): AlmValidation[T] = {
    try {
      a
    } catch {
      case scala.util.control.NonFatal(exn) => launderException(exn).failure
    }
  }

  /** Evaluate an unsafe expression that looks safe but might escalate or which you just don't trust */
  @deprecated(message = "Use unsafe", since = "0.5.213")
  def computeSafely[T](a: => AlmValidation[T]): AlmValidation[T] = {
    try {
      a
    } catch {
      case scala.util.control.NonFatal(exn) => launderException(exn).failure
    }
  }

  /** Evaluate an unsafe expression that looks safe but might escalate or which you just don't trust */
  def unsafeM[T](a: => AlmValidation[T])(message: String): AlmValidation[T] = {
    try {
      a
    } catch {
      case scala.util.control.NonFatal(exn) => launderException(exn).withMessage(message).failure
    }
  }

  /** Evaluate an unsafe expression that looks safe but might escalate or which you just don't trust */
  @deprecated(message = "Use unsafeM", since = "0.5.213")
  def computeSafelyM[T](a: => AlmValidation[T])(message: String): AlmValidation[T] = {
    try {
      a
    } catch {
      case scala.util.control.NonFatal(exn) => launderException(exn).withMessage(message).failure
    }
  }

  /** Evaluate an unsafe expression that looks safe but might escalate or which you just don't trust */
  def unsafeMM[T](a: => AlmValidation[T])(createMessage: Throwable => String): AlmValidation[T] = {
    try {
      a
    } catch {
      case scala.util.control.NonFatal(exn) => launderException(exn).withMessage(createMessage(exn)).failure
    }
  }

  /** Evaluate an unsafe expression that looks safe but might escalate or which you just don't trust */
  @deprecated(message = "Use unsafeMM", since = "0.5.213")
  def computeSafelyMM[T](a: => AlmValidation[T])(createMessage: Throwable => String): AlmValidation[T] = {
    try {
      a
    } catch {
      case scala.util.control.NonFatal(exn) => launderException(exn).withMessage(createMessage(exn)).failure
    }
  }
  
  /** Abort a workflow with the given Problem if the value evaluated to false */
  def mustBeTrue(cond: => Boolean, problem: => Problem): AlmValidation[Unit] =
    if (cond) ().success else problem.failure[Unit]

  def noneIsNoSuchElement[T](v: Option[T]): AlmValidation[T] =
    v match {
      case Some(that) => that.success
      case None => NoSuchElementProblem("A value was required but None was supplied.").failure[T]
    }

  def noneIsNotFound[T](v: Option[T]): AlmValidation[T] =
    v match {
      case Some(v) => v.success
      case None => NotFoundProblem("A value was expected but there was None").failure
    }

  def argumentIsMandatory[T](v: Option[T]): AlmValidation[T] =
    v match {
      case Some(v) => v.success
      case None => MandatoryDataProblem("A value was expected but there was None").failure
    }

  def getFromMap[K, V](key: K, map: Map[K, V]): AlmValidation[V] = {
    map.get(key) match {
      case Some(v) => v.success
      case None => NoSuchElementProblem(s"""Could not find a value for key "$key".""").failure
    }
  }

  def tryApply[K, V](key: K, f: K => Option[V]): AlmValidation[V] = {
    f(key) match {
      case Some(v) => v.success
      case None => NoSuchElementProblem(s"""Could not find a value for key "$key".""").failure
    }
  }
}