package almhirt.domain

import almhirt.core._
import almhirt.common._
import almhirt.environment.AlmhirtContext
import almhirt.util.TrackingTicket

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
  
  def apply[AR <: AggregateRoot[AR, Event], Event <: DomainEvent](actor: ActorRef)(implicit ctx: AlmhirtContext): AggregateRootRepository[AR, Event] = {
    new AggregateRootRepositoryActorHull[AR, Event](actor, ctx) {}
  }

  def unsafe[AR <: AggregateRoot[AR, Event], Event <: DomainEvent](arFactory: CanCreateAggragateRoot[AR, Event], eventLog: DomainEventLog)(implicit ctx: AlmhirtContext): AggregateRootRepository[AR, Event] = {
    val actor = ctx.system.actorSystem.actorOf(Props(new UnsafeAggregateRootRepositoryActor[AR, Event](eventLog, arFactory, ctx) {}))
    apply(actor)
  }
  
  def blocking[AR <: AggregateRoot[AR, Event], Event <: DomainEvent](arFactory: CanCreateAggragateRoot[AR, Event], eventLog: DomainEventLog)(implicit ctx: AlmhirtContext): AggregateRootRepository[AR, Event] = {
    val actor = ctx.system.actorSystem.actorOf(Props(new BlockingAggregateRootRepositoryActor[AR, Event](eventLog, arFactory, ctx) {}))
    apply(actor)
  }
  
}