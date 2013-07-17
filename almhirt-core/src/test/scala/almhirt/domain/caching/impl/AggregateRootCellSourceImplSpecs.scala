package almhirt.domain.caching.impl

import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers
import akka.testkit._
import almhirt.testing.TestConfigs
import java.util.{ UUID => JUUID }
import scala.concurrent.duration._
import scala.concurrent.Await
import scalaz._
import akka.actor._
import akka.pattern._
import akka.util.Timeout
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.almfuture.all._
import almhirt.core.Almhirt
import almhirt.domaineventlog.impl.InMemoryDomainEventLog
import almhirt.domain._
import almhirt.domain.impl.AggregateRootCellImpl

class AggregateRootCellSourceSpecs extends TestKit(ActorSystem("AggregateRootCellSourceSpecsSystem", TestConfigs.default)) with FunSpec with ShouldMatchers {
  import almhirt.domain.caching.AggregateRootCellSource._
  import almhirt.domain.DomainMessages._

  val almhirtAndHandle = Almhirt.notFromConfig(this.system).awaitResult(FiniteDuration(5, "s")).forceResult

  implicit val theAlmhirt = almhirtAndHandle._1
  implicit val defaultWaitDuration = theAlmhirt.durations.shortDuration
  implicit val futuresContext = theAlmhirt.futuresExecutor

  val nextSpecId = new java.util.concurrent.atomic.AtomicInteger(1)

  def createEventLog(specId: Int): ActorRef =
    this.system.actorOf(Props(new InMemoryDomainEventLog with Actor { override def receive: Actor.Receive = receiveDomainEventLogMsg }), "EventLog_" + specId.toString)

  def createCellSource(specId: Int, eventLog: ActorRef): ActorRef = {
    val props = Props(
      new AggregateRootCellSourceImpl with Actor with ActorLogging {
        override def createProps(aggregateRootId: JUUID, forArType: Class[_], notifiyOnDoesNotExist: () => Unit): Props =
          if (forArType == classOf[TestAr]) {
            Props(new AggregateRootCellImpl with Actor with ActorLogging {
              type Event = TestArEvent
              type AR = TestAr
              def onDoesNotExist() = notifiyOnDoesNotExist()
              def publisher = theAlmhirt.messageBus
              val managedAggregateRooId = aggregateRootId
              def rebuildAggregateRoot(events: Iterable[TestArEvent]) = TestAr.rebuildFromHistory(events)
              val theAlmhirt = AggregateRootCellSourceSpecs.this.theAlmhirt
              val domainEventLog = eventLog
              override def receive: Actor.Receive = receiveAggregateRootCellMsg
            })
          } else if (forArType == classOf[TestAr2]) {
            Props(new AggregateRootCellImpl with Actor with ActorLogging {
              type Event = TestAr2Event
              type AR = TestAr2
              def onDoesNotExist() = notifiyOnDoesNotExist()
              def publisher = theAlmhirt.messageBus
              val managedAggregateRooId = aggregateRootId
              def rebuildAggregateRoot(events: Iterable[TestAr2Event]) = TestAr2.rebuildFromHistory(events)
              val theAlmhirt = AggregateRootCellSourceSpecs.this.theAlmhirt
              val domainEventLog = eventLog
              override def receive: Actor.Receive = receiveAggregateRootCellMsg
            })
          } else {
            throw new Exception("unsupported aggregate root type for testing")
          }
        override def receive: Receive = receiveAggregateRootCellSourceMessage
      })
    this.system.actorOf(props, "CellSource_" + specId.toString())
  }

  def createTestRig(): (ActorRef, ActorRef, () => Unit) = {
    val specId = nextSpecId.getAndIncrement()
    val eventLog = createEventLog(specId)
    val cellSource = createCellSource(specId, eventLog)
    (cellSource, eventLog, () => { system.stop(cellSource); system.stop(eventLog) })
  }

