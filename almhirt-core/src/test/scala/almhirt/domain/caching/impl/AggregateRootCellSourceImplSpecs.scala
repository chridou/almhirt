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
import scala.concurrent.Future

class AggregateRootCellSourceSpecs extends TestKit(ActorSystem("AggregateRootCellSourceSpecsSystem", TestConfigs.default)) with FunSpec with ShouldMatchers {
  import almhirt.domain.caching.AggregateRootCellSource._
  import almhirt.domain.DomainMessages._
  import almhirt.domain.AggregateRootCell._

  val almhirtAndHandle = Almhirt.notFromConfig(this.system).awaitResult(FiniteDuration(5, "s")).forceResult

  implicit val theAlmhirt = almhirtAndHandle._1
  implicit val defaultWaitDuration = theAlmhirt.durations.shortDuration
  implicit val futuresContext = theAlmhirt.futuresExecutor

  val nextSpecId = new java.util.concurrent.atomic.AtomicInteger(1)

  def createEventLog(specId: Int): ActorRef =
    this.system.actorOf(Props(new InMemoryDomainEventLog with Actor { override def receive: Actor.Receive = receiveDomainEventLogMsg }), "EventLog_" + specId.toString)

  def createCellSource(specId: Int, eventLog: ActorRef): ActorRef = {
    val propsFactories: Map[Class[_], (JUUID, () => Unit) => Props] =
      Map(
        (classOf[TestAr], (arId: JUUID, notifyDoesNotExist: () => Unit) => Props(new AggregateRootCellImpl[TestAr, TestArEvent](arId, TestAr.rebuildFromHistory, eventLog, notifyDoesNotExist))),
        (classOf[AnotherTestAr], (arId: JUUID, notifyDoesNotExist: () => Unit) => Props(new AggregateRootCellImpl[AnotherTestAr, AnotherTestArEvent](arId, AnotherTestAr.rebuildFromHistory, eventLog, notifyDoesNotExist))))
    val props = Props(new AggregateRootCellSourceImpl(propsFactories.lift))
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

  describe("AggregateRootCellSource") {
    it("should return an aggregate root cell for an aggregate root of type A") {
      withTestRig { (source, eventLog) =>
        val workflowF =
          (source ? GetCell(theAlmhirt.getUuid, classOf[TestAr]))(defaultWaitDuration).successfulAlmFuture[AggregateRootCellSourceResult]
        val res = workflowF.awaitResult(defaultWaitDuration)
        res.isSuccess should be(true)
      }
    }

    it("should return an aggregate root cell for an aggregate root of type B") {
      withTestRig { (source, eventLog) =>
        val workflowF =
          (source ? GetCell(theAlmhirt.getUuid, classOf[AnotherTestAr]))(defaultWaitDuration).successfulAlmFuture[AggregateRootCellSourceResult]
        val res = workflowF.awaitResult(defaultWaitDuration)
        res.isSuccess should be(true)
      }
    }

    it("should allow updating an aggregate root of type A via the given cell") {
      val (ar, events) = TestAr.fromScratch(theAlmhirt.getUuid, "a").result.forceResult
      withTestRig { (source, eventLog) =>
        val workflowF =
          for {
            cellResult1 <- (source ? GetCell(ar.id, classOf[TestAr]))(defaultWaitDuration).successfulAlmFuture[AggregateRootCellSourceResult]
            updateRes <- cellResult1.cellHandle.onceWithCell { cell =>
              (cell ? UpdateAggregateRoot(ar, events))(defaultWaitDuration).successfulAlmFuture[Any]
            }
          } yield updateRes
        val res = workflowF.awaitResult(defaultWaitDuration)

        res should equal(scalaz.Success(AggregateRootUpdated(ar)))
      }
    }

    it("should allow updating an aggregate root of type B via the given cell") {
      val (ar, events) = AnotherTestAr.fromScratch(theAlmhirt.getUuid).result.forceResult
      withTestRig { (source, eventLog) =>
        val workflowF =
          for {
            cellResult1 <- (source ? GetCell(ar.id, classOf[AnotherTestAr]))(defaultWaitDuration).successfulAlmFuture[AggregateRootCellSourceResult]
            updateRes <- cellResult1.cellHandle.onceWithCell { cell =>
              (cell ? UpdateAggregateRoot(ar, events))(defaultWaitDuration).successfulAlmFuture[Any]
            }
          } yield updateRes
        val res = workflowF.awaitResult(defaultWaitDuration)

        res should equal(scalaz.Success(AggregateRootUpdated(ar)))
      }
    }

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
              (cell ? GetManagedAggregateRoot)(defaultWaitDuration).successfulAlmFuture[Any]
            }
          } yield getResult
        val res = workflowF.awaitResult(defaultWaitDuration)

