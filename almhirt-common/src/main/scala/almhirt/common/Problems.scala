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
package almhirt.common

/** A problem that just lives of its message or other contained data */
case class UnspecifiedProblem(message: String, severity: Severity = Major, category: ProblemCategory = SystemProblem, args: Map[String, Any] = Map(), cause: Option[ProblemCause] = None) extends Problem {
  type T = UnspecifiedProblem
  def withMessage(newMessage: String) = copy(message = newMessage)
  def withSeverity(severity: Severity) = copy(severity = severity)
  def withArg(key: String, value: Any) = copy(args = args + (key -> value))
  def withCause(aCause: ProblemCause) = copy(cause = Some(aCause))
  def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
}

/**
 * Multiple problems that occurred during an operation under the hood of one aggregating problem.
 * The cause property is usually None
 */
case class AggregateProblem(message: String, severity: Severity = Major, category: ProblemCategory = SystemProblem, args: Map[String, Any] = Map(), cause: Option[ProblemCause] = None, problems: List[Problem] = Nil) extends Problem {
  type T = AggregateProblem
  def withMessage(newMessage: String) = copy(message = newMessage)
  def withSeverity(severity: Severity) = copy(severity = severity)
  def withArg(key: String, value: Any) = copy(args = args + (key -> value))
  def withCause(aCause: ProblemCause) = copy(cause = Some(aCause))
  def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
  def addProblem(anotherOne: Problem) =
    AggregateProblem(this.message, severity = this.severity and anotherOne.severity, category = this.category and anotherOne.category, problems = anotherOne :: this.problems)

  override def toString(): String = {
    val builder = baseInfo
    builder.append("Aggregated problems:\n")
    problems.zipWithIndex.foreach {
      case (p, i) => {
        builder.append("Problem %d:\n".format(i))
        builder.append(p.toString())
      }
    }
    builder.result
  }
}

/**
 * Should be used in cause an attempt to register something somewhere failed.
 */
case class ExceptionCaughtProblem(message: String, severity: Severity = Major, category: ProblemCategory = SystemProblem, args: Map[String, Any] = Map(), cause: Option[ProblemCause] = None) extends Problem {
  type T = ExceptionCaughtProblem
  def withMessage(newMessage: String) = copy(message = newMessage)
  def withSeverity(severity: Severity) = copy(severity = severity)
  def withArg(key: String, value: Any) = copy(args = args + (key -> value))
  def withCause(aCause: ProblemCause) = copy(cause = Some(aCause))
  def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
}

/**
 * Should be used in cause an attempt to register something somewhere failed.
 */
case class RegistrationProblem(message: String, severity: Severity = Major, category: ProblemCategory = SystemProblem, args: Map[String, Any] = Map(), cause: Option[ProblemCause] = None) extends Problem {
  type T = RegistrationProblem
  def withMessage(newMessage: String) = copy(message = newMessage)
  def withSeverity(severity: Severity) = copy(severity = severity)
  def withArg(key: String, value: Any) = copy(args = args + (key -> value))
  def withCause(aCause: ProblemCause) = copy(cause = Some(aCause))
  def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
}

/**
 * A service couldn't be found.
 */
case class ServiceNotFoundProblem(message: String, severity: Severity = Major, category: ProblemCategory = SystemProblem, args: Map[String, Any] = Map(), cause: Option[ProblemCause] = None) extends Problem {
  type T = ServiceNotFoundProblem
  def withMessage(newMessage: String) = copy(message = newMessage)
  def withSeverity(severity: Severity) = copy(severity = severity)
  def withArg(key: String, value: Any) = copy(args = args + (key -> value))
  def withCause(aCause: ProblemCause) = copy(cause = Some(aCause))
  def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
}

/**
 * A connection couldn't be established. Use for networking problems.
 */
