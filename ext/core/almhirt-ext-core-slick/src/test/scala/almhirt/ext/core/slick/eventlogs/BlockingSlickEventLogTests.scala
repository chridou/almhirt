package almhirt.ext.core.slick.eventlogs

import org.scalatest._
import org.scalatest.matchers.MustMatchers
import scala.concurrent.duration._
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.core.Almhirt
import almhirt.almvalidation.kit._
import almhirt.environment._
import almhirt.eventlog._
import almhirt.domain._
import almhirt.core.test.BlockingRepoCoreBootstrapper
import almhirt.ext.core.riftwarp.RiftWarpBootstrapper
import almhirt.core.riftwarp.test.WithTestDecomposersAndRecomposersBootstrapper
import almhirt.core.test._
import almhirt.domain.AggregateRootRef
import com.typesafe.config.ConfigFactory
import almhirt.environment.configuration.Bootstrapper
import org.joda.time.DateTime
import almhirt.messaging.Message
import almhirt.core.Event
class BlockingSlickEventLogOnAlmhirtTests extends FunSuite with MustMatchers with AlmhirtTestKit {
  implicit object ccuad extends CanCreateUuidsAndDateTimes
  implicit val atMost = FiniteDuration(5, "s")
  def createConfig = {
    val aUniqueIdentifier = ccuad.getUuid
    val baseConfig =
      s"""| almhirt {
        |	systemname = "almhirt-testing-${aUniqueIdentifier.toString()}"
    	| 	eventlog {
    	|		eventlog_table = "SLICK_EVENTLOG${aUniqueIdentifier.toString()}"
    	|	  	blob_table = "SLICK_EVENTLOG_BLOBS${aUniqueIdentifier.toString()}"
    	|		log_domain_events = true
    	|	}
    	|	domaineventlog {
    	|		#disabled=true
    	|		eventlog_table = "SLICK_DOMAINEVENTLOG${aUniqueIdentifier.toString()}"
    	|		blob_table = "SLICK_DOMAINEVENTLOG_BLOBS${aUniqueIdentifier.toString()}"
    	|   }
    	|}	""".stripMargin
   ConfigFactory.parseString(baseConfig).withFallback(ConfigFactory.load)
  }
  val bootstrapper =
    new Bootstrapper with RiftWarpBootstrapper with BlockingRepoCoreBootstrapper with WithTestDecomposersAndRecomposersBootstrapper {
      val config = createConfig
    }

  def inLocalTestAlmhirt[T](compute: AlmhirtForExtendedTesting => T) =
    inExtendedTestAlmhirt(bootstrapper)(compute)

  val baseDateTime = new DateTime(2013, 1, 1, 0, 0)
  val events = (for (i <- 0 until 100) yield {
    new TestPersonCreated(DomainEventHeader(ccuad.getUuid, AggregateRootRef(ccuad.getUuid, i), baseDateTime.plusHours(i)), s"testEvent_$i")
  }).toVector

  val idsVector = events.map(_.header.id)

  def withIsolatedFilledEventLog[T](f: EventLog => AlmValidation[T]): AlmValidation[T] = {
    inLocalTestAlmhirt { almhirt =>
      for {
        eventlog <- almhirt.getService[EventLog]
        _ <- inTryCatch { events.foreach(eventlog.storeEvent(_)) }
        res <- f(eventlog)
      } yield res
    }
  }

  test("The eventlog must store the events") {
    val res = withIsolatedFilledEventLog(eventlog => true.success)
    println(res)
    res.isSuccess must be(true)
  }

  test("The eventlog must store and read the events") {
    withIsolatedFilledEventLog { eventlog =>
      val res = eventlog.getAllEvents.awaitResult
      res.map(events => events.map(_.header.id).toVector)
    }.forceResult must equal(idsVector)
  }

  test("The eventlog must return the first event when queried with its id") {
    withIsolatedFilledEventLog { eventlog =>
      val res = eventlog.getEvent(events.head.header.id).awaitResult
      res
    }.forceResult.header.id must equal(idsVector.head)
  }

  test("The eventlog fail when queried for an unknown id") {
    withIsolatedFilledEventLog { eventlog =>
      val res = eventlog.getEvent(ccuad.getUuid).awaitResult
      res
    }.isFailure must be(true)
  }

  test("The eventlog must return all events when from is before the first events timestamp") {
    withIsolatedFilledEventLog { eventlog =>
      val res = eventlog.getEventsFrom(events.head.header.timestamp.minusMillis(1)).awaitResult
      res.map(events => events.map(_.header.id).toVector)
    }.forceResult must equal(idsVector)
  }

  test("The eventlog must return all events when from is equal the first events timestamp") {
    withIsolatedFilledEventLog { eventlog =>
      val res = eventlog.getEventsFrom(events.head.header.timestamp).awaitResult
      res.map(events => events.map(_.header.id).toVector)
    }.forceResult must equal(idsVector)
  }

