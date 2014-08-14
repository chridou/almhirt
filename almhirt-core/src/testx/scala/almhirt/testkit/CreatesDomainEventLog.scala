package almhirt.testkit

import akka.actor._
import almhirt.core.HasAlmhirt
import almhirt.core.types._
import almhirt.domaineventlog.impl.InMemoryDomainEventLog
import almhirt.messaging.MessagePublisher

trait CreatesDomainEventLog {
  def createDomainEventLog(testId: Int): (ActorRef, () ⇒ Unit)
}

trait CreatesInMemoryDomainEventLog extends CreatesDomainEventLog { self: akka.testkit.TestKit ⇒
  def createDomainEventLog(testId: Int): (ActorRef, () ⇒ Unit) =
    (system.actorOf(Props(new InMemoryDomainEventLog with Actor with ActorLogging {
      override def receive: Actor.Receive = receiveDomainEventLogMsg
      override def publishCommittedEvent(event: DomainEvent) {}
    }), "domaineventlog_" + testId.toString),
      () ⇒ ())
}