case class NoConnectionProblem(message: String, severity: Severity = Major, category: ProblemCategory = SystemProblem, args: Map[String, Any] = Map(), cause: Option[ProblemCause] = None) extends Problem {
  type T = NoConnectionProblem
  def withMessage(newMessage: String) = copy(message = newMessage)
  def withSeverity(severity: Severity) = copy(severity = severity)
  def withArg(key: String, value: Any) = copy(args = args + (key -> value))
  def withCause(aCause: ProblemCause) = copy(cause = Some(aCause))
  def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
}

/**
 * An arbitrary operation timed out.
 * Especially useful in conjunction with futures.
 */
case class OperationTimedOutProblem(message: String, severity: Severity = Major, category: ProblemCategory = SystemProblem, args: Map[String, Any] = Map(), cause: Option[ProblemCause] = None) extends Problem {
  type T = OperationTimedOutProblem
  def withMessage(newMessage: String) = copy(message = newMessage)
  def withSeverity(severity: Severity) = copy(severity = severity)
  def withArg(key: String, value: Any) = copy(args = args + (key -> value))
  def withCause(aCause: ProblemCause) = copy(cause = Some(aCause))
  def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
}

/**
 * An arbitrary operation has been aborted by the system.
 * Not intended to be used in case a user pressed the cancel button.
 * For cancelled operations use [almhirt.OperationCancelledProblem]
 */
case class OperationAbortedProblem(message: String, severity: Severity = Major, category: ProblemCategory = SystemProblem, args: Map[String, Any] = Map(), cause: Option[ProblemCause] = None) extends Problem {
  type T = OperationAbortedProblem
  def withMessage(newMessage: String) = copy(message = newMessage)
  def withSeverity(severity: Severity) = copy(severity = severity)
  def withArg(key: String, value: Any) = copy(args = args + (key -> value))
  def withCause(aCause: ProblemCause) = copy(cause = Some(aCause))
  def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
}

/**
 * The operation was not allowed in the current state/context
 */
case class IllegalOperationProblem(message: String, severity: Severity = Major, category: ProblemCategory = SystemProblem, args: Map[String, Any] = Map(), cause: Option[ProblemCause] = None) extends Problem {
  type T = IllegalOperationProblem
  def withMessage(newMessage: String) = copy(message = newMessage)
  def withSeverity(severity: Severity) = copy(severity = severity)
  def withArg(key: String, value: Any) = copy(args = args + (key -> value))
  def withCause(aCause: ProblemCause) = copy(cause = Some(aCause))
  def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
}

/**
 * The operation was not allowed in the current state/context
 */
case class OperationNotSupportedProblem(message: String, severity: Severity = Major, category: ProblemCategory = SystemProblem, args: Map[String, Any] = Map(), cause: Option[ProblemCause] = None) extends Problem {
  type T = OperationNotSupportedProblem
  def withMessage(newMessage: String) = copy(message = newMessage)
  def withSeverity(severity: Severity) = copy(severity = severity)
  def withArg(key: String, value: Any) = copy(args = args + (key -> value))
  def withCause(aCause: ProblemCause) = copy(cause = Some(aCause))
  def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
}

/**
 * An argument violating the operations contract has been passed
 */
case class ArgumentProblem(message: String, severity: Severity = Major, category: ProblemCategory = SystemProblem, args: Map[String, Any] = Map(), cause: Option[ProblemCause] = None) extends Problem {
  type T = ArgumentProblem
  def withMessage(newMessage: String) = copy(message = newMessage)
  def withSeverity(severity: Severity) = copy(severity = severity)
  def withArg(key: String, value: Any) = copy(args = args + (key -> value))
  def withCause(aCause: ProblemCause) = copy(cause = Some(aCause))
  def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
}

/**
 * A collection is empty but at least one element was required
 */
case class EmptyCollectionProblem(message: String, severity: Severity = Minor, category: ProblemCategory = SystemProblem, args: Map[String, Any] = Map(), cause: Option[ProblemCause] = None) extends Problem {
  type T = EmptyCollectionProblem
  def withMessage(newMessage: String) = copy(message = newMessage)
  def withSeverity(severity: Severity) = copy(severity = severity)
  def withArg(key: String, value: Any) = copy(args = args + (key -> value))
  def withCause(aCause: ProblemCause) = copy(cause = Some(aCause))
  def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
}

