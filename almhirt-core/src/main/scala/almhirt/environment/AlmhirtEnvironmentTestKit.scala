package almhirt.environment

import akka.actor.ActorSystem
import almhirt._
import almhirt.commanding._
import almhirt.eventlog.impl._
import almhirt.domain.DomainEvent
import almhirt.messaging._
import almhirt.syntax.almvalidation._
import almhirt.parts.impl._
import com.typesafe.config._

trait AlmhirtEnvironmentTestKit {
  val contextTestKit = new AlmhirtContextTestKit {}
  private val configText =
    """  
      akka {
		loglevel = ERROR
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

  def createTestEnvironment(): AlmhirtEnvironment = createTestEnvironment(conf)
  def createTestEnvironment(aConf: Config): AlmhirtEnvironment = {
    implicit val almhirtCtx = contextTestKit.createTestContext(aConf)
    implicit val timeout = almhirtCtx.system.mediumDuration
    val tracker = util.OperationStateTracker()
    val trackerRegistration = (almhirtCtx.operationStateChannel <-<* { opState => tracker.updateState(opState) }).awaitResult.forceResult
    val repos = new UnsafeRepositoryRegistry(almhirtCtx)
    val cmdExecutor = new UnsafeCommandExecutorOnCallingThread(repos, almhirtCtx)
    val cmdExecutorRegistration = (almhirtCtx.commandChannel <-<* { cmdEnvelope => cmdExecutor.executeCommand(cmdEnvelope) }).awaitResult.forceResult
    val env =
      new AlmhirtEnvironment {
        val context = almhirtCtx

        val repositories = repos
        val commandExecutor = cmdExecutor
        val eventLog = new InefficientSerializingInMemoryDomainEventLog()
        val operationStateTracker = tracker
        def dispose {
          cmdExecutorRegistration.dispose
          trackerRegistration.dispose
          tracker.dispose
          context.dispose
        }
      }
    env
  }

   def inTestEnvironment[T](compute: AlmhirtEnvironment => T): T = inTestEnvironment[T](compute, conf)
  def inTestEnvironment[T](compute: AlmhirtEnvironment => T, conf: Config): T = {
    val context = createTestEnvironment(conf)
    val res = compute(context)
    context.dispose
    res
  }
}