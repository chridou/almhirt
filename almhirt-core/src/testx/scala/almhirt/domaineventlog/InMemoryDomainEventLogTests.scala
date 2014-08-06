package almhirt.domaineventlog

import akka.actor._
import almhirt.testkit._
import almhirt.testkit.domaineventlog.DomainEventLogSpecTemplate

class InMemoryDomainEventLogTests
  extends DomainEventLogSpecTemplate(ActorSystem("InMemoryDomainEventLogTests", TestConfigs.default))
  with AlmhirtFromAkkaTestKitWithoutConfiguration
  with CreatesInMemoryDomainEventLog