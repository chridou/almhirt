package almhirt.domain.impl

import scalaz._, Scalaz._
import akka.actor._
import akka.pattern._
import akka.util.Timeout._
import akka.dispatch._
import almhirt.core._
import almhirt.common._
import almhirt.almfuture.all._
import almhirt.domain._
import almhirt.eventlog.DomainEventLog
import almhirt.environment.AlmhirtContext
import almhirt.util._
import almhirt.common.AlmFuture

abstract class AggregateRootRepositoryActorHull[AR <: AggregateRoot[AR, Event], Event <: DomainEvent](val actor: ActorRef, almhirtContext: AlmhirtContext) extends AggregateRootRepository[AR, Event] with CanValidateAggregateRootsAgainstEvents[AR, Event] {
  implicit private def duration = almhirtContext.system.mediumDuration
  implicit private def futureContext = almhirtContext.system.futureDispatcher

  def get(id: java.util.UUID): AlmFuture[AR] = 
    (actor ? GetAggregateRootQry(id))(duration)
      .asInstanceOf[Future[AggregateRootFromRepositoryRsp[AR, Event]]]
      .map(_.ar) 

  def store(ar: AR, uncommittedEvents: List[Event], ticket: Option[TrackingTicket]): Unit =
    (actor ! StoreAggregateRootCmd(ar, uncommittedEvents, ticket))
  
}