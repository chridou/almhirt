package almhirt.environment.configuration

import almhirt.eventlog.EventLog

trait HasEventLog {
  def eventLog: EventLog
}