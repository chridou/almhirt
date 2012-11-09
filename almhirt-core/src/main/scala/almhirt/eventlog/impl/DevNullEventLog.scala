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
package almhirt.eventlog.impl

import java.util.UUID
import scalaz.syntax.validation._
import akka.actor.{ ActorRefFactory, Actor, Props }
import akka.pattern._
import akka.util.Timeout
import almhirt.messaging.Message
import almhirt._
import almhirt.environment.AlmhirtContext
import almhirt.almfuture.all._
import almhirt.domain.DomainEvent
import almhirt.eventlog._

class DevNullEventLog(implicit almhirtContext: AlmhirtContext) extends DomainEventLog {
  private implicit def timeout = Timeout(almhirtContext.system.mediumDuration)
  private implicit def executionContext = almhirtContext.system.futureDispatcher
  val actor = almhirtContext.system.actorSystem.actorOf(Props(new Coordinator()), "InefficientInMemoryEventLog")

  private class Coordinator() extends Actor with almakka.AlmActorLogging {
    def receive = {
      case LogEventsQry(events,_) =>
        sender ! CommittedDomainEventsRsp(events, None).success
      case GetAllEventsQry =>
        sender ! AllEventsRsp(DomainEventsChunk(0, true, Iterable.empty.success))
      case GetEventsQry(aggId) =>
        sender ! EventsForAggregateRootRsp(aggId, DomainEventsChunk(0, true, Iterable.empty.success))
      case GetEventsFromQry(aggId, from) =>
        sender ! EventsForAggregateRootRsp(aggId, DomainEventsChunk(0, true, Iterable.empty.success))
      case GetEventsFromToQry(aggId, from, to) =>
        sender ! EventsForAggregateRootRsp(aggId, DomainEventsChunk(0, true, Iterable.empty.success))
      case GetRequiredNextEventVersionQry(aggId) =>
        sender ! RequiredNextEventVersionRsp(aggId, 0L.success)
    }
  }

  def storeEvents(events: List[DomainEvent]) = (actor ? LogEventsQry(events, None)).toAlmFuture[CommittedDomainEventsRsp].map(_.events)
  def purgeEvents(aggRootId: java.util.UUID) = AlmPromise { Nil.success }

  def getAllEvents() = (actor ? GetAllEventsQry).mapTo[AllEventsRsp].map(x => x.chunk.events)
  def getEvents(id: UUID) = (actor ? GetEventsQry(id)).mapTo[EventsForAggregateRootRsp].map(x => x.chunk.events)
  def getEvents(id: UUID, fromVersion: Long) = (actor ? GetEventsFromQry(id, fromVersion)).mapTo[EventsForAggregateRootRsp].map(x => x.chunk.events)
  def getEvents(id: UUID, fromVersion: Long, toVersion: Long) = (actor ? GetEventsFromToQry(id, fromVersion, toVersion)).mapTo[EventsForAggregateRootRsp].map(x => x.chunk.events)
  override def getRequiredNextEventVersion(id: UUID): AlmFuture[Long] = (actor ? GetRequiredNextEventVersionQry(id)).mapTo[RequiredNextEventVersionRsp].map(x => x.nextVersion)
}

