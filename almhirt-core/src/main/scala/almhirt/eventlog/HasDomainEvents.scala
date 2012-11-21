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

import java.util.UUID
import scalaz.Validation
import almhirt.common._
import almhirt.domain.DomainEvent

trait HasDomainEvents {
  def getAllEvents: AlmFuture[Iterable[DomainEvent]]
  def getEvents(aggRootId: UUID): AlmFuture[Iterable[DomainEvent]]
  def getEvents(aggRootId: UUID, fromVersion: Long): AlmFuture[Iterable[DomainEvent]]
  def getEvents(aggRootId: UUID, fromVersion: Long, toVersion: Long): AlmFuture[Iterable[DomainEvent]]
  def getRequiredNextEventVersion(aggRootId: UUID): AlmFuture[Long]
}