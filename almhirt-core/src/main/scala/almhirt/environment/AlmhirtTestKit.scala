package almhirt.environment

import scalaz.syntax.validation._
import akka.event.NoLogging
import almhirt.common._
import almhirt.syntax.almvalidation._
import almhirt.core._
import almhirt.environment.configuration.AlmhirtBootstrapper
import com.typesafe.config._
import almhirt.environment.configuration.impl.LogBackLoggingAdapter

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

  def createTestAlmhirt(): (AlmhirtForTesting, ShutDown) = createTestAlmhirt(defaultConf)
  def createTestAlmhirt(aConf: Config): (AlmhirtForTesting, ShutDown) = {
    AlmhirtBootstrapper.createFromConfig(aConf).flatMap(bootstrapper =>
      AlmhirtBootstrapper.runStartupSequence(bootstrapper, NoLogging)).map { case (almhirt, shutDown) => (almhirt.asInstanceOf[AlmhirtForTesting], shutDown) }.forceResult
  }

  def inTestAlmhirt[T](compute: AlmhirtForTesting => T): T = inTestAlmhirt(compute, defaultConf)
  def inTestAlmhirt[T](compute: AlmhirtForTesting => T, aConf: Config): T = {
    val (almhirt, shutDown) = createTestAlmhirt(aConf)
    val res = compute(almhirt)
    shutDown.shutDown
    res
  }
}