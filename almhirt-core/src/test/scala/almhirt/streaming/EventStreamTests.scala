package almhirt.streaming

import scala.language.postfixOps
import scala.concurrent.duration._
import akka.actor._
import akka.stream.scaladsl.{ Flow }
import akka.stream.{ FlowMaterializer, MaterializerSettings }
import almhirt.common._
import akka.testkit._
import org.scalatest._

class EventStreamTests(_system: ActorSystem) extends TestKit(_system) with fixture.FlatSpecLike with Matchers with BeforeAndAfterAll {
  def this() = this(ActorSystem("EventStreamTests"))

  implicit val executionContext = system.dispatchers.defaultGlobalDispatcher
  implicit val ccuad = CanCreateUuidsAndDateTimes()

  behavior of "The AlmhirtStreams"

  case class TestEvent(id: EventId, timestamp: org.joda.time.LocalDateTime) extends Event
  object TestEvent {
    def apply(id: String): TestEvent = TestEvent(EventId(id), ccuad.getUtcTimestamp)
  }
  case class TestDomainEvent(id: EventId, timestamp: org.joda.time.LocalDateTime) extends DomainEvent
  object TestDomainEvent {
    def apply(id: String): TestDomainEvent = TestDomainEvent(EventId(id), ccuad.getUtcTimestamp)
  }
  case class TestSystemEvent(id: EventId, timestamp: org.joda.time.LocalDateTime) extends SystemEvent
  object TestSystemEvent {
    def apply(id: String): TestSystemEvent = TestSystemEvent(EventId(id), ccuad.getUtcTimestamp)
  }

  it should "dispatch an event on the event stream" in { fixture =>
    val FixtureParam(streams) = fixture
    val consumerProbeEvent = TestProbe()

    val event = TestEvent("a")
    val consumer = DelegatingEventConsumer[Event](consumerProbeEvent.ref)
    within(1 second) {
      streams.eventStream.produceTo(consumer)
      Stillage(List[Event](event)).supply(streams.eventConsumer)
      consumerProbeEvent.expectMsg(100 millis, event)
    }
  }

  it should "dispatch two events from two producers on the event stream" in { fixture =>
    val FixtureParam(streams) = fixture
    val consumerProbeEvent = TestProbe()

    val event1 = TestEvent("a")
    val event2 = TestEvent("b")
    val consumer = DelegatingEventConsumer[Event](consumerProbeEvent.ref)
    within(1 second) {
      streams.eventStream.produceTo(consumer)
      Stillage(List[Event](event1)).supply(streams.eventConsumer)
      Stillage(List[Event](event2)).supply(streams.eventConsumer)
      consumerProbeEvent.expectMsg(100 millis, event1)
      consumerProbeEvent.expectMsg(100 millis, event2)
    }
  }

  it should "dispatch an event to 2 consumers on the event stream" in { fixture =>
    val FixtureParam(streams) = fixture
    val consumerProbeEvent1 = TestProbe()
    val consumerProbeEvent2 = TestProbe()

    val event = TestEvent("a")
    val consumer1 = DelegatingEventConsumer[Event](consumerProbeEvent1.ref)
    val consumer2 = DelegatingEventConsumer[Event](consumerProbeEvent2.ref)
    within(1 second) {
      streams.eventStream.produceTo(consumer1)
      streams.eventStream.produceTo(consumer2)
      Stillage(List[Event](event)).supply(streams.eventConsumer)
      consumerProbeEvent1.expectMsg(100 millis, event)
      consumerProbeEvent2.expectMsg(100 millis, event)
    }
  }

  it should "NOT dispatch an event on the system event stream" in { fixture =>
    val FixtureParam(streams) = fixture
    val consumerProbeEvent = TestProbe()

    val event = TestEvent("a")
    val consumer = DelegatingEventConsumer[SystemEvent](consumerProbeEvent.ref)
    within(1 second) {
      streams.systemEventStream.produceTo(consumer)
      Stillage(List[Event](event)).supply(streams.eventConsumer)
      consumerProbeEvent.expectNoMsg(100 millis)
    }
  }

