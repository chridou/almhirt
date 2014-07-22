package almhirt.eventlog

import akka.actor._
import almhirt.testkit._
import almhirt.testkit.eventlog.EventLogSpecTemplate

class InMemoryEventLogSpecs
  extends EventLogSpecTemplate(ActorSystem("InMemoryEventLogTests", TestConfigs.default))
  with AlmhirtFromAkkaTestKitWithoutConfiguration
  with CreatesInMemoryEventLog {
  
  override val sleepMillisAfterWrite = None
  
}