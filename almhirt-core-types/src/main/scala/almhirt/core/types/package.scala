package almhirt.core

import scalaz.Validation
import almhirt.common._
import almhirt.aggregates.AggregateRoot

package object types {
  type DomainValidation[+α <: AggregateRoot] = Validation[Problem, α]
}