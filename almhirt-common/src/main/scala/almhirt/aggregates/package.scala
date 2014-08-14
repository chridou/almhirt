package almhirt

import scalaz.Validation
import almhirt.common._

package object aggregates {
  type AggregateValidation[+α <: AggregateRoot] = Validation[Problem, α]
  type PimpedAggregateValidation[+α <: AggregateRoot] = Validation[Problem, AggregateRootState[α]]

  object aggregatesforthelazyones {
    @inline
    def arid(value: String) = AggregateRootId(value)
    @inline
    def arv(value: Long) = AggregateRootVersion(value)
    
    import scala.language.implicitConversions 
    implicit def string2AggregateRootId(value: String): AggregateRootId = AggregateRootId(value)
    implicit def long2AggregateVersion(value: Long): AggregateRootVersion = AggregateRootVersion(value)
  }
}