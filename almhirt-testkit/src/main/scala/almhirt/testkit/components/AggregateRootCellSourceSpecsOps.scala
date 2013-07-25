package almhirt.testkit.components

import akka.testkit.TestProbe
import almhirt.testkit._
import akka.actor._
import almhirt.core._

trait AggregateRootCesllSourceSpecsOps { self: AlmhirtTestKit with CreatesCellSource =>
  def useCellSource[T](eventlog: ActorRef)(f: ActorRef => T): T = {
    val testId = nextTestId
    val cellSource = createCellSource(testId, eventlog)
    val close = () => { this.system.stop(cellSource); this.system.stop(eventlog)}
    try {
      val res = f(cellSource)
      close()
      res
    } catch {
      case exn: Exception =>
        close()
        throw exn
    }
  }
}

trait AggregateRootCellSourceSpecsOpsWithEventLog { self: AlmhirtTestKit with CreatesCellSource with CreatesEventLog =>
  def useCellSourceWithEventLog[T](f: (ActorRef, ActorRef) => T): T = {
    val testId = nextTestId
    val (eventlog, eventLogCleanUp) = createEventLog(testId)
    val cellSource = createCellSource(testId, eventlog)
    val close = () => { this.system.stop(cellSource); this.system.stop(eventlog); eventLogCleanUp()}
    try {
      val res = f(cellSource, eventlog)
      close()
      res
    } catch {
      case exn: Exception =>
        close()
        throw exn
    }
  }
}

trait AggregateRootCellSourceSpecsOpsWithEventLogSpy {self: AlmhirtTestKit with CreatesCellSource =>
  def useCellSourceWithEventLogSpy[T](f: (ActorRef, TestProbe) => T): T = {
    val testId = nextTestId
    val eventlog = TestProbe()
    val cellSource = createCellSource(testId, eventlog.ref)
    val close = () => { this.system.stop(cellSource); this.system.stop(eventlog.ref)}
    try {
      val res = f(cellSource, eventlog)
      close()
      res
    } catch {
      case exn: Exception =>
        close()
        throw exn
    }
  }
}