/**
 * As instanceOf failed
 */
case class TypeCastProblem(message: String, severity: Severity = Major, category: ProblemCategory = SystemProblem, args: Map[String, Any] = Map(), cause: Option[ProblemCause] = None) extends Problem {
  type T = TypeCastProblem
  def withMessage(newMessage: String) = copy(message = newMessage)
  def withSeverity(severity: Severity) = copy(severity = severity)
  def withArg(key: String, value: Any) = copy(args = args + (key -> value))
  def withCause(aCause: ProblemCause) = copy(cause = Some(aCause))
  def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
}

/**
 * There is a problem with the persistent store. This problem is more of technical nature and thus by default a system problem.
 */
case class PersistenceProblem(message: String, severity: Severity = Major, category: ProblemCategory = SystemProblem, args: Map[String, Any] = Map(), cause: Option[ProblemCause] = None) extends Problem {
  type T = PersistenceProblem
  def withMessage(newMessage: String) = copy(message = newMessage)
  def withSeverity(severity: Severity) = copy(severity = severity)
  def withArg(key: String, value: Any) = copy(args = args + (key -> value))
  def withCause(aCause: ProblemCause) = copy(cause = Some(aCause))
  def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
}

/**
 * The operation is not supported
 */
case class NotSupportedProblem(message: String, severity: Severity = Major, category: ProblemCategory = SystemProblem, args: Map[String, Any] = Map(), cause: Option[ProblemCause] = None) extends Problem {
  type T = NotSupportedProblem
  def withMessage(newMessage: String) = copy(message = newMessage)
  def withSeverity(severity: Severity) = copy(severity = severity)
  def withArg(key: String, value: Any) = copy(args = args + (key -> value))
  def withCause(aCause: ProblemCause) = copy(cause = Some(aCause))
  def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
}

/**
 * Some data structure couldn't be mapped from one to another. The key is giving the name of the field that caused the problem.
 */
case class MappingProblem(message: String, severity: Severity = Minor, category: ProblemCategory = SystemProblem, args: Map[String, Any] = Map(), cause: Option[ProblemCause] = None) extends Problem {
  type T = MappingProblem
  def withMessage(newMessage: String) = copy(message = newMessage)
  def withSeverity(severity: Severity) = copy(severity = severity)
  def withArg(key: String, value: Any) = copy(args = args + (key -> value))
  def withCause(aCause: ProblemCause) = copy(cause = Some(aCause))
  def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
}

/**
 * Some data couldn't be serialized
 */
case class SerializationProblem(message: String, severity: Severity = Minor, category: ProblemCategory = SystemProblem, args: Map[String, Any] = Map(), cause: Option[ProblemCause] = None) extends Problem {
  type T = SerializationProblem
  def withMessage(newMessage: String) = copy(message = newMessage)
  def withSeverity(severity: Severity) = copy(severity = severity)
  def withArg(key: String, value: Any) = copy(args = args + (key -> value))
  def withCause(aCause: ProblemCause) = copy(cause = Some(aCause))
  def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
}

/**
 * The application couldn't be started
 */
case class StartupProblem(message: String, severity: Severity = Minor, category: ProblemCategory = SystemProblem, args: Map[String, Any] = Map(), cause: Option[ProblemCause] = None) extends Problem {
  type T = StartupProblem
  def withMessage(newMessage: String) = copy(message = newMessage)
  def withSeverity(severity: Severity) = copy(severity = severity)
  def withArg(key: String, value: Any) = copy(args = args + (key -> value))
  def withCause(aCause: ProblemCause) = copy(cause = Some(aCause))
  def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
}

/**
 * Data couldn't be found. Use when looking for an entity or something similar. Do not use for a missing key in a map.
 */
