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
import almhirt.environment.configuration.bootstrappers.DefaultBootstrapperSequence
import almhirt.core.impl.SimpleConcurrentServiceRegistry

trait CoreBootstrapperWithBlockingRepo extends CreatesRepositoriesBootstrapperPhase { self: HasServiceRegistry =>
  override def createRepositories(theAlmhirt: Almhirt, startUpLogger: LoggingAdapter): BootstrapperPhaseResult =
    super.createRepositories(theAlmhirt, startUpLogger).andThen {
      self.serviceRegistry.getService[HasRepositories].flatMap { hasRepos =>
        self.serviceRegistry.getService[DomainEventLog].map { eventLog =>
          implicit val implicitAlmhirt = theAlmhirt
          val personRepository = AggregateRootRepository.blocking[TestPerson, TestPersonEvent]("TestPersonRepo", TestPerson, eventLog.actor)
          hasRepos.registerForAggregateRoot(personRepository)
          BootstrapperPhaseSuccess()
        }
      }.toBootstrapperPhaseResult
    }
}

trait CoreBootstrapperWithCommandHandlers extends RegistersCommandHandlersBootstrapperPhase { self: HasServiceRegistry =>
  override def registerCommandHandlers(theAlmhirt: Almhirt, startUpLogger: LoggingAdapter): BootstrapperPhaseResult =
    super.registerCommandHandlers(theAlmhirt, startUpLogger).andThen {
      self.serviceRegistry.getService[HasCommandHandlers].flatMap { hasHandlers =>
        for {
          uow1 <- TestPersonContext.createBasicUowFromServices(classOf[TestPersonCommand], self.serviceRegistry, None)(theAlmhirt)
        } yield {
          hasHandlers.addHandler(uow1)
          BootstrapperPhaseSuccess()
        }
      }.toBootstrapperPhaseResult
    }
}

trait CoreTestBootstrapper
  extends DefaultBootstrapperSequence
  with CoreBootstrapperWithCommandHandlers{
  val serviceRegistry = new SimpleConcurrentServiceRegistry()
}

trait BlockingRepoCoreBootstrapper extends CoreTestBootstrapper with CoreBootstrapperWithBlockingRepo