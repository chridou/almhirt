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
      case LogEventsCmd(events,_) =>
        sender ! CommittedDomainEventsRsp(events, None).success
      case GetAllEventsCmd =>
        sender ! AllEventsRsp(DomainEventsChunk(0, true, Iterable.empty.success))
      case GetEventsCmd(aggId) =>
        sender ! EventsForAggregateRootRsp(aggId, DomainEventsChunk(0, true, Iterable.empty.success))
      case GetEventsFromCmd(aggId, from) =>
        sender ! EventsForAggregateRootRsp(aggId, DomainEventsChunk(0, true, Iterable.empty.success))
      case GetEventsFromToCmd(aggId, from, to) =>
        sender ! EventsForAggregateRootRsp(aggId, DomainEventsChunk(0, true, Iterable.empty.success))
      case GetRequiredNextEventVersionCmd(aggId) =>
        sender ! RequiredNextEventVersionRsp(aggId, 0L.success)
    }
  }

  def storeEvents(events: List[DomainEvent]) = (actor ? LogEventsCmd(events, None)).toAlmFuture[CommittedDomainEventsRsp].map(_.events)
  def purgeEvents(aggRootId: java.util.UUID) = AlmPromise { Nil.success }

  def getAllEvents() = (actor ? GetAllEventsCmd).mapTo[AllEventsRsp].map(x => x.chunk.events)
  def getEvents(id: UUID) = (actor ? GetEventsCmd(id)).mapTo[EventsForAggregateRootRsp].map(x => x.chunk.events)
  def getEvents(id: UUID, fromVersion: Long) = (actor ? GetEventsFromCmd(id, fromVersion)).mapTo[EventsForAggregateRootRsp].map(x => x.chunk.events)
  def getEvents(id: UUID, fromVersion: Long, toVersion: Long) = (actor ? GetEventsFromToCmd(id, fromVersion, toVersion)).mapTo[EventsForAggregateRootRsp].map(x => x.chunk.events)
  override def getRequiredNextEventVersion(id: UUID): AlmFuture[Long] = (actor ? GetRequiredNextEventVersionCmd(id)).mapTo[RequiredNextEventVersionRsp].map(x => x.nextVersion)
}

