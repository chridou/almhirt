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

trait CreatesAndRegistersDefaultChannels extends CreatesCoreComponentsBootstrapperPhase with HasStandardChannels { self: HasServiceRegistry =>

  override def commandChannel: CommandChannel = {
    if(myCommandChannel == null)
      throw new Exception("You are trying to access the CommandChannel. It has not yet been initialized. A solution might be to adjust the ordering of the bootstrapper traits.")
    myCommandChannel
  }
  override def eventsChannel: EventsChannel = {
    if(myCommandChannel == null)
      throw new Exception("You are trying to access the EventsChannel. It has not yet been initialized. A solution might be to adjust the ordering of the bootstrapper traits.")
    myEventsChannel
  }
  override def domainEventsChannel: DomainEventsChannel = {
    if(myCommandChannel == null)
      throw new Exception("You are trying to access the DomainEventsChannel. It has not yet been initialized. A solution might be to adjust the ordering of the bootstrapper traits.")
    myDomainEventsChannel
  }
  override def operationStateChannel: OperationStateChannel = {
    if(myCommandChannel == null)
      throw new Exception("You are trying to access the OperationStateChannel. It has not yet been initialized. A solution might be to adjust the ordering of the bootstrapper traits.")
    myOperationStateChannel
  }

  private var myCommandChannel: CommandChannel = null
  private var myEventsChannel: EventsChannel = null
  private var myDomainEventsChannel: DomainEventsChannel = null
  private var myOperationStateChannel: OperationStateChannel = null

  override def createCoreComponents(theAlmhirt: Almhirt, startUpLogger: LoggingAdapter): BootstrapperPhaseResult =
    super.createCoreComponents(theAlmhirt, startUpLogger).andThen(createChannels(theAlmhirt, startUpLogger))

  private def createChannels(theAlmhirt: Almhirt, startUpLogger: LoggingAdapter): BootstrapperPhaseResult =
    inTryCatch {
      implicit val dur = Duration(1, "s")
      implicit val hasExecContext = theAlmhirt
      startUpLogger.info("Create CommandChannel, OperationStateChannel, DomainEventsChannel, ProblemsChannel")

      val commandChannelFuture = theAlmhirt.messageHub.createMessageChannel[CommandEnvelope]("CommandChannel")
      val operationStateChannelFuture = theAlmhirt.messageHub.createMessageChannel[OperationState]("OperationStateChannel")
      val eventsChannelChannelFuture = theAlmhirt.messageHub.createMessageChannel[Event]("EventsChannel")
      val domainEventsChannelFuture =
        eventsChannelChannelFuture.flatMap(eventsChannel => eventsChannel.createSubChannel[DomainEvent]("DomainEventsChannel"))
      val problemsChannelFuture = theAlmhirt.messageHub.createMessageChannel[Problem]("ProblemsChannel")

      val channels =
        (for {
          commandChannel <- commandChannelFuture
          operationStateChannel <- operationStateChannelFuture
          eventsChannel <- eventsChannelChannelFuture
          domainEventsChannel <- domainEventsChannelFuture
          problemsChannel <- problemsChannelFuture
        } yield (
          new CommandChannelWrapper(commandChannel),
          new OperationStateChannelWrapper(operationStateChannel),
          new EventsChannelWrapper(eventsChannel),
          new DomainEventsChannelWrapper(domainEventsChannel))).awaitResult

      channels.foreach { x =>
        myCommandChannel = x._1
        myOperationStateChannel = x._2
        myEventsChannel = x._3
        myDomainEventsChannel = x._4
        startUpLogger.info("Register CommandChannel")
        self.serviceRegistry.registerService[CommandChannel](myCommandChannel)
        startUpLogger.info("Register OperationStateChannel")
        self.serviceRegistry.registerService[OperationStateChannel](myOperationStateChannel)
        startUpLogger.info("Register EventsChannel")
        self.serviceRegistry.registerService[EventsChannel](myEventsChannel)
        startUpLogger.info("Register DomainEventsChannel")
        self.serviceRegistry.registerService[DomainEventsChannel](myDomainEventsChannel)
      }
      BootstrapperPhaseSuccess()
    }.toBootstrapperPhaseResult
}