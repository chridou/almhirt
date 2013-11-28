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
package almhirt.core.types

/** Implementors are supposed to be able to handle domain events
 * 
 * @tparam AR The type of the aggregate root this trait is mixed in
 * @tparam Event The base type of [[almhirt.domain.DomainEvent]]s handled by this trait
 */
trait CanHandleDomainEvent[AR <: AggregateRoot[AR, Event], Event <: DomainEvent] {
  /** A function that applies a single [[almhirt.domain.DomainEvent]] and returns the effect */
  def applyEvent(event: Event): DomainValidation[AR]
  def applyEvents(events: Iterable[Event]): DomainValidation[AR]
}
