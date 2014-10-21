package almhirt.components

import scala.language.implicitConversions
import scala.language.postfixOps
import scala.concurrent.duration._
import akka.actor._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.tracking._
import org.reactivestreams.Subscriber
import akka.stream.scaladsl2._
import akka.testkit._
import org.scalatest._
import almhirt.context.AlmhirtContext

class CommandStatusTrackerTests(_system: ActorSystem)
  extends TestKit(_system) with fixture.WordSpecLike with Matchers with BeforeAndAfterAll {
  def this() = this(ActorSystem("CommandStatusTrackerTests", almhirt.TestConfigs.logWarningConfig))

  implicit val mat = FlowMaterializer()

  implicit val executionContext = system.dispatchers.defaultGlobalDispatcher
  implicit val ccuad = CanCreateUuidsAndDateTimes()

  def createEvent(commandId: CommandId, status: CommandStatus): CommandStatusChanged =
    CommandStatusChanged(CommandHeader(commandId, ccuad.getUtcTimestamp), status)

  val trackerTimeoutScanInterval = 250.millis.dilated

  implicit def str2CommandId(str: String): CommandId = CommandId(str)

  "The CommandStatusTracker" when {
    import CommandStatusTracker._
    "receiving a single status and then a single subscription" when {
      """a "Executed" status is received and then a matching subscription""" should {
        """notify the subscriber with a "Executed""" in { fixture ⇒
          val FixtureParam(testId, tracker, eventSink) = fixture
          val probe = TestProbe()
          Source(createEvent("a", CommandStatus.Executed) :: Nil).connect(eventSink).run()
          system.scheduler.scheduleOnce(100.millis.dilated)(tracker ! TrackCommandMapped("a", res ⇒ probe.ref ! res, (100.millis.dilated).fromNow))
          probe.expectMsg(5.seconds.dilated, TrackedExecutued)
        }
      }
      """a "NotExecuted" status is received and then a matching subscription""" should {
        """notify the subscriber with a "NotExecuted"""" in { fixture ⇒
          val FixtureParam(testId, tracker, eventSink) = fixture
          val probe = TestProbe()
          val status = CommandStatus.NotExecuted(UnspecifiedProblem(""))
          Source(createEvent("a", status) :: Nil).connect(eventSink).run()
          system.scheduler.scheduleOnce(100.millis.dilated)(tracker ! TrackCommandMapped("a", res ⇒ probe.ref ! res, (100.millis.dilated).fromNow))
          tracker ! TrackCommandMapped("a", res ⇒ probe.ref ! res, (100.millis.dilated).fromNow)
          probe.expectMsg(5.seconds.dilated, TrackedNotExecutued(status.cause))
        }
      }
    }
    "receiving a single subscription and then a single status" when {
      """a subscription is received and then a matching "Executed" status""" should {
        """notify the subscriber with a "Executed"""" in { fixture ⇒
          val FixtureParam(testId, tracker, eventSink) = fixture
          val probe = TestProbe()
          tracker ! TrackCommandMapped("a", res ⇒ probe.ref ! res, (200.millis.dilated).fromNow)
          Source(createEvent("a", CommandStatus.Executed) :: Nil).connect(eventSink).run()
          probe.expectMsg(5.seconds.dilated, TrackedExecutued)
        }
      }
      """a subscription is received and then a non-matching "Executed" status""" should {
        """notify the subscriber with a "Timeout"""" in { fixture ⇒
          val FixtureParam(testId, tracker, eventSink) = fixture
          val probe = TestProbe()
          tracker ! TrackCommandMapped("a", res ⇒ probe.ref ! res, (200.millis.dilated).fromNow)
          Source(createEvent("b", CommandStatus.Executed) :: Nil).connect(eventSink).run()
          probe.expectMsg(5.seconds.dilated, TrackedTimeout)
        }
      }
      """a subscription is received and then a matching "NotExecuted" status""" should {
        """notify the subscriber with a "NotExecuted"""" in { fixture ⇒
          val FixtureParam(testId, tracker, eventSink) = fixture
          val probe = TestProbe()
          val status = CommandStatus.NotExecuted(UnspecifiedProblem(""))
          tracker ! TrackCommandMapped("a", res ⇒ probe.ref ! res, (100.millis.dilated).fromNow)
          Source(createEvent("a", status) :: Nil).connect(eventSink).run()
          probe.expectMsg(5.seconds.dilated, TrackedNotExecutued(status.cause))
        }
      }
      """a subscription is received and then a non-matching "NotExecuted" status""" should {
        """notify the subscriber with a "Timeout"""" in { fixture ⇒
          val FixtureParam(testId, tracker, eventSink) = fixture
          val probe = TestProbe()
          val status = CommandStatus.NotExecuted(UnspecifiedProblem(""))
          tracker ! TrackCommandMapped("a", res ⇒ probe.ref ! res, (100.millis.dilated).fromNow)
          Source(createEvent("b", status) :: Nil).connect(eventSink).run()
          probe.expectMsg(5.seconds.dilated, TrackedTimeout)
        }
      }
      """a subscription is received and then a matching "Executed" status after the subscriptions deadline""" should {
        """notify the subscriber with a "Timeout"""" in { fixture ⇒
          val FixtureParam(testId, tracker, eventSink) = fixture
          val probe = TestProbe()
          tracker ! TrackCommandMapped("a", res ⇒ probe.ref ! res, (100.millis.dilated).fromNow)
          system.scheduler.scheduleOnce(trackerTimeoutScanInterval)(Source(createEvent("a", CommandStatus.Executed) :: Nil).connect(eventSink).run())
          probe.expectMsg(5.seconds.dilated, TrackedTimeout)
        }
        """not notify the subscriber with a "Executed" after the timeout""" in { fixture ⇒
          val FixtureParam(testId, tracker, eventSink) = fixture
          val probe = TestProbe()
          tracker ! TrackCommandMapped("a", res ⇒ probe.ref ! res, (100.millis.dilated).fromNow)
          system.scheduler.scheduleOnce(trackerTimeoutScanInterval)(Source(createEvent("a", CommandStatus.Executed) :: Nil).connect(eventSink).run())
          probe.expectMsg(5.seconds.dilated, TrackedTimeout)
          probe.expectNoMsg(2 * trackerTimeoutScanInterval)
        }
      }
      """a subscription is received and then a matching "NotExecuted" status after the subscriptions deadline""" should {
        """notify the subscriber with a "Timeout"""" in { fixture ⇒
          val FixtureParam(testId, tracker, eventSink) = fixture
          val probe = TestProbe()
          val status = CommandStatus.NotExecuted(UnspecifiedProblem(""))
          tracker ! TrackCommandMapped("a", res ⇒ probe.ref ! res, (100.millis.dilated).fromNow)
          system.scheduler.scheduleOnce(trackerTimeoutScanInterval)(Source(createEvent("a", status) :: Nil).connect(eventSink).run())
          probe.expectMsg(5.seconds.dilated, TrackedTimeout)
        }
        """not notify the subscriber with a "NotExecuted" after the timeout""" in { fixture ⇒
          val FixtureParam(testId, tracker, eventSink) = fixture
          val probe = TestProbe()
          val status = CommandStatus.NotExecuted(UnspecifiedProblem(""))
          tracker ! TrackCommandMapped("a", res ⇒ probe.ref ! res, (100.millis.dilated).fromNow)
          system.scheduler.scheduleOnce(trackerTimeoutScanInterval)(Source(createEvent("a", status) :: Nil).connect(eventSink).run())
          probe.expectMsg(5.seconds.dilated, TrackedTimeout)
          probe.expectNoMsg(2 * trackerTimeoutScanInterval)
        }
      }
    }
    "receiving a single status and then multiple subscriptions" when {
      """a "Executed" status is received and then 2 matching subscriptions""" should {
        """notify the subscribers with a "Executed"""" in { fixture ⇒
          val FixtureParam(testId, tracker, eventSink) = fixture
          val probe = TestProbe()
          Source(createEvent("a", CommandStatus.Executed) :: Nil).connect(eventSink).run()
          system.scheduler.scheduleOnce(100.millis.dilated) {
            tracker ! TrackCommandMapped("a", res ⇒ probe.ref ! res, (100.millis.dilated).fromNow)
            tracker ! TrackCommandMapped("a", res ⇒ probe.ref ! res, (100.millis.dilated).fromNow)
          }
          probe.expectMsg(5.seconds.dilated, TrackedExecutued)
          probe.expectMsg(5.seconds.dilated, TrackedExecutued)
        }
      }
      """a "NotExecuted" status is received and then 2 matching subscriptions""" should {
        """notify the subscribers with a "NotExecuted"""" in { fixture ⇒
          val FixtureParam(testId, tracker, eventSink) = fixture
          val probe = TestProbe()
          val status = CommandStatus.NotExecuted(UnspecifiedProblem(""))
          Source(createEvent("a", status) :: Nil).connect(eventSink).run()
          system.scheduler.scheduleOnce(100.millis.dilated) {
            tracker ! TrackCommandMapped("a", res ⇒ probe.ref ! res, (100.millis.dilated).fromNow)
            tracker ! TrackCommandMapped("a", res ⇒ probe.ref ! res, (100.millis.dilated).fromNow)
          }
          probe.expectMsg(5.seconds.dilated, TrackedNotExecutued(status.cause))
          probe.expectMsg(5.seconds.dilated, TrackedNotExecutued(status.cause))
        }
      }
      """a "Executed" status is received and then 1 matching and one non-matching subscription""" should {
        """notify the matching subscriber with a "Executed" and the non-matching with a timeout""" in { fixture ⇒
          val FixtureParam(testId, tracker, eventSink) = fixture
          val probe = TestProbe()
          Source(createEvent("a", CommandStatus.Executed) :: Nil).connect(eventSink).run()
          system.scheduler.scheduleOnce(100.millis.dilated) {
            tracker ! TrackCommandMapped("b", res ⇒ probe.ref ! res, (100.millis.dilated).fromNow)
            tracker ! TrackCommandMapped("a", res ⇒ probe.ref ! res, (100.millis.dilated).fromNow)
          }
          probe.expectMsg(TrackedExecutued)
          probe.expectMsg(TrackedTimeout)
        }
      }
      """a "NotExecuted" status is received and then 1 matching and one non-matching subscription""" should {
        """notify the matching subscriber with a "NotExecuted" and the non-matching with a timeout"""" in { fixture ⇒
          val FixtureParam(testId, tracker, eventSink) = fixture
          val probe = TestProbe()
          val status = CommandStatus.NotExecuted(UnspecifiedProblem(""))
          Source(createEvent("a", status) :: Nil).connect(eventSink).run()
          system.scheduler.scheduleOnce(100.millis.dilated) {
            tracker ! TrackCommandMapped("b", res ⇒ probe.ref ! res, (100.millis.dilated).fromNow)
            tracker ! TrackCommandMapped("a", res ⇒ probe.ref ! res, (100.millis.dilated).fromNow)
          }
          probe.expectMsg(5.seconds.dilated, TrackedNotExecutued(status.cause))
          probe.expectMsg(5.seconds.dilated, TrackedTimeout)
        }
      }
    }
    "receiving a single status between 2 subscriptions" when {
      """a status is received and none of the subscriptions timed out""" should {
        """notify the subscribers with the status""" in { fixture ⇒
          val FixtureParam(testId, tracker, eventSink) = fixture
          val probe = TestProbe()
          tracker ! TrackCommandMapped("a", res ⇒ probe.ref ! res, (500.millis.dilated).fromNow)
          system.scheduler.scheduleOnce(200.millis.dilated)(Source(createEvent("a", CommandStatus.Executed) :: Nil).connect(eventSink).run())
          system.scheduler.scheduleOnce(300.millis.dilated)(tracker ! TrackCommandMapped("a", res ⇒ probe.ref ! res, (100.millis.dilated).fromNow))
          probe.expectMsg(5.seconds.dilated, TrackedExecutued)
          probe.expectMsg(5.seconds.dilated, TrackedExecutued)
        }
      }
      """a status is received and the first subscription already timed out""" should {
        """notify one subscriber with the status and the other with a timeout""" in { fixture ⇒
          val FixtureParam(testId, tracker, eventSink) = fixture
          val probe = TestProbe()
          tracker ! TrackCommandMapped("a", res ⇒ probe.ref ! res, (100.millis.dilated).fromNow)
          system.scheduler.scheduleOnce(trackerTimeoutScanInterval + 100.millis.dilated)(Source(createEvent("a", CommandStatus.Executed) :: Nil).connect(eventSink).run())
          system.scheduler.scheduleOnce(trackerTimeoutScanInterval + 200.millis.dilated)(tracker ! TrackCommandMapped("a", res ⇒ probe.ref ! res, (100.millis.dilated).fromNow))
          probe.expectMsg(5.seconds.dilated, TrackedTimeout)
          probe.expectMsg(5.seconds.dilated, TrackedExecutued)
        }
      }
    }
    "just receiveing subscriptions" should {
      """notify each subscriber with a timeout""" in { fixture ⇒
        val n = 10
        val FixtureParam(testId, tracker, eventSink) = fixture
        val probe = TestProbe()
        (1 to n).foreach(i ⇒ tracker ! TrackCommandMapped(s"$i-cmd", res ⇒ {probe.ref ! res}, (100.millis.dilated).fromNow))
        val count = probe.receiveN(n, 5.seconds.dilated).collect { case TrackedTimeout ⇒ 1 }.sum
        count should equal(n)
      }
    }
    "receiving many status events and many subscriptions" when {
      "the subscriptions are reveived before the events" should {
        """notify each subscriber""" in { fixture ⇒
          val n = 100
          val FixtureParam(testId, tracker, eventSink) = fixture
          val probe = TestProbe()
          (1 to n).foreach(i ⇒ tracker ! TrackCommandMapped(s"$i", res ⇒ probe.ref ! res, (250.millis.dilated).fromNow))
          Source((1 to n).map(i ⇒ createEvent(s"$i", CommandStatus.Executed))).connect(eventSink).run()
          val count = probe.receiveN(n, 10.seconds.dilated).collect { case TrackedExecutued ⇒ 1 }.sum
          count should equal(n)
        }
      }
      "the events are reveived before the subscriptions" should {
        """notify each subscriber""" in { fixture ⇒
          val n = 100
          val FixtureParam(testId, tracker, eventSink) = fixture
          val probe = TestProbe()
          Source((1 to n).map(i ⇒ createEvent(s"$i", CommandStatus.Executed))).connect(eventSink).run()
          system.scheduler.scheduleOnce(200.millis.dilated) {
            (1 to n).foreach(i ⇒ tracker ! TrackCommandMapped(s"$i", res ⇒ probe.ref ! res, (100.millis.dilated).fromNow))
          }
          val count = probe.receiveN(n, 10.seconds.dilated).collect { case TrackedExecutued ⇒ 1 }.sum
          count should equal(n)
        }
      }
      "the events and subscriptions have no specific order" should {
        """notify each subscriber""" in { fixture ⇒
          val n = 1000
          val FixtureParam(testId, tracker, eventSink) = fixture
          val probe = TestProbe()
          Source((1 to n).map(i ⇒ createEvent(s"$i", CommandStatus.Executed))).connect(eventSink).run()
          val deadline = (500.millis.dilated).fromNow
          (1 to n).foreach(i ⇒ tracker ! TrackCommandMapped(s"$i", res ⇒ probe.ref ! res, deadline))
          val tracked = probe.receiveN(n, 10.seconds.dilated)
          val executed = tracked.collect { case TrackedExecutued ⇒ 1 }.sum
          val timedout = tracked.collect { case TrackedTimeout ⇒ 1 }.sum
          timedout should equal(0)
          executed should equal(n)
        }
      }
    }
  }

  private val currentTestId = new java.util.concurrent.atomic.AtomicInteger(1)
  def nextTestId = currentTestId.getAndIncrement()

  case class FixtureParam(
    testId: Int,
    tracker: ActorRef,
    systemEventSink: Sink[SystemEvent])

  def withFixture(test: OneArgTest) = {
    val testId = nextTestId
    //info(s"Test $testId")
    val eventlogProps: Props = almhirt.eventlog.InMemoryAggregateRootEventLog.props()
    val eventlogActor: ActorRef = system.actorOf(eventlogProps, s"eventlog-$testId")
 
    implicit val almhirtContext = AlmhirtContext.TestContext.noComponentsDefaultGlobalDispatcher(s"almhirt-context-$testId", CommandStatusTrackerTests.this.ccuad, 5.seconds.dilated).awaitResultOrEscalate(5.seconds.dilated)
 
    val trackerProps = CommandStatusTracker.propsRaw(100, 110, trackerTimeoutScanInterval)
    val trackerActor = system.actorOf(trackerProps, s"tracker-$testId")
    val trackingSubscriber = CommandStatusTracker(trackerActor)
    val systemEventSink = Flow[SystemEvent].collect { case e: CommandStatusChanged ⇒ e }.connect(Sink(trackingSubscriber))

    try {
      withFixture(test.toNoArgTest(FixtureParam(testId, trackerActor, systemEventSink)))
    } finally {
      system.stop(trackerActor)
    }
  }

  override def beforeAll() {

  }

  override def afterAll() {
    TestKit.shutdownActorSystem(system)
  }

}