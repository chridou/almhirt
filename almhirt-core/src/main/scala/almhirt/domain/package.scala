package almhirt

import scalaz.Validation
import almhirt._

package object domain {
  //type EntityValidation[+α <: Entity[_, _]] = ({type λ[α]=Validation[Problem, α]})#λ[α]
  type DomainValidation[+α <: AggregateRoot[_, _]] = Validation[Problem, α]
}