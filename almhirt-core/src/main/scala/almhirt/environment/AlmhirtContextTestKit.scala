package almhirt.environment

import akka.actor.ActorSystem
import akka.util.Timeout.durationToTimeout
import akka.util.duration.doubleToDurationDouble
import almhirt._
import almhirt.commanding._
import almhirt.domain.DomainEvent
import almhirt.messaging.impl.DevNullMessageChannel
import almhirt.messaging.impl.DevNullMessageHub
import almhirt.messaging._
import almhirt.syntax.almvalidation._
import com.typesafe.config._

trait AlmhirtContextTestKit {
  private val configText =
    """  
      akka {
        default-dispatcher {
          type="akka.testkit.CallingThreadDispatcherConfigurator"
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

  def createTestContext(): AlmhirtContext = createTestContext(conf)
  def createTestContext(conf: Config): AlmhirtContext = {
    val uuidGen = new JavaUtilUuidGenerator()
    val akkaCtx = new AlmhirtSystem {
      val config = conf
      val actorSystem = ActorSystem(conf.getString("almhirt.systemname"), conf)
      val futureDispatcher = actorSystem.dispatcher
      val messageStreamDispatcherName = None
      val messageHubDispatcherName = None
      val shortDuration = conf.getDouble("almhirt.durations.short") seconds
      val mediumDuration = conf.getDouble("almhirt.durations.medium") seconds
      val longDuration = conf.getDouble("almhirt.durations.long") seconds
      def generateUuid = uuidGen.generate
      def dispose = actorSystem.shutdown
    }
    implicit val dur = akkaCtx.shortDuration
    val hub = MessageHub(Some("messageHub"), akkaCtx.actorSystem, akkaCtx.mediumDuration, akkaCtx.futureDispatcher, Some("almhirt.test-dispatcher"))
    val cmddChannel = hub.createUnnamedMessageChannel[CommandEnvelope](Some("commands")).awaitResult.forceResult
    val probTopic = None

    val context =
      new AlmhirtContext {
        val config = conf
        val system = akkaCtx
        val messageHub = hub
        val commandChannel = cmddChannel
        val domainEventsChannel = MessageChannel[DomainEvent](Some("domainEventsChannel"), akkaCtx.actorSystem, akkaCtx.mediumDuration, akkaCtx.futureDispatcher, Some("almhirt.test-dispatcher"), None, None)
        val problemChannel = MessageChannel[Problem](Some("problemChannel"), akkaCtx.actorSystem, akkaCtx.mediumDuration, akkaCtx.futureDispatcher, Some("almhirt.test-dispatcher"), None, None)
        val operationStateChannel = MessageChannel[OperationState](Some("operationStateChannel"), akkaCtx.actorSystem, akkaCtx.mediumDuration, akkaCtx.futureDispatcher, Some("almhirt.test-dispatcher"), None, None)

        val problemTopic = probTopic
        def reportProblem(prob: Problem) { 
          println("A problem has been captured:\r\n%s".format(prob))
          messageHub.broadcast(Message(prob)(system.generateUuid)) 
          }
        def reportOperationState(opState: OperationState) { messageHub.broadcast(Message(opState)(system.generateUuid)) }

        def dispose = system.dispose

      }
    context
  }

  def createFakeContext(): AlmhirtContext = createFakeContext(conf)
  def createFakeContext(conf: Config): AlmhirtContext = {
    val uuidGen = new JavaUtilUuidGenerator()
    val akkaCtx = new AlmhirtSystem {
      val config = conf
      val actorSystem = ActorSystem(conf.getString("almhirt.systemname"), conf)
      val futureDispatcher = actorSystem.dispatchers.lookup("almhirt.test-dispatcher")
      val messageStreamDispatcherName = None
      val messageHubDispatcherName = None
      val shortDuration = conf.getDouble("almhirt.durations.short") seconds
      val mediumDuration = conf.getDouble("almhirt.durations.medium") seconds
      val longDuration = conf.getDouble("almhirt.durations.long") seconds
      def generateUuid = uuidGen.generate
      def dispose = actorSystem.shutdown
    }
    val context =
      new AlmhirtContext {
        val config = conf
        val system = akkaCtx
        val messageHub = new DevNullMessageHub()(akkaCtx.futureDispatcher)
        val commandChannel = new DevNullMessageChannel[CommandEnvelope]()(akkaCtx.futureDispatcher)
        val domainEventsChannel = new DevNullMessageChannel[DomainEvent]()(akkaCtx.futureDispatcher)
        val problemChannel = new DevNullMessageChannel[Problem]()(akkaCtx.futureDispatcher)
        val operationStateChannel = new DevNullMessageChannel[OperationState]()(akkaCtx.futureDispatcher)

        val problemTopic = None
        def reportProblem(prob: Problem) {}
        def reportOperationState(opState: OperationState) {}

        val uuidGenerator = new JavaUtilUuidGenerator()

        def dispose = system.dispose

      }
    context
  }

  def inFakeContext[T](compute: AlmhirtContext => T): T = inFakeContext[T](compute, conf)
  def inFakeContext[T](compute: AlmhirtContext => T, conf: Config): T = {
    val context = createFakeContext(conf)
    val res = compute(context)
    context.dispose
    res
  }

  def inContext[T](compute: AlmhirtContext => T): T = inContext[T](compute, conf)
  def inContext[T](compute: AlmhirtContext => T, conf: Config): T = {
    val context = createTestContext(conf)
    val res = compute(context)
    context.dispose
    res
  }
}