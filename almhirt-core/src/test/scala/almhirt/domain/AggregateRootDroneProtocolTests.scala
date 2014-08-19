package almhirt.domain

import scala.language.postfixOps
import org.joda.time.{ DateTime, LocalDateTime, DateTimeZone }
import scala.concurrent._
import scala.concurrent.duration._
import akka.actor._
import almhirt.common._
import akka.testkit._
import org.scalatest._

class AggregateRootDroneProtocolTests(_system: ActorSystem)
  extends TestKit(_system) with fixture.WordSpecLike with Matchers with BeforeAndAfterAll {
  def this() = this(ActorSystem("AggregateRootDroneProtocolTests", almhirt.TestConfigs.logWarningConfig))

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
    import almhirt.tracking._
    import aggregatesforthelazyones._
    "receiving valid commands" when {
      "an aggregate root is created" should {
        "emit the status events [CommandExecutionStarted, CommandSuccessfullyExecuted]" in { fixture =>
          val FixtureParam(testId, droneActor, droneProbe, eventsProbe, statusProbe) = fixture
          within(1 second) {
            droneProbe.send(droneActor, CreateUser(CommandHeader(), "a", 0L, "hans", "meier"))
            statusProbe.expectMsgType[CommandExecutionStarted](500 millis)
            statusProbe.expectMsgType[CommandSuccessfullyExecuted](500 millis)
          }
        }
        "emit the aggregate events [Created]" in { fixture =>
          val FixtureParam(testId, droneActor, droneProbe, eventsProbe, statusProbe) = fixture
          within(1 second) {
            droneProbe.send(droneActor, CreateUser(CommandHeader(), "a", 0L, "hans", "meier"))
            eventsProbe.expectMsgType[UserCreated](500 millis)
          }
        }
      }
      "an aggregate root is created and modified" should {
        "emit the status events [Start, Success, Start, Success]" in { fixture =>
          val FixtureParam(testId, droneActor, droneProbe, eventsProbe, statusProbe) = fixture
          within(1 second) {
            droneProbe.send(droneActor, CreateUser(CommandHeader(), "a", 0L, "hans", "meier"))
            droneProbe.expectMsgType[CommandExecuted]
            droneProbe.send(droneActor, ChangeUserAgeForCreditCard(CommandHeader(), "a", 1L, 22))
            statusProbe.expectMsgType[CommandExecutionStarted](500 millis)
            statusProbe.expectMsgType[CommandSuccessfullyExecuted](500 millis)
            statusProbe.expectMsgType[CommandExecutionStarted](500 millis)
            statusProbe.expectMsgType[CommandSuccessfullyExecuted](500 millis)
          }
        }
        "emit the aggregate events [Created, Modified]" in { fixture =>
          val FixtureParam(testId, droneActor, droneProbe, eventsProbe, statusProbe) = fixture
          within(1 second) {
            droneProbe.send(droneActor, CreateUser(CommandHeader(), "a", 0L, "hans", "meier"))
            droneProbe.expectMsgType[CommandExecuted]
            droneProbe.send(droneActor, ChangeUserAgeForCreditCard(CommandHeader(), "a", 1L, 22))
            eventsProbe.expectMsgType[UserCreated](500 millis)
            eventsProbe.expectMsgType[UserAgeChanged](500 millis)
          }
        }
      }
      "an aggregate root is created, modified and then deleted" should {
        "emit the status events [Start, Success, Start, Success, Start, Success]" in { fixture =>
          val FixtureParam(testId, droneActor, droneProbe, eventsProbe, statusProbe) = fixture
          within(1 second) {
            droneProbe.send(droneActor, CreateUser(CommandHeader(), "a", 0L, "hans", "meier"))
            droneProbe.expectMsgType[CommandExecuted]
            droneProbe.send(droneActor, ChangeUserAgeForCreditCard(CommandHeader(), "a", 1L, 22))
            droneProbe.expectMsgType[CommandExecuted]
            droneProbe.send(droneActor, ConfirmUserDeath(CommandHeader(), "a", 2L))
            statusProbe.expectMsgType[CommandExecutionStarted](500 millis)
            statusProbe.expectMsgType[CommandSuccessfullyExecuted](500 millis)
            statusProbe.expectMsgType[CommandExecutionStarted](500 millis)
            statusProbe.expectMsgType[CommandSuccessfullyExecuted](500 millis)
            statusProbe.expectMsgType[CommandExecutionStarted](500 millis)
            statusProbe.expectMsgType[CommandSuccessfullyExecuted](500 millis)
          }
        }
        "emit the aggregate events [Created, Modified, Deleted]" in { fixture =>
          val FixtureParam(testId, droneActor, droneProbe, eventsProbe, statusProbe) = fixture
          within(1 second) {
            droneProbe.send(droneActor, CreateUser(CommandHeader(), "a", 0L, "hans", "meier"))
            droneProbe.expectMsgType[CommandExecuted]
            droneProbe.send(droneActor, ChangeUserAgeForCreditCard(CommandHeader(), "a", 1L, 22))
            droneProbe.expectMsgType[CommandExecuted]
            droneProbe.send(droneActor, ConfirmUserDeath(CommandHeader(), "a", 2L))
            eventsProbe.expectMsgType[UserCreated](500 millis)
            eventsProbe.expectMsgType[UserAgeChanged](500 millis)
            eventsProbe.expectMsgType[UserDied](500 millis)
          }
        }
      }
      "a command does nothing" should {
        "emit the status events [Start, Success]" in { fixture =>
          val FixtureParam(testId, droneActor, droneProbe, eventsProbe, statusProbe) = fixture
          within(1 second) {
            droneProbe.send(droneActor, UserUow(CommandHeader(), "a", 0L, Seq.empty))
            statusProbe.expectMsgType[CommandExecutionStarted](500 millis)
            statusProbe.expectMsgType[CommandSuccessfullyExecuted](500 millis)
          }
        }
        "emit NO aggregate events" in { fixture =>
          val FixtureParam(testId, droneActor, droneProbe, eventsProbe, statusProbe) = fixture
          within(1 second) {
            droneProbe.send(droneActor, UserUow(CommandHeader(), "a", 0L, Seq.empty))
            eventsProbe.expectNoMsg(500 millis)
          }
        }
      }
      "an aggregate root is created, *Nothing*, modified and then deleted" should {
        "emit the status events [Start, Success, Start, Success, Start, Success, Start, Success]" in { fixture =>
          val FixtureParam(testId, droneActor, droneProbe, eventsProbe, statusProbe) = fixture
          within(1 second) {
            droneProbe.send(droneActor, CreateUser(CommandHeader(), "a", 0L, "hans", "meier"))
            droneProbe.expectMsgType[CommandExecuted]
            droneProbe.send(droneActor, ChangeUserAgeForCreditCard(CommandHeader(), "a", 1L, 22))
            droneProbe.expectMsgType[CommandExecuted]
            droneProbe.send(droneActor, UserUow(CommandHeader(), "a", 2L, Seq.empty))
            droneProbe.expectMsgType[CommandExecuted]
            droneProbe.send(droneActor, ConfirmUserDeath(CommandHeader(), "a", 2L))
            statusProbe.expectMsgType[CommandExecutionStarted](500 millis)
            statusProbe.expectMsgType[CommandSuccessfullyExecuted](500 millis)
            statusProbe.expectMsgType[CommandExecutionStarted](500 millis)
            statusProbe.expectMsgType[CommandSuccessfullyExecuted](500 millis)
            statusProbe.expectMsgType[CommandExecutionStarted](500 millis)
            statusProbe.expectMsgType[CommandSuccessfullyExecuted](500 millis)
            statusProbe.expectMsgType[CommandExecutionStarted](500 millis)
            statusProbe.expectMsgType[CommandSuccessfullyExecuted](500 millis)
          }
        }
        "emit the aggregate events [Created, Modified, Deleted]" in { fixture =>
          val FixtureParam(testId, droneActor, droneProbe, eventsProbe, statusProbe) = fixture
          within(1 second) {
            droneProbe.send(droneActor, CreateUser(CommandHeader(), "a", 0L, "hans", "meier"))
            droneProbe.expectMsgType[CommandExecuted]
            droneProbe.send(droneActor, ChangeUserAgeForCreditCard(CommandHeader(), "a", 1L, 22))
            droneProbe.expectMsgType[CommandExecuted]
            droneProbe.send(droneActor, UserUow(CommandHeader(), "a", 2L, Seq.empty))
            droneProbe.expectMsgType[CommandExecuted]
            droneProbe.send(droneActor, ConfirmUserDeath(CommandHeader(), "a", 2L))
            eventsProbe.expectMsgType[UserCreated](500 millis)
            eventsProbe.expectMsgType[UserAgeChanged](500 millis)
            eventsProbe.expectMsgType[UserDied](500 millis)
          }
        }
      }

    }
    "receiving invalid commands" when {
      "a non existing aggregate root is modified" should {
        "emit the status events [CommandExecutionStarted, CommandFailed]" in { fixture =>
          val FixtureParam(testId, droneActor, droneProbe, eventsProbe, statusProbe) = fixture
          within(1 second) {
            droneProbe.send(droneActor, ChangeUserLastname(CommandHeader(), "a", 0L, "meier"))
            statusProbe.expectMsgType[CommandExecutionStarted](500 millis)
            statusProbe.expectMsgType[CommandFailed](500 millis)
          }
        }
        "emit NO events" in { fixture =>
          val FixtureParam(testId, droneActor, droneProbe, eventsProbe, statusProbe) = fixture
          within(1 second) {
            droneProbe.send(droneActor, ChangeUserLastname(CommandHeader(), "a", 0L, "meier"))
            eventsProbe.expectNoMsg(500 millis)
          }
        }
      }
      "an aggregate root is created, modified(wrong version), modified(creates 2 events) and then deleted" should {
        "emit the status events [Start, Success, Start, Failed, Start, Success, Start, Success]" in { fixture =>
          val FixtureParam(testId, droneActor, droneProbe, eventsProbe, statusProbe) = fixture
          within(1 second) {
            droneProbe.send(droneActor, CreateUser(CommandHeader(), "a", 0L, "hans", "meier"))
            droneProbe.expectMsgType[CommandExecuted]
            droneProbe.send(droneActor, ChangeUserAgeForCreditCard(CommandHeader(), "a", 7L, 22))
            droneProbe.expectMsgType[CommandNotExecuted]
            droneProbe.send(droneActor, ChangeUserFullName(CommandHeader(), "a", 1L, "fritz", "weller"))
            droneProbe.expectMsgType[CommandExecuted]
            droneProbe.send(droneActor, ConfirmUserDeath(CommandHeader(), "a", 3L))
            statusProbe.expectMsgType[CommandExecutionStarted](500 millis)
            statusProbe.expectMsgType[CommandSuccessfullyExecuted](500 millis)
            statusProbe.expectMsgType[CommandExecutionStarted](500 millis)
            statusProbe.expectMsgType[CommandFailed](500 millis)
            statusProbe.expectMsgType[CommandExecutionStarted](500 millis)
            statusProbe.expectMsgType[CommandSuccessfullyExecuted](500 millis)
            statusProbe.expectMsgType[CommandExecutionStarted](500 millis)
            statusProbe.expectMsgType[CommandSuccessfullyExecuted](500 millis)
          }
        }
        "emit the aggregate events [Created, Modified(x2), Deleted]" in { fixture =>
          val FixtureParam(testId, droneActor, droneProbe, eventsProbe, statusProbe) = fixture
          within(1 second) {
            droneProbe.send(droneActor, CreateUser(CommandHeader(), "a", 0L, "hans", "meier"))
            droneProbe.expectMsgType[CommandExecuted]
            droneProbe.send(droneActor, ChangeUserAgeForCreditCard(CommandHeader(), "a", 7L, 22))
            droneProbe.expectMsgType[CommandNotExecuted]
            droneProbe.send(droneActor, ChangeUserFullName(CommandHeader(), "a", 1L, "fritz", "weller"))
            droneProbe.expectMsgType[CommandExecuted]
            droneProbe.send(droneActor, ConfirmUserDeath(CommandHeader(), "a", 3L))
            eventsProbe.expectMsgType[UserCreated](500 millis)
            eventsProbe.expectMsgType[UserSurnameChanged](500 millis)
            eventsProbe.expectMsgType[UserLastnameChanged](500 millis)
            eventsProbe.expectMsgType[UserDied](500 millis)
          }
        }
      }
    }
  }

  private val currentTestId = new java.util.concurrent.atomic.AtomicInteger(1)
  def nextTestId = currentTestId.getAndIncrement()

  case class FixtureParam(testId: Int, drone: ActorRef, droneProbe: TestProbe, eventsProbe: TestProbe, statusProbe: TestProbe)

  def withFixture(test: OneArgTest) = {
    import almhirt.aggregates._
    import almhirt.tracking.CommandStatusChanged
    import almhirt.streaming.{ SequentialPostOfficeClient, FireAndForgetSink, PostOffice, PostOfficeClientSettings }
    val testId = nextTestId
    //info(s"Test $testId")
    val eventlogProps: Props = almhirt.eventlog.InMemoryAggregateEventLog.props()
    val eventlogActor: ActorRef = system.actorOf(eventlogProps, s"eventlog-$testId")
    val droneProbe = TestProbe()
    val eventsProbe = TestProbe()
    val statusProbe = TestProbe()
    val droneProps: Props = Props(
      new Actor with ActorLogging with AggregateRootDrone[User, UserEvent] with UserEventHandler with UserCommandHandler with UserUpdater with AggregateRootDroneCommandHandlerAdaptor[User, UserEvent] with SequentialPostOfficeClient {
        def ccuad = AggregateRootDroneProtocolTests.this.ccuad
        def futuresContext: ExecutionContext = executionContext
        def aggregateEventLog: ActorRef = eventlogActor
        def snapshotStorage: Option[ActorRef] = None
        val commandStatusSink = FireAndForgetSink.delegating[CommandStatusChanged](statusProbe.ref)
        val postOfficeSettings = PostOfficeClientSettings(100, 1 second, 10)
        val eventsPostOffice = PostOffice.faked[Event](eventsProbe.ref)

        override def sendMessage(msg: AggregateRootDroneInternalMessages.AggregateDroneMessage) {
          droneProbe.ref ! msg
        }

      })

    val droneActor: ActorRef = system.actorOf(droneProps, s"drone-$testId")
    try {
      withFixture(test.toNoArgTest(FixtureParam(testId, droneActor, droneProbe, eventsProbe, statusProbe)))
    } finally {
      system.stop(droneActor)
      system.stop(eventlogActor)
    }
  }

}