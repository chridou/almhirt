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
            statusProbe.expectMsgAllClassOf[CommandStatusChanged](500 millis, classOf[CommandExecutionStarted], classOf[CommandSuccessfullyExecuted])
          }
        }
        "emit the aggregate events [Created]" in { fixture =>
          val FixtureParam(testId, droneActor, droneProbe, eventsProbe, statusProbe) = fixture
          within(1 second) {
            droneProbe.send(droneActor, CreateUser(CommandHeader(), "a", 0L, "hans", "meier"))
            eventsProbe.expectMsgAllClassOf[AggregateEvent](500 millis, classOf[UserCreated])
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
            statusProbe.expectMsgAllClassOf[CommandStatusChanged](500 millis, 
                classOf[CommandExecutionStarted], classOf[CommandSuccessfullyExecuted],
                classOf[CommandExecutionStarted], classOf[CommandSuccessfullyExecuted])
          }
        }
        "emit the aggregate events [Created, Modified]" in { fixture =>
          val FixtureParam(testId, droneActor, droneProbe, eventsProbe, statusProbe) = fixture
          within(1 second) {
            droneProbe.send(droneActor, CreateUser(CommandHeader(), "a", 0L, "hans", "meier"))
            droneProbe.expectMsgType[CommandExecuted]
            droneProbe.send(droneActor, ChangeUserAgeForCreditCard(CommandHeader(), "a", 1L, 22))
            eventsProbe.expectMsgAllClassOf[AggregateEvent](500 millis, 
                classOf[UserCreated],
                classOf[UserAgeChanged])
          }
        }
      }
       "an aggregate root is created, modified and the deleted" should {
        "emit the status events [Start, Success, Start, Success, Start, Success]" in { fixture =>
          val FixtureParam(testId, droneActor, droneProbe, eventsProbe, statusProbe) = fixture
          within(1 second) {
            droneProbe.send(droneActor, CreateUser(CommandHeader(), "a", 0L, "hans", "meier"))
            droneProbe.expectMsgType[CommandExecuted]
            droneProbe.send(droneActor, ChangeUserAgeForCreditCard(CommandHeader(), "a", 1L, 22))
            droneProbe.expectMsgType[CommandExecuted]
            droneProbe.send(droneActor, ConfirmUserDeath(CommandHeader(), "a", 2L))
            statusProbe.expectMsgAllClassOf[CommandStatusChanged](500 millis, 
                classOf[CommandExecutionStarted], classOf[CommandSuccessfullyExecuted],
                classOf[CommandExecutionStarted], classOf[CommandSuccessfullyExecuted],
                classOf[CommandExecutionStarted], classOf[CommandSuccessfullyExecuted])
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
            eventsProbe.expectMsgAllClassOf[AggregateEvent](500 millis, 
                classOf[UserCreated],
                classOf[UserAgeChanged],
                classOf[UserDied])
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