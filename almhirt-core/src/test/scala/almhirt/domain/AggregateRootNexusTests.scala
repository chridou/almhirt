package almhirt.domain

import scala.language.postfixOps
import java.time.{ ZonedDateTime, LocalDateTime }
import scala.concurrent._
import scala.concurrent.duration._
import org.reactivestreams.Subscriber
import akka.actor._
import almhirt.common._
import almhirt.streaming._
import akka.stream.scaladsl._
import akka.testkit._
import org.scalatest._
import almhirt.context.AlmhirtContext

class AggregateRootNexusTests(_system: ActorSystem)
    extends TestKit(_system) with fixture.WordSpecLike with Matchers with BeforeAndAfterAll {
  def this() = this(ActorSystem("AggregateRootNexusTests", almhirt.TestConfigs.logErrorConfig))

  implicit val executionContext = system.dispatchers.defaultGlobalDispatcher
  implicit val ccuad = CanCreateUuidsAndDateTimes()

  implicit def implicitFlowMaterializer = akka.stream.ActorMaterializer()(_system)

  val counter = new java.util.concurrent.atomic.AtomicInteger(1)
  def nextCounter = counter.getAndIncrement()

  val rnd = new scala.util.Random()
  def createId(pre: Int): String = {
    s"${pre}-${rnd.nextInt(1000)}"
  }

  private case class StatusEventResults(initiated: Int, executed: Int, failed: Int) {
    override def toString(): String =
      s"(initiated: $initiated, executed: $executed, failed: $failed)"
  }

  "The AggregateRootNexus" when {
    import almhirt.eventlog.AggregateRootEventLog._
    import almhirt.aggregates._
    import aggregatesforthelazyones._
    import almhirt.tracking._

    def splitStatusEvents(events: Seq[Any]): (Seq[CommandStatusChanged], Seq[CommandStatusChanged], Seq[CommandStatusChanged]) =
      events.collect { case x: CommandStatusChanged ⇒ x }.foldLeft((Seq[CommandStatusChanged](), Seq[CommandStatusChanged](), Seq[CommandStatusChanged]())) {
        case ((a, b, c), cur) ⇒
          cur match {
            case x @ CommandStatusChanged(_, _, CommandStatus.Initiated)      ⇒ (a :+ x, b, c)
            case x @ CommandStatusChanged(_, _, CommandStatus.Executed)       ⇒ (a, b :+ x, c)
            case x @ CommandStatusChanged(_, _, CommandStatus.NotExecuted(_)) ⇒ (a, b, c :+ x)
          }
      }

    def assertStatusEvents(initiated: Int, ok: Int, failed: Int, events: Seq[Any]) {
      val (i, o, f) = splitStatusEvents(events)
      StatusEventResults(i.size, o.size, f.size) should equal(StatusEventResults(initiated, ok, failed))
    }

    "receiving valid commands" when {
      "an aggregate root is created" should {
        "should emit the status events [Initiated, Executed]" in { fixture ⇒
          val FixtureParam(testId, commandSubscriber, eventlog, streams) = fixture
          val statusProbe = TestProbe()
          Source(streams.eventStream).collect { case e: SystemEvent ⇒ e }.to(Sink(DelegatingSubscriber[SystemEvent](statusProbe.ref))).run()
          within(15 seconds) {
            Source[Command](CreateUser(CommandHeader(), createId(1), 0L, "hans", "meier") :: Nil).to(Sink(commandSubscriber)).run()
            statusProbe.expectMsgType[CommandStatusChanged].status should equal(CommandStatus.Initiated)
            statusProbe.expectMsgType[CommandStatusChanged].status should equal(CommandStatus.Executed)
          }
        }
      }
      "2 aggregate roots are created" should {
        "emit the status events [Initiated(a), Executed(a)] and [Initiated(b), Executed(b)]" in { fixture ⇒
          val FixtureParam(testId, commandSubscriber, eventlog, streams) = fixture
          val statusProbe = TestProbe()
          Source(streams.eventStream).collect { case e: SystemEvent ⇒ e }.to(Sink(DelegatingSubscriber[SystemEvent](statusProbe.ref))).run()
          within(15 seconds) {
            Source[Command](List(
              CreateUser(CommandHeader(), createId(1), 0L, "hans", "meier"),
              CreateUser(CommandHeader(), createId(2), 0L, "hans", "meier"))).to(Sink(commandSubscriber)).run()
            assertStatusEvents(initiated = 2, ok = 2, failed = 0, statusProbe.receiveN(2 * 2, 30 seconds))
          }
        }
      }
      val nn = 100
      val ids = (1 to nn).map(createId).toVector
      val n = ids.size
      s"$n aggregate roots are created" should {
        s"emit the status events [Initiated, Executed] $n times" in { fixture ⇒
          val FixtureParam(testId, commandSubscriber, eventlog, streams) = fixture
          val statusProbe = TestProbe()
          Source(streams.eventStream).collect { case e: SystemEvent ⇒ e }.to(Sink(DelegatingSubscriber[SystemEvent](statusProbe.ref))).run()
          val start = Deadline.now
          within(15 seconds) {
            Source[Command](ids.toStream.map(id ⇒ CreateUser(CommandHeader(), id, 0L, "hans", "meier"))).to(Sink(commandSubscriber)).run()
            assertStatusEvents(initiated = n, ok = n, failed = 0, statusProbe.receiveN(n * 2, 30 seconds))
            val time = start.lap
            info(s"Dispatched ${n} in ${start.lap.defaultUnitString}((${(n * 1000).toDouble / time.toMillis}/s)).")
          }
        }
      }
      s"$n aggregate roots are created and then updated" should {
        s"emit the status events ([Initiated(a), Executed]x2) $n times" in { fixture ⇒
          val FixtureParam(testId, commandSubscriber, eventlog, streams) = fixture
          val statusProbe = TestProbe()
          Source(streams.eventStream).collect { case e: SystemEvent ⇒ e }.to(Sink(DelegatingSubscriber[SystemEvent](statusProbe.ref))).run()
          val flow = Source[Command](ids.toStream.map(id ⇒ CreateUser(CommandHeader(), id, 0L, "hans", "meier"): AggregateRootCommand) ++
            ids.toStream.map(id ⇒ ChangeUserLastname(CommandHeader(), id, 1L, "müller"): AggregateRootCommand))
          val start = Deadline.now
          within(15 seconds) {
            flow.to(Sink(commandSubscriber)).run()
            assertStatusEvents(initiated = 2 * n, ok = 2 * n, failed = 0, statusProbe.receiveN(2 * n * 2, 30 seconds))
            val time = start.lap
            info(s"Dispatched ${n} commands in ${start.lap.defaultUnitString}((${(n * 1000).toDouble / time.toMillis}/s)).")
          }
        }
      }
      s"$n aggregate roots are created, updated and then deleted" should {
        s"emit the status events ([Initiated, Executed]x3) $n times" in { fixture ⇒
          val FixtureParam(testId, commandSubscriber, eventlog, streams) = fixture
          val statusProbe = TestProbe()
          Source(streams.eventStream).collect { case e: SystemEvent ⇒ e }.to(Sink(DelegatingSubscriber[SystemEvent](statusProbe.ref))).run()
          val flow = Source[Command](ids.toStream.map(id ⇒ CreateUser(CommandHeader(), id, 0L, "hans", "meier"): AggregateRootCommand) ++
            ids.toStream.map(id ⇒ ChangeUserLastname(CommandHeader(), id, 1L, "müller"): AggregateRootCommand) ++
            ids.toStream.map(id ⇒ ConfirmUserDeath(CommandHeader(), id, 2L): AggregateRootCommand))
          val start = Deadline.now
          within(30 seconds) {
            flow.to(Sink(commandSubscriber)).run()
            assertStatusEvents(initiated = 3 * n, ok = 3 * n, failed = 0, statusProbe.receiveN(3 * n * 2, 30 seconds))
            val time = start.lap
            info(s"Dispatched ${n} commands in ${start.lap.defaultUnitString}((${(n * 1000).toDouble / time.toMillis}/s)).")
          }
        }
        val bigN = 500
        s"$bigN aggregate roots are created, updated and then deleted" should {
          s"emit the status events ([Initiated, Executed]x3) $bigN times" in { fixture ⇒
            val FixtureParam(testId, commandSubscriber, eventlog, streams) = fixture
            val statusProbe = TestProbe()
            Source(streams.eventStream).collect { case e: SystemEvent ⇒ e }.to(Sink(DelegatingSubscriber[SystemEvent](statusProbe.ref))).run()
            val flow = Source[Command]((1 to bigN).toSeq.map(id ⇒ CreateUser(CommandHeader(), s"$id", 0L, s"hans-$id", s"meier-$id"): AggregateRootCommand) ++
              (1 to bigN).toSeq.map(id ⇒ ChangeUserLastname(CommandHeader(), s"$id", 1L, s"müller-$id"): AggregateRootCommand) ++
              (1 to bigN).toSeq.map(id ⇒ ConfirmUserDeath(CommandHeader(), s"$id", 2L): AggregateRootCommand))
            val start = Deadline.now
            within(30 seconds) {
              flow.to(Sink(commandSubscriber)).run()
              assertStatusEvents(initiated = 3 * bigN, ok = 3 * bigN, failed = 0, statusProbe.receiveN(3 * bigN * 2, 30 seconds))
            }
            val time = start.lap
            info(s"Dispatched ${bigN} commands in ${start.lap.defaultUnitString}((${(bigN * 1000).toDouble / time.toMillis}/s)).")
          }
        }
      }
    }
  }

  private val currentTestId = new java.util.concurrent.atomic.AtomicInteger(1)
  def nextTestId = currentTestId.getAndIncrement()

  case class FixtureParam(
    testId: Int,
    commandSubscriber: Subscriber[Command],
    eventlog: ActorRef,
    streams: AlmhirtStreams)

  def withFixture(test: OneArgTest) = {
    import scalaz.syntax.validation._
    import almhirt.aggregates._
    import almhirt.akkax._

    val testId = nextTestId
    //info(s"Test $testId")
    val eventlogProps: Props = almhirt.eventlog.InMemoryAggregateRootEventLog.props()
    val eventlogActor: ActorRef = system.actorOf(eventlogProps, s"eventlog-$testId")

    implicit val almhirtContext = AlmhirtContext.TestContext.noComponentsDefaultGlobalDispatcher(s"almhirt-context-$testId", AggregateRootNexusTests.this.ccuad, 5.seconds.dilated).awaitResultOrEscalate(5.seconds.dilated)
    def droneProps(ars: ActorRef, ss: Option[SnapshottingForDrone]): Props = Props(
      new AggregateRootDrone[User, UserEvent] with ActorLogging with UserEventHandler with UserCommandHandler with UserUpdater with AggregateRootDroneCommandHandlerAdaptor[User, UserCommand, UserEvent] {
        def ccuad = AggregateRootNexusTests.this.ccuad
        val arTag = scala.reflect.ClassTag[User](classOf[User])
        val snapshotting = ss
        def futuresContext: ExecutionContext = executionContext
        def aggregateEventLog: ActorRef = ars
        val eventsBroker: StreamBroker[Event] = almhirtContext.eventBroker
        val notifyHiveAboutUndispatchedEventsAfter: Option[FiniteDuration] = None
        val notifyHiveAboutUnstoredEventsAfterPerEvent: Option[FiniteDuration] = None
        def retryEventLogActionDelay: Option[FiniteDuration] = None
        val preStoreActionFor = (e: UserEvent) ⇒ PreStoreEventAction.NoAction
        val returnToUnitializedAfter = None

        override val aggregateCommandValidator = AggregateRootCommandValidator.Validated
        override val tag = scala.reflect.ClassTag[UserCommand](classOf[UserCommand])

      })

    val droneFactory = new AggregateRootDroneFactory {
      import scalaz._, Scalaz._
      def propsForCommand(command: AggregateRootCommand, ars: ActorRef, snapshotting: Option[(ActorRef, almhirt.snapshots.SnapshottingPolicyProvider)]): AlmValidation[Props] = {
        command match {
          case c: UserCommand ⇒
            snapshotting match {
              case None                   ⇒ droneProps(ars, None).success
              case Some((repo, provider)) ⇒ provider.apply("user").map(policy ⇒ droneProps(ars, Some(SnapshottingForDrone(repo, policy))))
            }

          case x ⇒
            NoSuchElementProblem(s"I don't have props for command $x").failure
        }
      }
    }

    def hiveProps(descriptor: HiveDescriptor) = Props(
      new AggregateRootHive(
        descriptor,
        NoResolvingRequired(eventlogActor),
        None,
        ResolveSettings.default,
        commandBuffersize = 16,
        droneFactory = droneFactory,
        almhirtContext.eventBroker,
        enqueuedEventsThrottlingThreshold = 8))

    val hiveFactory = new AggregateRootHiveFactory {
      def props(descriptor: HiveDescriptor): AlmValidation[Props] = hiveProps(descriptor).success
    }

    val hiveSelector: Seq[(HiveDescriptor, AggregateRootCommand ⇒ Boolean)] =
      Seq(
        (HiveDescriptor("0"), cmd ⇒ Math.abs(cmd.aggId.hashCode % 5) == 0),
        (HiveDescriptor("1"), cmd ⇒ Math.abs(cmd.aggId.hashCode % 5) == 1),
        (HiveDescriptor("2"), cmd ⇒ Math.abs(cmd.aggId.hashCode % 5) == 2),
        (HiveDescriptor("3"), cmd ⇒ Math.abs(cmd.aggId.hashCode % 5) == 3),
        (HiveDescriptor("4"), cmd ⇒ Math.abs(cmd.aggId.hashCode % 5) == 4))

    val nexusProps = AggregateRootNexus.propsRaw(hiveSelector, hiveFactory, Some(almhirtContext.commandStream))
    val nexusActor = system.actorOf(nexusProps, s"nexus-$testId")
    try {
      withFixture(test.toNoArgTest(FixtureParam(testId, almhirtContext.commandBroker.newSubscriber, eventlogActor, almhirtContext)))
    } finally {
      system.stop(nexusActor)
      system.stop(eventlogActor)
      almhirtContext.stop()
    }
  }

  override def beforeAll() {

  }

  override def afterAll() {
    TestKit.shutdownActorSystem(system)
  }

}