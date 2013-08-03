package almhirt.testkit

import akka.actor.ActorRef
import almhirt.eventlog.impl.InMemoryEventLog

trait CreatesEventLog {
  def createEventLog(testId: Int): (ActorRef, () => Unit)
}

trait CreatesInMemoryEventLog extends CreatesEventLog { self: akka.testkit.TestKit =>
  def createEventLog(testId: Int): (ActorRef, () => Unit) =
    (system.actorOf(InMemoryEventLog.props(), "eventlog_" + testId.toString),
      () => ())
}