  it should "NOT dispatch an event on the domain event stream" in { fixture =>
    val FixtureParam(streams) = fixture
    val consumerProbeEvent = TestProbe()

    val event = TestEvent("a")
    val consumer = DelegatingEventConsumer[DomainEvent](consumerProbeEvent.ref)
    within(1 second) {
      streams.domainEventStream.produceTo(consumer)
      Stillage(List[Event](event)).supply(streams.eventConsumer)
      consumerProbeEvent.expectNoMsg(100 millis)
    }
  }

  it should "dispatch a system event on the event stream" in { fixture =>
    val FixtureParam(streams) = fixture
    val consumerProbeEvent = TestProbe()

    val event = TestSystemEvent("a")
    val consumer = DelegatingEventConsumer[Event](consumerProbeEvent.ref)
    within(1 second) {
      streams.eventStream.produceTo(consumer)
      Stillage(List[Event](event)).supply(streams.eventConsumer)
      consumerProbeEvent.expectMsg(100 millis, event)
    }
  }

  it should "dispatch a system event on the system event stream" in { fixture =>
    val FixtureParam(streams) = fixture
    val consumerProbeEvent = TestProbe()

    val event = TestSystemEvent("a")
    val consumer = DelegatingEventConsumer[SystemEvent](consumerProbeEvent.ref)
    within(1 second) {
      streams.systemEventStream.produceTo(consumer)
      Stillage(List[Event](event)).supply(streams.eventConsumer)
      consumerProbeEvent.expectMsg(100 millis, event)
    }
  }

  it should "NOT dispatch a system event on the domain event stream" in { fixture =>
    val FixtureParam(streams) = fixture
    val consumerProbeEvent = TestProbe()

    val event = TestSystemEvent("a")
    val consumer = DelegatingEventConsumer[DomainEvent](consumerProbeEvent.ref)
    within(1 second) {
      streams.domainEventStream.produceTo(consumer)
      Stillage(List[Event](event)).supply(streams.eventConsumer)
      consumerProbeEvent.expectNoMsg(100 millis)
    }
  }

  it should "dispatch a domain event on the event stream" in { fixture =>
    val FixtureParam(streams) = fixture
    val consumerProbeEvent = TestProbe()

    val event = TestDomainEvent("a")
    val consumer = DelegatingEventConsumer[Event](consumerProbeEvent.ref)
    within(1 second) {
      streams.eventStream.produceTo(consumer)
      Stillage(List[Event](event)).supply(streams.eventConsumer)
      consumerProbeEvent.expectMsg(100 millis, event)
    }
  }

  it should "NOT dispatch a domain event on the system event stream" in { fixture =>
    val FixtureParam(streams) = fixture
    val consumerProbeEvent = TestProbe()

    val event = TestDomainEvent("a")
    val consumer = DelegatingEventConsumer[SystemEvent](consumerProbeEvent.ref)
    within(1 second) {
      streams.systemEventStream.produceTo(consumer)
      Stillage(List[Event](event)).supply(streams.eventConsumer)
      consumerProbeEvent.expectNoMsg(100 millis)
    }
  }

  it should "dispatch a domain event on the domain event stream" in { fixture =>
    val FixtureParam(streams) = fixture
    val consumerProbeEvent = TestProbe()

    val event = TestDomainEvent("a")
    val consumer = DelegatingEventConsumer[DomainEvent](consumerProbeEvent.ref)
    within(1 second) {
      streams.domainEventStream.produceTo(consumer)
      Stillage(List[Event](event)).supply(streams.eventConsumer)
      consumerProbeEvent.expectMsg(100 millis, event)
    }
  }

