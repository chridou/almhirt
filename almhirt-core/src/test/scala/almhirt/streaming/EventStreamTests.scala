package almhirt.streaming

import scala.language.postfixOps
import scala.concurrent.duration._
import akka.actor._
import almhirt.common._
import akka.stream.scaladsl._
import akka.testkit._
import org.scalatest._

class EventStreamTests(_system: ActorSystem) extends TestKit(_system) with fixture.WordSpecLike with Matchers with BeforeAndAfterAll {
  def this() = this(ActorSystem("EventStreamTests", almhirt.TestConfigs.logWarningConfig))

  implicit val executionContext = system.dispatchers.defaultGlobalDispatcher
  implicit val ccuad = CanCreateUuidsAndDateTimes()

  implicit def implicitFlowMaterializer = akka.stream.ActorMaterializer()(_system)

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

  val nMsgBig = 10000

  "The AlmhirtStreams" when {
    "accessed via a contractor" should {
      "dispatch an event on the event stream" in { fixture ⇒
        val FixtureParam(streams) = fixture
        val subscriberProbeEvent = TestProbe()

        val event = TestEvent("a")
        val subscriber = DelegatingEventSubscriber[Event](subscriberProbeEvent.ref, (err: Throwable) => info(s"A ${err.getMessage}"))
        within(10 seconds) {
          streams.eventStream.subscribe(subscriber)
          Stillage(List[Event](event)).signContract(streams.eventBroker)
          subscriberProbeEvent.expectMsg(100 millis, event)
        }
        Thread.sleep(1000)
      }

      "dispatch two events from two producers on the event stream" in { fixture ⇒
        val FixtureParam(streams) = fixture
        val subscriberProbeEvent = TestProbe()

        val event1 = TestEvent("a")
        val event2 = TestEvent("b")
        val subscriber = DelegatingEventSubscriber[Event](subscriberProbeEvent.ref, (err: Throwable) => info(s"B ${err.getMessage}"))
        within(10 seconds) {
          streams.eventStream.subscribe(subscriber)
          Stillage(List[Event](event1)).signContract(streams.eventBroker)
          Stillage(List[Event](event2)).signContract(streams.eventBroker)
          subscriberProbeEvent.expectMsgAllOf(100 millis, event1, event2)
        }
      }

      "dispatch an event to 2 subscribers on the event stream" in { fixture ⇒
        val FixtureParam(streams) = fixture
        val subscriberProbeEvent1 = TestProbe()
        val subscriberProbeEvent2 = TestProbe()

        val event = TestEvent("a")
        val subscriber1 = DelegatingEventSubscriber[Event](subscriberProbeEvent1.ref, (err: Throwable) => info(s"C1 ${err.getMessage}"))
        val subscriber2 = DelegatingEventSubscriber[Event](subscriberProbeEvent2.ref, (err: Throwable) => info(s"C2 ${err.getMessage}"))
        within(10 seconds) {
          streams.eventStream.subscribe(subscriber1)
          streams.eventStream.subscribe(subscriber2)
          Stillage(List[Event](event)).signContract(streams.eventBroker)
          subscriberProbeEvent1.expectMsg(100 millis, event)
          subscriberProbeEvent2.expectMsg(100 millis, event)
        }
      }

      "dispatch a domain event on the event stream" in { fixture ⇒
        val FixtureParam(streams) = fixture
        val subscriberProbeEvent = TestProbe()

        val event = TestDomainEvent("a")
        val subscriber = DelegatingEventSubscriber[Event](subscriberProbeEvent.ref, (err: Throwable) => info(s"D ${err.getMessage}"))
        within(10 seconds) {
          streams.eventStream.subscribe(subscriber)
          Stillage(List[Event](event)).signContract(streams.eventBroker)
          subscriberProbeEvent.expectMsg(100 millis, event)
        }
      }

      s"dispatch many(${nMsgBig * 3}) events on the event stream" in { fixture ⇒
        val FixtureParam(streams) = fixture
        val probe2 = TestProbe()

        val n = nMsgBig * 3
        val events = (1 to n).map(i ⇒ TestEvent(i.toString): Event).toVector
        val subscriber = DelegatingEventSubscriber[Event](probe2.ref, (err: Throwable) => info(s"E ${err.getMessage}"))
        val start = Deadline.now
        within(30 seconds) {
          streams.eventStream.subscribe(subscriber)
          Stillage(events).signContract(streams.eventBroker)
          val res = probe2.receiveN(n, 30 seconds)
          val time = start.lap
          info(s"Dispatched $n in ${start.lap.defaultUnitString}((${(nMsgBig * 3 * 1000).toDouble / time.toMillis}/s)).")
          res should equal(events)
        }
      }
    }
    "accessed via a subscriber" should {
      "dispatch an event on the event stream" in { fixture ⇒
        val FixtureParam(streams) = fixture
        val subscriberProbeEvent = TestProbe()

        val event = TestEvent("a")
        val subscriber = DelegatingEventSubscriber[Event](subscriberProbeEvent.ref, (err: Throwable) => info(s"F ${err.getMessage}"))
        val streamSubscriber = streams.eventBroker.newSubscriber
        within(10 seconds) {
          streams.eventStream.subscribe(streamSubscriber)
          Source(List[Event](event)).to(Sink(subscriber)).run()
          subscriberProbeEvent.expectMsg(100 millis, event)
        }
      }

      "dispatch two events from two producers on the event stream" in { fixture ⇒
        val FixtureParam(streams) = fixture
        val subscriberProbeEvent = TestProbe()

        val event1 = TestEvent("a")
        val event2 = TestEvent("b")
        val subscriber = DelegatingEventSubscriber[Event](subscriberProbeEvent.ref, (err: Throwable) => info(s"G ${err.getMessage}"))
        val streamSubscriber1 = streams.eventBroker.newSubscriber
        val streamSubscriber2 = streams.eventBroker.newSubscriber
        within(10 seconds) {
          streams.eventStream.subscribe(subscriber)
          Source(List[Event](event1)).to(Sink(streamSubscriber1)).run()
          subscriberProbeEvent.expectMsg(100 millis, event1)
          Source(List[Event](event2)).to(Sink(streamSubscriber2)).run()
          subscriberProbeEvent.expectMsg(100 millis, event2)
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
    val (streams) = AlmhirtStreams(s"almhirt-streams-$testId")(10 seconds).awaitResultOrEscalate(10 seconds)
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