package almhirt.streaming;
//package almhirt.messaging
//
//import scala.language.postfixOps
//import scala.concurrent.duration._
//import akka.actor._
//import almhirt.common._
//import akka.testkit._
//import org.scalatest._
//
//class EventStreamTests(_system: ActorSystem) extends TestKit(_system) with fixture.FlatSpecLike with Matchers with BeforeAndAfterAll {
//  def this() = this(ActorSystem("EventStreamTests"))
//
//  implicit val executionContext = system.dispatchers.defaultGlobalDispatcher
//  implicit val ccuad = CanCreateUuidsAndDateTimes()
//
//  behavior of "The AlmhirtChannels"
//
//  case class TestEvent(id: EventId, timestamp: org.joda.time.LocalDateTime) extends Event
//  object TestEvent {
//    def apply(id: String): TestEvent = TestEvent(EventId(id), ccuad.getUtcTimestamp)
//  }
//  case class TestDomainEvent(id: EventId, timestamp: org.joda.time.LocalDateTime) extends DomainEvent
//  object TestDomainEvent {
//    def apply(id: String): TestDomainEvent = TestDomainEvent(EventId(id), ccuad.getUtcTimestamp)
//  }
//  case class TestSystemEvent(id: EventId, timestamp: org.joda.time.LocalDateTime) extends SystemEvent
//  object TestSystemEvent {
//    def apply(id: String): TestSystemEvent = TestSystemEvent(EventId(id), ccuad.getUtcTimestamp)
//  }
//
//  it should "accept an event" in { fixture =>
//    val FixtureParam(channels) = fixture
//    val probe = TestProbe()
//
//    val event = TestEvent("a")
//    within(1 second) {
//      probe.send(channels.eventStreamDispatcher, DispatchEvent(event))
//      probe.expectMsgType[EventDispatched](100 millis)
//    }
//  }
//
//  it should "accept a system event" in { fixture =>
//    val FixtureParam(channels) = fixture
//    val probe = TestProbe()
//
//    val event = TestSystemEvent("a")
//    within(1 second) {
//      probe.send(channels.eventStreamDispatcher, DispatchEvent(event))
//      probe.expectMsgType[EventDispatched](100 millis)
//    }
//  }
//
//  it should "accept a domain event" in { fixture =>
//    val FixtureParam(channels) = fixture
//    val probe = TestProbe()
//
//    val event = TestDomainEvent("a")
//    within(1 second) {
//      probe.send(channels.eventStreamDispatcher, DispatchEvent(event))
//      probe.expectMsgType[EventDispatched](100 millis)
//    }
//  }
//  
//  it should "dispatch an event on the event stream" in { fixture =>
//    val FixtureParam(channels) = fixture
//    val probe = TestProbe()
//    val consumerProbeEvent = TestProbe()
//    
//    val event = TestEvent("a")
//    val consumer = DelegatingEventConsumer[Event](consumerProbeEvent.ref)
//    within(1 second) {
//      channels.eventStream.produceTo(consumer)
//      probe.send(channels.eventStreamDispatcher, DispatchEvent(event))
//      probe.expectMsgType[EventDispatched](100 millis)
//      consumerProbeEvent.expectMsg(100 millis, event)
//    }
//  }
//
//  it should "dispatch an event to 2 consumers on the event stream" in { fixture =>
//    val FixtureParam(channels) = fixture
//    val probe = TestProbe()
//    val consumerProbeEvent1 = TestProbe()
//    val consumerProbeEvent2 = TestProbe()
//    
//    val event = TestEvent("a")
//    val consumer1 = DelegatingEventConsumer[Event](consumerProbeEvent1.ref)
//    val consumer2 = DelegatingEventConsumer[Event](consumerProbeEvent2.ref)
//    within(1 second) {
//      channels.eventStream.produceTo(consumer1)
//      channels.eventStream.produceTo(consumer2)
//      probe.send(channels.eventStreamDispatcher, DispatchEvent(event))
//      probe.expectMsgType[EventDispatched](100 millis)
//      consumerProbeEvent1.expectMsg(100 millis, event)
//      consumerProbeEvent2.expectMsg(100 millis, event)
//    }
//  }
//  
//  it should "NOT dispatch an event on the system event stream" in { fixture =>
//    val FixtureParam(channels) = fixture
//    val probe = TestProbe()
//    val consumerProbeEvent = TestProbe()
//    
//    val event = TestEvent("a")
//    val consumer = DelegatingEventConsumer[SystemEvent](consumerProbeEvent.ref)
//    within(1 second) {
//      channels.systemEventStream.produceTo(consumer)
//      probe.send(channels.eventStreamDispatcher, DispatchEvent(event))
//      probe.expectMsgType[EventDispatched](100 millis)
//      consumerProbeEvent.expectNoMsg(100 millis)
//    }
//  }
//  
//
//  it should "NOT dispatch an event on the domain event stream" in { fixture =>
//    val FixtureParam(channels) = fixture
//    val probe = TestProbe()
//    val consumerProbeEvent = TestProbe()
//    
//    val event = TestEvent("a")
//    val consumer = DelegatingEventConsumer[DomainEvent](consumerProbeEvent.ref)
//    within(1 second) {
//      channels.domainEventStream.produceTo(consumer)
//      probe.send(channels.eventStreamDispatcher, DispatchEvent(event))
//      probe.expectMsgType[EventDispatched](100 millis)
//      consumerProbeEvent.expectNoMsg(100 millis)
//    }
//  }
//
//  it should "dispatch a system event on the event stream" in { fixture =>
//    val FixtureParam(channels) = fixture
//    val probe = TestProbe()
//    val consumerProbeEvent = TestProbe()
//
//    val event = TestSystemEvent("a")
//    val consumer = DelegatingEventConsumer[Event](consumerProbeEvent.ref)
//    within(1 second) {
//      channels.eventStream.produceTo(consumer)
//      probe.send(channels.eventStreamDispatcher, DispatchEvent(event))
//      probe.expectMsgType[EventDispatched](100 millis)
//      consumerProbeEvent.expectMsg(100 millis, event)
//    }
//  }
//
//  it should "dispatch a system event on the system event stream" in { fixture =>
//    val FixtureParam(channels) = fixture
//    val probe = TestProbe()
//    val consumerProbeEvent = TestProbe()
//
//    val event = TestSystemEvent("a")
//    val consumer = DelegatingEventConsumer[SystemEvent](consumerProbeEvent.ref)
//    within(1 second) {
//      channels.systemEventStream.produceTo(consumer)
//      probe.send(channels.eventStreamDispatcher, DispatchEvent(event))
//      probe.expectMsgType[EventDispatched](100 millis)
//      consumerProbeEvent.expectMsg(100 millis, event)
//    }
//  }
//  
//  it should "NOT dispatch a system event on the domain event stream" in { fixture =>
//    val FixtureParam(channels) = fixture
//    val probe = TestProbe()
//    val consumerProbeEvent = TestProbe()
//    
//    val event = TestSystemEvent("a")
//    val consumer = DelegatingEventConsumer[DomainEvent](consumerProbeEvent.ref)
//    within(1 second) {
//      channels.domainEventStream.produceTo(consumer)
//      probe.send(channels.eventStreamDispatcher, DispatchEvent(event))
//      probe.expectMsgType[EventDispatched](100 millis)
//      consumerProbeEvent.expectNoMsg(100 millis)
//    }
//  }
//
//  
//  it should "dispatch a domain event on the event stream" in { fixture =>
//    val FixtureParam(channels) = fixture
//    val probe = TestProbe()
//    val consumerProbeEvent = TestProbe()
//
//    val event = TestDomainEvent("a")
//    val consumer = DelegatingEventConsumer[Event](consumerProbeEvent.ref)
//    within(1 second) {
//      channels.eventStream.produceTo(consumer)
//      probe.send(channels.eventStreamDispatcher, DispatchEvent(event))
//      probe.expectMsgType[EventDispatched](100 millis)
//      consumerProbeEvent.expectMsg(100 millis, event)
//    }
//  }
//  
//  it should "NOT dispatch a domain event on the system event stream" in { fixture =>
//    val FixtureParam(channels) = fixture
//    val probe = TestProbe()
//    val consumerProbeEvent = TestProbe()
//
//    val event = TestDomainEvent("a")
//    val consumer = DelegatingEventConsumer[SystemEvent](consumerProbeEvent.ref)
//    within(1 second) {
//      channels.systemEventStream.produceTo(consumer)
//      probe.send(channels.eventStreamDispatcher, DispatchEvent(event))
//      probe.expectMsgType[EventDispatched](100 millis)
//      consumerProbeEvent.expectNoMsg(100 millis)
//    }
//  }
//
//  it should "dispatch a domain event on the domain event stream" in { fixture =>
//    val FixtureParam(channels) = fixture
//    val probe = TestProbe()
//    val consumerProbeEvent = TestProbe()
//
//    val event = TestDomainEvent("a")
//    val consumer = DelegatingEventConsumer[DomainEvent](consumerProbeEvent.ref)
//    within(1 second) {
//      channels.domainEventStream.produceTo(consumer)
//      probe.send(channels.eventStreamDispatcher, DispatchEvent(event))
//      probe.expectMsgType[EventDispatched](100 millis)
//      consumerProbeEvent.expectMsg(100 millis, event)
//    }
//  }
//  
//  it should "dispatch many events on the event stream" in { fixture =>
//    val FixtureParam(channels) = fixture
//    val probe = TestProbe()
//    val probe2 = TestProbe()
//
//    val n = 1000
//    val events = (1 to n).map(i => TestEvent(i.toString)).toVector
//    val consumer = DelegatingEventConsumer[Event](probe2.ref)
//    within(1 second) {
//      channels.eventStreamDispatcher ! SetBufferSize(1000)
//      channels.eventStream.produceTo(consumer)
//      events.foreach(event => probe.send(channels.eventStreamDispatcher, DispatchEvent(event)))
//      val res = probe2.receiveN(n)
//      res should equal(events)
//    }
//  }
//   
//  it should "dispatch many events of different kinds on the matching streams" in { fixture =>
//    val FixtureParam(channels) = fixture
//    val probe = TestProbe()
//    val probeEvent = TestProbe()
//    val probeSystemEvent = TestProbe()
//    val probeDomainEvent = TestProbe()
//
//    val n = 99
//    val events = (1 to n).map(i => 
//      if(i % 3 == 0) {
//        TestEvent(i.toString)
//      } else if (i % 3 == 1) {
//        TestSystemEvent(i.toString)
//      } else {
//        TestDomainEvent(i.toString)
//      }).toVector
//    val eventConfirmations = events.map(EventDispatched(_))
//    val consumerEvent = DelegatingEventConsumer[Event](probeEvent.ref)
//    val consumerSystemEvent = DelegatingEventConsumer[SystemEvent](probeSystemEvent.ref)
//    val consumerDomainEvent = DelegatingEventConsumer[DomainEvent](probeDomainEvent.ref)
//    within(1 second) {
//      channels.eventStreamDispatcher ! SetBufferSize(1000)
//      channels.eventStream.produceTo(consumerEvent)
//      channels.systemEventStream.produceTo(consumerSystemEvent)
//      channels.domainEventStream.produceTo(consumerDomainEvent)
//      events.foreach(event => probe.send(channels.eventStreamDispatcher, DispatchEvent(event)))
//      val resEvent = probeEvent.receiveN(n)
//      val resSystemEvent = probeSystemEvent.receiveN(n / 3)
//      val resDomainEvent = probeDomainEvent.receiveN(n / 3)
//      resEvent should equal(events)
//      resSystemEvent should equal(events.collect{ case m: SystemEvent => m})
//      resDomainEvent should equal(events.collect{ case m: DomainEvent => m})
//    }
//  }
//   
//  
//  private val currentTestId = new java.util.concurrent.atomic.AtomicInteger(1)
//  def nextTestId = currentTestId.getAndIncrement()
//
//  case class FixtureParam(channels: AlmhirtChannels)
//
//  def withFixture(test: OneArgTest) = {
//    val testId = nextTestId
//    val channels = AlmhirtChannels(s"ed$testId", s"cd$testId")
//    val fixture = FixtureParam(channels)
//    try {
//      withFixture(test.toNoArgTest(fixture))
//    } finally {
//      channels.closeChannels()
//    }
//  }
//
//  override def beforeAll() {
//
//  }
//
//  override def afterAll() {
//    TestKit.shutdownActorSystem(system)
//  }
//
//}