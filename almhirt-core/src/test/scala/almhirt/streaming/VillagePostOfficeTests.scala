package almhirt.streaming

import scala.language.postfixOps
import scala.concurrent.duration._
import org.reactivestreams.api.Producer
import akka.actor._
import almhirt.common._
import akka.testkit._
import org.scalatest._

class VillagePostOfficeTests(_system: ActorSystem) extends TestKit(_system) with fixture.FlatSpecLike with Matchers with BeforeAndAfterAll {
  def this() = this(ActorSystem("VillagePostOfficeTests"))

  implicit val executionContext = system.dispatchers.defaultGlobalDispatcher
  implicit val ccuad = CanCreateUuidsAndDateTimes()

  behavior of "The VillagePostOffice"

  it should "dispatch a package" in { fixture =>
    val FixtureParam(postOffice, producer) = fixture
    val consumerProbe = TestProbe()
    val consumer = DelegatingConsumer[String](consumerProbe.ref)
    producer.produceTo(consumer)

    val sample = "1" :: "2" :: "3" :: Nil

    val probe = TestProbe()
    within(1 second) {
      postOffice.deliverUntracked(probe.ref, sample: _*)
      probe.expectMsgType[DeliveryJobDone]
      val res = consumerProbe.receiveN(3)
      res should equal(sample)
    }
  }

  val bigN = 10
  val pSize = 3
  it should s"dispatch a many packages($bigN) of the same size($pSize) package" in { fixture =>
    val FixtureParam(postOffice, producer) = fixture
    val consumerProbe = TestProbe()
    val consumer = DelegatingConsumer[String](consumerProbe.ref)
    producer.produceTo(consumer)

    val samples = (1 to (bigN * 3)).map(_.toString).grouped(bigN)

    val probe = TestProbe()
    within(1 second) {
      samples.foreach(sample => postOffice.deliverUntracked(probe.ref, sample: _*))
      val acks = probe.receiveWhile(3 seconds){ case m: DeliveryJobDone => m }
      acks should have size(bigN)
      
      val res = consumerProbe.receiveN(3)
      res should equal("s")
    }
  }
  private val currentTestId = new java.util.concurrent.atomic.AtomicInteger(1)
  def nextTestId = currentTestId.getAndIncrement()

  case class FixtureParam(postOffice: PostOffice[String], producer: Producer[String])

  def withFixture(test: OneArgTest) = {
    val testId = nextTestId
    val transporterActor = system.actorOf(SuppliesTransporter.props[String](), s"transporter-$testId")
    val (broker, producer) = SuppliesTransporter[String](transporterActor)
    val postOfficeActor = system.actorOf(VillagePostOffice.props[String](broker, 16), s"village-post-office-$testId")
    val postOffice = PostOffice[String](postOfficeActor)
    val fixture = FixtureParam(postOffice, producer)
    try {
      withFixture(test.toNoArgTest(fixture))
    } finally {
    }
  }

  override def beforeAll() {

  }

  override def afterAll() {
    TestKit.shutdownActorSystem(system)
  }

}