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

/** A problem that just lives of its message or other contained data */
case class UnspecifiedProblem(message: String, severity: Severity = Major, category: ProblemCategory = SystemProblem, args: Map[String, Any] = Map(), cause: Option[ProblemCause] = None) extends Problem {
  type T = UnspecifiedProblem
  def withMessage(newMessage: String) = copy(message = newMessage)
  def withSeverity(severity: Severity) = copy(severity = severity)
  def withArg(key: String, value: Any) = copy(args = args + (key -> value))
  def withCause(aCause: ProblemCause) = copy(cause = Some(aCause))
  def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
}

/** Multiple problems that occurred during an operation under the hood of one aggregating problem.
 * The cause property is usually None
 */
case class AggregateProblem(message: String, severity: Severity = Major, category: ProblemCategory = SystemProblem, args: Map[String, Any] = Map(), cause: Option[ProblemCause] = None, problems: List[Problem] = Nil) extends Problem {
  type T = AggregateProblem
  def withMessage(newMessage: String) = copy(message = newMessage)
  def withSeverity(severity: Severity) = copy(severity = severity)
  def withArg(key: String, value: Any) = copy(args = args + (key -> value))
  def withCause(aCause: ProblemCause) = copy(cause = Some(aCause))
  def mapMessage(mapOp: String => String) = copy(message = mapOp(message))

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

/** Should be used in cause an attempt to register something somewhere failed.
 */
case class RegistrationProblem(message: String, severity: Severity = Major, category: ProblemCategory = SystemProblem, args: Map[String, Any] = Map(), cause: Option[ProblemCause] = None) extends Problem {
  type T = RegistrationProblem
  def withMessage(newMessage: String) = copy(message = newMessage)
  def withSeverity(severity: Severity) = copy(severity = severity)
  def withArg(key: String, value: Any) = copy(args = args + (key -> value))
  def withCause(aCause: ProblemCause) = copy(cause = Some(aCause))
  def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
}

/** A connection couldn't be established. Use for networking problems.
 */
case class NoConnectionProblem(message: String, severity: Severity = Major, category: ProblemCategory = SystemProblem, args: Map[String, Any] = Map(), cause: Option[ProblemCause] = None) extends Problem {
  type T = NoConnectionProblem
  def withMessage(newMessage: String) = copy(message = newMessage)
  def withSeverity(severity: Severity) = copy(severity = severity)
  def withArg(key: String, value: Any) = copy(args = args + (key -> value))
  def withCause(aCause: ProblemCause) = copy(cause = Some(aCause))
  def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
}

/** An arbitrary operation timed out.
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

/** An arbitrary operation has been aborted by the system.
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

/** The operation was not allowed in the current state/context
 */
case class IllegalOperationProblem(message: String, severity: Severity = Major, category: ProblemCategory = SystemProblem, args: Map[String, Any] = Map(), cause: Option[ProblemCause] = None) extends Problem {
  type T = IllegalOperationProblem
  def withMessage(newMessage: String) = copy(message = newMessage)
  def withSeverity(severity: Severity) = copy(severity = severity)
  def withArg(key: String, value: Any) = copy(args = args + (key -> value))
  def withCause(aCause: ProblemCause) = copy(cause = Some(aCause))
  def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
}

/** The operation was not allowed in the current state/context
 */
case class OperationNotSupportedProblem(message: String, severity: Severity = Major, category: ProblemCategory = SystemProblem, args: Map[String, Any] = Map(), cause: Option[ProblemCause] = None) extends Problem {
  type T = OperationNotSupportedProblem
  def withMessage(newMessage: String) = copy(message = newMessage)
  def withSeverity(severity: Severity) = copy(severity = severity)
  def withArg(key: String, value: Any) = copy(args = args + (key -> value))
  def withCause(aCause: ProblemCause) = copy(cause = Some(aCause))
  def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
}

/** An argument violating the operations contract has been passed
 */
case class ArgumentProblem(message: String, severity: Severity = Major, category: ProblemCategory = SystemProblem, args: Map[String, Any] = Map(), cause: Option[ProblemCause] = None) extends Problem {
  type T = ArgumentProblem
  def withMessage(newMessage: String) = copy(message = newMessage)
  def withSeverity(severity: Severity) = copy(severity = severity)
  def withArg(key: String, value: Any) = copy(args = args + (key -> value))
  def withCause(aCause: ProblemCause) = copy(cause = Some(aCause))
  def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
}

/** A collection is empty but at least one element was required
 */
case class EmptyCollectionProblem(message: String, severity: Severity = Minor, category: ProblemCategory = SystemProblem, args: Map[String, Any] = Map(), cause: Option[ProblemCause] = None) extends Problem {
  type T = EmptyCollectionProblem
  def withMessage(newMessage: String) = copy(message = newMessage)
  def withSeverity(severity: Severity) = copy(severity = severity)
  def withArg(key: String, value: Any) = copy(args = args + (key -> value))
  def withCause(aCause: ProblemCause) = copy(cause = Some(aCause))
  def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
}

/** There is a problem with the persistent store. This problem is more of technical nature and thus by default a system problem.
 */
case class PersistenceProblem(message: String, severity: Severity = Major, category: ProblemCategory = SystemProblem, args: Map[String, Any] = Map(), cause: Option[ProblemCause] = None) extends Problem {
  type T = PersistenceProblem
  def withMessage(newMessage: String) = copy(message = newMessage)
  def withSeverity(severity: Severity) = copy(severity = severity)
  def withArg(key: String, value: Any) = copy(args = args + (key -> value))
  def withCause(aCause: ProblemCause) = copy(cause = Some(aCause))
  def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
}

/** Some data structure couldn't be mapped from one to another. The key is giving the name of the field that caused the problem.
 */
case class SingleMappingProblem(message: String, key: String = "unknown", category: ProblemCategory = SystemProblem, severity: Severity = Minor, args: Map[String, Any] = Map(), cause: Option[ProblemCause] = None) extends MappingProblem with SingleKeyedProblem {
  type T = SingleMappingProblem
  def withMessage(newMessage: String) = copy(message = newMessage)
  def withSeverity(severity: Severity) = copy(severity = severity)
  def withArg(key: String, value: Any) = copy(args = args + (key -> value))
  def withCause(aCause: ProblemCause) = copy(cause = Some(aCause))
  def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
  def addTo(multipleMappingProblem: MultipleMappingProblem) = multipleMappingProblem.add(this)
  def add(other: SingleMappingProblem) = toMultipleMappingProblem().add(other)
  def toMultipleMappingProblem() = MultipleMappingProblem("Multiple errors occured", keysAndMessages = Map(key -> message), severity = severity)
  override def toString(): String = {
    val builder = baseInfo
    builder.append("Key: %s\n".format(key))
    builder.result
  }
}

/** Some data structure couldn't be mapped from one to another. The keysAndMessages is giving the names of the fields that caused the problems with the error messages.
 */
case class MultipleMappingProblem(message: String, keysAndMessages: Map[String, String], severity: Severity = Minor, category: ProblemCategory = SystemProblem, args: Map[String, Any] = Map(), cause: Option[ProblemCause] = None) extends MappingProblem with MultiKeyedProblem {
  type T = MultipleMappingProblem
  def withMessage(newMessage: String) = copy(message = newMessage)
  def withSeverity(severity: Severity) = copy(severity = severity)
  def withArg(key: String, value: Any) = copy(args = args + (key -> value))
  def withCause(aCause: ProblemCause) = copy(cause = Some(aCause))
  def withBadMapping(key: String, messageForKey: String): T =
    keysAndMessages.get(key) match {
      case Some(_) => withBadMapping(key + "_", messageForKey)
      case None => copy(keysAndMessages = keysAndMessages + (key -> messageForKey))
    }
  def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
  def add(badMapping: SingleMappingProblem) = withBadMapping(badMapping.key, badMapping.message).withSeverity(severity and badMapping.severity)
  def combineWith(other: MultipleMappingProblem) =
    other.keysAndMessages.toSeq
      .foldLeft(MultipleMappingProblem("Many bad mappings", Map.empty)) { case (state, (k, msg)) => state.withBadMapping(k, msg) }
      .withSeverity(severity and other.severity)
  def prefixWithPath(pathParts: List[String], sep: String = ".") = {
    pathParts match {
      case Nil => this
      case _ =>
        val path = pathParts.mkString(sep) + sep
        copy(keysAndMessages = keysAndMessages.toSeq.map { case (k, m) => (path + k) -> m }.toMap)
    }
  }
  override def toString(): String = {
    val builder = baseInfo
    builder.append("Keys and messages:\n")
    keysAndMessages.foreach { case (k, m) => builder.append("%s->%s".format(k, m)) }
    builder.result
  }
}

/** Data couldn't be found. Use when looking for an entity or something similar. Do not use for a missing key in a map.
 */
case class NotFoundProblem(message: String, severity: Severity = Minor, category: ProblemCategory = ApplicationProblem, args: Map[String, Any] = Map(), cause: Option[ProblemCause] = None) extends Problem {
  type T = NotFoundProblem
  def withMessage(newMessage: String) = copy(message = newMessage)
  def withSeverity(severity: Severity) = copy(severity = severity)
  def withArg(key: String, value: Any) = copy(args = args + (key -> value))
  def withCause(aCause: ProblemCause) = copy(cause = Some(aCause))
  def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
}

/** The map or dictionary doesn't contain the given key.
 */
case class KeyNotFoundProblem(message: String, severity: Severity = Minor, category: ProblemCategory = ApplicationProblem, args: Map[String, Any] = Map(), cause: Option[ProblemCause] = None) extends Problem {
  type T = KeyNotFoundProblem
  def withMessage(newMessage: String) = copy(message = newMessage)
  def withSeverity(severity: Severity) = copy(severity = severity)
  def withArg(key: String, value: Any) = copy(args = args + (key -> value))
  def withCause(aCause: ProblemCause) = copy(cause = Some(aCause))
  def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
}

/** A constraint on an operation has been violated by a user(or a client).
 */
case class ConstraintViolatedProblem(message: String, severity: Severity = Minor, category: ProblemCategory = ApplicationProblem, args: Map[String, Any] = Map(), cause: Option[ProblemCause] = None) extends Problem {
  type T = ConstraintViolatedProblem
  def withMessage(newMessage: String) = copy(message = newMessage)
  def withSeverity(severity: Severity) = copy(severity = severity)
  def withArg(key: String, value: Any) = copy(args = args + (key -> value))
  def withCause(aCause: ProblemCause) = copy(cause = Some(aCause))
  def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
}

/** A text couldn't be parsed. Usually used for failures when parsing DSLs
 */
case class ParsingProblem(message: String, input: Option[String], severity: Severity = Minor, category: ProblemCategory = ApplicationProblem, args: Map[String, Any] = Map(), cause: Option[ProblemCause] = None) extends Problem {
  type T = ParsingProblem
  def withMessage(newMessage: String) = copy(message = newMessage)
  def withSeverity(severity: Severity) = copy(severity = severity)
  def withArg(key: String, value: Any) = copy(args = args + (key -> value))
  def withCause(aCause: ProblemCause) = copy(cause = Some(aCause))
  def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
  override def toString(): String = {
    val builder = baseInfo
    input.foreach(inp => {
      builder.append("Input:\n")
      builder.append("%s".format(inp))
      builder.append("\n")
    })
    builder.result
  }
}

/** Some data is invalid. The key gives the context
 */
case class SingleBadDataProblem(message: String, key: String = "unknown", severity: Severity = Minor, category: ProblemCategory = ApplicationProblem, args: Map[String, Any] = Map(), cause: Option[ProblemCause] = None) extends BadDataProblem with SingleKeyedProblem {
  type T = SingleBadDataProblem
  def withMessage(newMessage: String) = copy(message = newMessage)
  def withSeverity(severity: Severity) = copy(severity = severity)
  def withArg(key: String, value: Any) = copy(args = args + (key -> value))
  def withCause(aCause: ProblemCause) = copy(cause = Some(aCause))
  def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
  def addTo(multipleBadData: MultipleBadDataProblem) = multipleBadData.add(this)
  def add(other: SingleBadDataProblem) = toMBD().add(other)
  def toMBD() = MultipleBadDataProblem("Multiple errors occured", keysAndMessages = Map(key -> message), severity = severity, cause = None)
  def prefixWithPath(pathParts: List[String], sep: String = ".") = {
    pathParts match {
      case Nil => this
      case _ =>
        val path = pathParts.mkString(sep) + sep
        copy(key = (path + sep + key))
    }
  }
  override def toString(): String = {
    val builder = baseInfo
    builder.append("Key: %s\n".format(key))
    builder.result
  }
}

/** Some data is invalid. The keys give the contexts
 */
case class MultipleBadDataProblem(message: String, keysAndMessages: Map[String, String], severity: Severity = Minor, category: ProblemCategory = ApplicationProblem, args: Map[String, Any] = Map(), cause: Option[ProblemCause] = None) extends BadDataProblem with MultiKeyedProblem {
  type T = MultipleBadDataProblem
  def withMessage(newMessage: String) = copy(message = newMessage)
  def withSeverity(severity: Severity) = copy(severity = severity)
  def withArg(key: String, value: Any) = copy(args = args + (key -> value))
  def withCause(aCause: ProblemCause) = copy(cause = Some(aCause))
  def withBadData(key: String, messageForKey: String): T =
    keysAndMessages.get(key) match {
      case Some(_) => withBadData(key + "_", messageForKey)
      case None => copy(keysAndMessages = keysAndMessages + (key -> messageForKey))
    }
  def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
  def add(badData: SingleBadDataProblem) = withBadData(badData.key, badData.message).withSeverity(severity and badData.severity)
  def combineWith(other: MultipleBadDataProblem) =
    other.keysAndMessages.toSeq
      .foldLeft(MultipleBadDataProblem("Multiple bad data", Map.empty)) { case (state, (k, msg)) => state.withBadData(k, msg) }
      .withSeverity(severity and other.severity)
  def prefixWithPath(pathParts: List[String], sep: String = ".") = {
    pathParts match {
      case Nil => this
      case _ =>
        val path = pathParts.mkString(sep) + sep
        copy(keysAndMessages = keysAndMessages.toSeq.map { case (k, m) => (path + k) -> m }.toMap)
    }
  }
  override def toString(): String = {
    val builder = baseInfo
    builder.append("Keys and messages:\n")
    keysAndMessages.foreach { case (k, m) => builder.append("%s->%s".format(k, m)) }
    builder.result
  }
}

/** Something has been changed by someone else. Stale data etc..
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

/** Something has already been created. Don't try again...
 */
case class AlreadyExistsProblem(message: String, severity: Severity = Minor, category: ProblemCategory = ApplicationProblem, args: Map[String, Any] = Map(), cause: Option[ProblemCause] = None) extends Problem {
  type T = AlreadyExistsProblem
  def withMessage(newMessage: String) = copy(message = newMessage)
  def withSeverity(severity: Severity) = copy(severity = severity)
  def withArg(key: String, value: Any) = copy(args = args + (key -> value))
  def withCause(aCause: ProblemCause) = copy(cause = Some(aCause))
  def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
}

/** Some external stimulus has cancelled an operation
 */
case class OperationCancelledProblem(message: String, severity: Severity = Minor, category: ProblemCategory = ApplicationProblem, args: Map[String, Any] = Map(), cause: Option[ProblemCause] = None) extends Problem {
  type T = OperationCancelledProblem
  def withMessage(newMessage: String) = copy(message = newMessage)
  def withSeverity(severity: Severity) = copy(severity = severity)
  def withArg(key: String, value: Any) = copy(args = args + (key -> value))
  def withCause(aCause: ProblemCause) = copy(cause = Some(aCause))
  def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
}

/** A rule on a process has been violated
 */
case class BusinessRuleViolatedProblem(message: String, key: String, severity: Severity = Minor, category: ProblemCategory = ApplicationProblem, args: Map[String, Any] = Map(), cause: Option[ProblemCause] = None) extends BusinessRuleProblem with SingleKeyedProblem {
  type T = BusinessRuleViolatedProblem
  def withMessage(newMessage: String) = copy(message = newMessage)
  def withSeverity(severity: Severity) = copy(severity = severity)
  def withArg(key: String, value: Any) = copy(args = args + (key -> value))
  def withCause(aCause: ProblemCause) = copy(cause = Some(aCause))
  def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
  def addTo(manyViolations: ManyBusinessRulesViolatedProblem) = manyViolations.add(this)
  def add(other: BusinessRuleViolatedProblem) = toMBRV().add(other)
  def toMBRV() = ManyBusinessRulesViolatedProblem("Multiple errors occured", keysAndMessages = Map(key -> message), severity = severity)
  override def toString(): String = {
    val builder = baseInfo
    builder.append("Key: %s\n".format(key))
    builder.result
  }
}

/** Multiple rules on a process have been violated
 */
case class ManyBusinessRulesViolatedProblem(message: String, keysAndMessages: Map[String, String], severity: Severity = Minor, category: ProblemCategory = ApplicationProblem, args: Map[String, Any] = Map(), cause: Option[ProblemCause] = None) extends BusinessRuleProblem with MultiKeyedProblem {
  type T = ManyBusinessRulesViolatedProblem
  def withMessage(newMessage: String) = copy(message = newMessage)
  def withSeverity(severity: Severity) = copy(severity = severity)
  def withArg(key: String, value: Any) = copy(args = args + (key -> value))
  def withCause(aCause: ProblemCause) = copy(cause = Some(aCause))
  def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
  def withViolation(key: String, messageForKey: String): T =
    keysAndMessages.get(key) match {
      case Some(_) => withViolation(key + "_", messageForKey)
      case None => copy(keysAndMessages = keysAndMessages + (key -> messageForKey))
    }
  def add(violation: BusinessRuleViolatedProblem) = withViolation(violation.key, violation.message).withSeverity(severity and violation.severity)
  def combineWith(other: ManyBusinessRulesViolatedProblem) =
    other.keysAndMessages.toSeq
      .foldLeft(ManyBusinessRulesViolatedProblem("Many business rules violated", Map.empty)) { case (state, (k, msg)) => state.withViolation(k, msg) }
      .withSeverity(severity and other.severity)
  def prefixWithPath(pathParts: List[String], sep: String = ".") = {
    pathParts match {
      case Nil => this
      case _ =>
        val path = pathParts.mkString(sep) + sep
        copy(keysAndMessages = keysAndMessages.toSeq.map { case (k, m) => (path + k) -> m }.toMap)
    }
  }

  override def toString(): String = {
    val builder = baseInfo
    builder.append("Keys and messages:\n")
    keysAndMessages.foreach { case (k, m) => builder.append("%s->%s".format(k, m)) }
    builder.result
  }
}

/** This locale simply isn't supported. Store the not supported locale code in 'locale'.
 */
case class LocaleNotSupportedProblem(message: String, locale: String, severity: Severity = NoProblem, category: ProblemCategory = ApplicationProblem, args: Map[String, Any] = Map(), cause: Option[ProblemCause] = None) extends Problem {
  type T = LocaleNotSupportedProblem
  def withMessage(newMessage: String) = copy(message = newMessage)
  def withSeverity(severity: Severity) = copy(severity = severity)
  def withArg(key: String, value: Any) = copy(args = args + (key -> value))
  def withCause(aCause: ProblemCause) = copy(cause = Some(aCause))
  def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
  override def toString(): String = {
    val builder = baseInfo
    builder.append("Unsupported locale:%s\n".format(locale))
    builder.result
  }
}
  
