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
package almhirt.eventlog

import scala.concurrent.duration.FiniteDuration
import scalaz.{Validation}
import almhirt._
import almhirt.common._
import almhirt.domain.DomainEvent


/** Logs domain events. It doesn't validate them or check for consistency in any way. 
 */
trait CanStoreDomainEvents {
  /** Log the domain events and in a case of success return them. Events must contain the events in order of occurence */
  def storeEvents(events: IndexedSeq[DomainEvent]): AlmFuture[(IndexedSeq[DomainEvent], Option[(Problem,IndexedSeq[DomainEvent])])]
}
