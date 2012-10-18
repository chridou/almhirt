package almhirt.context

import akka.actor.ActorSystem
import akka.dispatch.MessageDispatcher
import akka.util.duration._
import com.typesafe.config._

trait AlmhirtContextTestKit {
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
  val conf = ConfigFactory.parseString(configText).withFallback(ConfigFactory.load)

  def createTestContext(): AlmhirtContext = {
    val context =
      new AlmhirtContext {
        val actorSystem = ActorSystem(conf.getString("almhirt.systemname"), conf)
        val futureDispatcher = actorSystem.dispatchers.lookup("almhirt.test-dispatcher")
        val messageStreamDispatcherName = Some("almhirt.test-dispatcher")
        val messageHubDispatcherName = Some("almhirt.test-dispatcher")
        val shortDuration = conf.getDouble("almhirt.durations.short") seconds
        val mediumDuration = conf.getDouble("almhirt.durations.medium") seconds
        val longDuration = conf.getDouble("almhirt.durations.long") seconds
      }
    context
  }

  def inOwnContext[T](compute: AlmhirtContext => T): T = {
    val context = createTestContext
    val res = compute(context)
    context.actorSystem.shutdown()
    res
  }
}