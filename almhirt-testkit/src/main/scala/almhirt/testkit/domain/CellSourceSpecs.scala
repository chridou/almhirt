package almhirt.testkit.domain

import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers
import akka.actor._
import akka.pattern._
import akka.util.Timeout
import almhirt.testkit._
import akka.testkit.TestProbe
import almhirt.domain._
import scala.concurrent._
import almhirt.almvalidation.kit._
import almhirt.core.HasAlmhirt

abstract class CellSourceSpecs(theActorSystem: ActorSystem) extends AlmhirtTestKit(theActorSystem) with HasAlmhirt with FunSpec with ShouldMatchers { self: CreatesEventLog =>
  import almhirt.domain.DomainMessages._
  import almhirt.domain.AggregateRootCell._
  import almhirt.domaineventlog.DomainEventLog._
  
  lazy val managedAggregateRootId = theAlmhirt.getUuid
  
  def createCellForAR1(testId: Int, managedAggregateRootId: java.util.UUID, eventLog: ActorRef): ActorRef

  // cell, eventlog
  def useCellWithEventLog[T](f: (ActorRef, ActorRef) => T): T = {
    val testId = nextTestId
    val eventlog = this.createEventLog(testId)
    val cell = createCellForAR1(testId, managedAggregateRootId, eventlog)
    val close = () => { system.stop(cell); system.stop(eventlog) }
    try {
      val res = f(cell, eventlog)
      close()
      res
    } catch {
      case exn: Exception =>
        close()
        throw exn
    }
  }

  // cell, a spy on the eventlog
  def useCellWithSpy[T](f: (ActorRef, TestProbe) => T): T = {
    val testId = nextTestId
    val eventlog = TestProbe()
    val cell = createCellForAR1(testId, managedAggregateRootId, eventlog.ref)
    val close = () => { system.stop(cell); system.stop(eventlog.ref) }
    try {
      val res = f(cell, eventlog)
      close()
      res
    } catch {
      case exn: Exception =>
        close()
        throw exn
    }
  }

