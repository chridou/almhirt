package almhirt.domain.components

import akka.actor.ActorRef
import almhirt.common._
import almhirt.environment._
import almhirt.core.Almhirt

trait AggregateRootCacheFactory {
  def createAggregateRootCache(almhirt: Almhirt): AlmValidation[ActorRef]
}