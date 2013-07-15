package almhirt.domain.impl

import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers
import akka.testkit._
import scala.concurrent.duration._
import scalaz._
import akka.actor._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.core.Almhirt
import almhirt.domaineventlog.impl.InMemoryDomainEventLog
import almhirt.domain._

class AggregateRootCellImplSpecs extends TestKit(ActorSystem("AggregateRootCellImplSpecs")) with FunSpec with ShouldMatchers {
  val almhirtAndHandle = Almhirt(this.system).awaitResult(FiniteDuration(5, "s")).forceResult

  implicit val theAlmhirt = almhirtAndHandle._1

  val uniqueId = new java.util.concurrent.atomic.AtomicInteger(1)
  
  val arId = theAlmhirt.getUuid
  
  def createCellAndEventLog(nameSuffix: String): (ActorRef, ActorRef, () => Unit) = {
    val eventLog = this.system.actorOf(Props(new InMemoryDomainEventLog with Actor { override def receive: Actor.Receive = receiveDomainEventLogMsg }), "EventLog_" + nameSuffix)
    val cell = this.system.actorOf(Props(new AggregateRootCellImpl with Actor {
      type Event = TestArEvent
      type AR = TestAr
      val publisher = theAlmhirt.messageBus
      val managedAggregateRooId = arId
      def rebuildAggregateRoot(events: Iterable[TestArEvent]) = TestAr.rebuildFromHistory(events)
      val theAlmhirt = AggregateRootCellImplSpecs.this.theAlmhirt
      val domainEventLog = eventLog
      override def receive: Actor.Receive = receiveAggregateRootCellMsg
    }), "ArCell_" + nameSuffix)
    (cell, eventLog, () => {cell ! PoisonPill; eventLog ! PoisonPill})
  }
  
  def inCellWithEventLog[T](f: (ActorRef, ActorRef) => T): T = {
    val (cell, eventLog, close) = createCellAndEventLog(uniqueId.getAndIncrement().toString)
    val res = f(cell, eventLog)
    close()
    res
  }
  
  describe("An AggregateRootCellImpl") {
    it("should be creatable") {
      inCellWithEventLog{ case (cell, eventlog) =>
        true should be(true)
      }
    }
  }
}