package almhirt.environment

import scalaz.syntax.validation._
import almhirt.common._
import almhirt.syntax.almvalidation._
import almhirt.core._
import almhirt.environment.configuration.AlmhirtBootstrapper
import com.typesafe.config._

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
	  }
    """
  val defaultConf = ConfigFactory.parseString(configText).withFallback(ConfigFactory.load)

  def createTestAlmhirt(): (AlmhirtForTesting, ShutDown) = createTestAlmhirt(defaultConf)
  def createTestAlmhirt(aConf: Config): (AlmhirtForTesting, ShutDown) = {
    AlmhirtBootstrapper.createFromConfig(aConf).flatMap(bootstrapper =>
      AlmhirtBootstrapper.runStartupSequence(bootstrapper)).map{case (almhirt, shutDown) => (almhirt.asInstanceOf[AlmhirtForTesting], shutDown)}.forceResult
  }

  def inTestAlmhirt[T](compute: AlmhirtForTesting => T): T = inTestAlmhirt(compute, defaultConf)
  def inTestAlmhirt[T](compute: AlmhirtForTesting => T, aConf: Config): T = {
    val (almhirt, shutDown) = createTestAlmhirt(aConf)
    val res = compute(almhirt)
    shutDown.shutDown
    res
  }
}