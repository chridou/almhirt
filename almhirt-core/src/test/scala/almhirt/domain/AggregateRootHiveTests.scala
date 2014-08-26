package almhirt.domain

import scala.language.postfixOps
import org.joda.time.{ DateTime, LocalDateTime, DateTimeZone }
import scala.concurrent._
import scala.concurrent.duration._
import org.reactivestreams.api.Consumer
import akka.actor._
import akka.stream.{ FlowMaterializer, MaterializerSettings }
import akka.stream.scaladsl.Flow
import almhirt.common._
import akka.testkit._
import org.scalatest._

class AggregateRootHiveTests(_system: ActorSystem)
  extends TestKit(_system) with fixture.WordSpecLike with Matchers with BeforeAndAfterAll {
  def this() = this(ActorSystem("AggregateRootHiveTests", almhirt.TestConfigs.logDebugConfig))

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

  "The AggregateRootHive" when {
    import almhirt.eventlog.AggregateEventLog._
    import almhirt.aggregates._
    import aggregatesforthelazyones._
    import almhirt.tracking._

    def splitStatusEvents(events: Seq[Any]): (Seq[CommandExecutionStarted], Seq[CommandSuccessfullyExecuted], Seq[CommandFailed]) =
      events.collect { case x: CommandStatusChanged ⇒ x }.foldLeft((Seq[CommandExecutionStarted](), Seq[CommandSuccessfullyExecuted](), Seq[CommandFailed]())) {
        case ((a, b, c), cur) ⇒
          cur match {
            case x: CommandExecutionStarted ⇒ (a :+ x, b, c)
            case x: CommandSuccessfullyExecuted ⇒ (a, b :+ x, c)
            case x: CommandFailed ⇒ (a, b, c :+ x)
          }
      }

    def assertStatusEvents(started: Int, ok: Int, failed: Int, events: Seq[Any]) {
      val (s, o, f) = splitStatusEvents(events)
      (s.size, o.size, f.size) should equal((started, ok, failed))
    }

    val mat = FlowMaterializer(MaterializerSettings())
    "receiving valid commands" when {
      "an aggregate root is created" should {
        "should emit the status events [Start, Executed]" in { fixture ⇒
          val FixtureParam(testId, commandConsumer, eventlog, eventsProbe, statusProbe) = fixture
          within(1 second) {
            Flow(CreateUser(CommandHeader(), "a", 0L, "hans", "meier") :: Nil).produceTo(mat, commandConsumer)
            statusProbe.expectMsgType[CommandExecutionStarted]
            statusProbe.expectMsgType[CommandSuccessfullyExecuted]
          }
        }
      }
      "2 aggregate roots are created" should {
        "emit the status events [Start(a), Executed(a)] and [Start(b), Executed(b)]" in { fixture ⇒
          val FixtureParam(testId, commandConsumer, eventlog, eventsProbe, statusProbe) = fixture
          within(1 second) {
            Flow(List(
              CreateUser(CommandHeader(), "a", 0L, "hans", "meier"),
              CreateUser(CommandHeader(), "b", 0L, "hans", "meier"))).produceTo(mat, commandConsumer)
            assertStatusEvents(started = 2, ok = 2, failed = 0, statusProbe.receiveN(4, 1 second))
          }
        }
      }
      val n = 1000
      s"$n aggregate roots are created" should {
        s"emit the status events [Start(a), Executed(a)] $n times" in { fixture ⇒
          val FixtureParam(testId, commandConsumer, eventlog, eventsProbe, statusProbe) = fixture
          val start = Deadline.now
          within(3 seconds) {
            Flow((1 to n).toSeq.map(id ⇒ CreateUser(CommandHeader(), s"$id", 0L, "hans", "meier"))).produceTo(mat, commandConsumer)
            assertStatusEvents(started = n, ok = n, failed = 0, statusProbe.receiveN(2 * n, 2 seconds))
          }
          info(s"Took ${start.lap.defaultUnitString}")
        }
      }
      s"$n aggregate roots are created and then updated" should {
        s"emit the status events ([Start(a), Executed(a)]x2) $n times" in { fixture ⇒
          val FixtureParam(testId, commandConsumer, eventlog, eventsProbe, statusProbe) = fixture
          val flow1 = Flow((1 to n).toSeq.map(id ⇒ CreateUser(CommandHeader(), s"$id", 0L, "hans", "meier"): AggregateCommand))
          val flow2 = Flow((1 to n).toSeq.map(id ⇒ ChangeUserLastname(CommandHeader(), s"$id", 1L, "müller"): AggregateCommand))
          val start = Deadline.now
          within(3 seconds) {
            flow1.concat(flow2.toProducer(mat)).produceTo(mat, commandConsumer)
            assertStatusEvents(started = 2 * n, ok = 2 * n, failed = 0, statusProbe.receiveN(4 * n, 2 second))
          }
          info(s"Took ${start.lap.defaultUnitString}")
        }
      }
      s"$n aggregate roots are created, updated and then deleted" should {
        s"emit the status events ([Start(a), Executed(a)]x3) $n times" in { fixture ⇒
          val FixtureParam(testId, commandConsumer, eventlog, eventsProbe, statusProbe) = fixture
          val flow1 = Flow((1 to n).toSeq.map(id ⇒ CreateUser(CommandHeader(), s"$id", 0L, "hans", "meier"): AggregateCommand))
          val flow2 = Flow((1 to n).toSeq.map(id ⇒ ChangeUserLastname(CommandHeader(), s"$id", 1L, "müller"): AggregateCommand))
          val flow3 = Flow((1 to n).toSeq.map(id ⇒ ConfirmUserDeath(CommandHeader(), s"$id", 2L): AggregateCommand))
          val start = Deadline.now
          within(3 seconds) {
            flow1.concat(flow2.toProducer(mat)).concat(flow3.toProducer(mat)).produceTo(mat, commandConsumer)
            assertStatusEvents(started = 3 * n, ok = 3 * n, failed = 0, statusProbe.receiveN(6 * n, 2 second))
          }
          info(s"Took ${start.lap.defaultUnitString}")
        }
      }
    }
  }

  private val currentTestId = new java.util.concurrent.atomic.AtomicInteger(1)
  def nextTestId = currentTestId.getAndIncrement()

  case class FixtureParam(
    testId: Int,
    hiveConsumer: Consumer[AggregateCommand],
    eventlog: ActorRef,
    eventsProbe: TestProbe,
    statusProbe: TestProbe)

  def withFixture(test: OneArgTest) = {
    import almhirt.aggregates._
    import almhirt.tracking.CommandStatusChanged
    import almhirt.streaming.{ SequentialPostOfficeClient, PostOffice, PostOfficeClientSettings }
    val testId = nextTestId
    //info(s"Test $testId")
    val eventlogProps: Props = almhirt.eventlog.InMemoryAggregateEventLog.props()
    val eventlogActor: ActorRef = system.actorOf(eventlogProps, s"eventlog-$testId")
    val eventsProbe = TestProbe()
    val statusProbe = TestProbe()
    val cmdStatusSink = FireAndForgetSink.delegating[CommandStatusChanged](elem ⇒ statusProbe.ref ! elem)
    val droneProps: Props = Props(
      new Actor with ActorLogging with AggregateRootDrone[User, UserEvent] with UserEventHandler with UserCommandHandler with UserUpdater with AggregateRootDroneCommandHandlerAdaptor[User, UserEvent] with SequentialPostOfficeClient {
        def ccuad = AggregateRootHiveTests.this.ccuad
        def futuresContext: ExecutionContext = executionContext
        def aggregateEventLog: ActorRef = eventlogActor
        def snapshotStorage: Option[ActorRef] = None
        val commandStatusSink = cmdStatusSink
        val postOfficeSettings = PostOfficeClientSettings(100, (50 millis).dilated, 10)
        val eventsPostOffice = PostOffice.faked[Event](eventsProbe.ref)
      })

    val droneFactory = new AggregateRootDroneFactory {
      import scalaz._, Scalaz._
      def propsForCommand(command: AggregateCommand): AlmValidation[Props] = {
        command match {
          case c: UserCommand ⇒ droneProps.success
          case x ⇒ NoSuchElementProblem(s"I don't have props for command $x").failure
        }
      }
    }

    val hiveProps = Props(
      new AggregateRootHive(
        HiveDescriptor(s"hive-$testId"),
        buffersize = 10,
        AggregateRootHive.CommandTimeouts(commandTimeout = (1 second).dilated, checkForTimeoutsInterval = (100 millis).dilated),
        droneFactory = droneFactory,
        commandStatusSink = cmdStatusSink)(AggregateRootHiveTests.this.ccuad, AggregateRootHiveTests.this.executionContext))
    val hiveActor = system.actorOf(hiveProps, s"hive-$testId-test")
    val hiveConsumer = akka.stream.actor.ActorConsumer[AggregateCommand](hiveActor)

    try {
      withFixture(test.toNoArgTest(FixtureParam(testId, hiveConsumer, eventlogActor, eventsProbe, statusProbe)))
    } finally {
      system.stop(hiveActor)
      system.stop(eventlogActor)
    }
  }

  override def beforeAll() {

  }

  override def afterAll() {
    TestKit.shutdownActorSystem(system)
  }

}