///* Copyright 2012 Christian Douven
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.
//*/
//package almhirt.domain
//
//import scalaz._, Scalaz._
//import almhirt.core._
//import almhirt.common._
//import almhirt.syntax.almvalidation._
//
///** Records the events for aggregate root updates.
// * Use flatMap to record events on successfully updated [[almhirt.domain.AggregateRoot]]s
// * Writer monad.
// */
//trait DeferredUpdateRecorder[+Event <: DomainEvent, +AR <: AggregateRoot[_, _]] {
//  /** Apply the events to this Update and return a result
//   * 
//   * @param events The events to process by the application to create the result
//   * @return The current events and the resulting AR 
//   */
//  def apply[EE >: Event](events: List[EE]): (List[EE], DomainValidation[AR])
//
//  /** Creates a new Update with an aggregate root transformed by f and the same events as written to this instance.
//   * It does not execute f  if the current aggregate root is already a failure
//   * Usually it makes no sense to call this method manually 
//   * 
//   * @param f Function that returns a new aggregate root. Usually a mutation of the one stored in this instance.
//   * @return The [[almhirt.domain.UpdateRecorder]] with the old events and a mapped aggregate root
//   */
//  def map[AAR <: AggregateRoot[_, _]](f: AR => AAR): DeferredUpdateRecorder[Event, AAR] =
//  	DeferredUpdateRecorder[Event, AAR] { events =>
//  	  val (currentEvents, validation) = this(events)
//  	  validation fold (problem => (currentEvents, problem.failure), ar => (currentEvents, f(ar).success))}
//
//  /** Creates a new Update from the Update returned by f.
//   * The new aggregate root and the new events are determined by calling apply with the current events on the Update returned by f 
//   * It does not execute f if the current aggregate root is already a failure 
//   * 
//   * @param f Function that returns an Update which will be used to create the new Update(Write operation)
//   * @return The [[almhirt.domain.UpdateRecorder]] with eventually updated events and the new AR state
//   */
//  def flatMap[EEvent >: Event <: DomainEvent , AAR <: AggregateRoot[_, _]](f: AR => DeferredUpdateRecorder[EEvent, AAR]): DeferredUpdateRecorder[EEvent, AAR] =
//  	DeferredUpdateRecorder[EEvent, AAR] { events =>
//  	  val (currentEvents, validation) = this(events)
//  	  validation fold (
//  	    problem => 
//  	      (currentEvents, problem.failure), 
//  	    currentAggr => {
//  	      val (updatedEvents, newAggr) = f(currentAggr)(currentEvents)
//  	      (updatedEvents, newAggr)})
//  	}
//
//  /** Execute a side effect in case of a success
//   * 
//   * @param onSuccessAction The action that triggers the side effect
//   * @return This 
//   */
//  def onSuccess(onSuccessAction: (List[Event], AR) => Unit): DeferredUpdateRecorder[Event, AR] = {
//    val (events, validation) = apply(Nil)
//    validation onSuccess (ar => { onSuccessAction(events, ar)})
//    this
//  }
//
//  /** Check whether the current AR state is a success
//   * 
//   * @return True in case of a success
//   */
//  def isAccepted() = apply(Nil)._2.isSuccess
//
//  /** Check whether the current AR state is a failure
//   * 
//   * @return True in case of a failure
//   */
//  def isRejected() = apply(Nil)._2.isFailure
//  
//  /** The result of previous recordings 
//   * Returns the current aggregate root in a success or a failure */
//  def result(): DomainValidation[AR] = {
//    val (_, validation) = apply(Nil)
//    validation
//  }
//
//  /** Returns the recorded events in chronological order */
//  def events(): List[Event] = {
//    val (events, _) = apply(Nil)
//    events.reverse
//  }
//  
//  /** Returns the AR and the events in chronological order. If the AR is in a Failure, the whole result is a Failure. */
//  def recordings: AlmValidation[(AR, List[Event])] = {
//    val (events, validation) = apply(Nil)
//    validation.map((_, events.reverse))
//  }
//}
//
//object DeferredUpdateRecorder {
//  /** Creates a new update.
//   * The function f will be applied when the new Update's apply method is triggered.
//   * 
//   * @param f Function which takes a list of (previous) events and returns the new events with the result on the modified aggregate root  
//   */
//  def apply[Event <: DomainEvent, AR <: AggregateRoot[_, _]](f: List[Event] => (List[Event], DomainValidation[AR])) =
//  	new DeferredUpdateRecorder[Event, AR] {
//  	  def apply[EE >: Event](events: List[EE]) = f(events.asInstanceOf[List[Event]])
//  }
//  
//  /** Starts a new recording with a fresh aggregate root and no previous events
//   * 
//   * @param aggregate root The unmodified aggregate root
//   */
//  def startWith[Event <: DomainEvent, AR <: AggregateRoot[_, _]](ar: AR) =
//    DeferredUpdateRecorder[Event, AR](events => (events, ar.success))
//
//  /** Takes an event and the resulting Aggregate Root. The event is prepended to the previous events
//   * 
//   * @param event The event resulting from an aggregate root operation
//   * @param result The state of the aggregate root corresponding to the event
//   */
//  def accept[Event <: DomainEvent, AR <: AggregateRoot[_, _]](event: Event, ar: AR) =
//    DeferredUpdateRecorder[Event, AR](events => (event :: events, ar.success))
//
//  /** Takes an event and the resulting Aggregate Root. Previously written events are still contained
//   * 
//   * @param error The problem causing this update to fail.
//   */
//  def reject[Event <: DomainEvent, AR <: AggregateRoot[_, _]](error: Problem) =
//    DeferredUpdateRecorder[Event, AR](events => (events, error.failure))
//
//}