//package almhirt.ext.eventlog.anorm
//
//import almhirt._
//import almhirt.environment._
//import almhirt.domain.AggregateRootRepository
//import com.typesafe.config.ConfigFactory
//
//import almhirt.common._
//import almhirt.core.Almhirt
//import almhirt.almvalidation.kit._
//import almhirt.environment._
//import almhirt.domain.AggregateRootRepository
//import almhirt.parts.HasRepositories
//
//trait TestAlmhirtKit {
//  val testKit = new AlmhirtTestKit {}
//
//  def createTestAlmhirt(): (AlmhirtForTesting, ShutDown) = {
//    implicit val (almhirt, shutDown) = testKit.createTestAlmhirt(ConfigFactory.load)
//    val personRepository = AggregateRootRepository.blocking[TestPerson, TestPersonEvent](TestPerson, almhirt.eventLog.actor)
//    almhirt.repositories.registerForAggregateRoot[TestPerson, TestPersonEvent](personRepository)
//    almhirt.hasCommandHandlers.addHandler(TestPersonHandlerFactory.newTestPersonUnitOfWork)
//    almhirt.hasCommandHandlers.addHandler(TestPersonHandlerFactory.changeTestPersonNameUnitOfWork)
//    almhirt.hasCommandHandlers.addHandler(TestPersonHandlerFactory.setTestPersonAdressUnitOfWork)
//    almhirt.hasCommandHandlers.addHandler(TestPersonHandlerFactory.moveTestPersonNameUnitOfWork)
//    almhirt.hasCommandHandlers.addHandler(TestPersonHandlerFactory.moveBecauseOfMarriageUnitOfWork)
//    
//    (almhirt, shutDown)
//  }
//
//  def inTestAlmhirt[T](compute: AlmhirtForTesting => T) = {
//    val (almhirt, shutDown) = createTestAlmhirt()
//    try {
//      compute(almhirt)
//    } finally {
//      shutDown.shutDown
//    }
//  }
//}