        res should equal(scalaz.Success(RequestedAggregateRoot(ar)))
      }
    }
    
    it("should take an aggregate root of type B and give it back") {
      val (ar, events) = AnotherTestAr.fromScratch(theAlmhirt.getUuid).result.forceResult
      withTestRig { (source, eventLog) =>
        val workflowF =
          for {
            cellResult1 <- (source ? GetCell(ar.id, classOf[AnotherTestAr]))(defaultWaitDuration).successfulAlmFuture[AggregateRootCellSourceResult]
            _ <- cellResult1.cellHandle.onceWithCell { cell =>
              (cell ? UpdateAggregateRoot(ar, events))(defaultWaitDuration).successfulAlmFuture[Any]
            }
            cellResult2 <- (source ? GetCell(ar.id, classOf[AnotherTestAr]))(defaultWaitDuration).successfulAlmFuture[AggregateRootCellSourceResult]
            getResult <- cellResult2.cellHandle.onceWithCell { cell =>
              (cell ? GetManagedAggregateRoot)(defaultWaitDuration).successfulAlmFuture[Any]
            }
          } yield getResult
        val res = workflowF.awaitResult(defaultWaitDuration)

        res should equal(scalaz.Success(RequestedAggregateRoot(ar)))
      }
    }
    
    it("should take an aggregate root of type A and B and give both back(sequenced GetCell->Action pairs)") {
      val (arA, eventsA) = TestAr.fromScratch(theAlmhirt.getUuid, "a").result.forceResult
      val (arB, eventsB) = AnotherTestAr.fromScratch(theAlmhirt.getUuid).result.forceResult
      withTestRig { (source, eventLog) =>
        val workflowF =
          for {
            cellResult1 <- (source ? GetCell(arA.id, classOf[TestAr]))(defaultWaitDuration).successfulAlmFuture[AggregateRootCellSourceResult]
            _ <- cellResult1.cellHandle.onceWithCell { cell =>
              (cell ? UpdateAggregateRoot(arA, eventsA))(defaultWaitDuration).successfulAlmFuture[Any]
            }
            cellResult2 <- (source ? GetCell(arB.id, classOf[AnotherTestAr]))(defaultWaitDuration).successfulAlmFuture[AggregateRootCellSourceResult]
            _ <- cellResult2.cellHandle.onceWithCell { cell =>
              (cell ? UpdateAggregateRoot(arB, eventsB))(defaultWaitDuration).successfulAlmFuture[Any]
            }
            cellResult3 <- (source ? GetCell(arA.id, classOf[TestAr]))(defaultWaitDuration).successfulAlmFuture[AggregateRootCellSourceResult]
            getResultA <- cellResult3.cellHandle.onceWithCell { cell =>
              (cell ? GetManagedAggregateRoot)(defaultWaitDuration).successfulAlmFuture[Any]
            }
            cellResult4 <- (source ? GetCell(arB.id, classOf[AnotherTestAr]))(defaultWaitDuration).successfulAlmFuture[AggregateRootCellSourceResult]
            getResultB <- cellResult4.cellHandle.onceWithCell { cell =>
              (cell ? GetManagedAggregateRoot)(defaultWaitDuration).successfulAlmFuture[Any]
            }
            sourceStats <- (source ? GetStats)(defaultWaitDuration).successfulAlmFuture[AggregateRootCellSourceStats]
          } yield (getResultA, getResultB, sourceStats)
        val res = workflowF.awaitResult(defaultWaitDuration).forceResult

        res._1 should equal((RequestedAggregateRoot(arA)))
        res._2 should equal((RequestedAggregateRoot(arB)))
      }
    }
    
    it("should take an aggregate root of type A and B and give both back(parallel)") {
      val (arA, eventsA) = TestAr.fromScratch(theAlmhirt.getUuid, "a").result.forceResult
      val (arB, eventsB) = AnotherTestAr.fromScratch(theAlmhirt.getUuid).result.forceResult
      withTestRig { (source, eventLog) =>
        val arAWorkFlow = 
          for {
             cellResult1 <- (source ? GetCell(arA.id, classOf[TestAr]))(defaultWaitDuration).successfulAlmFuture[AggregateRootCellSourceResult]
            _ <- cellResult1.cellHandle.onceWithCell { cell =>
              (cell ? UpdateAggregateRoot(arA, eventsA))(defaultWaitDuration).successfulAlmFuture[Any]
            }
            cellResult2 <- (source ? GetCell(arA.id, classOf[TestAr]))(defaultWaitDuration).successfulAlmFuture[AggregateRootCellSourceResult]
            getResultA <- cellResult2.cellHandle.onceWithCell { cell =>
              (cell ? GetManagedAggregateRoot)(defaultWaitDuration).successfulAlmFuture[Any]
            }
         } yield getResultA

        val arBWorkFlow = 
          for {
             cellResult1 <- (source ? GetCell(arB.id, classOf[AnotherTestAr]))(defaultWaitDuration).successfulAlmFuture[AggregateRootCellSourceResult]
            _ <- cellResult1.cellHandle.onceWithCell { cell =>
              (cell ? UpdateAggregateRoot(arB, eventsB))(defaultWaitDuration).successfulAlmFuture[Any]
            }
            cellResult2 <- (source ? GetCell(arB.id, classOf[AnotherTestAr]))(defaultWaitDuration).successfulAlmFuture[AggregateRootCellSourceResult]
            getResultB <- cellResult2.cellHandle.onceWithCell { cell =>
              (cell ? GetManagedAggregateRoot)(defaultWaitDuration).successfulAlmFuture[Any]
            }
         } yield getResultB
        
        val res = (arAWorkFlow.flatMap(resA => arBWorkFlow.map((resA, _)))).awaitResult(defaultWaitDuration).forceResult 
        res._1 should equal((RequestedAggregateRoot(arA)))
        res._2 should equal((RequestedAggregateRoot(arB)))
      }
    }

    ignore("should should remove all cells that report that they do not exist(which they do when queried)") {
      withTestRig { (source, eventLog) =>
        val queriesF = for(x <- 1 to 50 ) yield {
          (source ? GetCell(theAlmhirt.getUuid, classOf[AnotherTestAr]))(defaultWaitDuration).mapTo[AggregateRootCellSourceResult].flatMap(x => x.cellHandle.onceWithCell { cell =>
              (cell ? GetManagedAggregateRoot)(defaultWaitDuration).successfulAlmFuture[Any]}.underlying)
        }
        val resF = 
          Future.sequence(queriesF).flatMap(_ => (source ? GetStats)(defaultWaitDuration)).successfulAlmFuture[AggregateRootCellSourceStats]
        val res = resF.awaitResult(defaultWaitDuration).forceResult
        res should equal (AggregateRootCellSourceStats(0,0,0))
      }
    }
  }
}