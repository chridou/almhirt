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

import scalaz.Validation
import scala.concurrent.ExecutionContext
import almhirt.problem._

/** Classes and traits needed at other places*/
package object common {
  /** A registration using a UUID as a token */
  type RegistrationUUID = Registration[java.util.UUID]

  type AlmValidation[+α] = Validation[Problem, α]
  type AlmValidationAP[+α] = Validation[AggregateProblem, α]

  implicit def ProblemEqual[T <: Problem]: scalaz.Equal[T] = new scalaz.Equal[T] { def equal(p1: T, p2: T): Boolean = p1 == p2 }

  def launderException(exn: Exception): Problem = (CommonExceptionToProblem orElse (AnyExceptionToCaughtExceptionProblem))(exn)
  def handleThrowable(throwable: Throwable): Problem =
    throwable match {
      case exn: Exception => launderException(exn)
    }

  implicit object DateTimeOrdering extends Ordering[org.joda.time.DateTime] {
    def compare(a: org.joda.time.DateTime, b: org.joda.time.DateTime) = a.compareTo(b)
  }
}