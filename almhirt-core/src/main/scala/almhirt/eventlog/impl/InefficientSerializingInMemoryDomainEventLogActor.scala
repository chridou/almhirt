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
import almhirt.almvalidation.kit._
import almhirt.messaging.Message
import almhirt.environment._
import almhirt.environment.configuration._
import almhirt.almfuture.all._
import almhirt.domain.DomainEvent
import almhirt.eventlog._
import almhirt.core.Almhirt

class InefficientSerializingInMemoryDomainEventLogFactory() extends DomainEventLogFactory {
  def createDomainEventLog(args: Map[String, Any]): AlmValidation[ActorRef] = {
    (args.lift >! "almhirt").flatMap(_.castTo[Almhirt].flatMap(theAlmhirt =>
      theAlmhirt.getConfig.flatMap(config =>
        ConfigHelper.domainEventLog.getConfig(config).map { subConfig =>
          val name = ConfigHelper.domainEventLog.getActorName(subConfig)
          theAlmhirt.log.info(s"DomainEventLog is InefficientSerializingInMemoryDomainEventLog with name '$name'.")
          theAlmhirt.log.warning("*** THE DOMAIN EVENT LOG IS TRANSIENT ***")
          val dispatcherName =
            ConfigHelper.getDispatcherNameFromComponentConfig(subConfig).fold(
              fail => {
                theAlmhirt.log.warning("No dispatchername found for EventLog. Using default Dispatcher")
                None
              },
              succ => {
                theAlmhirt.log.info(s"DomainEventLog is using dispatcher '$succ'")
                Some(succ)
              })
          val props = SystemHelper.addDispatcherByNameToProps(dispatcherName)(Props(new InefficientSerializingInMemoryDomainEventLogActor(theAlmhirt)))
          theAlmhirt.actorSystem.actorOf(props, name)
        })))
  }
}

class InefficientSerializingInMemoryDomainEventLogActor(theAlmhirt: Almhirt) extends Actor {
  import almhirt.almvalidation.kit._
  private var loggedEvents: IndexedSeq[DomainEvent] = IndexedSeq.empty
  def receive: Receive = {
    case event: DomainEventLogCmd =>
      event match {
        case LogDomainEventsQry(events, executionIdent) =>
          loggedEvents = loggedEvents ++ events
          events.foreach(event => theAlmhirt.publishEvent(event))
          sender ! LoggedDomainEventsRsp(events.toVector, None, executionIdent)
        case GetDomainEventByIdQry(id, correlationId) =>
          val res = loggedEvents.find(_.id == id).noneIsNotFound
          sender ! DomainEventByIdRsp(res, correlationId)
        case GetAllDomainEventsQry(chunkSize, execIdent) =>
          sender ! AllDomainEventsRsp(DomainEventsChunk(0, true, loggedEvents.toIterable.success), execIdent)
        case GetDomainEventsQry(aggId, chunkSize, execIdent) =>
          sender ! DomainEventsForAggregateRootRsp(aggId, DomainEventsChunk(0, true, loggedEvents.view.filter(_.aggId == aggId).toIterable.success), execIdent)
        case GetDomainEventsFromQry(aggId, from, chunkSize, execIdent) =>
          sender ! DomainEventsForAggregateRootRsp(aggId, DomainEventsChunk(0, true, loggedEvents.view.filter(x => x.aggId == aggId && x.aggVersion >= from).toIterable.success), execIdent)
        case GetDomainEventsToQry(aggId, to, chunkSize, execIdent) =>
          sender ! DomainEventsForAggregateRootRsp(aggId, DomainEventsChunk(0, true, loggedEvents.view.filter(x => x.aggId == aggId && x.aggVersion <= to).toIterable.success), execIdent)
        case GetDomainEventsFromToQry(aggId, from, to, chunkSize, execIdent) =>
          sender ! DomainEventsForAggregateRootRsp(aggId, DomainEventsChunk(0, true, loggedEvents.view.filter(x => x.aggId == aggId && x.aggVersion >= from && x.aggVersion <= to).toIterable.success), execIdent)
      }
  }
}
