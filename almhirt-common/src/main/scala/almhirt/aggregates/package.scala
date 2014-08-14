package almhirt

import scalaz.Validation
import almhirt.common._

package object aggregates {
  type AggregateValidation[+α <: AggregateRoot] = Validation[Problem, α]
  type PimpedAggregateValidation[+α <: AggregateRoot] = Validation[Problem, AggregateRootState[α]]
}