package almhirt.domain

import scala.language.postfixOps
import java.time.{ ZonedDateTime, LocalDateTime }
import scala.concurrent._
import scala.concurrent.duration._
import akka.actor._
import almhirt.common._
import akka.testkit._
import org.scalatest._
import almhirt.snapshots.InMemorySnapshotRepository

class AggregateRootDroneWithSnapshottingTests(_system: ActorSystem)
    extends TestKit(_system) with fixture.WordSpecLike with Matchers with BeforeAndAfterAll {
  def this() = this(ActorSystem("AggregateRootDroneWithSnapshottingTests", almhirt.TestConfigs.logErrorConfig))

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
    import almhirt.snapshots._
    "receiving valid commands" when {
      "no aggregate root exists" should {
        "execute a create command" in { fixture ⇒
          val FixtureParam(testId, drone, eventlog, probe, snapshotRepository) = fixture
          within(10 seconds) {
            probe.send(drone, CreateUser(CommandHeader(), "a", 0L, "hans", "meier"))
            probe.expectMsgType[CommandExecuted]

            probe.send(snapshotRepository, SnapshotRepository.FindSnapshot("a"))
            val snapshotMsg = probe.expectMsgType[SnapshotRepository.FoundSnapshot]
            snapshotMsg.snapshoot.version.value should equal(1L)
          }
        }

        "execute a create command and then a modifying command" in { fixture ⇒
          val FixtureParam(testId, drone, eventlog, probe, snapshotRepository) = fixture
          within(10 seconds) {
            probe.send(drone, CreateUser(CommandHeader(), "a", 0L, "hans", "meier"))
            probe.expectMsgType[CommandExecuted]

            probe.send(snapshotRepository, SnapshotRepository.FindSnapshot("a"))
            var snapshotMsg = probe.expectMsgType[SnapshotRepository.FoundSnapshot]
            snapshotMsg.snapshoot.version.value should equal(1L)

            probe.send(drone, ChangeUserAgeForCreditCard(CommandHeader(), "a", 1L, 22))
            probe.expectMsgType[CommandExecuted]

            probe.send(snapshotRepository, SnapshotRepository.FindSnapshot("a"))
            snapshotMsg = probe.expectMsgType[SnapshotRepository.FoundSnapshot]
            snapshotMsg.snapshoot.version.value should equal(2L)

          }
        }

        "execute a create command, then a modifying and then a deleting command" in { fixture ⇒
          val FixtureParam(testId, drone, eventlog, probe, snapshotRepository) = fixture
          within(10 seconds) {
            probe.send(drone, CreateUser(CommandHeader(), "a", 0L, "hans", "meier"))
            probe.expectMsgType[CommandExecuted]

            probe.send(snapshotRepository, SnapshotRepository.FindSnapshot("a"))
            val snapshotMsg1 = probe.expectMsgType[SnapshotRepository.FoundSnapshot]
            snapshotMsg1.snapshoot.version.value should equal(1L)

            probe.send(drone, ChangeUserAgeForCreditCard(CommandHeader(), "a", 1L, 22))
            probe.expectMsgType[CommandExecuted]

            probe.send(snapshotRepository, SnapshotRepository.FindSnapshot("a"))
            val snapshotMsg2 = probe.expectMsgType[SnapshotRepository.FoundSnapshot]
            snapshotMsg2.snapshoot.version.value should equal(2L)

            probe.send(drone, ConfirmUserDeath(CommandHeader(), "a", 2L))
            probe.expectMsgType[CommandExecuted]

            probe.send(snapshotRepository, SnapshotRepository.FindSnapshot("a"))
            val snapshotMsg3 = probe.expectMsgType[SnapshotRepository.AggregateRootWasDeleted]
            snapshotMsg3.version.value should equal(3L)
          }
        }

        "execute a command that deletes the aggregate root right away" in { fixture ⇒
          val FixtureParam(testId, drone, eventlog, probe, snapshotRepository) = fixture
          within(10 seconds) {
            probe.send(drone, RejectUser(CommandHeader(), "a", 0L, "hans", "meier"))
            probe.expectMsgType[CommandExecuted]

            probe.send(snapshotRepository, SnapshotRepository.FindSnapshot("a"))
            val snapshotMsg1 = probe.expectMsgType[SnapshotRepository.AggregateRootWasDeleted]
            snapshotMsg1.version.value should equal(1L)
          }
        }

        "execute a create command and then a command that does nothing" in { fixture ⇒
          val FixtureParam(testId, drone, eventlog, probe, snapshotRepository) = fixture
          within(10 seconds) {
            probe.send(drone, CreateUser(CommandHeader(), "a", 0L, "hans", "meier"))
            probe.expectMsgType[CommandExecuted]

            probe.send(snapshotRepository, SnapshotRepository.FindSnapshot("a"))
            val snapshotMsg1 = probe.expectMsgType[SnapshotRepository.FoundSnapshot]
            snapshotMsg1.snapshoot.version.value should equal(1L)

            probe.send(drone, UserUow(CommandHeader(), "a", 1L, Seq.empty))
            probe.expectMsgType[CommandExecuted]

            probe.send(snapshotRepository, SnapshotRepository.FindSnapshot("a"))
            val snapshotMsg2 = probe.expectMsgType[SnapshotRepository.FoundSnapshot]
            snapshotMsg2.snapshoot.version.value should equal(1L)
          }
        }
      }
      "an aggregate root with version 2 exists" should {
        def createAr(eventlog: ActorRef, probe: TestProbe, snapshotRepository: ActorRef) {
          probe.send(eventlog, CommitAggregateRootEvent(UserCreated(EventHeader(), "a", 0, "hans", "meier")))
          probe.expectMsgType[AggregateRootEventCommitted]
          probe.send(eventlog, CommitAggregateRootEvent(UserSurnameChanged(EventHeader(), "a", 1, "peter")))
          probe.expectMsgType[AggregateRootEventCommitted]
          probe.send(snapshotRepository, SnapshotRepository.StoreSnapshot(User("a", 2, surname = "peter", lastname = "meier", age = None)))
          probe.expectMsgType[SnapshotRepository.SnapshotStored]
        }

        "execute a modifying command" in { fixture ⇒
          val FixtureParam(testId, drone, eventlog, probe, snapshotRepository) = fixture
          within(10 seconds) {
            createAr(eventlog, probe, snapshotRepository)
            probe.send(drone, ChangeUserLastname(CommandHeader(), "a", 2L, "müller"))
            probe.expectMsgType[CommandExecuted]

            probe.send(snapshotRepository, SnapshotRepository.FindSnapshot("a"))
            val snapshotMsg1 = probe.expectMsgType[SnapshotRepository.FoundSnapshot]
            snapshotMsg1.snapshoot.version.value should equal(3L)
          }
        }

        "execute a deleting command" in { fixture ⇒
          val FixtureParam(testId, drone, eventlog, probe, snapshotRepository) = fixture
          within(10 seconds) {
            createAr(eventlog, probe, snapshotRepository)
            probe.send(drone, ConfirmUserDeath(CommandHeader(), "a", 2L))
            probe.expectMsgType[CommandExecuted]

            probe.send(snapshotRepository, SnapshotRepository.FindSnapshot("a"))
            val snapshotMsg1 = probe.expectMsgType[SnapshotRepository.AggregateRootWasDeleted]
            snapshotMsg1.version.value should equal(3L)
          }
        }
      }
      "an aggregate root with version 2 exists but the snapshot is for version 1" should {
        def createAr(eventlog: ActorRef, probe: TestProbe, snapshotRepository: ActorRef) {
          probe.send(eventlog, CommitAggregateRootEvent(UserCreated(EventHeader(), "a", 0, "hans", "meier")))
          probe.expectMsgType[AggregateRootEventCommitted]
          probe.send(snapshotRepository, SnapshotRepository.StoreSnapshot(User("a", 1, surname = "hans", lastname = "meier", age = None)))
          probe.expectMsgType[SnapshotRepository.SnapshotStored]
          probe.send(eventlog, CommitAggregateRootEvent(UserSurnameChanged(EventHeader(), "a", 1, "peter")))
          probe.expectMsgType[AggregateRootEventCommitted]
        }

        "execute a modifying command" in { fixture ⇒
          val FixtureParam(testId, drone, eventlog, probe, snapshotRepository) = fixture
          within(10 seconds) {
            createAr(eventlog, probe, snapshotRepository)
            probe.send(drone, ChangeUserLastname(CommandHeader(), "a", 2L, "müller"))
            probe.expectMsgType[CommandExecuted]

            probe.send(snapshotRepository, SnapshotRepository.FindSnapshot("a"))
            val snapshotMsg1 = probe.expectMsgType[SnapshotRepository.FoundSnapshot]
            snapshotMsg1.snapshoot.version.value should equal(3L)
          }
        }

        "execute a deleting command" in { fixture ⇒
          val FixtureParam(testId, drone, eventlog, probe, snapshotRepository) = fixture
          within(10 seconds) {
            createAr(eventlog, probe, snapshotRepository)
            probe.send(drone, ConfirmUserDeath(CommandHeader(), "a", 2L))
            probe.expectMsgType[CommandExecuted]

            probe.send(snapshotRepository, SnapshotRepository.FindSnapshot("a"))
            val snapshotMsg1 = probe.expectMsgType[SnapshotRepository.AggregateRootWasDeleted]
            snapshotMsg1.version.value should equal(3L)
          }
        }
      }
    }
    "receiving invalid commands" when {
      "no aggregate root exists" should {
        "not execute a modifying command" in { fixture ⇒
          val FixtureParam(testId, drone, eventlog, probe, snapshotRepository) = fixture
          within(10 seconds) {
            probe.send(drone, ChangeUserLastname(CommandHeader(), "a", 0, "müller"))
            probe.expectMsgType[CommandNotExecuted]

            probe.send(snapshotRepository, SnapshotRepository.FindSnapshot("a"))
            probe.expectMsgType[SnapshotRepository.SnapshotNotFound]
          }
        }
        "not execute a deleting command" in { fixture ⇒
          val FixtureParam(testId, drone, eventlog, probe, snapshotRepository) = fixture
          within(10 seconds) {
            probe.send(drone, ConfirmUserDeath(CommandHeader(), "a", 0))
            probe.expectMsgType[CommandNotExecuted]

            probe.send(snapshotRepository, SnapshotRepository.FindSnapshot("a"))
            probe.expectMsgType[SnapshotRepository.SnapshotNotFound]
          }
        }
      }
      "an aggregate root with version 2 exists" should {
        def createAr(eventlog: ActorRef, probe: TestProbe, snapshotRepository: ActorRef) {
          probe.send(eventlog, CommitAggregateRootEvent(UserCreated(EventHeader(), "a", 0, "hans", "meier")))
          probe.expectMsgType[AggregateRootEventCommitted]
          probe.send(eventlog, CommitAggregateRootEvent(UserSurnameChanged(EventHeader(), "a", 1, "peter")))
          probe.expectMsgType[AggregateRootEventCommitted]
          probe.send(snapshotRepository, SnapshotRepository.StoreSnapshot(User("a", 2, surname = "peter", lastname = "meier", age = None)))
          probe.expectMsgType[SnapshotRepository.SnapshotStored]
        }

        "not execute a creating command that targets version 0" in { fixture ⇒
          val FixtureParam(testId, drone, eventlog, probe, snapshotRepository) = fixture
          within(10 seconds) {
            createAr(eventlog, probe, snapshotRepository)
            probe.send(drone, CreateUser(CommandHeader(), "a", 0, "hans", "meier"))
            probe.expectMsgType[CommandNotExecuted]

            probe.send(snapshotRepository, SnapshotRepository.FindSnapshot("a"))
            val snapshotMsg1 = probe.expectMsgType[SnapshotRepository.FoundSnapshot]
            snapshotMsg1.snapshoot.version.value should equal(2L)
          }
        }

        "not execute a creating command that targets version 2" in { fixture ⇒
          val FixtureParam(testId, drone, eventlog, probe, snapshotRepository) = fixture
          within(10 seconds) {
            createAr(eventlog, probe, snapshotRepository)
            probe.send(drone, CreateUser(CommandHeader(), "a", 2, "hans", "meier"))
            probe.expectMsgType[CommandNotExecuted]

            probe.send(snapshotRepository, SnapshotRepository.FindSnapshot("a"))
            val snapshotMsg1 = probe.expectMsgType[SnapshotRepository.FoundSnapshot]
            snapshotMsg1.snapshoot.version.value should equal(2L)
          }
        }
        "not execute an invalid command but afterwards a valid command" in { fixture ⇒
          val FixtureParam(testId, drone, eventlog, probe, snapshotRepository) = fixture
          within(10 seconds) {
            createAr(eventlog, probe, snapshotRepository)
            probe.send(drone, CreateUser(CommandHeader(), "a", 2, "hans", "meier"))
            probe.expectMsgType[CommandNotExecuted]
            probe.send(drone, ChangeUserLastname(CommandHeader(), "a", 2, "müller"))
            probe.expectMsgType[CommandExecuted]

            probe.send(snapshotRepository, SnapshotRepository.FindSnapshot("a"))
            val snapshotMsg1 = probe.expectMsgType[SnapshotRepository.FoundSnapshot]
            snapshotMsg1.snapshoot.version.value should equal(3L)
          }
        }
      }
      "an aggregate root with version 2 exists but the snapshot is for version 1" should {
        def createAr(eventlog: ActorRef, probe: TestProbe, snapshotRepository: ActorRef) {
          probe.send(eventlog, CommitAggregateRootEvent(UserCreated(EventHeader(), "a", 0, "hans", "meier")))
          probe.expectMsgType[AggregateRootEventCommitted]
          probe.send(snapshotRepository, SnapshotRepository.StoreSnapshot(User("a", 1, surname = "hans", lastname = "meier", age = None)))
          probe.expectMsgType[SnapshotRepository.SnapshotStored]
          probe.send(eventlog, CommitAggregateRootEvent(UserSurnameChanged(EventHeader(), "a", 1, "peter")))
          probe.expectMsgType[AggregateRootEventCommitted]
        }

        "not execute a creating command that targets version 0" in { fixture ⇒
          val FixtureParam(testId, drone, eventlog, probe, snapshotRepository) = fixture
          within(10 seconds) {
            createAr(eventlog, probe, snapshotRepository)
            probe.send(drone, CreateUser(CommandHeader(), "a", 0, "hans", "meier"))
            probe.expectMsgType[CommandNotExecuted]

            probe.send(snapshotRepository, SnapshotRepository.FindSnapshot("a"))
            val snapshotMsg1 = probe.expectMsgType[SnapshotRepository.FoundSnapshot]
            snapshotMsg1.snapshoot.version.value should equal(1L)
          }
        }

        "not execute a creating command that targets version 2" in { fixture ⇒
          val FixtureParam(testId, drone, eventlog, probe, snapshotRepository) = fixture
          within(10 seconds) {
            createAr(eventlog, probe, snapshotRepository)
            probe.send(drone, CreateUser(CommandHeader(), "a", 2, "hans", "meier"))
            probe.expectMsgType[CommandNotExecuted]

            probe.send(snapshotRepository, SnapshotRepository.FindSnapshot("a"))
            val snapshotMsg1 = probe.expectMsgType[SnapshotRepository.FoundSnapshot]
            snapshotMsg1.snapshoot.version.value should equal(1L)
          }
        }
        "not execute an invalid command but afterwards a valid command" in { fixture ⇒
          val FixtureParam(testId, drone, eventlog, probe, snapshotRepository) = fixture
          within(10 seconds) {
            createAr(eventlog, probe, snapshotRepository)
            probe.send(drone, CreateUser(CommandHeader(), "a", 2, "hans", "meier"))
            probe.expectMsgType[CommandNotExecuted]
            probe.send(drone, ChangeUserLastname(CommandHeader(), "a", 2, "müller"))
            probe.expectMsgType[CommandExecuted]

            probe.send(snapshotRepository, SnapshotRepository.FindSnapshot("a"))
            val snapshotMsg1 = probe.expectMsgType[SnapshotRepository.FoundSnapshot]
            snapshotMsg1.snapshoot.version.value should equal(3L)
          }
        }
      }

      "a dead aggregate root with version 3 exists" should {
        def createAr(eventlog: ActorRef, probe: TestProbe, snapshotRepository: ActorRef) {
          probe.send(eventlog, CommitAggregateRootEvent(UserCreated(EventHeader(), "a", 0, "hans", "meier")))
          probe.expectMsgType[AggregateRootEventCommitted]
          probe.send(eventlog, CommitAggregateRootEvent(UserSurnameChanged(EventHeader(), "a", 1, "peter")))
          probe.expectMsgType[AggregateRootEventCommitted]
          probe.send(eventlog, CommitAggregateRootEvent(UserDied(EventHeader(), "a", 2)))
          probe.expectMsgType[AggregateRootEventCommitted]
          probe.send(snapshotRepository, SnapshotRepository.MarkAggregateRootMortuus("a", 3))
          probe.expectMsgType[SnapshotRepository.AggregateRootMarkedMortuus]
        }

        "not execute a modifying command that targets version 3" in { fixture ⇒
          val FixtureParam(testId, drone, eventlog, probe, snapshotRepository) = fixture
          within(10 seconds) {
            createAr(eventlog, probe, snapshotRepository)
            probe.send(drone, ChangeUserLastname(CommandHeader(), "a", 3, "müller"))
            probe.expectMsgType[CommandNotExecuted]

            probe.send(snapshotRepository, SnapshotRepository.FindSnapshot("a"))
            val snapshotMsg1 = probe.expectMsgType[SnapshotRepository.AggregateRootWasDeleted]
            snapshotMsg1.version.value should equal(3L)

          }
        }
      }
      "a dead aggregate root with version 3 exists but the last snapshot is for version 2" should {
        def createAr(eventlog: ActorRef, probe: TestProbe, snapshotRepository: ActorRef) {
          probe.send(eventlog, CommitAggregateRootEvent(UserCreated(EventHeader(), "a", 0, "hans", "meier")))
          probe.expectMsgType[AggregateRootEventCommitted]
          probe.send(eventlog, CommitAggregateRootEvent(UserSurnameChanged(EventHeader(), "a", 1, "peter")))
          probe.expectMsgType[AggregateRootEventCommitted]
          probe.send(snapshotRepository, SnapshotRepository.StoreSnapshot(User("a", 2, surname = "peter", lastname = "meier", age = None)))
          probe.expectMsgType[SnapshotRepository.SnapshotStored]
          probe.send(eventlog, CommitAggregateRootEvent(UserDied(EventHeader(), "a", 2)))
          probe.expectMsgType[AggregateRootEventCommitted]
        }

        "not execute a modifying command that targets version 3" in { fixture ⇒
          val FixtureParam(testId, drone, eventlog, probe, snapshotRepository) = fixture
          within(10 seconds) {
            createAr(eventlog, probe, snapshotRepository)
            probe.send(drone, ChangeUserLastname(CommandHeader(), "a", 3, "müller"))
            probe.expectMsgType[CommandNotExecuted]

            probe.send(snapshotRepository, SnapshotRepository.FindSnapshot("a"))
            val snapshotMsg1 = probe.expectMsgType[SnapshotRepository.FoundSnapshot]
            snapshotMsg1.snapshoot.version.value should equal(2L)

          }
        }
      }
    }
    "the eventlog is corrupted" should {
      "crash when receiving an event" in { fixture ⇒
        val FixtureParam(testId, drone, eventlog, probe, snapshotRepository) = fixture
        within(10 seconds) {
          probe.send(eventlog, CommitAggregateRootEvent(UserSurnameChanged(EventHeader(), "a", 0, "peter")))
          probe.expectMsgType[AggregateRootEventCommitted]
          probe.send(drone, ChangeUserLastname(CommandHeader(), "a", 1, "müller"))
          probe.expectMsgType[CommandNotExecuted]
        }
      }
    }
  }

  private val currentTestId = new java.util.concurrent.atomic.AtomicInteger(1)
  def nextTestId = currentTestId.getAndIncrement()

  case class FixtureParam(testId: Int, drone: ActorRef, eventlog: ActorRef, probe: TestProbe, snapshotRepository: ActorRef)

  def withFixture(test: OneArgTest) = {
    import almhirt.aggregates._
    import almhirt.tracking.CommandStatusChanged
    import almhirt.streaming._

    val testId = nextTestId
    //info(s"Test $testId")
    val eventlogProps: Props = almhirt.eventlog.InMemoryAggregateRootEventLog.props()
    val eventlogActor: ActorRef = system.actorOf(eventlogProps, s"eventlog-$testId")
    val streams = AlmhirtStreams(s"almhirt-streams-$testId")(10 seconds).awaitResultOrEscalate(10 seconds)

    val snapshotRepository: ActorRef = system.actorOf(almhirt.snapshots.InMemorySnapshotRepository.propsRaw(false), s"snapshot-repo-$testId")

    val testProbe = TestProbe()
    val droneProps: Props = Props(
      new AggregateRootDrone[User, UserEvent] with ActorLogging with UserEventHandler with UserCommandHandler with UserUpdater with AggregateRootDroneCommandHandlerAdaptor[User, UserCommand, UserEvent] {
        def ccuad = AggregateRootDroneWithSnapshottingTests.this.ccuad
        val arTag = scala.reflect.ClassTag[User](classOf[User])
        val snapshotting = Some(SnapshottingForDrone(snapshotRepository, almhirt.snapshots.SnapshottingPolicy.AlwaysSnapshoot))
        def futuresContext: ExecutionContext = executionContext
        def aggregateEventLog: ActorRef = eventlogActor
        val eventsBroker: StreamBroker[Event] = streams.eventBroker
        val notifyHiveAboutUndispatchedEventsAfter: Option[FiniteDuration] = None
        val notifyHiveAboutUnstoredEventsAfterPerEvent: Option[FiniteDuration] = None
        def retryEventLogActionDelay: Option[FiniteDuration] = None
        val preStoreActionFor = (e: UserEvent) ⇒ PreStoreEventAction.NoAction
        val returnToUnitializedAfter = None

        override val aggregateCommandValidator = AggregateRootCommandValidator.Validated
        override val tag = scala.reflect.ClassTag[UserCommand](classOf[UserCommand])

        override def logError(msg: ⇒ String, cause: almhirt.problem.ProblemCause): Unit = { info(s"ERROR: $msg") }

        override def sendMessage(msg: AggregateRootHiveInternals.AggregateDroneMessage) {
          testProbe.ref ! msg
        }
      })

    val droneActor: ActorRef = system.actorOf(droneProps, s"drone-$testId")
    try {
      withFixture(test.toNoArgTest(FixtureParam(testId, droneActor, eventlogActor, testProbe, snapshotRepository)))
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