package almhirt.environment.configuration.bootstrappers

import akka.event.LoggingAdapter
import almhirt.almvalidation.kit._
import almhirt.core._
import almhirt.environment.configuration._
import almhirt.eventlog.DomainEventLog
import almhirt.eventlog.impl.DomainEventLogActorHull

trait CreatesAndRegistersDomainEventLog extends CreatesCoreComponentsBootstrapperPhase with HasDomainEventLog { self: HasServiceRegistry with HasConfig =>
  override def domainEventLog: DomainEventLog = myDomainEventLog
  private var myDomainEventLog: DomainEventLog = null

  override def createCoreComponents(theAlmhirt: Almhirt, startUpLogger: LoggingAdapter): BootstrapperPhaseResult =
    super.createCoreComponents(theAlmhirt, startUpLogger).andThen {
      ConfigHelper.tryGetNotDisabledSubConfig(self.config, ConfigPaths.domaineventlog).foreach { eventLogConfig =>
        startUpLogger.info(s"Create DomainEventLog")
        val eventLogActor = SystemHelper.createEventLogFromFactory(theAlmhirt).forceResult
        startUpLogger.info(s"Register DomainEventLog")
        self.serviceRegistry.registerService[DomainEventLog](DomainEventLogActorHull(eventLogActor, config)(theAlmhirt))
      }
      BootstrapperPhaseSuccess()
    }
}