  test("The eventlog must return no events when from is after the last events timestamp") {
    withIsolatedFilledEventLog { eventlog =>
      val res = eventlog.getEventsFrom(events.last.header.timestamp.plusMillis(1)).awaitResult
      res.map(events => events.map(_.header.id).toVector)
    }.forceResult must equal(Vector.empty)
  }

  test("The eventlog must return the event when from is equal to the last events timestamp") {
    withIsolatedFilledEventLog { eventlog =>
      val res = eventlog.getEventsFrom(events.last.header.timestamp).awaitResult
      res.map(events => events.map(_.header.id).toVector)
    }.forceResult must equal(Vector(idsVector.last))
  }

  test("The eventlog must return all events when until is after the last events timestamp") {
    withIsolatedFilledEventLog { eventlog =>
      val res = eventlog.getEventsUntil(events.last.header.timestamp.plusMillis(1)).awaitResult
      res.map(events => events.map(_.header.id).toVector)
    }.forceResult must equal(idsVector)
  }

  test("The eventlog must return all events except the last when until is equal to the last events timestamp") {
    withIsolatedFilledEventLog { eventlog =>
      val res = eventlog.getEventsUntil(events.last.header.timestamp).awaitResult
      res.map(events => events.map(_.header.id).toVector)
    }.forceResult must equal(idsVector.take(idsVector.length - 1))
  }

  test("The eventlog must return no events when until is equal the first events timestamp") {
    withIsolatedFilledEventLog { eventlog =>
      val res = eventlog.getEventsUntil(events.head.header.timestamp).awaitResult
      res.map(events => events.map(_.header.id).toVector)
    }.forceResult must equal(Vector.empty)
  }

  test("The eventlog must return no events when until is before the first events timestamp") {
    withIsolatedFilledEventLog { eventlog =>
      val res = eventlog.getEventsUntil(events.head.header.timestamp.minusMillis(1)).awaitResult
      res.map(events => events.map(_.header.id).toVector)
    }.forceResult must equal(Vector.empty)
  }

  test("The eventlog must return all events when from is the first events timestamp and until is after the last events timestamp") {
    withIsolatedFilledEventLog { eventlog =>
      val res = eventlog.getEventsFromUntil(events.head.header.timestamp, events.last.header.timestamp.plusMillis(1)).awaitResult
      res.map(events => events.map(_.header.id).toVector)
    }.forceResult must equal(idsVector)
  }

  test("The eventlog must return all events except the last when from is the first events timestamp and until the last events timestamp") {
    withIsolatedFilledEventLog { eventlog =>
      val res = eventlog.getEventsFromUntil(events.head.header.timestamp, events.last.header.timestamp).awaitResult
      res.map(events => events.map(_.header.id).toVector)
    }.forceResult must equal(idsVector.take(idsVector.length - 1))
  }

  test("The eventlog must return all events except the first and last when from after the first events timestamp and until the last events timestamp") {
    withIsolatedFilledEventLog { eventlog =>
      val res = eventlog.getEventsFromUntil(events.head.header.timestamp.plusMillis(1), events.last.header.timestamp).awaitResult
      res.map(events => events.map(_.header.id).toVector)
    }.forceResult must equal(idsVector.take(idsVector.length - 1).drop(1))
  }

  test("The eventlog must return no events  when from is the last events events timestamp and until the first events timestamp") {
    withIsolatedFilledEventLog { eventlog =>
      val res = eventlog.getEventsFromUntil(events.last.header.timestamp, events.head.header.timestamp).awaitResult
      res.map(events => events.map(_.header.id).toVector)
    }.forceResult must equal(Vector.empty)
  }

  test("The eventlog must store the events via the eventsChannel and then return all events") {
    (inLocalTestAlmhirt { almhirt => 
      for {
        eventsChannel <- almhirt.getService[EventsChannel]
        eventlog <- almhirt.getService[EventLog]
        _ <- inTryCatch { events.foreach(event => eventsChannel.post(Message(event))); Thread.sleep(100) }
        res <- eventlog.getAllEvents.awaitResult.map(events => events.map(event => event.header.id).toVector)
      } yield res
    }).forceResult must equal(idsVector)
  }
  
  test("The eventlog must store the events via the messagehub and then return all events") {
    (inLocalTestAlmhirt { almhirt => 
      for {
        eventlog <- almhirt.getService[EventLog]
        _ <- inTryCatch { events.foreach(event => almhirt.publish(Message(event))); Thread.sleep(100) }
        res <- eventlog.getAllEvents.awaitResult.map(events => events.map(event => event.header.id).toVector)
      } yield res
    }).forceResult must equal(idsVector)
  }
  
}