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
import almhirt.messaging.Message
import almhirt._
import almhirt.environment.AlmhirtContext
import almhirt.almfuture.all._
import almhirt.domain.DomainEvent
import almhirt.eventlog._

class InefficientSerializingInMemoryDomainEventLog(implicit almhirtContext: AlmhirtContext) extends DomainEventLog {
  private implicit def timeout = Timeout(almhirtContext.system.mediumDuration)
  private implicit def executionContext = almhirtContext.system.futureDispatcher
  private var loggedEvents: List[DomainEvent] = Nil

  private case class LogEvents(events: List[DomainEvent])
  private case object GetAllEvents
  private case class GetEvents(entityId: UUID)
  private case class GetEventsFrom(entityId: UUID, from: Long)
  private case class GetEventsFromTo(entityId: UUID, from: Long, to: Long)
  private case class GetVersion(entityId: UUID)

  private val coordinator = almhirtContext.system.actorSystem.actorOf(Props(new Coordinator()), "InefficientInMemoryEventLog")

  private class Coordinator() extends Actor with almakka.AlmActorLogging {
    def receive = {
      case LogEvents(events) =>
        loggedEvents = loggedEvents ++ events
        events.foreach(event => almhirtContext.broadcastDomainEvent(event))
        sender ! CommittedDomainEvents(events).success
      case GetAllEvents =>
        sender ! loggedEvents.toIterable.success
      case GetEvents(entityId) =>
        sender ! loggedEvents.view.filter(_.id == entityId).toIterable.success
      case GetEventsFrom(entityId, from) =>
        sender ! loggedEvents.view.filter(x =>x.id == entityId && x.version >= from).toIterable.success
      case GetEventsFromTo(entityId, from, to) =>
        sender ! loggedEvents.view.filter(x =>x.id == entityId && x.version >= from && x.version <= to).toIterable.success
      case GetVersion(entityId) =>
        sender ! loggedEvents.view.filter(x => x.id == entityId).lastOption.map(_.version).success
    }
  }

  def storeEvents(events: List[DomainEvent]) = (coordinator ? LogEvents(events)).toAlmFuture[CommittedDomainEvents]
  def purgeEvents(aggRootId: java.util.UUID) = AlmPromise { PurgedDomainEvents(Nil).success }

  def getAllEvents() = (coordinator ? GetAllEvents).toAlmFuture[Iterable[DomainEvent]]
  def getEvents(id: UUID) = (coordinator ? GetEvents(id)).toAlmFuture[Iterable[DomainEvent]]
  def getEvents(id: UUID, fromVersion: Long) = (coordinator ? GetEventsFrom(id, fromVersion)).toAlmFuture[Iterable[DomainEvent]]
  def getEvents(id: UUID, fromVersion: Long, toVersion: Long) = (coordinator ? GetEventsFromTo(id, fromVersion, toVersion)).toAlmFuture[Iterable[DomainEvent]]
  override def getVersion(id: UUID): AlmFuture[Option[Long]] = (coordinator ? GetVersion(id)).toAlmFuture[Option[Long]]

}