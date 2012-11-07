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

class DevNullEventLog(implicit almhirtContext: AlmhirtContext) extends DomainEventLog {
  implicit private def futureContext = almhirtContext.system.futureDispatcher
  def storeEvents(events: List[DomainEvent]) = AlmPromise { CommittedDomainEvents(Nil).success }
  def purgeEvents(aggRootId: java.util.UUID) = AlmPromise { PurgedDomainEvents(Nil).success }

  def getAllEvents() = AlmPromise { Iterable.empty.success }
  def getEvents(id: UUID) = AlmPromise { Iterable.empty.success }
  def getEvents(id: UUID, fromVersion: Long) = AlmPromise { Iterable.empty.success }
  def getEvents(id: UUID, fromVersion: Long, toVersion: Long) = AlmPromise { Iterable.empty.success }
  override def getRawVersion(aggRootId: UUID) = AlmPromise.successful { 0L }
}