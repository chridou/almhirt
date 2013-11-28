package almhirt.domain

import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers
import scalaz._
import almhirt.common._
import almhirt.core.types._
import almhirt.almvalidation.kit._

class AggregateRootSpecs extends FunSpec with ShouldMatchers {
  implicit val ccuad = CanCreateUuidsAndDateTimes()

  val initialState = TestAr((ccuad.getUuid, 1L), "a", None, false)

  describe("An AggregateRoot") {
    it("should fail with an UnhandledDomainEventException on a CreatesNewAggregateRootEvent on applyEvent") {
      val event = TestArCreated(initialState.ref, "a")
      intercept[UnhandledDomainEventException] {
        initialState.applyEvent(event)
      }
    }
    it("should fail with an UnhandledDomainEventException on an event not in the handler") {
      val event = UnhandableTestArEvent(initialState.ref)
      intercept[UnhandledDomainEventException] {
        initialState.applyEvent(event)
      }
    }
    it("should handle a mutation on applyEvent") {
      val event = BChanged(initialState.ref, Some("b"))
      val resV = initialState.applyEvent(event)
      resV should equal(Success(TestAr((event.aggId, 2L), "a", Some("b"), false)))
    }
    it("should handle a DeletesAggregateRootEvent on applyEvent") {
      val event = TestArDeleted(initialState.ref)
      val resV = initialState.applyEvent(event)
      resV should equal(Success(TestAr((event.aggId, 2L), "a", None, true)))
    }
    it("should fail on a mutation event with a wrong id") {
      val event = BChanged(initialState.ref.copy(id = ccuad.getUuid), Some("b"))
      val resV = initialState.applyEvent(event)
      resV should equal(Failure(UnspecifiedProblem("Ids do not match!")))
    }
    it("should fail on a mutation event with a wrong version") {
      val event = BChanged(initialState.ref.inc, Some("b"))
      val resV = initialState.applyEvent(event)
      resV.isFailure should be(true)
      resV.forceProblem.problemType.isInstanceOf[CollisionProblem.type] should be(true)
    }

    it("should handle 2 mutations on applyEvents") {
      val event1 = BChanged(initialState.ref, Some("b"))
      val event2 = AChanged(event1.aggRef.inc, "aa")
      val resV = initialState.applyEvents(Iterable(event1, event2))
      resV should equal(Success(TestAr((event1.aggId, 3L), "aa", Some("b"), false)))
    }
    it("should handle a mutation and then a delete on applyEvents") {
      val event1 = BChanged(initialState.ref, Some("b"))
      val event2 = TestArDeleted(event1.aggRef.inc)
      val resV = initialState.applyEvents(Iterable(event1, event2))
      resV should equal(Success(TestAr((event1.aggId, 3L), "a", Some("b"), true)))
    }
    it("should fail on a delete and then a mutation on applyEvents") {
      val event1 = TestArDeleted(initialState.ref)
      val event2 = BChanged(event1.aggRef.inc, Some("b"))
      val resV = initialState.applyEvents(Iterable(event1, event2))
      resV should equal(Failure(AggregateRootDeletedProblem(initialState.id)))
    }
    it("should fail on a mutation and then an event not in the handler on applyEvents") {
      val event1 = BChanged(initialState.ref, Some("b"))
      val event2 = UnhandableTestArEvent(event1.aggRef.inc)
      intercept[UnhandledDomainEventException] {
        val res = initialState.applyEvents(Iterable(event1, event2))
        println(res)
      }
    }
  }

}