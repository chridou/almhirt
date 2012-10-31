package test

import almhirt._
import almhirt.environment._

trait TestAlmhirtKit {
  val testKit = new AlmhirtTestKit{}
  
  def createTestAlmhirt(): Almhirt = {
    val almhirt = testKit.createTestAlmhirt()
    almhirt.environment.registerRepository[TestPerson, TestPersonEvent, TestPersonRepository](new TestPersonRepository(almhirt.environment.eventLog)(almhirt.environment.context))
    almhirt.environment.addCommandHandler(NewTestPersonUnitOfWork)
    almhirt.environment.addCommandHandler(ChangeTestPersonNameUnitOfWork)
    almhirt.environment.addCommandHandler(SetTestPersonAdressUnitOfWork)
    almhirt.environment.addCommandHandler(MoveTestPersonNameUnitOfWork)
    almhirt.environment.addCommandHandler(MoveBecauseOfMarriageUnitOfWork)
    
    almhirt
  }
  
  def inTestAlmhirt[T](compute: Almhirt => T) = {
    val almhirt = createTestAlmhirt()
    val res = compute(almhirt)
    almhirt.dispose
    res
  }
}