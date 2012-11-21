package almhirt.eventlog.anorm

import org.specs2.mutable._
import scalaz.syntax.validation._
import akka.util.Duration
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.environment._
import almhirt.eventlog._
import test._

class SerializingAnormEventLogSpecs extends Specification with TestAlmhirtKit {
  "A anorm SerializingAnormEventLogFactory" should {
    "create an eventlog with an SerializingAnormEventLogActor when configured" in {
      inTestAlmhirt(implicit almhirt => {
        true
      })
    }
  }

  val idForEvent = java.util.UUID.randomUUID()
  private def withEmptyEventLog[T](f: (DomainEventLog, Almhirt) => T) =
    inTestAlmhirt(almhirt => f(almhirt.environment.eventLog, almhirt))

  val testEventA = TestPersonCreated(java.util.UUID.randomUUID(), "testEventA", new org.joda.time.DateTime)
  private def withEventLogWithOneTestEventA[T](f: (DomainEventLog, Almhirt) => T) =
    inTestAlmhirt { almhirt =>
      almhirt.environment.eventLog.storeEvents(List(testEventA)).awaitResult(Duration(1, "s")).forceResult
      f(almhirt.environment.eventLog, almhirt)
    }

  "A anorm SerializingAnormEventLog" should {
    "when empty" in {
      "return 0L as the next required version" in {
        withEmptyEventLog { (eventLog, almhirt) =>
          val next = eventLog.getRequiredNextEventVersion(almhirt.getUuid).awaitResult(Duration(1, "s")).forceResult
          next === 0L
        }
      }
      "accept an event" in {
        withEmptyEventLog { (eventLog, almhirt) =>
          val event = TestPersonCreated(idForEvent, "test", almhirt.getDateTime)
          val res = eventLog.storeEvents(List(event)).awaitResult(Duration(1, "s")).forceResult
          true
        }
      }
      "accept an event and return exactly 1 event as the commited events" in {
        withEmptyEventLog { (eventLog, almhirt) =>
          val event = TestPersonCreated(idForEvent, "test", almhirt.getDateTime)
          val res = eventLog.storeEvents(List(event)).awaitResult(Duration(1, "s")).forceResult
          res.size === 1
        }
      }
      "accept an event and return exactly 1 event when queried for all events" in {
        withEmptyEventLog { (eventLog, almhirt) =>
          val event = TestPersonCreated(idForEvent, "test", almhirt.getDateTime)
          val resCommit = eventLog.storeEvents(List(event)).awaitResult(Duration(1, "s")).forceResult
          val res = eventLog.getEvents(idForEvent).awaitResult(Duration(1, "s")).forceResult
          res.size === 1
        }
      }
      "accept an event and return exactly the same event when queried for all events" in {
        withEmptyEventLog { (eventLog, almhirt) =>
          val event = TestPersonCreated(idForEvent, "test", almhirt.getDateTime)
          val resCommit = eventLog.storeEvents(List(event)).awaitResult(Duration(1, "s")).forceResult
          val res = eventLog.getEvents(idForEvent).awaitResult(Duration(1, "s")).forceResult
          res.headOption === Some(event)
        }
      }
    }
    "when already conatining an event with version 0L" in {
      "return exactly 1 event when queried for all events" in {
        withEventLogWithOneTestEventA { (eventLog, almhirt) =>
          val res = eventLog.getAllEvents.awaitResult(Duration(1, "s")).forceResult
          println(res)
          res.size === 1
        }
      }
      "return exactly 1 event when queried for all events for the given id" in {
        withEventLogWithOneTestEventA { (eventLog, almhirt) =>
          val res = eventLog.getEvents(testEventA.id).awaitResult(Duration(1, "s")).forceResult
          res.size === 1
        }
      }
      "return 1L as the next required version" in {
        withEventLogWithOneTestEventA { (eventLog, almhirt) =>
          val next = eventLog.getRequiredNextEventVersion(testEventA.id).awaitResult(Duration(1, "s")).forceResult
          next === 1L
        }
      }
    }
  }

}