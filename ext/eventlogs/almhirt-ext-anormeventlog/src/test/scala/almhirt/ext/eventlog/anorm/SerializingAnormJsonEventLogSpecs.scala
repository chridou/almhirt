package almhirt.ext.eventlog.anorm

import org.scalatest._
import org.scalatest.matchers.ShouldMatchers
import scala.concurrent.duration._
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.core.Almhirt
import almhirt.almvalidation.kit._
import almhirt.environment._
import almhirt.eventlog._
import almhirt.domain.DomainEvent
import com.typesafe.config.ConfigFactory
import almhirt.core.test.BlockingRepoCoreBootstrapper
import almhirt.ext.core.riftwarp.RiftWarpBootstrapper
import almhirt.core.riftwarp.test.WithTestDecomposersAndRecomposersBootstrapper
import almhirt.core.test._
import almhirt.domain.AggregateRootRef

class SerializingAnormJsonEventLogSpecs extends FlatSpec with ShouldMatchers with AlmhirtTestKit {
  val bootstrapper =
    new BlockingRepoCoreBootstrapper(ConfigFactory.load()) with RiftWarpBootstrapper with WithTestDecomposersAndRecomposersBootstrapper

  def inLocalTestAlmhirt[T](compute: AlmhirtForExtendedTesting => T) =
    inExtendedTestAlmhirt(bootstrapper)(compute)

  "An anorm SerializingAnormEventLogFactory" should
    "create an eventlog with an SerializingAnormEventLogActor when configured" in {
      inLocalTestAlmhirt(implicit almhirt => {
        true
      })
    }

  val aggIdForEvent = java.util.UUID.randomUUID()
  private def withEmptyEventLog[T](f: (DomainEventLog, Almhirt) => T) =
    inLocalTestAlmhirt(almhirt => f(almhirt.eventLog, almhirt))

  val testEventA = TestPersonCreated(java.util.UUID.randomUUID(), AggregateRootRef(java.util.UUID.randomUUID), "testEventA", new org.joda.time.DateTime)
  private def withEventLogWithOneTestEventA[T](f: (DomainEventLog, Almhirt) => T) =
    inLocalTestAlmhirt { almhirt =>
      almhirt.eventLog.storeEvents(IndexedSeq(testEventA)).awaitResult(Duration(1, "s")).forceResult
      f(almhirt.eventLog, almhirt)
    }

