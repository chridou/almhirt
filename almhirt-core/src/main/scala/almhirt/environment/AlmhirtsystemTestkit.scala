package almhirt.environment

import akka.actor.ActorSystem
import akka.util.duration._
import almhirt._
import almhirt.core.JavaUtilUuidGenerator
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
		  short = 0.5
		  medium = 2.5
		  long = 10.0
		}
		test-dispatcher {
		  # Dispatcher is the name of the event-based dispatcher
		  type = "akka.testkit.CallingThreadDispatcherConfigurator"
	    }
	   }
    """
  val defaultConfig = ConfigFactory.parseString(configText).withFallback(ConfigFactory.load)

  def createTestSystem(conf: Config): AlmhirtSystem = {
    val uuidGen = new JavaUtilUuidGenerator()
    new AlmhirtSystem {
      val config = conf
      val actorSystem = ActorSystem(conf.getString("almhirt.systemname"), conf)
      val futureDispatcher = actorSystem.dispatchers.lookup("almhirt.test-dispatcher")
      val messageStreamDispatcherName = Some("almhirt.test-dispatcher")
      val messageHubDispatcherName = Some("almhirt.test-dispatcher")
      val shortDuration = conf.getDouble("almhirt.durations.short") seconds
      val mediumDuration = conf.getDouble("almhirt.durations.medium") seconds
      val longDuration = conf.getDouble("almhirt.durations.long") seconds
      def generateUuid = uuidGen.generate
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
