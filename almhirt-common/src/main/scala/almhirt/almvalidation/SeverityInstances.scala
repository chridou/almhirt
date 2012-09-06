package almhirt.almvalidation

import scalaz.Monoid
import almhirt._

trait SeverityInstances {
  implicit def toSeverityMonoid: Monoid[Severity] =
    new Monoid[Severity] {
      def append(a: Severity, b: => Severity): Severity = a and b
      val zero = NoProblem
    }
}