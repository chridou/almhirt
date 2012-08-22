package almhirt.domain

import scalaz._, Scalaz._
import almhirt.validation.Problem
import almhirt.validation.Problem._

/** Records the events for entity updates.
 * Use flatMap to record events on successfully updated entities
 * Writer monad.
 */
trait Update[+Event <: EntityEvent, +ES <: Entity[_, _]] {
  /** Apply the events to this Update and return a result */
  def apply[EE >: Event](events: List[EE]): (List[EE], EntityValidation[ES])

  /** Creates a new Update with an entity transformed by f and the same events as written to this instance.
   * It does not execute f  if the current entity is already a failure 
   * 
   * @param f Function that returns a new entity. Usually a mutation of the one stored in this instance.
   */
  def map[EES <: Entity[_, _]](f: ES => EES): Update[Event, EES] =
  	Update[Event, EES] { events =>
  	  val (currentEvents, validation) = this(events)
  	  validation fold (problem => (currentEvents, problem.failure), ar => (currentEvents, f(ar).success))}

  /** Creates a new Update from the Update returned by f.
   * The new entity and the new events are determined by calling apply with the current events on the Update returned by f 
   * It does not execute f if the current entity is already a failure 
   * 
   * @param f Function that returns an Update which will be used to create the new Update(Write operation)
   */
  def flatMap[EEvent >: Event <: EntityEvent , EES <: Entity[_, _]](f: ES => Update[EEvent, EES]): Update[EEvent, EES] =
  	Update[EEvent, EES] { events =>
  	  val (currentEvents, validation) = this(events)
  	  validation fold (
  	    problem => 
  	      (currentEvents, problem.failure), 
  	    currentEntity => {
  	      val (updatedEvents, newEntity) = f(currentEntity)(currentEvents)
  	      (updatedEvents, newEntity)})
  	}

  def onSuccess(onSuccessAction: (List[Event], ES) => Unit = (e, r) => ()): EntityValidation[ES] = {
    val (events, validation) = apply(Nil)
    validation bind (result => { onSuccessAction(events, result); result.success })
  }

  def isAccepted() = apply(Nil)._2.isSuccess
  def isRejected() = apply(Nil)._2.isFailure
  
  /** The result of previous recordings 
   * Returns the current entity in a success or a failure */
  def result(): EntityValidation[ES] = {
    val (_, validation) = apply(Nil)
    validation
  }

  /** Returns the recorded events in chronological order */
  def events(): List[Event] = {
    val (events, _) = apply(Nil)
    events.reverse
  }
}

object Update {
  /** Creates a new update.
   * The function f will be applied when the new Update's apply method is triggered.
   * 
   * @param f Function which takes a list of (previous) events and returns the new events with the result on the modified entity  
   */
  def apply[Event <: EntityEvent, ES <: Entity[_, _]](f: List[Event] => (List[Event], EntityValidation[ES])) =
  	new Update[Event, ES] {
  	  def apply[EE >: Event](events: List[EE]) = f(events.asInstanceOf[List[Event]])
  }
  
  /** Starts a new recording with a fresh entity and no previous events
   * 
   * @param entity The unmodified entity
   */
  def startWith[Event <: EntityEvent, ES <: Entity[_, _]](entity: ES) =
    Update[Event, ES](events => (events, entity.success))

  /** Takes an event and the resulting Entity. The event is prepended to the previous events
   * 
   * @param event The event resulting from an entity operation
   * @param result The state of the entity corresponding to the event
   */
  def accept[Event <: EntityEvent, ES <: Entity[_, _]](event: Event, entity: ES) =
    Update[Event, ES](events => (event :: events, entity.success))

  /** Takes an event and the resulting Entity. Previously written events are still contained
   * 
   * @param error The problem causing this update to fail.
   */
  def reject[Event <: EntityEvent, ES <: Entity[_, _]](error: Problem) =
    Update[Event, ES](events => (events, error.failure))

}