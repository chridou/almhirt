package almhirt.environment

import scalaz.syntax.validation._
import akka.event.NoLogging
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.core._
import almhirt.environment.configuration._
import com.typesafe.config._
import almhirt.environment.configuration.impl._
import almhirt.environment.configuration.bootstrappers._
import almhirt.core.impl.SimpleConcurrentServiceRegistry

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

  def createDefaultBootStrapper(): Bootstrapper =
    createDefaultBootStrapper(defaultConf)

  def createDefaultBootStrapper(aConfig: Config): Bootstrapper =
    new Bootstrapper with HasConfig with HasServiceRegistry with CreatesActorSystemFromConfig with CreatesAlmhirtFromConfigAndActorSystem {
      def config = aConfig
      val serviceRegistry = new SimpleConcurrentServiceRegistry()
    }

  def createExtendedBootStrapper(): Bootstrapper =
    createExtendedBootStrapper(defaultConf)

  def createExtendedBootStrapper(aConfig: Config): Bootstrapper =
    new Bootstrapper with DefaultBootstrapperSequence {
      def config = aConfig
      val serviceRegistry = new SimpleConcurrentServiceRegistry()
    }

  def createTestAlmhirt(bootStrapper: Bootstrapper): AlmValidation[(AlmhirtForTesting, ShutDown)] = {
    implicit val startupLogger = LogBackLoggingAdapter()
    Bootstrapper.runBootstrapper(bootStrapper).map {
      case (theAlmhirt, shutDown) =>
        (AlmhirtForTesting(theAlmhirt), shutDown)
    }.withFailEffect(p => startupLogger.error(p.toString))
  }

  def createExtendedTestAlmhirt(bootstrapper: Bootstrapper): AlmValidation[(AlmhirtForExtendedTesting, ShutDown)] = {
    implicit val startupLogger = LogBackLoggingAdapter()
    Bootstrapper.runBootstrapper(bootstrapper).flatMap {
      case (theAlmhirt, shutDown) =>
        theAlmhirt.getService[ServiceRegistry].flatMap(reg =>
          AlmhirtForExtendedTesting(theAlmhirt, reg).map(anAlmhirt =>
            (anAlmhirt, shutDown)))
    }.withFailEffect(p => startupLogger.error(p.toString))
  }

  def createExtendedTestAlmhirt(): AlmValidation[(AlmhirtForExtendedTesting, ShutDown)] =
    createExtendedTestAlmhirt(createExtendedBootStrapper())

  def inTestAlmhirt[T](bootstrapper: Bootstrapper)(compute: AlmhirtForTesting => T): T = {
    val (almhirt, shutDown) = createTestAlmhirt(bootstrapper).forceResult
    try {
      compute(almhirt)
    } finally {
      shutDown.shutDown
    }
  }

  def inExtendedTestAlmhirt[T](bootStrapper: Bootstrapper)(compute: AlmhirtForExtendedTesting => T): T = {
    val (almhirt, shutDown) = createExtendedTestAlmhirt(bootStrapper).forceResult
    try {
      compute(almhirt)
    } finally {
      shutDown.shutDown
    }
  }

}