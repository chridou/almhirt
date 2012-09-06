package almhirt.validation

import scalaz.Monoid

trait SeverityInstances {
  implicit def toSeverityMonoid: Monoid[Severity] =
    new Monoid[Severity] {
      def append(a: Severity, b: => Severity): Severity = a and b
      val zero = NoProblem
    }
}

object SeverityInstances extends SeverityInstances