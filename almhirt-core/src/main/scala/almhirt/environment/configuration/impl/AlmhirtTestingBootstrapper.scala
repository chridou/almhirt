package almhirt.environment.configuration.impl

import scala.reflect.ClassTag
import scala.concurrent.duration.FiniteDuration
import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import almhirt.almvalidation.kit._
import almhirt.environment._
import almhirt.common._
import almhirt.commanding.CommandEnvelope
import almhirt.util.OperationState
import almhirt.domain.DomainEvent
import almhirt.environment.configuration.AlmhirtBootstrapper
import almhirt.parts.HasRepositories
import almhirt.parts.HasCommandHandlers
import almhirt.eventlog.DomainEventLog
import almhirt.util.OperationStateTracker
import almhirt.core.ServiceRegistry
import almhirt.environment.configuration.CleanUpAction
import com.typesafe.config.Config
import almhirt.core.Almhirt
import almhirt.core.HasActorSystem

class AlmhirtTestingBootstrapper(config: Config) extends AlmhirtDefaultBootStrapper(config) {
  override def createAlmhirt(hasActorSystem: HasActorSystem, theServiceRegistry: ServiceRegistry, startUpLogger: LoggingAdapter): AlmValidation[(Almhirt, CleanUpAction)] = {
    super.createAlmhirt(hasActorSystem, theServiceRegistry, startUpLogger).flatMap {
      case (theAlmhirt, cleanUp) =>
        AlmhirtForTesting(theAlmhirt, theServiceRegistry).map((_, cleanUp))
    }
  }
}