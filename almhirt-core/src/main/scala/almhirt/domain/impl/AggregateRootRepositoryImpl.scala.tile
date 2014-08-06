package almhirt.domain.impl

import scala.reflect.ClassTag
import akka.actor._
import almhirt.core.Almhirt
import almhirt.core.types._

class AggregateRootRepositoryImpl[TAR <: AggregateRoot[TAR, TEvent], TEvent <: DomainEvent](
  val theAlmhirt: Almhirt,
  val cellCache: ActorRef,
  val cellAskMaxDuration: scala.concurrent.duration.FiniteDuration,
  val cacheAskMaxDuration: scala.concurrent.duration.FiniteDuration)(implicit tagAr: ClassTag[TAR], tagE: ClassTag[TEvent]) extends AggregateRepositoryTemplate with AggregateRootRepositoryWithCellSourceActor with Actor {
  type Event = TEvent
  type AR = TAR
  val arTag = tagAr
  val eventTag = tagE

  def receive: Receive = receiveRepositoryMsg
}

