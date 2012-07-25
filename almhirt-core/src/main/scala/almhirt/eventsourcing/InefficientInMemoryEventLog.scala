package almhirt.eventsourcing

import java.util.UUID
import akka.actor.{ActorRefFactory, Actor, Props}
import akka.pattern._
import akka.util.Timeout
import scalaz.NonEmptyList
import almhirt.almakka.AlmAkka
import almhirt.concurrent.AlmFuture
import almhirt.concurrent.AlmFuture._
import almhirt.domain.EntityEvent

class InefficientInMemoryEventLog() extends HasEntityEvents with CanLogEntityEvents with AlmAkka {
  implicit val timeout = Timeout(defaultTimeoutDuration)
  var loggedEvents: List[EntityEvent] = Nil
  
  private case class LogEvents(events: NonEmptyList[EntityEvent])
  private case object GetAllEvents
  private case class GetEvents(entityId: UUID)
  private case class GetEventsFrom(entityId: UUID, from: Long)
  private case class GetEventsFromTo(entityId: UUID, from: Long, to: Long)
  
  private val coordinator = defaultActorSystem.actorOf(Props[Coordinator], "InefficientInMemoryEventLog")
  
  private class Coordinator extends Actor {
    def receive = {
      case LogEvents(events) => 
        val reversed = events.reverse
        sender ! new CommittedEntityEvents(NonEmptyList(reversed.head, reversed.tail : _*))
      case GetAllEvents => sender ! loggedEvents.toIterable
    }
  }

  def logEvents(events: NonEmptyList[EntityEvent]) = (coordinator ? LogEvents(events)).toAlmFuture[CommittedEntityEvents]
  
  def getAllEvents() = (coordinator ? GetAllEvents).toAlmFuture[Iterable[EntityEvent]]
  def getEvents(entityId: UUID) = (coordinator ? GetAllEvents).toAlmFuture[Iterable[EntityEvent]]
  def getEvents(entityId: UUID, fromVersion: Long) = (coordinator ? GetAllEvents).toAlmFuture[Iterable[EntityEvent]]
  def getEvents(entityId: UUID, fromVersion: Long, toVersion: Long) = (coordinator ? GetAllEvents).toAlmFuture[Iterable[EntityEvent]]
  
}