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
import akka.actor._
import akka.pattern._
import akka.util.Timeout
import almhirt.common._
import almhirt.messaging.Message
import almhirt.environment.AlmhirtContext
import almhirt.environment.configuration._
import almhirt.almfuture.all._
import almhirt.domain.DomainEvent
import almhirt.eventlog._

class InefficientSerializingInMemoryDomainEventLogFactory() extends DomainEventLogFactory {
  def createDomainEventLog(ctx: AlmhirtContext): AlmValidation[DomainEventLog] = {
    val props =
      SystemHelper.addDispatcherToProps(ctx.config)(ConfigPaths.eventlog, Props(new impl.InefficientSerializingInMemoryDomainEventLogActor()(ctx)))
    val actor = ctx.system.actorSystem.actorOf(props, "domainEventLog")
    new impl.DomainEventLogActorHull(actor)(ctx).success
  }
}

class InefficientSerializingInMemoryDomainEventLogActor(implicit almhirtContext: AlmhirtContext) extends Actor {
  private var loggedEvents: List[DomainEvent] = Nil
  def receive: Receive = {
    case LogEventsQry(events, executionIdent) =>
      loggedEvents = loggedEvents ++ events
      events.foreach(event => almhirtContext.broadcastDomainEvent(event))
      sender ! CommittedDomainEventsRsp(events.success, executionIdent)
    case GetAllEventsQry =>
      sender ! AllEventsRsp(DomainEventsChunk(0, true, loggedEvents.toIterable.success))
    case GetEventsQry(aggId) =>
      sender ! EventsForAggregateRootRsp(aggId, DomainEventsChunk(0, true, loggedEvents.view.filter(_.id == aggId).toIterable.success))
    case GetEventsFromQry(aggId, from) =>
      sender ! EventsForAggregateRootRsp(aggId, DomainEventsChunk(0, true, loggedEvents.view.filter(x => x.id == aggId && x.version >= from).toIterable.success))
    case GetEventsFromToQry(aggId, from, to) =>
      sender ! EventsForAggregateRootRsp(aggId, DomainEventsChunk(0, true, loggedEvents.view.filter(x => x.id == aggId && x.version >= from && x.version <= to).toIterable.success))
    case GetRequiredNextEventVersionQry(aggId) =>
      sender ! RequiredNextEventVersionRsp(aggId, loggedEvents.view.filter(x => x.id == aggId).lastOption.map(_.version + 1L).getOrElse(0L).success)
  }
}
