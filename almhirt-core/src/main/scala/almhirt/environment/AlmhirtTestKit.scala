package almhirt.environment

import scalaz.syntax.validation._
import akka.event.NoLogging
import almhirt.common._
import almhirt.syntax.almvalidation._
import almhirt.core._
import almhirt.environment.configuration.AlmhirtBootstrapper
import com.typesafe.config._
import almhirt.environment.configuration.impl._

trait AlmhirtTestKit {
  private val configText =
    """  
      akka {
		loglevel = ERROR
      }
      almhirt {
		bootstrapper { 
          class = "almhirt.environment.configuration.impl.AlmhirtTestingBootstrapper" 
        }
		systemname = "almhirt-testing"
		  	durations {
		  short = 500
		  medium = 2500
		  long = 10000
		  extralong = 20000
		  	}
		  eventlog {
		  	factory = "almhirt.eventlog.impl.InefficientSerializingInMemoryDomainEventLogFactory"
		  }
        operationstate {
          factory = "almhirt.util.impl.OperationStateTrackerWithoutTimeoutFactory"
        }
        commandendpoint {
          factory = "almhirt.util.impl.CommandEndpointWithUuidTicketsFactory"
          mode = post
        }
	  }
    """
  val defaultConf = ConfigFactory.parseString(configText).withFallback(ConfigFactory.load)

  def createDefaultBootStrapper(): AlmhirtBootstrapper =
    createDefaultBootStrapper(defaultConf)

  def createDefaultBootStrapper(aConfig: Config): AlmhirtBootstrapper =
    new AlmhirtBaseBootstrapper(aConfig)

  def createExtendedBootStrapper(): AlmhirtBootstrapper =
    createExtendedBootStrapper(defaultConf)

  def createExtendedBootStrapper(aConfig: Config): AlmhirtBootstrapper =
    new AlmhirtBaseBootstrapper(aConfig) with RegistersServiceRegistry with BootstrapperWithDefaultChannels with BootstrapperDefaultCoreComponents

  def createTestAlmhirt(bootStrapper: AlmhirtBootstrapper): AlmValidation[(AlmhirtForTesting, ShutDown)] =
    AlmhirtBootstrapper.runStartupSequence(bootStrapper, NoLogging).map {
      case (theAlmhirt, shutDown) =>
        (AlmhirtForTesting(theAlmhirt), shutDown)
    }

  def createExtendedTestAlmhirt(bootStrapper: AlmhirtBootstrapper): AlmValidation[(AlmhirtForExtendedTesting, ShutDown)] =
    AlmhirtBootstrapper.runStartupSequence(bootStrapper, NoLogging).flatMap {
      case (theAlmhirt, shutDown) =>
        theAlmhirt.getService[ServiceRegistry].flatMap(reg =>
          AlmhirtForExtendedTesting(theAlmhirt, reg).map(anAlmhirt =>
            (anAlmhirt, shutDown)))
    }
  
  def createExtendedTestAlmhirt(): AlmValidation[(AlmhirtForExtendedTesting, ShutDown)] =
   createExtendedTestAlmhirt(createExtendedBootStrapper())

  def inTestAlmhirt[T](bootStrapper: AlmhirtBootstrapper)(compute: AlmhirtForTesting => T): T = {
    val (almhirt, shutDown) = createTestAlmhirt(bootStrapper).forceResult
    val res = compute(almhirt)
    shutDown.shutDown
    res
  }

  def inExtendedTestAlmhirt[T](bootStrapper: AlmhirtBootstrapper)(compute: AlmhirtForExtendedTesting => T): T = {
    val (almhirt, shutDown) = createExtendedTestAlmhirt(bootStrapper).forceResult
    val res = compute(almhirt)
    shutDown.shutDown
    res
  }

}