  it should "dispatch many events on the event stream" in { fixture =>
    val FixtureParam(streams) = fixture
    val probe2 = TestProbe()

    val n = 1000
    val events = (1 to n).map(i => TestEvent(i.toString): Event).toVector
    val consumer = DelegatingEventConsumer[Event](probe2.ref)
    within(1 second) {
      streams.eventStream.produceTo(consumer)
      Stillage(events).supply(streams.eventConsumer)
      val res = probe2.receiveN(n)
      res should equal(events)
    }
  }

  it should "dispatch many events of different kinds on the matching streams" in { fixture =>
    val FixtureParam(streams) = fixture
    val probeEvent = TestProbe()
    val probeSystemEvent = TestProbe()
    val probeDomainEvent = TestProbe()

    val n = 999
    val events = (1 to n).map(i =>
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
    within(1 second) {
      streams.eventStream.produceTo(consumerEvent)
      streams.systemEventStream.produceTo(consumerSystemEvent)
      streams.domainEventStream.produceTo(consumerDomainEvent)
      Stillage(events).supply(streams.eventConsumer)
      val resEvent = probeEvent.receiveN(n)
      val resSystemEvent = probeSystemEvent.receiveN(n / 3)
      val resDomainEvent = probeDomainEvent.receiveN(n / 3)
      resEvent should equal(events)
      resSystemEvent should equal(events.collect { case m: SystemEvent => m })
      resDomainEvent should equal(events.collect { case m: DomainEvent => m })
    }
  }

  it should "dispatch many events of different kinds from many producers on the matching streams" in { fixture =>
    val FixtureParam(streams) = fixture
    val probeEvent = TestProbe()
    val probeSystemEvent = TestProbe()
    val probeDomainEvent = TestProbe()

    val n = 99999
    val events = (1 to n).map(i =>
      if (i % 3 == 0) {
        TestEvent(f"$i%07d")
      } else if (i % 3 == 1) {
        TestSystemEvent(f"$i%07d")
      } else {
        TestDomainEvent(f"$i%07d")
      }: Event).toVector
    val parts = events.grouped(n / 10)
    val consumerEvent = DelegatingEventConsumer[Event](probeEvent.ref)
    val consumerSystemEvent = DelegatingEventConsumer[SystemEvent](probeSystemEvent.ref)
    val consumerDomainEvent = DelegatingEventConsumer[DomainEvent](probeDomainEvent.ref)

    var resEvent: Seq[Any] = null
    var resSystemEvent: Seq[Any] = null
    var resDomainEvent: Seq[Any] = null
    within(2 seconds) {
      streams.eventStream.produceTo(consumerEvent)
      streams.systemEventStream.produceTo(consumerSystemEvent)
      streams.domainEventStream.produceTo(consumerDomainEvent)
      parts.foreach(Stillage(_).supply(streams.eventConsumer))
      resEvent = probeEvent.receiveN(n)
      resSystemEvent = probeSystemEvent.receiveN(n / 3)
      resDomainEvent = probeDomainEvent.receiveN(n / 3)
    }
    resEvent.map(_.asInstanceOf[Event]).sortBy(_.id.id) should equal(events)
    resSystemEvent.map(_.asInstanceOf[SystemEvent]).sortBy(_.id.id) should equal(events.collect { case m: SystemEvent => m })
    resDomainEvent.map(_.asInstanceOf[DomainEvent]).sortBy(_.id.id) should equal(events.collect { case m: DomainEvent => m })
  }

  private val currentTestId = new java.util.concurrent.atomic.AtomicInteger(1)
  def nextTestId = currentTestId.getAndIncrement()

  case class FixtureParam(streams: AlmhirtStreams)

  def withFixture(test: OneArgTest) = {
    val testId = nextTestId
    val streams = AlmhirtStreams(s"event-trader-$testId", s"command-trader-$testId")
    val fixture = FixtureParam(streams)
    try {
      withFixture(test.toNoArgTest(fixture))
    } finally {
      streams.closeStreams()
    }
  }

  override def beforeAll() {

  }

  override def afterAll() {
    TestKit.shutdownActorSystem(system)
  }

}