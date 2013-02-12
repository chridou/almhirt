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
import almhirt.environment._
import almhirt.environment.configuration._
import almhirt.almfuture.all._
import almhirt.domain.DomainEvent
import almhirt.eventlog._
import almhirt.core.Almhirt

class InefficientSerializingInMemoryDomainEventLogFactory() extends DomainEventLogFactory {
  def createDomainEventLog(theAlmhirt: Almhirt): AlmValidation[ActorRef] = {
    theAlmhirt.getConfig.flatMap(config =>
      ConfigHelper.eventLog.getConfig(config).map { subConfig =>
        val name = ConfigHelper.eventLog.getActorName(subConfig)
        theAlmhirt.log.info(s"EventLog is InefficientSerializingInMemoryDomainEventLog with name '$name'. *** THE EVENT LOG IS TRANSIENT ***")
        val props =
          SystemHelper.addDispatcherToProps(subConfig)(Props(new InefficientSerializingInMemoryDomainEventLogActor(theAlmhirt)))
        theAlmhirt.actorSystem.actorOf(props, name)
      })
  }
}

class InefficientSerializingInMemoryDomainEventLogActor(theAlmhirt: Almhirt) extends Actor {
  private var loggedEvents: List[DomainEvent] = Nil
  def receive: Receive = {
    case LogEventsQry(events, executionIdent) =>
      loggedEvents = loggedEvents ++ events
      events.foreach(event => theAlmhirt.publishDomainEvent(event))
      sender ! CommittedDomainEventsRsp(events.success, executionIdent)
    case GetAllEventsQry(chunkSize, execIdent) =>
      sender ! AllEventsRsp(DomainEventsChunk(0, true, loggedEvents.toIterable.success), execIdent)
    case GetEventsQry(aggId, chunkSize, execIdent) =>
      sender ! EventsForAggregateRootRsp(aggId, DomainEventsChunk(0, true, loggedEvents.view.filter(_.aggId == aggId).toIterable.success), execIdent)
    case GetEventsFromQry(aggId, from, chunkSize, execIdent) =>
      sender ! EventsForAggregateRootRsp(aggId, DomainEventsChunk(0, true, loggedEvents.view.filter(x => x.aggId == aggId && x.aggVersion >= from).toIterable.success), execIdent)
    case GetEventsFromToQry(aggId, from, to, chunkSize, execIdent) =>
      sender ! EventsForAggregateRootRsp(aggId, DomainEventsChunk(0, true, loggedEvents.view.filter(x => x.aggId == aggId && x.aggVersion >= from && x.aggVersion <= to).toIterable.success), execIdent)
    case GetRequiredNextEventVersionQry(aggId) =>
      sender ! RequiredNextEventVersionRsp(aggId, loggedEvents.view.filter(x => x.aggId == aggId).lastOption.map(_.aggVersion + 1L).getOrElse(0L).success)
  }
}
