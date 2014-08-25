package almhirt.streaming

import scala.language.postfixOps
import scala.concurrent.duration._
import org.reactivestreams.api.Producer
import akka.actor._
import almhirt.common._
import akka.testkit._
import org.scalatest._

class SequentialPostOfficeClientTests(_system: ActorSystem) extends TestKit(_system) with fixture.FlatSpecLike with Matchers with BeforeAndAfterAll {
  def this() = this(ActorSystem("VillagePostOfficeTests", almhirt.TestConfigs.logWarningConfig))

  implicit val executionContext = system.dispatchers.defaultGlobalDispatcher

  val villageOfficeBufferSize = 16

  behavior of "The SequentialPostOfficeClient"

  val bigN = 30000
  val pSize = 3
  it should s"dispatch many packages($bigN) of the same size($pSize) (${bigN * pSize} items)" in { fixture ⇒
    val FixtureParam(testId, postOffice, producer) = fixture
    val consumerProbe = TestProbe()
    val consumer = DelegatingConsumer[String](consumerProbe.ref)
    producer.produceTo(consumer)

    val items = (1 to (bigN * pSize)).map(_.toString)
    val packages = items.grouped(pSize)

    val probe = TestProbe()
    val clientSettings = PostOfficeClientSettings(10, 10 millis, 10)
    val start = Deadline.now
    within(10 seconds) {
      val dropper = system.actorOf(SequentialPostOfficeDropper.props(postOffice, packages.toSeq, clientSettings), s"dropper_$testId")
      val res = consumerProbe.receiveN(bigN * pSize, 9 seconds)
      val time = start.lap
      info(s"Took ${time.defaultUnitString}. (${(bigN * pSize * 1000).toDouble / time.toMillis}/s)")
      res should equal(items)
    }
  }

  private val currentTestId = new java.util.concurrent.atomic.AtomicInteger(1)
  def nextTestId = currentTestId.getAndIncrement()

  case class FixtureParam(testId: Int, postOffice: PostOffice[String], producer: Producer[String])

  def withFixture(test: OneArgTest) = {
    val testId = nextTestId
    val transporterActor = system.actorOf(StreamShipper.props[String](), s"shipper-$testId")
    val (broker, producer, stopper) = StreamShipper[String](transporterActor)
    val postOfficeActor = system.actorOf(VillagePostOffice.props[String](broker, villageOfficeBufferSize), s"village-post-office-$testId")
    val postOffice = PostOffice[String](postOfficeActor)
    val fixture = FixtureParam(testId, postOffice, producer)
    try {
      withFixture(test.toNoArgTest(fixture))
    } finally {
      stopper.stop()
    }
  }

  override def beforeAll() {

  }

  override def afterAll() {
    TestKit.shutdownActorSystem(system)
  }

}