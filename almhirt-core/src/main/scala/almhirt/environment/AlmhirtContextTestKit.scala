package almhirt.environment

import akka.actor.ActorSystem
import akka.util.Timeout.durationToTimeout
import akka.util.duration.doubleToDurationDouble
import almhirt.almakka.AlmAkkaContext
import almhirt.commanding.DomainCommand
import almhirt.domain.DomainEvent
import almhirt.messaging.impl.DevNullMessageChannel
import almhirt.messaging.impl.DevNullMessageHub
import almhirt.messaging.MessageChannel
import almhirt.messaging.MessageHub
import almhirt.OperationState
import almhirt.Problem
import almhirt.syntax.almvalidation._
import com.typesafe.config.ConfigFactory

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
    val akkaCtx = new AlmAkkaContext {
      val config = conf
      val actorSystem = ActorSystem(conf.getString("almhirt.systemname"), conf)
      val futureDispatcher = actorSystem.dispatchers.lookup("almhirt.test-dispatcher")
      val messageStreamDispatcherName = Some("almhirt.test-dispatcher")
      val messageHubDispatcherName = Some("almhirt.test-dispatcher")
      val shortDuration = conf.getDouble("almhirt.durations.short") seconds
      val mediumDuration = conf.getDouble("almhirt.durations.medium") seconds
      val longDuration = conf.getDouble("almhirt.durations.long") seconds
    }
    implicit val dur = akkaCtx.shortDuration
    val hub = MessageHub(Some("messageHub"), akkaCtx.actorSystem, akkaCtx.mediumDuration, akkaCtx.futureDispatcher, Some("almhirt.test-dispatcher"))
    val cmddChannel = hub.createMessageChannel[DomainCommand](Some("commands")).awaitResult.forceResult

    val context =
      new AlmhirtContext {
        val config = conf
        val akkaContext = akkaCtx
        val messageHub = hub
        val commandChannel = cmddChannel
        val domainEventsChannel = MessageChannel[DomainEvent](Some("domainEventsChannel"), akkaCtx.actorSystem, akkaCtx.mediumDuration, akkaCtx.futureDispatcher, Some("almhirt.test-dispatcher"), None, None)
        val problemChannel =  MessageChannel[Problem](Some("problemChannel"), akkaCtx.actorSystem, akkaCtx.mediumDuration, akkaCtx.futureDispatcher, Some("almhirt.test-dispatcher"), None, None)
        val operationStateChannel = MessageChannel[OperationState](Some("operationStateChannel"), akkaCtx.actorSystem, akkaCtx.mediumDuration, akkaCtx.futureDispatcher, Some("almhirt.test-dispatcher"), None, None)
      }
    context
  }

  def createFakeContext: AlmhirtContext = {
    val akkaCtx = new AlmAkkaContext {
        val config = conf
        val actorSystem = ActorSystem(conf.getString("almhirt.systemname"), conf)
        val futureDispatcher = actorSystem.dispatchers.lookup("almhirt.test-dispatcher")
        val messageStreamDispatcherName = None
        val messageHubDispatcherName = None
        val shortDuration = conf.getDouble("almhirt.durations.short") seconds
        val mediumDuration = conf.getDouble("almhirt.durations.medium") seconds
        val longDuration = conf.getDouble("almhirt.durations.long") seconds
    }
    val context =
      new AlmhirtContext {
        val config = conf
        val akkaContext = akkaCtx
        val messageHub = new DevNullMessageHub()(akkaCtx.futureDispatcher)
        val commandChannel = new DevNullMessageChannel[DomainCommand]()(akkaCtx.futureDispatcher)
        val domainEventsChannel = new DevNullMessageChannel[DomainEvent]()(akkaCtx.futureDispatcher)
        val problemChannel = new DevNullMessageChannel[Problem]()(akkaCtx.futureDispatcher)
        val operationStateChannel = new DevNullMessageChannel[OperationState]()(akkaCtx.futureDispatcher)
      }
    context
  }

  def inFakeContext[T](compute: AlmhirtContext => T): T = {
    val context = createFakeContext
    val res = compute(context)
    context.akkaContext.actorSystem.shutdown()
    res
  }

  def inContext[T](compute: AlmhirtContext => T): T = {
    val context = createTestContext
    val res = compute(context)
    context.akkaContext.actorSystem.shutdown()
    res
  }
}