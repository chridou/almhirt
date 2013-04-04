package almhirt.domain

import scala.language.existentials

import java.util.{ UUID => JUUID }
import scala.reflect.ClassTag
import scalaz.std._
import almhirt.core._
import almhirt.common._
import almhirt.environment._
import almhirt.util.TrackingTicket
import almhirt.almakka.ActorBased
import almhirt.core.Almhirt
import almhirt.util.ExecutionStyle


trait AggregateRootRepository[AR <: AggregateRoot[AR, Event], Event <: DomainEvent] extends HasAggregateRoots[AR, Event] with StoresAggregateRoots[AR, Event] with ActorBased

object AggregateRootRepository {
  import almhirt.domain.impl._
  import almhirt.eventlog._
  import akka.actor._

  def apply[AR <: AggregateRoot[AR, Event], Event <: DomainEvent](aName: String, arFactory: CanCreateAggragateRoot[AR, Event], eventLog: ActorRef)(implicit theAlmhirt: Almhirt, tagAr: ClassTag[AR], tag: ClassTag[Event]): AggregateRootRepository[AR, Event] = {
    blocking(aName, arFactory, eventLog)
  }

  def unsafe[AR <: AggregateRoot[AR, Event], Event <: DomainEvent](aName: String, arFactory: CanCreateAggragateRoot[AR, Event], eventLog: ActorRef)(implicit theAlmhirt: Almhirt, tagAr: ClassTag[AR], tagEvent: ClassTag[Event]): AggregateRootRepository[AR, Event] = {
    val actor = theAlmhirt.actorSystem.actorOf(Props(new UnsafeAggregateRootRepositoryActor[AR, Event](eventLog, arFactory) {}), aName)
    new AggregateRootRepositoryActorHull[AR, Event](actor) {}
  }

  def blocking[AR <: AggregateRoot[AR, Event], Event <: DomainEvent](aName: String, arFactory: CanCreateAggragateRoot[AR, Event], eventLog: ActorRef)(implicit theAlmhirt: Almhirt, tagAr: ClassTag[AR], tagEvent: ClassTag[Event]): AggregateRootRepository[AR, Event] = {
    val actor = theAlmhirt.actorSystem.actorOf(Props(new BlockingAggregateRootRepositoryActor[AR, Event](eventLog, arFactory) {}), aName)
    new AggregateRootRepositoryActorHull[AR, Event](actor) {}
  }

  def dummy[AR <: AggregateRoot[AR, Event], Event <: DomainEvent]: AggregateRootRepository[AR, Event] =
    new AggregateRootRepository[AR, Event] {
      def get(id: JUUID) = ???
      def store(ar: AR, uncommittedEvents: IndexedSeq[Event], ticket: Option[TrackingTicket]) { ??? }
      def actor = ???
    }

}