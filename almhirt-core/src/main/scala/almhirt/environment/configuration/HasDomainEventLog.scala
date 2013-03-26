package almhirt.environment.configuration

import almhirt.eventlog.DomainEventLog

trait HasDomainEventLog {
  def domainEventLog: DomainEventLog
}