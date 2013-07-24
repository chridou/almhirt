package almhirt.testkit

import akka.actor._
import almhirt.core.HasAlmhirt
import almhirt.domaineventlog.impl.InMemoryDomainEventLog

trait CreatesEventLog {
  def createEventLog(specId: Int): ActorRef
}

trait CreatesInMemoryEventLog extends CreatesEventLog {self: HasAlmhirt =>
  def createEventLog(specId: Int): ActorRef =
    theAlmhirt.actorSystem.actorOf(Props(new InMemoryDomainEventLog with Actor { override def receive: Actor.Receive = receiveDomainEventLogMsg }), "EventLog_" + specId.toString)
}

