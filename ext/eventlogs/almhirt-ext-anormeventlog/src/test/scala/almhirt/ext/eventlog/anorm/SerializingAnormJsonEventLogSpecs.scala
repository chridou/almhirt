package almhirt.ext.eventlog.anorm

import org.specs2.mutable._
import scalaz.syntax.validation._
import akka.util.Duration
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.environment._
import almhirt.eventlog._
import almhirt.domain.DomainEvent

class SerializingAnormJsonEventLogSpecs extends Specification with TestAlmhirtKit {
  "An anorm SerializingAnormEventLogFactory" should {
    "create an eventlog with an SerializingAnormEventLogActor when configured" in {
      inTestAlmhirt(implicit almhirt => {
        true
      })
    }
  }

  val aggIdForEvent = java.util.UUID.randomUUID()
  private def withEmptyEventLog[T](f: (DomainEventLog, Almhirt) => T) =
    inTestAlmhirt(almhirt => f(almhirt.eventLog, almhirt))

  val testEventA = TestPersonCreated(java.util.UUID.randomUUID(), java.util.UUID.randomUUID(), "testEventA", new org.joda.time.DateTime)
  private def withEventLogWithOneTestEventA[T](f: (DomainEventLog, Almhirt) => T) =
    inTestAlmhirt { almhirt =>
      almhirt.eventLog.storeEvents(List(testEventA)).awaitResult(Duration(1, "s")).forceResult
      f(almhirt.eventLog, almhirt)
    }

