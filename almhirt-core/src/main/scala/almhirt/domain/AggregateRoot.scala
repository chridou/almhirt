/* Copyright 2012 Christian Douven

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package almhirt.domain

import java.util.UUID
import scalaz._, Scalaz._
import almhirt.core._
import almhirt.common._
import almhirt.syntax.almvalidation._


/** An aggregate root is the topmost entity of an aggregate. It aggregates those entities and value objects which cannot exist without the whole.
 * All entities within the aggregate should only be accessible via the aggregate root. Only an aggregate root justifies a repository.
 */
trait AggregateRoot[AR <: AggregateRoot[AR, Event], Event <: DomainEvent] extends CanHandleDomainEvent[AR, Event]{
  /** The unique id that gives the aggregate its identity */
  def id: UUID
  /** The monotonically growing version which is increased by one with each event generated via mutation. 
   * The minimum value is 1L */
  def version: Long

}

trait AggregateRootWithHandlers[AR <: AggregateRoot[AR, Event], Event <: DomainEvent] extends AggregateRoot[AR, Event]{
  /** Applies the event by calling the default handler after validating the event. */
  def applyEvent = {event: Event => applyValidated(event, handlers)}
  
  /** A [[scala.PartialFunction]] that takes an event and returns a modified AR according to the event.
   * This should be the standard handler. You can also create specialized handlers and invoke them via update(event, handler)
   * The handler must increase the aggregate root's version
   */ 
  protected def handlers: PartialFunction[Event,AR] 
 
  /** Validates the event and then applies the handler
   * 
   * @param event The Event to validate and then apply
   * @param handler the handler to call with the event
   * @return The modified aggregate root or a failure
   */
  protected def applyValidated(event: Event, handler: PartialFunction[Event,AR]): DomainValidation[AR] = {
    validateEvent(event) flatMap ( validated =>
      	try {
      	  handler(validated).success
      	} catch {
      		case err: MatchError => UnhandledDomainEventProblem("Unhandled event: %s".format(event.getClass.getName), event).failure
      		case err: Exception => ExceptionCaughtProblem(err.getMessage()).failure
      	})
  }
  
  /** Check if the event targets this AR by comparing the ids and versions of this instance and the event
   * As this method is called before applying the event, the versions must have the same value.
   * 
   * @param event The Event to check against
   * @return The passed event wrapped in a success if it is valid otherwise a failure
   */
  protected def validateEvent(event: Event): AlmValidation[Event] = {
  	if (event.aggId != this.id)
  	  UnspecifiedProblem("Ids do not match!").failure
  	else if(event.aggVersion != this.version)
  	  CollisionProblem("Conflict: Versions do not match. Targetted version is %d but the entity has version %d. The event was: %s".format(event.aggVersion, this.version, event.getClass().getName)).failure
  	else
  	  event.success
  }
  
}

trait AddsUpdateToAggregateRoot[AR <: AggregateRoot[AR, Event], Event <: DomainEvent] extends UpdatesAggregateRoot[AR, Event] { ar: AggregateRootWithHandlers[AR, Event]  => 
  /** Apply the event by calling the default handler defined by the protected abstract method 'handlers' 
   * 
   * @param event The event to apply the standard handler to
   */
  protected def update(event: Event): UpdateRecorder[AR, Event] = update(event, handlers)
  }

