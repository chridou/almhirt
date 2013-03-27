package almhirt.eventlog.impl

import java.util.UUID
import scalaz.syntax.validation._
import akka.actor.{ ActorRefFactory, Actor, ActorRef, Props }
import akka.pattern._
import akka.util.Timeout
import almhirt.common._
import almhirt.messaging.Message
import almhirt.core._
import almhirt.almakka._
import almhirt.environment._
import almhirt.environment.configuration._
import almhirt.almfuture.all._
import almhirt.domain.DomainEvent
import almhirt.eventlog._
import almhirt.common.AlmFuture
import almhirt.core.Almhirt

class DevNullEventLogFactory() extends EventLogFactory {
  def createEventLog(theAlmhirt: Almhirt): AlmValidation[ActorRef] = {
    theAlmhirt.getConfig.flatMap(config =>
      ConfigHelper.domainEventLog.getConfig(config).map { subConfig =>
        val name = ConfigHelper.eventLog.getActorName(subConfig)
        theAlmhirt.log.info(s"EventLog is DevNullEventLog with name '$name'.")
        theAlmhirt.log.warning("*** THE EVENT LOG DOES NOTHING ***")
        val dispatcherName =
          ConfigHelper.getDispatcherNameFromComponentConfig(subConfig).fold(
            fail => {
              theAlmhirt.log.warning("No dispatchername found for DomainEventLog. Using default Dispatcher")
              None
            },
            succ => {
              theAlmhirt.log.info(s"EventLog is using dispatcher '$succ'")
              Some(succ)
            })
        val props = SystemHelper.addDispatcherByNameToProps(dispatcherName)(Props(new DevNullEventLogActor()))
        theAlmhirt.actorSystem.actorOf(props, name)
      })
  }
}

class DevNullEventLogActor() extends Actor {
  def receive = {
    case LogEventsQry(events, _) =>
      sender ! LoggedDomainEventsRsp(events.toVector, None, None)
    case GetAllDomainEventsQry(chunkSize, execIdent) =>
      sender ! AllDomainEventsRsp(DomainEventsChunk(0, true, Iterable.empty.success), execIdent)
    case GetDomainEventsQry(aggId, chunkSize, execIdent) =>
      sender ! DomainEventsForAggregateRootRsp(aggId, DomainEventsChunk(0, true, Iterable.empty.success), execIdent)
    case GetDomainEventsFromQry(aggId, from, chunkSize, execIdent) =>
      sender ! DomainEventsForAggregateRootRsp(aggId, DomainEventsChunk(0, true, Iterable.empty.success), execIdent)
    case GetDomainEventsFromToQry(aggId, from, to, chunkSize, execIdent) =>
      sender ! DomainEventsForAggregateRootRsp(aggId, DomainEventsChunk(0, true, Iterable.empty.success), execIdent)
  }
}


