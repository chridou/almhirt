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
package almhirt.eventsourcing.impl

import java.util.UUID
import scalaz.syntax.validation._
import akka.actor.{ ActorRefFactory, Actor, Props }
import akka.pattern._
import akka.util.Timeout
import almhirt.almakka.AlmAkkaContext
import almhirt._
import almhirt.almfuture.all._
import almhirt.domain.DomainEvent
import almhirt.eventsourcing._

class InefficientInMemoryDomainEventLog(implicit almAkkaContext: AlmAkkaContext) extends HasDomainEvents with CanLogDomainEvents {
  implicit def timeout = Timeout(almAkkaContext.mediumDuration)
  implicit def executionContext = almAkkaContext.futureDispatcher
  var loggedEvents: List[DomainEvent] = Nil

  private case class LogEvents(events: List[DomainEvent])
  private case object GetAllEvents
  private case class GetEvents(entityId: UUID)
  private case class GetEventsFrom(entityId: UUID, from: Long)
  private case class GetEventsFromTo(entityId: UUID, from: Long, to: Long)

  private val coordinator = almAkkaContext.actorSystem.actorOf(Props[Coordinator], "InefficientInMemoryEventLog")

  private class Coordinator extends Actor with almakka.AlmActorLogging {
    def receive = {
      case LogEvents(events) =>
        loggedEvents = loggedEvents ++ events
        sender ! new CommittedDomainEvents(events)
      case GetAllEvents =>
        sender ! loggedEvents.toIterable
      case GetEvents(entityId) =>
        val pinnedSender = sender
        AlmFuture { loggedEvents.view.filter(_.aggRootId == entityId).toIterable.success }.onComplete(pinnedSender ! _)
      case GetEventsFrom(entityId, from) =>
        val pinnedSender = sender
        AlmFuture { loggedEvents.view.filter(x =>x.aggRootId == entityId && x.aggRootVersion >= from).toIterable.success }.onComplete(pinnedSender ! _)
      case GetEventsFromTo(entityId, from, to) =>
        val pinnedSender = sender
        AlmFuture { loggedEvents.view.filter(x =>x.aggRootId == entityId && x.aggRootVersion >= from && x.aggRootVersion <= to).toIterable.success }.onComplete(pinnedSender ! _)
    }
  }

  def logEvents(events: List[DomainEvent]) = (coordinator ? LogEvents(events)).toAlmFuture[CommittedDomainEvents]

  def getAllEvents() = (coordinator ? GetAllEvents).toAlmFuture[Iterable[DomainEvent]]
  def getEvents(entityId: UUID) = (coordinator ? GetAllEvents).toAlmFuture[Iterable[DomainEvent]]
  def getEvents(entityId: UUID, fromVersion: Long) = (coordinator ? GetAllEvents).toAlmFuture[Iterable[DomainEvent]]
  def getEvents(entityId: UUID, fromVersion: Long, toVersion: Long) = (coordinator ? GetAllEvents).toAlmFuture[Iterable[DomainEvent]]

}