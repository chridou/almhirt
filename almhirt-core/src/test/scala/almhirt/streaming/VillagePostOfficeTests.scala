package almhirt.streaming

import scala.language.postfixOps
import scala.concurrent.duration._
import org.reactivestreams.api.Producer
import akka.actor._
import almhirt.common._
import akka.testkit._
import org.scalatest._

class VillagePostOfficeTests(_system: ActorSystem) extends TestKit(_system) with fixture.FlatSpecLike with Matchers with BeforeAndAfterAll {
  def this() = this(ActorSystem("VillagePostOfficeTests", almhirt.TestConfigs.logWarningConfig))

  implicit val executionContext = system.dispatchers.defaultGlobalDispatcher

  val villageOfficeBufferSize = 16

  behavior of "The VillagePostOffice"

  it should "dispatch an empty package" in { fixture =>
    val FixtureParam(postOffice, producer) = fixture
    val consumerProbe = TestProbe()
    val consumer = DelegatingConsumer[String](consumerProbe.ref)
    producer.produceTo(consumer)

    val probe = TestProbe()
    within(1 second) {
      postOffice.deliverUntracked(probe.ref)
      probe.expectMsgType[DeliveryJobDone]
    }
  }

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

  it should "dispatch 2 packages" in { fixture =>
    val FixtureParam(postOffice, producer) = fixture
    val consumerProbe = TestProbe()
    val consumer = DelegatingConsumer[String](consumerProbe.ref)
    producer.produceTo(consumer)

    val sample1 = "1" :: "2" :: "3" :: Nil
    val sample2 = "4" :: "5" :: "6" :: Nil

    val probe = TestProbe()
    within(3 second) {
      postOffice.deliverUntracked(probe.ref, sample1: _*)
      probe.expectMsgType[DeliveryJobDone]
      postOffice.deliverUntracked(probe.ref, sample2: _*)
      probe.expectMsgType[DeliveryJobDone]
      val res = consumerProbe.receiveN(6)
      res should equal(sample1 ++ sample2)
    }
  }

  it should s"dispatch as many packages as the postoffice has space in its buffer($villageOfficeBufferSize) when waiting for each delivery" in { fixture =>
    val FixtureParam(postOffice, producer) = fixture
    val consumerProbe = TestProbe()
    val consumer = DelegatingConsumer[String](consumerProbe.ref)
    producer.produceTo(consumer)

    val items = (1 to (villageOfficeBufferSize * 3)).map(_.toString)
    val packages = items.grouped(3)

    val probe = TestProbe()
    val start = Deadline.now
    within(1 second) {
      packages.foreach { sample =>
        postOffice.deliverUntracked(probe.ref, sample: _*)
        probe.expectMsgType[DeliveryJobDone]
      }
      val res = consumerProbe.receiveN(villageOfficeBufferSize * 3)
      info(s"Took ${start.lap.defaultUnitString}")
      res should equal(items)
    }
  }

  it should s"dispatch as many packages as the postoffice has space in its buffer($villageOfficeBufferSize)" in { fixture =>
    val FixtureParam(postOffice, producer) = fixture
    val consumerProbe = TestProbe()
    val consumer = DelegatingConsumer[String](consumerProbe.ref)
    producer.produceTo(consumer)

    val items = (1 to (villageOfficeBufferSize * 3)).map(_.toString)
    val packages = items.grouped(3)

    val probe = TestProbe()
    val start = Deadline.now
    within(1 seconds) {
      packages.foreach(sample => postOffice.deliverUntracked(probe.ref, sample: _*))
      val acks = probe.receiveN(villageOfficeBufferSize)
      info(s"${acks.size} delivery status messages received.")
      acks.collect { case m: DeliveryJobDone => m } should have size (villageOfficeBufferSize)

      val res = consumerProbe.receiveN(villageOfficeBufferSize * 3)
      info(s"Took ${start.lap.defaultUnitString}")
      res should equal(items)
    }
  }

  val bigN = 10000
  val pSize = 30
  it should s"dispatch many packages($bigN) of the same size($pSize) when waiting for each delivery" in { fixture =>
    val FixtureParam(postOffice, producer) = fixture
    val consumerProbe = TestProbe()
    val consumer = DelegatingConsumer[String](consumerProbe.ref)
    producer.produceTo(consumer)

    val items = (1 to (bigN * pSize)).map(_.toString)
    val packages = items.grouped(pSize)

    val probe = TestProbe()
    val start = Deadline.now
    within(10 seconds) {
      packages.foreach { sample =>
        postOffice.deliverUntracked(probe.ref, sample: _*)
        probe.expectMsgType[DeliveryJobDone]
      }

      val res = consumerProbe.receiveN(bigN * pSize)
      info(s"Took ${start.lap.defaultUnitString}")
      res should equal(items)
    }
  }

  private val currentTestId = new java.util.concurrent.atomic.AtomicInteger(1)
  def nextTestId = currentTestId.getAndIncrement()

  case class FixtureParam(postOffice: PostOffice[String], producer: Producer[String])

  def withFixture(test: OneArgTest) = {
    val testId = nextTestId
    val transporterActor = system.actorOf(StreamShipper.props[String](), s"shipper-$testId")
    val (broker, producer, stopper) = StreamShipper[String](transporterActor)
    val postOfficeActor = system.actorOf(VillagePostOffice.props[String](broker, villageOfficeBufferSize), s"village-post-office-$testId")
    val postOffice = PostOffice[String](postOfficeActor)
    val fixture = FixtureParam(postOffice, producer)
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

