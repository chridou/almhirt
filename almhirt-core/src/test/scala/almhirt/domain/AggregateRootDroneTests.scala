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
          within(2 seconds) {
            probe.send(drone, CreateUser(CommandHeader(), "a", 0L, "hans", "meier"))
            probe.expectMsgType[CommandExecuted]
          }
        }
        "execute a create command and store the event" in { fixture ⇒
          val FixtureParam(testId, drone, eventlog, probe) = fixture
          within(2 seconds) {
            probe.send(drone, CreateUser(CommandHeader(), "a", 0L, "hans", "meier"))
            probe.expectMsgType[CommandExecuted]

            probe.send(eventlog, GetAllAggregateEventsFor("a"))
            val eventsEnumerator = probe.expectMsgType[FetchedAggregateEvents].enumerator
            val iteratee = Iteratee.fold[AggregateEvent, Vector[AggregateEvent]](Vector.empty) { case (acc, cur) => acc :+ cur }
            val events: Vector[AggregateEvent] = Await.result(eventsEnumerator.run(iteratee), 100.millis.dilated)
            events should equal(List(UserCreated(EventHeader(), "a", 0, "hans", "meier")))
          }
        }

        "execute a create command and then a modifying command" in { fixture ⇒
          val FixtureParam(testId, drone, eventlog, probe) = fixture
          within(2 seconds) {
            probe.send(drone, CreateUser(CommandHeader(), "a", 0L, "hans", "meier"))
            probe.expectMsgType[CommandExecuted]
            probe.send(drone, ChangeUserAgeForCreditCard(CommandHeader(), "a", 1L, 22))
            probe.expectMsgType[CommandExecuted]
          }
        }
        "execute a create command and then a modifying command and store the events" in { fixture ⇒
          val FixtureParam(testId, drone, eventlog, probe) = fixture
          within(2 seconds) {
            probe.send(drone, CreateUser(CommandHeader(), "a", 0L, "hans", "meier"))
            probe.expectMsgType[CommandExecuted]
            probe.send(drone, ChangeUserAgeForCreditCard(CommandHeader(), "a", 1L, 22))
            probe.expectMsgType[CommandExecuted]

            probe.send(eventlog, GetAllAggregateEventsFor("a"))
            val eventsEnumerator = probe.expectMsgType[FetchedAggregateEvents].enumerator
            val iteratee = Iteratee.fold[AggregateEvent, Vector[AggregateEvent]](Vector.empty) { case (acc, cur) => acc :+ cur }
            val events: Vector[AggregateEvent] = Await.result(eventsEnumerator.run(iteratee), 100.millis.dilated)
            events should equal(List(
              UserCreated(EventHeader(), "a", 0, "hans", "meier"),
              UserAgeChanged(EventHeader(), "a", 1, 22)))
          }
        }

        "execute a create command, then a modifying and then a deleting command" in { fixture ⇒
          val FixtureParam(testId, drone, eventlog, probe) = fixture
          within(2 seconds) {
            probe.send(drone, CreateUser(CommandHeader(), "a", 0L, "hans", "meier"))
            probe.expectMsgType[CommandExecuted]
            probe.send(drone, ChangeUserAgeForCreditCard(CommandHeader(), "a", 1L, 22))
            probe.expectMsgType[CommandExecuted]
            probe.send(drone, ConfirmUserDeath(CommandHeader(), "a", 2L))
            probe.expectMsgType[CommandExecuted]
          }
        }
        "execute a create command, then a modifying command and then a deleting command and store the events" in { fixture ⇒
          val FixtureParam(testId, drone, eventlog, probe) = fixture
          within(2 seconds) {
            probe.send(drone, CreateUser(CommandHeader(), "a", 0L, "hans", "meier"))
            probe.expectMsgType[CommandExecuted]
            probe.send(drone, ChangeUserAgeForCreditCard(CommandHeader(), "a", 1L, 22))
            probe.expectMsgType[CommandExecuted]
            probe.send(drone, ConfirmUserDeath(CommandHeader(), "a", 2L))
            probe.expectMsgType[CommandExecuted]

            probe.send(eventlog, GetAllAggregateEventsFor("a"))
            val eventsEnumerator = probe.expectMsgType[FetchedAggregateEvents].enumerator
            val iteratee = Iteratee.fold[AggregateEvent, Vector[AggregateEvent]](Vector.empty) { case (acc, cur) => acc :+ cur }
            val events: Vector[AggregateEvent] = Await.result(eventsEnumerator.run(iteratee), 100.millis.dilated)
            events should equal(List(
              UserCreated(EventHeader(), "a", 0, "hans", "meier"),
              UserAgeChanged(EventHeader(), "a", 1, 22),
              UserDied(EventHeader(), "a", 2)))
          }
        }

        "execute a command that deletes the aggregate root right away" in { fixture ⇒
          val FixtureParam(testId, drone, eventlog, probe) = fixture
          within(2 seconds) {
            probe.send(drone, RejectUser(CommandHeader(), "a", 0L, "hans", "meier"))
            probe.expectMsgType[CommandExecuted]
          }
        }
        "execute a command that deletes the aggregate root right away and store the event" in { fixture ⇒
          val FixtureParam(testId, drone, eventlog, probe) = fixture
          within(2 seconds) {
            probe.send(drone, RejectUser(CommandHeader(), "a", 0L, "hans", "meier"))
            probe.expectMsgType[CommandExecuted]

            probe.send(eventlog, GetAllAggregateEventsFor("a"))
            val eventsEnumerator = probe.expectMsgType[FetchedAggregateEvents].enumerator
            val iteratee = Iteratee.fold[AggregateEvent, Vector[AggregateEvent]](Vector.empty) { case (acc, cur) => acc :+ cur }
            val events: Vector[AggregateEvent] = Await.result(eventsEnumerator.run(iteratee), 100.millis.dilated)
            events should equal(List(UserNotAccepted(EventHeader(), "a", 0, "hans", "meier")))
          }
        }

        "execute a create command and then a command that does nothing" in { fixture ⇒
          val FixtureParam(testId, drone, eventlog, probe) = fixture
          within(2 seconds) {
            probe.send(drone, CreateUser(CommandHeader(), "a", 0L, "hans", "meier"))
            probe.expectMsgType[CommandExecuted]
            probe.send(drone, UserUow(CommandHeader(), "a", 1L, Seq.empty))
            probe.expectMsgType[CommandExecuted]
          }
        }
        "execute a create command and then a command that does nothing and store the (create) event" in { fixture ⇒
          val FixtureParam(testId, drone, eventlog, probe) = fixture
          within(2 seconds) {
            probe.send(drone, CreateUser(CommandHeader(), "a", 0L, "hans", "meier"))
            probe.expectMsgType[CommandExecuted]
            probe.send(drone, UserUow(CommandHeader(), "a", 1L, Seq.empty))
            probe.expectMsgType[CommandExecuted]

            probe.send(eventlog, GetAllAggregateEventsFor("a"))
            val eventsEnumerator = probe.expectMsgType[FetchedAggregateEvents].enumerator
            val iteratee = Iteratee.fold[AggregateEvent, Vector[AggregateEvent]](Vector.empty) { case (acc, cur) => acc :+ cur }
            val events: Vector[AggregateEvent] = Await.result(eventsEnumerator.run(iteratee), 100.millis.dilated)
            events should equal(List(UserCreated(EventHeader(), "a", 0, "hans", "meier")))
          }
        }
      }
      "an aggregate root with version 2 exists" should {
        def createAr(eventlog: ActorRef, probe: TestProbe) {
          probe.send(eventlog, CommitAggregateEvent(UserCreated(EventHeader(), "a", 0, "hans", "meier")))
          probe.expectMsgType[AggregateEventCommitted]
          probe.send(eventlog, CommitAggregateEvent(UserSurnameChanged(EventHeader(), "a", 1, "peter")))
          probe.expectMsgType[AggregateEventCommitted]
        }

        "execute a modifying command" in { fixture ⇒
          val FixtureParam(testId, drone, eventlog, probe) = fixture
          within(2 seconds) {
            createAr(eventlog, probe)
            probe.send(drone, ChangeUserLastname(CommandHeader(), "a", 2L, "müller"))
            probe.expectMsgType[CommandExecuted]
          }
        }
        "execute a modifying command and store the event" in { fixture ⇒
          val FixtureParam(testId, drone, eventlog, probe) = fixture
          within(2 seconds) {
            createAr(eventlog, probe)
            probe.send(drone, ChangeUserLastname(CommandHeader(), "a", 2, "müller"))
            probe.expectMsgType[CommandExecuted]

            probe.send(eventlog, GetAggregateEventsFrom("a", 2))
            val eventsEnumerator = probe.expectMsgType[FetchedAggregateEvents].enumerator
            val iteratee = Iteratee.fold[AggregateEvent, Vector[AggregateEvent]](Vector.empty) { case (acc, cur) => acc :+ cur }
            val events: Vector[AggregateEvent] = Await.result(eventsEnumerator.run(iteratee), 100.millis.dilated)
            events should equal(List(UserLastnameChanged(EventHeader(), "a", 2, "müller")))
          }
        }

        "execute a deleting command" in { fixture ⇒
          val FixtureParam(testId, drone, eventlog, probe) = fixture
          within(2 seconds) {
            createAr(eventlog, probe)
            probe.send(drone, ConfirmUserDeath(CommandHeader(), "a", 2L))
            probe.expectMsgType[CommandExecuted]
          }
        }
        "execute a deleting command and store the event" in { fixture ⇒
          val FixtureParam(testId, drone, eventlog, probe) = fixture
          within(2 seconds) {
            createAr(eventlog, probe)
            probe.send(drone, ConfirmUserDeath(CommandHeader(), "a", 2L))
            probe.expectMsgType[CommandExecuted]

            probe.send(eventlog, GetAggregateEventsFrom("a", 2))
            val eventsEnumerator = probe.expectMsgType[FetchedAggregateEvents].enumerator
            val iteratee = Iteratee.fold[AggregateEvent, Vector[AggregateEvent]](Vector.empty) { case (acc, cur) => acc :+ cur }
            val events: Vector[AggregateEvent] = Await.result(eventsEnumerator.run(iteratee), 100.millis.dilated)
            events should equal(List(UserDied(EventHeader(), "a", 2)))
          }
        }
      }
    }
    "receiving invalid commands" when {
      "no aggregate root exists" should {
        "not execute a modifying command" in { fixture ⇒
          val FixtureParam(testId, drone, eventlog, probe) = fixture
          within(2 seconds) {
            probe.send(drone, ChangeUserLastname(CommandHeader(), "a", 0, "müller"))
            probe.expectMsgType[CommandNotExecuted]
          }
        }
        "not execute a modifying and store no event" in { fixture ⇒
          val FixtureParam(testId, drone, eventlog, probe) = fixture
          within(2 seconds) {
            probe.send(drone, ChangeUserLastname(CommandHeader(), "a", 0, "müller"))
            probe.expectMsgType[CommandNotExecuted]

            probe.send(eventlog, GetAllAggregateEventsFor("a"))
            val eventsEnumerator = probe.expectMsgType[FetchedAggregateEvents].enumerator
            val iteratee = Iteratee.fold[AggregateEvent, Vector[AggregateEvent]](Vector.empty) { case (acc, cur) => acc :+ cur }
            val events: Vector[AggregateEvent] = Await.result(eventsEnumerator.run(iteratee), 100.millis.dilated)
            events should equal(List())
          }
        }
        "not execute a deleting command" in { fixture ⇒
          val FixtureParam(testId, drone, eventlog, probe) = fixture
          within(2 seconds) {
            probe.send(drone, ConfirmUserDeath(CommandHeader(), "a", 0))
            probe.expectMsgType[CommandNotExecuted]
          }
        }
        "not execute a deleting and store no event" in { fixture ⇒
          val FixtureParam(testId, drone, eventlog, probe) = fixture
          within(2 seconds) {
            probe.send(drone, ConfirmUserDeath(CommandHeader(), "a", 0))
            probe.expectMsgType[CommandNotExecuted]

            probe.send(eventlog, GetAllAggregateEventsFor("a"))
            val eventsEnumerator = probe.expectMsgType[FetchedAggregateEvents].enumerator
            val iteratee = Iteratee.fold[AggregateEvent, Vector[AggregateEvent]](Vector.empty) { case (acc, cur) => acc :+ cur }
            val events: Vector[AggregateEvent] = Await.result(eventsEnumerator.run(iteratee), 100.millis.dilated)
            events should equal(List())
          }
        }
      }
      "an aggregate root with version 2 exists" should {
        def createAr(eventlog: ActorRef, probe: TestProbe) {
          probe.send(eventlog, CommitAggregateEvent(UserCreated(EventHeader(), "a", 0, "hans", "meier")))
          probe.expectMsgType[AggregateEventCommitted]
          probe.send(eventlog, CommitAggregateEvent(UserSurnameChanged(EventHeader(), "a", 1, "peter")))
          probe.expectMsgType[AggregateEventCommitted]
        }

        "not execute a creating command that targets version 0" in { fixture ⇒
          val FixtureParam(testId, drone, eventlog, probe) = fixture
          within(2 seconds) {
            createAr(eventlog, probe)
            probe.send(drone, CreateUser(CommandHeader(), "a", 0, "hans", "meier"))
            probe.expectMsgType[CommandNotExecuted]
          }
        }
        "not execute a creating command that targets version 0 and store no event" in { fixture ⇒
          val FixtureParam(testId, drone, eventlog, probe) = fixture
          within(2 seconds) {
            createAr(eventlog, probe)
            probe.send(drone, CreateUser(CommandHeader(), "a", 0, "hans", "meier"))
            probe.expectMsgType[CommandNotExecuted]

            probe.send(eventlog, GetAggregateEventsFrom("a", 2))
            val eventsEnumerator = probe.expectMsgType[FetchedAggregateEvents].enumerator
            val iteratee = Iteratee.fold[AggregateEvent, Vector[AggregateEvent]](Vector.empty) { case (acc, cur) => acc :+ cur }
            val events: Vector[AggregateEvent] = Await.result(eventsEnumerator.run(iteratee), 100.millis.dilated)
            events should equal(List())
          }
        }

        "not execute a creating command that targets version 2" in { fixture ⇒
          val FixtureParam(testId, drone, eventlog, probe) = fixture
          within(2 seconds) {
            createAr(eventlog, probe)
            probe.send(drone, CreateUser(CommandHeader(), "a", 2, "hans", "meier"))
            probe.expectMsgType[CommandNotExecuted]
          }
        }
        "not execute a creating command that targets version 2 and store no event" in { fixture ⇒
          val FixtureParam(testId, drone, eventlog, probe) = fixture
          within(2 seconds) {
            createAr(eventlog, probe)
            probe.send(drone, CreateUser(CommandHeader(), "a", 2, "hans", "meier"))
            probe.expectMsgType[CommandNotExecuted]

            probe.send(eventlog, GetAggregateEventsFrom("a", 2))
            val eventsEnumerator = probe.expectMsgType[FetchedAggregateEvents].enumerator
            val iteratee = Iteratee.fold[AggregateEvent, Vector[AggregateEvent]](Vector.empty) { case (acc, cur) => acc :+ cur }
            val events: Vector[AggregateEvent] = Await.result(eventsEnumerator.run(iteratee), 100.millis.dilated)
            events should equal(List())
          }
        }
        "not execute an invalid command but afterwards a valid command" in { fixture ⇒
          val FixtureParam(testId, drone, eventlog, probe) = fixture
          within(2 seconds) {
            createAr(eventlog, probe)
            probe.send(drone, CreateUser(CommandHeader(), "a", 2, "hans", "meier"))
            probe.expectMsgType[CommandNotExecuted]
            probe.send(drone, ChangeUserLastname(CommandHeader(), "a", 2, "müller"))
            probe.expectMsgType[CommandExecuted]
          }
        }
        "not execute an invalid command but afterwards a valid command and store the event" in { fixture ⇒
          val FixtureParam(testId, drone, eventlog, probe) = fixture
          within(2 seconds) {
            createAr(eventlog, probe)
            probe.send(drone, CreateUser(CommandHeader(), "a", 2, "hans", "meier"))
            probe.expectMsgType[CommandNotExecuted]
            probe.send(drone, ChangeUserLastname(CommandHeader(), "a", 2, "müller"))
            probe.expectMsgType[CommandExecuted]

            probe.send(eventlog, GetAggregateEventsFrom("a", 2))
            val eventsEnumerator = probe.expectMsgType[FetchedAggregateEvents].enumerator
            val iteratee = Iteratee.fold[AggregateEvent, Vector[AggregateEvent]](Vector.empty) { case (acc, cur) => acc :+ cur }
            val events: Vector[AggregateEvent] = Await.result(eventsEnumerator.run(iteratee), 100.millis.dilated)
            events should equal(List(UserLastnameChanged(EventHeader(), "a", 2, "müller")))
          }
        }
      }
      "a dead aggregate root with version 3 exists" should {
        def createAr(eventlog: ActorRef, probe: TestProbe) {
          probe.send(eventlog, CommitAggregateEvent(UserCreated(EventHeader(), "a", 0, "hans", "meier")))
          probe.expectMsgType[AggregateEventCommitted]
          probe.send(eventlog, CommitAggregateEvent(UserSurnameChanged(EventHeader(), "a", 1, "peter")))
          probe.expectMsgType[AggregateEventCommitted]
          probe.send(eventlog, CommitAggregateEvent(UserDied(EventHeader(), "a", 2)))
          probe.expectMsgType[AggregateEventCommitted]
        }

        "not execute a creating command that targets version 0" in { fixture ⇒
          val FixtureParam(testId, drone, eventlog, probe) = fixture
          within(2 seconds) {
            createAr(eventlog, probe)
            probe.send(drone, CreateUser(CommandHeader(), "a", 0, "hans", "meier"))
            probe.expectMsgType[CommandNotExecuted]
          }
        }
        "not execute a creating command that targets version 0 and store no event" in { fixture ⇒
          val FixtureParam(testId, drone, eventlog, probe) = fixture
          within(2 seconds) {
            createAr(eventlog, probe)
            probe.send(drone, CreateUser(CommandHeader(), "a", 0, "hans", "meier"))
            probe.expectMsgType[CommandNotExecuted]

            probe.send(eventlog, GetAggregateEventsFrom("a", 3))
            val eventsEnumerator = probe.expectMsgType[FetchedAggregateEvents].enumerator
            val iteratee = Iteratee.fold[AggregateEvent, Vector[AggregateEvent]](Vector.empty) { case (acc, cur) => acc :+ cur }
            val events: Vector[AggregateEvent] = Await.result(eventsEnumerator.run(iteratee), 100.millis.dilated)
            events should equal(List())
          }
        }

        "not execute a creating command that targets version 3" in { fixture ⇒
          val FixtureParam(testId, drone, eventlog, probe) = fixture
          within(2 seconds) {
            createAr(eventlog, probe)
            probe.send(drone, CreateUser(CommandHeader(), "a", 3, "hans", "meier"))
            probe.expectMsgType[CommandNotExecuted]
          }
        }
        "not execute a creating command that targets version 3 and store no event" in { fixture ⇒
          val FixtureParam(testId, drone, eventlog, probe) = fixture
          within(2 seconds) {
            createAr(eventlog, probe)
            probe.send(drone, CreateUser(CommandHeader(), "a", 3, "hans", "meier"))
            probe.expectMsgType[CommandNotExecuted]

            probe.send(eventlog, GetAggregateEventsFrom("a", 3))
            val eventsEnumerator = probe.expectMsgType[FetchedAggregateEvents].enumerator
            val iteratee = Iteratee.fold[AggregateEvent, Vector[AggregateEvent]](Vector.empty) { case (acc, cur) => acc :+ cur }
            val events: Vector[AggregateEvent] = Await.result(eventsEnumerator.run(iteratee), 100.millis.dilated)
            events should equal(List())
          }
        }

        "not execute a mutating command that targets version 3" in { fixture ⇒
          val FixtureParam(testId, drone, eventlog, probe) = fixture
          within(2 seconds) {
            createAr(eventlog, probe)
            probe.send(drone, ChangeUserLastname(CommandHeader(), "a", 3, "müller"))
            probe.expectMsgType[CommandNotExecuted]
          }
        }
        "not execute a mutating command that targets version 3 and store no event" in { fixture ⇒
          val FixtureParam(testId, drone, eventlog, probe) = fixture
          within(2 seconds) {
            createAr(eventlog, probe)
            probe.send(drone, ChangeUserLastname(CommandHeader(), "a", 3, "müller"))
            probe.expectMsgType[CommandNotExecuted]

            probe.send(eventlog, GetAggregateEventsFrom("a", 3))
            val eventsEnumerator = probe.expectMsgType[FetchedAggregateEvents].enumerator
            val iteratee = Iteratee.fold[AggregateEvent, Vector[AggregateEvent]](Vector.empty) { case (acc, cur) => acc :+ cur }
            val events: Vector[AggregateEvent] = Await.result(eventsEnumerator.run(iteratee), 100.millis.dilated)
            events should equal(List())
          }
        }

        "not execute a deleting command that targets version 3" in { fixture ⇒
          val FixtureParam(testId, drone, eventlog, probe) = fixture
          within(2 seconds) {
            createAr(eventlog, probe)
            probe.send(drone, ConfirmUserDeath(CommandHeader(), "a", 3))
            probe.expectMsgType[CommandNotExecuted]
          }
        }
        "not execute a deleting command that targets version 3 and store no event" in { fixture ⇒
          val FixtureParam(testId, drone, eventlog, probe) = fixture
          within(2 seconds) {
            createAr(eventlog, probe)
            probe.send(drone, ConfirmUserDeath(CommandHeader(), "a", 3))
            probe.expectMsgType[CommandNotExecuted]

            probe.send(eventlog, GetAggregateEventsFrom("a", 3))
            val eventsEnumerator = probe.expectMsgType[FetchedAggregateEvents].enumerator
            val iteratee = Iteratee.fold[AggregateEvent, Vector[AggregateEvent]](Vector.empty) { case (acc, cur) => acc :+ cur }
            val events: Vector[AggregateEvent] = Await.result(eventsEnumerator.run(iteratee), 100.millis.dilated)
            events should equal(List())
          }
        }

      }
    }
    "the eventlog is corrupted" should {
      "crash when receiving an event" in { fixture ⇒
        val FixtureParam(testId, drone, eventlog, probe) = fixture
        within(2 seconds) {
          probe.send(eventlog, CommitAggregateEvent(UserSurnameChanged(EventHeader(), "a", 0, "peter")))
          probe.expectMsgType[AggregateEventCommitted]
          probe.send(drone, ChangeUserLastname(CommandHeader(), "a", 1, "müller"))
          probe.expectMsgType[CommandNotExecuted]
        }
      }
    }
  }

  private val currentTestId = new java.util.concurrent.atomic.AtomicInteger(1)
  def nextTestId = currentTestId.getAndIncrement()

  case class FixtureParam(testId: Int, drone: ActorRef, eventlog: ActorRef, probe: TestProbe)

  def withFixture(test: OneArgTest) = {
    import almhirt.aggregates._
    import almhirt.tracking.CommandStatusChanged
    import almhirt.streaming.{ SequentialPostOfficeClient, PostOffice, PostOfficeClientSettings }
    val testId = nextTestId
    //info(s"Test $testId")
    val eventlogProps: Props = almhirt.eventlog.InMemoryAggregateEventLog.props()
    val eventlogActor: ActorRef = system.actorOf(eventlogProps, s"eventlog-$testId")
    val testProbe = TestProbe()
    val droneProps: Props = Props(
      new Actor with ActorLogging with AggregateRootDrone[User, UserEvent] with UserEventHandler with UserCommandHandler with UserUpdater with AggregateRootDroneCommandHandlerAdaptor[User, UserEvent] with SequentialPostOfficeClient {
        def ccuad = AggregateRootDroneTests.this.ccuad
        def futuresContext: ExecutionContext = executionContext
        def aggregateEventLog: ActorRef = eventlogActor
        def snapshotStorage: Option[ActorRef] = None
        val commandStatusSink = FireAndForgetSink.devNull[CommandStatusChanged]
        val postOfficeSettings = PostOfficeClientSettings(100, 1 second, 10)
        val eventsPostOffice = PostOffice.devNull[Event]

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