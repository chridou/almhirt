package test

import almhirt._
import almhirt.environment._
import almhirt.domain.AggregateRootRepository
import almhirt.environment.Almhirt
import com.typesafe.config.ConfigFactory

trait TestAlmhirtKit {
  val defaultConf = ConfigFactory.load

  val testKit = new AlmhirtTestKit {}

  def createTestAlmhirt(): Almhirt = {
    val almhirt = testKit.createTestAlmhirt(defaultConf)
    implicit val ctx = almhirt.environment.context
    val personRepository = AggregateRootRepository.blocking[TestPerson, TestPersonEvent](TestPerson, almhirt.environment.eventLog)
    almhirt.environment.registerRepository[TestPerson, TestPersonEvent](personRepository)
    almhirt.environment.addCommandHandler(new NewTestPersonUnitOfWork)
    almhirt.environment.addCommandHandler(new ChangeTestPersonNameUnitOfWork)
    almhirt.environment.addCommandHandler(new SetTestPersonAdressUnitOfWork)
    almhirt.environment.addCommandHandler(new MoveTestPersonNameUnitOfWork)
    almhirt.environment.addCommandHandler(new MoveBecauseOfMarriageUnitOfWork)

    val barracks = ctx.riftWarp.barracks

    barracks.addDecomposer(new TestPersonCreatedDecomposer)
    barracks.addDecomposer(new TestPersonNameChangedDecomposer)
    barracks.addDecomposer(new TestPersonAddressAquiredDecomposer)
    barracks.addDecomposer(new TestPersonMovedDecomposer)
    barracks.addDecomposer(new TestPersonUnhandledEventDecomposer)

    barracks.addRecomposer(new TestPersonCreatedRecomposer)
    barracks.addRecomposer(new TestPersonNameChangedRecomposer)
    barracks.addRecomposer(new TestPersonAddressAquiredRecomposer)
    barracks.addRecomposer(new TestPersonMovedRecomposer)
    barracks.addRecomposer(new TestPersonUnhandledEventRecomposer)

    almhirt
  }

  def inTestAlmhirt[T](compute: Almhirt => T) = {
    val almhirt = createTestAlmhirt()
    try {
      val res = compute(almhirt)
      res
    } finally {
      almhirt.dispose
    }
  }
}