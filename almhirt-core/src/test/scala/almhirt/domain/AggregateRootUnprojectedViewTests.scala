package almhirt.domain

import scala.language.postfixOps
import scala.concurrent._
import scala.concurrent.duration._
import akka.actor._
import org.reactivestreams.Subscriber
import akka.stream.scaladsl2._
import almhirt.common._
import almhirt.aggregates._
import almhirt.problem.{ CauseIsThrowable, HasAThrowable }
import almhirt.tracking.CommandStatusChanged
import almhirt.streaming._

import akka.testkit._
import org.scalatest._

class AggregateRootUnprojectedViewTests(_system: ActorSystem)
  extends TestKit(_system) with fixture.WordSpecLike with Matchers with BeforeAndAfterAll {
  def this() = this(ActorSystem("AggregateRootUnprojectedViewTests", almhirt.TestConfigs.logWarningConfig))

  implicit val executionContext = system.dispatchers.defaultGlobalDispatcher
  implicit val ccuad = CanCreateUuidsAndDateTimes()

  val theId = AggregateRootId("the_id")
  implicit val mat = FlowMaterializer()

  sealed trait UserTestRsp
  case class UserState(state: AggregateRootLifecycle[User]) extends UserTestRsp
  case class ViewFailure(problem: Problem) extends UserTestRsp

  case class NotAUserEvent(header: EventHeader, aggId: AggregateRootId, aggVersion: AggregateRootVersion) extends AggregateRootEvent

  "An AggregateRootUnprojectedView" when {
    import aggregatesforthelazyones._
    import AggregateRootDroneInternalMessages._
    import almhirt.eventlog.AggregateRootEventLog._
    "receiving valid events" when {
      "no aggregate root exists" should {
        "not deliver an aggregate root" in { fixture ⇒
          val FixtureParam(testId, directView, drone, droneProbe, eventlog, streams) = fixture
          val probe = TestProbe()
          within(1 second) {
            probe.send(directView, AggregateRootViewMessages.GetAggregateRootProjection)
            probe.expectMsg(UserState(Vacat))
          }
        }
        "deliver an aggregate root after one has been created" in { fixture ⇒
          val FixtureParam(testId, directView, drone, droneProbe, eventlog, streams) = fixture
          val probe = TestProbe()
          within(1 second) {
            probe.send(directView, AggregateRootViewMessages.GetAggregateRootProjection)
            probe.expectMsg(UserState(Vacat))
            probe.send(drone, CreateUser(CommandHeader(), theId, 0L, "hans", "meier"))

            probe.expectNoMsg(100 millis)
            probe.send(directView, AggregateRootViewMessages.GetAggregateRootProjection)
            val UserState(Vivus(state)) = probe.expectMsgType[UserState]
            state.version should equal(arv(1))
            state.id should equal(theId)
          }
        }
        "deliver an aggregate root after one has been created and then modified" in { fixture ⇒
          val FixtureParam(testId, directView, drone, droneProbe, eventlog, streams) = fixture
          val probe = TestProbe()
          within(1 second) {
            probe.send(directView, AggregateRootViewMessages.GetAggregateRootProjection)
            probe.expectMsg(UserState(Vacat))
            probe.send(drone, CreateUser(CommandHeader(), theId, 0L, "hans", "meier"))
            droneProbe.expectMsgType[AggregateRootDroneInternalMessages.ExecuteCommandResponse]
            probe.send(drone, ChangeUserAgeForCreditCard(CommandHeader(), theId, 1L, 22))
            droneProbe.expectMsgType[AggregateRootDroneInternalMessages.ExecuteCommandResponse]

            probe.expectNoMsg(100 millis)
            probe.send(directView, AggregateRootViewMessages.GetAggregateRootProjection)
            val UserState(Vivus(state)) = probe.expectMsgType[UserState]
            state.version should equal(arv(2))
            state.id should equal(theId)
          }
        }
        "signal an aggregate root to be deleted after one has been created, modified and then deleted" in { fixture ⇒
          val FixtureParam(testId, directView, drone, droneProbe, eventlog, streams) = fixture
          val probe = TestProbe()
          within(1 second) {
            probe.send(directView, AggregateRootViewMessages.GetAggregateRootProjection)
            probe.expectMsg(UserState(Vacat))
            probe.send(drone, CreateUser(CommandHeader(), theId, 0L, "hans", "meier"))
            droneProbe.expectMsgType[AggregateRootDroneInternalMessages.ExecuteCommandResponse]
            probe.send(drone, ChangeUserAgeForCreditCard(CommandHeader(), theId, 1L, 22))
            droneProbe.expectMsgType[AggregateRootDroneInternalMessages.ExecuteCommandResponse]
            probe.send(drone, ConfirmUserDeath(CommandHeader(), theId, 2L))
            droneProbe.expectMsgType[AggregateRootDroneInternalMessages.ExecuteCommandResponse]

            probe.expectNoMsg(100 millis)
            probe.send(directView, AggregateRootViewMessages.GetAggregateRootProjection)
            val UserState(Mortuus(id, v)) = probe.expectMsgType[UserState]
            v should equal(arv(3))
            id should equal(theId)
          }
        }
        "not be able to handle a complete lifecycle via events right after being started" in { fixture ⇒
          val FixtureParam(testId, directView, drone, droneProbe, eventlog, streams) = fixture
          val probe = TestProbe()
          within(1 second) {
            val flow = FlowFrom(List[Event](
              UserCreated(EventHeader(), theId, 0L, "hans", "meier"),
              UserLastnameChanged(EventHeader(), theId, 1L, "müller"),
              UserDied(EventHeader(), theId, 2L)))
            val eventsSubscriber = streams.eventBroker.newSubscriber()
            flow.publishTo(eventsSubscriber)

            probe.expectNoMsg(100 millis)
            probe.send(directView, AggregateRootViewMessages.GetAggregateRootProjection)
            val userState = probe.expectMsgType[UserState]
            userState should equal(UserState(Vacat))
          }
        }
        "be able to handle a complete lifecycle via events once it has been initialized" in { fixture ⇒
          val FixtureParam(testId, directView, drone, droneProbe, eventlog, streams) = fixture
          val probe = TestProbe()
          within(1 second) {
            probe.send(directView, AggregateRootViewMessages.GetAggregateRootProjection)
            probe.expectMsgType[UserState]

            val flow = FlowFrom(List[Event](
              UserCreated(EventHeader(), theId, 0L, "hans", "meier"),
              UserLastnameChanged(EventHeader(), theId, 1L, "müller"),
              UserDied(EventHeader(), theId, 2L)))
            val eventsSubscriber = streams.eventBroker.newSubscriber()
            flow.publishTo(eventsSubscriber)

            probe.expectNoMsg(100 millis)
            probe.send(directView, AggregateRootViewMessages.GetAggregateRootProjection)
            val UserState(Mortuus(id, v)) = probe.expectMsgType[UserState]
            v should equal(arv(3))
            id should equal(theId)
          }
        }
      }
      "an aggregate root exists" should {
        "deliver an aggregate root" in { fixture ⇒
          val FixtureParam(testId, directView, drone, droneProbe, eventlog, streams) = fixture
          val probe = TestProbe()
          within(1 second) {
            probe.send(eventlog, CommitAggregateRootEvent(UserCreated(EventHeader(), theId, 0L, "hans", "meier")))
            probe.send(eventlog, CommitAggregateRootEvent(UserLastnameChanged(EventHeader(), theId, 1L, "müller")))
            probe.receiveN(2)

            probe.expectNoMsg(100 millis)
            probe.send(directView, AggregateRootViewMessages.GetAggregateRootProjection)
            val UserState(Vivus(state)) = probe.expectMsgType[UserState]
            state.version should equal(arv(2))
            state.id should equal(theId)
          }
        }
      }
      "a deleted aggregate root exists" should {
        "signal deletion" in { fixture ⇒
          val FixtureParam(testId, directView, drone, droneProbe, eventlog, streams) = fixture
          val probe = TestProbe()
          within(1 second) {
            probe.send(eventlog, CommitAggregateRootEvent(UserCreated(EventHeader(), theId, 0L, "hans", "meier")))
            probe.send(eventlog, CommitAggregateRootEvent(UserLastnameChanged(EventHeader(), theId, 1L, "müller")))
            probe.send(eventlog, CommitAggregateRootEvent(UserDied(EventHeader(), theId, 2L)))
            probe.receiveN(3)

            probe.expectNoMsg(100 millis)
            probe.send(directView, AggregateRootViewMessages.GetAggregateRootProjection)
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
          val FixtureParam(testId, directView, drone, droneProbe, eventlog, streams) = fixture
          val probe = TestProbe()
          within(1 second) {
            probe.send(eventlog, CommitAggregateRootEvent(UserCreated(EventHeader(), theId, 0L, "hans", "meier")))
            probe.send(eventlog, CommitAggregateRootEvent(UserLastnameChanged(EventHeader(), theId, 7L, "müller")))
            probe.receiveN(2)

            probe.send(directView, AggregateRootViewMessages.GetAggregateRootProjection)
            val UserState(Vivus(ar)) = probe.expectMsgType[UserState]
            ar.version should equal(arv(2))
            ar.id should equal(theId)
          }
        }
      }
      "the eventlog is corrupted(wrong type of event)" should {
        "fail to aggregate root" in { fixture ⇒
          val FixtureParam(testId, directView, drone, droneProbe, eventlog, streams) = fixture
          val probe = TestProbe()
          within(1 second) {
            probe.send(eventlog, CommitAggregateRootEvent(UserCreated(EventHeader(), theId, 0L, "hans", "meier")))
            probe.send(eventlog, CommitAggregateRootEvent(NotAUserEvent(EventHeader(), theId, 1L)))
            probe.receiveN(2)

            probe.send(directView, AggregateRootViewMessages.GetAggregateRootProjection)
            val ViewFailure(UnspecifiedProblem(p)) = probe.expectMsgType[ViewFailure]
            val Some(CauseIsThrowable(HasAThrowable(exn))) = p.cause
            exn.isInstanceOf[RebuildAggregateRootFailedException] should be(true)
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
    droneProbe: TestProbe,
    eventlog: ActorRef,
    streams: AlmhirtStreams)

  def withFixture(test: OneArgTest) = {
    import almhirt.streaming._
    import almhirt.tracking.CommandStatusChanged
    import almhirt.aggregates._

    val testId = nextTestId
    info(s"Test $testId")
    val eventlogProps: Props = almhirt.eventlog.InMemoryAggregateRootEventLog.props()
    val eventlogActor: ActorRef = system.actorOf(eventlogProps, s"eventlog-$testId")
    val droneProbe = TestProbe()

    val streams = AlmhirtStreams(s"almhirt-streams-$testId")(1 second).awaitResultOrEscalate(1 second)

    val droneProps: Props = Props(
      new AggregateRootDrone[User, UserEvent] with ActorLogging with UserEventHandler with UserCommandHandler with UserUpdater with AggregateRootDroneCommandHandlerAdaptor[User, UserCommand, UserEvent] {
        def ccuad = AggregateRootUnprojectedViewTests.this.ccuad
        def futuresContext: ExecutionContext = executionContext
        def aggregateEventLog: ActorRef = eventlogActor
        def snapshotStorage: Option[ActorRef] = None
        val eventsBroker: StreamBroker[Event] = streams.eventBroker

        override val aggregateCommandValidator = AggregateRootCommandValidator.Validated
        override val tag = scala.reflect.ClassTag[UserCommand](classOf[UserCommand])

        override def sendMessage(msg: AggregateRootDroneInternalMessages.AggregateDroneMessage) {
          droneProbe.ref ! msg
        }
      })

    val droneActor = system.actorOf(droneProps, s"drone-$testId")

    val viewProps = Props(new AggregateRootUnprojectedView[User, UserEvent](
      theId, eventlogActor, None,
      (lc, receiver) ⇒ receiver ! UserState(lc),
      (prob, receiver) ⇒ receiver ! ViewFailure(prob)) with UserEventHandler {
      override def confirmAggregateRootEventHandled() {
      }
    })

    val viewActor = system.actorOf(viewProps, s"view-$testId")

    FlowFrom(streams.eventStream).collect{ case e: AggregateRootEvent => e}.withSink(ForeachSink(event ⇒ viewActor ! AggregateRootViewMessages.ApplyAggregateRootEvent(event)))

    try {
      withFixture(test.toNoArgTest(FixtureParam(testId, viewActor, droneActor, droneProbe, eventlogActor, streams)))
    } finally {
      streams.stop()
      system.stop(droneActor)
      system.stop(viewActor)
      system.stop(eventlogActor)
    }
  }

  override def beforeAll() {

  }

  override def afterAll() {
    TestKit.shutdownActorSystem(system)
  }

}