package almhirt.components

import scala.language.implicitConversions
import scala.language.postfixOps

import scala.concurrent.duration._
import akka.actor._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.tracking._

import org.reactivestreams.Subscriber
import akka.stream.{ FlowMaterializer, MaterializerSettings }
import akka.stream.scaladsl.Flow

import akka.testkit._
import org.scalatest._

class CommandStatusTrackerTests(_system: ActorSystem)
  extends TestKit(_system) with fixture.WordSpecLike with Matchers with BeforeAndAfterAll {
  def this() = this(ActorSystem("CommandStatusTrackerTests", almhirt.TestConfigs.logWarningConfig))

  implicit val mat = FlowMaterializer(MaterializerSettings())

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
        """notify the subscriber with a "Executed""" in { fixture =>
          val FixtureParam(testId, tracker, eventSubscriber) = fixture
          val probe = TestProbe()
          Flow(createEvent("a", CommandStatus.Executed) :: Nil).produceTo(eventSubscriber)
          system.scheduler.scheduleOnce(100.millis.dilated)(tracker ! TrackCommandMapped("a", res => probe.ref ! res, (100.millis.dilated).fromNow))
          probe.expectMsg(TrackedExecutued)
        }
      }
      """a "NotExecuted" status is received and then a matching subscription""" should {
        """notify the subscriber with a "NotExecuted"""" in { fixture =>
          val FixtureParam(testId, tracker, eventSubscriber) = fixture
          val probe = TestProbe()
          val status = CommandStatus.NotExecuted(UnspecifiedProblem(""))
          Flow(createEvent("a", status) :: Nil).produceTo(eventSubscriber)
          system.scheduler.scheduleOnce(100.millis.dilated)(tracker ! TrackCommandMapped("a", res => probe.ref ! res, (100.millis.dilated).fromNow))
          tracker ! TrackCommandMapped("a", res => probe.ref ! res, (100.millis.dilated).fromNow)
          probe.expectMsg(TrackedNotExecutued(status.cause))
        }
      }
    }
    "receiving a single subscription and then a single status" when {
      """a subscription is received and then a matching "Executed" status""" should {
        """notify the subscriber with a "Executed"""" in { fixture =>
          val FixtureParam(testId, tracker, eventSubscriber) = fixture
          val probe = TestProbe()
          tracker ! TrackCommandMapped("a", res => probe.ref ! res, (200.millis.dilated).fromNow)
          Flow(createEvent("a", CommandStatus.Executed) :: Nil).produceTo(eventSubscriber)
          probe.expectMsg(TrackedExecutued)
        }
      }
      """a subscription is received and then a non-matching "Executed" status""" should {
        """notify the subscriber with a "Timeout"""" in { fixture =>
          val FixtureParam(testId, tracker, eventSubscriber) = fixture
          val probe = TestProbe()
          tracker ! TrackCommandMapped("a", res => probe.ref ! res, (200.millis.dilated).fromNow)
          Flow(createEvent("b", CommandStatus.Executed) :: Nil).produceTo(eventSubscriber)
          probe.expectMsg(TrackedTimeout)
        }
      }
      """a subscription is received and then a matching "NotExecuted" status""" should {
        """notify the subscriber with a "NotExecuted"""" in { fixture =>
          val FixtureParam(testId, tracker, eventSubscriber) = fixture
          val probe = TestProbe()
          val status = CommandStatus.NotExecuted(UnspecifiedProblem(""))
          tracker ! TrackCommandMapped("a", res => probe.ref ! res, (100.millis.dilated).fromNow)
          Flow(createEvent("a", status) :: Nil).produceTo(eventSubscriber)
          probe.expectMsg(TrackedNotExecutued(status.cause))
        }
      }
      """a subscription is received and then a non-matching "NotExecuted" status""" should {
        """notify the subscriber with a "Timeout"""" in { fixture =>
          val FixtureParam(testId, tracker, eventSubscriber) = fixture
          val probe = TestProbe()
          val status = CommandStatus.NotExecuted(UnspecifiedProblem(""))
          tracker ! TrackCommandMapped("a", res => probe.ref ! res, (100.millis.dilated).fromNow)
          Flow(createEvent("b", status) :: Nil).produceTo(eventSubscriber)
          probe.expectMsg(TrackedTimeout)
        }
      }
      """a subscription is received and then a matching "Executed" status after the subscriptions deadline""" should {
        """notify the subscriber with a "Timeout"""" in { fixture =>
          val FixtureParam(testId, tracker, eventSubscriber) = fixture
          val probe = TestProbe()
          tracker ! TrackCommandMapped("a", res => probe.ref ! res, (100.millis.dilated).fromNow)
          system.scheduler.scheduleOnce(trackerTimeoutScanInterval)(Flow(createEvent("a", CommandStatus.Executed) :: Nil).produceTo(eventSubscriber))
          probe.expectMsg(TrackedTimeout)
        }
        """not notify the subscriber with a "Executed" after the timeout""" in { fixture =>
          val FixtureParam(testId, tracker, eventSubscriber) = fixture
          val probe = TestProbe()
          tracker ! TrackCommandMapped("a", res => probe.ref ! res, (100.millis.dilated).fromNow)
          system.scheduler.scheduleOnce(trackerTimeoutScanInterval)(Flow(createEvent("a", CommandStatus.Executed) :: Nil).produceTo(eventSubscriber))
          probe.expectMsg(TrackedTimeout)
          probe.expectNoMsg(2 * trackerTimeoutScanInterval)
        }
      }
      """a subscription is received and then a matching "NotExecuted" status after the subscriptions deadline""" should {
        """notify the subscriber with a "Timeout"""" in { fixture =>
          val FixtureParam(testId, tracker, eventSubscriber) = fixture
          val probe = TestProbe()
          val status = CommandStatus.NotExecuted(UnspecifiedProblem(""))
          tracker ! TrackCommandMapped("a", res => probe.ref ! res, (100.millis.dilated).fromNow)
          system.scheduler.scheduleOnce(trackerTimeoutScanInterval)(Flow(createEvent("a", status) :: Nil).produceTo(eventSubscriber))
          probe.expectMsg(TrackedTimeout)
        }
        """not notify the subscriber with a "NotExecuted" after the timeout""" in { fixture =>
          val FixtureParam(testId, tracker, eventSubscriber) = fixture
          val probe = TestProbe()
          val status = CommandStatus.NotExecuted(UnspecifiedProblem(""))
          tracker ! TrackCommandMapped("a", res => probe.ref ! res, (100.millis.dilated).fromNow)
          system.scheduler.scheduleOnce(trackerTimeoutScanInterval)(Flow(createEvent("a", status) :: Nil).produceTo(eventSubscriber))
          probe.expectMsg(TrackedTimeout)
          probe.expectNoMsg(2 * trackerTimeoutScanInterval)
        }
      }
    }
    "receiving a single status and then multiple subscriptions" when {
      """a "Executed" status is received and then 2 matching subscriptions""" should {
        """notify the subscribers with a "Executed"""" in { fixture =>
          val FixtureParam(testId, tracker, eventSubscriber) = fixture
          val probe = TestProbe()
          Flow(createEvent("a", CommandStatus.Executed) :: Nil).produceTo(eventSubscriber)
          system.scheduler.scheduleOnce(100.millis.dilated) {
            tracker ! TrackCommandMapped("a", res => probe.ref ! res, (100.millis.dilated).fromNow)
            tracker ! TrackCommandMapped("a", res => probe.ref ! res, (100.millis.dilated).fromNow)
          }
          probe.expectMsg(TrackedExecutued)
          probe.expectMsg(TrackedExecutued)
        }
      }
      """a "NotExecuted" status is received and then 2 matching subscriptions""" should {
        """notify the subscribers with a "NotExecuted"""" in { fixture =>
          val FixtureParam(testId, tracker, eventSubscriber) = fixture
          val probe = TestProbe()
          val status = CommandStatus.NotExecuted(UnspecifiedProblem(""))
          Flow(createEvent("a", status) :: Nil).produceTo(eventSubscriber)
          system.scheduler.scheduleOnce(100.millis.dilated) {
            tracker ! TrackCommandMapped("a", res => probe.ref ! res, (100.millis.dilated).fromNow)
            tracker ! TrackCommandMapped("a", res => probe.ref ! res, (100.millis.dilated).fromNow)
          }
          probe.expectMsg(TrackedNotExecutued(status.cause))
          probe.expectMsg(TrackedNotExecutued(status.cause))
        }
      }
      """a "Executed" status is received and then 1 matching and one non-matching subscription""" should {
        """notify the matching subscriber with a "Executed" and the non-matching with a timeout""" in { fixture =>
          val FixtureParam(testId, tracker, eventSubscriber) = fixture
          val probe = TestProbe()
          Flow(createEvent("a", CommandStatus.Executed) :: Nil).produceTo(eventSubscriber)
          system.scheduler.scheduleOnce(100.millis.dilated) {
            tracker ! TrackCommandMapped("b", res => probe.ref ! res, (100.millis.dilated).fromNow)
            tracker ! TrackCommandMapped("a", res => probe.ref ! res, (100.millis.dilated).fromNow)
          }
          probe.expectMsg(TrackedExecutued)
          probe.expectMsg(TrackedTimeout)
        }
      }
      """a "NotExecuted" status is received and then 1 matching and one non-matching subscription""" should {
        """notify the matching subscriber with a "NotExecuted" and the non-matching with a timeout"""" in { fixture =>
          val FixtureParam(testId, tracker, eventSubscriber) = fixture
          val probe = TestProbe()
          val status = CommandStatus.NotExecuted(UnspecifiedProblem(""))
          Flow(createEvent("a", status) :: Nil).produceTo(eventSubscriber)
          system.scheduler.scheduleOnce(100.millis.dilated) {
            tracker ! TrackCommandMapped("b", res => probe.ref ! res, (100.millis.dilated).fromNow)
            tracker ! TrackCommandMapped("a", res => probe.ref ! res, (100.millis.dilated).fromNow)
          }
          probe.expectMsg(TrackedNotExecutued(status.cause))
          probe.expectMsg(TrackedTimeout)
        }
      }
    }
    "receiving a single status between 2 subscriptions" when {
      """a status is received and none of the subscriptions timed out""" should {
        """notify the subscribers with the status""" in { fixture =>
          val FixtureParam(testId, tracker, eventSubscriber) = fixture
          val probe = TestProbe()
          tracker ! TrackCommandMapped("a", res => probe.ref ! res, (500.millis.dilated).fromNow)
          system.scheduler.scheduleOnce(200.millis.dilated)(Flow(createEvent("a", CommandStatus.Executed) :: Nil).produceTo(eventSubscriber))
          system.scheduler.scheduleOnce(300.millis.dilated)(tracker ! TrackCommandMapped("a", res => probe.ref ! res, (100.millis.dilated).fromNow))
          probe.expectMsg(TrackedExecutued)
          probe.expectMsg(TrackedExecutued)
        }
      }
      """a status is received and the first subscription already timed out""" should {
        """notify one subscriber with the status and the other with a timeout""" in { fixture =>
          val FixtureParam(testId, tracker, eventSubscriber) = fixture
          val probe = TestProbe()
          tracker ! TrackCommandMapped("a", res => probe.ref ! res, (100.millis.dilated).fromNow)
          system.scheduler.scheduleOnce(trackerTimeoutScanInterval + 100.millis.dilated)(Flow(createEvent("a", CommandStatus.Executed) :: Nil).produceTo(eventSubscriber))
          system.scheduler.scheduleOnce(trackerTimeoutScanInterval + 200.millis.dilated)(tracker ! TrackCommandMapped("a", res => probe.ref ! res, (100.millis.dilated).fromNow))
          probe.expectMsg(TrackedTimeout)
          probe.expectMsg(TrackedExecutued)
        }
      }
    }
    "just receiveing subscriptions" should {
      """notify each subscriber with a timeout""" in { fixture =>
        val n = 100
        val FixtureParam(testId, tracker, eventSubscriber) = fixture
        val probe = TestProbe()
        (1 to n).foreach(i => tracker ! TrackCommandMapped(s"$i", res => probe.ref ! res, (100.millis.dilated).fromNow))
        val count = probe.receiveN(n).collect { case TrackedTimeout => 1 }.sum
        count should equal(n)
      }
    }
    "receiving many status events and many subscriptions" when {
      "the subscriptions are reveived before the events" should {
        """notify each subscriber""" in { fixture =>
          val n = 100
          val FixtureParam(testId, tracker, eventSubscriber) = fixture
          val probe = TestProbe()
          (1 to n).foreach(i => tracker ! TrackCommandMapped(s"$i", res => probe.ref ! res, (500.millis.dilated).fromNow))
          Flow((1 to n).map(i => createEvent(s"$i", CommandStatus.Executed))).produceTo(eventSubscriber)
          val count = probe.receiveN(n).collect { case TrackedExecutued => 1 }.sum
          count should equal(n)
        }
      }
      "the events are reveived before the subscriptions" should {
        """notify each subscriber""" in { fixture =>
          val n = 100
          val FixtureParam(testId, tracker, eventSubscriber) = fixture
          val probe = TestProbe()
          Flow((1 to n).map(i => createEvent(s"$i", CommandStatus.Executed))).produceTo(eventSubscriber)
          system.scheduler.scheduleOnce(200.millis.dilated) {
            (1 to n).foreach(i => tracker ! TrackCommandMapped(s"$i", res => probe.ref ! res, (100.millis.dilated).fromNow))
          }
          val count = probe.receiveN(n).collect { case TrackedExecutued => 1 }.sum
          count should equal(n)
        }
      }
      "the events and subscriptions have no specific order" should {
        """notify each subscriber""" in { fixture =>
          val n = 1000
          val FixtureParam(testId, tracker, eventSubscriber) = fixture
          val probe = TestProbe()
          Flow((1 to n).map(i => createEvent(s"$i", CommandStatus.Executed))).produceTo(eventSubscriber)
          val deadline = (500.millis.dilated).fromNow
          (1 to n).foreach(i => tracker ! TrackCommandMapped(s"$i", res => probe.ref ! res, deadline))
          val tracked = probe.receiveN(n)
          val executed = tracked.collect { case TrackedExecutued => 1 }.sum
          val timedout = tracked.collect { case TrackedTimeout => 1 }.sum
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
    eventSubscriber: Subscriber[SystemEvent])

  def withFixture(test: OneArgTest) = {
    val testId = nextTestId
    //info(s"Test $testId")
    val eventlogProps: Props = almhirt.eventlog.InMemoryAggregateRootEventLog.props()
    val eventlogActor: ActorRef = system.actorOf(eventlogProps, s"eventlog-$testId")

    val trackerProps = CommandStatusTracker.props(100, 110, trackerTimeoutScanInterval)
    val trackerActor = system.actorOf(trackerProps, s"tracker-$testId")

    val trackerSubscriber = CommandStatusTracker.systemEventSubscriber(trackerActor)
    try {
      withFixture(test.toNoArgTest(FixtureParam(testId, trackerActor, trackerSubscriber)))
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