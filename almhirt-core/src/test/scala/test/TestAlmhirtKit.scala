package test

import almhirt._
import almhirt.environment._

trait TestAlmhirtKit {
  val testKit = new AlmhirtTestKit{}
  
  def createTestAlmhirt(): Almhirt = {
    val almhirt = testKit.createTestAlmhirt()
    implicit val ctx = almhirt.environment.context
    almhirt.environment.registerRepository[TestPerson, TestPersonEvent, TestPersonRepository](new TestPersonRepository(almhirt.environment.eventLog)(almhirt.environment.context))
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