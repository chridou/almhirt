package almhirt.environment

import akka.actor.ActorSystem
import almhirt._
import almhirt.commanding._
import almhirt.eventlog.impl._
import almhirt.domain.DomainEvent
import almhirt.messaging._
import almhirt.syntax.almvalidation._
import almhirt.parts._
import com.typesafe.config._

trait AlmhirtEnvironmentTestKit {
  val contextTestKit = new AlmhirtContextTestKit {}
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
		  	factory = "almhirt.eventlog.impl.InefficientSerializingInMemoryDomainEventLogFactory"
		  }
	  }
    """
  val defaultConf = ConfigFactory.parseString(configText).withFallback(ConfigFactory.load)

  def createTestEnvironment(): AlmhirtEnvironment = createTestEnvironment(defaultConf)
  def createTestEnvironment(aConf: Config): AlmhirtEnvironment = {
    implicit val context = contextTestKit.createTestContext(aConf)
    AlmhirtEnvironment().awaitResult(context.system.shortDuration).forceResult
  }

  def inTestEnvironment[T](compute: AlmhirtEnvironment => T): T = inTestEnvironment[T](compute, defaultConf)
  def inTestEnvironment[T](compute: AlmhirtEnvironment => T, aConf: Config): T = {
    val context = createTestEnvironment(aConf)
    val res = compute(context)
    context.dispose
    res
  }
}