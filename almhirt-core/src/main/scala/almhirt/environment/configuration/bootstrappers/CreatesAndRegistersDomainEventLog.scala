package almhirt.environment.configuration.bootstrappers

import akka.event.LoggingAdapter
import almhirt.almvalidation.kit._
import almhirt.core._
import almhirt.environment.configuration._
import almhirt.eventlog.DomainEventLog
import almhirt.eventlog.impl.DomainEventLogActorHull

trait CreatesAndRegistersDomainEventLog extends CreatesCoreComponentsBootstrapperPhase with HasDomainEventLog { self: HasServiceRegistry with HasConfig =>
  override def domainEventLog: DomainEventLog = {
    if (myDomainEventLog == null)
      throw new Exception("You are trying to access the DomainEventLog. It has not yet been initialized. A solution might be to adjust the ordering of the bootstrapper traits.")
    myDomainEventLog
  }
  private var myDomainEventLog: DomainEventLog = null

  override def createCoreComponents(theAlmhirt: Almhirt, startUpLogger: LoggingAdapter): BootstrapperPhaseResult =
    super.createCoreComponents(theAlmhirt, startUpLogger).andThen {
      startUpLogger.info(s"""Create DomainEventLog from config section "${ConfigPaths.domaineventlog}"""")
      ConfigHelper.tryGetNotDisabledSubConfig(self.config, ConfigPaths.domaineventlog) match {
        case Some(eventLogConfig) =>
          val eventLogActor = SystemHelper.createDomainEventLogFromFactory(theAlmhirt).forceResult
          startUpLogger.info(s"Register DomainEventLog")
          myDomainEventLog = DomainEventLogActorHull(eventLogActor, config)(theAlmhirt)
          self.serviceRegistry.registerService[DomainEventLog](myDomainEventLog)
          BootstrapperPhaseSuccess()
        case None =>
          startUpLogger.warning("""Tried to initialize a domain event log, but it has no config section or is explicitly disabled with "disabled=true" in its config section.""")
          BootstrapperPhaseSuccess()
      }
    }
}