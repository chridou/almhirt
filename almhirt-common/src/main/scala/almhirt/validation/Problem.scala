package almhirt.validation

import scalaz.{NonEmptyList, Show}
import scala.collection.mutable.StringBuilder

trait Problem{
  type T <: Problem
  def message: String
  def severity: Severity
  def category: ProblemCategory
  def exception: Option[Throwable]
  def args: Map[String, Any]
  def causes: List[Problem]
 
  def withException(err: Throwable): T
  def withSeverity(severity: Severity): T
  def withArg(key: String, value: Any): T
  def withMessage(newMessage: String): T
  def mapMessage(mapOp: String => String): T
  
  def isSystemProblem = category == SystemProblem
}

trait SingleKeyedProblem { self: Problem =>
  def key: String
}

trait MultiKeyedProblem { self: Problem =>
  def keysAndMessages: Map[String, String]
}

trait MappingProblem extends Problem

trait SecurityProblem extends Problem
trait BadDataProblem extends Problem
trait BusinessRuleProblem extends Problem

object Problem extends ProblemImplicits {

  def badData(key: String, message: String) =
    SingleBadDataProblem(message, key)
  
  val defaultSystemProblem = UnspecifiedProblem("unspecified system problem", category = SystemProblem)
  val defaultApplicationProblem = UnspecifiedProblem("unspecified application problem", category = ApplicationProblem)
  val defaultProblem = defaultSystemProblem
  
