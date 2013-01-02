package test

import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.environment._
import almhirt.domain.AggregateRootRepository
import almhirt.environment.Almhirt
import almhirt.parts.HasRepositories

trait TestAlmhirtKit {
  val testKit = new AlmhirtTestKit {}

  def createTestAlmhirt(): (AlmhirtForTesting, ShutDown) = {
    implicit val (almhirt, shutDown) = testKit.createTestAlmhirt()
    implicit val system = almhirt.system
    val personRepository = AggregateRootRepository.blocking[TestPerson, TestPersonEvent](TestPerson, almhirt.eventLog.actor)
    almhirt.repositories.registerForAggregateRoot[TestPerson, TestPersonEvent](personRepository)
    almhirt.hasCommandHandlers.addHandler(new NewTestPersonUnitOfWork)
    almhirt.hasCommandHandlers.addHandler(new ChangeTestPersonNameUnitOfWork)
    almhirt.hasCommandHandlers.addHandler(new SetTestPersonAdressUnitOfWork)
    almhirt.hasCommandHandlers.addHandler(new MoveTestPersonNameUnitOfWork)
    almhirt.hasCommandHandlers.addHandler(new MoveBecauseOfMarriageUnitOfWork)

    //    val barracks = ctx.riftWarp.barracks
    //    
    //    barracks.addDecomposer(new TestPersonCreatedDecomposer)
    //    barracks.addDecomposer(new TestPersonNameChangedDecomposer)
    //    barracks.addDecomposer(new TestPersonAddressAquiredDecomposer)
    //    barracks.addDecomposer(new TestPersonMovedDecomposer)
    //    barracks.addDecomposer(new TestPersonUnhandledEventDecomposer)
    //
    //    barracks.addRecomposer(new TestPersonCreatedRecomposer)
    //    barracks.addRecomposer(new TestPersonNameChangedRecomposer)
    //    barracks.addRecomposer(new TestPersonAddressAquiredRecomposer)
    //    barracks.addRecomposer(new TestPersonMovedRecomposer)
    //    barracks.addRecomposer(new TestPersonUnhandledEventRecomposer)

    (almhirt, shutDown)
  }

  def inTestAlmhirt[T](compute: AlmhirtForTesting => T) = {
    val (almhirt, shutDown) = createTestAlmhirt()
    val res = compute(almhirt)
    shutDown.shutDown
    res
  }
}