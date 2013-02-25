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
import org.joda.time.DateTime

/** These events can create or mutate an aggregate root in the dimension of time */
trait DomainEvent {
  /** The events unique identifier */
  def id: UUID
  /** The affected aggregate root */
  def aggRef: AggregateRootRef
  /** The events date of creation */
  def timestamp: DateTime
  
  def aggId = aggRef.id
  def aggVersion = aggRef.version
}