  "An empty anorm SerializingAnormEventLog" should
    "accept an event" in {
      withEmptyEventLog { (eventLog, almhirt) =>
        val event = TestPersonCreated(java.util.UUID.randomUUID(), AggregateRootRef(aggIdForEvent), "test", almhirt.getDateTime)
        val res = eventLog.storeEvents(IndexedSeq(event)).awaitResult(Duration(1, "s")).forceResult
      }
    }
  it should "accept an event and return exactly 1 event as the commited events" in {
    withEmptyEventLog { (eventLog, almhirt) =>
      val event = TestPersonCreated(java.util.UUID.randomUUID(), AggregateRootRef(aggIdForEvent), "test", almhirt.getDateTime)
      val res = eventLog.storeEvents(IndexedSeq(event)).awaitResult(Duration(1, "s"))
      res.forceResult should have size 1
    }
  }
  it should "accept an event and return exactly 1 event when queried for all events" in {
    withEmptyEventLog { (eventLog, almhirt) =>
      val event = TestPersonCreated(java.util.UUID.randomUUID(), AggregateRootRef(aggIdForEvent), "test", almhirt.getDateTime)
      val resCommit = eventLog.storeEvents(IndexedSeq(event)).awaitResult(Duration(1, "s")).forceResult
      val res = eventLog.getEvents(aggIdForEvent).awaitResult(Duration(1, "s"))
      println("xxxx" + res)
      res.forceResult should have size 1
    }
  }
  it should "accept an event and return exactly the same event when queried for all events" in {
    withEmptyEventLog { (eventLog, almhirt) =>
      val event = TestPersonCreated(java.util.UUID.randomUUID(), AggregateRootRef(aggIdForEvent), "test", almhirt.getDateTime)
      val resCommit = eventLog.storeEvents(IndexedSeq(event)).awaitResult(Duration(1, "s")).forceResult
      val res = eventLog.getEvents(aggIdForEvent).awaitResult(Duration(1, "s")).forceResult
      res.headOption should equal(Some(event))
    }
  }
  it should "accept 100 events with the same aggId and return them in the same order (getEvents(aggId))" in {
    withEmptyEventLog { (eventLog, almhirt) =>
      val aggId = almhirt.getUuid
      val firstEvent = TestPersonCreated(almhirt.getUuid, AggregateRootRef(aggId), "testEvent0")
      val events = firstEvent +: (for (i <- 1 until 100) yield TestPersonNameChanged(almhirt.getUuid, (aggId, i.toLong), "testEvent%d".format(i)))
      val resCommit = eventLog.storeEvents(events).awaitResult(Duration(1, "s")).forceResult
      val res = eventLog.getEvents(aggId).awaitResult(Duration(1, "s")).withFailEffect(p => println(p))
      res.forceResult should equal(events)
    }
  }
  it should "accept 100 events with the same aggId shuffled with 100 other events and return the events for a specific aggId in the same order(getEvents(aggId))" in {
    withEmptyEventLog { (eventLog, almhirt) =>
      val aggId = almhirt.getUuid
      val firstEvent = TestPersonCreated(almhirt.getUuid, AggregateRootRef(aggId), "testEvent0")
      val events = firstEvent :: (for (i <- 1 until 100) yield TestPersonNameChanged(almhirt.getUuid, (aggId, i.toLong), "testEvent%d".format(i)).asInstanceOf[DomainEvent]).toList
      val eventsToShuffleIn = (for (i <- 0 until 100) yield TestPersonCreated(almhirt.getUuid, AggregateRootRef(almhirt.getUuid), "shuffle%d".format(i)).asInstanceOf[DomainEvent]).toList
      val shuffled = events.reverse.zip(eventsToShuffleIn.reverse).foldLeft(List.empty[DomainEvent])((acc, elem) => elem._1 :: elem._2 :: acc)
      val resCommit = eventLog.storeEvents(shuffled.toVector).awaitResult(Duration(1, "s")).forceResult
      val res = eventLog.getEvents(aggId).awaitResult(Duration(1, "s")).withFailEffect(p => println(p)).forceResult
      res should equal(events)
    }
  }
  it should "accept 100 events with the same aggId shuffled with 100 other events and return the last 10 events for a specific aggId in the same order(getEvents(aggId, from))" in {
    withEmptyEventLog { (eventLog, almhirt) =>
      val aggId = almhirt.getUuid
      val firstEvent = TestPersonCreated(almhirt.getUuid, AggregateRootRef(aggId), "testEvent0")
      val events = firstEvent :: (for (i <- 1 until 100) yield TestPersonNameChanged(almhirt.getUuid, (aggId, i.toLong), "testEvent%d".format(i)).asInstanceOf[DomainEvent]).toList
      val eventsToShuffleIn = (for (i <- 0 until 100) yield TestPersonCreated(almhirt.getUuid, AggregateRootRef(almhirt.getUuid), "shuffle%d".format(i)).asInstanceOf[DomainEvent]).toList
      val shuffled = events.reverse.zip(eventsToShuffleIn.reverse).foldLeft(List.empty[DomainEvent])((acc, elem) => elem._1 :: elem._2 :: acc)
      val resCommit = eventLog.storeEvents(shuffled.toVector).awaitResult(Duration(1, "s")).forceResult
      val res = eventLog.getEvents(aggId, 90).awaitResult(Duration(1, "s")).withFailEffect(p => println(p)).forceResult
      res should equal(events.drop(90))
    }
  }
  it should "accept 100 events with the same aggId shuffled with 100 other events and return the first 10 events for a specific aggId in the same order(getEvents(aggId, from, to))" in {
    withEmptyEventLog { (eventLog, almhirt) =>
      val aggId = almhirt.getUuid
      val firstEvent = TestPersonCreated(almhirt.getUuid, AggregateRootRef(aggId), "testEvent0")
      val events = firstEvent :: (for (i <- 1 until 100) yield TestPersonNameChanged(almhirt.getUuid, (aggId, i.toLong), "testEvent%d".format(i)).asInstanceOf[DomainEvent]).toList
      val eventsToShuffleIn = (for (i <- 0 until 100) yield TestPersonCreated(almhirt.getUuid, AggregateRootRef(almhirt.getUuid), "shuffle%d".format(i)).asInstanceOf[DomainEvent]).toList
      val shuffled = events.reverse.zip(eventsToShuffleIn.reverse).foldLeft(List.empty[DomainEvent])((acc, elem) => elem._1 :: elem._2 :: acc)
      val resCommit = eventLog.storeEvents(shuffled.toVector).awaitResult(Duration(1, "s")).forceResult
      val res = eventLog.getEvents(aggId, 0, 9).awaitResult(Duration(1, "s")).withFailEffect(p => println(p)).forceResult
      res should equal(events.take(10))
    }
  }
  it should "accept 100 events with the same aggId shuffled with 100 other events and return the events from 40 to 59 for a specific aggId in the same order(getEvents(aggId, from, to))" in {
    withEmptyEventLog { (eventLog, almhirt) =>
      val aggId = almhirt.getUuid
      val firstEvent = TestPersonCreated(almhirt.getUuid, AggregateRootRef(aggId), "testEvent0")
      val events = firstEvent :: (for (i <- 1 until 100) yield TestPersonNameChanged(almhirt.getUuid, (aggId, i.toLong), "testEvent%d".format(i)).asInstanceOf[DomainEvent]).toList
      val eventsToShuffleIn = (for (i <- 0 until 100) yield TestPersonCreated(almhirt.getUuid, AggregateRootRef(almhirt.getUuid), "shuffle%d".format(i)).asInstanceOf[DomainEvent]).toList
      val shuffled = events.reverse.zip(eventsToShuffleIn.reverse).foldLeft(List.empty[DomainEvent])((acc, elem) => elem._1 :: elem._2 :: acc)
      val resCommit = eventLog.storeEvents(shuffled.toVector).awaitResult(Duration(1, "s")).forceResult
      val res = eventLog.getEvents(aggId, 40, 59).awaitResult(Duration(1, "s")).withFailEffect(p => println(p)).forceResult
      res should equal(events.drop(40).take(20))
    }
  }

