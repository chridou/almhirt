package almhirt.core

import scalaz.Validation
import almhirt.common._

package object types {
  type DomainValidation[+α <: AggregateRoot] = Validation[Problem, α]
}