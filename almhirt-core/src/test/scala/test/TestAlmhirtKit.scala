package test

import almhirt._
import almhirt.environment._
import almhirt.domain.AggregateRootRepository
import almhirt.environment.Almhirt

trait TestAlmhirtKit {
  val testKit = new AlmhirtTestKit{}
  
  def createTestAlmhirt(): Almhirt = {
    val almhirt = testKit.createTestAlmhirt()
    implicit val ctx = almhirt.environment.context
    val personRepository = AggregateRootRepository.blocking[TestPerson, TestPersonEvent](TestPerson, almhirt.environment.eventLog)
    almhirt.environment.registerRepository[TestPerson, TestPersonEvent](personRepository)
    almhirt.environment.addCommandHandler(new NewTestPersonUnitOfWork)
    almhirt.environment.addCommandHandler(new ChangeTestPersonNameUnitOfWork)
    almhirt.environment.addCommandHandler(new SetTestPersonAdressUnitOfWork)
    almhirt.environment.addCommandHandler(new MoveTestPersonNameUnitOfWork)
    almhirt.environment.addCommandHandler(new MoveBecauseOfMarriageUnitOfWork)
    
    almhirt
  }
  
  def inTestAlmhirt[T](compute: Almhirt => T) = {
    val almhirt = createTestAlmhirt()
    val res = compute(almhirt)
    almhirt.dispose
    res
  }
}