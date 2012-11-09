package almhirt.domain

import almhirt._
import almhirt.ActorBased
import almhirt.environment.AlmhirtContext

trait AggregateRootRepository[AR <: AggregateRoot[AR,Event], Event <: DomainEvent] extends HasAggregateRoots[AR, Event] with StoresAggregateRoots[AR, Event] with almhirt.ActorBased

trait AggregateRootRepositoryMessage
case class GetAggregateRoot(aggId: java.util.UUID) extends AggregateRootRepositoryMessage
case class StoreAggregateRoot[AR <: AggregateRoot[AR,Event], Event <: DomainEvent](ar: AggregateRoot[AR,Event], uncommittedEvents: List[DomainEvent], ticket: Option[String]) extends AggregateRootRepositoryMessage

trait AggregateRootRepositoryResponse
case class AggregateRootFromRepositoryResponse[AR <: AggregateRoot[AR,Event], Event <: DomainEvent](ar: AlmValidation[AR]) extends AggregateRootRepositoryResponse

object AggregateRootRepository {
  import almhirt.domain.impl._
  import almhirt.eventlog._
  import akka.actor._
  def basic[AR <: AggregateRoot[AR,Event], Event <: DomainEvent](arFactory: CanCreateAggragateRoot[AR, Event], eventLog: DomainEventLog)(implicit ctx: AlmhirtContext): AggregateRootRepository[AR, Event] = {
    val actor = ctx.system.actorSystem.actorOf(Props(new BasicAggregateRootRepositoryActor[AR, Event](eventLog, arFactory, ctx) {}))
    new BasicAggregateRootRepository[AR, Event](actor, ctx){}
  }
}