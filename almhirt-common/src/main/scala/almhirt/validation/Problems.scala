package almhirt.validation

import scalaz.{NonEmptyList, Show}
import scala.collection.mutable.StringBuilder

  case class UnspecifiedProblem(message: String, severity: Severity = Major, category: ProblemCategory = SystemProblem, args: Map[String, Any] = Map(), cause: Option[ProblemCause] = None) extends Problem {
	type T = UnspecifiedProblem
    def withMessage(newMessage: String) = copy(message = newMessage)
	def withSeverity(severity: Severity) = copy(severity = severity)
	def withArg(key: String, value: Any) = copy(args = args + (key -> value))
    def withCause(aCause: ProblemCause) = copy(cause = Some(aCause))
	def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
  }

  case class AggregateProblem(message: String, severity: Severity = Major, category: ProblemCategory = SystemProblem, args: Map[String, Any] = Map(), cause: Option[ProblemCause] = None, causes: List[ProblemCause] = Nil) extends Problem {
	type T = AggregateProblem
    def withMessage(newMessage: String) = copy(message = newMessage)
	def withSeverity(severity: Severity) = copy(severity = severity)
	def withArg(key: String, value: Any) = copy(args = args + (key -> value))
    def withCause(aCause: ProblemCause) = copy(cause = Some(aCause))
	def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
  }

  case class RegistrationProblem(message: String, severity: Severity = Major, category: ProblemCategory = SystemProblem, args: Map[String, Any] = Map(), cause: Option[ProblemCause] = None) extends Problem {
	type T = RegistrationProblem
    def withMessage(newMessage: String) = copy(message = newMessage)
	def withSeverity(severity: Severity) = copy(severity = severity)
	def withArg(key: String, value: Any) = copy(args = args + (key -> value))
    def withCause(aCause: ProblemCause) = copy(cause = Some(aCause))
	def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
  }
  case class NoConnectionProblem(message: String, severity: Severity = Major, category: ProblemCategory = SystemProblem, args: Map[String, Any] = Map(), cause: Option[ProblemCause] = None) extends Problem {
	type T = NoConnectionProblem
    def withMessage(newMessage: String) = copy(message = newMessage)
	def withSeverity(severity: Severity) = copy(severity = severity)
	def withArg(key: String, value: Any) = copy(args = args + (key -> value))
    def withCause(aCause: ProblemCause) = copy(cause = Some(aCause))
	def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
  }
  case class OperationTimedOutProblem(message: String, severity: Severity = Major, category: ProblemCategory = SystemProblem, args: Map[String, Any] = Map(), cause: Option[ProblemCause] = None) extends Problem {
	type T = OperationTimedOutProblem
    def withMessage(newMessage: String) = copy(message = newMessage)
	def withSeverity(severity: Severity) = copy(severity = severity)
	def withArg(key: String, value: Any) = copy(args = args + (key -> value))
    def withCause(aCause: ProblemCause) = copy(cause = Some(aCause))
	def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
  }
  case class OperationAbortedProblem(message: String, severity: Severity = Major, category: ProblemCategory = SystemProblem, args: Map[String, Any] = Map(), cause: Option[ProblemCause] = None) extends Problem {
	type T = OperationAbortedProblem
    def withMessage(newMessage: String) = copy(message = newMessage)
	def withSeverity(severity: Severity) = copy(severity = severity)
	def withArg(key: String, value: Any) = copy(args = args + (key -> value))
    def withCause(aCause: ProblemCause) = copy(cause = Some(aCause))
	def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
  }
  case class IllegalOperationProblem(message: String, severity: Severity = Major, category: ProblemCategory = SystemProblem, args: Map[String, Any] = Map(), cause: Option[ProblemCause] = None) extends Problem {
	type T = IllegalOperationProblem
    def withMessage(newMessage: String) = copy(message = newMessage)
	def withSeverity(severity: Severity) = copy(severity = severity)
	def withArg(key: String, value: Any) = copy(args = args + (key -> value))
    def withCause(aCause: ProblemCause) = copy(cause = Some(aCause))
	def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
  }
  case class ArgumentProblem(message: String, severity: Severity = Major, category: ProblemCategory = SystemProblem, args: Map[String, Any] = Map(), cause: Option[ProblemCause] = None) extends Problem {
	type T = ArgumentProblem
    def withMessage(newMessage: String) = copy(message = newMessage)
	def withSeverity(severity: Severity) = copy(severity = severity)
	def withArg(key: String, value: Any) = copy(args = args + (key -> value))
    def withCause(aCause: ProblemCause) = copy(cause = Some(aCause))
	def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
  }
  case class PersistenceProblem(message: String, severity: Severity = Major, category: ProblemCategory = SystemProblem, args: Map[String, Any] = Map(), cause: Option[ProblemCause] = None) extends Problem {
	type T = PersistenceProblem
    def withMessage(newMessage: String) = copy(message = newMessage)
	def withSeverity(severity: Severity) = copy(severity = severity)
	def withArg(key: String, value: Any) = copy(args = args + (key -> value))
    def withCause(aCause: ProblemCause) = copy(cause = Some(aCause))
	def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
  }
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
  }
  case class MultipleMappingProblem(message: String, keysAndMessages: Map[String, String], severity: Severity = Minor, category: ProblemCategory = SystemProblem, args: Map[String, Any] = Map(), cause: Option[ProblemCause] = None) extends MappingProblem with MultiKeyedProblem  {
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
	  .foldLeft(MultipleMappingProblem("Many bad mappings", Map.empty)){case (state,(k, msg)) => state.withBadMapping(k, msg)}
	  .withSeverity(severity and other.severity)
	def prefixWithPath(pathParts: List[String], sep: String = ".") = {
	  pathParts match {
	    case Nil => this
	    case _ =>
	      val path = pathParts.mkString(sep)+sep
	      copy(keysAndMessages = keysAndMessages.toSeq.map{case (k, m) => (path + k) -> m}.toMap)  
	  }
	}
  }
  
  case class NotFoundProblem(message: String, severity: Severity = Minor, category: ProblemCategory = ApplicationProblem, args: Map[String, Any] = Map(), cause: Option[ProblemCause] = None) extends Problem {
	type T = NotFoundProblem
    def withMessage(newMessage: String) = copy(message = newMessage)
	def withSeverity(severity: Severity) = copy(severity = severity)
	def withArg(key: String, value: Any) = copy(args = args + (key -> value))
    def withCause(aCause: ProblemCause) = copy(cause = Some(aCause))
	def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
  }
  case class KeyNotFoundProblem(message: String, severity: Severity = Minor, category: ProblemCategory = ApplicationProblem, args: Map[String, Any] = Map(), cause: Option[ProblemCause] = None) extends Problem {
	type T = KeyNotFoundProblem
    def withMessage(newMessage: String) = copy(message = newMessage)
	def withSeverity(severity: Severity) = copy(severity = severity)
	def withArg(key: String, value: Any) = copy(args = args + (key -> value))
    def withCause(aCause: ProblemCause) = copy(cause = Some(aCause))
	def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
  }
  case class ConstraintViolatedProblem(message: String, severity: Severity = Minor, category: ProblemCategory = ApplicationProblem, args: Map[String, Any] = Map(), cause: Option[ProblemCause] = None) extends Problem {
	type T = ConstraintViolatedProblem
    def withMessage(newMessage: String) = copy(message = newMessage)
	def withSeverity(severity: Severity) = copy(severity = severity)
	def withArg(key: String, value: Any) = copy(args = args + (key -> value))
    def withCause(aCause: ProblemCause) = copy(cause = Some(aCause))
	def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
  }
  case class ParsingProblem(message: String, severity: Severity = Minor, category: ProblemCategory = ApplicationProblem, args: Map[String, Any] = Map(), cause: Option[ProblemCause] = None) extends Problem {
	type T = ParsingProblem
    def withMessage(newMessage: String) = copy(message = newMessage)
	def withSeverity(severity: Severity) = copy(severity = severity)
	def withArg(key: String, value: Any) = copy(args = args + (key -> value))
    def withCause(aCause: ProblemCause) = copy(cause = Some(aCause))
	def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
  }
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
	      val path = pathParts.mkString(sep)+sep
	      copy(key = (path+ sep + key))  
	  }
	}
  }
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
	  .foldLeft(MultipleBadDataProblem("Multiple bad data", Map.empty)){case (state,(k, msg)) => state.withBadData(k, msg)}
	  .withSeverity(severity and other.severity)
	def prefixWithPath(pathParts: List[String], sep: String = ".") = {
	  pathParts match {
	    case Nil => this
	    case _ =>
	      val path = pathParts.mkString(sep)+sep
	      copy(keysAndMessages = keysAndMessages.toSeq.map{case (k, m) => (path + k) -> m}.toMap)  
	  }
	}
  }
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
  case class AlreadyExistsProblem(message: String, severity: Severity = Minor, category: ProblemCategory = ApplicationProblem, args: Map[String, Any] = Map(), cause: Option[ProblemCause] = None) extends Problem {
	type T = AlreadyExistsProblem
    def withMessage(newMessage: String) = copy(message = newMessage)
	def withSeverity(severity: Severity) = copy(severity = severity)
	def withArg(key: String, value: Any) = copy(args = args + (key -> value))
    def withCause(aCause: ProblemCause) = copy(cause = Some(aCause))
	def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
  }
  case class OperationCancelledProblem(message: String, severity: Severity = Minor, category: ProblemCategory = ApplicationProblem, args: Map[String, Any] = Map(), cause: Option[ProblemCause] = None) extends Problem {
	type T = OperationCancelledProblem
    def withMessage(newMessage: String) = copy(message = newMessage)
	def withSeverity(severity: Severity) = copy(severity = severity)
	def withArg(key: String, value: Any) = copy(args = args + (key -> value))
    def withCause(aCause: ProblemCause) = copy(cause = Some(aCause))
	def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
  }
  case class BusinessRuleViolatedProblem(message: String, key: String, severity: Severity = Minor, category: ProblemCategory = ApplicationProblem, args: Map[String, Any] = Map(), cause: Option[ProblemCause] = None) extends BusinessRuleProblem with SingleKeyedProblem{
	type T = BusinessRuleViolatedProblem
    def withMessage(newMessage: String) = copy(message = newMessage)
	def withSeverity(severity: Severity) = copy(severity = severity)
	def withArg(key: String, value: Any) = copy(args = args + (key -> value))
    def withCause(aCause: ProblemCause) = copy(cause = Some(aCause))
	def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
	def addTo(manyViolations: ManyBusinessRulesViolatedProblem) = manyViolations.add(this)
	def add(other: BusinessRuleViolatedProblem) = toMBRV().add(other)
	def toMBRV() = ManyBusinessRulesViolatedProblem("Multiple errors occured", keysAndMessages = Map(key -> message), severity = severity)
  }
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
	  .foldLeft(ManyBusinessRulesViolatedProblem("Many business rules violated", Map.empty)){case (state,(k, msg)) => state.withViolation(k, msg)}
	  .withSeverity(severity and other.severity)
	def prefixWithPath(pathParts: List[String], sep: String = ".") = {
	  pathParts match {
	    case Nil => this
	    case _ =>
	      val path = pathParts.mkString(sep)+sep
	      copy(keysAndMessages = keysAndMessages.toSeq.map{case (k, m) => (path + k) -> m}.toMap)  
	  }
	}
  }
