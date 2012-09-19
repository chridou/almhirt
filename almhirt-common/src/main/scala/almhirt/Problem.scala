package almhirt

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
  
  protected def baseInfo(): StringBuilder = {
      val builder = new StringBuilder()
      builder.append("%s\n".format(this.getClass.getName))
      builder.append("%s\n".format(message))
      builder.append("Category: %s\n".format(severity))
      builder.append("Severity: %s\n".format(severity))
      builder.append("Arguments: %s\n".format(args))
      cause match {
      	case None => 
      	  ()
      	case Some(CauseIsThrowable(exn)) => 
          builder.append("Exception: %s\n".format(exn.toString))
          builder.append("Stacktrace:\n%s\n".format(exn.getStackTraceString))
      	case Some(CauseIsProblem(prob)) => 
          builder.append("Problem: %s\n".format(prob.toString))
      }
      builder
  }
  
  override def toString() = baseInfo.result
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