  describe("An AggregateRootCell interacting with an eventlog") {
    it("should be creatable") {
      useCellWithEventLog {
        case (cell, eventlog) =>
          true should be(true)
      }
    }

    it("should answer with a AggregateRootNotFound when the aggregate root does not exist") {
      useCellWithEventLog {
        case (cell, eventlog) =>
          val resF = (cell ? GetManagedAggregateRoot)(defaultDuration).mapTo[DomainMessage]
          val res = Await.result(resF, defaultDuration)
          res should equal(AggregateRootNotFound(managedAggregateRootId))
      }
    }

    it("should answer with an AggregateRoot when the aggregate root does exist") {
      val (ar, events) =
        (for {
          state1 <- AR1.fromScratch(managedAggregateRootId, "a")
          state2 <- state1.changeB(Some("b"))
        } yield state2).result.forceResult
      useCellWithEventLog {
        case (cell, eventlog) =>
          Await.result((eventlog ? CommitDomainEvents(events))(defaultDuration), defaultDuration)
          val resF = (cell ? GetManagedAggregateRoot)(defaultDuration)
          val res = Await.result(resF, defaultDuration)
          res should equal(RequestedAggregateRoot(ar))
      }
    }

    it("should answer with an AggregateRootWasDeleted when the aggregate root is marked as deleted") {
      val (ar, events) =
        (for {
          state1 <- AR1.fromScratch(managedAggregateRootId, "a")
          state2 <- state1.changeB(Some("b"))
          state3 <- state2.delete
        } yield state3).result.forceResult
      useCellWithEventLog {
        case (cell, eventlog) =>
          Await.result((eventlog ? CommitDomainEvents(events))(defaultDuration), defaultDuration)
          val resF = (cell ? GetManagedAggregateRoot)(defaultDuration)
          val res = Await.result(resF, defaultDuration)
          res should equal(AggregateRootWasDeleted(managedAggregateRootId))
      }
    }

    it("should create a new aggregate root") {
      val (ar, events) =
        (for {
          state1 <- AR1.fromScratch(managedAggregateRootId, "a")
          state2 <- state1.changeB(Some("b"))
        } yield state2).result.forceResult
      useCellWithEventLog {
        case (cell, eventlog) =>
          val updF = (cell ? UpdateAggregateRoot(ar, events))(defaultDuration)
          Await.result(updF, defaultDuration) should equal(AggregateRootUpdated(ar))
      }
    }

    it("should update an existing aggreagate root") {
      val initialStateRecorder =
        (for {
          state1 <- AR1.fromScratch(managedAggregateRootId, "a")
          state2 <- state1.changeB(Some("b"))
        } yield state2)
      val (ar, events) =
        (for {
          initialState <- initialStateRecorder
          endState <- initialState.changeA("aaa")
        } yield endState).result.forceResult

      useCellWithEventLog {
        case (cell, eventlog) =>
          Await.result((eventlog ? CommitDomainEvents(events.take(2)))(defaultDuration), defaultDuration)
          val updF = (cell ? UpdateAggregateRoot(ar, Vector(events.last)))(defaultDuration)
          Await.result(updF, defaultDuration)
          val resF = (cell ? GetManagedAggregateRoot)(defaultDuration)
          val res = Await.result(resF, defaultDuration)
          res should equal(RequestedAggregateRoot(ar))
      }
    }

    it("create an aggregate root and then update it") {
      val (initialAr, initialEvents) =
        (for {
          state1 <- AR1.fromScratch(managedAggregateRootId, "a")
          state2 <- state1.changeB(Some("b"))
        } yield state2).result.forceResult
      val (resultAr, resultEvents) =
        (for {
          endState <- initialAr.changeA("aaa")
        } yield endState).result.forceResult

      useCellWithEventLog {
        case (cell, eventlog) =>
          val upd1F = (cell ? UpdateAggregateRoot(initialAr, initialEvents))(defaultDuration)
          val upd2F = (cell ? UpdateAggregateRoot(resultAr, resultEvents))(defaultDuration)
          Await.result(upd2F, defaultDuration)
          val resF = (cell ? GetManagedAggregateRoot)(defaultDuration)
          val res = Await.result(resF, defaultDuration)
          res should equal(RequestedAggregateRoot(resultAr))
      }
    }

    it("should fail on updating a non existing aggregate root with a AggregateRootUpdateFailed") {
      val (ar, events) =
        (for {
          state1 <- AR1.fromScratch(managedAggregateRootId, "a")
          state2 <- state1.changeB(Some("b"))
        } yield state2).result.forceResult
      useCellWithEventLog {
        case (cell, eventlog) =>
          val updF = (cell ? UpdateAggregateRoot(ar, Vector(events.last)))(defaultDuration)
          Await.result(updF, defaultDuration).isInstanceOf[AggregateRootUpdateFailed] should be(true)
      }
    }

    it("should fail on creating an aggregate root a second time with an AggregateRootUpdateFailed") {
      val (ar, events) =
        (for {
          state1 <- AR1.fromScratch(managedAggregateRootId, "a")
          state2 <- state1.changeB(Some("b"))
        } yield state2).result.forceResult
      useCellWithEventLog {
        case (cell, eventlog) =>
          (cell ? UpdateAggregateRoot(ar, events))(defaultDuration)
          val updF = (cell ? UpdateAggregateRoot(ar, events))(defaultDuration)
          val res = Await.result(updF, defaultDuration)
          res.isInstanceOf[AggregateRootUpdateFailed] should be(true)
      }
    }

    it("should fail on trying to create an aggragate root that has an id other than the cells managedId with an AggregateRootUpdateFailed") {
      val (ar, events) =
        (for {
          state1 <- AR1.fromScratch(theAlmhirt.getUuid, "a")
          state2 <- state1.changeB(Some("b"))
        } yield state2).result.forceResult
      useCellWithEventLog {
        case (cell, eventlog) =>
          val updF = (cell ? UpdateAggregateRoot(ar, events))(defaultDuration)
          val res = Await.result(updF, defaultDuration)
          res.isInstanceOf[AggregateRootUpdateFailed] should be(true)
      }
    }

    it("should fail on trying to create an aggragate root that has an event with a differrent aggregate root id than the cells managedId with an AggregateRootUpdateFailed") {
      val (ar, events) =
        (for {
          state1 <- AR1.fromScratch(managedAggregateRootId, "a")
          state2 <- state1.changeB(Some("b"))
        } yield state2).result.forceResult
      useCellWithEventLog {
        case (cell, eventlog) =>
          val eventsInit = events.init
          val lastEvent = events.last.asInstanceOf[AR1BChanged]
          val lastEventHeader = lastEvent.header
          val newWrongLastEvent = AR1BChanged(DomainEventHeader(AggregateRootRef(theAlmhirt.getUuid, lastEventHeader.aggRef.version)), lastEvent.newB)
          val updF = (cell ? UpdateAggregateRoot(ar, eventsInit :+ newWrongLastEvent))(defaultDuration)
          val res = Await.result(updF, defaultDuration)
          res.isInstanceOf[AggregateRootUpdateFailed] should be(true)
      }
    }

    it("should fail on trying to create an aggragate root where the first events version is not 0L iwth an AggregateRootUpdateFailed") {
      val (ar, events) = AR1.fromScratch(managedAggregateRootId, "a").result.forceResult
      useCellWithEventLog {
        case (cell, eventlog) =>
          val eventsInit = events.init
          val lastEvent = events.last.asInstanceOf[AR1Created]
          val lastEventHeader = lastEvent.header
          val newWrongLastEvent = AR1Created(DomainEventHeader(AggregateRootRef(managedAggregateRootId, 1L)), lastEvent.newA)
          val updF = (cell ? UpdateAggregateRoot(ar, eventsInit :+ newWrongLastEvent))(defaultDuration)
          val res = Await.result(updF, defaultDuration)
          res.isInstanceOf[AggregateRootUpdateFailed] should be(true)
      }
    }

    it("should fail on trying to create an aggragate root from events where at least one has an invalid version iwth an AggregateRootUpdateFailed") {
      val (ar, events) =
        (for {
          state1 <- AR1.fromScratch(managedAggregateRootId, "a")
          state2 <- state1.changeB(Some("b"))
        } yield state2).result.forceResult
      useCellWithEventLog {
        case (cell, eventlog) =>
          val eventsInit = events.init
          val lastEvent = events.last.asInstanceOf[AR1BChanged]
          val lastEventHeader = lastEvent.header
          val newWrongLastEvent = AR1BChanged(DomainEventHeader(lastEventHeader.aggRef.inc), lastEvent.newB)
          val updF = (cell ? UpdateAggregateRoot(ar, eventsInit :+ newWrongLastEvent))(defaultDuration)
          val res = Await.result(updF, defaultDuration)
          res.isInstanceOf[AggregateRootUpdateFailed] should be(true)
      }
    }

    ignore("CHECK ALL CONDITIONS FOR MUTATING!") {

    }
  }
}