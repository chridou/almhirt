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
		  short = 0.5
		  medium = 2.5
		  long = 10.0
		  }
		  eventlog {
		  	factory = "almhirt.eventlog.impl.InefficientSerializingInMemoryDomainEventLogFactory"
		  }
	  }
    """
  val defaultConf = ConfigFactory.parseString(configText).withFallback(ConfigFactory.load)

  def createTestAlmhirt(): Almhirt = createTestAlmhirt(defaultConf)
  def createTestAlmhirt(aConf: Config): Almhirt = {
    AlmhirtBootstrapper.createFromConfig(aConf).bind(bootstrapper =>
      AlmhirtBootstrapper.runStartupSequence(bootstrapper)).forceResult
  }

  def inTestAlmhirt[T](compute: Almhirt => T): T = inTestAlmhirt(compute, defaultConf)
  def inTestAlmhirt[T](compute: Almhirt => T, aConf: Config): T = {
    val almhirt = createTestAlmhirt(aConf)
    val res = compute(almhirt)
    almhirt.close
    res
  }
}