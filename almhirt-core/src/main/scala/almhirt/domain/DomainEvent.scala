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

import scala.language.implicitConversions
import java.util.UUID
import org.joda.time.DateTime
import almhirt.common._
import almhirt.core._

/** These events can create or mutate an aggregate root in the dimension of time */
trait DomainEvent extends Event { def header: DomainEventHeader }

object DomainEvent {
  implicit class DomainEventOps(event: DomainEvent) {
    def aggRef: AggregateRootRef = event.header.aggRef
    def aggId: UUID = event.header.aggRef.id
    def aggVersion: Long = event.header.aggRef.version
    def id: UUID = event.header.id
    def timestamp: DateTime = event.header.timestamp
  }
}

final case class DomainEventHeader(
  /** The events unique identifier */
  id: java.util.UUID,
  /** The affected aggregate root */
  aggRef: AggregateRootRef,
  /** The events timestamp of creation */
  timestamp: DateTime) extends EventHeader

object DomainEventHeader {
  def apply(aggRef: AggregateRootRef)(implicit ccuad: CanCreateUuidsAndDateTimes): DomainEventHeader =
    apply(ccuad.getUuid, aggRef, ccuad.getDateTime)

  implicit def aggregateRootRef2DomainEventHeader(aggRef: AggregateRootRef)(implicit ccuad: CanCreateUuidsAndDateTimes): DomainEventHeader =
    apply(aggRef)

  implicit class DomainEventHeaderOps(header: DomainEventHeader) {
    def aggRef: AggregateRootRef = header.aggRef
    def aggId: UUID = header.aggRef.id
    def aggVersion: Long = header.aggRef.version
    def id: UUID = header.id
    def timestamp: DateTime = header.timestamp
  }

}

