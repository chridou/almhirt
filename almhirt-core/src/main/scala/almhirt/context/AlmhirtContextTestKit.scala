package almhirt.context

import akka.actor.ActorSystem
import akka.dispatch.MessageDispatcher
import akka.util.duration._
import almhirt._
import almhirt.messaging._
import almhirt.domain.DomainEvent
import almhirt.commanding.DomainCommand
import com.typesafe.config._
import almhirt.messaging.impl.NullMessageHub
import almhirt.messaging.impl.NullMessageChannel

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
        val config = conf
        val actorSystem = ActorSystem(conf.getString("almhirt.systemname"), conf)
        val futureDispatcher = actorSystem.dispatchers.lookup("almhirt.test-dispatcher")
        val messageStreamDispatcherName = Some("almhirt.test-dispatcher")
        val messageHubDispatcherName = Some("almhirt.test-dispatcher")
        val shortDuration = conf.getDouble("almhirt.durations.short") seconds
        val mediumDuration = conf.getDouble("almhirt.durations.medium") seconds
        val longDuration = conf.getDouble("almhirt.durations.long") seconds
        val messageHub = MessageHub(Some("messageHub"), actorSystem, mediumDuration, futureDispatcher, Some("almhirt.test-dispatcher"))
        val commandChannel = MessageChannel[DomainCommand](Some("commandChannel"), actorSystem, mediumDuration, futureDispatcher, Some("almhirt.test-dispatcher"), None, None)
        val domainEventsChannel = MessageChannel[DomainEvent](Some("domainEventsChannel"), actorSystem, mediumDuration, futureDispatcher, Some("almhirt.test-dispatcher"), None, None)
        val problemChannel = MessageChannel[Problem](Some("problemChannel"), actorSystem, mediumDuration, futureDispatcher, Some("almhirt.test-dispatcher"), None, None)
        val operationStateChannel = MessageChannel[OperationState](Some("operationStateChannel"), actorSystem, mediumDuration, futureDispatcher, Some("almhirt.test-dispatcher"), None, None)
      }
    context
  }

  def createFakeContext: AlmhirtContext = {
    val context =
      new AlmhirtContext {
        def config = conf
        def actorSystem = ActorSystem(conf.getString("almhirt.systemname"), conf)
        def futureDispatcher = actorSystem.dispatchers.lookup("almhirt.test-dispatcher")
        def messageStreamDispatcherName = None
        def messageHubDispatcherName = None
        val shortDuration = conf.getDouble("almhirt.durations.short") seconds
        val mediumDuration = conf.getDouble("almhirt.durations.medium") seconds
        val longDuration = conf.getDouble("almhirt.durations.long") seconds
        def messageHub = new NullMessageHub()(futureDispatcher)
        def commandChannel = new NullMessageChannel()(futureDispatcher)
        def domainEventsChannel = new NullMessageChannel()(futureDispatcher)
        def problemChannel = new NullMessageChannel()(futureDispatcher)
        def operationStateChannel = new NullMessageChannel()(futureDispatcher)
      }
    context
  }

  def inFakeContext[T](compute: AlmhirtContext => T): T = {
    val context = createFakeContext
    val res = compute(context)
    context.actorSystem.shutdown()
    res
  }

  def inContext[T](compute: AlmhirtContext => T): T = {
    val context = createTestContext
    val res = compute(context)
    context.actorSystem.shutdown()
    res
  }
}