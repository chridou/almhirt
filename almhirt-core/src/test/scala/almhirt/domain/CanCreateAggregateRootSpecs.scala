package almhirt.domain

import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers
import scalaz._
import almhirt.common._

class CanCreateAggregateRootSpecs extends FunSpec with ShouldMatchers {
  implicit val ccuad = CanCreateUuidsAndDateTimes()

  describe("CanCreateAggregateRoot") {
    it("should create a new AggregateRoot on a CreatesNewAggregateRootEvent on applyEvent") {
      val event = TestArCreated(ccuad.getUuid, "a")
      val resV = TestAr.applyEvent(event)
      resV should equal(Success(TestAr((event.aggId, 1L), "a", None, false)))
    }
    it("should fail with an UnhandledDomainEventException on a non CreatesNewAggregateRootEvent on applyEvent") {
      val event = AChanged(ccuad.getUuid, "a")
      intercept[UnhandledDomainEventException] {
        TestAr.applyEvent(event)
      }
    }
    it("should fail with an UnhandledDomainEventException on a DeletesAggregateRootEvent on applyEvent") {
      val event = TestArDeleted(ccuad.getUuid)
      intercept[UnhandledDomainEventException] {
        TestAr.applyEvent(event)
      }
    }
    it("should rebuild an aggregate root from a single CreatesNewAggregateRootEvent") {
      val event = TestArCreated(ccuad.getUuid, "a")
      val resV = TestAr.rebuildFromHistory(Iterable(event))
      resV should equal(Success(TestAr((event.aggId, 1L), "a", None, false)))
    }
    it("should not rebuild an aggregate root(fail with a UnhandledDomainEventException) from a single event which is not a CreatesNewAggregateRootEvent") {
      val event = AChanged(ccuad.getUuid, "a")
      intercept[UnhandledDomainEventException] {
        TestAr.rebuildFromHistory(Iterable(event))
      }
    }
    it("should not rebuild an aggregate root(fail with a UnhandledDomainEventException) from a single event which is a DeletesAggregateRootEvent") {
      val event = TestArDeleted(ccuad.getUuid)
      intercept[UnhandledDomainEventException] {
        TestAr.rebuildFromHistory(Iterable(event))
      }
    }
    it("should rebuild an aggregate root from 2 events starting with a CreatesNewAggregateRootEvent") {
      val event1 = TestArCreated(ccuad.getUuid, "a")
      val event2 = BChanged((event1.aggRef.inc), Some("b"))
      val resV = TestAr.rebuildFromHistory(Iterable(event1, event2))
      resV should equal(Success(TestAr((event1.aggId, 2L), "a", Some("b"), false)))
    }
    it("should rebuild an aggregate root from 3 events starting with a CreatesNewAggregateRootEvent and ending with a DeletesAggregateRootEvent") {
      val event1 = TestArCreated(ccuad.getUuid, "a")
      val event2 = BChanged((event1.aggRef.inc), Some("b"))
      val event3 = TestArDeleted((event2.aggRef.inc))
      val resV = TestAr.rebuildFromHistory(Iterable(event1, event2, event3))
      resV should equal(Success(TestAr((event1.aggId, 3L), "a", Some("b"), true)))
    }
    it("should not rebuild an aggregate root from 3 events: Create -> Delete -> Mutate(the result is a Failure(AggregateRootDeletedProblem))") {
      val event1 = TestArCreated(ccuad.getUuid, "a")
      val event2 = TestArDeleted((event1.aggRef.inc))
      val event3 = BChanged((event2.aggRef.inc), Some("b"))
      val resV = TestAr.rebuildFromHistory(Iterable(event1, event2, event3))
      resV should equal(Failure(AggregateRootDeletedProblem(event1.aggId)))
    }
  }

}