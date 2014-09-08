package almhirt.domain

import scala.language.postfixOps

import org.joda.time.{ DateTime, LocalDateTime, DateTimeZone }
import scala.concurrent._
import scala.concurrent.duration._
import akka.actor._
import almhirt.common._

import org.reactivestreams.Subscriber
import almhirt.streaming._
import akka.stream.scaladsl.Flow
import akka.stream._
import akka.stream.{ FlowMaterializer, MaterializerSettings }

import akka.testkit._
import org.scalatest._

class AggregateRootHiveTests(_system: ActorSystem)
  extends TestKit(_system) with fixture.WordSpecLike with Matchers with BeforeAndAfterAll {
  def this() = this(ActorSystem("AggregateRootHiveTests", almhirt.TestConfigs.logWarningConfig))

  implicit val mat = FlowMaterializer(MaterializerSettings())

  implicit val executionContext = system.dispatchers.defaultGlobalDispatcher
  implicit val ccuad = {
    val currentId = new java.util.concurrent.atomic.AtomicInteger(1)
    val dt = new LocalDateTime(0L)
    new CanCreateUuidsAndDateTimes {
      override def getUuid(): java.util.UUID = ???
      override def getUniqueString(): String = s"id-${currentId.getAndIncrement()}"
      override def getDateTime(): DateTime = ???
      override def getUtcTimestamp(): LocalDateTime = dt
      override def parseUuid(str: String): AlmValidation[java.util.UUID] = ???
    }
  }

  private case class StatusEventResults(initiated: Int, executed: Int, failed: Int) {
    override def toString(): String =
      s"(initiated: $initiated, executed: $executed, failed: $failed)"
  }

  info("These tests have timing issues since one can not really tell, whether commands overlap unless effort is taken to prevent this.")

  "The AggregateRootHive" when {
    import almhirt.eventlog.AggregateEventLog._
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
          Flow(streams.systemEventStream).produceTo(DelegatingSubscriber[SystemEvent](statusProbe.ref))
          within(1 second) {
            Flow(CreateUser(CommandHeader(), "a", 0L, "hans", "meier") :: Nil).produceTo(commandSubscriber)
            statusProbe.expectMsgType[CommandStatusChanged].status should equal(CommandStatus.Initiated)
            statusProbe.expectMsgType[CommandStatusChanged].status should equal(CommandStatus.Executed)
          }
        }
      }
      "2 aggregate roots are created" should {
        "emit the status events [Initiated(a), Executed(a)] and [Initiated(b), Executed(b)]" in { fixture ⇒
          val FixtureParam(testId, commandSubscriber, eventlog, streams) = fixture
          val statusProbe = TestProbe()
          Flow(streams.systemEventStream).produceTo(DelegatingSubscriber[SystemEvent](statusProbe.ref))
          within(1 second) {
            Flow(List(
              CreateUser(CommandHeader(), "a", 0L, "hans", "meier"),
              CreateUser(CommandHeader(), "b", 0L, "hans", "meier"))).produceTo(commandSubscriber)
            assertStatusEvents(initiated = 2, ok = 2, failed = 0, statusProbe.receiveN(2 * 2, 1 second))
          }
        }
      }
      val n = 1000
      s"$n aggregate roots are created" should {
        s"emit the status events [Initiated, Executed] $n times" in { fixture ⇒
          val FixtureParam(testId, commandSubscriber, eventlog, streams) = fixture
          val statusProbe = TestProbe()
          Flow(streams.systemEventStream).produceTo(DelegatingSubscriber[SystemEvent](statusProbe.ref))
          val start = Deadline.now
          within(3 seconds) {
            Flow((1 to n).toSeq.map(id ⇒ CreateUser(CommandHeader(), s"$id", 0L, "hans", "meier"))).produceTo(commandSubscriber)
            assertStatusEvents(initiated = n, ok = n, failed = 0, statusProbe.receiveN(n * 2, 2 seconds))
          }
          val time = start.lap
          info(s"Dispatched ${n} in ${start.lap.defaultUnitString}((${(n * 1000).toDouble / time.toMillis}/s)).")
        }
      }
      s"$n aggregate roots are created and then updated" should {
        s"emit the status events ([Initiated, Executed]x2) $n times" in { fixture ⇒
          val FixtureParam(testId, commandSubscriber, eventlog, streams) = fixture
          val statusProbe = TestProbe()
          Flow(streams.systemEventStream).produceTo(DelegatingSubscriber[SystemEvent](statusProbe.ref))
          val flow1 = Flow((1 to n).toSeq.map(id ⇒ CreateUser(CommandHeader(), s"$id", 0L, "hans", "meier"): AggregateRootCommand))
          val flow2 = Flow((1 to n).toSeq.map(id ⇒ ChangeUserLastname(CommandHeader(), s"$id", 1L, "müller"): AggregateRootCommand))
          val start = Deadline.now
          within(3 seconds) {
            flow1.concat(flow2.toPublisher()).produceTo(commandSubscriber)
            assertStatusEvents(initiated = 2 * n, ok = 2 * n, failed = 0, statusProbe.receiveN(2 * n * 2, 2 second))
          }
          val time = start.lap
          info(s"Dispatched ${n} commands in ${start.lap.defaultUnitString}((${(n * 1000).toDouble / time.toMillis}/s)).")
        }
      }
      s"$n aggregate roots are created, updated and then deleted" should {
        s"emit the status events ([Initiated, Executed]x3) $n times" in { fixture ⇒
          val FixtureParam(testId, commandSubscriber, eventlog, streams) = fixture
          val statusProbe = TestProbe()
          Flow(streams.systemEventStream).produceTo(DelegatingSubscriber[SystemEvent](statusProbe.ref))
          val flow1 = Flow((1 to n).toSeq.map(id ⇒ CreateUser(CommandHeader(), s"$id", 0L, "hans", "meier"): AggregateRootCommand))
          val flow2 = Flow((1 to n).toSeq.map(id ⇒ ChangeUserLastname(CommandHeader(), s"$id", 1L, "müller"): AggregateRootCommand))
          val flow3 = Flow((1 to n).toSeq.map(id ⇒ ConfirmUserDeath(CommandHeader(), s"$id", 2L): AggregateRootCommand))
          val start = Deadline.now
          within(3 seconds) {
            flow1.concat(flow2.toPublisher()).concat(flow3.toPublisher()).produceTo(commandSubscriber)
            assertStatusEvents(initiated = 3 * n, ok = 3 * n, failed = 0, statusProbe.receiveN(3 * n * 2, 2 seconds))
          }
          val time = start.lap
          info(s"Dispatched ${n} commands in ${start.lap.defaultUnitString}((${(n * 1000).toDouble / time.toMillis}/s)).")
        }
      }
      val bigN = 5000
      s"$bigN aggregate roots are created, updated and then deleted" should {
        s"emit the status events ([Initiated, Executed]x3) $bigN times" in { fixture ⇒
          val FixtureParam(testId, commandSubscriber, eventlog, streams) = fixture
          val statusProbe = TestProbe()
          Flow(streams.systemEventStream).produceTo(DelegatingSubscriber[SystemEvent](statusProbe.ref))
          val flow1 = Flow((1 to bigN).toSeq.map(id ⇒ CreateUser(CommandHeader(), s"$id", 0L, "hans", "meier"): AggregateRootCommand))
          val flow2 = Flow((1 to bigN).toSeq.map(id ⇒ ChangeUserLastname(CommandHeader(), s"$id", 1L, "müller"): AggregateRootCommand))
          val flow3 = Flow((1 to bigN).toSeq.map(id ⇒ ConfirmUserDeath(CommandHeader(), s"$id", 2L): AggregateRootCommand))
          val start = Deadline.now
          within(15 seconds) {
            flow1.concat(flow2.toPublisher()).concat(flow3.toPublisher()).produceTo(commandSubscriber)
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
    val testId = nextTestId
    //info(s"Test $testId")
    val eventlogProps: Props = almhirt.eventlog.InMemoryAggregateEventLog.props()
    val eventlogActor: ActorRef = system.actorOf(eventlogProps, s"eventlog-$testId")

    val (streams, stopStreams) = AlmhirtStreams.supervised(s"streams-$testId", 1 second).awaitResultOrEscalate(1 second)

    val droneProps: Props = Props(
      new AggregateRootDrone[User, UserEvent] with ActorLogging with UserEventHandler with UserCommandHandler with UserUpdater with AggregateRootDroneCommandHandlerAdaptor[User, UserEvent] {
        def ccuad = AggregateRootHiveTests.this.ccuad
        def futuresContext: ExecutionContext = executionContext
        def aggregateEventLog: ActorRef = eventlogActor
        def snapshotStorage: Option[ActorRef] = None
        val eventsBroker: StreamBroker[Event] = streams.eventBroker

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

    val hiveProps = Props(
      new AggregateRootHive(
        HiveDescriptor(s"hive-$testId"),
        commandBuffersize = 10,
        droneFactory = droneFactory,
        streams.eventBroker)(AggregateRootHiveTests.this.ccuad, AggregateRootHiveTests.this.executionContext))
    val hiveActor = system.actorOf(hiveProps, s"hive-$testId-test")
    val hiveSubscriber = akka.stream.actor.ActorSubscriber[AggregateRootCommand](hiveActor)

    try {
      withFixture(test.toNoArgTest(FixtureParam(testId, hiveSubscriber, eventlogActor, streams)))
    } finally {
      system.stop(hiveActor)
      system.stop(eventlogActor)
      stopStreams.stop()
    }
  }

  override def beforeAll() {

  }

  override def afterAll() {
    TestKit.shutdownActorSystem(system)
  }

}