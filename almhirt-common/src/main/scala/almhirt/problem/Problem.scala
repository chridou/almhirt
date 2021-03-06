package almhirt.problem

import scala.language.implicitConversions
import almhirt.tracking.CorrelationId

sealed trait Problem {
  def message: String
  def problemType: ProblemType
  def correlationId: Option[CorrelationId]
  def args: Map[String, Any]
  private[problem] def baseInfo(indentLevel: Int): StringBuilder
}

sealed trait SingleProblem extends Problem {
  def cause: Option[ProblemCause]

  override private[problem] def baseInfo(indentLevel: Int): StringBuilder = {
    val indentation = (0 until (indentLevel * 2)).map(_ ⇒ "\u00A0").mkString
    val builder = new StringBuilder()
    builder.append(indentation + "Problem:\n")
    builder.append(indentation + "Type: %s\n".format(problemType))
    builder.append(indentation + s"CorrelationId: $correlationId\n")
    builder.append(indentation + "Message: %s\n".format(message))
    builder.append(indentation + "Arguments: %s\n".format(args))
    cause match {
      case None ⇒
        ()
      case Some(CauseIsThrowable(HasAThrowable(exn))) ⇒
        builder.append(indentation + "Caused by:\n")
        builder.append(indentation + "  Message: %s\n".format(exn.toString))
        builder.append(indentation + "  Stacktrace:\n%s\n".format(exn.getStackTrace.mkString("\n")))
      case Some(CauseIsThrowable(desc @ HasAThrowableDescribed(_, _, _, _))) ⇒
        builder.append(indentation + "  Description: %s\n".format(desc.toString))
      case Some(CauseIsProblem(prob)) ⇒
        builder.append(indentation + "Caused by:")
        builder.append(prob.baseInfo(indentLevel + 1))
    }
    builder
  }

  override def toString() = baseInfo(0).result

}

sealed trait AggregatedProblem extends Problem {
  def problems: Seq[Problem]
  override private[problem] def baseInfo(indentLevel: Int): StringBuilder = {
    val indentation = (0 until (indentLevel * 2)).map(_ ⇒ "\u00A0").mkString
    val builder = new StringBuilder()
    builder.append(indentation + "Multiple Problems:\n")
    builder.append(indentation + s"CorrelationId: $correlationId\n")
    builder.append(indentation + "Message: %s\n".format(message))
    builder.append(indentation + "Arguments: %s\n".format(args))
    builder.append(indentation + "Aggregated problems:\n")
    problems.zipWithIndex.foreach {
      case (p, i) ⇒ {
        builder.append(p.baseInfo(indentLevel + 1))
      }
    }
    builder
  }

  override def toString(): String = {
    val builder = baseInfo(0)
    builder.result
  }
}

object Problem {
  def apply(msg: String, problemType: ProblemType, args: Map[String, Any] = Map.empty, cause: Option[ProblemCause] = None): Problem =
    SingleProblem(msg, problemType, args, cause)

  def unapply(problem: Problem): Option[(String, ProblemType, Map[String, Any])] =
    Some(problem.message, problem.problemType, problem.args)

  implicit class ProblemOps(self: Problem) {
    def withArg(name: String, value: Any): Problem =
      self match {
        case sp: SingleProblem     ⇒ SingleProblem.withArg(sp, name, value)
        case ap: AggregatedProblem ⇒ AggregatedProblem.withArg(ap, name, value)
      }

    def withCorrelationId(cid: CorrelationId): Problem =
      self match {
        case sp: SingleProblem     ⇒ SingleProblem.withCorrelationId(sp, cid)
        case ap: AggregatedProblem ⇒ AggregatedProblem.withCorrelationId(ap, cid)
      }

    def withLabel(label: String): Problem = self.withArg("label", label)

    def escalate(): Nothing = {
      throw new almhirt.common.EscalatedProblemException(self)
    }
  }
}

object SingleProblem {
  def apply(msg: String, problemType: ProblemType = problemtypes.UnspecifiedProblem, args: Map[String, Any] = Map.empty, cause: Option[ProblemCause] = None): SingleProblem =
    SingleProblemImpl(msg, problemType, None, args, cause)

