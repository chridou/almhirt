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
import almhirt.messaging.Message
import almhirt._
import almhirt.context.AlmhirtContext
import almhirt.almfuture.all._
import almhirt.domain.DomainEvent
import almhirt.eventsourcing._

class InefficientSerialziedInMemoryDomainEventLog(implicit almhirtContext: AlmhirtContext) extends DomainEventLog {
  private implicit def timeout = Timeout(almhirtContext.mediumDuration)
  private implicit def executionContext = almhirtContext.futureDispatcher
  private var loggedEvents: List[DomainEvent] = Nil

  private case class LogEvents(events: List[DomainEvent])
  private case class LogEventsAsync(events: List[DomainEvent], ticket: Option[String])
  private case object GetAllEvents
  private case class GetEvents(entityId: UUID)
  private case class GetEventsFrom(entityId: UUID, from: Long)
  private case class GetEventsFromTo(entityId: UUID, from: Long, to: Long)

  private val coordinator = almhirtContext.actorSystem.actorOf(Props(new Coordinator()), "InefficientInMemoryEventLog")

  private class Coordinator() extends Actor with almakka.AlmActorLogging {
    def receive = {
      case LogEvents(events) =>
        loggedEvents = loggedEvents ++ events
        sender ! CommittedDomainEvents(events).success
      case LogEventsAsync(events, ticket) =>
        loggedEvents = loggedEvents ++ events
        ticket match {
          case Some(t) => almhirtContext.operationStateChannel.post(Message.createWithUuid(Executed(t)))
          case None => ()
        }
      case GetAllEvents =>
        sender ! loggedEvents.toIterable.success
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

  def storeEvents(events: List[DomainEvent]) = (coordinator ? LogEvents(events)).toAlmFuture[CommittedDomainEvents]
  def storeEvents(events: List[DomainEvent], ticket: Option[String]) = (coordinator ? LogEvents(events)).toAlmFuture[CommittedDomainEvents]

  def getAllEvents() = (coordinator ? GetAllEvents).toAlmFuture[Iterable[DomainEvent]]
  def getEvents(id: UUID) = (coordinator ? GetEvents(id)).toAlmFuture[Iterable[DomainEvent]]
  def getEvents(id: UUID, fromVersion: Long) = (coordinator ? GetEventsFrom(id, fromVersion)).toAlmFuture[Iterable[DomainEvent]]
  def getEvents(id: UUID, fromVersion: Long, toVersion: Long) = (coordinator ? GetEventsFromTo(id, fromVersion, toVersion)).toAlmFuture[Iterable[DomainEvent]]

}