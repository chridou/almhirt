package almhirt.validation

import scalaz.NonEmptyList

trait Problem{
  type T <: Problem
  def message: String
  def severity: Severity
  def exception: Option[Throwable]
  def args: Map[String, Any]
 
  def withException(err: Throwable): T
  def withSeverity(severity: Severity): T
  def withArg(key: String, value: Any): T
  def withMessage(newMessage: String): T
  def mapMessage(mapOp: String => String): T
}

trait SystemProblem extends Problem
trait ApplicationProblem extends Problem
trait SecurityProblem extends ApplicationProblem
trait BadDataProblem extends ApplicationProblem
sealed trait ProblemCategory

object Problem extends ProblemImplicits {

  def badData(key: String, message: String) =
    SingleBadDataProblem(message, key)
  
  val defaultSystemProblem = UnspecifiedSystemProblem("unspecified system problem")
  val defaultApplicationProblem = UnspecifiedApplicationProblem("unspecified application problem")
  val defaultProblem = defaultSystemProblem
  
  case class UnspecifiedSystemProblem(message: String, severity: Severity = Major, exception: Option[Throwable] = None, args: Map[String, Any] = Map()) extends SystemProblem {
	type T = UnspecifiedSystemProblem
    def withMessage(newMessage: String) = copy(message = newMessage)
    def withException(err: Throwable) = copy(exception = Some(err))
	def withSeverity(severity: Severity) = copy(severity = severity)
	def withArg(key: String, value: Any) = copy(args = args + (key -> value))
	def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
  }
  case class NoConnectionProblem(message: String, severity: Severity = Major, exception: Option[Throwable] = None, args: Map[String, Any] = Map()) extends SystemProblem {
	type T = NoConnectionProblem
    def withMessage(newMessage: String) = copy(message = newMessage)
    def withException(err: Throwable) = copy(exception = Some(err))
	def withSeverity(severity: Severity) = copy(severity = severity)
	def withArg(key: String, value: Any) = copy(args = args + (key -> value))
	def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
  }
  case class OperationTimedOutProblem(message: String, severity: Severity = Major, exception: Option[Throwable] = None, args: Map[String, Any] = Map()) extends SystemProblem {
	type T = OperationTimedOutProblem
    def withMessage(newMessage: String) = copy(message = newMessage)
    def withException(err: Throwable) = copy(exception = Some(err))
	def withSeverity(severity: Severity) = copy(severity = severity)
	def withArg(key: String, value: Any) = copy(args = args + (key -> value))
	def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
  }
  case class OperationAbortedProblem(message: String, severity: Severity = Major, exception: Option[Throwable] = None, args: Map[String, Any] = Map()) extends SystemProblem {
	type T = OperationAbortedProblem
    def withMessage(newMessage: String) = copy(message = newMessage)
    def withException(err: Throwable) = copy(exception = Some(err))
	def withSeverity(severity: Severity) = copy(severity = severity)
	def withArg(key: String, value: Any) = copy(args = args + (key -> value))
	def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
  }
  case class IllegalOperationProblem(message: String, severity: Severity = Major, exception: Option[Throwable] = None, args: Map[String, Any] = Map()) extends SystemProblem {
	type T = IllegalOperationProblem
    def withMessage(newMessage: String) = copy(message = newMessage)
    def withException(err: Throwable) = copy(exception = Some(err))
	def withSeverity(severity: Severity) = copy(severity = severity)
	def withArg(key: String, value: Any) = copy(args = args + (key -> value))
	def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
  }
  case class PersistenceProblem(message: String, severity: Severity = Major, exception: Option[Throwable] = None, args: Map[String, Any] = Map()) extends SystemProblem {
	type T = PersistenceProblem
    def withMessage(newMessage: String) = copy(message = newMessage)
    def withException(err: Throwable) = copy(exception = Some(err))
	def withSeverity(severity: Severity) = copy(severity = severity)
	def withArg(key: String, value: Any) = copy(args = args + (key -> value))
	def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
  }
  
