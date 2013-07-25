package almhirt.testkit

import akka.actor._
import almhirt.core.HasAlmhirt
import almhirt.domaineventlog.impl.InMemoryDomainEventLog

trait CreatesEventLog {
  def createEventLog(testId: Int): ActorRef
}

trait CreatesInMemoryEventLog extends CreatesEventLog {self: akka.testkit.TestKit =>
  def createEventLog(testId: Int): ActorRef =
    system.actorOf(Props(new InMemoryDomainEventLog with Actor { override def receive: Actor.Receive = receiveDomainEventLogMsg }), "EventLog_" + testId.toString)
}

