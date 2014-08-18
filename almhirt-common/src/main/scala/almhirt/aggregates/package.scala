package almhirt

import scalaz.Validation
import almhirt.common._

package object aggregates {
  type FlatAggregateValidation[+αρ <: AggregateRoot] = Validation[Problem, αρ]
  type AggregateValidation[+αρ <: AggregateRoot] = Validation[Problem, AggregateRootLifecycle[αρ]]

  implicit class UpdateRecorderValidationStateSingleOps[AR <: AggregateRoot, E <: AggregateEvent](self: AlmValidation[(AggregateRootLifecycle[AR], E)]) {
    /** Make this validation an [[UpdateRecorder]] */
    def record: UpdateRecorder[AR, E] = self.fold(
      fail => UpdateRecorder.reject(fail),
      succ => UpdateRecorder.accept(succ._1, succ._2))
  }

  implicit class UpdateRecorderValidationStateMultiOps[AR <: AggregateRoot, E <: AggregateEvent](self: AlmValidation[(AggregateRootLifecycle[AR], Seq[E])]) {
    /** Make this validation an [[UpdateRecorder]] containing some events that lead to the state.*/
    def record: UpdateRecorder[AR, E] = self.fold(
      fail => UpdateRecorder.reject(fail),
      succ => UpdateRecorder.acceptMany(succ._1, succ._2))
  }

  implicit class UpdateRecorderValidationVivusSingleOps[AR <: AggregateRoot, E <: AggregateEvent](self: AlmValidation[(AR, E)]) {
    /** Make this validation that might contain an existing aggregate root an [[UpdateRecorder]].*/
    def record: UpdateRecorder[AR, E] = self.fold(
      fail => UpdateRecorder.reject(fail),
      succ => UpdateRecorder.accept(Vivus(succ._1), succ._2))
  }

  implicit class UpdateRecorderValidationVivusMultiOps[AR <: AggregateRoot, E <: AggregateEvent](self: AlmValidation[(AR, Seq[E])]) {
    /**
     * Make this validation that might contain an existing aggregate root an [[UpdateRecorder]]
     * containing some events that lead to the state.
     */
    def record: UpdateRecorder[AR, E] = self.fold(
      fail => UpdateRecorder.reject(fail),
      succ => UpdateRecorder.acceptMany(Vivus(succ._1), succ._2))
  }

  /**
   * Well, this is intended for testing where you might be too lazy to type the
   *  full names. For the really lazy ones, there are even implicit conversion to
   *  use a String where an [[AggregateRootId]] is required and a Long where an [[AggregateVersion]] is required.
   *  This will certainly obscure your code and cause funny effects where the customer's nickname is used as
   *  an [[AggregateRootId]]!
   */
  object aggregatesforthelazyones {
    @inline
    def arid(value: String) = AggregateRootId(value)
    @inline
    def arv(value: Long) = AggregateRootVersion(value)

    import scala.language.implicitConversions

    implicit def string2AggregateRootId(value: String): AggregateRootId = AggregateRootId(value)
    implicit def long2AggregateRootVersion(value: Long): AggregateRootVersion = AggregateRootVersion(value)
  }
}