package almhirt.ext.eventlog.anorm

import almhirt._
import almhirt.environment._
import almhirt.domain.AggregateRootRepository
import almhirt.environment.Almhirt
import com.typesafe.config.ConfigFactory

import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.environment._
import almhirt.domain.AggregateRootRepository
import almhirt.environment.Almhirt
import almhirt.parts.HasRepositories

trait TestAlmhirtKit {
  val testKit = new AlmhirtTestKit {}

  def createTestAlmhirt(): AlmhirtForTesting = {
    implicit val almhirt = testKit.createTestAlmhirt(ConfigFactory.load)
    implicit val system = almhirt.system
    val personRepository = AggregateRootRepository.blocking[TestPerson, TestPersonEvent](TestPerson, almhirt.eventLog)
    almhirt.repositories.registerForAggregateRoot[TestPerson, TestPersonEvent](personRepository)
    almhirt.hasCommandHandlers.addHandler(new NewTestPersonUnitOfWork)
    almhirt.hasCommandHandlers.addHandler(new ChangeTestPersonNameUnitOfWork)
    almhirt.hasCommandHandlers.addHandler(new SetTestPersonAdressUnitOfWork)
    almhirt.hasCommandHandlers.addHandler(new MoveTestPersonNameUnitOfWork)
    almhirt.hasCommandHandlers.addHandler(new MoveBecauseOfMarriageUnitOfWork)

    almhirt
  }

  def inTestAlmhirt[T](compute: AlmhirtForTesting => T) = {
    val almhirt = createTestAlmhirt()
    try {
      compute(almhirt)
    } finally {
      almhirt.close
    }
  }
}