case class NotFoundProblem(message: String, severity: Severity = Minor, category: ProblemCategory = ApplicationProblem, args: Map[String, Any] = Map(), cause: Option[ProblemCause] = None) extends Problem {
  type T = NotFoundProblem
  def withMessage(newMessage: String) = copy(message = newMessage)
  def withSeverity(severity: Severity) = copy(severity = severity)
  def withArg(key: String, value: Any) = copy(args = args + (key -> value))
  def withCause(aCause: ProblemCause) = copy(cause = Some(aCause))
  def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
}

/**
 * The map or dictionary doesn't contain the given key.
 */
case class KeyNotFoundProblem(message: String, severity: Severity = Minor, category: ProblemCategory = ApplicationProblem, args: Map[String, Any] = Map(), cause: Option[ProblemCause] = None) extends Problem {
  type T = KeyNotFoundProblem
  def withMessage(newMessage: String) = copy(message = newMessage)
  def withSeverity(severity: Severity) = copy(severity = severity)
  def withArg(key: String, value: Any) = copy(args = args + (key -> value))
  def withCause(aCause: ProblemCause) = copy(cause = Some(aCause))
  def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
}

/**
 * A constraint on an operation has been violated by a user(or a client).
 */
case class ConstraintViolatedProblem(message: String, severity: Severity = Minor, category: ProblemCategory = ApplicationProblem, args: Map[String, Any] = Map(), cause: Option[ProblemCause] = None) extends Problem {
  type T = ConstraintViolatedProblem
  def withMessage(newMessage: String) = copy(message = newMessage)
  def withSeverity(severity: Severity) = copy(severity = severity)
  def withArg(key: String, value: Any) = copy(args = args + (key -> value))
  def withCause(aCause: ProblemCause) = copy(cause = Some(aCause))
  def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
}

/**
 * A text couldn't be parsed. Usually used for failures when parsing DSLs
 */
case class ParsingProblem(message: String, severity: Severity = Minor, category: ProblemCategory = ApplicationProblem, args: Map[String, Any] = Map(), cause: Option[ProblemCause] = None) extends Problem {
  type T = ParsingProblem
  def withMessage(newMessage: String) = copy(message = newMessage)
  def withSeverity(severity: Severity) = copy(severity = severity)
  def withArg(key: String, value: Any) = copy(args = args + (key -> value))
  def withCause(aCause: ProblemCause) = copy(cause = Some(aCause))
  def withInput(input: String) = withArg("input", input)
  def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
}

/**
 * Some data is invalid. The key gives the context
 */
case class BadDataProblem(message: String, severity: Severity = Minor, category: ProblemCategory = ApplicationProblem, args: Map[String, Any] = Map(), cause: Option[ProblemCause] = None) extends Problem {
  type T = BadDataProblem
  def withMessage(newMessage: String) = copy(message = newMessage)
  def withSeverity(severity: Severity) = copy(severity = severity)
  def withArg(key: String, value: Any) = copy(args = args + (key -> value))
  def withCause(aCause: ProblemCause) = copy(cause = Some(aCause))
  def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
}

/**
 * Something has been changed by someone else. Stale data etc..
 */
case class CollisionProblem(message: String, severity: Severity = Minor, category: ProblemCategory = ApplicationProblem, args: Map[String, Any] = Map(), cause: Option[ProblemCause] = None) extends Problem {
  type T = CollisionProblem
  def withMessage(newMessage: String) = copy(message = newMessage)
  def withSeverity(severity: Severity) = copy(severity = severity)
  def withArg(key: String, value: Any) = copy(args = args + (key -> value))
  def withCause(aCause: ProblemCause) = copy(cause = Some(aCause))
  def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
}

case class NotAuthorizedProblem(message: String, severity: Severity = Minor, category: ProblemCategory = ApplicationProblem, args: Map[String, Any] = Map(), cause: Option[ProblemCause] = None) extends SecurityProblem {
  type T = NotAuthorizedProblem
  def withMessage(newMessage: String) = copy(message = newMessage)
  def withSeverity(severity: Severity) = copy(severity = severity)
  def withArg(key: String, value: Any) = copy(args = args + (key -> value))
  def withCause(aCause: ProblemCause) = copy(cause = Some(aCause))
  def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
}