  def withTestRig[T](f: (ActorRef, ActorRef) => T): T = {
    val (cellSource, eventLog, close) = createTestRig()
    try {
      val res = f(cellSource, eventLog)
      close()
      res
    } catch {
      case exn: Exception =>
        close()
        throw exn
    }
  }

  describe("" + "(sequenced tests)") {
//    it("should return an aggregate root cell for an aggregate root of type A") {
//      withTestRig { (source, eventLog) =>
//        val workflowF =
//          (source ? GetCell(theAlmhirt.getUuid, classOf[TestAr]))(defaultWaitDuration).successfulAlmFuture[AggregateRootCellSourceResult]
//        val res = workflowF.awaitResult(defaultWaitDuration)
//        res.isSuccess should be(true)
//      }
//    }
//    
//    it("should return an aggregate root cell for an aggregate root of type B") {
//      withTestRig { (source, eventLog) =>
//        val workflowF =
//          (source ? GetCell(theAlmhirt.getUuid, classOf[TestAr2]))(defaultWaitDuration).successfulAlmFuture[AggregateRootCellSourceResult]
//        val res = workflowF.awaitResult(defaultWaitDuration)
//        res.isSuccess should be(true)
//      }
//    }
//
//    it("should allow updating an aggregate root of type A via the given cell") {
//      val (ar, events) = TestAr.fromScratch(theAlmhirt.getUuid, "a").result.forceResult
//      withTestRig { (source, eventLog) =>
//        val workflowF =
//          for {
//            cellResult1 <- (source ? GetCell(ar.id, classOf[TestAr]))(defaultWaitDuration).successfulAlmFuture[AggregateRootCellSourceResult]
//            updateRes <- cellResult1.cellHandle.onceWithCell { cell =>
//              (cell ? UpdateAggregateRoot(ar, events))(defaultWaitDuration).successfulAlmFuture[Any]
//            }
//          } yield updateRes
//        val res = workflowF.awaitResult(defaultWaitDuration)
//
//        res should equal(scalaz.Success(AggregateRootUpdated(ar)))
//      }
//    }
//
//    it("should allow updating an aggregate root of type B via the given cell") {
//      val (ar, events) = TestAr2.fromScratch(theAlmhirt.getUuid).result.forceResult
//      withTestRig { (source, eventLog) =>
//        val workflowF =
//          for {
//            cellResult1 <- (source ? GetCell(ar.id, classOf[TestAr2]))(defaultWaitDuration).successfulAlmFuture[AggregateRootCellSourceResult]
//            updateRes <- cellResult1.cellHandle.onceWithCell { cell =>
//              (cell ? UpdateAggregateRoot(ar, events))(defaultWaitDuration).successfulAlmFuture[Any]
//            }
//          } yield updateRes
//        val res = workflowF.awaitResult(defaultWaitDuration)
//
//        res should equal(scalaz.Success(AggregateRootUpdated(ar)))
//      }
//    }
    
    it("should take an aggregate root of type A and give it back") {
      val (ar, events) = TestAr.fromScratch(theAlmhirt.getUuid, "a").result.forceResult
      withTestRig { (source, eventLog) =>
        val workflowF =
          for {
            cellResult1 <- (source ? GetCell(ar.id, classOf[TestAr]))(defaultWaitDuration).successfulAlmFuture[AggregateRootCellSourceResult]
            _ <- cellResult1.cellHandle.onceWithCell { cell =>
              (cell ? UpdateAggregateRoot(ar, events))(defaultWaitDuration).successfulAlmFuture[Any]
            }
            cellResult2 <- (source ? GetCell(ar.id, classOf[TestAr]))(defaultWaitDuration).successfulAlmFuture[AggregateRootCellSourceResult]
            getResult <- cellResult2.cellHandle.onceWithCell { cell =>
              (cell ? GetAggregateRoot(ar.id))(defaultWaitDuration).successfulAlmFuture[Any]
            }
          } yield getResult
        val res = workflowF.awaitResult(defaultWaitDuration)

        res should equal(scalaz.Success(RequestedAggregateRoot(ar)))
      }
    }
  }
}