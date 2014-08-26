package almhirt.domain

import scala.language.postfixOps

import akka.testkit._
import org.scalatest._

import org.joda.time.{ DateTime, LocalDateTime, DateTimeZone }
import scala.concurrent._
import scala.concurrent.duration._
import akka.actor._
import org.reactivestreams.api.Consumer
import akka.stream.{ FlowMaterializer, MaterializerSettings }
import akka.stream.scaladsl.Flow
import almhirt.common._
import almhirt.aggregates._
import almhirt.aggregates.UserEventHandler
import almhirt.streaming.StreamBroker
import almhirt.problem.{ CauseIsThrowable, HasAThrowable }

class AggregateRootDirectViewTests(_system: ActorSystem)
  extends TestKit(_system) with fixture.WordSpecLike with Matchers with BeforeAndAfterAll {
  def this() = this(ActorSystem("AggregateRootDirectViewTests", almhirt.TestConfigs.logWarningConfig))

  implicit val executionContext = system.dispatchers.defaultGlobalDispatcher
  implicit val ccuad = CanCreateUuidsAndDateTimes()

  val theId = AggregateRootId("the_id")
  val mat = FlowMaterializer(MaterializerSettings())

  sealed trait UserTestRsp
  case class UserState(state: AggregateRootLifecycle[User]) extends UserTestRsp
  case class ViewFailure(problem: Problem) extends UserTestRsp

  case class NotAUserEvent(header: EventHeader, aggId: AggregateRootId, aggVersion: AggregateRootVersion) extends AggregateEvent

  "An AggregateRootDirectView" when {
    import aggregatesforthelazyones._
    import AggregateRootDroneInternalMessages._
    import almhirt.eventlog.AggregateEventLog._
    "receiving valid events" when {
      "no aggregate root exists" should {
        "not deliver an aggregate root" in { fixture ⇒
          val FixtureParam(testId, directView, drone, eventlog, eventBroker, statusProbe) = fixture
          val probe = TestProbe()
          within(1 second) {
            probe.send(directView, GetAggregateRootProjection)
            probe.expectMsg(UserState(Vacat))
          }
        }
        "deliver an aggregate root after one has been created" in { fixture ⇒
          val FixtureParam(testId, directView, drone, eventlog, eventBroker, statusProbe) = fixture
          val probe = TestProbe()
          within(1 second) {
            probe.send(directView, GetAggregateRootProjection)
            probe.expectMsg(UserState(Vacat))
            probe.send(drone, CreateUser(CommandHeader(), theId, 0L, "hans", "meier"))
            statusProbe.receiveN(2)

            probe.expectNoMsg(100 millis)
            probe.send(directView, GetAggregateRootProjection)
            val UserState(Vivus(state)) = probe.expectMsgType[UserState]
            state.version should equal(arv(1))
            state.id should equal(theId)
          }
        }
        "deliver an aggregate root after one has been created and then modified" in { fixture ⇒
          val FixtureParam(testId, directView, drone, eventlog, eventBroker, statusProbe) = fixture
          val probe = TestProbe()
          within(1 second) {
            probe.send(directView, GetAggregateRootProjection)
            probe.expectMsg(UserState(Vacat))
            probe.send(drone, CreateUser(CommandHeader(), theId, 0L, "hans", "meier"))
            statusProbe.receiveN(2)
            probe.send(drone, ChangeUserAgeForCreditCard(CommandHeader(), theId, 1L, 22))
            statusProbe.receiveN(2)

            probe.expectNoMsg(100 millis)
            probe.send(directView, GetAggregateRootProjection)
            val UserState(Vivus(state)) = probe.expectMsgType[UserState]
            state.version should equal(arv(2))
            state.id should equal(theId)
          }
        }
        "signal an aggregate root to be deleted after one has been created, modified and then deleted" in { fixture ⇒
          val FixtureParam(testId, directView, drone, eventlog, eventBroker, statusProbe) = fixture
          val probe = TestProbe()
          within(1 second) {
            probe.send(directView, GetAggregateRootProjection)
            probe.expectMsg(UserState(Vacat))
            probe.send(drone, CreateUser(CommandHeader(), theId, 0L, "hans", "meier"))
            statusProbe.receiveN(2)
            probe.send(drone, ChangeUserAgeForCreditCard(CommandHeader(), theId, 1L, 22))
            statusProbe.receiveN(2)
            probe.send(drone, ConfirmUserDeath(CommandHeader(), theId, 2L))
            statusProbe.receiveN(2)

            probe.expectNoMsg(100 millis)
            probe.send(directView, GetAggregateRootProjection)
            val UserState(Mortuus(id, v)) = probe.expectMsgType[UserState]
            v should equal(arv(3))
            id should equal(theId)
          }
        }
        "not be able to handle a complete lifecycle via events right after being started" in { fixture ⇒
          val FixtureParam(testId, directView, drone, eventlog, eventBroker, statusProbe) = fixture
          val probe = TestProbe()
          within(1 second) {
            val flow = Flow(List[Event](
              UserCreated(EventHeader(), theId, 0L, "hans", "meier"),
              UserLastnameChanged(EventHeader(), theId, 1L, "müller"),
              UserDied(EventHeader(), theId, 2L)))
            val eventsConsumer = eventBroker.newConsumer()
            flow.produceTo(mat, eventsConsumer)

            probe.expectNoMsg(100 millis)
            probe.send(directView, GetAggregateRootProjection)
            val userState = probe.expectMsgType[UserState]
            userState should equal(UserState(Vacat))
          }
        }
        "be able to handle a complete lifecycle via events once it has been initialized" in { fixture ⇒
          val FixtureParam(testId, directView, drone, eventlog, eventBroker, statusProbe) = fixture
          val probe = TestProbe()
          within(1 second) {
            probe.send(directView, GetAggregateRootProjection)
            probe.expectMsgType[UserState]

            val flow = Flow(List[Event](
              UserCreated(EventHeader(), theId, 0L, "hans", "meier"),
              UserLastnameChanged(EventHeader(), theId, 1L, "müller"),
              UserDied(EventHeader(), theId, 2L)))
            val eventsConsumer = eventBroker.newConsumer()
            flow.produceTo(mat, eventsConsumer)

            probe.expectNoMsg(100 millis)
            probe.send(directView, GetAggregateRootProjection)
            val UserState(Mortuus(id, v)) = probe.expectMsgType[UserState]
            v should equal(arv(3))
            id should equal(theId)
          }
        }
      }
      "an aggregate root exists" should {
        "deliver an aggregate root" in { fixture ⇒
          val FixtureParam(testId, directView, drone, eventlog, eventBroker, statusProbe) = fixture
          val probe = TestProbe()
          within(1 second) {
            probe.send(eventlog, CommitAggregateEvent(UserCreated(EventHeader(), theId, 0L, "hans", "meier")))
            probe.send(eventlog, CommitAggregateEvent(UserLastnameChanged(EventHeader(), theId, 1L, "müller")))
            probe.receiveN(2)

            probe.expectNoMsg(100 millis)
            probe.send(directView, GetAggregateRootProjection)
            val UserState(Vivus(state)) = probe.expectMsgType[UserState]
            state.version should equal(arv(2))
            state.id should equal(theId)
          }
        }
      }
      "a deleted aggregate root exists" should {
        "signal deletion" in { fixture ⇒
          val FixtureParam(testId, directView, drone, eventlog, eventBroker, statusProbe) = fixture
          val probe = TestProbe()
          within(1 second) {
            probe.send(eventlog, CommitAggregateEvent(UserCreated(EventHeader(), theId, 0L, "hans", "meier")))
            probe.send(eventlog, CommitAggregateEvent(UserLastnameChanged(EventHeader(), theId, 1L, "müller")))
            probe.send(eventlog, CommitAggregateEvent(UserDied(EventHeader(), theId, 2L)))
            probe.receiveN(3)

            probe.expectNoMsg(100 millis)
            probe.send(directView, GetAggregateRootProjection)
            val UserState(Mortuus(id, version)) = probe.expectMsgType[UserState]
            version should equal(arv(3))
            id should equal(theId)
          }
        }
      }
    }
    "receiving invalid events" when {
      "the eventlog is corrupted(versions do not match and event handler doesn't check)" should {
        "deliver a potentially corrupted aggregate root" in { fixture ⇒
          val FixtureParam(testId, directView, drone, eventlog, eventBroker, statusProbe) = fixture
          val probe = TestProbe()
          within(1 second) {
            probe.send(eventlog, CommitAggregateEvent(UserCreated(EventHeader(), theId, 0L, "hans", "meier")))
            probe.send(eventlog, CommitAggregateEvent(UserLastnameChanged(EventHeader(), theId, 7L, "müller")))
            probe.receiveN(2)

            probe.send(directView, GetAggregateRootProjection)
            val UserState(Vivus(ar)) = probe.expectMsgType[UserState]
            ar.version should equal(arv(2))
            ar.id should equal(theId)
          }
        }
      }
      "the eventlog is corrupted(wrong type of event)" should {
        "deliver a potentially corrupted aggregate root" in { fixture ⇒
          val FixtureParam(testId, directView, drone, eventlog, eventBroker, statusProbe) = fixture
          val probe = TestProbe()
          within(1 second) {
            probe.send(eventlog, CommitAggregateEvent(UserCreated(EventHeader(), theId, 0L, "hans", "meier")))
            probe.send(eventlog, CommitAggregateEvent(NotAUserEvent(EventHeader(), theId, 1L)))
            probe.receiveN(2)

            probe.send(directView, GetAggregateRootProjection)
            val ViewFailure(UnspecifiedProblem(p)) = probe.expectMsgType[ViewFailure]
            val Some(CauseIsThrowable(HasAThrowable(exn))) = p.cause 
            exn should be(RebuildAggregateRootFailedException)
          }
        }
      }
    }
  }

  private val currentTestId = new java.util.concurrent.atomic.AtomicInteger(1)
  def nextTestId = currentTestId.getAndIncrement()

  case class FixtureParam(
    testId: Int,
    directView: ActorRef,
    drone: ActorRef,
    eventlog: ActorRef,
    eventBroker: StreamBroker[Event],
    statusProbe: TestProbe)

  def withFixture(test: OneArgTest) = {
    import almhirt.streaming._
    import almhirt.tracking.CommandStatusChanged
    import almhirt.aggregates._

    val testId = nextTestId
    info(s"Test $testId")
    val eventlogProps: Props = almhirt.eventlog.InMemoryAggregateEventLog.props()
    val eventlogActor: ActorRef = system.actorOf(eventlogProps, s"eventlog-$testId")

    val (streaming, stopStreaming) = AlmhirtStreams.supervised(s"streaming-$testId").awaitResultOrEscalate(1 second)

    val statusProbe = TestProbe()
    val cmdStatusSink = FireAndForgetSink.delegating[CommandStatusChanged](elem ⇒ statusProbe.ref ! elem)

    val eventsPostOfficeProps = VillagePostOffice.props[Event](streaming.eventBroker, 2)
    val eventsPostOfficeActor = system.actorOf(eventsPostOfficeProps, s"events-post-office-$testId")
    val theEventsPostOffice = PostOffice[Event](eventsPostOfficeActor)

    val droneProps: Props = Props(
      new Actor with ActorLogging with AggregateRootDrone[User, UserEvent] with UserEventHandler with UserCommandHandler with UserUpdater with AggregateRootDroneCommandHandlerAdaptor[User, UserEvent] with SequentialPostOfficeClient {
        def ccuad = AggregateRootDirectViewTests.this.ccuad
        def futuresContext: ExecutionContext = executionContext
        def aggregateEventLog: ActorRef = eventlogActor
        def snapshotStorage: Option[ActorRef] = None
        val commandStatusSink = cmdStatusSink
        val postOfficeSettings = PostOfficeClientSettings(100, (50 millis).dilated, 10)
        val eventsPostOffice = theEventsPostOffice
      })

    val droneActor = system.actorOf(droneProps, s"drone-$testId")

    val viewProps = Props(new AggregateRootDirectView[User, UserEvent](
      theId, eventlogActor, None,
      (lc, receiver) => receiver ! UserState(lc),
      (prob, receiver) => receiver ! ViewFailure(prob)) with UserEventHandler)

    val viewActor = system.actorOf(viewProps, s"view-$testId")

    Flow(streaming.aggregateEventStream).foreach(event => viewActor ! ApplyAggregateEvent(event)).consume(mat)

    try {
      withFixture(test.toNoArgTest(FixtureParam(testId, viewActor, droneActor, eventlogActor, streaming.eventBroker, statusProbe)))
    } finally {
      stopStreaming.stop()
      system.stop(droneActor)
      system.stop(viewActor)
      //system.stop(eventsPostOfficeActor)
      system.stop(eventlogActor)
    }
  }

  override def beforeAll() {

  }

  override def afterAll() {
    TestKit.shutdownActorSystem(system)
  }

}