  "An anorm SerializingAnormEventLog" should {
    "when empty" in {
      "return 0L as the next required version" in {
        withEmptyEventLog { (eventLog, almhirt) =>
          val next = eventLog.getRequiredNextEventVersion(almhirt.getUuid).awaitResult(Duration(1, "s")).forceResult
          next === 0L
        }
      }
      "accept an event" in {
        withEmptyEventLog { (eventLog, almhirt) =>
          val event = TestPersonCreated(java.util.UUID.randomUUID(), aggIdForEvent, "test", almhirt.getDateTime)
          val res = eventLog.storeEvents(List(event)).awaitResult(Duration(1, "s")).forceResult
          true
        }
      }
      "accept an event and return exactly 1 event as the commited events" in {
        withEmptyEventLog { (eventLog, almhirt) =>
          val event = TestPersonCreated(java.util.UUID.randomUUID(), aggIdForEvent, "test", almhirt.getDateTime)
          val res = eventLog.storeEvents(List(event)).awaitResult(Duration(1, "s"))
          res.forceResult must have size 1
        }
      }
      "accept an event and return exactly 1 event when queried for all events" in {
        withEmptyEventLog { (eventLog, almhirt) =>
          val event = TestPersonCreated(java.util.UUID.randomUUID(), aggIdForEvent, "test", almhirt.getDateTime)
          val resCommit = eventLog.storeEvents(List(event)).awaitResult(Duration(1, "s")).forceResult
          val res = eventLog.getEvents(aggIdForEvent).awaitResult(Duration(1, "s"))
          res.forceResult must have size 1
        }
      }
      "accept an event and return exactly the same event when queried for all events" in {
        withEmptyEventLog { (eventLog, almhirt) =>
          val event = TestPersonCreated(java.util.UUID.randomUUID(), aggIdForEvent, "test", almhirt.getDateTime)
          val resCommit = eventLog.storeEvents(List(event)).awaitResult(Duration(1, "s")).forceResult
          val res = eventLog.getEvents(aggIdForEvent).awaitResult(Duration(1, "s")).forceResult
          res.headOption === Some(event)
        }
      }
      "accept 100 events with the same aggId and return them in the same order (getEvents(aggId))" in {
        withEmptyEventLog { (eventLog, almhirt) =>
          val aggId = almhirt.getUuid
          val firstEvent = TestPersonCreated(almhirt.getUuid, aggId, "testEvent0")
          val events = firstEvent :: (for (i <- 1 until 100) yield TestPersonNameChanged(almhirt.getUuid, aggId, i, "testEvent%d".format(i))).toList
          val resCommit = eventLog.storeEvents(events).awaitResult(Duration(1, "s")).forceResult
          val res = eventLog.getEvents(aggId).awaitResult(Duration(1, "s"))
          res.forceResult === events
        }
      }
      "accept 100 events with the same aggId shuffled with 100 other events and return the events for a specific aggId in the same order(getEvents(aggId))" in {
        withEmptyEventLog { (eventLog, almhirt) =>
          val aggId = almhirt.getUuid
          val firstEvent = TestPersonCreated(almhirt.getUuid, aggId, "testEvent0")
          val events = firstEvent :: (for (i <- 1 until 100) yield TestPersonNameChanged(almhirt.getUuid, aggId, i, "testEvent%d".format(i)).asInstanceOf[DomainEvent]).toList
          val eventsToShuffleIn = (for (i <- 0 until 100) yield TestPersonCreated(almhirt.getUuid, almhirt.getUuid, "shuffle%d".format(i)).asInstanceOf[DomainEvent]).toList
          val shuffled = events.reverse.zip(eventsToShuffleIn.reverse).foldLeft(List.empty[DomainEvent])((acc, elem) => elem._1 :: elem._2 :: acc)
          val resCommit = eventLog.storeEvents(shuffled).awaitResult(Duration(1, "s")).forceResult
          val res = eventLog.getEvents(aggId).awaitResult(Duration(1, "s")).forceResult
          res === events
        }
      }
      "accept 100 events with the same aggId shuffled with 100 other events and return the last 10 events for a specific aggId in the same order(getEvents(aggId, from))" in {
        withEmptyEventLog { (eventLog, almhirt) =>
          val aggId = almhirt.getUuid
          val firstEvent = TestPersonCreated(almhirt.getUuid, aggId, "testEvent0")
          val events = firstEvent :: (for (i <- 1 until 100) yield TestPersonNameChanged(almhirt.getUuid, aggId, i, "testEvent%d".format(i)).asInstanceOf[DomainEvent]).toList
          val eventsToShuffleIn = (for (i <- 0 until 100) yield TestPersonCreated(almhirt.getUuid, almhirt.getUuid, "shuffle%d".format(i)).asInstanceOf[DomainEvent]).toList
          val shuffled = events.reverse.zip(eventsToShuffleIn.reverse).foldLeft(List.empty[DomainEvent])((acc, elem) => elem._1 :: elem._2 :: acc)
          val resCommit = eventLog.storeEvents(shuffled).awaitResult(Duration(1, "s")).forceResult
          val res = eventLog.getEvents(aggId, 90).awaitResult(Duration(1, "s")).forceResult
          res === events.drop(90)
        }
      }
      "accept 100 events with the same aggId shuffled with 100 other events and return the first 10 events for a specific aggId in the same order(getEvents(aggId, from, to))" in {
        withEmptyEventLog { (eventLog, almhirt) =>
          val aggId = almhirt.getUuid
          val firstEvent = TestPersonCreated(almhirt.getUuid, aggId, "testEvent0")
          val events = firstEvent :: (for (i <- 1 until 100) yield TestPersonNameChanged(almhirt.getUuid, aggId, i, "testEvent%d".format(i)).asInstanceOf[DomainEvent]).toList
          val eventsToShuffleIn = (for (i <- 0 until 100) yield TestPersonCreated(almhirt.getUuid, almhirt.getUuid, "shuffle%d".format(i)).asInstanceOf[DomainEvent]).toList
          val shuffled = events.reverse.zip(eventsToShuffleIn.reverse).foldLeft(List.empty[DomainEvent])((acc, elem) => elem._1 :: elem._2 :: acc)
          val resCommit = eventLog.storeEvents(shuffled).awaitResult(Duration(1, "s")).forceResult
          val res = eventLog.getEvents(aggId, 0, 9).awaitResult(Duration(1, "s")).forceResult
          res === events.take(10)
        }
      }
      "accept 100 events with the same aggId shuffled with 100 other events and return the events from 40 to 59 for a specific aggId in the same order(getEvents(aggId, from, to))" in {
        withEmptyEventLog { (eventLog, almhirt) =>
          val aggId = almhirt.getUuid
          val firstEvent = TestPersonCreated(almhirt.getUuid, aggId, "testEvent0")
          val events = firstEvent :: (for (i <- 1 until 100) yield TestPersonNameChanged(almhirt.getUuid, aggId, i, "testEvent%d".format(i)).asInstanceOf[DomainEvent]).toList
          val eventsToShuffleIn = (for (i <- 0 until 100) yield TestPersonCreated(almhirt.getUuid, almhirt.getUuid, "shuffle%d".format(i)).asInstanceOf[DomainEvent]).toList
          val shuffled = events.reverse.zip(eventsToShuffleIn.reverse).foldLeft(List.empty[DomainEvent])((acc, elem) => elem._1 :: elem._2 :: acc)
          val resCommit = eventLog.storeEvents(shuffled).awaitResult(Duration(1, "s")).forceResult
          val res = eventLog.getEvents(aggId, 40, 59).awaitResult(Duration(1, "s")).forceResult
          res === events.drop(40).take(20)
        }
      }
    }
    "when already containing an event with version 0L" in {
      "and none were added" in {
        "return exactly 1 event when queried for all events for the given id(the one that was already present)" in {
          withEventLogWithOneTestEventA { (eventLog, almhirt) =>
            val res = eventLog.getEvents(testEventA.aggId).awaitResult(Duration(1, "s")).forceResult
            res must have size 1
          }
        }
        "return 1L as the next required version" in {
          withEventLogWithOneTestEventA { (eventLog, almhirt) =>
            val next = eventLog.getRequiredNextEventVersion(testEventA.aggId).awaitResult(Duration(1, "s")).forceResult
            next === 1L
          }
        }
      }
      "after adding another event" in {
        "have accepted an event with a different id" in {
          withEmptyEventLog { (eventLog, almhirt) =>
            val event = TestPersonCreated(almhirt.getUuid, almhirt.getUuid, "test", almhirt.getDateTime)
            val res = eventLog.storeEvents(List(event)).awaitResult(Duration(1, "s")).forceResult
            true
          }
        }
        "return exactly 2 events when queried for all events" in {
          withEventLogWithOneTestEventA { (eventLog, almhirt) =>
            val event = TestPersonCreated(almhirt.getUuid, almhirt.getUuid, "test", almhirt.getDateTime)
            eventLog.storeEvents(List(event)).awaitResult(Duration(1, "s")).forceResult
            val res = eventLog.getAllEvents.awaitResult(Duration(1, "s")).forceResult
            res must have size 2
          }
        }
        "return exactly 1 event after adding another event when queried for all events for the given id(the one that was already present)" in {
          withEventLogWithOneTestEventA { (eventLog, almhirt) =>
            val event = TestPersonCreated(almhirt.getUuid, almhirt.getUuid, "test", almhirt.getDateTime)
            eventLog.storeEvents(List(event)).awaitResult(Duration(1, "s")).forceResult
            val res = eventLog.getEvents(testEventA.aggId).awaitResult(Duration(1, "s")).forceResult
            res must have size 1
          }
        }
        "return exactly 1 event after adding another event when queried for all events for the given id(the added one)" in {
          withEventLogWithOneTestEventA { (eventLog, almhirt) =>
            val event = TestPersonCreated(almhirt.getUuid, almhirt.getUuid, "test", almhirt.getDateTime)
            eventLog.storeEvents(List(event)).awaitResult(Duration(1, "s")).forceResult
            val res = eventLog.getEvents(event.aggId).awaitResult(Duration(1, "s")).forceResult
            res must have size 1
          }
        }
        "return the 2 events in the order they have been added" in {
          withEventLogWithOneTestEventA { (eventLog, almhirt) =>
            val event = TestPersonCreated(almhirt.getUuid, almhirt.getUuid, "test", almhirt.getDateTime)
            eventLog.storeEvents(List(event)).awaitResult(Duration(1, "s")).forceResult
            val res = eventLog.getAllEvents.awaitResult(Duration(1, "s")).forceResult
            res.toList === List(testEventA, event)
          }
        }
      }
    }
  }

}