  case class UnspecifiedProblem(message: String, severity: Severity = Major, category: ProblemCategory = SystemProblem, exception: Option[Throwable] = None, args: Map[String, Any] = Map(), causes: List[Problem] = Nil) extends Problem {
	type T = UnspecifiedProblem
    def withMessage(newMessage: String) = copy(message = newMessage)
    def withException(err: Throwable) = copy(exception = Some(err))
	def withSeverity(severity: Severity) = copy(severity = severity)
	def withArg(key: String, value: Any) = copy(args = args + (key -> value))
	def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
  }
  case class RegistrationProblem(message: String, severity: Severity = Major, category: ProblemCategory = SystemProblem, exception: Option[Throwable] = None, args: Map[String, Any] = Map(), causes: List[Problem] = Nil) extends Problem {
	type T = RegistrationProblem
    def withMessage(newMessage: String) = copy(message = newMessage)
    def withException(err: Throwable) = copy(exception = Some(err))
	def withSeverity(severity: Severity) = copy(severity = severity)
	def withArg(key: String, value: Any) = copy(args = args + (key -> value))
	def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
  }
  case class NoConnectionProblem(message: String, severity: Severity = Major, category: ProblemCategory = SystemProblem, exception: Option[Throwable] = None, args: Map[String, Any] = Map(), causes: List[Problem] = Nil) extends Problem {
	type T = NoConnectionProblem
    def withMessage(newMessage: String) = copy(message = newMessage)
    def withException(err: Throwable) = copy(exception = Some(err))
	def withSeverity(severity: Severity) = copy(severity = severity)
	def withArg(key: String, value: Any) = copy(args = args + (key -> value))
	def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
  }
  case class OperationTimedOutProblem(message: String, severity: Severity = Major, category: ProblemCategory = SystemProblem, exception: Option[Throwable] = None, args: Map[String, Any] = Map(), causes: List[Problem] = Nil) extends Problem {
	type T = OperationTimedOutProblem
    def withMessage(newMessage: String) = copy(message = newMessage)
    def withException(err: Throwable) = copy(exception = Some(err))
	def withSeverity(severity: Severity) = copy(severity = severity)
	def withArg(key: String, value: Any) = copy(args = args + (key -> value))
	def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
  }
  case class OperationAbortedProblem(message: String, severity: Severity = Major, category: ProblemCategory = SystemProblem, exception: Option[Throwable] = None, args: Map[String, Any] = Map(), causes: List[Problem] = Nil) extends Problem {
	type T = OperationAbortedProblem
    def withMessage(newMessage: String) = copy(message = newMessage)
    def withException(err: Throwable) = copy(exception = Some(err))
	def withSeverity(severity: Severity) = copy(severity = severity)
	def withArg(key: String, value: Any) = copy(args = args + (key -> value))
	def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
  }
  case class IllegalOperationProblem(message: String, severity: Severity = Major, category: ProblemCategory = SystemProblem, exception: Option[Throwable] = None, args: Map[String, Any] = Map(), causes: List[Problem] = Nil) extends Problem {
	type T = IllegalOperationProblem
    def withMessage(newMessage: String) = copy(message = newMessage)
    def withException(err: Throwable) = copy(exception = Some(err))
	def withSeverity(severity: Severity) = copy(severity = severity)
	def withArg(key: String, value: Any) = copy(args = args + (key -> value))
	def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
  }
  case class ArgumentProblem(message: String, severity: Severity = Major, category: ProblemCategory = SystemProblem, exception: Option[Throwable] = None, args: Map[String, Any] = Map(), causes: List[Problem] = Nil) extends Problem {
	type T = ArgumentProblem
    def withMessage(newMessage: String) = copy(message = newMessage)
    def withException(err: Throwable) = copy(exception = Some(err))
	def withSeverity(severity: Severity) = copy(severity = severity)
	def withArg(key: String, value: Any) = copy(args = args + (key -> value))
	def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
  }
  case class PersistenceProblem(message: String, severity: Severity = Major, category: ProblemCategory = SystemProblem, exception: Option[Throwable] = None, args: Map[String, Any] = Map(), causes: List[Problem] = Nil) extends Problem {
	type T = PersistenceProblem
    def withMessage(newMessage: String) = copy(message = newMessage)
    def withException(err: Throwable) = copy(exception = Some(err))
	def withSeverity(severity: Severity) = copy(severity = severity)
	def withArg(key: String, value: Any) = copy(args = args + (key -> value))
	def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
  }
  case class SingleMappingProblem(message: String, key: String = "unknown", category: ProblemCategory = SystemProblem, severity: Severity = Minor, exception: Option[Throwable] = None, args: Map[String, Any] = Map(), causes: List[Problem] = Nil) extends MappingProblem with SingleKeyedProblem {
	type T = SingleMappingProblem
    def withMessage(newMessage: String) = copy(message = newMessage)
    def withException(err: Throwable) = copy(exception = Some(err))
	def withSeverity(severity: Severity) = copy(severity = severity)
	def withArg(key: String, value: Any) = copy(args = args + (key -> value))
	def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
	def addTo(multipleMappingProblem: MultipleMappingProblem) = multipleMappingProblem.add(this)
	def add(other: SingleMappingProblem) = toMultipleMappingProblem().add(other)
	def toMultipleMappingProblem() = MultipleMappingProblem("Multiple errors occured", keysAndMessages = Map(key -> message), severity = severity)
  }
  case class MultipleMappingProblem(message: String, keysAndMessages: Map[String, String], severity: Severity = Minor, category: ProblemCategory = SystemProblem, exception: Option[Throwable] = None, args: Map[String, Any] = Map(), causes: List[Problem] = Nil) extends MappingProblem with MultiKeyedProblem  {
	type T = MultipleMappingProblem
    def withMessage(newMessage: String) = copy(message = newMessage)
    def withException(err: Throwable) = copy(exception = Some(err))
	def withSeverity(severity: Severity) = copy(severity = severity)
	def withArg(key: String, value: Any) = copy(args = args + (key -> value))
	def withBadMapping(key: String, messageForKey: String): T =
	  keysAndMessages.get(key) match {
	  case Some(_) => withBadMapping(key + "_", messageForKey)
	  case None => copy(keysAndMessages = keysAndMessages + (key -> messageForKey))
	}
	def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
	def add(badMapping: SingleMappingProblem) = withBadMapping(badMapping.key, badMapping.message).withSeverity(severity and badMapping.severity)
	def combineWith(other: MultipleMappingProblem) =
	  other.keysAndMessages.toSeq
	  .foldLeft(this){case (state,(k, msg)) => state.withBadMapping(k, msg)}
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
  
  case class NotFoundProblem(message: String, severity: Severity = Minor, category: ProblemCategory = ApplicationProblem, exception: Option[Throwable] = None, args: Map[String, Any] = Map(), causes: List[Problem] = Nil) extends Problem {
	type T = NotFoundProblem
    def withMessage(newMessage: String) = copy(message = newMessage)
    def withException(err: Throwable) = copy(exception = Some(err))
	def withSeverity(severity: Severity) = copy(severity = severity)
	def withArg(key: String, value: Any) = copy(args = args + (key -> value))
	def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
  }
  case class KeyNotFoundProblem(message: String, severity: Severity = Minor, category: ProblemCategory = ApplicationProblem, exception: Option[Throwable] = None, args: Map[String, Any] = Map(), causes: List[Problem] = Nil) extends Problem {
	type T = KeyNotFoundProblem
    def withMessage(newMessage: String) = copy(message = newMessage)
    def withException(err: Throwable) = copy(exception = Some(err))
	def withSeverity(severity: Severity) = copy(severity = severity)
	def withArg(key: String, value: Any) = copy(args = args + (key -> value))
	def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
  }
  case class ConstraintViolatedProblem(message: String, severity: Severity = Minor, category: ProblemCategory = ApplicationProblem, exception: Option[Throwable] = None, args: Map[String, Any] = Map(), causes: List[Problem] = Nil) extends Problem {
	type T = ConstraintViolatedProblem
    def withMessage(newMessage: String) = copy(message = newMessage)
    def withException(err: Throwable) = copy(exception = Some(err))
	def withSeverity(severity: Severity) = copy(severity = severity)
	def withArg(key: String, value: Any) = copy(args = args + (key -> value))
	def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
  }
  case class SingleBadDataProblem(message: String, key: String = "unknown", severity: Severity = Minor, category: ProblemCategory = ApplicationProblem, exception: Option[Throwable] = None, args: Map[String, Any] = Map(), causes: List[Problem] = Nil) extends BadDataProblem with SingleKeyedProblem {
	type T = SingleBadDataProblem
    def withMessage(newMessage: String) = copy(message = newMessage)
    def withException(err: Throwable) = copy(exception = Some(err))
	def withSeverity(severity: Severity) = copy(severity = severity)
	def withArg(key: String, value: Any) = copy(args = args + (key -> value))
	def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
	def addTo(multipleBadData: MultipleBadDataProblem) = multipleBadData.add(this)
	def add(other: SingleBadDataProblem) = toMBD().add(other)
	def toMBD() = MultipleBadDataProblem("Multiple errors occured", keysAndMessages = Map(key -> message), severity = severity, causes = List(this))
	def prefixWithPath(pathParts: List[String], sep: String = ".") = {
	  pathParts match {
	    case Nil => this
	    case _ =>
	      val path = pathParts.mkString(sep)+sep
	      copy(key = (path+ sep + key))  
	  }
	}
	def toSystemProblem() = SingleMappingProblem(message, key = key, severity = severity, args = args, exception = exception, causes = List(this))
  }
  case class MultipleBadDataProblem(message: String, keysAndMessages: Map[String, String], severity: Severity = Minor, category: ProblemCategory = ApplicationProblem, exception: Option[Throwable] = None, args: Map[String, Any] = Map(), causes: List[Problem] = Nil) extends BadDataProblem with MultiKeyedProblem {
	type T = MultipleBadDataProblem
    def withMessage(newMessage: String) = copy(message = newMessage)
    def withException(err: Throwable) = copy(exception = Some(err))
	def withSeverity(severity: Severity) = copy(severity = severity)
	def withArg(key: String, value: Any) = copy(args = args + (key -> value))
	def withBadData(key: String, messageForKey: String): T =
	  keysAndMessages.get(key) match {
	  case Some(_) => withBadData(key + "_", messageForKey)
	  case None => copy(keysAndMessages = keysAndMessages + (key -> messageForKey))
	}
	def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
	def add(badData: SingleBadDataProblem) = withBadData(badData.key, badData.message).withSeverity(severity and badData.severity)
	def combineWith(other: MultipleBadDataProblem) =
	  other.keysAndMessages.toSeq
	  .foldLeft(this){case (state,(k, msg)) => state.withBadData(k, msg)}
	  .withSeverity(severity and other.severity)
	  .copy(causes = this.causes ++ other.causes)
	def prefixWithPath(pathParts: List[String], sep: String = ".") = {
	  pathParts match {
	    case Nil => this
	    case _ =>
	      val path = pathParts.mkString(sep)+sep
	      copy(keysAndMessages = keysAndMessages.toSeq.map{case (k, m) => (path + k) -> m}.toMap)  
	  }
	}
	def toSystemProblem() = MultipleMappingProblem(message, keysAndMessages = keysAndMessages, severity = severity, args = args, exception = exception, causes = List(this))
  }
  case class CollisionProblem(message: String, severity: Severity = Minor, category: ProblemCategory = ApplicationProblem, exception: Option[Throwable] = None, args: Map[String, Any] = Map(), causes: List[Problem] = Nil) extends Problem {
	type T = CollisionProblem
    def withMessage(newMessage: String) = copy(message = newMessage)
    def withException(err: Throwable) = copy(exception = Some(err))
	def withSeverity(severity: Severity) = copy(severity = severity)
	def withArg(key: String, value: Any) = copy(args = args + (key -> value))
	def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
  }
  case class NotAuthorizedProblem(message: String, severity: Severity = Minor, category: ProblemCategory = ApplicationProblem, exception: Option[Throwable] = None, args: Map[String, Any] = Map(), causes: List[Problem] = Nil) extends SecurityProblem {
	type T = NotAuthorizedProblem
    def withMessage(newMessage: String) = copy(message = newMessage)
    def withException(err: Throwable) = copy(exception = Some(err))
	def withSeverity(severity: Severity) = copy(severity = severity)
	def withArg(key: String, value: Any) = copy(args = args + (key -> value))
	def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
  }
  case class NotAuthenticatedProblem(message: String, severity: Severity = Minor, category: ProblemCategory = ApplicationProblem, exception: Option[Throwable] = None, args: Map[String, Any] = Map(), causes: List[Problem] = Nil) extends SecurityProblem {
	type T = NotAuthenticatedProblem
    def withMessage(newMessage: String) = copy(message = newMessage)
    def withException(err: Throwable) = copy(exception = Some(err))
	def withSeverity(severity: Severity) = copy(severity = severity)
	def withArg(key: String, value: Any) = copy(args = args + (key -> value))
	def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
  }
  case class AlreadyExistsProblem(message: String, severity: Severity = Minor, category: ProblemCategory = ApplicationProblem, exception: Option[Throwable] = None, args: Map[String, Any] = Map(), causes: List[Problem] = Nil) extends Problem {
	type T = AlreadyExistsProblem
    def withMessage(newMessage: String) = copy(message = newMessage)
    def withException(err: Throwable) = copy(exception = Some(err))
	def withSeverity(severity: Severity) = copy(severity = severity)
	def withArg(key: String, value: Any) = copy(args = args + (key -> value))
	def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
  }
  case class OperationCancelledProblem(message: String, severity: Severity = Minor, category: ProblemCategory = ApplicationProblem, exception: Option[Throwable] = None, args: Map[String, Any] = Map(), causes: List[Problem] = Nil) extends Problem {
	type T = OperationCancelledProblem
    def withMessage(newMessage: String) = copy(message = newMessage)
    def withException(err: Throwable) = copy(exception = Some(err))
	def withSeverity(severity: Severity) = copy(severity = severity)
	def withArg(key: String, value: Any) = copy(args = args + (key -> value))
	def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
  }
  case class BusinessRuleViolatedProblem(message: String, key: String, severity: Severity = Minor, category: ProblemCategory = ApplicationProblem, exception: Option[Throwable] = None, args: Map[String, Any] = Map(), causes: List[Problem] = Nil) extends BusinessRuleProblem with SingleKeyedProblem{
	type T = BusinessRuleViolatedProblem
    def withMessage(newMessage: String) = copy(message = newMessage)
    def withException(err: Throwable) = copy(exception = Some(err))
	def withSeverity(severity: Severity) = copy(severity = severity)
	def withArg(key: String, value: Any) = copy(args = args + (key -> value))
	def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
	def addTo(manyViolations: ManyBusinessRulesViolatedProblem) = manyViolations.add(this)
	def add(other: BusinessRuleViolatedProblem) = toMBRV().add(other)
	def toMBRV() = ManyBusinessRulesViolatedProblem("Multiple errors occured", keysAndMessages = Map(key -> message), severity = severity)
  }
  case class ManyBusinessRulesViolatedProblem(message: String, keysAndMessages: Map[String, String], severity: Severity = Minor, category: ProblemCategory = ApplicationProblem, exception: Option[Throwable] = None, args: Map[String, Any] = Map(), causes: List[Problem] = Nil) extends BusinessRuleProblem with MultiKeyedProblem {
	type T = ManyBusinessRulesViolatedProblem
    def withMessage(newMessage: String) = copy(message = newMessage)
    def withException(err: Throwable) = copy(exception = Some(err))
	def withSeverity(severity: Severity) = copy(severity = severity)
	def withArg(key: String, value: Any) = copy(args = args + (key -> value))
	def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
	def withViolation(key: String, messageForKey: String): T =
	  keysAndMessages.get(key) match {
	  case Some(_) => withViolation(key + "_", messageForKey)
	  case None => copy(keysAndMessages = keysAndMessages + (key -> messageForKey))
	}
	def add(violation: BusinessRuleViolatedProblem) = withViolation(violation.key, violation.message).withSeverity(severity and violation.severity)
	def combineWith(other: ManyBusinessRulesViolatedProblem) =
	  other.keysAndMessages.toSeq
	  .foldLeft(this){case (state,(k, msg)) => state.withViolation(k, msg)}
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
}






