package almhirt.environment

import akka.actor.ActorSystem
import akka.util.Timeout.durationToTimeout
import akka.util.duration.doubleToDurationDouble
import almhirt._
import almhirt.commanding._
import almhirt.domain.DomainEvent
import almhirt.messaging._
import almhirt.syntax.almvalidation._
import com.typesafe.config._
import almhirt.util._

trait AlmhirtContextTestKit {
  private val configText =
    """  
      akka {
		loglevel = WARNING
      }
      almhirt {
		systemname = "almhirt-testing"
		durations {
		  short = 0.5
		  medium = 2.5
		  long = 10.0
		}
     }
    """
  val conf = ConfigFactory.parseString(configText).withFallback(ConfigFactory.load)

  def createTestContext(): AlmhirtContext = createTestContext(conf)
  def createTestContext(conf: Config): AlmhirtContext = {
    val uuidGen = new JavaUtilUuidGenerator()
    implicit val almhirtSys = new AlmhirtSystem {
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
    implicit val dur = almhirtSys.shortDuration
    val hub = MessageHub("messageHub")
    val cmdChannel = hub.createMessageChannel[CommandEnvelope]("commandChannel").awaitResult.forceResult
    val opStateChannel = hub.createMessageChannel[OperationState]("operationStateChannel").awaitResult.forceResult
    val probChannel = hub.createMessageChannel[Problem]("problemChannel").awaitResult.forceResult
    val domEventsChannel = hub.createMessageChannel[DomainEvent]("domainEventsChannel").awaitResult.forceResult
    val probTopic = None

    val context =
      new AlmhirtContext {
        val config = conf
        val system = almhirtSys
        val messageHub = hub
        val commandChannel = cmdChannel
        val domainEventsChannel = domEventsChannel
        val problemChannel = probChannel
        val operationStateChannel = opStateChannel

        val problemTopic = probTopic

        def dispose = {
          messageHub.close
          cmdChannel.close
          opStateChannel.close
          probChannel.close
          domEventsChannel.close
          system.dispose
        }

      }
    context
  }

  def inTestContext[T](compute: AlmhirtContext => T): T = inTestContext[T](compute, conf)
  def inTestContext[T](compute: AlmhirtContext => T, conf: Config): T = {
    val context = createTestContext(conf)
    val res = compute(context)
    context.dispose
    res
  }
}