case class NotAuthenticatedProblem(message: String, severity: Severity = Minor, category: ProblemCategory = ApplicationProblem, args: Map[String, Any] = Map(), cause: Option[ProblemCause] = None) extends SecurityProblem {
  type T = NotAuthenticatedProblem
  def withMessage(newMessage: String) = copy(message = newMessage)
  def withSeverity(severity: Severity) = copy(severity = severity)
  def withArg(key: String, value: Any) = copy(args = args + (key -> value))
  def withCause(aCause: ProblemCause) = copy(cause = Some(aCause))
  def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
}

/**
 * Something has already been created. Don't try again...
 */
case class AlreadyExistsProblem(message: String, severity: Severity = Minor, category: ProblemCategory = ApplicationProblem, args: Map[String, Any] = Map(), cause: Option[ProblemCause] = None) extends Problem {
  type T = AlreadyExistsProblem
  def withMessage(newMessage: String) = copy(message = newMessage)
  def withSeverity(severity: Severity) = copy(severity = severity)
  def withArg(key: String, value: Any) = copy(args = args + (key -> value))
  def withCause(aCause: ProblemCause) = copy(cause = Some(aCause))
  def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
}

/**
 * Some external stimulus has cancelled an operation
 */
case class OperationCancelledProblem(message: String, severity: Severity = Minor, category: ProblemCategory = ApplicationProblem, args: Map[String, Any] = Map(), cause: Option[ProblemCause] = None) extends Problem {
  type T = OperationCancelledProblem
  def withMessage(newMessage: String) = copy(message = newMessage)
  def withSeverity(severity: Severity) = copy(severity = severity)
  def withArg(key: String, value: Any) = copy(args = args + (key -> value))
  def withCause(aCause: ProblemCause) = copy(cause = Some(aCause))
  def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
}

/**
 * A rule on a process has been violated
 */
case class BusinessRuleViolatedProblem(message: String, severity: Severity = Minor, category: ProblemCategory = ApplicationProblem, args: Map[String, Any] = Map(), cause: Option[ProblemCause] = None) extends Problem {
  type T = BusinessRuleViolatedProblem
  def withMessage(newMessage: String) = copy(message = newMessage)
  def withSeverity(severity: Severity) = copy(severity = severity)
  def withArg(key: String, value: Any) = copy(args = args + (key -> value))
  def withCause(aCause: ProblemCause) = copy(cause = Some(aCause))
  def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
}

/**
 * This locale simply isn't supported. Store the not supported locale code in 'locale'.
 */
case class LocaleNotSupportedProblem(message: String, severity: Severity = NoProblem, category: ProblemCategory = ApplicationProblem, args: Map[String, Any] = Map(), cause: Option[ProblemCause] = None) extends Problem {
  type T = LocaleNotSupportedProblem
  def withMessage(newMessage: String) = copy(message = newMessage)
  def withSeverity(severity: Severity) = copy(severity = severity)
  def withArg(key: String, value: Any) = copy(args = args + (key -> value))
  def withCause(aCause: ProblemCause) = copy(cause = Some(aCause))
  def withUnsupportedLocale(locale: String) = withArg("unsupportedLocale", locale)
  def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
}

/**
 * An expected element was not present in some kind of collection
 */
case class NoSuchElementProblem(message: String, severity: Severity = Minor, category: ProblemCategory = ApplicationProblem, args: Map[String, Any] = Map(), cause: Option[ProblemCause] = None) extends Problem {
  type T = NoSuchElementProblem
  def withMessage(newMessage: String) = copy(message = newMessage)
  def withSeverity(severity: Severity) = copy(severity = severity)
  def withArg(key: String, value: Any) = copy(args = args + (key -> value))
  def withCause(aCause: ProblemCause) = copy(cause = Some(aCause))
  def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
}

  

