package almhirt.almvalidation

import scalaz.Monoid
import almhirt._

trait ProblemCategoryInstances {
  implicit def toProbelCategoryMonoid: Monoid[ProblemCategory] =
    new Monoid[ProblemCategory] {
      def append(a: ProblemCategory, b: => ProblemCategory): ProblemCategory = a and b
      val zero = ApplicationProblem
    }
}
