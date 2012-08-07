package almhirt.validation

import scalaz._
import Scalaz._
import Problem._

trait ProblemImplicits {
  implicit def toSeverityMonoid: Monoid[Severity] =
    new Monoid[Severity] {
      def append(a: Severity, b: => Severity): Severity = a and b
      def zero = NoProblem
    }
  
  implicit def toMBDSemiGroup: Semigroup[MultipleBadDataProblem] =
    new Semigroup[MultipleBadDataProblem] {
      def append(a: MultipleBadDataProblem, b: => MultipleBadDataProblem): MultipleBadDataProblem = a combineWith b
  }
  
  implicit def toMultipleMappingSemiGroup: Semigroup[MultipleMappingProblem] =
    new Semigroup[MultipleMappingProblem] {
      def append(a: MultipleMappingProblem, b: => MultipleMappingProblem): MultipleMappingProblem = a combineWith b
  }

  implicit def toManyBusinessRulesViolatedSemiGroup: Semigroup[ManyBusinessRulesViolatedProblem] =
    new Semigroup[ManyBusinessRulesViolatedProblem] {
      def append(a: ManyBusinessRulesViolatedProblem, b: => ManyBusinessRulesViolatedProblem): ManyBusinessRulesViolatedProblem = a combineWith b
  }
  
  final class SeqProblemW(nel: NonEmptyList[Problem]) {
    def aggregate(msg: String): Problem = {
      val severity = nel.map(_.severity).concatenate
      if(nel.list.exists(p => p.isInstanceOf[SystemProblem]))
        UnspecifiedSystemProblem(msg, severity = severity, causes = nel.list)
      else
        UnspecifiedApplicationProblem(msg, severity = severity, causes = nel.list)
    }
  }
}
