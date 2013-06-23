package almhirt.problem

trait Problem {
  def message: String
  def group: ProblemGroup
  def severity: Severity
  def category: ProblemCategory
  def args: Map[String, Any]
  def cause: Option[ProblemCause]
  def aggregated: Seq[Problem]

  def withSeverity(severity: Severity): Problem
  def withGroup(newGroup: ProblemGroup): Problem
  def withArg(key: String, value: Any): Problem
  def withMessage(newMessage: String): Problem
  def withCause(cause: ProblemCause): Problem
  def mapMessage(mapOp: String => String): Problem

  def isSystemProblem = category == SystemProblem
  
  protected def baseInfo(): StringBuilder = {
    val builder = new StringBuilder()
    builder.append("%s\n".format(this.getClass.getName))
    builder.append("%s\n".format(message))
    builder.append("Category: %s\n".format(category))
    builder.append("Severity: %s\n".format(severity))
    builder.append("Arguments: %s\n".format(args))
    cause match {
      case None =>
        ()
      case Some(CauseIsThrowable(HasAThrowable(exn))) =>
        builder.append("Message: %s\n".format(exn.toString))
        builder.append("Stacktrace:\n%s\n".format(exn.getStackTraceString))
      case Some(CauseIsThrowable(desc @ HasAThrowableDescribed(_, _, _, _))) =>
        builder.append("Description: %s\n".format(desc.toString))
      case Some(CauseIsProblem(prob)) =>
        builder.append("Problem: %s\n".format(prob.toString))
    }
    builder
  }

  override def toString() = baseInfo.result

}

object Problem {
  case class ProblemImpl(
      message: String, 
      group: ProblemGroup,  
      severity: Severity, 
      category: ProblemCategory,
      args: Map[String, Any],
      cause: Option[ProblemCause],
      aggregated: Seq[Problem]) extends Problem {
  def withSeverity(severity: Severity): Problem
  def withGroup(newGroup: ProblemGroup): Problem
  def withArg(key: String, value: Any): Problem
  def withMessage(newMessage: String): Problem
  def withCause(cause: ProblemCause): Problem
  def mapMessage(mapOp: String => String): Problem
  
  
  
  implicit class ProblemOps[T <: Problem](prob: T) {
    def withIdentifier(ident: String): T = almhirt.problem.funs.withIdentifier(prob, ident)

    def markLogged(): T = prob.withArg("isLogged", true).asInstanceOf[T]
    def isLogged(): Boolean = prob.args.contains("isLogged") && prob.args("isLogged") == true

    def setTag(tag: String): T = prob.withArg("tag", tag).asInstanceOf[T]
    def isTagged(): Boolean = prob.args.contains("tag") && prob.args("tag").isInstanceOf[String]
    def tryGetTag(): Option[String] = if (isTagged) Some(prob.args("tag").asInstanceOf[String]) else None
  }
}

object IsSystemProblem {
  def unapply[T <: Problem](prob: T): Option[T] =
    if(prob.isSystemProblem) Some(prob) else None
}

object IsApplicationProblem {
  def unapply[T <: Problem](prob: T): Option[T] =
    if(!prob.isSystemProblem) Some(prob) else None
}


