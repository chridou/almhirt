package almhirt.streaming

import scala.language.postfixOps
import scala.concurrent.duration._
import akka.actor._
import akka.stream.scaladsl.{ Flow }
import akka.stream.{ FlowMaterializer, MaterializerSettings }
import almhirt.common._
import akka.testkit._
import org.scalatest._

class EventStreamTests(_system: ActorSystem) extends TestKit(_system) with fixture.WordSpecLike with Matchers with BeforeAndAfterAll {
  def this() = this(ActorSystem("EventStreamTests", almhirt.TestConfigs.logWarningConfig))

  implicit val executionContext = system.dispatchers.defaultGlobalDispatcher
  implicit val ccuad = CanCreateUuidsAndDateTimes()

  case class TestEvent(header: EventHeader) extends Event
  object TestEvent {
    def apply(id: String): TestEvent = TestEvent(EventHeader(EventId(id)))
  }
  case class TestDomainEvent(header: EventHeader) extends DomainEvent
  object TestDomainEvent {
    def apply(id: String): TestDomainEvent = TestDomainEvent(EventHeader(EventId(id)))
  }
  case class TestSystemEvent(header: EventHeader) extends SystemEvent
  object TestSystemEvent {
    def apply(id: String): TestSystemEvent = TestSystemEvent(EventHeader(EventId(id)))
  }

  val nMsgBig = 100000

