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
import scala.annotation.tailrec
import scalaz._, Scalaz._
import almhirt.core._
import almhirt.common._

/** Functionality to create a new aggregate root */
trait CanCreateAggragateRoot[AR <: AggregateRoot[AR, Event], Event <: DomainEvent] extends CanHandleDomainEvent[AR, Event] {
  /** Applies the event and returns a new aggregate root from the event or a failure */
  def applyEvent = { event: Event =>
    try {
      creationHandler(event).success
    } catch {
      case err: MatchError => throw new UnhandledDomainEventException(event.header.id, event)
      case err: Exception => throw err
    }
  }

  /** Creates a new aggregate root and applies all the events to it */
  def rebuildFromHistory(history: Iterable[Event]): DomainValidation[AR] = {
    @tailrec
    def buildEventSourced(ar: AR, events: Iterable[Event]): DomainValidation[AR] = {
      if (events.isEmpty)
        ar.success
      else {
        val nextEvent = events.head
        if(!(nextEvent.isInstanceOf[DeletesAggregateRootEvent]))
        ar.applyEvent(nextEvent) match {
          case scalaz.Success(newState) =>
            buildEventSourced(newState, events.tail)
          case scalaz.Failure(prob) =>
            prob.failure
        }
        else
          AggregateRootDeletedProblem(nextEvent.header.aggRef.id).failure
      }
    }
    if (history.isEmpty)
      EmptyCollectionProblem("At least one event is required to rebuild from history").failure
    else
      applyEvent(history.head) flatMap (freshAR => buildEventSourced(freshAR, history.drop(1)))
  }

  /** Creates an UpdateRecorder from the creating event */
  def create(event: Event): UpdateRecorder[AR, Event] =
    applyEvent(event) fold (UpdateRecorder.reject(_), UpdateRecorder.accept(event, _))

  /** The event passed to this handler must create a new aggregate root */
  protected def creationHandler: PartialFunction[Event, AR]
}
