package almhirt.problem

import almhirt.ApplicationProblem
import almhirt.ProblemCategory
import scalaz.Monoid

trait ProblemCategoryInstances {
  implicit def toProbelCategoryMonoid: Monoid[ProblemCategory] =
    new Monoid[ProblemCategory] {
      def append(a: ProblemCategory, b: => ProblemCategory): ProblemCategory = a and b
      val zero = ApplicationProblem
    }
}