  "An anorm SerializingAnormEventLogwhen already containing an event with version 0L when nothing else is added" should
    "return exactly 1 event when queried for all events for the given id(the one that was already present)" in {
      withEventLogWithOneTestEventA { (eventLog, almhirt) =>
        val res = eventLog.getEvents(testEventA.aggId).awaitResult(Duration(1, "s")).forceResult
        res should have size 1
      }
    }

  "An anorm SerializingAnormEventLogwhen already containing an event with version 0L after adding another event" should
    "have accepted an event with a different id" in {
      withEmptyEventLog { (eventLog, almhirt) =>
        val event = TestPersonCreated(almhirt.getUuid, AggregateRootRef(almhirt.getUuid), "test", almhirt.getDateTime)
        val res = eventLog.storeEvents(IndexedSeq(event)).awaitResult(Duration(1, "s")).forceResult
      }
    }
  it should "return exactly 2 events when queried for all events" in {
    withEventLogWithOneTestEventA { (eventLog, almhirt) =>
      val event = TestPersonCreated(almhirt.getUuid, AggregateRootRef(almhirt.getUuid), "test", almhirt.getDateTime)
      eventLog.storeEvents(IndexedSeq(event)).awaitResult(Duration(1, "s")).forceResult
      val res = eventLog.getAllEvents.awaitResult(Duration(1, "s")).forceResult
      res should have size 2
    }
  }
  it should "return exactly 1 event after adding another event when queried for all events for the given id(the one that was already present)" in {
    withEventLogWithOneTestEventA { (eventLog, almhirt) =>
      val event = TestPersonCreated(almhirt.getUuid, AggregateRootRef(almhirt.getUuid), "test", almhirt.getDateTime)
      eventLog.storeEvents(IndexedSeq(event)).awaitResult(Duration(1, "s")).forceResult
      val res = eventLog.getEvents(testEventA.aggId).awaitResult(Duration(1, "s")).forceResult
      res should have size 1
    }
  }
  it should "return exactly 1 event after adding another event when queried for all events for the given id(the added one)" in {
    withEventLogWithOneTestEventA { (eventLog, almhirt) =>
      val event = TestPersonCreated(almhirt.getUuid, AggregateRootRef(almhirt.getUuid), "test", almhirt.getDateTime)
      eventLog.storeEvents(IndexedSeq(event)).awaitResult(Duration(1, "s")).forceResult
      val res = eventLog.getEvents(event.aggId).awaitResult(Duration(1, "s")).forceResult
      res should have size 1
    }
  }
  it should "return the 2 events in the order they have been added" in {
    withEventLogWithOneTestEventA { (eventLog, almhirt) =>
      val event = TestPersonCreated(almhirt.getUuid, AggregateRootRef(almhirt.getUuid), "test", almhirt.getDateTime)
      eventLog.storeEvents(IndexedSeq(event)).awaitResult(Duration(1, "s")).forceResult
      val res = eventLog.getAllEvents.awaitResult(Duration(1, "s")).forceResult
      res.toList should equal(List(testEventA, event))
    }
  }
}