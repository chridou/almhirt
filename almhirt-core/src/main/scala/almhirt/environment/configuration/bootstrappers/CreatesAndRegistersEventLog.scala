package almhirt.environment.configuration.bootstrappers

import akka.event.LoggingAdapter
import almhirt.almvalidation.kit._
import almhirt.core._
import almhirt.environment.configuration._
import almhirt.environment.HasStandardChannels
import almhirt.eventlog.EventLog
import almhirt.eventlog.impl.EventLogActorHull

trait CreatesAndRegistersEventLog extends CreatesCoreComponentsBootstrapperPhase with HasEventLog { self: HasServiceRegistry with HasConfig with HasStandardChannels =>
  override def eventLog: EventLog = {
    if(myEventLog == null)
      throw new Exception("You are trying to access the EventLog. It has not yet been initialized. A solution might be to adjust the ordering of the bootstrapper traits.")
    myEventLog
  }
  private var myEventLog: EventLog = null

  override def createCoreComponents(theAlmhirt: Almhirt, startUpLogger: LoggingAdapter): BootstrapperPhaseResult =
    super.createCoreComponents(theAlmhirt, startUpLogger).andThen {
      ConfigHelper.tryGetNotDisabledSubConfig(self.config, ConfigPaths.eventlog).foreach { eventLogConfig =>
        startUpLogger.info(s"Create EventLog")
        val eventLogActor = SystemHelper.createEventLogFromFactory(theAlmhirt).forceResult
        startUpLogger.info(s"Register EventLog")
        self.serviceRegistry.registerService[EventLog](EventLogActorHull(eventLogActor, config)(theAlmhirt))
      }
      BootstrapperPhaseSuccess()
    }
}