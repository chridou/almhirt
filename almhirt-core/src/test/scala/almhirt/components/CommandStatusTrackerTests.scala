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
  
  implicit def str2CommandId(str: String): CommandId = CommandId(str)  
    
  "The CommandStatusTracker" when {
    import CommandStatusTracker._
    "receiving events that notify about command results" when {
      "a success status is received and then a matching subscription" should {
        """notify the subscriber with a "Executed""" in { fixture =>
          val FixtureParam(testId, tracker, eventSubscriber) = fixture
          val probe = TestProbe()
          Flow(createEvent("a", CommandStatus.Executed) :: Nil).produceTo(eventSubscriber)
          Thread.sleep(100.millis.dilated.toMillis)
          tracker ! TrackCommand("a", res => probe.ref ! (res.forceResult), (100.millis.dilated).fromNow)
          probe.expectMsg(CommandStatus.NotExecuted)
        }
      }
      "a failure status is received and then a matching subscription" should {
        """notify the subscriber with a "NotExecuted"""" in { fixture =>
          val FixtureParam(testId, tracker, eventSubscriber) = fixture
          val probe = TestProbe()
          val status = CommandStatus.NotExecuted(UnspecifiedProblem(""))
          Flow(createEvent("a", status) :: Nil).produceTo(eventSubscriber)
          Thread.sleep(100.millis.dilated.toMillis)
          tracker ! TrackCommand("a", res => probe.ref ! (res.forceResult), (100.millis.dilated).fromNow)
          probe.expectMsg(status)
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
    val eventlogProps: Props = almhirt.eventlog.InMemoryAggregateEventLog.props()
    val eventlogActor: ActorRef = system.actorOf(eventlogProps, s"eventlog-$testId")

    val trackerProps = CommandStatusTracker.props(5, 8, 500.millis.dilated)
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