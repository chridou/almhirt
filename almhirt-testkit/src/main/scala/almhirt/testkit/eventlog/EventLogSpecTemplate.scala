package almhirt.testkit.eventlog

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSpec
import akka.testkit.TestProbe
import almhirt.testkit._
import almhirt.testkit.testevents._
import akka.actor._
import akka.pattern._
import akka.util.Timeout
import almhirt.almvalidation.kit._
import almhirt.almfuture.all._
import almhirt.core.HasAlmhirt
import almhirt.common._
import org.joda.time.LocalDateTime

abstract class EventLogSpecTemplate(theActorSystem: ActorSystem)
  extends AlmhirtTestKit(theActorSystem)
  with FunSpec
  with ShouldMatchers { self: CreatesEventLog =>

  import almhirt.eventlog.EventLog._  
  import almhirt.eventlog.EventLog.EventLogMessage  

  implicit def execContext = theAlmhirt.futuresExecutor
  
  def sleepMillisAfterWrite: Option[Int]
  
  def useEventLog[T](f: ActorRef => T): T = {
    val testId = nextTestId
    val (eventlog, eventLogCleanUp) = createEventLog(testId)
    try {
      val res = f(eventlog)
      system.stop(eventlog)
      eventLogCleanUp()
      res
    } catch {
      case exn: Exception =>
        system.stop(eventlog)
        eventLogCleanUp()
        throw exn
    }
  }
  
  lazy val uuids = (for(n <- 0 until 1000) yield theAlmhirt.getUuid).toVector
  
  val refTimestamp = LocalDateTime.parse("2013-12-31T0:00:00.000")
  
  def createEvents(n: Int, start: LocalDateTime, intervalInHours: Int): Vector[TestEvent] = {
    def getNextTimeStamp(n: Int): LocalDateTime = start.plusHours((n-1)*intervalInHours)
    val createFuncs: Vector[Int => TestEvent] =
      Vector(
          x => TestEvent1(EventHeader(uuids(x), getNextTimeStamp(x))),
          x => TestEvent2(EventHeader(uuids(x), getNextTimeStamp(x)), x),
          x => TestEvent3(EventHeader(uuids(x), getNextTimeStamp(x)), (for(c <- 1 to x) yield (c*c)).toVector))
    def nextEvent(n: Int) = createFuncs(n%3)(n)
    (for(x <- 1 to n) yield nextEvent(x)).toVector
  }

  protected def waitSomeTime() {
    sleepMillisAfterWrite.foreach(t => Thread.sleep(t))
  }
  
  describe("An event log") {
    it("""should accept an event and return a QueriedEvent(Some(event)) when queried with GetEvent""") {
      useEventLog { eventlog =>
      	val event = TestEvent1(EventHeader())
        eventlog ! LogEvent(event)
        waitSomeTime()
        val res = (eventlog ? GetEvent(event.eventId))(defaultDuration).successfulAlmFuture[EventLogMessage].awaitResultOrEscalate(defaultDuration)
        res should equal(QueriedEvent(event.eventId, Some(event)))
      }
    }
    it("""should return a QueriedEvent(None) when queried with GetEvent for a non existing event""") {
      useEventLog { eventlog =>
      	val event = TestEvent1(EventHeader())
        val res = (eventlog ? GetEvent(event.eventId))(defaultDuration).successfulAlmFuture[EventLogMessage].awaitResultOrEscalate(defaultDuration)
        res should equal(QueriedEvent(event.eventId, None))
      }
    }
    it("""should accept an event and give it back as the only event when queried with GetAllEvents""") {
      useEventLog { eventlog =>
      	val event = TestEvent1(EventHeader())
        eventlog ! LogEvent(event)
        waitSomeTime()
        val res = (eventlog ? GetAllEvents)(defaultDuration).successfulAlmFuture[EventLogMessage].awaitResultOrEscalate(defaultDuration)
        res should equal(EventsChunk(0, true, Vector(event)))
      }
    }
    it("""should accept many events and return the same number of events logged when queired with GetAllEvents""") {
      useEventLog { eventlog =>
        val baseTime = LocalDateTime.now()
      	val events = for(n <- 1 to 100) yield TestEvent2(EventHeader(theAlmhirt.getUuid, baseTime.plusMillis(n-1)), n)
        events foreach(eventlog ! LogEvent(_))
        waitSomeTime()
        val res = (eventlog ? GetAllEvents)(defaultDuration).successfulAlmFuture[EventsChunk].awaitResultOrEscalate(defaultDuration)
        res.events should have size(events.size)
      }
    }
    it("""should accept many events and return the same timestamps as the logged events when queried with GetAllEvents""") {
      useEventLog { eventlog =>
        val baseTime = LocalDateTime.now()
      	val events = for(n <- 1 to 100) yield TestEvent2(EventHeader(theAlmhirt.getUuid, baseTime.plusMillis(n-1)), n)
        events foreach(eventlog ! LogEvent(_))
        waitSomeTime()
        val res = (eventlog ? GetAllEvents)(defaultDuration).successfulAlmFuture[EventsChunk].awaitResultOrEscalate(defaultDuration)
        res.events.map(_.timestamp).toSet should equal(events.map(_.timestamp).toSet)
      }
    }
    it("""should accept many events and return all events in the correct order when queried with GetAllEvents""") {
      useEventLog { eventlog =>
        val baseTime = LocalDateTime.now()
      	val events = for(n <- 1 to 100) yield TestEvent2(EventHeader(theAlmhirt.getUuid, baseTime.plusMillis(n-1)), n)
        events foreach(eventlog ! LogEvent(_))
        waitSomeTime()
        val res = (eventlog ? GetAllEvents)(defaultDuration).successfulAlmFuture[EventLogMessage].awaitResultOrEscalate(defaultDuration)
        res should equal(EventsChunk(0, true, events))
      }
    }
    it("""should accept many events and return a QueriedEvent(Some(event)) when queried with GetEvent""") {
      useEventLog { eventlog =>
      	val events = for(n <- 1 to 100) yield TestEvent2(EventHeader(), n)
        events foreach(eventlog ! LogEvent(_))
        waitSomeTime()
        val res = (eventlog ? GetEvent(events(10).eventId))(defaultDuration).successfulAlmFuture[EventLogMessage].awaitResultOrEscalate(defaultDuration)
        res should equal(QueriedEvent(events(10).eventId, Some(events(10))))
      }
    }
    it("""should accept many events and return a QueriedEvent(None) when queried with GetEvent for a non existing event""") {
      useEventLog { eventlog =>
      	val events = for(n <- 1 to 100) yield TestEvent2(EventHeader(), n)
        events.init.foreach(eventlog ! LogEvent(_))
        waitSomeTime()
        val res = (eventlog ? GetEvent(events.last.eventId))(defaultDuration).successfulAlmFuture[EventLogMessage].awaitResultOrEscalate(defaultDuration)
        res should equal(QueriedEvent(events.last.eventId, None))
      }
    }
    it("""should accept many events ordered by their timestamps in reverse order and still return all events in the correct order by timestamp when queried with GetAllEvents""") {
      useEventLog { eventlog =>
      	val events = createEvents(100, refTimestamp, 1)
        events.reverse foreach(eventlog ! LogEvent(_))
        waitSomeTime()
        val res = (eventlog ? GetAllEvents)(defaultDuration).successfulAlmFuture[EventLogMessage].awaitResultOrEscalate(defaultDuration)
        res should equal(EventsChunk(0, true, events))
      }
    }
    
    // GetEventsFrom

    it("""should return the all events when queried with GetEventsFrom(x < minTimestamp)""") {
      useEventLog { eventlog =>
      	val events = createEvents(100, refTimestamp, 12)
        events foreach(eventlog ! LogEvent(_))
        waitSomeTime()
        val res = (eventlog ? GetEventsFrom(refTimestamp.minusMillis(10)))(defaultDuration).successfulAlmFuture[EventLogMessage].awaitResultOrEscalate(defaultDuration)
        res should equal(EventsChunk(0, true, events))
      }
    }

    it("""should return the correct events when queried with GetEventsFrom(x == minTimestamp)""") {
      useEventLog { eventlog =>
      	val events = createEvents(100, refTimestamp, 12)
        events foreach(eventlog ! LogEvent(_))
        waitSomeTime()
        val res = (eventlog ? GetEventsFrom(refTimestamp))(defaultDuration).successfulAlmFuture[EventLogMessage].awaitResultOrEscalate(defaultDuration)
        res should equal(EventsChunk(0, true, events))
      }
    }
    
    it("""should return the correct events when queried with GetEventsFrom(x > minTimestamp)""") {
      useEventLog { eventlog =>
      	val events = createEvents(100, refTimestamp, 12)
        events foreach(eventlog ! LogEvent(_))
        waitSomeTime()
        val res = (eventlog ? GetEventsFrom(refTimestamp.plusHours(24)))(defaultDuration).successfulAlmFuture[EventLogMessage].awaitResultOrEscalate(defaultDuration)
        res should equal(EventsChunk(0, true, events.drop(2)))
      }
    }
 
    it("""should return the last event when queried with GetEventsFrom(x == maxTimestamp)""") {
      useEventLog { eventlog =>
      	val events = createEvents(100, refTimestamp, 12)
        events foreach(eventlog ! LogEvent(_))
        waitSomeTime()
        val res = (eventlog ? GetEventsFrom(events.last.timestamp))(defaultDuration).successfulAlmFuture[EventLogMessage].awaitResultOrEscalate(defaultDuration)
        res should equal(EventsChunk(0, true, Vector(events.last)))
      }
    }

    it("""should return no event when queried with GetEventsFrom(x > maxTimestamp)""") {
      useEventLog { eventlog =>
      	val events = createEvents(100, refTimestamp, 12)
        events foreach(eventlog ! LogEvent(_))
        waitSomeTime()
        val res = (eventlog ? GetEventsFrom(events.last.timestamp.plusMillis(10)))(defaultDuration).successfulAlmFuture[EventLogMessage].awaitResultOrEscalate(defaultDuration)
        res should equal(EventsChunk(0, true, Vector.empty))
      }
    }

    // GetEventsAfter

    it("""should return the all events when queried with GetEventsAfter(x < minTimestamp)""") {
      useEventLog { eventlog =>
      	val events = createEvents(100, refTimestamp, 12)
        events foreach(eventlog ! LogEvent(_))
        waitSomeTime()
        val res = (eventlog ? GetEventsAfter(refTimestamp.minusMillis(10)))(defaultDuration).successfulAlmFuture[EventLogMessage].awaitResultOrEscalate(defaultDuration)
        res should equal(EventsChunk(0, true, events))
      }
    }

    it("""should return the all events except the first when queried with GetEventsAfter(x == minTimestamp)""") {
      useEventLog { eventlog =>
      	val events = createEvents(100, refTimestamp, 12)
        events foreach(eventlog ! LogEvent(_))
        waitSomeTime()
        val res = (eventlog ? GetEventsAfter(refTimestamp))(defaultDuration).successfulAlmFuture[EventLogMessage].awaitResultOrEscalate(defaultDuration)
        res should equal(EventsChunk(0, true, events.tail))
      }
    }
    
    it("""should return the correct events when queried with GetEventsAfter(x > minTimestamp)""") {
      useEventLog { eventlog =>
      	val events = createEvents(100, refTimestamp, 12)
        events foreach(eventlog ! LogEvent(_))
        waitSomeTime()
        val res = (eventlog ? GetEventsAfter(refTimestamp.plusHours(24)))(defaultDuration).successfulAlmFuture[EventLogMessage].awaitResultOrEscalate(defaultDuration)
        res should equal(EventsChunk(0, true, events.drop(3)))
      }
    }
 
    it("""should return no event when queried with GetEventsAfter(x == maxTimestamp)""") {
      useEventLog { eventlog =>
      	val events = createEvents(100, refTimestamp, 12)
        events foreach(eventlog ! LogEvent(_))
        waitSomeTime()
        val res = (eventlog ? GetEventsAfter(events.last.timestamp))(defaultDuration).successfulAlmFuture[EventLogMessage].awaitResultOrEscalate(defaultDuration)
        res should equal(EventsChunk(0, true, Vector.empty))
      }
    }

    it("""should return no event when queried with GetEventsAfter(x > maxTimestamp)""") {
      useEventLog { eventlog =>
      	val events = createEvents(100, refTimestamp, 12)
        events foreach(eventlog ! LogEvent(_))
        waitSomeTime()
        val res = (eventlog ? GetEventsAfter(events.last.timestamp.plusMillis(10)))(defaultDuration).successfulAlmFuture[EventLogMessage].awaitResultOrEscalate(defaultDuration)
        res should equal(EventsChunk(0, true, Vector.empty))
      }
    }

    // GetEventsTo

    it("""should no event when queried with GetEventsTo(x < minTimestamp)""") {
      useEventLog { eventlog =>
      	val events = createEvents(100, refTimestamp, 12)
        events foreach(eventlog ! LogEvent(_))
        waitSomeTime()
        val res = (eventlog ? GetEventsTo(refTimestamp.minusMillis(10)))(defaultDuration).successfulAlmFuture[EventLogMessage].awaitResultOrEscalate(defaultDuration)
        res should equal(EventsChunk(0, true, Vector.empty))
      }
    }

    it("""should return the first event when queried with GetEventsTo(x == minTimestamp)""") {
      useEventLog { eventlog =>
      	val events = createEvents(100, refTimestamp, 12)
        events foreach(eventlog ! LogEvent(_))
        waitSomeTime()
        val res = (eventlog ? GetEventsTo(refTimestamp))(defaultDuration).successfulAlmFuture[EventLogMessage].awaitResultOrEscalate(defaultDuration)
        res should equal(EventsChunk(0, true, Vector(events.head)))
      }
    }
    
    it("""should return the correct events when queried with GetEventsTo(x > minTimestamp)""") {
      useEventLog { eventlog =>
      	val events = createEvents(100, refTimestamp, 12)
        events foreach(eventlog ! LogEvent(_))
        waitSomeTime()
        val res = (eventlog ? GetEventsTo(refTimestamp.plusHours(24)))(defaultDuration).successfulAlmFuture[EventLogMessage].awaitResultOrEscalate(defaultDuration)
        res should equal(EventsChunk(0, true, events.take(3)))
      }
    }
 
    it("""should return all events when queried with GetEventsTo(x == maxTimestamp)""") {
      useEventLog { eventlog =>
      	val events = createEvents(100, refTimestamp, 12)
        events foreach(eventlog ! LogEvent(_))
        waitSomeTime()
        val res = (eventlog ? GetEventsTo(events.last.timestamp))(defaultDuration).successfulAlmFuture[EventLogMessage].awaitResultOrEscalate(defaultDuration)
        res should equal(EventsChunk(0, true, events))
      }
    }

    it("""should return all events when queried with GetEventsTo(x > maxTimestamp)""") {
      useEventLog { eventlog =>
      	val events = createEvents(100, refTimestamp, 12)
        events foreach(eventlog ! LogEvent(_))
        waitSomeTime()
        val res = (eventlog ? GetEventsTo(events.last.timestamp.plusMillis(10)))(defaultDuration).successfulAlmFuture[EventLogMessage].awaitResultOrEscalate(defaultDuration)
        res should equal(EventsChunk(0, true, events))
      }
    }

    // GetEventsUntil

    it("""should no event when queried with GetEventsUntil(x < minTimestamp)""") {
      useEventLog { eventlog =>
      	val events = createEvents(100, refTimestamp, 12)
        events foreach(eventlog ! LogEvent(_))
        waitSomeTime()
        val res = (eventlog ? GetEventsUntil(refTimestamp.minusMillis(10)))(defaultDuration).successfulAlmFuture[EventLogMessage].awaitResultOrEscalate(defaultDuration)
        res should equal(EventsChunk(0, true, Vector.empty))
      }
    }

    it("""should return no event when queried with GetEventsUntil(x == minTimestamp)""") {
      useEventLog { eventlog =>
      	val events = createEvents(100, refTimestamp, 12)
        events foreach(eventlog ! LogEvent(_))
        waitSomeTime()
        val res = (eventlog ? GetEventsUntil(refTimestamp))(defaultDuration).successfulAlmFuture[EventLogMessage].awaitResultOrEscalate(defaultDuration)
        res should equal(EventsChunk(0, true, Vector.empty))
      }
    }
    
    it("""should return the correct events when queried with GetEventsUntil(x > minTimestamp)""") {
      useEventLog { eventlog =>
      	val events = createEvents(100, refTimestamp, 12)
        events foreach(eventlog ! LogEvent(_))
        waitSomeTime()
        val res = (eventlog ? GetEventsUntil(refTimestamp.plusHours(24)))(defaultDuration).successfulAlmFuture[EventLogMessage].awaitResultOrEscalate(defaultDuration)
        res should equal(EventsChunk(0, true, events.take(2)))
      }
    }
 
    it("""should return all events except the last when queried with GetEventsUntil(x == maxTimestamp)""") {
      useEventLog { eventlog =>
      	val events = createEvents(100, refTimestamp, 12)
        events foreach(eventlog ! LogEvent(_))
        waitSomeTime()
        val res = (eventlog ? GetEventsUntil(events.last.timestamp))(defaultDuration).successfulAlmFuture[EventLogMessage].awaitResultOrEscalate(defaultDuration)
        res should equal(EventsChunk(0, true, events.init))
      }
    }

    it("""should return all events when queried with GetEventsUntil(x > maxTimestamp)""") {
      useEventLog { eventlog =>
      	val events = createEvents(100, refTimestamp, 12)
        events foreach(eventlog ! LogEvent(_))
        waitSomeTime()
        val res = (eventlog ? GetEventsUntil(events.last.timestamp.plusMillis(10)))(defaultDuration).successfulAlmFuture[EventLogMessage].awaitResultOrEscalate(defaultDuration)
        res should equal(EventsChunk(0, true, events))
      }
    }
    
  }
}