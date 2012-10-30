package test

import almhirt.environment._

trait TestEnvironment {
  val testKit = new AlmhirtEnvironmentTestKit{}
  
  def createEnvironment(): AlmhirtEnvironment = {
    val env = testKit.createTestEnvironment()
    env.registerRepository[TestPerson, TestPersonEvent, TestPersonRepository](new TestPersonRepository(env.eventLog)(env.context))
    env.addCommandHandler(NewTestPersonUnitOfWork)
    env.addCommandHandler(ChangeTestPersonNameUnitOfWork)
    env.addCommandHandler(SetTestPersonAdressUnitOfWork)
    env.addCommandHandler(MoveTestPersonNameUnitOfWork)
    env.addCommandHandler(MoveBecauseOfMarriageUnitOfWork)
    
    env
  }
  
  def inTestEnvironment[T](compute: AlmhirtEnvironment => T) = {
    val env = createEnvironment
    val res = compute(env)
    env.dispose
    res
  }
}