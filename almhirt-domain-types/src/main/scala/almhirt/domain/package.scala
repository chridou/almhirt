package almhirt

import scalaz.Validation
import almhirt.common._

package object domain {
  type DomainValidation[+α <: AggregateRoot[_, _]] = Validation[Problem, α]
}