  "The AlmhirtStreams" when {
    "accessed via a contractor" should {
      "dispatch an event on the event stream" in { fixture ⇒
        val FixtureParam(streams) = fixture
        val consumerProbeEvent = TestProbe()

        val event = TestEvent("a")
        val consumer = DelegatingEventConsumer[Event](consumerProbeEvent.ref)
        within(1 second) {
          streams.eventStream.produceTo(consumer)
          Stillage(List[Event](event)).signContract(streams.eventBroker)
          consumerProbeEvent.expectMsg(100 millis, event)
        }
      }

      "dispatch two events from two producers on the event stream" in { fixture ⇒
        val FixtureParam(streams) = fixture
        val consumerProbeEvent = TestProbe()

        val event1 = TestEvent("a")
        val event2 = TestEvent("b")
        val consumer = DelegatingEventConsumer[Event](consumerProbeEvent.ref)
        within(1 second) {
          streams.eventStream.produceTo(consumer)
          Stillage(List[Event](event1)).signContract(streams.eventBroker)
          Stillage(List[Event](event2)).signContract(streams.eventBroker)
          consumerProbeEvent.expectMsgAllOf(100 millis, event1, event2)
        }
      }

      "dispatch an event to 2 consumers on the event stream" in { fixture ⇒
        val FixtureParam(streams) = fixture
        val consumerProbeEvent1 = TestProbe()
        val consumerProbeEvent2 = TestProbe()

        val event = TestEvent("a")
        val consumer1 = DelegatingEventConsumer[Event](consumerProbeEvent1.ref)
        val consumer2 = DelegatingEventConsumer[Event](consumerProbeEvent2.ref)
        within(1 second) {
          streams.eventStream.produceTo(consumer1)
          streams.eventStream.produceTo(consumer2)
          Stillage(List[Event](event)).signContract(streams.eventBroker)
          consumerProbeEvent1.expectMsg(100 millis, event)
          consumerProbeEvent2.expectMsg(100 millis, event)
        }
      }

      "NOT dispatch an event on the system event stream" in { fixture ⇒
        val FixtureParam(streams) = fixture
        val consumerProbeEvent = TestProbe()

        val event = TestEvent("a")
        val consumer = DelegatingEventConsumer[SystemEvent](consumerProbeEvent.ref)
        within(1 second) {
          streams.systemEventStream.produceTo(consumer)
          Stillage(List[Event](event)).signContract(streams.eventBroker)
          consumerProbeEvent.expectNoMsg(100 millis)
        }
      }

      "NOT dispatch an event on the domain event stream" in { fixture ⇒
        val FixtureParam(streams) = fixture
        val consumerProbeEvent = TestProbe()

        val event = TestEvent("a")
        val consumer = DelegatingEventConsumer[DomainEvent](consumerProbeEvent.ref)
        within(1 second) {
          streams.domainEventStream.produceTo(consumer)
          Stillage(List[Event](event)).signContract(streams.eventBroker)
          consumerProbeEvent.expectNoMsg(100 millis)
        }
      }

      "dispatch a system event on the event stream" in { fixture ⇒
        val FixtureParam(streams) = fixture
        val consumerProbeEvent = TestProbe()

        val event = TestSystemEvent("a")
        val consumer = DelegatingEventConsumer[Event](consumerProbeEvent.ref)
        within(1 second) {
          streams.eventStream.produceTo(consumer)
          Stillage(List[Event](event)).signContract(streams.eventBroker)
          consumerProbeEvent.expectMsg(100 millis, event)
        }
      }

      "dispatch a system event on the system event stream" in { fixture ⇒
        val FixtureParam(streams) = fixture
        val consumerProbeEvent = TestProbe()

        val event = TestSystemEvent("a")
        val consumer = DelegatingEventConsumer[SystemEvent](consumerProbeEvent.ref)
        within(1 second) {
          streams.systemEventStream.produceTo(consumer)
          Stillage(List[Event](event)).signContract(streams.eventBroker)
          consumerProbeEvent.expectMsg(100 millis, event)
        }
      }

      "NOT dispatch a system event on the domain event stream" in { fixture ⇒
        val FixtureParam(streams) = fixture
        val consumerProbeEvent = TestProbe()

        val event = TestSystemEvent("a")
        val consumer = DelegatingEventConsumer[DomainEvent](consumerProbeEvent.ref)
        within(1 second) {
          streams.domainEventStream.produceTo(consumer)
          Stillage(List[Event](event)).signContract(streams.eventBroker)
          consumerProbeEvent.expectNoMsg(100 millis)
        }
      }

      "dispatch a domain event on the event stream" in { fixture ⇒
        val FixtureParam(streams) = fixture
        val consumerProbeEvent = TestProbe()

        val event = TestDomainEvent("a")
        val consumer = DelegatingEventConsumer[Event](consumerProbeEvent.ref)
        within(1 second) {
          streams.eventStream.produceTo(consumer)
          Stillage(List[Event](event)).signContract(streams.eventBroker)
          consumerProbeEvent.expectMsg(100 millis, event)
        }
      }

      "NOT dispatch a domain event on the system event stream" in { fixture ⇒
        val FixtureParam(streams) = fixture
        val consumerProbeEvent = TestProbe()

        val event = TestDomainEvent("a")
        val consumer = DelegatingEventConsumer[SystemEvent](consumerProbeEvent.ref)
        within(1 second) {
          streams.systemEventStream.produceTo(consumer)
          Stillage(List[Event](event)).signContract(streams.eventBroker)
          consumerProbeEvent.expectNoMsg(100 millis)
        }
      }

      "dispatch a domain event on the domain event stream" in { fixture ⇒
        val FixtureParam(streams) = fixture
        val consumerProbeEvent = TestProbe()

        val event = TestDomainEvent("a")
        val consumer = DelegatingEventConsumer[DomainEvent](consumerProbeEvent.ref)
        within(1 second) {
          streams.domainEventStream.produceTo(consumer)
          Stillage(List[Event](event)).signContract(streams.eventBroker)
          consumerProbeEvent.expectMsg(100 millis, event)
        }
      }

      s"dispatch many(${nMsgBig * 3}) events on the event stream" in { fixture ⇒
        val FixtureParam(streams) = fixture
        val probe2 = TestProbe()

        val n = nMsgBig * 3
        val events = (1 to n).map(i ⇒ TestEvent(i.toString): Event).toVector
        val consumer = DelegatingEventConsumer[Event](probe2.ref)
        val start = Deadline.now
        within(6 seconds) {
          streams.eventStream.produceTo(consumer)
          Stillage(events).signContract(streams.eventBroker)
          val res = probe2.receiveN(n, 5 seconds)
          val time = start.lap
          info(s"Dispatched $n in ${start.lap.defaultUnitString}((${(nMsgBig * 3 * 1000).toDouble / time.toMillis}/s)).")
          res should equal(events)
        }
      }

      s"dispatch many events(${nMsgBig * 3}) of different kinds on the matching streams" in { fixture ⇒
        val FixtureParam(streams) = fixture
        val probeEvent = TestProbe()
        val probeSystemEvent = TestProbe()
        val probeDomainEvent = TestProbe()

        val n = nMsgBig * 3
        val events = (1 to n).map(i ⇒
          if (i % 3 == 0) {
            TestEvent(i.toString)
          } else if (i % 3 == 1) {
            TestSystemEvent(i.toString)
          } else {
            TestDomainEvent(i.toString)
          }: Event).toVector
        val consumerEvent = DelegatingEventConsumer[Event](probeEvent.ref)
        val consumerSystemEvent = DelegatingEventConsumer[SystemEvent](probeSystemEvent.ref)
        val consumerDomainEvent = DelegatingEventConsumer[DomainEvent](probeDomainEvent.ref)
        val start = Deadline.now
        within(6 seconds) {
          streams.eventStream.produceTo(consumerEvent)
          streams.systemEventStream.produceTo(consumerSystemEvent)
          streams.domainEventStream.produceTo(consumerDomainEvent)
          Stillage(events).signContract(streams.eventBroker)
          val resEvent = probeEvent.receiveN(n, 5 seconds)
          val resSystemEvent = probeSystemEvent.receiveN(n / 3, 5 seconds)
          val resDomainEvent = probeDomainEvent.receiveN(n / 3, 5 seconds)
          val time = start.lap
          info(s"Dispatched $n in ${start.lap.defaultUnitString}((${(nMsgBig * 3 * 1000).toDouble / time.toMillis}/s)).")
          resEvent should equal(events)
          resSystemEvent should equal(events.collect { case m: SystemEvent ⇒ m })
          resDomainEvent should equal(events.collect { case m: DomainEvent ⇒ m })
        }
      }

      val nContractors = 10
      s"dispatch many events(${nMsgBig * 3}) of different kinds from many($nContractors) contractors on the matching streams" in { fixture ⇒
        val FixtureParam(streams) = fixture
        val probeEvent = TestProbe()
        val probeSystemEvent = TestProbe()
        val probeDomainEvent = TestProbe()

        val n = nMsgBig * 3
        val events = (1 to n).map(i ⇒
          if (i % 3 == 0) {
            TestEvent(f"$i%07d")
          } else if (i % 3 == 1) {
            TestSystemEvent(f"$i%07d")
          } else {
            TestDomainEvent(f"$i%07d")
          }: Event).toVector
        val parts = events.grouped(n / nContractors)
        val consumerEvent = DelegatingEventConsumer[Event](probeEvent.ref)
        val consumerSystemEvent = DelegatingEventConsumer[SystemEvent](probeSystemEvent.ref)
        val consumerDomainEvent = DelegatingEventConsumer[DomainEvent](probeDomainEvent.ref)

        var resEvent: Seq[Any] = null
        var resSystemEvent: Seq[Any] = null
        var resDomainEvent: Seq[Any] = null
        val start = Deadline.now
        within(6 seconds) {
          streams.eventStream.produceTo(consumerEvent)
          streams.systemEventStream.produceTo(consumerSystemEvent)
          streams.domainEventStream.produceTo(consumerDomainEvent)
          parts.foreach(Stillage(_).signContract(streams.eventBroker))
          resEvent = probeEvent.receiveN(n, 5 seconds)
          resSystemEvent = probeSystemEvent.receiveN(n / 3, 5 seconds)
          resDomainEvent = probeDomainEvent.receiveN(n / 3, 5 seconds)
          val time = start.lap
          info(s"Dispatched $n in ${start.lap.defaultUnitString}((${(nMsgBig * 3 * 1000).toDouble / time.toMillis}/s)).")
        }
        resEvent.map(_.asInstanceOf[Event]).sortBy(_.eventId.value) should equal(events)
        resSystemEvent.map(_.asInstanceOf[SystemEvent]).sortBy(_.eventId.value) should equal(events.collect { case m: SystemEvent ⇒ m })
        resDomainEvent.map(_.asInstanceOf[DomainEvent]).sortBy(_.eventId.value) should equal(events.collect { case m: DomainEvent ⇒ m })
      }

      s"dispatch many events(${nMsgBig * 3}) of different kinds from really many(${nContractors * 10}) contractors on the matching streams" in { fixture ⇒
        val FixtureParam(streams) = fixture
        val probeEvent = TestProbe()
        val probeSystemEvent = TestProbe()
        val probeDomainEvent = TestProbe()

        val n = nMsgBig * 3
        val events = (1 to n).map(i ⇒
          if (i % 3 == 0) {
            TestEvent(f"$i%07d")
          } else if (i % 3 == 1) {
            TestSystemEvent(f"$i%07d")
          } else {
            TestDomainEvent(f"$i%07d")
          }: Event).toVector
        val parts = events.grouped(n / (nContractors * 10))
        val consumerEvent = DelegatingEventConsumer[Event](probeEvent.ref)
        val consumerSystemEvent = DelegatingEventConsumer[SystemEvent](probeSystemEvent.ref)
        val consumerDomainEvent = DelegatingEventConsumer[DomainEvent](probeDomainEvent.ref)

        var resEvent: Seq[Any] = null
        var resSystemEvent: Seq[Any] = null
        var resDomainEvent: Seq[Any] = null
        val start = Deadline.now
        within(6 seconds) {
          streams.eventStream.produceTo(consumerEvent)
          streams.systemEventStream.produceTo(consumerSystemEvent)
          streams.domainEventStream.produceTo(consumerDomainEvent)
          parts.foreach(Stillage(_).signContract(streams.eventBroker))
          resEvent = probeEvent.receiveN(n, 5 seconds)
          resSystemEvent = probeSystemEvent.receiveN(n / 3, 5 seconds)
          resDomainEvent = probeDomainEvent.receiveN(n / 3, 5 seconds)
          val time = start.lap
          info(s"Dispatched $n in ${start.lap.defaultUnitString}((${(nMsgBig * 3 * 1000).toDouble / time.toMillis}/s)).")
        }
        resEvent.map(_.asInstanceOf[Event]).sortBy(_.eventId.value) should equal(events)
        resSystemEvent.map(_.asInstanceOf[SystemEvent]).sortBy(_.eventId.value) should equal(events.collect { case m: SystemEvent ⇒ m })
        resDomainEvent.map(_.asInstanceOf[DomainEvent]).sortBy(_.eventId.value) should equal(events.collect { case m: DomainEvent ⇒ m })
      }
    }
    "accessed via a consumer" should {
      val mat = FlowMaterializer(MaterializerSettings())
      "dispatch an event on the event stream" in { fixture ⇒
        val FixtureParam(streams) = fixture
        val consumerProbeEvent = TestProbe()

        val event = TestEvent("a")
        val consumer = DelegatingEventConsumer[Event](consumerProbeEvent.ref)
        val streamConsumer = streams.eventBroker.newConsumer
        within(1 second) {
          streams.eventStream.produceTo(streamConsumer)
          Flow(List[Event](event)).produceTo(mat, consumer)
          consumerProbeEvent.expectMsg(100 millis, event)
        }
      }

      "dispatch two events from two producers on the event stream" in { fixture ⇒
        val FixtureParam(streams) = fixture
        val consumerProbeEvent = TestProbe()

        val event1 = TestEvent("a")
        val event2 = TestEvent("b")
        val consumer = DelegatingEventConsumer[Event](consumerProbeEvent.ref)
        val streamConsumer1 = streams.eventBroker.newConsumer
        val streamConsumer2 = streams.eventBroker.newConsumer
        within(1 second) {
          streams.eventStream.produceTo(consumer)
          Flow(List[Event](event1)).produceTo(mat, streamConsumer1)
          consumerProbeEvent.expectMsg(100 millis, event1)
          Flow(List[Event](event2)).produceTo(mat, streamConsumer2)
          consumerProbeEvent.expectMsg(100 millis, event2)
        }
      }

    }
  }

  private val currentTestId = new java.util.concurrent.atomic.AtomicInteger(1)
  def nextTestId = currentTestId.getAndIncrement()

  case class FixtureParam(streams: AlmhirtStreams)

  def withFixture(test: OneArgTest) = {
    val testId = nextTestId
    info(s"Test $testId")
    val streams = AlmhirtStreams(s"event-broker-$testId", s"command-broker-$testId")
    val fixture = FixtureParam(streams)
    try {
      withFixture(test.toNoArgTest(fixture))
    } finally {
      streams.stop()
    }
  }

  override def beforeAll() {

  }

  override def afterAll() {
    TestKit.shutdownActorSystem(system)
  }

}