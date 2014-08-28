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
import almhirt.streaming._
import akka.stream.scaladsl.Flow
import akka.stream._

import akka.testkit._
import org.scalatest._

class AggregateRootNexusTests(_system: ActorSystem)
  extends TestKit(_system) with fixture.WordSpecLike with Matchers with BeforeAndAfterAll {
  def this() = this(ActorSystem("AggregateRootNexusTests", almhirt.TestConfigs.logWarningConfig))

  implicit val executionContext = system.dispatchers.defaultGlobalDispatcher
  implicit val ccuad = CanCreateUuidsAndDateTimes()

  val counter = new java.util.concurrent.atomic.AtomicInteger(1)
  def nextCounter = counter.getAndIncrement()

  val rnd = new scala.util.Random()
  def createId(pre: Int): String = {
    s"${pre}-${rnd.nextInt(1000)}"
  }

  "The AggregateRootNexus" when {
    import almhirt.eventlog.AggregateEventLog._
    import almhirt.aggregates._
    import aggregatesforthelazyones._
    import almhirt.tracking._

    def splitStatusEvents(events: Seq[Any]): (Seq[CommandSuccessfullyExecuted], Seq[CommandExecutionFailed]) =
      events.collect { case x: CommandStatusChanged ⇒ x }.foldLeft((Seq[CommandSuccessfullyExecuted](), Seq[CommandExecutionFailed]())) {
        case ((a, b), cur) ⇒
          cur match {
            case x: CommandSuccessfullyExecuted ⇒ (a :+ x, b)
            case x: CommandExecutionFailed ⇒ (a, b :+ x)
          }
      }

    def assertStatusEvents(ok: Int, failed: Int, events: Seq[Any]) {
      val (o, f) = splitStatusEvents(events)
      (o.size, f.size) should equal((ok, failed))
    }

    val mat = FlowMaterializer(MaterializerSettings())
    "receiving valid commands" when {
      "an aggregate root is created" should {
        "should emit the status events [Executed]" in { fixture ⇒
          val FixtureParam(testId, commandConsumer, eventlog, streams) = fixture
          val statusProbe = TestProbe()
          Flow(streams.systemEventStream).produceTo(mat, DelegatingConsumer[SystemEvent](statusProbe.ref))
          within(2 seconds) {
            Flow(CreateUser(CommandHeader(), createId(1), 0L, "hans", "meier") :: Nil).produceTo(mat, commandConsumer)
            statusProbe.expectMsgType[CommandSuccessfullyExecuted]
          }
        }
      }
      "2 aggregate roots are created" should {
        "emit the status events [Executed(a)] and [Executed(b)]" in { fixture ⇒
          val FixtureParam(testId, commandConsumer, eventlog, streams) = fixture
          val statusProbe = TestProbe()
          Flow(streams.systemEventStream).produceTo(mat, DelegatingConsumer[SystemEvent](statusProbe.ref))
          within(2 seconds) {
            Flow(List(
              CreateUser(CommandHeader(), createId(1), 0L, "hans", "meier"),
              CreateUser(CommandHeader(), createId(2), 0L, "hans", "meier"))).produceTo(mat, commandConsumer)
            assertStatusEvents(ok = 2, failed = 0, statusProbe.receiveN(2, 3 seconds))
          }
        }
      }
      val nn = 2000
      val ids = (1 to nn).map(createId).toVector
      val n = ids.size
      s"$n aggregate roots are created" should {
        s"emit the status events [Executed(a)] $n times" in { fixture ⇒
          val FixtureParam(testId, commandConsumer, eventlog, streams) = fixture
          val statusProbe = TestProbe()
          Flow(streams.systemEventStream).produceTo(mat, DelegatingConsumer[SystemEvent](statusProbe.ref))
          val start = Deadline.now
          within(3 seconds) {
            Flow(ids.toStream.map(id ⇒ CreateUser(CommandHeader(), id, 0L, "hans", "meier"))).produceTo(mat, commandConsumer)
            assertStatusEvents(ok = n, failed = 0, statusProbe.receiveN(n, 3 seconds))
            info(s"Took ${start.lap.defaultUnitString}")
          }
        }
      }
      s"$n aggregate roots are created and then updated" should {
        s"emit the status events ([Executed(a)]x2) $n times" in { fixture ⇒
          val FixtureParam(testId, commandConsumer, eventlog, streams) = fixture
          val statusProbe = TestProbe()
          Flow(streams.systemEventStream).produceTo(mat, DelegatingConsumer[SystemEvent](statusProbe.ref))
          val flow1 = Flow(ids.toStream.map(id ⇒ CreateUser(CommandHeader(), id, 0L, "hans", "meier"): AggregateRootCommand))
          val flow2 = Flow(ids.toStream.map(id ⇒ ChangeUserLastname(CommandHeader(), id, 1L, "müller"): AggregateRootCommand))
          val start = Deadline.now
          within(3 seconds) {
            flow1.concat(flow2.toProducer(mat)).produceTo(mat, commandConsumer)
            assertStatusEvents(ok = 2 * n, failed = 0, statusProbe.receiveN(2 * n, 3 seconds))
            info(s"Took ${start.lap.defaultUnitString}")
          }
        }
      }
      s"$n aggregate roots are created, updated and then deleted" should {
        s"emit the status events ([Executed(a)]x3) $n times" in { fixture ⇒
          val FixtureParam(testId, commandConsumer, eventlog, streams) = fixture
          val statusProbe = TestProbe()
          Flow(streams.systemEventStream).produceTo(mat, DelegatingConsumer[SystemEvent](statusProbe.ref))
          val flow1 = Flow(ids.toStream.map(id ⇒ CreateUser(CommandHeader(), id, 0L, "hans", "meier"): AggregateRootCommand))
          val flow2 = Flow(ids.toStream.map(id ⇒ ChangeUserLastname(CommandHeader(), id, 1L, "müller"): AggregateRootCommand))
          val flow3 = Flow(ids.toStream.map(id ⇒ ConfirmUserDeath(CommandHeader(), id, 2L): AggregateRootCommand))
          val start = Deadline.now
          within(3 seconds) {
            flow1.concat(flow2.concat(flow3.toProducer(mat)).toProducer(mat)).produceTo(mat, commandConsumer)
            assertStatusEvents(ok = 3 * n, failed = 0, statusProbe.receiveN(3 * n, 3 seconds))
            info(s"Took ${start.lap.defaultUnitString}")
          }
        }
      }
    }
  }

  private val currentTestId = new java.util.concurrent.atomic.AtomicInteger(1)
  def nextTestId = currentTestId.getAndIncrement()

  case class FixtureParam(
    testId: Int,
    commandConsumer: Consumer[AggregateRootCommand],
    eventlog: ActorRef,
    streams: AlmhirtStreams)

  def withFixture(test: OneArgTest) = {
    import scalaz._, Scalaz._
    import akka.stream.scaladsl.Duct
    import almhirt.aggregates._

    val testId = nextTestId
    //info(s"Test $testId")
    val eventlogProps: Props = almhirt.eventlog.InMemoryAggregateEventLog.props()
    val eventlogActor: ActorRef = system.actorOf(eventlogProps, s"eventlog-$testId")

    val (streams, stopStreams) = AlmhirtStreams.supervised(s"streams-$testId", 1 second).awaitResultOrEscalate(1 second)
    val droneProps: Props = Props(
      new AggregateRootDrone[User, UserEvent] with ActorLogging with UserEventHandler with UserCommandHandler with UserUpdater with AggregateRootDroneCommandHandlerAdaptor[User, UserEvent] {
        def ccuad = AggregateRootNexusTests.this.ccuad
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

    def hiveProps(desc: HiveDescriptor) = Props(
      new AggregateRootHive(
        desc,
        buffersize = 10,
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

    val (commandConsumer, commandProducer) = Duct[AggregateRootCommand].build(FlowMaterializer(MaterializerSettings()))

    val nexusProps = Props(new AggregateRootNexus(commandProducer, hiveSelector, hiveFactory))
    val nexusActor = system.actorOf(nexusProps, s"nexus-$testId")
    try {
      withFixture(test.toNoArgTest(FixtureParam(testId, commandConsumer, eventlogActor, streams)))
    } finally {
      system.stop(nexusActor)
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