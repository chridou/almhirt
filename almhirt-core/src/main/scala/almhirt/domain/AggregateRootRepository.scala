package almhirt.domain

import java.util.{ UUID => JUUID }
import scalaz.std._
import almhirt.core._
import almhirt.common._
import almhirt.environment._
import almhirt.util.TrackingTicket
import almhirt.almakka.ActorBased
import almhirt.core.Almhirt
import scala.reflect.ClassTag

sealed trait AggregateRootRepositoryCmd
case class GetAggregateRootQry(aggId: JUUID) extends AggregateRootRepositoryCmd
case class StoreAggregateRootCmd[AR <: AggregateRoot[AR, Event], Event <: DomainEvent](ar: AggregateRoot[AR, Event], uncommittedEvents: IndexedSeq[DomainEvent], ticket: Option[TrackingTicket]) extends AggregateRootRepositoryCmd

sealed trait AggregateRootRepositoryRsp
case class AggregateRootFromRepositoryRsp[AR <: AggregateRoot[AR, Event], Event <: DomainEvent](ar: AlmValidation[AR]) extends AggregateRootRepositoryRsp

trait AggregateRootRepository[AR <: AggregateRoot[AR, Event], Event <: DomainEvent] extends HasAggregateRoots[AR, Event] with StoresAggregateRoots[AR, Event] with ActorBased

object AggregateRootRepository {
  import almhirt.domain.impl._
  import almhirt.eventlog._
  import akka.actor._

  def apply[AR <: AggregateRoot[AR, Event], Event <: DomainEvent](aName: String, arFactory: CanCreateAggragateRoot[AR, Event], eventLog: ActorRef)(implicit theAlmhirt: Almhirt, tag: ClassTag[Event]): AggregateRootRepository[AR, Event] = {
    blocking(aName, arFactory, eventLog)
  }

  def unsafe[AR <: AggregateRoot[AR, Event], Event <: DomainEvent](aName: String, arFactory: CanCreateAggragateRoot[AR, Event], eventLog: ActorRef)(implicit theAlmhirt: Almhirt, tag: ClassTag[Event]): AggregateRootRepository[AR, Event] = {
    val actor = theAlmhirt.actorSystem.actorOf(Props(new UnsafeAggregateRootRepositoryActor[AR, Event](eventLog, arFactory, theAlmhirt) {}), aName)
    new AggregateRootRepositoryActorHull[AR, Event](actor, theAlmhirt) {}
  }

  def blocking[AR <: AggregateRoot[AR, Event], Event <: DomainEvent](aName: String, arFactory: CanCreateAggragateRoot[AR, Event], eventLog: ActorRef)(implicit theAlmhirt: Almhirt, tag: ClassTag[Event]): AggregateRootRepository[AR, Event] = {
    val actor = theAlmhirt.actorSystem.actorOf(Props(new BlockingAggregateRootRepositoryActor[AR, Event](eventLog, arFactory, theAlmhirt) {}), aName)
    new AggregateRootRepositoryActorHull[AR, Event](actor, theAlmhirt) {}
  }

  def dummy[AR <: AggregateRoot[AR, Event], Event <: DomainEvent]: AggregateRootRepository[AR, Event] =
    new AggregateRootRepository[AR, Event] {
      def get(id: JUUID) = ???
      def store(ar: AR, uncommittedEvents: IndexedSeq[Event], ticket: Option[TrackingTicket]) { ??? }
      def actor = ???
    }

}