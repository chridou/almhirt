package almhirt.domain

import scala.language.postfixOps
import org.joda.time.{ DateTime, LocalDateTime, DateTimeZone }
import scala.concurrent._
import scala.concurrent.duration._
import akka.actor._
import almhirt.common._
import almhirt.streaming._
import akka.stream.scaladsl._
import akka.testkit._
import org.scalatest._
import akka.stream.FlowMaterializer

class AggregateRootDroneProtocolTests(_system: ActorSystem)
  extends TestKit(_system) with fixture.WordSpecLike with Matchers with BeforeAndAfterAll {
  def this() = this(ActorSystem("AggregateRootDroneProtocolTests", almhirt.TestConfigs.logWarningConfig))

  implicit val mat = akka.stream.ActorFlowMaterializer()

  implicit val executionContext = system.dispatchers.defaultGlobalDispatcher
  implicit val ccuad = {
    val dt = new LocalDateTime(0L)
    new CanCreateUuidsAndDateTimes {
      override def getUuid(): java.util.UUID = ???
      override def getUniqueString(): String = "unique"
      override def getDateTime(): DateTime = ???
      override def getUtcTimestamp(): LocalDateTime = dt
    }
  }

  "The AggregateRootDrone" when {
    import almhirt.eventlog.AggregateRootEventLog._
    import AggregateRootDroneInternalMessages._
    import almhirt.aggregates._
    import almhirt.tracking._
    import aggregatesforthelazyones._
    "receiving valid commands" when {
      "an aggregate root is created" should {
        "emit the aggregate events [Created]" in { fixture ⇒
          val FixtureParam(testId, droneActor, droneProbe, streams) = fixture
          val eventsProbe = TestProbe()
          Source(streams.eventStream).collect { case e: AggregateRootEvent ⇒ e }.to(Sink(DelegatingSubscriber[AggregateRootEvent](eventsProbe.ref))).run()
          within(1 second) {
            droneProbe.send(droneActor, CreateUser(CommandHeader(), "a", 0L, "hans", "meier"))
            eventsProbe.expectMsgType[UserCreated]
          }
        }
      }
      "an aggregate root is created and modified" should {
        "emit the aggregate events [Created, Modified]" in { fixture ⇒
          val FixtureParam(testId, droneActor, droneProbe, streams) = fixture
          val eventsProbe = TestProbe()
          Source(streams.eventStream).collect { case e: AggregateRootEvent ⇒ e }.to(Sink(DelegatingSubscriber[AggregateRootEvent](eventsProbe.ref))).run()
          within(1 second) {
            droneProbe.send(droneActor, CreateUser(CommandHeader(), "a", 0L, "hans", "meier"))
            droneProbe.expectMsgType[CommandExecuted]
            droneProbe.send(droneActor, ChangeUserAgeForCreditCard(CommandHeader(), "a", 1L, 22))
            eventsProbe.expectMsgType[UserCreated]
            eventsProbe.expectMsgType[UserAgeChanged]
          }
        }
      }
      "an aggregate root is created, modified and then deleted" should {
        "emit the aggregate events [Created, Modified, Deleted]" in { fixture ⇒
          val FixtureParam(testId, droneActor, droneProbe, streams) = fixture
          val eventsProbe = TestProbe()
          Source(streams.eventStream).collect { case e: AggregateRootEvent ⇒ e }.to(Sink(DelegatingSubscriber[AggregateRootEvent](eventsProbe.ref))).run()
          within(1 second) {
            droneProbe.send(droneActor, CreateUser(CommandHeader(), "a", 0L, "hans", "meier"))
            droneProbe.expectMsgType[CommandExecuted]
            droneProbe.send(droneActor, ChangeUserAgeForCreditCard(CommandHeader(), "a", 1L, 22))
            droneProbe.expectMsgType[CommandExecuted]
            droneProbe.send(droneActor, ConfirmUserDeath(CommandHeader(), "a", 2L))
            eventsProbe.expectMsgType[UserCreated]
            eventsProbe.expectMsgType[UserAgeChanged]
            eventsProbe.expectMsgType[UserDied]
          }
        }
      }
      "a command does nothing" should {
        "emit NO aggregate events" in { fixture ⇒
          val FixtureParam(testId, droneActor, droneProbe, streams) = fixture
          val eventsProbe = TestProbe()
          Source(streams.eventStream).collect{case e: AggregateRootEvent ⇒ e}.to(Sink(DelegatingSubscriber[AggregateRootEvent](eventsProbe.ref))).run()
          within(1 second) {
            droneProbe.send(droneActor, UserUow(CommandHeader(), "a", 0L, Seq.empty))
            eventsProbe.expectNoMsg(500 millis)
          }
        }
      }
      "an aggregate root is created, *Nothing*, modified and then deleted" should {
        "emit the aggregate events [Created, Modified, Deleted]" in { fixture ⇒
          val FixtureParam(testId, droneActor, droneProbe, streams) = fixture
          val eventsProbe = TestProbe()
          Source(streams.eventStream).collect { case e: AggregateRootEvent ⇒ e }.to(Sink(DelegatingSubscriber[AggregateRootEvent](eventsProbe.ref))).run()
          within(1 second) {
            droneProbe.send(droneActor, CreateUser(CommandHeader(), "a", 0L, "hans", "meier"))
            droneProbe.expectMsgType[CommandExecuted]
            droneProbe.send(droneActor, ChangeUserAgeForCreditCard(CommandHeader(), "a", 1L, 22))
            droneProbe.expectMsgType[CommandExecuted]
            droneProbe.send(droneActor, UserUow(CommandHeader(), "a", 2L, Seq.empty))
            droneProbe.expectMsgType[CommandExecuted]
            droneProbe.send(droneActor, ConfirmUserDeath(CommandHeader(), "a", 2L))
            eventsProbe.expectMsgType[UserCreated]
            eventsProbe.expectMsgType[UserAgeChanged]
            eventsProbe.expectMsgType[UserDied]
          }
        }
      }

    }
    "receiving invalid commands" when {
      "a non existing aggregate root is modified" should {
        "emit NO events" in { fixture ⇒
          val FixtureParam(testId, droneActor, droneProbe, streams) = fixture
          val eventsProbe = TestProbe()
          Source(streams.eventStream).collect { case e: AggregateRootEvent ⇒ e }.to(Sink(DelegatingSubscriber[AggregateRootEvent](eventsProbe.ref))).run()
          within(1 second) {
            droneProbe.send(droneActor, ChangeUserLastname(CommandHeader(), "a", 0L, "meier"))
            eventsProbe.expectNoMsg(500 millis)
          }
        }
      }
      "an aggregate root is created, modified(wrong version), modified(creates 2 events) and then deleted" should {
        "emit the aggregate events [Created, Modified(x2), Deleted]" in { fixture ⇒
          val FixtureParam(testId, droneActor, droneProbe, streams) = fixture
          val eventsProbe = TestProbe()
          Source(streams.eventStream).collect { case e: AggregateRootEvent ⇒ e }.to(Sink(DelegatingSubscriber[AggregateRootEvent](eventsProbe.ref))).run()
          within(1 second) {
            droneProbe.send(droneActor, CreateUser(CommandHeader(), "a", 0L, "hans", "meier"))
            droneProbe.expectMsgType[CommandExecuted]
            droneProbe.send(droneActor, ChangeUserAgeForCreditCard(CommandHeader(), "a", 7L, 22))
            droneProbe.expectMsgType[CommandNotExecuted]
            droneProbe.send(droneActor, ChangeUserFullName(CommandHeader(), "a", 1L, "fritz", "weller"))
            droneProbe.expectMsgType[CommandExecuted]
            droneProbe.send(droneActor, ConfirmUserDeath(CommandHeader(), "a", 3L))
            eventsProbe.expectMsgType[UserCreated]
            eventsProbe.expectMsgType[UserSurnameChanged]
            eventsProbe.expectMsgType[UserLastnameChanged]
            eventsProbe.expectMsgType[UserDied]
          }
        }
      }
    }
  }

  private val currentTestId = new java.util.concurrent.atomic.AtomicInteger(1)
  def nextTestId = currentTestId.getAndIncrement()

  case class FixtureParam(testId: Int, drone: ActorRef, droneProbe: TestProbe, streams: AlmhirtStreams)

  def withFixture(test: OneArgTest) = {
    import almhirt.aggregates._

    val testId = nextTestId
    //info(s"Test $testId")
    val eventlogProps: Props = almhirt.eventlog.InMemoryAggregateRootEventLog.props()
    val eventlogActor: ActorRef = system.actorOf(eventlogProps, s"eventlog-$testId")
    val droneProbe = TestProbe()
    val streams = AlmhirtStreams(s"almhirt-streams-$testId")(1 second).awaitResultOrEscalate(1 second)
    val droneProps: Props = Props(
      new AggregateRootDrone[User, UserEvent] with ActorLogging with UserEventHandler with UserCommandHandler with UserUpdater with AggregateRootDroneCommandHandlerAdaptor[User, UserCommand, UserEvent] {
        def ccuad = AggregateRootDroneProtocolTests.this.ccuad
        def futuresContext: ExecutionContext = executionContext
        def aggregateEventLog: ActorRef = eventlogActor
        def snapshotStorage: Option[ActorRef] = None
        val eventsBroker: StreamBroker[Event] = streams.eventBroker
        val returnToUnitializedAfter = None

        override val aggregateCommandValidator = AggregateRootCommandValidator.Validated
        override val tag = scala.reflect.ClassTag[UserCommand](classOf[UserCommand])

        override def sendMessage(msg: AggregateRootDroneInternalMessages.AggregateDroneMessage) {
          droneProbe.ref ! msg
        }

      })

    val droneActor: ActorRef = system.actorOf(droneProps, s"drone-$testId")
    try {
      withFixture(test.toNoArgTest(FixtureParam(testId, droneActor, droneProbe, streams)))
    } finally {
      system.stop(droneActor)
      system.stop(eventlogActor)
      streams.stop()

    }
  }
}