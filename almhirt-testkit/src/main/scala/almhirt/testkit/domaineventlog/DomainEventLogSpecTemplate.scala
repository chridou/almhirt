package almhirt.testkit.domaineventlog

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSpec
import scala.concurrent.duration.FiniteDuration
import akka.testkit.TestProbe
import almhirt.testkit._
import akka.actor._
import akka.pattern._
import akka.util.Timeout
import almhirt.common._
import almhirt.core.types._
import almhirt.almvalidation.kit._
import almhirt.almfuture.all._
import almhirt.core.HasAlmhirt
import almhirt.domain._
import play.api.libs.iteratee.Iteratee

abstract class DomainEventLogSpecTemplate(theActorSystem: ActorSystem)
  extends AlmhirtTestKit(theActorSystem)
  with HasAlmhirt
  with FunSpec
  with ShouldMatchers { self: CreatesDomainEventLog =>

  implicit def execContext = theAlmhirt.futuresExecutor

  // Override in case your database needs some "startup" time...
  def sleepBeforeEachTest: Option[FiniteDuration] = None
  // Override in case your database needs some time after writing which is a bad thing......
  def sleepAfterInsert: Option[FiniteDuration] = None

  def useDomainEventLog[T](f: ActorRef => T): T = {
    val testId = nextTestId
    val (domaineventlog, eventLogCleanUp) = createDomainEventLog(testId)
    try {
      val res = f(domaineventlog)
      system.stop(domaineventlog)
      eventLogCleanUp()
      res
    } catch {
      case exn: Exception =>
        system.stop(domaineventlog)
        eventLogCleanUp()
        throw exn
    }
  }

  def sleepAfterInsertIfNeccessary(): AlmFuture[Unit] =
    sleepAfterInsert match { case Some(dur) => AlmFuture.successful(Thread.sleep(dur.toMillis)) case None => AlmFuture.successful(()) }

  import almhirt.domaineventlog.DomainEventLog._

  describe("A domain event log") {
    it("""should accept a "CommitDomainEvents" with 0 events and answer with a "CommittedDomainEvents" containing no committed events""") {
      useDomainEventLog { eventlog =>
        sleepBeforeEachTest.foreach(d => Thread.sleep(d.toMillis))
        val res = (eventlog ? CommitDomainEvents(Vector.empty))(defaultDuration).successfulAlmFuture[DomainEventLogMessage].awaitResultOrEscalate(defaultDuration)
        res should equal(CommittedDomainEvents(IndexedSeq.empty))
      }
    }

    it("""should accept a "CommitDomainEvents" with 1 event and answer with a "CommittedDomainEvents" containing the committed event""") {
      useDomainEventLog { eventlog =>
        sleepBeforeEachTest.foreach(d => Thread.sleep(d.toMillis))
        val event = AR1Created(DomainEventHeader(AggregateRootRef(theAlmhirt.getUuid)), "a")
        val res = (eventlog ? CommitDomainEvents(Vector(event)))(defaultDuration).successfulAlmFuture[DomainEventLogMessage].awaitResultOrEscalate(defaultDuration)
        res should equal(CommittedDomainEvents(IndexedSeq(event)))
      }
    }

    it("""should accept a "CommitDomainEvents" with many events for one aggregate root and answer with a "CommittedDomainEvents" containing the committed events in the original order""") {
      useDomainEventLog { eventlog =>
        sleepBeforeEachTest.foreach(d => Thread.sleep(d.toMillis))
        val events = createEvents(theAlmhirt.getUuid, 100)
        val res = (eventlog ? CommitDomainEvents(events))(defaultDuration).successfulAlmFuture[DomainEventLogMessage].awaitResultOrEscalate(defaultDuration)
        res should equal(CommittedDomainEvents(events))
      }
    }

    it("""should accept 3x "CommitDomainEvents" for 3 ARs with many events one aggregate root each and answer with a "CommittedDomainEvents" containing the committed events in the original order""") {
      useDomainEventLog { eventlog =>
        sleepBeforeEachTest.foreach(d => Thread.sleep(d.toMillis))
        val events1 = createEvents(theAlmhirt.getUuid, 100)
        val events2 = createEvents(theAlmhirt.getUuid, 100)
        val events3 = createEvents(theAlmhirt.getUuid, 100)
        val res1F = (eventlog ? CommitDomainEvents(events1))(defaultDuration).successfulAlmFuture[DomainEventLogMessage]
        val res2F = (eventlog ? CommitDomainEvents(events2))(defaultDuration).successfulAlmFuture[DomainEventLogMessage]
        val res3F = (eventlog ? CommitDomainEvents(events3))(defaultDuration).successfulAlmFuture[DomainEventLogMessage]

        val res =
          (for {
            res1 <- res1F
            res2 <- res2F
            res3 <- res3F
          } yield (res1, res2, res3)).awaitResultOrEscalate(defaultDuration)
        res should equal((CommittedDomainEvents(events1), CommittedDomainEvents(events2), CommittedDomainEvents(events3)))
      }
    }

    it("""should accept 3x "CommitDomainEvents" for 3 ARs with many events for one aggregate root each and when queried for each aggregate root the events should be returned in the original order""") {
      useDomainEventLog { eventlog =>
        sleepBeforeEachTest.foreach(d => Thread.sleep(d.toMillis))
        val events1 = createEvents(theAlmhirt.getUuid, 100)
        val events2 = createEvents(theAlmhirt.getUuid, 100)
        val events3 = createEvents(theAlmhirt.getUuid, 100)
        val res1F =
          for {
            _ <- (eventlog ? CommitDomainEvents(events1))(defaultDuration).successfulAlmFuture[DomainEventLogMessage]
            _ <- sleepAfterInsertIfNeccessary()
            enumerator <- (eventlog ? GetAllDomainEventsFor(events1.head.aggId))(defaultDuration).successfulAlmFuture[FetchedDomainEvents].map(_.enumerator)
            events <- enumerator.run(Iteratee.getChunks[DomainEvent]).toSuccessfulAlmFuture
          } yield events
        val res2F =
          for {
            _ <- (eventlog ? CommitDomainEvents(events2))(defaultDuration).successfulAlmFuture[DomainEventLogMessage]
            _ <- sleepAfterInsertIfNeccessary()
            enumerator <- (eventlog ? GetAllDomainEventsFor(events2.head.aggId))(defaultDuration).successfulAlmFuture[FetchedDomainEvents].map(_.enumerator)
            events <- enumerator.run(Iteratee.getChunks[DomainEvent]).toSuccessfulAlmFuture
          } yield events
        val res3F =
          for {
            _ <- (eventlog ? CommitDomainEvents(events3))(defaultDuration).successfulAlmFuture[DomainEventLogMessage]
            _ <- sleepAfterInsertIfNeccessary()
            enumerator <- (eventlog ? GetAllDomainEventsFor(events3.head.aggId))(defaultDuration).successfulAlmFuture[FetchedDomainEvents].map(_.enumerator)
            events <- enumerator.run(Iteratee.getChunks[DomainEvent]).toSuccessfulAlmFuture
          } yield events

        val res =
          (for {
            res1 <- res1F
            res2 <- res2F
            res3 <- res3F
          } yield (res1, res2, res3)).awaitResultOrEscalate(defaultDuration)
        (res._1 ++ res._2 ++ res._3) should have size((events1 ++ events2 ++ events3).size)
        res should equal((
          events1,
          events2,
          events3))
      }
    }

    it("""should accept a "CommitDomainEvents" with many events for one aggregate root which when queried for are returned in the order they were committed""") {
      useDomainEventLog { eventlog =>
        sleepBeforeEachTest.foreach(d => Thread.sleep(d.toMillis))
        val arId = theAlmhirt.getUuid
        val events = createEvents(arId, 100)
        val res =
          (for {
            _ <- (eventlog ? CommitDomainEvents(events))(defaultDuration).successfulAlmFuture[DomainEventLogMessage]
            _ <- sleepAfterInsertIfNeccessary()
            enumerator <- (eventlog ? GetAllDomainEventsFor(arId))(defaultDuration).successfulAlmFuture[FetchedDomainEvents].map(_.enumerator)
            events <- enumerator.run(Iteratee.getChunks[DomainEvent]).toSuccessfulAlmFuture
          } yield events).awaitResultOrEscalate(defaultDuration)
        res should equal(events)
      }
    }

    it("""should when queried with "GetAllDomainEvents" return no events if the domain event log is empty""") {
      useDomainEventLog { eventlog =>
        sleepBeforeEachTest.foreach(d => Thread.sleep(d.toMillis))
        val res =
          (for {
            enumerator <- (eventlog ? GetAllDomainEvents)(defaultDuration).successfulAlmFuture[FetchedDomainEvents].map(_.enumerator)
            events <- enumerator.run(Iteratee.getChunks[DomainEvent]).toSuccessfulAlmFuture
          } yield events).awaitResultOrEscalate(defaultDuration)
        res should equal(Seq.empty)
      }
    }

    it("""should when queried with "GetAllDomainEvents" return all events in the correct order""") {
      useDomainEventLog { eventlog =>
        sleepBeforeEachTest.foreach(d => Thread.sleep(d.toMillis))
        val arId = theAlmhirt.getUuid
        val events = createEvents(arId, 100)
        val res =
          (for {
            _ <- (eventlog ? CommitDomainEvents(events))(defaultDuration).successfulAlmFuture[DomainEventLogMessage]
            _ <- sleepAfterInsertIfNeccessary()
            enumerator <- (eventlog ? GetAllDomainEvents)(defaultDuration).successfulAlmFuture[FetchedDomainEvents].map(_.enumerator)
            events <- enumerator.run(Iteratee.getChunks[DomainEvent]).toSuccessfulAlmFuture
          } yield events).awaitResultOrEscalate(defaultDuration)
        res should have size(events.size)
        res should equal(events)
      }
    }

    it("""should when queried with "GetAllDomainEventsFor" return no events if the domain event log is empty""") {
      useDomainEventLog { eventlog =>
        sleepBeforeEachTest.foreach(d => Thread.sleep(d.toMillis))
        val arId = theAlmhirt.getUuid
        val res =
          (for {
            enumerator <- (eventlog ? GetAllDomainEventsFor(arId))(defaultDuration).successfulAlmFuture[FetchedDomainEvents].map(_.enumerator)
            events <- enumerator.run(Iteratee.getChunks[DomainEvent]).toSuccessfulAlmFuture
          } yield events).awaitResult(defaultDuration)
        println(res)
        res.resultOrEscalate should equal(Seq.empty)
      }
    }

    it("""should when queried with "GetAllDomainEventsFor" return all events in the correct order""") {
      useDomainEventLog { eventlog =>
        sleepBeforeEachTest.foreach(d => Thread.sleep(d.toMillis))
        val arId = theAlmhirt.getUuid
        val events = createEvents(arId, 100)
        val res =
          (for {
            _ <- (eventlog ? CommitDomainEvents(events))(defaultDuration).successfulAlmFuture[DomainEventLogMessage]
            _ <- sleepAfterInsertIfNeccessary()
            enumerator <- (eventlog ? GetAllDomainEventsFor(arId))(defaultDuration).successfulAlmFuture[FetchedDomainEvents].map(_.enumerator)
            events <- enumerator.run(Iteratee.getChunks[DomainEvent]).toSuccessfulAlmFuture
          } yield events).awaitResultOrEscalate(defaultDuration)
        res should have size(events.size)
        res should equal(events)
      }
    }

    it("""should when queried with "GetDomainEventsFrom(0L)" return all events in the correct order""") {
      useDomainEventLog { eventlog =>
        sleepBeforeEachTest.foreach(d => Thread.sleep(d.toMillis))
        val arId = theAlmhirt.getUuid
        val events = createEvents(arId, 100)
        val res =
          (for {
            _ <- (eventlog ? CommitDomainEvents(events))(defaultDuration).successfulAlmFuture[DomainEventLogMessage]
            _ <- sleepAfterInsertIfNeccessary()
            enumerator <- (eventlog ? GetDomainEventsFrom(arId, 0L))(defaultDuration).successfulAlmFuture[FetchedDomainEvents].map(_.enumerator)
            events <- enumerator.run(Iteratee.getChunks[DomainEvent]).toSuccessfulAlmFuture
          } yield events).awaitResultOrEscalate(defaultDuration)
        res should have size(events.size)
        res should equal(events)
      }
    }

    it("""should when queried with "GetDomainEventsFrom(0L < x < maxVersion)" return all events starting from x to the end in the correct order""") {
      useDomainEventLog { eventlog =>
        sleepBeforeEachTest.foreach(d => Thread.sleep(d.toMillis))
        val arId = theAlmhirt.getUuid
        val events = createEvents(arId, 100)
        val res =
          (for {
            _ <- (eventlog ? CommitDomainEvents(events))(defaultDuration).successfulAlmFuture[DomainEventLogMessage]
            _ <- sleepAfterInsertIfNeccessary()
            enumerator <- (eventlog ? GetDomainEventsFrom(arId, 3L))(defaultDuration).successfulAlmFuture[FetchedDomainEvents].map(_.enumerator)
            events <- enumerator.run(Iteratee.getChunks[DomainEvent]).toSuccessfulAlmFuture
          } yield events).awaitResultOrEscalate(defaultDuration)
        res should have size(events.drop(3).size)
        res should equal(events.drop(3))
      }
    }

    it("""should when queried with "GetDomainEventsFrom(x = maxVersion)" return the last event""") {
      useDomainEventLog { eventlog =>
        sleepBeforeEachTest.foreach(d => Thread.sleep(d.toMillis))
        val arId = theAlmhirt.getUuid
        val events = createEvents(arId, 100)
        val res =
          (for {
            _ <- (eventlog ? CommitDomainEvents(events))(defaultDuration).successfulAlmFuture[DomainEventLogMessage]
            _ <- sleepAfterInsertIfNeccessary()
            enumerator <- (eventlog ? GetDomainEventsFrom(arId, events.last.aggVersion))(defaultDuration).successfulAlmFuture[FetchedDomainEvents].map(_.enumerator)
            events <- enumerator.run(Iteratee.getChunks[DomainEvent]).toSuccessfulAlmFuture
          } yield events).awaitResultOrEscalate(defaultDuration)
        res.size should equal(1)
        res should equal(Seq(events.last))
      }
    }

    it("""should when queried with "GetDomainEventsFrom(x > maxVersion)" return no events""") {
      useDomainEventLog { eventlog =>
        sleepBeforeEachTest.foreach(d => Thread.sleep(d.toMillis))
        val arId = theAlmhirt.getUuid
        val events = createEvents(arId, 100)
        val res =
          (for {
            _ <- (eventlog ? CommitDomainEvents(events))(defaultDuration).successfulAlmFuture[DomainEventLogMessage]
            _ <- sleepAfterInsertIfNeccessary()
            enumerator <- (eventlog ? GetDomainEventsFrom(arId, events.last.aggVersion + 1L))(defaultDuration).successfulAlmFuture[FetchedDomainEvents].map(_.enumerator)
            events <- enumerator.run(Iteratee.getChunks[DomainEvent]).toSuccessfulAlmFuture
          } yield events).awaitResultOrEscalate(defaultDuration)
        res should have size(0)
        res should equal(Seq.empty)
      }
    }

    it("""should when queried with "GetDomainEventsTo(0L)" return the first event""") {
      useDomainEventLog { eventlog =>
        sleepBeforeEachTest.foreach(d => Thread.sleep(d.toMillis))
        val arId = theAlmhirt.getUuid
        val events = createEvents(arId, 100)
        val res =
          (for {
            _ <- (eventlog ? CommitDomainEvents(events))(defaultDuration).successfulAlmFuture[DomainEventLogMessage]
            _ <- sleepAfterInsertIfNeccessary()
            enumerator <- (eventlog ? GetDomainEventsTo(arId, 0L))(defaultDuration).successfulAlmFuture[FetchedDomainEvents].map(_.enumerator)
            events <- enumerator.run(Iteratee.getChunks[DomainEvent]).toSuccessfulAlmFuture
          } yield events).awaitResultOrEscalate(defaultDuration)
        res should have size(1)
        res should equal(events.take(1))
      }
    }

    it("""should when queried with "GetDomainEventsTo(x = maxVersion)" return all events""") {
      useDomainEventLog { eventlog =>
        sleepBeforeEachTest.foreach(d => Thread.sleep(d.toMillis))
        val arId = theAlmhirt.getUuid
        val events = createEvents(arId, 100)
        val res =
          (for {
            _ <- (eventlog ? CommitDomainEvents(events))(defaultDuration).successfulAlmFuture[DomainEventLogMessage]
            _ <- sleepAfterInsertIfNeccessary()
            enumerator <- (eventlog ? GetDomainEventsTo(arId, events.last.aggVersion))(defaultDuration).successfulAlmFuture[FetchedDomainEvents].map(_.enumerator)
            events <- enumerator.run(Iteratee.getChunks[DomainEvent]).toSuccessfulAlmFuture
          } yield events).awaitResultOrEscalate(defaultDuration)
        res should have size(events.size)
        res should equal(events)
      }
    }

    it("""should when queried with "GetDomainEventsTo(x > maxVersion)" return all events""") {
      useDomainEventLog { eventlog =>
        sleepBeforeEachTest.foreach(d => Thread.sleep(d.toMillis))
        val arId = theAlmhirt.getUuid
        val events = createEvents(arId, 100)
        val res =
          (for {
            _ <- (eventlog ? CommitDomainEvents(events))(defaultDuration).successfulAlmFuture[DomainEventLogMessage]
            _ <- sleepAfterInsertIfNeccessary()
            enumerator <- (eventlog ? GetDomainEventsTo(arId, events.last.aggVersion + 1))(defaultDuration).successfulAlmFuture[FetchedDomainEvents].map(_.enumerator)
            events <- enumerator.run(Iteratee.getChunks[DomainEvent]).toSuccessfulAlmFuture
          } yield events).awaitResultOrEscalate(defaultDuration)
        res should have size(events.size)
        res should equal(events)
      }
    }

    it("""should when queried with "GetDomainEventsTo(0 < x < maxVersion)" return all events up to including version x""") {
      useDomainEventLog { eventlog =>
        sleepBeforeEachTest.foreach(d => Thread.sleep(d.toMillis))
        val arId = theAlmhirt.getUuid
        val events = createEvents(arId, 100)
        val res =
          (for {
            _ <- (eventlog ? CommitDomainEvents(events))(defaultDuration).successfulAlmFuture[DomainEventLogMessage]
            _ <- sleepAfterInsertIfNeccessary()
            enumerator <- (eventlog ? GetDomainEventsTo(arId, 10L))(defaultDuration).successfulAlmFuture[FetchedDomainEvents].map(_.enumerator)
            events <- enumerator.run(Iteratee.getChunks[DomainEvent]).toSuccessfulAlmFuture
          } yield events).awaitResultOrEscalate(defaultDuration)
        res should have size (11)
        res should equal(events.take(11))
        res.last.aggVersion should equal(10L)
      }
    }

    it("""should when queried with "GetDomainEventsUntil(0L)" return no events""") {
      useDomainEventLog { eventlog =>
        sleepBeforeEachTest.foreach(d => Thread.sleep(d.toMillis))
        val arId = theAlmhirt.getUuid
        val events = createEvents(arId, 100)
        val res =
          (for {
            _ <- (eventlog ? CommitDomainEvents(events))(defaultDuration).successfulAlmFuture[DomainEventLogMessage]
            _ <- sleepAfterInsertIfNeccessary()
            enumerator <- (eventlog ? GetDomainEventsUntil(arId, 0L))(defaultDuration).successfulAlmFuture[FetchedDomainEvents].map(_.enumerator)
            events <- enumerator.run(Iteratee.getChunks[DomainEvent]).toSuccessfulAlmFuture
          } yield events).awaitResultOrEscalate(defaultDuration)
        res should have size(0)
        res should equal(Seq.empty)
      }
    }

    it("""should when queried with "GetDomainEventsUntil(1L)" return the first event (version 0)""") {
      useDomainEventLog { eventlog =>
        sleepBeforeEachTest.foreach(d => Thread.sleep(d.toMillis))
        val arId = theAlmhirt.getUuid
        val events = createEvents(arId, 100)
        val res =
          (for {
            _ <- (eventlog ? CommitDomainEvents(events))(defaultDuration).successfulAlmFuture[DomainEventLogMessage]
            _ <- sleepAfterInsertIfNeccessary()
            enumerator <- (eventlog ? GetDomainEventsUntil(arId, 1L))(defaultDuration).successfulAlmFuture[FetchedDomainEvents].map(_.enumerator)
            events <- enumerator.run(Iteratee.getChunks[DomainEvent]).toSuccessfulAlmFuture
          } yield events).awaitResultOrEscalate(defaultDuration)
        res should have size(1)
        res should equal(events.take(1))
        res.last.aggVersion should equal(0L)
      }
    }

    it("""should when queried with "GetDomainEventsUntil(x = maxVersion)" return all events except the last""") {
      useDomainEventLog { eventlog =>
        sleepBeforeEachTest.foreach(d => Thread.sleep(d.toMillis))
        val arId = theAlmhirt.getUuid
        val events = createEvents(arId, 100)
        val res =
          (for {
            _ <- (eventlog ? CommitDomainEvents(events))(defaultDuration).successfulAlmFuture[DomainEventLogMessage]
            _ <- sleepAfterInsertIfNeccessary()
            enumerator <- (eventlog ? GetDomainEventsUntil(arId, events.last.aggVersion))(defaultDuration).successfulAlmFuture[FetchedDomainEvents].map(_.enumerator)
            events <- enumerator.run(Iteratee.getChunks[DomainEvent]).toSuccessfulAlmFuture
          } yield events).awaitResultOrEscalate(defaultDuration)
        res should have size(events.init.size)
        res should equal(events.init)
        res.last.aggVersion should equal(events.last.aggVersion - 1)
      }
    }

    it("""should when queried with "GetDomainEventsUntil(x > maxVersion)" return all events""") {
      useDomainEventLog { eventlog =>
        sleepBeforeEachTest.foreach(d => Thread.sleep(d.toMillis))
        val arId = theAlmhirt.getUuid
        val events = createEvents(arId, 100)
        val res =
          (for {
            _ <- (eventlog ? CommitDomainEvents(events))(defaultDuration).successfulAlmFuture[DomainEventLogMessage]
            _ <- sleepAfterInsertIfNeccessary()
            enumerator <- (eventlog ? GetDomainEventsUntil(arId, events.last.aggVersion + 1))(defaultDuration).successfulAlmFuture[FetchedDomainEvents].map(_.enumerator)
            events <- enumerator.run(Iteratee.getChunks[DomainEvent]).toSuccessfulAlmFuture
          } yield events).awaitResultOrEscalate(defaultDuration)
        res should have size(events.size)
        res should equal(events)
      }
    }

    it("""should when queried with "GetDomainEventsUntil(0 < x < maxVersion)" return all events up to version x excluding the one with version x""") {
      useDomainEventLog { eventlog =>
        sleepBeforeEachTest.foreach(d => Thread.sleep(d.toMillis))
        val arId = theAlmhirt.getUuid
        val events = createEvents(arId, 100)
        val res =
          (for {
            _ <- (eventlog ? CommitDomainEvents(events))(defaultDuration).successfulAlmFuture[DomainEventLogMessage]
            _ <- sleepAfterInsertIfNeccessary()
            enumerator <- (eventlog ? GetDomainEventsUntil(arId, 10L))(defaultDuration).successfulAlmFuture[FetchedDomainEvents].map(_.enumerator)
            events <- enumerator.run(Iteratee.getChunks[DomainEvent]).toSuccessfulAlmFuture
          } yield events).awaitResultOrEscalate(defaultDuration)
        res should equal(events.take(10))
        res.last.aggVersion should equal(9L)
      }
    }

    it("""should when queried with "GetDomainEventsFromTo(0L, 0L)" return the first event""") {
      useDomainEventLog { eventlog =>
        sleepBeforeEachTest.foreach(d => Thread.sleep(d.toMillis))
        val arId = theAlmhirt.getUuid
        val events = createEvents(arId, 100)
        val res =
          (for {
            _ <- (eventlog ? CommitDomainEvents(events))(defaultDuration).successfulAlmFuture[DomainEventLogMessage]
            _ <- sleepAfterInsertIfNeccessary()
            enumerator <- (eventlog ? GetDomainEventsFromTo(arId, 0L, 0L))(defaultDuration).successfulAlmFuture[FetchedDomainEvents].map(_.enumerator)
            events <- enumerator.run(Iteratee.getChunks[DomainEvent]).toSuccessfulAlmFuture
          } yield events).awaitResultOrEscalate(defaultDuration)
        res should equal(events.take(1))
      }
    }

    it("""should when queried with "GetDomainEventsFromTo(0L, maxVersion)" return all events""") {
      useDomainEventLog { eventlog =>
        sleepBeforeEachTest.foreach(d => Thread.sleep(d.toMillis))
        val arId = theAlmhirt.getUuid
        val events = createEvents(arId, 100)
        val res =
          (for {
            _ <- (eventlog ? CommitDomainEvents(events))(defaultDuration).successfulAlmFuture[DomainEventLogMessage]
            _ <- sleepAfterInsertIfNeccessary()
            enumerator <- (eventlog ? GetDomainEventsFromTo(arId, 0L, events.last.aggVersion))(defaultDuration).successfulAlmFuture[FetchedDomainEvents].map(_.enumerator)
            events <- enumerator.run(Iteratee.getChunks[DomainEvent]).toSuccessfulAlmFuture
          } yield events).awaitResultOrEscalate(defaultDuration)
        res should equal(events)
      }
    }

    it("""should when queried with "GetDomainEventsFromTo(0L, x < maxVersion)" return all events up to the one including with version = x""") {
      useDomainEventLog { eventlog =>
        sleepBeforeEachTest.foreach(d => Thread.sleep(d.toMillis))
        val arId = theAlmhirt.getUuid
        val events = createEvents(arId, 100)
        val res =
          (for {
            _ <- (eventlog ? CommitDomainEvents(events))(defaultDuration).successfulAlmFuture[DomainEventLogMessage]
            _ <- sleepAfterInsertIfNeccessary()
            enumerator <- (eventlog ? GetDomainEventsFromTo(arId, 0L, 10L))(defaultDuration).successfulAlmFuture[FetchedDomainEvents].map(_.enumerator)
            events <- enumerator.run(Iteratee.getChunks[DomainEvent]).toSuccessfulAlmFuture
          } yield events).awaitResultOrEscalate(defaultDuration)
        res should have size(11)
        res should equal(events.take(11))
        res.head.aggVersion should equal(0L)
        res.last.aggVersion should equal(10L)
      }
    }

    it("""should when queried with "GetDomainEventsFromTo(0L,  x > maxVersion)" return all events""") {
      useDomainEventLog { eventlog =>
        sleepBeforeEachTest.foreach(d => Thread.sleep(d.toMillis))
        val arId = theAlmhirt.getUuid
        val events = createEvents(arId, 100)
        val res =
          (for {
            _ <- (eventlog ? CommitDomainEvents(events))(defaultDuration).successfulAlmFuture[DomainEventLogMessage]
            _ <- sleepAfterInsertIfNeccessary()
            enumerator <- (eventlog ? GetDomainEventsFromTo(arId, 0L, events.last.aggVersion + 1))(defaultDuration).successfulAlmFuture[FetchedDomainEvents].map(_.enumerator)
            events <- enumerator.run(Iteratee.getChunks[DomainEvent]).toSuccessfulAlmFuture
          } yield events).awaitResultOrEscalate(defaultDuration)
        res should have size(events.size)
        res should equal(events)
      }
    }

    it("""should when queried with "GetDomainEventsFromTo(x < maxVersion,  maxVersion)" return all events from including x to the end""") {
      useDomainEventLog { eventlog =>
        sleepBeforeEachTest.foreach(d => Thread.sleep(d.toMillis))
        val arId = theAlmhirt.getUuid
        val events = createEvents(arId, 100)
        val res =
          (for {
            _ <- (eventlog ? CommitDomainEvents(events))(defaultDuration).successfulAlmFuture[DomainEventLogMessage]
            _ <- sleepAfterInsertIfNeccessary()
            enumerator <- (eventlog ? GetDomainEventsFromTo(arId, 10L, events.last.aggVersion))(defaultDuration).successfulAlmFuture[FetchedDomainEvents].map(_.enumerator)
            events <- enumerator.run(Iteratee.getChunks[DomainEvent]).toSuccessfulAlmFuture
          } yield events).awaitResultOrEscalate(defaultDuration)
        res should equal(events.drop(10))
        res.head.aggVersion should equal(10L)
        res.last.aggVersion should equal(events.last.aggVersion)
      }
    }
    it("""should when queried with "GetDomainEventsFromTo(x < maxVersion,  y < maxVersion)" return all events from including x to the one including y""") {
      useDomainEventLog { eventlog =>
        sleepBeforeEachTest.foreach(d => Thread.sleep(d.toMillis))
        val arId = theAlmhirt.getUuid
        val events = createEvents(arId, 100)
        val res =
          (for {
            _ <- (eventlog ? CommitDomainEvents(events))(defaultDuration).successfulAlmFuture[DomainEventLogMessage]
            _ <- sleepAfterInsertIfNeccessary()
            enumerator <- (eventlog ? GetDomainEventsFromTo(arId, 10L, 20L))(defaultDuration).successfulAlmFuture[FetchedDomainEvents].map(_.enumerator)
            events <- enumerator.run(Iteratee.getChunks[DomainEvent]).toSuccessfulAlmFuture
          } yield events).awaitResultOrEscalate(defaultDuration)
        res should have size (11)
        res should equal(events.drop(10).take(11))
        res.head.aggVersion should equal(10L)
        res.last.aggVersion should equal(20L)
      }
    }
    it("""should when queried with "GetDomainEventsFromTo(0L x < maxVersion,  y = x)" return the event with version x""") {
      useDomainEventLog { eventlog =>
        sleepBeforeEachTest.foreach(d => Thread.sleep(d.toMillis))
        val arId = theAlmhirt.getUuid
        val events = createEvents(arId, 100)
        val res =
          (for {
            _ <- (eventlog ? CommitDomainEvents(events))(defaultDuration).successfulAlmFuture[DomainEventLogMessage]
            _ <- sleepAfterInsertIfNeccessary()
            enumerator <- (eventlog ? GetDomainEventsFromTo(arId, 10L, 10L))(defaultDuration).successfulAlmFuture[FetchedDomainEvents].map(_.enumerator)
            events <- enumerator.run(Iteratee.getChunks[DomainEvent]).toSuccessfulAlmFuture
          } yield events).awaitResultOrEscalate(defaultDuration)
        res should have size (1)
        res should equal(events.drop(10).take(1))
        res.head.aggVersion should equal(10L)
        res.last.aggVersion should equal(10L)
      }
    }

    it("""should when queried with "GetDomainEventsFromTo(0L x < maxVersion,  y < x)" return no events""") {
      useDomainEventLog { eventlog =>
        sleepBeforeEachTest.foreach(d => Thread.sleep(d.toMillis))
        val arId = theAlmhirt.getUuid
        val events = createEvents(arId, 100)
        val res =
          (for {
            _ <- (eventlog ? CommitDomainEvents(events))(defaultDuration).successfulAlmFuture[DomainEventLogMessage]
            _ <- sleepAfterInsertIfNeccessary()
            enumerator <- (eventlog ? GetDomainEventsFromTo(arId, 10L, 9L))(defaultDuration).successfulAlmFuture[FetchedDomainEvents].map(_.enumerator)
            events <- enumerator.run(Iteratee.getChunks[DomainEvent]).toSuccessfulAlmFuture
          } yield events).awaitResultOrEscalate(defaultDuration)
        res should equal(IndexedSeq.empty)
      }
    }

    // ---
    it("""should when queried with "GetDomainEventsFromUntil(0L, 0L)" return no event""") {
      useDomainEventLog { eventlog =>
        sleepBeforeEachTest.foreach(d => Thread.sleep(d.toMillis))
        val arId = theAlmhirt.getUuid
        val events = createEvents(arId, 100)
        val res =
          (for {
            _ <- (eventlog ? CommitDomainEvents(events))(defaultDuration).successfulAlmFuture[DomainEventLogMessage]
            _ <- sleepAfterInsertIfNeccessary()
            enumerator <- (eventlog ? GetDomainEventsFromUntil(arId, 0L, 0L))(defaultDuration).successfulAlmFuture[FetchedDomainEvents].map(_.enumerator)
            events <- enumerator.run(Iteratee.getChunks[DomainEvent]).toSuccessfulAlmFuture
          } yield events).awaitResultOrEscalate(defaultDuration)
        res should equal(Vector.empty)
      }
    }

    it("""should when queried with "GetDomainEventsFromUntil(0L, maxVersion)" return all events except the last""") {
      useDomainEventLog { eventlog =>
        sleepBeforeEachTest.foreach(d => Thread.sleep(d.toMillis))
        val arId = theAlmhirt.getUuid
        val events = createEvents(arId, 100)
        val res =
          (for {
            _ <- (eventlog ? CommitDomainEvents(events))(defaultDuration).successfulAlmFuture[DomainEventLogMessage]
            _ <- sleepAfterInsertIfNeccessary()
            enumerator <- (eventlog ? GetDomainEventsFromUntil(arId, 0L, events.last.aggVersion))(defaultDuration).successfulAlmFuture[FetchedDomainEvents].map(_.enumerator)
            events <- enumerator.run(Iteratee.getChunks[DomainEvent]).toSuccessfulAlmFuture
          } yield events).awaitResultOrEscalate(defaultDuration)
        res should equal(events.init)
        res.head.aggVersion should equal(0L)
        res.last.aggVersion should equal(events.last.aggVersion - 1L)
      }
    }

    it("""should when queried with "GetDomainEventsFromUntil(0L, x < maxVersion)" return all events up to the one excluding version = x""") {
      useDomainEventLog { eventlog =>
        sleepBeforeEachTest.foreach(d => Thread.sleep(d.toMillis))
        val arId = theAlmhirt.getUuid
        val events = createEvents(arId, 100)
        val res =
          (for {
            _ <- (eventlog ? CommitDomainEvents(events))(defaultDuration).successfulAlmFuture[DomainEventLogMessage]
            _ <- sleepAfterInsertIfNeccessary()
            enumerator <- (eventlog ? GetDomainEventsFromUntil(arId, 0L, 10L))(defaultDuration).successfulAlmFuture[FetchedDomainEvents].map(_.enumerator)
            events <- enumerator.run(Iteratee.getChunks[DomainEvent]).toSuccessfulAlmFuture
          } yield events).awaitResultOrEscalate(defaultDuration)
        res should have size (10)
        res should equal(events.take(10))
        res.head.aggVersion should equal(0L)
        res.last.aggVersion should equal(9L)
      }
    }

    it("""should when queried with "GetDomainEventsFromUntil(0L,  x > maxVersion)" return all events""") {
      useDomainEventLog { eventlog =>
        sleepBeforeEachTest.foreach(d => Thread.sleep(d.toMillis))
        val arId = theAlmhirt.getUuid
        val events = createEvents(arId, 100)
        val res =
          (for {
            _ <- (eventlog ? CommitDomainEvents(events))(defaultDuration).successfulAlmFuture[DomainEventLogMessage]
            _ <- sleepAfterInsertIfNeccessary()
            enumerator <- (eventlog ? GetDomainEventsFromUntil(arId, 0L, events.last.aggVersion + 1))(defaultDuration).successfulAlmFuture[FetchedDomainEvents].map(_.enumerator)
            events <- enumerator.run(Iteratee.getChunks[DomainEvent]).toSuccessfulAlmFuture
          } yield events).awaitResultOrEscalate(defaultDuration)
        res should equal(events)
      }
    }

    it("""should when queried with "GetDomainEventsFromUntil(x < maxVersion,  maxVersion)" return all events from including x to the end except the last one with maxVersion""") {
      useDomainEventLog { eventlog =>
        sleepBeforeEachTest.foreach(d => Thread.sleep(d.toMillis))
        val arId = theAlmhirt.getUuid
        val events = createEvents(arId, 100)
        val res =
          (for {
            _ <- (eventlog ? CommitDomainEvents(events))(defaultDuration).successfulAlmFuture[DomainEventLogMessage]
            _ <- sleepAfterInsertIfNeccessary()
            enumerator <- (eventlog ? GetDomainEventsFromUntil(arId, 10L, events.last.aggVersion))(defaultDuration).successfulAlmFuture[FetchedDomainEvents].map(_.enumerator)
            events <- enumerator.run(Iteratee.getChunks[DomainEvent]).toSuccessfulAlmFuture
          } yield events).awaitResultOrEscalate(defaultDuration)
        res should equal(events.drop(10).init)
        //        res.head.aggVersion should equal(10L)
        //        res.last.aggVersion should equal(events.last.aggVersion - 1L)
      }
    }
    it("""should when queried with "GetDomainEventsFromUntil(x < maxVersion,  y < maxVersion)" return all events from including x to the one excluding y""") {
      useDomainEventLog { eventlog =>
        sleepBeforeEachTest.foreach(d => Thread.sleep(d.toMillis))
        val arId = theAlmhirt.getUuid
        val events = createEvents(arId, 100)
        val res =
          (for {
            _ <- (eventlog ? CommitDomainEvents(events))(defaultDuration).successfulAlmFuture[DomainEventLogMessage]
            _ <- sleepAfterInsertIfNeccessary()
            enumerator <- (eventlog ? GetDomainEventsFromUntil(arId, 10L, 20L))(defaultDuration).successfulAlmFuture[FetchedDomainEvents].map(_.enumerator)
            events <- enumerator.run(Iteratee.getChunks[DomainEvent]).toSuccessfulAlmFuture
          } yield events).awaitResultOrEscalate(defaultDuration)
        res should equal(events.drop(10).take(10))
        res.head.aggVersion should equal(10L)
        res.last.aggVersion should equal(19L)
      }
    }
    it("""should when queried with "GetDomainEventsFromUntil(0L x < maxVersion,  y = x)" return no event""") {
      useDomainEventLog { eventlog =>
        sleepBeforeEachTest.foreach(d => Thread.sleep(d.toMillis))
        val arId = theAlmhirt.getUuid
        val events = createEvents(arId, 100)
        val res =
          (for {
            _ <- (eventlog ? CommitDomainEvents(events))(defaultDuration).successfulAlmFuture[DomainEventLogMessage]
            _ <- sleepAfterInsertIfNeccessary()
            enumerator <- (eventlog ? GetDomainEventsFromUntil(arId, 10L, 10L))(defaultDuration).successfulAlmFuture[FetchedDomainEvents].map(_.enumerator)
            events <- enumerator.run(Iteratee.getChunks[DomainEvent]).toSuccessfulAlmFuture
          } yield events).awaitResultOrEscalate(defaultDuration)
        res should equal(Seq.empty)
      }
    }

    it("""should when queried with "GetDomainEventsFromUntil(0L x < maxVersion,  y < x)" return no events""") {
      useDomainEventLog { eventlog =>
        sleepBeforeEachTest.foreach(d => Thread.sleep(d.toMillis))
        val arId = theAlmhirt.getUuid
        val events = createEvents(arId, 100)
        val res =
          (for {
            _ <- (eventlog ? CommitDomainEvents(events))(defaultDuration).successfulAlmFuture[DomainEventLogMessage]
            _ <- sleepAfterInsertIfNeccessary()
            enumerator <- (eventlog ? GetDomainEventsFromUntil(arId, 10L, 9L))(defaultDuration).successfulAlmFuture[FetchedDomainEvents].map(_.enumerator)
            events <- enumerator.run(Iteratee.getChunks[DomainEvent]).toSuccessfulAlmFuture
          } yield events).awaitResultOrEscalate(defaultDuration)
        res should equal(IndexedSeq.empty)
      }
    }

    it("""should when queried with "GetDomainEvent(existing id)" reply with QueriedDomainEvent(Some(theEvent))""") {
      useDomainEventLog { eventlog =>
        sleepBeforeEachTest.foreach(d => Thread.sleep(d.toMillis))
        val arId = theAlmhirt.getUuid
        val events = createEvents(arId, 100)
        val res =
          (for {
            _ <- (eventlog ? CommitDomainEvents(events))(defaultDuration).successfulAlmFuture[DomainEventLogMessage]
            _ <- sleepAfterInsertIfNeccessary()
            res <- (eventlog ? GetDomainEvent(events(10).id))(defaultDuration).successfulAlmFuture[QueriedDomainEvent]
          } yield res).awaitResultOrEscalate(defaultDuration)
        res should equal(QueriedDomainEvent(events(10).id, Some(events(10))))
      }
    }

    it("""should when queried with "GetDomainEvent(nonexisting id)" reply with QueriedDomainEvent(None)""") {
      useDomainEventLog { eventlog =>
        sleepBeforeEachTest.foreach(d => Thread.sleep(d.toMillis))
        val arId = theAlmhirt.getUuid
        val events = createEvents(arId, 100)
        val nonexistingid = theAlmhirt.getUuid
        val res =
          (for {
            _ <- (eventlog ? CommitDomainEvents(events))(defaultDuration).successfulAlmFuture[DomainEventLogMessage]
            _ <- sleepAfterInsertIfNeccessary()
            res <- (eventlog ? GetDomainEvent(nonexistingid))(defaultDuration).successfulAlmFuture[QueriedDomainEvent]
          } yield res).awaitResultOrEscalate(defaultDuration)
        res should equal(QueriedDomainEvent(nonexistingid, None))
      }
    }
  }

  private def createEvents(arId: java.util.UUID, n: Int): IndexedSeq[DomainEvent] = {
    val first = AR1Created(DomainEventHeader(AggregateRootRef(arId)), "a")
    val rest = for (n <- 1 until n) yield AR1BChanged(DomainEventHeader(AggregateRootRef(arId, n)), Some(n.toString))
    first +: rest
  }

}