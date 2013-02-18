package almhirt.core.test

import scala.reflect.ClassTag
import scalaz.syntax.validation._
import akka.event.LoggingAdapter
import almhirt.common._
import almhirt.core._
import almhirt.environment.configuration.impl._
import almhirt.environment.configuration._
import com.typesafe.config.Config
import almhirt.parts.HasRepositories
import almhirt.domain.AggregateRootRepository
import almhirt.eventlog.DomainEventLog
import almhirt.parts.HasCommandHandlers

trait CoreBootstrapperWithBlockingRepo extends AlmhirtBootstrapper {
  override def registerRepositories(theAlmhirt: Almhirt, theServiceRegistry: ServiceRegistry, startUpLogger: LoggingAdapter): AlmValidation[CleanUpAction] =
    super.registerRepositories(theAlmhirt, theServiceRegistry, startUpLogger).flatMap { superCleanUp =>
      theServiceRegistry.getService[HasRepositories].flatMap { hasRepos =>
        theServiceRegistry.getService[DomainEventLog].flatMap { eventLog =>
          implicit val implicitAlmhirt = theAlmhirt
          val personRepository = AggregateRootRepository.blocking[TestPerson, TestPersonEvent]("TestPersonRepo" ,TestPerson, eventLog.actor)
          hasRepos.registerForAggregateRoot(personRepository)
          superCleanUp.success
        }
      }
    }
}

trait CoreBootstrapperWithCommandHandlers extends AlmhirtBootstrapper {
  override def registerCommandHandlers(theAlmhirt: Almhirt, theServiceRegistry: ServiceRegistry, startUpLogger: LoggingAdapter): AlmValidation[CleanUpAction] =
    super.registerRepositories(theAlmhirt, theServiceRegistry, startUpLogger).flatMap { superCleanUp =>
      theServiceRegistry.getService[HasCommandHandlers].flatMap { hasHandlers =>
        for {
          uow1 <- TestPersonContext.createBasicUowFromServices(classOf[TestPersonCommand], theServiceRegistry, None)(theAlmhirt)
        } yield {
          hasHandlers.addHandler(uow1)
          superCleanUp}
      }
    }
}

class CoreTestBootstrapper(config: Config) extends AlmhirtBaseBootstrapper(config)
  with RegistersServiceRegistry
  with BootstrapperWithDefaultChannels
  with BootstrapperDefaultCoreComponents
  with CoreBootstrapperWithCommandHandlers
  
class BlockingRepoCoreBootstrapper(config: Config) extends CoreTestBootstrapper(config: Config) with CoreBootstrapperWithBlockingRepo