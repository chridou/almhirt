package almhirt.domaineventlog.impl

import scalaz.syntax.validation._
import akka.actor._
import almhirt.common._
import almhirt.configuration._
import almhirt.domain.DomainEvent
import almhirt.domaineventlog.DomainEventLog
import almhirt.core.Almhirt
import almhirt.domaineventlog.DomainEventLogRouter
import com.typesafe.config.Config
import play.api.libs.iteratee.Enumerator

object InMemoryDomainEventLog {
  def props(theAlmhirt: Almhirt): Props = 
    Props(new InMemoryDomainEventLog with Actor with ActorLogging {
      override def receive: Receive = receiveDomainEventLogMsg
      override def publishCommittedEvent(event: DomainEvent) {
        theAlmhirt.messageBus.publish(event)(theAlmhirt)
      }
      
      override def postStop() {
        super.postStop()
        logStatistics()
      }
    })
  
  def apply(theAlmhirt: Almhirt, configSection: Config, actorFactory: ActorRefFactory): AlmValidation[(ActorRef, CloseHandle)] =
    for {
      numActors <- configSection.v[Int]("number-of-actors")
    } yield {
      val theProps = props(theAlmhirt)
      theAlmhirt.log.info(s"""Domain event log: "number-of-actors" is $numActors""")
      if (numActors <= 1) {
        (actorFactory.actorOf(theProps, "domain-event-log"), CloseHandle.noop)
      } else {
        (actorFactory.actorOf(Props(new DomainEventLogRouter(numActors, theProps)), "domain-event-log"), CloseHandle.noop)
      }
    }

  def apply(theAlmhirt: Almhirt, configPath: String, actorFactory: ActorRefFactory): AlmValidation[(ActorRef, CloseHandle)] =
    theAlmhirt.config.v[Config](configPath).flatMap(configSection =>
      apply(theAlmhirt, configSection, actorFactory: ActorRefFactory))
    
  def apply(theAlmhirt: Almhirt, actorFactory: ActorRefFactory): AlmValidation[(ActorRef, CloseHandle)] = apply(theAlmhirt, "almhirt.domain-event-log", actorFactory)

  def apply(theAlmhirt: Almhirt): AlmValidation[(ActorRef, CloseHandle)] = apply(theAlmhirt, theAlmhirt.actorSystem)

}

trait InMemoryDomainEventLog extends DomainEventLog { actor: Actor with ActorLogging =>
  import DomainEventLog._

  private var domainEventLog = Vector.empty[DomainEvent]

  protected def logStatistics() {
    val numberOfEvents = domainEventLog.size
    val numberOfAggregateRoots = domainEventLog.groupBy(x => x.aggId).size
    
    val msg = s"""I stored $numberOfEvents domainEvents of $numberOfAggregateRoots aggregate roots."""
    log.info(msg)
  }
  
  final protected def receiveDomainEventLogMsg: Receive = {
    case CommitDomainEvents(events) =>
      domainEventLog = domainEventLog ++ events
      sender ! CommittedDomainEvents(events)
      events.foreach(publishCommittedEvent)
    case GetAllDomainEvents =>
      sender ! FetchedDomainEvents(Enumerator(domainEventLog.map(_.success): _*))
    case GetDomainEvent(eventId) =>
      sender ! QueriedDomainEvent(eventId, domainEventLog.find(_.id == eventId))
    case GetAllDomainEventsFor(aggId) =>
      sender ! FetchedDomainEvents(Enumerator(domainEventLog.filter(_.aggId == aggId).map(_.success): _*))
    case GetDomainEventsFrom(aggId, fromVersion) =>
      sender ! FetchedDomainEvents(Enumerator(domainEventLog.filter(event => event.aggId == aggId && event.aggVersion >= fromVersion).map(_.success): _*))
    case GetDomainEventsTo(aggId, toVersion) =>
      sender ! FetchedDomainEvents(Enumerator(domainEventLog.filter(event => event.aggId == aggId && event.aggVersion <= toVersion).map(_.success): _*))
    case GetDomainEventsUntil(aggId, untilVersion) =>
      sender ! FetchedDomainEvents(Enumerator(domainEventLog.filter(event => event.aggId == aggId && event.aggVersion < untilVersion).map(_.success): _*))
    case GetDomainEventsFromTo(aggId, fromVersion, toVersion) =>
      sender ! FetchedDomainEvents(Enumerator(domainEventLog.filter(event => event.aggId == aggId && event.aggVersion >= fromVersion && event.aggVersion <= toVersion).map(_.success): _*))
    case GetDomainEventsFromUntil(aggId, fromVersion, untilVersion) =>
      sender ! FetchedDomainEvents(Enumerator(domainEventLog.filter(event => event.aggId == aggId && event.aggVersion >= fromVersion && event.aggVersion < untilVersion).map(_.success): _*))
  }
}