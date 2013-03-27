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
    case cmd: EventLogCmd =>
      cmd match {
        case LogEventQry(event, correlationId) =>
          sender ! LoggedEventRsp(event.success, correlationId)
        case GetAllEventsQry(chunkSize, correlationId) =>
          sender ! EventsRsp(EventsChunk(0, true, Iterable.empty.success), correlationId)
        case GetEventQry(eventId, chunkSize, correlationId) =>
          sender ! EventRsp(NotFoundProblem(s"Event with id: ${eventId.toString()}. Remember that this is the devnull-Eventstore!").failure, correlationId)
        case GetEventsFromQry(from, chunkSize, correlationId) =>
          sender ! EventsRsp(EventsChunk(0, true, Iterable.empty.success), correlationId)
        case GetEventsUntilQry(until, chunkSize, correlationId) =>
          sender ! EventsRsp(EventsChunk(0, true, Iterable.empty.success), correlationId)
        case GetEventsFromUntilQry(from, until, chunkSize, correlationId) =>
          sender ! EventsRsp(EventsChunk(0, true, Iterable.empty.success), correlationId)
      }
  }
}


