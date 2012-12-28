package almhirt.environment

import akka.actor.ActorSystem
import akka.util.Timeout.durationToTimeout
import almhirt.common._
import almhirt.commanding._
import almhirt.domain.DomainEvent
import almhirt.messaging._
import almhirt.syntax.almvalidation._
import almhirt.util._
import com.typesafe.config._

class AlmhirtContextForTesting(context: AlmhirtContext, val system: AlmhirtSystem) extends AlmhirtContext {
  def messageHub = context.messageHub
  def commandChannel = context.commandChannel
  def domainEventsChannel = context.domainEventsChannel
  def problemChannel = context.problemChannel
  def operationStateChannel = context.operationStateChannel
  def reportProblem(prob: Problem) = context.reportProblem(prob)
  def reportOperationState(opState: OperationState) = context.reportOperationState(opState)
  def broadcastDomainEvent(event: DomainEvent) = context.broadcastDomainEvent(event)
  def postCommand(comEnvelope: CommandEnvelope) = context.postCommand(comEnvelope)
  def broadcast[T <: AnyRef](payload: T, metaData: Map[String, String] = Map.empty) = context.broadcast(payload, metaData)
  def createMessage[T <: AnyRef](payload: T, metaData: Map[String, String] = Map.empty) = context.createMessage(payload, metaData)
  def executionContext = context.executionContext

  def shortDuration = context.shortDuration
  def mediumDuration = context.mediumDuration
  def longDuration = context.longDuration

  def getDateTime = system.getDateTime
  def getUuid = system.getUuid
  
  def dispose = context.dispose
}

trait AlmhirtContextTestKit {
  val systemTestKit = new AlmhirtsystemTestkit {}
  private val configText =
    """  
      akka {
		loglevel = WARNING
      }
      almhirt {
		systemname = "almhirt-testing"
		durations {
		  short = 500
		  medium = 2500
		  long = 10000
		}
     }
    """
  val defaultConf = ConfigFactory.parseString(configText).withFallback(ConfigFactory.load)

  def createTestContext(): AlmhirtContextForTesting = createTestContext(defaultConf)
  def createTestContext(aConf: Config): AlmhirtContextForTesting = {
    implicit val almhirtSys = systemTestKit.createTestSystem(aConf)
    new AlmhirtContextForTesting(AlmhirtContext().awaitResult(almhirtSys.shortDuration).forceResult, almhirtSys)
  }

  def inTestContext[T](compute: AlmhirtContextForTesting => T): T = inTestContext[T](compute, defaultConf)
  def inTestContext[T](compute: AlmhirtContextForTesting => T, aConf: Config): T = {
    val context = createTestContext(aConf)
    val res = compute(context)
    context.dispose
    res
  }
}