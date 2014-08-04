package almhirt

import scalaz.Validation
import almhirt.common._

package object aggregates {
  type AggregateValidation[+α <: AggregateRoot] = Validation[Problem, α]
}