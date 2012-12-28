package almhirt.environment

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration._
import akka.actor.ActorSystem
import almhirt._
import almhirt.almvalidation.kit._
import almhirt.core.JavaUtilUuidGenerator
import almhirt.environment.configuration._
import com.typesafe.config._

trait AlmhirtsystemTestkit {
  private val configText =
    """  
		  akka {
		  	default-dispatcher {
          	type="akka.testkit.CallingThreadDispatcher"
		  	}     
		  }
		  almhirt {
		  	systemname = "almhirt-testing"
		  	durations {
		  short = 500
		  medium = 2500
		  long = 10000
		  	}
    messagehub {
      dispatchername = "almhirt.dispatchers.test-dispatcher"
    }
    messagechannels {
      dispatchername = "almhirt.dispatchers.test-dispatcher"
    }
    eventlog {
      dispatchername = "almhirt.dispatchers.test-dispatcher"
    }
    commandexecutor {
      dispatchername = "almhirt.dispatchers.test-dispatcher"
    }
    repositories {
      dispatchername = "almhirt.dispatchers.test-dispatcher"
    }
    futures {
      dispatchername = "almhirt.dispatchers.test-dispatcher"
    }
		  	dispatchers {
		  		test-dispatcher {
		  		# Dispatcher is the name of the event-based dispatcher
		  		type = "akka.testkit.CallingThreadDispatcherConfigurator"
		  	}
		  }
	   }
    """
  val defaultConfig = ConfigFactory.parseString(configText).withFallback(ConfigFactory.load)

  def createTestSystem(conf: Config): AlmhirtSystem = {
    val uuidGen = new JavaUtilUuidGenerator()
    (for {
      short <- ConfigHelper.getDuration(conf)("almhirt.durations.short")
      medium <- ConfigHelper.getDuration(conf)("almhirt.durations.medium")
      long <- ConfigHelper.getDuration(conf)("almhirt.durations.long")
    } yield new AlmhirtSystem {
      val config = conf
      val actorSystem = ActorSystem(conf.getString("almhirt.systemname"), conf)
      val executionContext = ConfigHelper.lookUpDispatcher(actorSystem)(ConfigHelper.tryGetDispatcherName(config)(ConfigPaths.futures))
      val shortDuration = short
      val mediumDuration = medium
      val longDuration = long
      def getUuid = uuidGen.generate
      def dispose = actorSystem.shutdown
    }).forceResult
  }

  def createTestSystem(): AlmhirtSystem = createTestSystem(defaultConfig)

  def inTestSystem[T](compute: AlmhirtSystem => T): T = inTestSystem(defaultConfig, compute)
  def inTestSystem[T](conf: Config, compute: AlmhirtSystem => T): T = {
    val context = createTestSystem(conf)
    val res = compute(context)
    context.dispose()
    res
  }

}
