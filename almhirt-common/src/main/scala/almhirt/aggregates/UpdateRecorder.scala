package almhirt.aggregates

import scalaz._, Scalaz._
import almhirt.common._
import almhirt.syntax.almvalidation._

/**
 * Records the events for aggregate root updates.
 * Use flatMap to record events on successfully updated [[almhirt.domain.AggregateRoot]]s
 * Writer monad.
 */
trait UpdateRecorder[+AR <: AggregateRoot, +Event <: AggregateEvent] {
  /**
   * Apply the events to this Update and return a result
   *
   * @param events The events to process by the application to create the result
   * @return The current events and the resulting AR
   */
  def apply[EE >: Event](events: List[EE]): (AggregateValidation[AR], List[EE])
  /**
   * Creates a new Update with an aggregate root transformed by f and the same events as written to this instance.
   * It does not execute f if the current aggregate root is already a failure
   * Usually it makes no sense to call this method manually
   *
   * @param f Function that returns a new aggregate root. Usually a mutation of the one stored in this instance.
   * @return The [[almhirt.domain.UpdateRecorder]] with the old events and a mapped aggregate root
   */
  def map[AAR <: AggregateRoot](f: AggregateRootLifecycle[AR] ⇒ AggregateRootLifecycle[AAR]): UpdateRecorder[AAR, Event] =
    UpdateRecorder[AAR, Event] { events ⇒
      val (validation, currentEvents) = this(events)
      validation fold (problem ⇒ (problem.failure, currentEvents), ar ⇒ (f(ar).success, currentEvents))
    }
  /**
   * Creates a new Update from the Update returned by f.
   * The new aggregate root and the new events are determined by calling apply with the current events on the Update returned by f
   * It does not execute f if the current aggregate root is already a failure
   *
   * @param f Function that returns an Update which will be used to create the new Update(Write operation)
   * @return The [[almhirt.domain.UpdateRecorder]] with eventually updated events and the new AR state
   */
  def flatMap[AAR <: AggregateRoot, EEvent >: Event <: AggregateEvent](f: AggregateRootLifecycle[AR] ⇒ UpdateRecorder[AAR, EEvent]): UpdateRecorder[AAR, EEvent] =
    UpdateRecorder[AAR, EEvent] { events ⇒
      val (validation, currentEvents) = this(events)
      validation fold (
        problem ⇒
          (problem.failure, currentEvents),
        currentAggr ⇒ {
          val (newAggr, updatedEvents) = f(currentAggr)(currentEvents)
          (newAggr, updatedEvents)
        })
    }

  /**
   * Check whether the current AR state is a success
   *
   * @return True in case of a success
   */
  def isAccepted() = apply(Nil)._1.isSuccess
  /**
   * Check whether the current AR state is a failure
   *
   * @return True in case of a failure
   */
  def isRejected() = apply(Nil)._1.isFailure
  /**
   * The result of previous recordings
   * Returns the current aggregate root in a success or a failure
   */
  def result(): AggregateValidation[AR] = {
    val (validation, _) = apply(Nil)
    validation
  }
  /** Returns the recorded events in chronological order */
  def events(): List[Event] = {
    val (_, events) = apply(Nil)
    events.reverse
  }

  def recordings: AlmValidation[(AggregateRootLifecycle[AR], List[Event])] = {
    val (validation, events) = apply(Nil)
    validation.map((_, events.reverse))
  }
}

object UpdateRecorder {
  /**
   * Creates a new update.
   * The function f will be applied when the new Update's apply method is triggered.
   *
   * @param f Function which takes a list of (previous) events and returns the new events with the result on the modified aggregate root
   */
  def apply[AR <: AggregateRoot, Event <: AggregateEvent](f: List[Event] ⇒ (AggregateValidation[AR], List[Event])) =
    new UpdateRecorder[AR, Event] {
      def apply[EE >: Event](events: List[EE]) = f(events.asInstanceOf[List[Event]])
    }

  def noop[AR <: AggregateRoot, Event <: AggregateEvent](ar: AggregateRootLifecycle[AR]) =
    UpdateRecorder[AR, Event](events ⇒ (ar.success, events))
  
  def startVivus[AR <: AggregateRoot, Event <: AggregateEvent](ar: AR) =
    UpdateRecorder[AR, Event](events ⇒ (Vivus(ar).success, events))

  def startFresh[AR <: AggregateRoot, Event <: AggregateEvent]() =
    UpdateRecorder[AR, Event](events ⇒ (Vacat.success, events))

  def startMortuus[AR <: AggregateRoot, Event <: AggregateEvent](d: Mortuus) =
    UpdateRecorder[AR, Event](events ⇒ (d.success, events))

  /**
   * Takes an event and the resulting Aggregate Root. The event is prepended to the previous events
   *
   * @param event The event resulting from an aggregate root operation
   * @param result The state of the aggregate root corresponding to the event
   */
  def accept[AR <: AggregateRoot, Event <: AggregateEvent](ar: AggregateRootLifecycle[AR], event: Event) =
    UpdateRecorder[AR, Event](events ⇒ (ar.success, event :: events))

  def accept[AR <: AggregateRoot, Event <: AggregateEvent](t: (AggregateRootLifecycle[AR], Event)) =
    UpdateRecorder[AR, Event](events ⇒ (t._1.success, t._2 :: events))

  def acceptMany[AR <: AggregateRoot, Event <: AggregateEvent](ar: AggregateRootLifecycle[AR], newEvents: Seq[Event]) =
    UpdateRecorder[AR, Event](events ⇒ (ar.success, newEvents.foldLeft(events) { case (acc, event) => event :: acc }))

  /**
   * Takes an event and the resulting Aggregate Root. Previously written events are still contained
   *
   * @param error The problem causing this update to fail.
   */
  def reject[AR <: AggregateRoot, Event <: AggregateEvent](error: Problem) =
    UpdateRecorder[AR, Event](events ⇒ (error.failure, events))

  def record[AR <: AggregateRoot, Event <: AggregateEvent](v: AlmValidation[(AggregateRootLifecycle[AR], Event)]) =
    v.fold(fail => UpdateRecorder.reject(fail), succ => UpdateRecorder.accept(succ._1, succ._2))

  def recordVivus[AR <: AggregateRoot, Event <: AggregateEvent](v: AlmValidation[(AR, Event)]) =
    v.fold(fail => UpdateRecorder.reject(fail), succ => UpdateRecorder.accept(Vivus(succ._1), succ._2))

  def ifVivus[AR <: AggregateRoot, Event <: AggregateEvent](f: AR => UpdateRecorder[AR, Event]): AggregateRootLifecycle[AR] => UpdateRecorder[AR, Event] =
    (state) => state match {
      case Vacat => UpdateRecorder.reject(IllegalOperationProblem(s"There is no aggregate root."))
      case Mortuus(id, v) => UpdateRecorder.reject(IllegalOperationProblem(s"The aggregate root with id ${id.value} and version ${v.value} is already dead."))
      case Vivus(ar) => f(ar)
    }
}