package almhirt.validation
package syntax

import scalaz.Scalaz._
import scalaz.NonEmptyList
import scalaz.syntax.Ops


trait ProblemOps0 extends Ops[NonEmptyList[Problem]]{
  import SeverityInstances._
  
  implicit def nelProblemtoNelProblemW(probs: NonEmptyList[Problem]) = new NelProblemW(probs)
  final class NelProblemW(nel: NonEmptyList[Problem]) {
    def aggregate(msg: String): Problem = {
      val severity = nel.map(_.severity).concatenate
      if(nel.list.exists(p => p.isSystemProblem))
        UnspecifiedProblem(msg, severity = severity, category = SystemProblem, causes = nel.list)
      else
        UnspecifiedProblem(msg, severity = severity, category = ApplicationProblem, causes = nel.list)
    }
    def aggregate(): Problem = aggregate("One or more problems. See causes.")
  }
}

trait ToProblemOps {
  implicit def ToProblemOps0(a: NonEmptyList[Problem]) = new ProblemOps0{ def self = a }
}