package almhirt.testkit

import akka.actor._
import almhirt.core.HasAlmhirt
import almhirt.domaineventlog.impl.InMemoryDomainEventLog

trait CreatesDomainEventLog {
  def createDomainEventLog(testId: Int): (ActorRef, () => Unit)
}

trait CreatesInMemoryDomainEventLog extends CreatesDomainEventLog { self: akka.testkit.TestKit =>
  def createDomainEventLog(testId: Int): (ActorRef, () => Unit) =
    (system.actorOf(Props(new InMemoryDomainEventLog with Actor { override def receive: Actor.Receive = receiveDomainEventLogMsg }), "EventLog_" + testId.toString),
      () => ())
}

