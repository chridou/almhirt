package almhirt.domain

import akka.actor._
import almhirt.common._
import almhirt.aggregates._

private[almhirt] trait AggregateRootViewsSkeleton[T, E <: AggregateRootEvent] { me: Actor with ActorLogging =>
	def genProps: AggregateRootId => Props
}