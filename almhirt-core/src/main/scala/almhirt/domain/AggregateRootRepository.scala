package almhirt.domain

import almhirt.core._
import almhirt.common._
import almhirt.environment._
import almhirt.util.TrackingTicket
import almhirt.almakka.ActorBased

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

  def unsafe[AR <: AggregateRoot[AR, Event], Event <: DomainEvent](arFactory: CanCreateAggragateRoot[AR, Event], eventLog: DomainEventLog)(implicit almhirt: Almhirt): AggregateRootRepository[AR, Event] = {
    val actor = almhirt.system.actorSystem.actorOf(Props(new UnsafeAggregateRootRepositoryActor[AR, Event](eventLog, arFactory, almhirt) {}))
    apply(actor)
  }
  
  def blocking[AR <: AggregateRoot[AR, Event], Event <: DomainEvent](arFactory: CanCreateAggragateRoot[AR, Event], eventLog: ActorRef)(implicit almhirt: Almhirt): AggregateRootRepository[AR, Event] = {
    val actor = almhirt.system.actorSystem.actorOf(Props(new BlockingAggregateRootRepositoryActor[AR, Event](eventLog, arFactory, almhirt) {}))
    apply(actor)
  }
  
  def dummy[AR <: AggregateRoot[AR, Event], Event <: DomainEvent]: AggregateRootRepository[AR, Event] =
    new AggregateRootRepository[AR, Event]{ 
     def get(id: java.util.UUID) = ???
     def store(ar: AR, uncommittedEvents: List[Event], ticket: Option[TrackingTicket]) { ??? }
     def actor = ???
}
  
}