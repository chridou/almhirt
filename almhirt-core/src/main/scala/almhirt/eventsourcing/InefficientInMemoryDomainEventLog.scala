package almhirt.eventsourcing

import java.util.UUID
import akka.actor.{ActorRefFactory, Actor, Props}
import akka.pattern._
import akka.util.Timeout
import scalaz.NonEmptyList
import almhirt.almakka.AlmAkkaContext
import almhirt.concurrent.AlmFuture
import almhirt.concurrent.AlmFuture._
import almhirt.domain.DomainEvent

class InefficientInMemoryDomainEventLog(implicit almAkkaContext: AlmAkkaContext) extends HasDomainEvents with CanLogDomainEvents {
  implicit def timeout = Timeout(almAkkaContext.mediumDuration)
  implicit def executionContext = almAkkaContext.futureDispatcher
  var loggedEvents: List[DomainEvent] = Nil
  
  private case class LogEvents(events: NonEmptyList[DomainEvent])
  private case object GetAllEvents
  private case class GetEvents(entityId: UUID)
  private case class GetEventsFrom(entityId: UUID, from: Long)
  private case class GetEventsFromTo(entityId: UUID, from: Long, to: Long)
  
  private val coordinator = almAkkaContext.actorSystem.actorOf(Props[Coordinator], "InefficientInMemoryEventLog")
  
  private class Coordinator extends Actor {
    def receive = {
      case LogEvents(events) => 
        val reversed = events.reverse
        sender ! new CommittedDomainEvents(NonEmptyList(reversed.head, reversed.tail : _*))
      case GetAllEvents => sender ! loggedEvents.toIterable
    }
  }

  def logEvents(events: NonEmptyList[DomainEvent]) = (coordinator ? LogEvents(events)).toAlmFuture[CommittedDomainEvents]
  
  def getAllEvents() = (coordinator ? GetAllEvents).toAlmFuture[Iterable[DomainEvent]]
  def getEvents(entityId: UUID) = (coordinator ? GetAllEvents).toAlmFuture[Iterable[DomainEvent]]
  def getEvents(entityId: UUID, fromVersion: Long) = (coordinator ? GetAllEvents).toAlmFuture[Iterable[DomainEvent]]
  def getEvents(entityId: UUID, fromVersion: Long, toVersion: Long) = (coordinator ? GetAllEvents).toAlmFuture[Iterable[DomainEvent]]
  
}