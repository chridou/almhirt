package test

import almhirt._
import almhirt.environment._
import almhirt.domain.AggregateRootRepository
import almhirt.environment.Almhirt
import com.typesafe.config.ConfigFactory

trait TestAlmhirtKit {
  private val configText =
    """  
      akka {
		loglevel = ERROR
      }
      almhirt {
		systemname = "almhirt-testing"
		durations {
		  short = 0.5
		  medium = 2.5
		  long = 10.0
		  }
		  eventlog {
		  	factory = "almhirt.eventlog.anorm.SerializingAnormEventLogFactory"
		  	#dbtemplate = "postgres"
		  	dbtemplate = "h2"
		  	#driver = "org.h2.Driver"
		  	#ddlpath = "/conf/h2ddl.sql"
		  	actorname = "anorm-test-eventlog"
		  	connection = "jdbc:h2:mem:almhirtanormtest;DB_CLOSE_DELAY=-1"
		  	#connection = "jdbc:postgresql://lumsearch/eventlogtest"
		  	properties {
		  		user = "lumsearchTestUser"
		  		password = "lumsearchTestUser"
		  	}
		  	eventlogtable = "eventlog"
		  	create_schema = true
		  	drop_on_close = true
		  	randomize_tablename = true
		  }
	  }
    """
  val defaultConf = ConfigFactory.parseString(configText).withFallback(ConfigFactory.load)
  
  
  val testKit = new AlmhirtTestKit{}
  
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
    val res = compute(almhirt)
    almhirt.dispose
    res
  }
}