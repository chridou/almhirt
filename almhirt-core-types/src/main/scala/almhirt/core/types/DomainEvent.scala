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

import scala.language.implicitConversions
import java.util.UUID
import org.joda.time.LocalDateTime
import almhirt.common._

/** These events can create or mutate an aggregate root in the dimension of time */
trait DomainEvent extends Event {
  override def header: DomainEventHeader
  override def changeMetadata(newMetaData: Map[String, String]): DomainEvent
}

object DomainEvent {
  implicit class DomainEventOps(event: DomainEvent) {
    def aggId: AggregateRootId = event.header.aggId
    def aggVersion: AggregateRootVersion = event.header.aggVersion
    def id: EventId = event.header.id
    def timestamp: LocalDateTime = event.header.timestamp
  }
}

final case class DomainEventHeader(
  /** The events unique identifier */
  id: EventId,
  /** The affected aggregate root */
  aggId: AggregateRootId,
  aggVersion: AggregateRootVersion,
  /** The events timestamp of creation */
  timestamp: LocalDateTime,
  metadata: Map[String, String]) extends EventHeader {
  def changeMetadata(newMetadata: Map[String, String]): DomainEventHeader = this.copy(metadata = newMetadata)
}

object DomainEventHeader {
  def apply(aggIdAndVersion: (AggregateRootId, AggregateRootVersion))(implicit ccuad: CanCreateUuidsAndDateTimes): DomainEventHeader =
    DomainEventHeader(EventId(ccuad.getUniqueString), aggIdAndVersion._1, aggIdAndVersion._2  , ccuad.getUtcTimestamp, Map.empty)
  def apply(aggIdAndVersion: (AggregateRootId, AggregateRootVersion), metaData: Map[String, String])(implicit ccuad: CanCreateUuidsAndDateTimes): DomainEventHeader =
    DomainEventHeader(EventId(ccuad.getUniqueString), aggIdAndVersion._1, aggIdAndVersion._2, ccuad.getUtcTimestamp, metaData)
  def apply(id: EventId, aggIdAndVersion: (AggregateRootId, AggregateRootVersion), timestamp: LocalDateTime): DomainEventHeader =
    apply(id, aggIdAndVersion._1, aggIdAndVersion._2, timestamp, Map.empty)

  implicit def aggregateRootRef2DomainEventHeader(aggRef: (AggregateRootId, AggregateRootVersion))(implicit ccuad: CanCreateUuidsAndDateTimes): DomainEventHeader =
    apply(aggRef)


  implicit def uuidAndLong2DomainEventHeader(arId: AggregateRootId, version: AggregateRootVersion)(implicit ccuad: CanCreateUuidsAndDateTimes): DomainEventHeader =
    apply((arId, version))
}

