package almhirt.environment.configuration.bootstrappers

import scala.concurrent.duration.Duration
import scalaz.syntax.validation._
import akka.event.LoggingAdapter
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.core._
import almhirt.environment.configuration._
import almhirt.commanding.CommandEnvelope
import almhirt.util.OperationState
import almhirt.domain.DomainEvent
import almhirt.environment._

trait CreatesAndRegistersDefaultChannels extends CreatesCoreComponentsBootstrapperPhase with HasStandardChannels{ self: HasServiceRegistry =>
  
  override def commandChannel: CommandChannel = myCommandChannel
  override def domainEventsChannel: DomainEventsChannel = myDomainEventsChannel
  override def operationStateChannel: OperationStateChannel = myOperationStateChannel
  override def problemChannel: ProblemChannel = myProblemChannel
  
  private var myCommandChannel: CommandChannel = null
  private var myDomainEventsChannel: DomainEventsChannel = null
  private var myOperationStateChannel: OperationStateChannel = null
  private var myProblemChannel: ProblemChannel = null
  
  
  override def createCoreComponents(theAlmhirt: Almhirt, startUpLogger: LoggingAdapter): BootstrapperPhaseResult =
    super.createCoreComponents(theAlmhirt, startUpLogger).andThen(createChannels(theAlmhirt, startUpLogger))

  private def createChannels(theAlmhirt: Almhirt, startUpLogger: LoggingAdapter): BootstrapperPhaseResult =
    inTryCatch {
      implicit val dur = Duration(1, "s")
      implicit val hasExecContext = theAlmhirt
      startUpLogger.info("Create CommandChannel, OperationStateChannel, DomainEventsChannel, ProblemsChannel")
      val channels =
        (for {
          commandChannel <- theAlmhirt.messageHub.createMessageChannel[CommandEnvelope]("CommandChannel")
          operationStateChannel <- theAlmhirt.messageHub.createMessageChannel[OperationState]("OperationStateChannel")
          domainEventsChannel <- theAlmhirt.messageHub.createMessageChannel[DomainEvent]("DomainEventsChannel")
          problemsChannel <- theAlmhirt.messageHub.createMessageChannel[Problem]("ProblemsChannel")
        } yield (
          new CommandChannelWrapper(commandChannel),
          new OperationStateChannelWrapper(operationStateChannel),
          new DomainEventsChannelWrapper(domainEventsChannel),
          new ProblemChannelWrapper(problemsChannel))).awaitResult

      channels.foreach { x =>
        myCommandChannel = x._1
        myOperationStateChannel = x._2
        myDomainEventsChannel = x._3
        myProblemChannel = x._4
        startUpLogger.info("Register CommandChannel")
        self.serviceRegistry.registerService[CommandChannel](myCommandChannel)
        startUpLogger.info("Register OperationStateChannel")
        self.serviceRegistry.registerService[OperationStateChannel](myOperationStateChannel)
        startUpLogger.info("Register DomainEventsChannel")
        self.serviceRegistry.registerService[DomainEventsChannel](myDomainEventsChannel)
        startUpLogger.info("Register ProblemsChannel")
        self.serviceRegistry.registerService[ProblemChannel](myProblemChannel)
      }
      BootstrapperPhaseSuccess()
    }.toBootstrapperPhaseResult
}