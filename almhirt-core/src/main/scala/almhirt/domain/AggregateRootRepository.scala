package almhirt.domain

import almhirt.core._
import almhirt.common._
import almhirt.environment._
import almhirt.util.TrackingTicket
import almhirt.common.ActorBased

sealed trait AggregateRootRepositoryCmd
case class GetAggregateRootQry(aggId: java.util.UUID) extends AggregateRootRepositoryCmd
case class StoreAggregateRootCmd[AR <: AggregateRoot[AR, Event], Event <: DomainEvent](ar: AggregateRoot[AR, Event], uncommittedEvents: List[DomainEvent], ticket: Option[TrackingTicket]) extends AggregateRootRepositoryCmd

sealed trait AggregateRootRepositoryRsp
case class AggregateRootFromRepositoryRsp[AR <: AggregateRoot[AR, Event], Event <: DomainEvent](ar: AlmValidation[AR]) extends AggregateRootRepositoryRsp

trait AggregateRootRepository[AR <: AggregateRoot[AR, Event], Event <: DomainEvent] extends HasAggregateRoots[AR, Event] with StoresAggregateRoots[AR, Event] with ActorBased

object AggregateRootRepository {
  import almhirt.domain.impl._
  import almhirt.eventlog._
  import akka.actor._
  
  def apply[AR <: AggregateRoot[AR, Event], Event <: DomainEvent](actor: ActorRef)(implicit baseOps: AlmhirtBaseOps): AggregateRootRepository[AR, Event] = {
    new AggregateRootRepositoryActorHull[AR, Event](actor, baseOps) {}
  }

  def unsafe[AR <: AggregateRoot[AR, Event], Event <: DomainEvent](arFactory: CanCreateAggragateRoot[AR, Event], eventLog: DomainEventLog)(implicit baseOps: AlmhirtBaseOps, system: AlmhirtSystem): AggregateRootRepository[AR, Event] = {
    val actor = system.actorSystem.actorOf(Props(new UnsafeAggregateRootRepositoryActor[AR, Event](eventLog, arFactory, baseOps) {}))
    apply(actor)
  }
  
  def blocking[AR <: AggregateRoot[AR, Event], Event <: DomainEvent](arFactory: CanCreateAggragateRoot[AR, Event], eventLog: DomainEventLog)(implicit baseOps: AlmhirtBaseOps, system: AlmhirtSystem): AggregateRootRepository[AR, Event] = {
    val actor = system.actorSystem.actorOf(Props(new BlockingAggregateRootRepositoryActor[AR, Event](eventLog, arFactory, baseOps) {}))
    apply(actor)
  }
  
}