package almhirt.eventlog.impl

import java.util.UUID
import scalaz.syntax.validation._
import akka.actor.{ ActorRefFactory, Actor, Props }
import akka.pattern._
import akka.util.Timeout
import almhirt.almakka.AlmAkkaContext
import almhirt.messaging.Message
import almhirt._
import almhirt.environment.AlmhirtContext
import almhirt.almfuture.all._
import almhirt.domain.DomainEvent
import almhirt.eventlog._

class DevNullEventLog(implicit almhirtContext: AlmhirtContext) extends DomainEventLog {
  implicit private def futureContext = almhirtContext.akkaContext.futureDispatcher 
  def storeEvents(events: List[DomainEvent]) = AlmPromise{ CommittedDomainEvents(Nil).success }
  def storeEvents(events: List[DomainEvent], ticket: Option[String]) = AlmPromise{ CommittedDomainEvents(Nil).success }

  def getAllEvents() = AlmPromise{ Iterable.empty.success }
  def getEvents(id: UUID) = AlmPromise{ Iterable.empty.success }
  def getEvents(id: UUID, fromVersion: Long) = AlmPromise{ Iterable.empty.success }
  def getEvents(id: UUID, fromVersion: Long, toVersion: Long) = AlmPromise{ Iterable.empty.success }

}