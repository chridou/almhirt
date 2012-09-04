package almhirt.validation


trait Problem{
  type T <: Problem
  def message: String
  def severity: Severity
  def category: ProblemCategory
  def args: Map[String, Any]
  def cause: Option[ProblemCause]
 
  def withSeverity(severity: Severity): T
  def withArg(key: String, value: Any): T
  def withMessage(newMessage: String): T
  def withCause(cause: ProblemCause): T
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

