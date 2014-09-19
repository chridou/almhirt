package almhirt.domain

import scala.language.postfixOps
import org.joda.time.{ DateTime, LocalDateTime, DateTimeZone }
import scala.concurrent._
import scala.concurrent.duration._
import org.reactivestreams.Subscriber
import akka.actor._
import almhirt.common._
import almhirt.streaming._
import akka.stream.scaladsl2._

import akka.testkit._
import org.scalatest._

class AggregateRootNexusTests(_system: ActorSystem)
  extends TestKit(_system) with fixture.WordSpecLike with Matchers with BeforeAndAfterAll {
  def this() = this(ActorSystem("AggregateRootNexusTests", almhirt.TestConfigs.logWarningConfig))

  implicit val executionContext = system.dispatchers.defaultGlobalDispatcher
  implicit val ccuad = CanCreateUuidsAndDateTimes()

  implicit val mat = FlowMaterializer()

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
          FlowFrom(streams.eventStream).collect { case e: SystemEvent => e }.publishTo(DelegatingSubscriber[SystemEvent](statusProbe.ref))
          within(2 seconds) {
            FlowFrom[Command](CreateUser(CommandHeader(), createId(1), 0L, "hans", "meier") :: Nil).publishTo(commandSubscriber)
            statusProbe.expectMsgType[CommandStatusChanged].status should equal(CommandStatus.Initiated)
            statusProbe.expectMsgType[CommandStatusChanged].status should equal(CommandStatus.Executed)
          }
        }
      }
      "2 aggregate roots are created" should {
        "emit the status events [Initiated(a), Executed(a)] and [Initiated(b), Executed(b)]" in { fixture ⇒
          val FixtureParam(testId, commandSubscriber, eventlog, streams) = fixture
          val statusProbe = TestProbe()
          FlowFrom(streams.eventStream).collect { case e: SystemEvent => e }.publishTo(DelegatingSubscriber[SystemEvent](statusProbe.ref))
          within(2 seconds) {
            FlowFrom[Command](List(
              CreateUser(CommandHeader(), createId(1), 0L, "hans", "meier"),
              CreateUser(CommandHeader(), createId(2), 0L, "hans", "meier"))).publishTo(commandSubscriber)
            assertStatusEvents(initiated = 2, ok = 2, failed = 0, statusProbe.receiveN(2 * 2, 3 seconds))
          }
        }
      }
      val nn = 1000
      val ids = (1 to nn).map(createId).toVector
      val n = ids.size
      s"$n aggregate roots are created" should {
        s"emit the status events [Initiated, Executed] $n times" in { fixture ⇒
          val FixtureParam(testId, commandSubscriber, eventlog, streams) = fixture
          val statusProbe = TestProbe()
          FlowFrom(streams.eventStream).collect { case e: SystemEvent => e }.publishTo(DelegatingSubscriber[SystemEvent](statusProbe.ref))
          val start = Deadline.now
          within(3 seconds) {
            FlowFrom[Command](ids.toStream.map(id ⇒ CreateUser(CommandHeader(), id, 0L, "hans", "meier"))).publishTo(commandSubscriber)
            assertStatusEvents(initiated = n, ok = n, failed = 0, statusProbe.receiveN(n * 2, 3 seconds))
            val time = start.lap
            info(s"Dispatched ${n} in ${start.lap.defaultUnitString}((${(n * 1000).toDouble / time.toMillis}/s)).")
          }
        }
      }
      s"$n aggregate roots are created and then updated" should {
        s"emit the status events ([Initiated(a), Executed]x2) $n times" in { fixture ⇒
          val FixtureParam(testId, commandSubscriber, eventlog, streams) = fixture
          val statusProbe = TestProbe()
          FlowFrom(streams.eventStream).collect { case e: SystemEvent => e }.publishTo(DelegatingSubscriber[SystemEvent](statusProbe.ref))
          val flow = FlowFrom[Command](ids.toStream.map(id ⇒ CreateUser(CommandHeader(), id, 0L, "hans", "meier"): AggregateRootCommand) ++
            ids.toStream.map(id ⇒ ChangeUserLastname(CommandHeader(), id, 1L, "müller"): AggregateRootCommand))
          val start = Deadline.now
          within(3 seconds) {
            flow.publishTo(commandSubscriber)
            assertStatusEvents(initiated = 2 * n, ok = 2 * n, failed = 0, statusProbe.receiveN(2 * n * 2, 3 seconds))
            val time = start.lap
            info(s"Dispatched ${n} commands in ${start.lap.defaultUnitString}((${(n * 1000).toDouble / time.toMillis}/s)).")
          }
        }
      }
      s"$n aggregate roots are created, updated and then deleted" should {
        s"emit the status events ([Initiated, Executed]x3) $n times" in { fixture ⇒
          val FixtureParam(testId, commandSubscriber, eventlog, streams) = fixture
          val statusProbe = TestProbe()
          FlowFrom(streams.eventStream).collect { case e: SystemEvent => e }.publishTo(DelegatingSubscriber[SystemEvent](statusProbe.ref))
          val flow = FlowFrom[Command](ids.toStream.map(id ⇒ CreateUser(CommandHeader(), id, 0L, "hans", "meier"): AggregateRootCommand) ++
            ids.toStream.map(id ⇒ ChangeUserLastname(CommandHeader(), id, 1L, "müller"): AggregateRootCommand) ++
            ids.toStream.map(id ⇒ ConfirmUserDeath(CommandHeader(), id, 2L): AggregateRootCommand))
          val start = Deadline.now
          within(3 seconds) {
            flow.publishTo(commandSubscriber)
            assertStatusEvents(initiated = 3 * n, ok = 3 * n, failed = 0, statusProbe.receiveN(3 * n * 2, 3 seconds))
            val time = start.lap
            info(s"Dispatched ${n} commands in ${start.lap.defaultUnitString}((${(n * 1000).toDouble / time.toMillis}/s)).")
          }
        }
        val bigN = 2500
        s"$bigN aggregate roots are created, updated and then deleted" should {
          s"emit the status events ([Initiated, Executed]x3) $bigN times" in { fixture ⇒
            val FixtureParam(testId, commandSubscriber, eventlog, streams) = fixture
            val statusProbe = TestProbe()
            FlowFrom(streams.eventStream).collect { case e: SystemEvent => e }.publishTo(DelegatingSubscriber[SystemEvent](statusProbe.ref))
            val flow = FlowFrom[Command]((1 to bigN).toSeq.map(id ⇒ CreateUser(CommandHeader(), s"$id", 0L, "hans", "meier"): AggregateRootCommand) ++
            		(1 to bigN).toSeq.map(id ⇒ ChangeUserLastname(CommandHeader(), s"$id", 1L, "müller"): AggregateRootCommand) ++
            		(1 to bigN).toSeq.map(id ⇒ ConfirmUserDeath(CommandHeader(), s"$id", 2L): AggregateRootCommand))
            val start = Deadline.now
            within(15 seconds) {
              flow.publishTo(commandSubscriber)
              assertStatusEvents(initiated = 3 * bigN, ok = 3 * bigN, failed = 0, statusProbe.receiveN(3 * bigN * 2, 15 seconds))
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
    import scalaz._, Scalaz._
    import akka.stream.scaladsl.Duct
    import almhirt.aggregates._

    val testId = nextTestId
    //info(s"Test $testId")
    val eventlogProps: Props = almhirt.eventlog.InMemoryAggregateRootEventLog.props()
    val eventlogActor: ActorRef = system.actorOf(eventlogProps, s"eventlog-$testId")

    val streams = AlmhirtStreams(s"almhirt-streams-$testId").awaitResultOrEscalate(1 second)
    val droneProps: Props = Props(
      new AggregateRootDrone[User, UserEvent] with ActorLogging with UserEventHandler with UserCommandHandler with UserUpdater with AggregateRootDroneCommandHandlerAdaptor[User, UserCommand, UserEvent] {
        def ccuad = AggregateRootNexusTests.this.ccuad
        def futuresContext: ExecutionContext = executionContext
        def aggregateEventLog: ActorRef = eventlogActor
        def snapshotStorage: Option[ActorRef] = None
        val eventsBroker: StreamBroker[Event] = streams.eventBroker

        override val aggregateCommandValidator = AggregateRootCommandValidator.Validated
        override val tag = scala.reflect.ClassTag[UserCommand](classOf[UserCommand])
      })

    val droneFactory = new AggregateRootDroneFactory {
      import scalaz._, Scalaz._
      def propsForCommand(command: AggregateRootCommand): AlmValidation[Props] = {
        command match {
          case c: UserCommand ⇒ droneProps.success
          case x ⇒ NoSuchElementProblem(s"I don't have props for command $x").failure
        }
      }
    }

    def hiveProps(desc: HiveDescriptor) = Props(
      new AggregateRootHive(
        desc,
        commandBuffersize = 10,
        droneFactory = droneFactory,
        streams.eventBroker)(AggregateRootNexusTests.this.ccuad, AggregateRootNexusTests.this.executionContext))

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

    //val (commandSubscriber, commandPublisher) = Duct[AggregateRootCommand].build()
    val f = FlowFrom[AggregateRootCommand].collect{ case e: AggregateRootCommand => e}

    val nexusProps = Props(new AggregateRootNexus(streams.aggregateCommandStream, hiveSelector, hiveFactory))
    val nexusActor = system.actorOf(nexusProps, s"nexus-$testId")
    try {
      withFixture(test.toNoArgTest(FixtureParam(testId, streams.commandBroker.newSubscriber, eventlogActor, streams)))
    } finally {
      system.stop(nexusActor)
      system.stop(eventlogActor)
      streams.stop()
    }
  }

  override def beforeAll() {

  }

  override def afterAll() {
    TestKit.shutdownActorSystem(system)
  }

}