package almhirt.streaming

import scala.language.postfixOps
import scala.concurrent.duration._
import akka.actor._
import akka.stream.actor.ActorPublisher
import akka.stream.scaladsl2._
import almhirt.common._
import akka.testkit._
import org.scalatest._

class UnboundedSinkTests(_system: ActorSystem) extends TestKit(_system) with FunSuiteLike with Matchers with BeforeAndAfterAll {
  def this() = this(ActorSystem("UnboundedSinkTests", almhirt.TestConfigs.logInfoConfig))

  implicit val executionContext = system.dispatchers.defaultGlobalDispatcher
  implicit val ccuad = CanCreateUuidsAndDateTimes()

  implicit val mat = FlowMaterializer()

  private val currentTestId = new java.util.concurrent.atomic.AtomicInteger(1)
  def nextTestId = currentTestId.getAndIncrement()

  test("UnboundedSink should publish an item") {
    val testId = nextTestId
    val sinkActor = system.actorOf(UnboundedSink.props[String], s"publisher-$testId")
    val eventSink = UnboundedSink[String](sinkActor)
    val probe = TestProbe()
    val subscr = DelegatingSubscriber[String](probe.ref)
    FlowFrom[String].withSource(PublisherSource(ActorPublisher[String](sinkActor))).withSink(SubscriberSink(subscr)).run
    eventSink.publish("1")
    probe.expectMsg("1")
    eventSink.stop()
  }

  test("UnboundedSink should publish 2 items") {
    val testId = nextTestId
    val sinkActor = system.actorOf(UnboundedSink.props[String], s"publisher-$testId")
    val eventSink = UnboundedSink[String](sinkActor)
    val probe = TestProbe()
    val subscr = DelegatingSubscriber[String](probe.ref)
    FlowFrom[String].withSource(PublisherSource(ActorPublisher[String](sinkActor))).withSink(SubscriberSink(subscr)).run
    eventSink.publish("1")
    eventSink.publish("2")
    probe.expectMsg("1")
    probe.expectMsg("2")
    eventSink.stop()
  }

  test("UnboundedSink should publish many items") {
    val testId = nextTestId
    val sinkActor = system.actorOf(UnboundedSink.props[String], s"publisher-$testId")
    val eventSink = UnboundedSink[String](sinkActor)
    val probe = TestProbe()
    val subscr = DelegatingSubscriber[String](probe.ref)
    FlowFrom[String].withSource(PublisherSource(ActorPublisher[String](sinkActor))).withSink(SubscriberSink(subscr)).run
    (1 to 100).map(_.toString).foreach(eventSink.publish(_))
    val items = probe.receiveN(100).map(_.asInstanceOf[String]).toList
    items should equal((1 to 100).map(_.toString))
  }
  
  override def beforeAll() {

  }

  override def afterAll() {
    TestKit.shutdownActorSystem(system)
  }

}