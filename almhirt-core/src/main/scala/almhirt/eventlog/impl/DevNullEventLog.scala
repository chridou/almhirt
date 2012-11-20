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
import almhirt.common._
import almhirt.messaging.Message
import almhirt.core._
import almhirt.almakka._
import almhirt.environment.AlmhirtContext
import almhirt.environment.configuration._
import almhirt.almfuture.all._
import almhirt.domain.DomainEvent
import almhirt.eventlog._
import almhirt.common.AlmFuture

class DevNullEventLogFactory() extends DomainEventLogFactory {
  def createDomainEventLog(ctx: AlmhirtContext): AlmValidation[DomainEventLog] = {
    val props =
      SystemHelper.addDispatcherToProps(ctx.config)(ConfigPaths.eventlog, Props(new impl.DevNullEventLogActor()(ctx)))
    val actor = ctx.system.actorSystem.actorOf(props, "domainEventLog")
    new impl.DomainEventLogActorHull(actor)(ctx).success
  }
}

class DevNullEventLogActor(implicit almhirtContext: AlmhirtContext) extends Actor {
  def receive = {
    case LogEventsQry(events, _) =>
      sender ! CommittedDomainEventsRsp(events.success, None)
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