  case class UnspecifiedApplicationProblem(message: String, severity: Severity = Major, exception: Option[Throwable] = None, args: Map[String, Any] = Map()) extends ApplicationProblem {
	type T = UnspecifiedApplicationProblem
    def withMessage(newMessage: String) = copy(message = newMessage)
    def withException(err: Throwable) = copy(exception = Some(err))
	def withSeverity(severity: Severity) = copy(severity = severity)
	def withArg(key: String, value: Any) = copy(args = args + (key -> value))
	def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
  }
  case class NotFoundProblem(message: String, severity: Severity = Major, exception: Option[Throwable] = None, args: Map[String, Any] = Map()) extends ApplicationProblem {
	type T = NotFoundProblem
    def withMessage(newMessage: String) = copy(message = newMessage)
    def withException(err: Throwable) = copy(exception = Some(err))
	def withSeverity(severity: Severity) = copy(severity = severity)
	def withArg(key: String, value: Any) = copy(args = args + (key -> value))
	def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
  }
  case class SingleBadDataProblem(message: String, key: String = "unknown", severity: Severity = Minor, exception: Option[Throwable] = None, args: Map[String, Any] = Map()) extends BadDataProblem {
	type T = SingleBadDataProblem
    def withMessage(newMessage: String) = copy(message = newMessage)
    def withException(err: Throwable) = copy(exception = Some(err))
	def withSeverity(severity: Severity) = copy(severity = severity)
	def withArg(key: String, value: Any) = copy(args = args + (key -> value))
	def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
	def addTo(multipleBadData: MultipleSingleBadDataProblem) = multipleBadData.add(this)
	def add(other: SingleBadDataProblem) = toMultipleBadData().add(other)
	def toMultipleBadData() = MultipleSingleBadDataProblem("Multiple errors occured", keysAndMessages = Map(key -> message), severity = severity)
  }
  case class MultipleSingleBadDataProblem(message: String, keysAndMessages: Map[String, String], severity: Severity = Minor, exception: Option[Throwable] = None, args: Map[String, Any] = Map()) extends BadDataProblem {
	type T = MultipleSingleBadDataProblem
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
	def combineWith(other: MultipleSingleBadDataProblem) =
	  other.keysAndMessages.toSeq
	  .foldLeft(this){case (state,(k, msg)) => state.withBadData(k, msg)}
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
  case class CollisionProblem(message: String, severity: Severity = Major, exception: Option[Throwable] = None, args: Map[String, Any] = Map()) extends ApplicationProblem {
	type T = CollisionProblem
    def withMessage(newMessage: String) = copy(message = newMessage)
    def withException(err: Throwable) = copy(exception = Some(err))
	def withSeverity(severity: Severity) = copy(severity = severity)
	def withArg(key: String, value: Any) = copy(args = args + (key -> value))
	def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
  }
  case class NotAuthorizedProblem(message: String, severity: Severity = Major, exception: Option[Throwable] = None, args: Map[String, Any] = Map()) extends SecurityProblem {
	type T = NotAuthorizedProblem
    def withMessage(newMessage: String) = copy(message = newMessage)
    def withException(err: Throwable) = copy(exception = Some(err))
	def withSeverity(severity: Severity) = copy(severity = severity)
	def withArg(key: String, value: Any) = copy(args = args + (key -> value))
	def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
  }
  case class NotAuthenticatedProblem(message: String, severity: Severity = Major, exception: Option[Throwable] = None, args: Map[String, Any] = Map()) extends SecurityProblem {
	type T = NotAuthenticatedProblem
    def withMessage(newMessage: String) = copy(message = newMessage)
    def withException(err: Throwable) = copy(exception = Some(err))
	def withSeverity(severity: Severity) = copy(severity = severity)
	def withArg(key: String, value: Any) = copy(args = args + (key -> value))
	def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
  }
  case class AlreadyExistsProblem(message: String, severity: Severity = Major, exception: Option[Throwable] = None, args: Map[String, Any] = Map()) extends ApplicationProblem {
	type T = AlreadyExistsProblem
    def withMessage(newMessage: String) = copy(message = newMessage)
    def withException(err: Throwable) = copy(exception = Some(err))
	def withSeverity(severity: Severity) = copy(severity = severity)
	def withArg(key: String, value: Any) = copy(args = args + (key -> value))
	def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
  }
  case class OperationCancelledProblem(message: String, severity: Severity = Major, exception: Option[Throwable] = None, args: Map[String, Any] = Map()) extends ApplicationProblem {
	type T = OperationCancelledProblem
    def withMessage(newMessage: String) = copy(message = newMessage)
    def withException(err: Throwable) = copy(exception = Some(err))
	def withSeverity(severity: Severity) = copy(severity = severity)
	def withArg(key: String, value: Any) = copy(args = args + (key -> value))
	def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
  }
  case class BusinessRuleViolatedProblem(message: String, severity: Severity = Major, exception: Option[Throwable] = None, args: Map[String, Any] = Map()) extends ApplicationProblem {
	type T = BusinessRuleViolatedProblem
    def withMessage(newMessage: String) = copy(message = newMessage)
    def withException(err: Throwable) = copy(exception = Some(err))
	def withSeverity(severity: Severity) = copy(severity = severity)
	def withArg(key: String, value: Any) = copy(args = args + (key -> value))
	def mapMessage(mapOp: String => String) = copy(message = mapOp(message))
  }
  
}






