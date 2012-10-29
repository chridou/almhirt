package almhirt.environment

import akka.actor.ActorSystem
import almhirt._
import almhirt.almakka.AlmAkkaContext
import almhirt.commanding._
import almhirt.eventlog.impl._
import almhirt.domain.DomainEvent
import almhirt.messaging.impl._
import almhirt.messaging._
import almhirt.syntax.almvalidation._
import almhirt.parts.impl._
import com.typesafe.config._

trait AlmhirtEnvironmentTestKit {
  val contextTestKit = new AlmhirtContextTestKit {}
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

  def createTestEnvironment(): AlmhirtEnvironment = createTestEnvironment(conf)
  def createTestEnvironment(aConf: Config): AlmhirtEnvironment = {
    implicit val almhirtCtx = contextTestKit.createTestContext(aConf)
    val env =
      new AlmhirtEnvironment {
        val context = almhirtCtx

        val repositories = new UnsafeRepositoryRegistry()
        val commandExecutor = new UnsafeCommandExecutorOnCallingThread(repositories, almhirtCtx)
        val eventLog = new InefficientSerialziedInMemoryDomainEventLog()

        def dispose = context.dispose
      }
    env
  }

  def createFakeEnvironment(): AlmhirtEnvironment = createFakeEnvironment(conf)
  def createFakeEnvironment(aConf: Config): AlmhirtEnvironment = {
    implicit val almhirtCtx = contextTestKit.createTestContext(aConf)
    val env =
      new AlmhirtEnvironment {
        val context = almhirtCtx

        val repositories = new DevNullRepositoryRegistry()
        val commandExecutor = new DevNullCommandExecutor()
        val eventLog = new DevNullEventLog

        def dispose = context.dispose
      }
    env
  }

  def inFakeEnvironment[T](compute: AlmhirtEnvironment => T): T = inFakeEnvironment[T](compute, conf)
  def inFakeEnvironment[T](compute: AlmhirtEnvironment => T, conf: Config): T = {
    val context = createFakeEnvironment(conf)
    val res = compute(context)
    context.dispose
    res
  }

  def inTestEnvironment[T](compute: AlmhirtEnvironment => T): T = inTestEnvironment[T](compute, conf)
  def inTestEnvironment[T](compute: AlmhirtEnvironment => T, conf: Config): T = {
    val context = createTestEnvironment(conf)
    val res = compute(context)
    context.dispose
    res
  }
}