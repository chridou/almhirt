package almhirt

import scalaz.Validation
import almhirt.common._

package object aggregates {
  type FlatAggregateValidation[+α <: AggregateRoot] = Validation[Problem, α]
  type AggregateValidation[+α <: AggregateRoot] = Validation[Problem, AggregateRootState[α]]

  implicit class UpdateRecorderValidationStateSingleOps[AR <: AggregateRoot, E <: AggregateEvent](self: AlmValidation[(AggregateRootState[AR], E)]) {
    def record: UpdateRecorder[AR, E] = self.fold(fail => UpdateRecorder.reject(fail), succ => UpdateRecorder.accept(succ._1, succ._2))
  }

  implicit class UpdateRecorderValidationStateMultiOps[AR <: AggregateRoot, E <: AggregateEvent](self: AlmValidation[(AggregateRootState[AR], Seq[E])]) {
    def record: UpdateRecorder[AR, E] = self.fold(fail => UpdateRecorder.reject(fail), succ => UpdateRecorder.acceptMany(succ._1, succ._2))
  }

  implicit class UpdateRecorderValidationAliveSingleOps[AR <: AggregateRoot, E <: AggregateEvent](self: AlmValidation[(AR, E)]) {
    def record: UpdateRecorder[AR, E] = self.fold(fail => UpdateRecorder.reject(fail), succ => UpdateRecorder.accept(Alive(succ._1), succ._2))
  }

  implicit class UpdateRecorderValidationAliveMultiOps[AR <: AggregateRoot, E <: AggregateEvent](self: AlmValidation[(AR, Seq[E])]) {
    def record: UpdateRecorder[AR, E] = self.fold(fail => UpdateRecorder.reject(fail), succ => UpdateRecorder.acceptMany(Alive(succ._1), succ._2))
  }
  
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