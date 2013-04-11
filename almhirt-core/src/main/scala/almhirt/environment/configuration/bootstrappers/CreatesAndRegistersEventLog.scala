package almhirt.environment.configuration.bootstrappers

import scala.concurrent.duration.Duration
import akka.pattern._
import akka.event.LoggingAdapter
import almhirt.almvalidation.kit._
import almhirt.almfuture.all._
import almhirt.core._
import almhirt.messaging._
import almhirt.environment.configuration._
import almhirt.environment.HasStandardChannels
import almhirt.domain.DomainEvent
import almhirt.eventlog.EventLog
import almhirt.eventlog.impl.EventLogActorHull
import almhirt.eventlog.LogEventQry

trait CreatesAndRegistersEventLog extends CreatesCoreComponentsBootstrapperPhase with HasEventLog { self: HasServiceRegistry with HasConfig with HasStandardChannels =>
  override def eventLog: EventLog = {
    if (myEventLog == null)
      throw new Exception("You are trying to access the EventLog. It has not yet been initialized. A solution might be to adjust the ordering of the bootstrapper traits.")
    myEventLog
  }
  private var myEventLog: EventLog = null

  override def createCoreComponents(theAlmhirt: Almhirt, startUpLogger: LoggingAdapter): BootstrapperPhaseResult =
    super.createCoreComponents(theAlmhirt, startUpLogger).andThen {
      implicit val atMost = Duration(1, "s")
      implicit val executionContext = theAlmhirt.executionContext
      implicit val hasExecutionContext = theAlmhirt
      startUpLogger.info(s"""Create EventLog from config section "${ConfigPaths.eventlog}"""")
      ConfigHelper.tryGetNotDisabledSubConfig(self.config, ConfigPaths.eventlog) match {
        case Some(eventLogConfig) =>
          val eventLogActor = SystemHelper.createEventLogFromFactory(theAlmhirt).forceResult
          var eventLogRegistration =
            (self.eventsChannel.actor ? SubscribeQry(MessagingSubscription.forActorMapped[Event, LogEventQry](
              eventLogActor,
              event => LogEventQry(event, None))))(atMost)
              .mapTo[SubscriptionRsp]
              .map(_.registration)
              .toAlmFuture
              .awaitResult
              .forceResult
          myEventLog = EventLogActorHull(eventLogActor, config)(theAlmhirt)
          startUpLogger.info(s"Register EventLog")
          self.serviceRegistry.registerService[EventLog](myEventLog)
          BootstrapperPhaseSuccess(CleanUpAction(() => eventLogRegistration.dispose(), "EventLog"))
        case None =>
          startUpLogger.warning("""Tried to initialize an event log, but it has no config section or is explicitly disabled with "disabled=true" in its config section.""")
          BootstrapperPhaseSuccess()
      }
    }
}