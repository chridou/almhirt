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