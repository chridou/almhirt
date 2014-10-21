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
import almhirt.context.AlmhirtContext

class AggregateRootViewsTests(_system: ActorSystem)
  extends TestKit(_system) with fixture.WordSpecLike with Matchers with BeforeAndAfterAll {
  def this() = this(ActorSystem("AggregateRootUnprojectedViewTests", almhirt.TestConfigs.logErrorConfig))

  implicit val executionContext = system.dispatchers.defaultGlobalDispatcher
  implicit val ccuad = CanCreateUuidsAndDateTimes()

  implicit val mat = FlowMaterializer()

  sealed trait UserTestRsp
  case class UserState(state: AggregateRootLifecycle[User]) extends UserTestRsp
  case class ViewFailure(problem: Problem) extends UserTestRsp

  case class NotAUserEvent(header: EventHeader, aggId: AggregateRootId, aggVersion: AggregateRootVersion) extends AggregateRootEvent

  info("These tests have timing issues since one can not really tell, whether commands overlap unless effort is taken to prevent this.")

  "An AggregateRootViews" when {
    import aggregatesforthelazyones._
    import AggregateRootDroneInternalMessages._
    val n = 500
    "aggregate roots exists before querying them" when {
      s"$n aggregate roots are created" should {
        s"deliver $n aggregate roots with version 1" in { fixture ⇒
          val FixtureParam(testId, views, hive, streams) = fixture
          val statusProbe = TestProbe()
          Source(streams.eventStream).collect { case e: SystemEvent ⇒ e }.connect(Sink(DelegatingSubscriber[SystemEvent](statusProbe.ref))).run()
          val start = Deadline.now
          val probe = TestProbe()
          within(10 seconds) {
            Source((1 to n).toSeq.map(id ⇒ CreateUser(CommandHeader(), s"$id", 0L, "hans", "meier"))).connect(Sink(streams.commandBroker.newSubscriber)).run()
            statusProbe.receiveN(2 * n, 15 seconds)
            (1 to n).foreach(id ⇒ probe.send(views, AggregateRootViewMessages.GetAggregateRootProjectionFor(s"$id")))
            val users = probe.receiveN(n, 15 seconds).collect { case UserState(VivusRef(id, AggregateRootVersion(1))) ⇒ id }.toSet
            users should have size (n)
            users.toSet should equal((1 to n).map(id ⇒ AggregateRootId(s"$id")).toSet)
          }
          val time = start.lap
          info(s"Dispatched ${n} in ${start.lap.defaultUnitString}((${(n * 1000).toDouble / time.toMillis}/s)).")
        }
      }
      s"$n aggregate roots are created, updated and deleted" should {
        s"signal each aggregate root as dead" in { fixture ⇒
          val FixtureParam(testId, views, hive, streams) = fixture
          val statusProbe = TestProbe()
          Source(streams.eventStream).collect { case e: SystemEvent ⇒ e }.connect(Sink(DelegatingSubscriber[SystemEvent](statusProbe.ref))).run()
          val start = Deadline.now
          val probe = TestProbe()
          val theFlow = Source[Command]((1 to n).toSeq.map(id ⇒ CreateUser(CommandHeader(), s"$id", 0L, "hans", "meier"): AggregateRootCommand) ++
            (1 to n).toSeq.map(id ⇒ ChangeUserLastname(CommandHeader(), s"$id", 1L, "müller"): AggregateRootCommand) ++
            (1 to n).toSeq.map(id ⇒ ConfirmUserDeath(CommandHeader(), s"$id", 2L): AggregateRootCommand))

          within(15 seconds) {
            theFlow.connect(Sink(streams.commandBroker.newSubscriber)).run()
            statusProbe.receiveN(6 * n, 15 seconds)
            (1 to n).foreach(id ⇒ probe.send(views, AggregateRootViewMessages.GetAggregateRootProjectionFor(s"$id")))
            val users = probe.receiveN(n, 15 seconds).collect { case UserState(Mortuus(id, AggregateRootVersion(3L))) ⇒ id }.toSet
            users should have size (n)
            users.toSet should equal((1 to n).map(id ⇒ AggregateRootId(s"$id")).toSet)
          }
          val time = start.lap
          info(s"Dispatched ${n} in ${start.lap.defaultUnitString}((${(n * 1000).toDouble / time.toMillis}/s)).")
        }
      }
    }
    "aggregate roots are queried before they exist" when {
      s"$n aggregate roots are queried" should {
        s"signal $n aggregate roots to be non existent" in { fixture ⇒
          val FixtureParam(testId, views, hive, streams) = fixture
          val statusProbe = TestProbe()
          Source(streams.eventStream).collect { case e: SystemEvent ⇒ e }.connect(Sink(DelegatingSubscriber[SystemEvent](statusProbe.ref))).run()
          val start = Deadline.now
          val probe = TestProbe()
          within(15 seconds) {
            (1 to n).foreach(id ⇒ probe.send(views, AggregateRootViewMessages.GetAggregateRootProjectionFor(s"$id")))
            val res = probe.receiveN(n, 15 seconds).collect { case UserState(Vacat) ⇒ 1 }.sum
            res should equal(n)
          }
          val time = start.lap
          info(s"Dispatched ${n} in ${start.lap.defaultUnitString}((${(n * 1000).toDouble / time.toMillis}/s)).")
        }
      }
      s"$n aggregate roots are created" should {
        s"deliver $n aggregate roots with version 1" in { fixture ⇒
          val FixtureParam(testId, views, hive, streams) = fixture
          val statusProbe = TestProbe()
          Source(streams.eventStream).collect { case e: SystemEvent ⇒ e }.connect(Sink(DelegatingSubscriber[SystemEvent](statusProbe.ref))).run()
          val start = Deadline.now
          val probe = TestProbe()
          within(15 seconds) {
            (1 to n).foreach(id ⇒ probe.send(views, AggregateRootViewMessages.GetAggregateRootProjectionFor(s"$id")))
            probe.receiveN(n, 15 seconds)
            Source[Command]((1 to n).toSeq.map(id ⇒ CreateUser(CommandHeader(), s"$id", 0L, "hans", "meier"))).connect(Sink(streams.commandBroker.newSubscriber)).run()
            statusProbe.receiveN(2 * n, 15 seconds)
            (1 to n).foreach(id ⇒ probe.send(views, AggregateRootViewMessages.GetAggregateRootProjectionFor(s"$id")))
            val users = probe.receiveN(n, 15 seconds).collect { case UserState(VivusRef(id, AggregateRootVersion(1))) ⇒ id }.toSet
            users should have size (n)
            users.toSet should equal((1 to n).map(id ⇒ AggregateRootId(s"$id")).toSet)
          }
          val time = start.lap
          info(s"Dispatched ${n} in ${start.lap.defaultUnitString}((${(n * 1000).toDouble / time.toMillis}/s)).")
        }
      }
      s"$n aggregate roots are created, updated and deleted" should {
        s"signal each aggregate root as dead" in { fixture ⇒
          val FixtureParam(testId, views, hive, streams) = fixture
          val statusProbe = TestProbe()
          Source(streams.eventStream).collect { case e: SystemEvent ⇒ e }.connect(Sink(DelegatingSubscriber[SystemEvent](statusProbe.ref))).run()
          val start = Deadline.now
          val probe = TestProbe()
          val theFlow = Source[Command]((1 to n).toSeq.map(id ⇒ CreateUser(CommandHeader(), s"$id", 0L, "hans", "meier"): AggregateRootCommand) ++
            (1 to n).toSeq.map(id ⇒ ChangeUserLastname(CommandHeader(), s"$id", 1L, "müller"): AggregateRootCommand) ++
            (1 to n).toSeq.map(id ⇒ ConfirmUserDeath(CommandHeader(), s"$id", 2L): AggregateRootCommand))

          within(15 seconds) {
            (1 to n).foreach(id ⇒ probe.send(views, AggregateRootViewMessages.GetAggregateRootProjectionFor(s"$id")))
            probe.receiveN(n, 15 seconds)
            theFlow.connect(Sink(streams.commandBroker.newSubscriber)).run()
            statusProbe.receiveN(6 * n, 15 seconds)
            (1 to n).foreach(id ⇒ probe.send(views, AggregateRootViewMessages.GetAggregateRootProjectionFor(s"$id")))
            val users = probe.receiveN(n, 15 seconds).collect { case UserState(Mortuus(id, AggregateRootVersion(3L))) ⇒ id }.toSet
            users should have size (n)
            users.toSet should equal((1 to n).map(id ⇒ AggregateRootId(s"$id")).toSet)
          }
          val time = start.lap
          info(s"Dispatched ${n} in ${start.lap.defaultUnitString}((${(n * 1000).toDouble / time.toMillis}/s)).")
        }
      }
    }
    "aggregate roots are queried between events" when {
      s"$n aggregate roots are created and updated" should {
        s"signal each aggregate root as dead" in { fixture ⇒
          val FixtureParam(testId, views, hive, streams) = fixture
          val statusProbe = TestProbe()
          Source(streams.eventStream).collect { case e: SystemEvent ⇒ e }.connect(Sink(DelegatingSubscriber[SystemEvent](statusProbe.ref))).run()
          val start = Deadline.now
          val probe = TestProbe()
          val flow1 = Source[Command]((1 to n).toSeq.map(id ⇒ CreateUser(CommandHeader(), s"$id", 0L, "hans", "meier"): AggregateRootCommand))
          val flow2 = Source[Command]((1 to n).toSeq.map(id ⇒ ChangeUserLastname(CommandHeader(), s"$id", 1L, "müller"): AggregateRootCommand))

          within(15 seconds) {
            flow1.connect(Sink(streams.commandBroker.newSubscriber)).run()
            statusProbe.receiveN(2 * n, 15 seconds)
            (1 to n).foreach(id ⇒ probe.send(views, AggregateRootViewMessages.GetAggregateRootProjectionFor(s"$id")))
            probe.receiveN(n, 10 seconds)
            flow2.connect(Sink(streams.commandBroker.newSubscriber)).run()
            statusProbe.receiveN(2 * n, 15 seconds)
            (1 to n).foreach(id ⇒ probe.send(views, AggregateRootViewMessages.GetAggregateRootProjectionFor(s"$id")))
            val users = probe.receiveN(n, 15 seconds).collect { case UserState(VivusRef(id, AggregateRootVersion(2))) ⇒ id }.toSet
            users should have size (n)
            users.toSet should equal((1 to n).map(id ⇒ AggregateRootId(s"$id")).toSet)
          }
          val time = start.lap
          info(s"Dispatched ${n} in ${start.lap.defaultUnitString}((${(n * 1000).toDouble / time.toMillis}/s)).")
        }
      }
    }
  }

  private val currentTestId = new java.util.concurrent.atomic.AtomicInteger(1)
  def nextTestId = currentTestId.getAndIncrement()

  case class FixtureParam(
    testId: Int,
    views: ActorRef,
    hive: ActorRef,
    streams: AlmhirtStreams)

  def withFixture(test: OneArgTest) = {
    import almhirt.streaming._
    import almhirt.tracking.CommandStatusChanged
    import almhirt.aggregates._
    import almhirt.akkax._

    val testId = nextTestId
    info(s"Test $testId")
    val eventlogProps: Props = almhirt.eventlog.InMemoryAggregateRootEventLog.props()
    val eventlogActor: ActorRef = system.actorOf(eventlogProps, s"eventlog-$testId")

    implicit val almhirtContext = AlmhirtContext.TestContext.noComponentsDefaultGlobalDispatcher(s"almhirt-context-$testId", AggregateRootViewsTests.this.ccuad, 5.seconds.dilated).awaitResultOrEscalate(5.seconds.dilated)

    def droneProps(ars: ActorRef, ss: Option[ActorRef]): Props = Props(
      new AggregateRootDrone[User, UserEvent] with ActorLogging with UserEventHandler with UserCommandHandler with UserUpdater with AggregateRootDroneCommandHandlerAdaptor[User, UserCommand, UserEvent] {
        def ccuad = AggregateRootViewsTests.this.ccuad
        def futuresContext: ExecutionContext = executionContext
        def aggregateEventLog: ActorRef = ars
        def snapshotStorage: Option[ActorRef] = ss
        val eventsBroker: StreamBroker[Event] = almhirtContext.eventBroker
        val returnToUnitializedAfter = None

        override val aggregateCommandValidator = AggregateRootCommandValidator.Validated
        override val tag = scala.reflect.ClassTag[UserCommand](classOf[UserCommand])

      })

    val droneFactory = new AggregateRootDroneFactory {
      import scalaz._, Scalaz._
      def propsForCommand(command: AggregateRootCommand, ars: ActorRef, ss: Option[ActorRef]): AlmValidation[Props] = {
        command match {
          case c: UserCommand ⇒ droneProps(ars, ss).success
          case x ⇒ NoSuchElementProblem(s"I don't have props for command $x").failure
        }
      }
    }

    val hiveProps = Props(
      new AggregateRootHive(
        HiveDescriptor(s"hive-$testId"),
        NoResolvingRequired(eventlogActor),
        None,
        ResolveSettings.default,
        commandBuffersize = 10,
        droneFactory = droneFactory,
        almhirtContext.eventBroker))

    val hiveActor = system.actorOf(hiveProps, s"hive-$testId-test")
    val hiveSubscriber = akka.stream.actor.ActorSubscriber[AggregateRootCommand](hiveActor)
    Source(almhirtContext.commandStream).collect { case c: AggregateRootCommand ⇒ c }.connect(Sink(hiveSubscriber)).run()

    def getViewProps(id: AggregateRootId, el: ActorRef, ss: Option[ActorRef]) = Props(new AggregateRootUnprojectedView[User, UserEvent](
      id, el, ss,
      (lc, receiver) ⇒ receiver ! UserState(lc),
      (prob, receiver) ⇒ receiver ! ViewFailure(prob),
      None) with UserEventHandler {
    })

    import almhirt.akkax._
    val viewsActor = AggregateRootViews.connectedActor[UserEvent](almhirtContext.eventStream)(
      getViewProps,
      NoResolvingRequired(eventlogActor),
      None,
      ResolveSettings.default,
      50,
      s"views-$testId")

    try {
      withFixture(test.toNoArgTest(FixtureParam(testId, viewsActor, hiveActor, almhirtContext)))
    } finally {
      almhirtContext.stop()
      system.stop(hiveActor)
      system.stop(viewsActor)
      system.stop(eventlogActor)
    }
  }

  override def beforeAll() {

  }

  override def afterAll() {
    TestKit.shutdownActorSystem(system)
  }

}