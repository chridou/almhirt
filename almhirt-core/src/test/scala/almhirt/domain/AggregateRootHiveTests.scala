package almhirt.domain

import scala.language.postfixOps
import org.joda.time.{ DateTime, LocalDateTime, DateTimeZone }
import scala.concurrent._
import scala.concurrent.duration._
import akka.actor._
import almhirt.common._
import org.reactivestreams.Subscriber
import almhirt.streaming._
import akka.stream.scaladsl2._
import akka.testkit._
import org.scalatest._
import almhirt.context.AlmhirtContext

class AggregateRootHiveTests(_system: ActorSystem)
  extends TestKit(_system) with fixture.WordSpecLike with Matchers with BeforeAndAfterAll {
  def this() = this(ActorSystem("AggregateRootHiveTests", almhirt.TestConfigs.logWarningConfig))

  implicit val mat = FlowMaterializer()

  implicit val executionContext = system.dispatchers.defaultGlobalDispatcher
  implicit val ccuad = {
    val currentId = new java.util.concurrent.atomic.AtomicInteger(1)
    val dt = new LocalDateTime(0L)
    new CanCreateUuidsAndDateTimes {
      override def getUuid(): java.util.UUID = ???
      override def getUniqueString(): String = s"id-${currentId.getAndIncrement()}"
      override def getDateTime(): DateTime = ???
      override def getUtcTimestamp(): LocalDateTime = dt
    }
  }

  private case class StatusEventResults(initiated: Int, executed: Int, failed: Int) {
    override def toString(): String =
      s"(initiated: $initiated, executed: $executed, failed: $failed)"
  }

  info("These tests have timing issues since one can not really tell, whether commands overlap unless effort is taken to prevent this.")

  "The AggregateRootHive" when {
    import almhirt.eventlog.AggregateRootEventLog._
    import almhirt.aggregates._
    import aggregatesforthelazyones._
    import almhirt.tracking._

    def splitStatusEvents(events: Seq[Any]): (Seq[CommandStatusChanged], Seq[CommandStatusChanged], Seq[CommandStatusChanged]) =
      events.collect { case x: CommandStatusChanged ⇒ x }.foldLeft((Seq[CommandStatusChanged](), Seq[CommandStatusChanged](), Seq[CommandStatusChanged]())) {
        case ((a, b, c), cur) ⇒
          cur match {
            case x @ CommandStatusChanged(_, _, CommandStatus.Initiated) ⇒ (a :+ x, b, c)
            case x @ CommandStatusChanged(_, _, CommandStatus.Executed) ⇒ (a, b :+ x, c)
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
          Source(streams.eventStream).collect { case e: SystemEvent ⇒ e }.connect(Sink(DelegatingSubscriber[SystemEvent](statusProbe.ref))).run()
          within(1 second) {
            Source(CreateUser(CommandHeader(), "a", 0L, "hans", "meier") :: Nil).connect(Sink(commandSubscriber)).run()
            statusProbe.expectMsgType[CommandStatusChanged].status should equal(CommandStatus.Initiated)
            statusProbe.expectMsgType[CommandStatusChanged].status should equal(CommandStatus.Executed)
          }
        }
      }
      "2 aggregate roots are created" should {
        "emit the status events [Initiated(a), Executed(a)] and [Initiated(b), Executed(b)]" in { fixture ⇒
          val FixtureParam(testId, commandSubscriber, eventlog, streams) = fixture
          val statusProbe = TestProbe()
          Source(streams.eventStream).collect { case e: SystemEvent ⇒ e }.connect(Sink(DelegatingSubscriber[SystemEvent](statusProbe.ref))).run()
          within(1 second) {
            Source(List(
              CreateUser(CommandHeader(), "a", 0L, "hans", "meier"),
              CreateUser(CommandHeader(), "b", 0L, "hans", "meier"))).connect(Sink(commandSubscriber)).run()
            assertStatusEvents(initiated = 2, ok = 2, failed = 0, statusProbe.receiveN(2 * 2, 1 second))
          }
        }
      }
      val n = 500
      s"$n aggregate roots are created" should {
        s"emit the status events [Initiated, Executed] $n times" in { fixture ⇒
          val FixtureParam(testId, commandSubscriber, eventlog, streams) = fixture
          val statusProbe = TestProbe()
          Source(streams.eventStream).collect { case e: SystemEvent ⇒ e }.connect(Sink(DelegatingSubscriber[SystemEvent](statusProbe.ref))).run()
          val start = Deadline.now
          within(15 seconds) {
            Source((1 to n).toSeq.map(id ⇒ CreateUser(CommandHeader(), s"$id", 0L, "hans", "meier"))).connect(Sink(commandSubscriber)).run()
            assertStatusEvents(initiated = n, ok = n, failed = 0, statusProbe.receiveN(n * 2, 15 seconds))
          }
          val time = start.lap
          info(s"Dispatched ${n} in ${start.lap.defaultUnitString}((${(n * 1000).toDouble / time.toMillis}/s)).")
        }
      }
      s"$n aggregate roots are created and then updated" should {
        s"emit the status events ([Initiated, Executed]x2) $n times" in { fixture ⇒
          val FixtureParam(testId, commandSubscriber, eventlog, streams) = fixture
          val statusProbe = TestProbe()
          Source(streams.eventStream).collect { case e: SystemEvent ⇒ e }.connect(Sink(DelegatingSubscriber[SystemEvent](statusProbe.ref))).run()
          val flow = Source(
            (1 to n).toSeq.map(id ⇒ CreateUser(CommandHeader(), s"$id", 0L, "hans", "meier"): AggregateRootCommand) ++
              (1 to n).toSeq.map(id ⇒ ChangeUserLastname(CommandHeader(), s"$id", 1L, "müller"): AggregateRootCommand))
          val start = Deadline.now
          within(15 seconds) {
            flow.connect(Sink(commandSubscriber)).run()
            assertStatusEvents(initiated = 2 * n, ok = 2 * n, failed = 0, statusProbe.receiveN(2 * n * 2, 15 seconds))
          }
          val time = start.lap
          info(s"Dispatched ${n} commands in ${start.lap.defaultUnitString}((${(n * 1000).toDouble / time.toMillis}/s)).")
        }
      }
      s"$n aggregate roots are created, updated and then deleted" should {
        s"emit the status events ([Initiated, Executed]x3) $n times" in { fixture ⇒
          val FixtureParam(testId, commandSubscriber, eventlog, streams) = fixture
          val statusProbe = TestProbe()
          Source(streams.eventStream).collect { case e: SystemEvent ⇒ e }.connect(Sink(DelegatingSubscriber[SystemEvent](statusProbe.ref))).run()
          val flow = Source((1 to n).toSeq.map(id ⇒ CreateUser(CommandHeader(), s"$id", 0L, "hans", "meier"): AggregateRootCommand) ++
            (1 to n).toSeq.map(id ⇒ ChangeUserLastname(CommandHeader(), s"$id", 1L, "müller"): AggregateRootCommand) ++
            (1 to n).toSeq.map(id ⇒ ConfirmUserDeath(CommandHeader(), s"$id", 2L): AggregateRootCommand))
          val start = Deadline.now
          within(15 seconds) {
            flow.connect(Sink(commandSubscriber)).run()
            assertStatusEvents(initiated = 3 * n, ok = 3 * n, failed = 0, statusProbe.receiveN(3 * n * 2, 15 seconds))
          }
          val time = start.lap
          info(s"Dispatched ${n} commands in ${start.lap.defaultUnitString}((${(n * 1000).toDouble / time.toMillis}/s)).")
        }
      }
      val bigN = 2500
      s"$bigN aggregate roots are created, updated and then deleted" should {
        s"emit the status events ([Initiated, Executed]x3) $bigN times" in { fixture ⇒
          val FixtureParam(testId, commandSubscriber, eventlog, streams) = fixture
          val statusProbe = TestProbe()
          Source(streams.eventStream).collect { case e: SystemEvent ⇒ e }.connect(Sink(DelegatingSubscriber[SystemEvent](statusProbe.ref))).run()
          val flow = Source((1 to bigN).toSeq.map(id ⇒ CreateUser(CommandHeader(), s"$id", 0L, "hans", "meier"): AggregateRootCommand) ++
          (1 to bigN).toSeq.map(id ⇒ ChangeUserLastname(CommandHeader(), s"$id", 1L, "müller"): AggregateRootCommand) ++
          (1 to bigN).toSeq.map(id ⇒ ConfirmUserDeath(CommandHeader(), s"$id", 2L): AggregateRootCommand))
          val start = Deadline.now
          within(15 seconds) {
            flow.connect(Sink(commandSubscriber)).run()
            assertStatusEvents(initiated = 3 * bigN, ok = 3 * bigN, failed = 0, statusProbe.receiveN(3 * bigN * 2, 15 seconds))
          }
          val time = start.lap
          info(s"Dispatched ${bigN} commands in ${start.lap.defaultUnitString}((${(bigN * 1000).toDouble / time.toMillis}/s)).")
        }
      }
    }
  }

  private val currentTestId = new java.util.concurrent.atomic.AtomicInteger(1)
  def nextTestId = currentTestId.getAndIncrement()

  case class FixtureParam(
    testId: Int,
    hiveSubscriber: Subscriber[AggregateRootCommand],
    eventlog: ActorRef,
    streams: AlmhirtStreams)

  def withFixture(test: OneArgTest) = {
    import almhirt.aggregates._
    import almhirt.tracking.CommandStatusChanged
    import almhirt.akkax._
    val testId = nextTestId
    //info(s"Test $testId")
    val eventlogProps: Props = almhirt.eventlog.InMemoryAggregateRootEventLog.props()
    val eventlogActor: ActorRef = system.actorOf(eventlogProps, s"eventlog-$testId")

    implicit val almhirtContext = AlmhirtContext.TestContext.noComponentsDefaultGlobalDispatcher(s"almhirt-context-$testId", AggregateRootHiveTests.this.ccuad, 5.seconds.dilated).awaitResultOrEscalate(5.seconds.dilated)
 
    def droneProps(ars: ActorRef, ss: Option[ActorRef]): Props = Props(
      new AggregateRootDrone[User, UserEvent] with ActorLogging with UserEventHandler with UserCommandHandler with UserUpdater with AggregateRootDroneCommandHandlerAdaptor[User, UserCommand, UserEvent] {
         def ccuad = AggregateRootHiveTests.this.ccuad
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

    try {
      withFixture(test.toNoArgTest(FixtureParam(testId, hiveSubscriber, eventlogActor, almhirtContext)))
    } finally {
      system.stop(hiveActor)
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