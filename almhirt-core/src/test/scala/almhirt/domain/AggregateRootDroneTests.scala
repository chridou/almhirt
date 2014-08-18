package almhirt.domain

import scala.language.postfixOps

import org.joda.time.{ DateTime, LocalDateTime, DateTimeZone }
import scala.concurrent._
import scala.concurrent.duration._
import akka.actor._
import almhirt.common._
import akka.testkit._
import org.scalatest._

class AggregateRootDroneTests(_system: ActorSystem)
  extends TestKit(_system) with fixture.WordSpecLike with Matchers with BeforeAndAfterAll {
  def this() = this(ActorSystem("AggregateRootDroneTests", almhirt.TestConfigs.logWarningConfig))

  implicit val executionContext = system.dispatchers.defaultGlobalDispatcher
  implicit val ccuad = {
    val dt = new LocalDateTime(0L)
    new CanCreateUuidsAndDateTimes {
      override def getUuid(): java.util.UUID = ???
      override def getUniqueString(): String = "unique"
      override def getDateTime(): DateTime = ???
      override def getUtcTimestamp(): LocalDateTime = dt
      override def parseUuid(str: String): AlmValidation[java.util.UUID] = ???
    }
  }

  "The AggregateRootDrone" when {
    import almhirt.eventlog.AggregateEventLog._
    import AggregateRootDroneInternalMessages._
    import almhirt.aggregates._
    import aggregatesforthelazyones._
    import play.api.libs.iteratee._
    "receiving valid commands" when {
      "no aggregate root exists" should {
        "execute a create command" in { fixture ⇒
          val FixtureParam(testId, drone, eventlog, probe) = fixture
          within(1 second) {
            probe.send(drone, CreateUser(CommandHeader(), "a", 0L, "hans", "meier"))
            probe.expectMsgType[CommandExecuted]
          }
        }
        "execute a create command and store the event" in { fixture ⇒
          val FixtureParam(testId, drone, eventlog, probe) = fixture
          within(1 second) {
            probe.send(drone, CreateUser(CommandHeader(), "a", 0L, "hans", "meier"))
            probe.expectMsgType[CommandExecuted]
            probe.send(eventlog, GetAllAggregateEventsFor("a"))
            val eventsEnumerator = probe.expectMsgType[FetchedAggregateEvents].enumerator
            val iteratee = Iteratee.fold[AggregateEvent, Vector[AggregateEvent]](Vector.empty) { case (acc, cur) => acc :+ cur }
            val events: Vector[AggregateEvent] = Await.result(eventsEnumerator.run(iteratee), 100 millis)
            events should equal(List(UserCreated(EventHeader(), "a", 0, "hans", "meier")))
          }
        }
      }
    }
  }

  private val currentTestId = new java.util.concurrent.atomic.AtomicInteger(1)
  def nextTestId = currentTestId.getAndIncrement()

  case class FixtureParam(testId: Int, drone: ActorRef, eventlog: ActorRef, probe: TestProbe)

  def withFixture(test: OneArgTest) = {
    import almhirt.aggregates._
    val testId = nextTestId
    info(s"Test $testId")
    val eventlogProps: Props = almhirt.eventlog.InMemoryAggregateEventLog.props()
    val eventlogActor: ActorRef = system.actorOf(eventlogProps, s"eventlog-$testId")
    val testProbe = TestProbe()
    val droneProps: Props = Props(
      new Actor with ActorLogging with AggregateRootDrone[User, UserEvent] with UserEventHandler with UserCommandHandler with UserUpdater with AggregateRootDroneCommandHandlerAdaptor[User, UserEvent] {
        def ccuad = AggregateRootDroneTests.this.ccuad
        def futuresContext: ExecutionContext = executionContext
        def aggregateEventLog: ActorRef = eventlogActor
        def snapshotStorage: Option[ActorRef] = None

        override def sendMessage(msg: AggregateRootDroneInternalMessages.AggregateDroneMessage) {
          testProbe.ref ! msg
        }

      })

    val droneActor: ActorRef = system.actorOf(droneProps, s"drone-$testId")
    try {
      withFixture(test.toNoArgTest(FixtureParam(testId, droneActor, eventlogActor, testProbe)))
    } finally {
      system.stop(droneActor)
      system.stop(eventlogActor)
    }
  }

  override def beforeAll() {

  }

  override def afterAll() {
    TestKit.shutdownActorSystem(system)
  }

}