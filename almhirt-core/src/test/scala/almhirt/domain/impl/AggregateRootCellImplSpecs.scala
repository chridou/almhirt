package almhirt.domain.impl

import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers
import akka.testkit._
import almhirt.testing.TestConfigs
import scala.concurrent.duration._
import scala.concurrent.Await
import scalaz._
import akka.actor._
import akka.pattern._
import akka.util.Timeout
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.core.Almhirt
import almhirt.domaineventlog.impl.InMemoryDomainEventLog
import almhirt.domain._

class AggregateRootCellImplSpecs extends TestKit(ActorSystem("AggregateRootCellSystem", TestConfigs.default)) with FunSpec with ShouldMatchers {
  import AggregateRootCell._
  import DomainMessages._
  import almhirt.domaineventlog._

  val almhirtAndHandle = Almhirt(this.system).awaitResult(FiniteDuration(5, "s")).forceResult

  implicit val theAlmhirt = almhirtAndHandle._1
  implicit val defaultTimeout = theAlmhirt.durations.shortDuration

  val uniqueId = new java.util.concurrent.atomic.AtomicInteger(1)

  val arId = theAlmhirt.getUuid

  def createCellAndEventLog(nameSuffix: String): (ActorRef, ActorRef, () => Unit) = {
    val eventLog = this.system.actorOf(Props(new InMemoryDomainEventLog with Actor { override def receive: Actor.Receive = receiveDomainEventLogMsg }), "EventLog_" + nameSuffix)
    val cell = this.system.actorOf(Props(new AggregateRootCellImpl with Actor {
      type Event = TestArEvent
      type AR = TestAr
      def publisher = theAlmhirt.messageBus
      val managedAggregateRooId = arId
      def rebuildAggregateRoot(events: Iterable[TestArEvent]) = TestAr.rebuildFromHistory(events)
      val theAlmhirt = AggregateRootCellImplSpecs.this.theAlmhirt
      val domainEventLog = eventLog
      override def receive: Actor.Receive = receiveAggregateRootCellMsg
    }), "ArCell_" + nameSuffix)
    (cell, eventLog, () => { cell ! PoisonPill; eventLog ! PoisonPill })
  }

  def inCellWithEventLog[T](f: (ActorRef, ActorRef) => T): T = {
    val (cell, eventLog, close) = createCellAndEventLog(uniqueId.getAndIncrement().toString)
    try {
      val res = f(cell, eventLog)
      close()
      res
    } catch {
      case exn: Exception => 
        close()
        throw exn
    }
  }

  describe("An AggregateRootCellImpl") {
    it("should be creatable") {
      inCellWithEventLog {
        case (cell, eventlog) =>
          true should be(true)
      }
    }
    it("should answer with a AggregateRootNotFound when the aggregate root does not exist") {
      inCellWithEventLog {
        case (cell, eventlog) =>
          val resF = (cell ? GetAggregateRoot)(defaultTimeout).mapTo[AggregateRootNotFound]
          val res = Await.result(resF, defaultTimeout)
          res should equal(AggregateRootNotFound(arId))
      }
    }
    it("should answer with an AggregateRoot when the aggregate root does exist") {
      val (ar, events) = 
        (for {
          state1 <- TestAr.fromScratch(arId, "a")
          state2 <- state1.changeB(Some("b"))
        } yield state2).result.forceResult
      inCellWithEventLog {
        case (cell, eventlog) =>
          Await.result((eventlog ? DomainEventLog.CommitDomainEvents(events))(defaultTimeout), defaultTimeout) 
          val resF = (cell ? GetAggregateRoot)(defaultTimeout).mapTo[RequestedAggregateRoot]
          val res = Await.result(resF, defaultTimeout)
          res should equal(RequestedAggregateRoot(ar))
      }
    }
    it("should answer with an AggregateRootWasDeleted when the aggregate root is marked as deleted") {
      val (ar, events) = 
        (for {
          state1 <- TestAr.fromScratch(arId, "a")
          state2 <- state1.changeB(Some("b"))
          state3 <- state2.delete
        } yield state3).result.forceResult
      inCellWithEventLog {
        case (cell, eventlog) =>
          Await.result((eventlog ? DomainEventLog.CommitDomainEvents(events))(defaultTimeout), defaultTimeout) 
          val resF = (cell ? GetAggregateRoot)(defaultTimeout).mapTo[RequestedAggregateRoot]
          val res = Await.result(resF, defaultTimeout)
          res should equal(AggregateRootWasDeleted(arId))
      }
    }
  }
}