  def unapply(problem: SingleProblem): Option[(String, ProblemType, Map[String, Any], Option[ProblemCause])] =
    Some(problem.message, problem.problemType, problem.args, problem.cause)

  def unapplyAgainst(problem: SingleProblem, against: ProblemType): Option[SingleProblem] =
    if (problem.problemType == against)
      Some(problem)
    else None

  final case class SingleProblemImpl(
    message: String,
    problemType: ProblemType,
    correlationId: Option[CorrelationId],
    args: Map[String, Any],
    cause: Option[ProblemCause]) extends SingleProblem {
  }

  def causedBy(prob: SingleProblem, aCause: ProblemCause): SingleProblem = prob.asInstanceOf[SingleProblemImpl].copy(cause = Some(aCause))
  def withMessage(prob: SingleProblem, msg: String): SingleProblem = prob.asInstanceOf[SingleProblemImpl].copy(message = msg)
  def withCorrelationId(prob: SingleProblem, cid: CorrelationId): SingleProblem = prob.asInstanceOf[SingleProblemImpl].copy(correlationId = Some(cid))
  def withArg(prob: SingleProblem, name: String, value: Any): SingleProblem = prob.asInstanceOf[SingleProblemImpl].copy(args = prob.args + (name → value))

  implicit def MsgType2SingleProblem(what: (String, ProblemType)): SingleProblem = SingleProblem(what._1, what._2)

  implicit class SingleProblemOps(self: SingleProblem) {
    def causedBy(aCause: ProblemCause): SingleProblem = SingleProblem.causedBy(self, aCause)
    def withMessage(msg: String): SingleProblem = SingleProblem.withMessage(self, msg)
    def withCorrelationId(cid: CorrelationId): SingleProblem = SingleProblem.withCorrelationId(self, cid)
    def withArg(name: String, value: Any): SingleProblem = SingleProblem.withArg(self, name: String, value: Any)
  }
}

object IsSingleProblem {
  def unapply(problem: Problem): Option[SingleProblem] =
    problem match {
      case p: SingleProblem ⇒ Some(p)
      case _                ⇒ None
    }
}

object AggregatedProblem {
  def apply(problems: Seq[Problem], args: Map[String, Any] = Map.empty): AggregatedProblem =
    new AggregateProblemImpl(args, problems, None)

  val empty = apply(Seq.empty, Map.empty)

  def unapply(problem: AggregatedProblem): Option[(String, ProblemType, Map[String, Any], Seq[Problem])] =
    Some(problem.message, problem.problemType, problem.args, problem.problems)

  final case class AggregateProblemImpl(
    args: Map[String, Any],
    problems: Seq[Problem],
    correlationId: Option[CorrelationId]) extends AggregatedProblem {
    override val problemType = problemtypes.MultipleProblems
    override val message = "One or more problems occured"
  }

  def withArg(prob: AggregatedProblem, name: String, value: Any): AggregatedProblem = prob.asInstanceOf[AggregateProblemImpl].copy(args = prob.args + (name → value))
  def withCorrelationId(prob: AggregatedProblem, cid: CorrelationId): AggregatedProblem = prob.asInstanceOf[AggregateProblemImpl].copy(correlationId = Some(cid))
  def add(prob: AggregatedProblem, problem: Problem): AggregatedProblem = prob.asInstanceOf[AggregateProblemImpl].copy(problems = prob.problems :+ problem)

  implicit class AggregateProblemOps(self: AggregatedProblem) {
    def withArg(name: String, value: Any): AggregatedProblem = AggregatedProblem.withArg(self, name: String, value: Any)
    def withCorrelationId(cid: CorrelationId): AggregatedProblem = AggregatedProblem.withCorrelationId(self, cid)
    def add(problem: Problem): AggregatedProblem = AggregatedProblem.add(self, problem: Problem)
    def singleProblemIfApplicable: Problem =
      self.problems match {
        case Seq(x) ⇒ x
        case _      ⇒ self
      }
  }

}

