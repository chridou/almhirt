package almhirt.domain

import scalaz._, Scalaz._
import almhirt.validation.Problem
import almhirt.validation.Problem._

/** Records the events for aggregate root updates.
 * Use flatMap to record events on successfully updated entities
 * Writer monad.
 */
trait UpdateRecorder[+Event <: DomainEvent, +AR <: AggregateRoot[_, _]] {
  /** Apply the events to this Update and return a result */
  def apply[EE >: Event](events: List[EE]): (List[EE], DomainValidation[AR])

  /** Creates a new Update with an aggregate root transformed by f and the same events as written to this instance.
   * It does not execute f  if the current aggregate root is already a failure 
   * 
   * @param f Function that returns a new aggregate root. Usually a mutation of the one stored in this instance.
   */
  def map[AAR <: AggregateRoot[_, _]](f: AR => AAR): UpdateRecorder[Event, AAR] =
  	UpdateRecorder[Event, AAR] { events =>
  	  val (currentEvents, validation) = this(events)
  	  validation fold (problem => (currentEvents, problem.failure), ar => (currentEvents, f(ar).success))}

  /** Creates a new Update from the Update returned by f.
   * The new aggregate root and the new events are determined by calling apply with the current events on the Update returned by f 
   * It does not execute f if the current aggregate root is already a failure 
   * 
   * @param f Function that returns an Update which will be used to create the new Update(Write operation)
   */
  def flatMap[EEvent >: Event <: DomainEvent , AAR <: AggregateRoot[_, _]](f: AR => UpdateRecorder[EEvent, AAR]): UpdateRecorder[EEvent, AAR] =
  	UpdateRecorder[EEvent, AAR] { events =>
  	  val (currentEvents, validation) = this(events)
  	  validation fold (
  	    problem => 
  	      (currentEvents, problem.failure), 
  	    currentAggr => {
  	      val (updatedEvents, newAggr) = f(currentAggr)(currentEvents)
  	      (updatedEvents, newAggr)})
  	}

  def onSuccess(onSuccessAction: (List[Event], AR) => Unit = (ar, r) => ()): DomainValidation[AR] = {
    val (events, validation) = apply(Nil)
    validation bind (result => { onSuccessAction(events, result); result.success })
  }

  def isAccepted() = apply(Nil)._2.isSuccess
  def isRejected() = apply(Nil)._2.isFailure
  
  /** The result of previous recordings 
   * Returns the current aggregate root in a success or a failure */
  def result(): DomainValidation[AR] = {
    val (_, validation) = apply(Nil)
    validation
  }

  /** Returns the recorded events in chronological order */
  def events(): List[Event] = {
    val (events, _) = apply(Nil)
    events.reverse
  }
}

object UpdateRecorder {
  /** Creates a new update.
   * The function f will be applied when the new Update's apply method is triggered.
   * 
   * @param f Function which takes a list of (previous) events and returns the new events with the result on the modified aggregate root  
   */
  def apply[Event <: DomainEvent, AR <: AggregateRoot[_, _]](f: List[Event] => (List[Event], DomainValidation[AR])) =
  	new UpdateRecorder[Event, AR] {
  	  def apply[EE >: Event](events: List[EE]) = f(events.asInstanceOf[List[Event]])
  }
  
  /** Starts a new recording with a fresh aggregate root and no previous events
   * 
   * @param aggregate root The unmodified aggregate root
   */
  def startWith[Event <: DomainEvent, AR <: AggregateRoot[_, _]](ar: AR) =
    UpdateRecorder[Event, AR](events => (events, ar.success))

  /** Takes an event and the resulting Aggregate Root. The event is prepended to the previous events
   * 
   * @param event The event resulting from an aggregate root operation
   * @param result The state of the aggregate root corresponding to the event
   */
  def accept[Event <: DomainEvent, AR <: AggregateRoot[_, _]](event: Event, ar: AR) =
    UpdateRecorder[Event, AR](events => (event :: events, ar.success))

  /** Takes an event and the resulting Aggregate Root. Previously written events are still contained
   * 
   * @param error The problem causing this update to fail.
   */
  def reject[Event <: DomainEvent, AR <: AggregateRoot[_, _]](error: Problem) =
    UpdateRecorder[Event, AR](events => (events, error.failure))

}