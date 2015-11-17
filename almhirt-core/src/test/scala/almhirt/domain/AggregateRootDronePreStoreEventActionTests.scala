package almhirt.domain

import scala.language.postfixOps
import java.time.{ ZonedDateTime, LocalDateTime }
import scala.concurrent._
import scala.concurrent.duration._
import akka.actor._
import almhirt.common._
import akka.testkit._
import org.scalatest._

class AggregateRootDronePreStoreEventActionTests(_system: ActorSystem)
    extends TestKit(_system) with fixture.WordSpecLike with Matchers with BeforeAndAfterAll {
  def this() = this(ActorSystem("AggregateRootDronePreStoreEventActionTests", almhirt.TestConfigs.logErrorConfig))

  implicit val executionContext = system.dispatchers.defaultGlobalDispatcher
  implicit val ccuad = {
    val dt = LocalDateTime.of(0: Int, 0: Int, 0: Int, 0: Int, 0: Int)
    new CanCreateUuidsAndDateTimes {
      override def getUuid(): java.util.UUID = ???
      override def getUniqueString(): String = "unique"
      override def getDateTime(): ZonedDateTime = ???
      override def getUtcTimestamp(): LocalDateTime = dt
    }
  }

  "The AggregateRootDrone" when {
    import almhirt.eventlog.AggregateRootEventLog._
    import AggregateRootHiveInternals._
    import almhirt.aggregates._
    import aggregatesforthelazyones._
    import play.api.libs.iteratee._
    "receiving valid commands" when {
      "UserSurnameChanged has a pre store action that succeeds" when {
        "UserLastChanged has a pre store action that fails" when {
          "receiving a create command" should {
            "succeed and not execute the pre store handler" in { fixture ⇒
              val FixtureParam(testId, drone, eventlog, probe, counter) = fixture
              within(10 seconds) {
                probe.send(drone, CreateUser(CommandHeader(), "a", 0L, "hans", "meier"))
                probe.expectMsgType[CommandExecuted]
                counter.get should equal(0)
              }
            }
          }
          "receiving a create command and then a ChangeUserSurname" should {
            "succeed and execute the pre store handler once" in { fixture ⇒
              val FixtureParam(testId, drone, eventlog, probe, counter) = fixture
              within(10 seconds) {
                probe.send(drone, CreateUser(CommandHeader(), "a", 0L, "hans", "meier"))
                probe.expectMsgType[CommandExecuted]
                counter.get should equal(0)
                probe.send(drone, ChangeUserSurname(CommandHeader(), "a", 1L, "miller"))
                probe.expectMsgType[CommandExecuted]
                counter.get should equal(1)
              }
            }
            "store the correct events" in { fixture ⇒
              val FixtureParam(testId, drone, eventlog, probe, counter) = fixture
              within(10 seconds) {
                probe.send(drone, CreateUser(CommandHeader(), "a", 0L, "hans", "meier"))
                probe.expectMsgType[CommandExecuted]
                probe.send(drone, ChangeUserSurname(CommandHeader(), "a", 1L, "miller"))
                probe.expectMsgType[CommandExecuted]
                probe.send(eventlog, GetAggregateRootEventsFrom("a", 0L))
                val eventsEnumerator = probe.expectMsgType[FetchedAggregateRootEvents].enumerator
                val iteratee = Iteratee.fold[AggregateRootEvent, Vector[AggregateRootEvent]](Vector.empty) { case (acc, cur) ⇒ acc :+ cur }
                val events: Vector[AggregateRootEvent] = Await.result(eventsEnumerator.run(iteratee), 100.millis.dilated)
                events should equal(List(
                  UserCreated(EventHeader(), "a", 0, "hans", "meier"),
                  UserSurnameChanged(EventHeader(), "a", 1, "miller")))
              }
            }
          }
          "receiving a create command and then a ChangeUserSurname and then a ChangeUserAge" should {
            "succeed and execute the pre store handler once" in { fixture ⇒
              val FixtureParam(testId, drone, eventlog, probe, counter) = fixture
              within(10 seconds) {
                probe.send(drone, CreateUser(CommandHeader(), "a", 0L, "hans", "meier"))
                probe.expectMsgType[CommandExecuted]
                counter.get should equal(0)
                probe.send(drone, ChangeUserSurname(CommandHeader(), "a", 1L, "miller"))
                probe.expectMsgType[CommandExecuted]
                counter.get should equal(1)
                probe.send(drone, ChangeUserAge(CommandHeader(), "a", 2L, 25))
                probe.expectMsgType[CommandExecuted]
                counter.get should equal(1)
              }
            }
          }
          "receiving a create command and then a ChangeUserSurname and then a ChangeUserAge and then a ChangeUserSurname " should {
            "succeed and execute the pre store handler twice" in { fixture ⇒
              val FixtureParam(testId, drone, eventlog, probe, counter) = fixture
              within(10 seconds) {
                probe.send(drone, CreateUser(CommandHeader(), "a", 0L, "hans", "meier"))
                probe.expectMsgType[CommandExecuted]
                counter.get should equal(0)
                probe.send(drone, ChangeUserSurname(CommandHeader(), "a", 1L, "miller"))
                probe.expectMsgType[CommandExecuted]
                counter.get should equal(1)
                probe.send(drone, ChangeUserAge(CommandHeader(), "a", 2L, 25))
                probe.expectMsgType[CommandExecuted]
                counter.get should equal(1)
                probe.send(drone, ChangeUserSurname(CommandHeader(), "a", 3L, "smith"))
                probe.expectMsgType[CommandExecuted]
                counter.get should equal(2)
              }
            }
          }
          "receiving a create command and then a ChangeUserSurname" should {
            "fail when executing the prestore action" in { fixture ⇒
              val FixtureParam(testId, drone, eventlog, probe, counter) = fixture
              within(10 seconds) {
                probe.send(drone, CreateUser(CommandHeader(), "a", 0L, "hans", "meier"))
                probe.expectMsgType[CommandExecuted]
                counter.get should equal(0)
                probe.send(drone, ChangeUserLastname(CommandHeader(), "a", 1L, "miller"))
                probe.expectMsgType[CommandNotExecuted]
                counter.get should equal(0)
              }
            }
            "fail but store the UserCreatedEvent" in { fixture ⇒
              val FixtureParam(testId, drone, eventlog, probe, counter) = fixture
              within(10 seconds) {
                probe.send(drone, CreateUser(CommandHeader(), "a", 0L, "hans", "meier"))
                probe.expectMsgType[CommandExecuted]
                probe.send(drone, ChangeUserLastname(CommandHeader(), "a", 1L, "miller"))
                probe.expectMsgType[CommandNotExecuted]
                probe.send(eventlog, GetAggregateRootEventsFrom("a", 0L))
                val eventsEnumerator = probe.expectMsgType[FetchedAggregateRootEvents].enumerator
                val iteratee = Iteratee.fold[AggregateRootEvent, Vector[AggregateRootEvent]](Vector.empty) { case (acc, cur) ⇒ acc :+ cur }
                val events: Vector[AggregateRootEvent] = Await.result(eventsEnumerator.run(iteratee), 100.millis.dilated)
                events should equal(List(UserCreated(EventHeader(), "a", 0, "hans", "meier")))
              }
            }
          }
        }
      }
    }
  }

  private val currentTestId = new java.util.concurrent.atomic.AtomicInteger(1)
  def nextTestId = currentTestId.getAndIncrement()

  case class FixtureParam(testId: Int, drone: ActorRef, eventlog: ActorRef, probe: TestProbe, counter: java.util.concurrent.atomic.AtomicInteger)

  def withFixture(test: OneArgTest) = {
    import almhirt.aggregates._
    import almhirt.tracking.CommandStatusChanged
    import almhirt.streaming._

    val testId = nextTestId
    //info(s"Test $testId")
    val eventlogProps: Props = almhirt.eventlog.InMemoryAggregateRootEventLog.props()
    val eventlogActor: ActorRef = system.actorOf(eventlogProps, s"eventlog-$testId")
    val streams = AlmhirtStreams(s"almhirt-streams-$testId")(10 seconds).awaitResultOrEscalate(10 seconds)

    val testProbe = TestProbe()

    val counter = new java.util.concurrent.atomic.AtomicInteger(0)

    val droneProps: Props = Props(
      new AggregateRootDrone[User, UserEvent] with ActorLogging with UserEventHandler with UserCommandHandler with UserUpdater with AggregateRootDroneCommandHandlerAdaptor[User, UserCommand, UserEvent] {
        val arTag = scala.reflect.ClassTag[User](classOf[User])
        val snapshotting = None
        def ccuad = AggregateRootDronePreStoreEventActionTests.this.ccuad
        def futuresContext: ExecutionContext = executionContext
        def aggregateEventLog: ActorRef = eventlogActor
        val eventsBroker: StreamBroker[Event] = streams.eventBroker
        val notifyHiveAboutUndispatchedEventsAfter: Option[FiniteDuration] = None
        val notifyHiveAboutUnstoredEventsAfterPerEvent: Option[FiniteDuration] = None
        def retryEventLogActionDelay: Option[FiniteDuration] = None
        val preStoreActionFor = (e: UserEvent) ⇒ e match {
          case _: UserSurnameChanged ⇒ PreStoreEventAction.AsyncPreStoreAction(() ⇒ AlmFuture.successful {
            val c = counter.incrementAndGet()
            info(s"Num executed: $c")
            ()
          })
          case _: UserLastnameChanged ⇒ PreStoreEventAction.AsyncPreStoreAction(() ⇒ AlmFuture.failed {
            UnspecifiedProblem("The pre store action for UserLastnameChanged causes this failure")
          })
          case _ ⇒ PreStoreEventAction.NoAction
        }

        override def logWarning(msg: ⇒ String, cause: Option[almhirt.problem.ProblemCause]): Unit = {}

        override def logWarning(msg: ⇒ String): Unit = {}

        override def logError(msg: ⇒ String, cause: almhirt.problem.ProblemCause): Unit = {}

        override def asyncInitializeForCommand(cmd: AggregateRootCommand, state: AggregateRootLifecycle[User]): Option[AlmFuture[Unit]] = Some(AlmFuture.successful(info(s"Init: $cmd")))

        override def asyncCleanupAfterCommand(cmd: AggregateRootCommand, problem: Option[Problem], state: Option[AggregateRootLifecycle[User]]): Option[AlmFuture[Unit]] = Some(AlmFuture.successful(info(s"Cleanup: $cmd")))

        val returnToUnitializedAfter = None

        override val aggregateCommandValidator = AggregateRootCommandValidator.Validated
        override val tag = scala.reflect.ClassTag[UserCommand](classOf[UserCommand])

        override def sendMessage(msg: AggregateRootHiveInternals.AggregateDroneMessage) {
          testProbe.ref ! msg
        }
      })

    val droneActor: ActorRef = system.actorOf(droneProps, s"drone-$testId")
    try {
      withFixture(test.toNoArgTest(FixtureParam(testId, droneActor, eventlogActor, testProbe, counter)))
    } finally {
      system.stop(droneActor)
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