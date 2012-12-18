package almhirt.environment

import akka.actor.ActorSystem
import akka.util.duration._
import almhirt._
import almhirt.core.JavaUtilUuidGenerator
import com.typesafe.config._
import almhirt.environment.configuration.ConfigPaths
import almhirt.environment.configuration.ConfigHelper

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
		  		short = 0.5
		  		medium = 2.5
		  		long = 10.0
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
    new AlmhirtSystem {
      val config = conf
      val actorSystem = ActorSystem(conf.getString("almhirt.systemname"), conf)
      val futureDispatcher = ConfigHelper.lookUpDispatcher(actorSystem)(ConfigHelper.tryGetDispatcherName(config)(ConfigPaths.futures))
      val shortDuration = conf.getDouble("almhirt.durations.short") seconds
      val mediumDuration = conf.getDouble("almhirt.durations.medium") seconds
      val longDuration = conf.getDouble("almhirt.durations.long") seconds
      def getUuid = uuidGen.generate
      def dispose = actorSystem.shutdown
    }
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
