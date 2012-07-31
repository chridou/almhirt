package almhirt.validation

import scalaz.Semigroup
import scalaz.{Validation, ValidationNEL, Success, Failure}
import scalaz.syntax.validation._
import Problem._

trait ProblemImplicits {
  implicit def toSeveritySemigroup: Semigroup[Severity] =
    new Semigroup[Severity] {
      def append(a: Severity, b: => Severity): Severity = 
        (a,b) match {
	      case(Critical,_) => Critical
	      case(_,Critical) => Critical
	      case(Major,_) => Major
	      case(_,Major) => Major
	      case _ => Minor
        }
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
  
}
