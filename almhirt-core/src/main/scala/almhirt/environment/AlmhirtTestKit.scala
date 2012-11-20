package almhirt.environment

import scalaz.syntax.validation._
import akka.actor.ActorSystem
import almhirt.common._
import almhirt.core._
import com.typesafe.config._

trait AlmhirtTestKit {
  val environmentTestKit = new AlmhirtEnvironmentTestKit {}
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

  def createTestAlmhirt(): Almhirt = createTestAlmhirt(defaultConf)
  def createTestAlmhirt(aConf: Config): Almhirt = {
    val env = environmentTestKit.createTestEnvironment(aConf)
    implicit val futureContext = env.context.system.futureDispatcher
    val almhirt =
      new Almhirt {
        def environment = env
        def getService[T <: AnyRef] = AlmFuture { OperationNotSupportedProblem("getService").failure }
        def awaitService[T <: AnyRef] = OperationNotSupportedProblem("awaitService").failure
        def dispose { env.dispose }
      }
    almhirt
  }

  def inTestAlmhirt[T](compute: Almhirt => T): T = inTestAlmhirt(compute, defaultConf)
  def inTestAlmhirt[T](compute: Almhirt => T, aConf: Config): T = {
    val almhirt = createTestAlmhirt(aConf)
    val res = compute(almhirt)
    almhirt.dispose
    res
  }
}