package almhirt.validation
package syntax

import scalaz.Scalaz._
import scalaz.NonEmptyList
import scalaz.syntax.Ops


trait ProblemOps0 extends Ops[NonEmptyList[Problem]]{
  import SeverityInstances._
  
  def aggregate(msg: String): AggregateProblem = {
    val severity = self.map(_.severity).concatenate
    if(self.list.exists(p => p.isSystemProblem))
      AggregateProblem(msg, severity = severity, category = SystemProblem, problems = self.list)
    else
      AggregateProblem(msg, severity = severity, category = ApplicationProblem, problems = self.list)
  }

  def aggregate(): AggregateProblem = aggregate("One or more problems. See causes.")
}

trait ToProblemOps {
  implicit def ToProblemOps0(a: NonEmptyList[Problem]) = new ProblemOps0{ def self = a }
}

object ProblemOps extends ToProblemOps