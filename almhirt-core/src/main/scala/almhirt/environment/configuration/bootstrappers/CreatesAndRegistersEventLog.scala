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
      ConfigHelper.tryGetNotDisabledSubConfig(self.config, ConfigPaths.eventlog).map { eventLogConfig =>
        startUpLogger.info(s"Create EventLog")
        val eventLogActor = SystemHelper.createEventLogFromFactory(theAlmhirt).forceResult
        var eventLogRegistration =
          (if (ConfigHelper.isBooleanSetToFalse(eventLogConfig)("log_domain_events")) {
            startUpLogger.info(s"The event log does NOT log domain events")
            (self.eventsChannel.actor ? SubscribeQry(MessagingSubscription.forActorWithFilter[Event](eventLogActor, payload => !payload.isInstanceOf[DomainEvent])))(atMost)
          } else {
            startUpLogger.info(s"The event log DOES log domain events")
            (self.eventsChannel.actor ? SubscribeQry(MessagingSubscription.forActor[Event](eventLogActor)))(atMost)
          })
            .mapTo[SubscriptionRsp]
            .map(_.registration)
            .toAlmFuture
            .awaitResult
            .forceResult
        myEventLog = EventLogActorHull(eventLogActor, config)(theAlmhirt)
        startUpLogger.info(s"Register EventLog")
        self.serviceRegistry.registerService[EventLog](myEventLog)
        BootstrapperPhaseSuccess(CleanUpAction(() => eventLogRegistration.dispose(), "EventLog"))
      }.getOrElse(BootstrapperPhaseSuccess())
    }
}