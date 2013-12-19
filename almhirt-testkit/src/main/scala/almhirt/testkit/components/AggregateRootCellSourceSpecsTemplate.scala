package almhirt.testkit.components

import org.scalatest._
import akka.testkit.TestProbe
import almhirt.testkit._
import scala.concurrent.Future
import akka.actor._
import akka.pattern._
import akka.util.Timeout
import almhirt.almvalidation.kit._
import almhirt.almfuture.all._
import almhirt.core._

abstract class AggregateRootCellSourceSpecsTemplate(theActorSystem: ActorSystem)
  extends AlmhirtTestKit(theActorSystem)
  with HasAlmhirt
  with CreatesCellSourceForTestAggregateRoots
  with AggregateRootCellSourceSpecsOpsWithEventLog
  with FunSpecLike
  with Matchers { self: CreatesCellSource with CreatesDomainEventLog =>
  import almhirt.components.AggregateRootCellSource._
  import almhirt.domain.DomainMessages._
  import almhirt.domain.AggregateRootCell._

  implicit def execContext = theAlmhirt.futuresExecutor

  describe("AggregateRootCellSource") {
    it("should return an aggregate root cell for an aggregate root of type A") {
      useCellSourceWithEventLog { (source, eventLog) =>
        val workflowF =
          (source ? GetCell(theAlmhirt.getUuid, classOf[AR1]))(defaultDuration).successfulAlmFuture[AggregateRootCellSourceResult]
        val res = workflowF.awaitResult(defaultDuration)
        res.isSuccess should be(true)
      }
    }

    it("should return an aggregate root cell for an aggregate root of type B") {
      useCellSourceWithEventLog { (source, eventLog) =>
        val workflowF =
          (source ? GetCell(theAlmhirt.getUuid, classOf[AR2]))(defaultDuration).successfulAlmFuture[AggregateRootCellSourceResult]
        val res = workflowF.awaitResult(defaultDuration)
        res.isSuccess should be(true)
      }
    }

    it("should allow updating an aggregate root of type A via the given cell") {
      val (ar, events) = AR1.fromScratch(theAlmhirt.getUuid, "a").result.forceResult
      useCellSourceWithEventLog { (source, eventLog) =>
        val workflowF =
          for {
            cellResult1 <- (source ? GetCell(ar.id, classOf[AR1]))(defaultDuration).successfulAlmFuture[AggregateRootCellSourceResult]
            updateRes <- cellResult1.cellHandle.onceWithCell { cell =>
              (cell ? UpdateAggregateRoot(ar, events))(defaultDuration).successfulAlmFuture[Any]
            }
          } yield updateRes
        val res = workflowF.awaitResultOrEscalate(defaultDuration)

        res should equal(AggregateRootUpdated(ar))
      }
    }

    it("should allow updating an aggregate root of type B via the given cell") {
      val (ar, events) = AR2.fromScratch(theAlmhirt.getUuid).result.forceResult
      useCellSourceWithEventLog { (source, eventLog) =>
        val workflowF =
          for {
            cellResult1 <- (source ? GetCell(ar.id, classOf[AR2]))(defaultDuration).successfulAlmFuture[AggregateRootCellSourceResult]
            updateRes <- cellResult1.cellHandle.onceWithCell { cell =>
              (cell ? UpdateAggregateRoot(ar, events))(defaultDuration).successfulAlmFuture[Any]
            }
          } yield updateRes
        val res = workflowF.awaitResult(defaultDuration)

        res should equal(scalaz.Success(AggregateRootUpdated(ar)))
      }
    }

    it("should take an aggregate root of type A and give it back") {
      val (ar, events) = AR1.fromScratch(theAlmhirt.getUuid, "a").result.forceResult
      useCellSourceWithEventLog { (source, eventLog) =>
        val workflowF =
          for {
            cellResult1 <- (source ? GetCell(ar.id, classOf[AR1]))(defaultDuration).successfulAlmFuture[AggregateRootCellSourceResult].mapTimeoutMessage(m => s"A: $m")
            _ <- cellResult1.cellHandle.onceWithCell { cell =>
              (cell ? UpdateAggregateRoot(ar, events))(defaultDuration).successfulAlmFuture[Any].mapTimeoutMessage(m => s"B: $m")
            }
            cellResult2 <- (source ? GetCell(ar.id, classOf[AR1]))(defaultDuration).successfulAlmFuture[AggregateRootCellSourceResult].mapTimeoutMessage(m => s"C: $m")
            getResult <- cellResult2.cellHandle.onceWithCell { cell =>
              (cell ? GetManagedAggregateRoot)(defaultDuration).successfulAlmFuture[Any].mapTimeoutMessage(m => s"D: $m")
            }
          } yield getResult
        val res = workflowF.mapTimeoutMessage(m => s"E: $m").awaitResult(defaultDuration)

        res should equal(scalaz.Success(RequestedAggregateRoot(ar)))
      }
    }

    it("should take an aggregate root of type B and give it back") {
      val (ar, events) = AR2.fromScratch(theAlmhirt.getUuid).result.forceResult
      useCellSourceWithEventLog { (source, eventLog) =>
        val workflowF =
          for {
            cellResult1 <- (source ? GetCell(ar.id, classOf[AR2]))(defaultDuration).successfulAlmFuture[AggregateRootCellSourceResult]
            _ <- cellResult1.cellHandle.onceWithCell { cell =>
              (cell ? UpdateAggregateRoot(ar, events))(defaultDuration).successfulAlmFuture[Any]
            }
            cellResult2 <- (source ? GetCell(ar.id, classOf[AR2]))(defaultDuration).successfulAlmFuture[AggregateRootCellSourceResult]
            getResult <- cellResult2.cellHandle.onceWithCell { cell =>
              (cell ? GetManagedAggregateRoot)(defaultDuration).successfulAlmFuture[Any]
            }
          } yield getResult
        val res = workflowF.awaitResult(defaultDuration)

        res should equal(scalaz.Success(RequestedAggregateRoot(ar)))
      }
    }

    it("should take an aggregate root of type A and B and give both back(sequenced GetCell->Action pairs)") {
      val (arA, eventsA) = AR1.fromScratch(theAlmhirt.getUuid, "a").result.forceResult
      val (arB, eventsB) = AR2.fromScratch(theAlmhirt.getUuid).result.forceResult
      useCellSourceWithEventLog { (source, eventLog) =>
        val workflowF =
          for {
            cellResult1 <- (source ? GetCell(arA.id, classOf[AR1]))(defaultDuration).successfulAlmFuture[AggregateRootCellSourceResult]
            _ <- cellResult1.cellHandle.onceWithCell { cell =>
              (cell ? UpdateAggregateRoot(arA, eventsA))(defaultDuration).successfulAlmFuture[Any]
            }
            cellResult2 <- (source ? GetCell(arB.id, classOf[AR2]))(defaultDuration).successfulAlmFuture[AggregateRootCellSourceResult]
            _ <- cellResult2.cellHandle.onceWithCell { cell =>
              (cell ? UpdateAggregateRoot(arB, eventsB))(defaultDuration).successfulAlmFuture[Any]
            }
            cellResult3 <- (source ? GetCell(arA.id, classOf[AR1]))(defaultDuration).successfulAlmFuture[AggregateRootCellSourceResult]
            getResultA <- cellResult3.cellHandle.onceWithCell { cell =>
              (cell ? GetManagedAggregateRoot)(defaultDuration).successfulAlmFuture[Any]
            }
            cellResult4 <- (source ? GetCell(arB.id, classOf[AR2]))(defaultDuration).successfulAlmFuture[AggregateRootCellSourceResult]
            getResultB <- cellResult4.cellHandle.onceWithCell { cell =>
              (cell ? GetManagedAggregateRoot)(defaultDuration).successfulAlmFuture[Any]
            }
            sourceStats <- (source ? GetStats)(defaultDuration).successfulAlmFuture[AggregateRootCellSourceStats]
          } yield (getResultA, getResultB, sourceStats)
        val res = workflowF.awaitResult(defaultDuration).forceResult

        res._1 should equal((RequestedAggregateRoot(arA)))
        res._2 should equal((RequestedAggregateRoot(arB)))
      }
    }

    it("should take an aggregate root of type A and B and give both back(parallel)") {
      val (arA, eventsA) = AR1.fromScratch(theAlmhirt.getUuid, "a").result.forceResult
      val (arB, eventsB) = AR2.fromScratch(theAlmhirt.getUuid).result.forceResult
      useCellSourceWithEventLog { (source, eventLog) =>
        val arAWorkFlow =
          for {
            cellResult1 <- (source ? GetCell(arA.id, classOf[AR1]))(defaultDuration).successfulAlmFuture[AggregateRootCellSourceResult]
            _ <- cellResult1.cellHandle.onceWithCell { cell =>
              (cell ? UpdateAggregateRoot(arA, eventsA))(defaultDuration).successfulAlmFuture[Any]
            }
            cellResult2 <- (source ? GetCell(arA.id, classOf[AR1]))(defaultDuration).successfulAlmFuture[AggregateRootCellSourceResult]
            getResultA <- cellResult2.cellHandle.onceWithCell { cell =>
              (cell ? GetManagedAggregateRoot)(defaultDuration).successfulAlmFuture[Any]
            }
          } yield getResultA

        val arBWorkFlow =
          for {
            cellResult1 <- (source ? GetCell(arB.id, classOf[AR2]))(defaultDuration).successfulAlmFuture[AggregateRootCellSourceResult]
            _ <- cellResult1.cellHandle.onceWithCell { cell =>
              (cell ? UpdateAggregateRoot(arB, eventsB))(defaultDuration).successfulAlmFuture[Any]
            }
            cellResult2 <- (source ? GetCell(arB.id, classOf[AR2]))(defaultDuration).successfulAlmFuture[AggregateRootCellSourceResult]
            getResultB <- cellResult2.cellHandle.onceWithCell { cell =>
              (cell ? GetManagedAggregateRoot)(defaultDuration).successfulAlmFuture[Any]
            }
          } yield getResultB

        val res = (arAWorkFlow.flatMap(resA => arBWorkFlow.map((resA, _)))).awaitResult(defaultDuration).forceResult
        res._1 should equal((RequestedAggregateRoot(arA)))
        res._2 should equal((RequestedAggregateRoot(arB)))
      }
    }

    ignore("should should remove all cells that report that they do not exist(which they do when queried)") {
//      useCellSourceWithEventLog { (source, eventLog) =>
//        val queriesF = for (x <- 1 to 100) yield {
//          (source ? GetCell(theAlmhirt.getUuid, classOf[AR2]))(defaultDuration).mapTo[AggregateRootCellSourceResult].flatMap(x => x.cellHandle.onceWithCell { cell =>
//            (cell ? GetManagedAggregateRoot)(defaultDuration).successfulAlmFuture[Any]
//          }.underlying)
//        }
//        Future.sequence(queriesF).successfulAlmFuture[AggregateRootCellSourceStats].awaitResult(defaultDuration)
//        val stats = (source ? GetStats)(defaultDuration).successfulAlmFuture[AggregateRootCellSourceStats].awaitResult(defaultDuration).forceResult
//        stats should equal(AggregateRootCellSourceStats(0, 0, 0))
//      }
    }
  }
}


