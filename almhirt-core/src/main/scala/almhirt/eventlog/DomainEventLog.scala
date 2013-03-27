package almhirt.eventlog

import java.util.UUID
import scala.concurrent.duration._
import scalaz.std._
import almhirt.common._
import almhirt.core._
import almhirt.domain.DomainEvent
import almhirt.core.Almhirt



trait DomainEventLog extends HasDomainEvents with CanStoreDomainEvents with almhirt.almakka.ActorBased

object DomainEventLog {
  import scalaz.syntax.validation._
  import akka.actor._
  import almhirt.environment._
  import almhirt.eventlog.impl._

  def apply()(implicit theAlmhirt: Almhirt): DomainEventLog = apply(None)
  def apply(aName: String)(implicit theAlmhirt: Almhirt): DomainEventLog = apply(Some(aName))
  def apply(aName: Option[String])(implicit theAlmhirt: Almhirt): DomainEventLog = unsafeInMemory(aName)

  def unsafeInMemory()(implicit theAlmhirt: Almhirt): DomainEventLog = unsafeInMemory(None)
  def unsafeInMemory(aName: String)(implicit theAlmhirt: Almhirt): DomainEventLog = unsafeInMemory(Some(aName))
  def unsafeInMemory(aName: Option[String])(implicit theAlmhirt: Almhirt): DomainEventLog = {
    val name = option.cata(aName)(n => n, "eventlog")
    DomainEventLogActorHull(theAlmhirt.actorSystem.actorOf(Props(new InefficientSerializingInMemoryDomainEventLogActor(theAlmhirt)), name), theAlmhirt.durations.extraLongDuration)
  }

  def devNull()(implicit foundations: HasActorSystem with HasDurations with HasExecutionContext): DomainEventLog = devNull(None)
  def devNull(aName: String)(implicit foundations: HasActorSystem with HasDurations with HasExecutionContext): DomainEventLog = devNull(Some(aName))
  def devNull(aName: Option[String])(implicit foundations: HasActorSystem with HasDurations with HasExecutionContext): DomainEventLog = {
    val name = option.cata(aName)(n => n, "eventlog")
    DomainEventLogActorHull(foundations.actorSystem.actorOf(Props(new DevNullDomainEventLogActor()), name), foundations.durations.extraLongDuration)
  }
  
}