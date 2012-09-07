package almhirt.problem

import almhirt.NoProblem
import almhirt.Severity
import scalaz.Monoid

trait SeverityInstances {
  implicit def toSeverityMonoid: Monoid[Severity] =
    new Monoid[Severity] {
      def append(a: Severity, b: => Severity): Severity = a and b
      val zero = NoProblem
    }
}