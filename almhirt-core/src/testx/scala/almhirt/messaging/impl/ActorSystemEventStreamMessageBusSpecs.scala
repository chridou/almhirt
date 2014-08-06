package almhirt.messaging.impl

import org.scalatest._
import akka.testkit._
import scala.concurrent.duration.FiniteDuration
import akka.actor._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.messaging.Classifier

class ActorSystemEventStreamMessageBusSpecs extends TestKit(ActorSystem("ActorSystemEventStreamMessageBusSpecs")) with FunSpecLike with Matchers {
  implicit val defaultDuration = FiniteDuration(1, "s")
  implicit val ccuad = CanCreateUuidsAndDateTimes()
  implicit val executionContext = this.system.dispatcher
  val maxMsgDuration = FiniteDuration(100, "ms")

  class A()
  class B() extends A
  class C() extends A
  case class D(content: String = "") extends A

  val classifyD = Classifier.payloadPredicate[D](x => !x.content.isEmpty)

  val (messagebus, _) = ActorSystemEventStreamMessageBus(this.system).awaitResult(defaultDuration).forceResult
  val channelA = messagebus.channel[A].awaitResult(maxMsgDuration).forceResult
  val channelAB = channelA.channel[B].awaitResult(maxMsgDuration).forceResult
  val channelC = messagebus.channel[C].awaitResult(maxMsgDuration).forceResult
  val channelAC = channelA.channel[C].awaitResult(maxMsgDuration).forceResult
  val channelAD = channelA.channel[D].awaitResult(maxMsgDuration).forceResult
  val channelDPred = messagebus.channel[D](classifyD).awaitResult(maxMsgDuration).forceResult
  val channelADPred = channelA.channel[D](classifyD).awaitResult(maxMsgDuration).forceResult

  describe("""An ActorSystemEventStreamMessageBus with a subscription on AnyRef""") {
    it("""should publish to a subscriber""") {
      val probe = TestProbe()
      val payload = "testMessage"
      val msg = Message(payload)
      val subscriptionF = messagebus.subscribe(probe.ref, Classifier.forClass(classOf[AnyRef]))
      messagebus.publishMessage(msg)
      probe.expectMsg(maxMsgDuration, msg)
      subscriptionF.onSuccess(_.cancel)
    }
    it("""should not publish to a subscriber when the subscription has been cancelled""") {
      val probe = TestProbe()
      val payload = "testMessage"
      val msg = Message(payload)
      val subscriptionF = messagebus.subscribe(probe.ref, Classifier.forClass(classOf[AnyRef]))
      messagebus.publishMessage(msg)
      probe.expectMsg(maxMsgDuration, msg)
      subscriptionF.awaitResult(maxMsgDuration).forceResult.cancel
      messagebus.publishMessage(msg)
      probe.expectNoMsg(maxMsgDuration)
    }
  }

  describe("""An ActorSystemEventStreamMessageBus with a subscription on A""") {
    it("""should trigger a Message(A) on the subscriber when a Message(A) is published""") {
      val probe = TestProbe()
      val payload = new A()
      val msg = Message(payload)
      val subscriptionF = messagebus.subscribe(probe.ref, Classifier.forClass(classOf[A]))
      messagebus.publishMessage(msg)
      probe.expectMsg(maxMsgDuration, msg)
      subscriptionF.onSuccess(_.cancel)
    }
    it("""should trigger a Message(B) on the subscriber when a Message(B) is published""") {
      val probe = TestProbe()
      val payload = new B()
      val msg = Message(payload)
      val subscriptionF = messagebus.subscribe(probe.ref, Classifier.forClass(classOf[A]))
      messagebus.publishMessage(msg)
      probe.expectMsg(maxMsgDuration, msg)
      subscriptionF.onSuccess(_.cancel)
    }
    it("""should trigger a Message(C) on the subscriber when a Message(C) is published""") {
      val probe = TestProbe()
      val payload = new C()
      val msg = Message(payload)
      val subscriptionF = messagebus.subscribe(probe.ref, Classifier.forClass(classOf[A]))
      messagebus.publishMessage(msg)
      probe.expectMsg(maxMsgDuration, msg)
      subscriptionF.onSuccess(_.cancel)
    }
    it("""should trigger a Message(D) on the subscriber when a Message(D) is published""") {
      val probe = TestProbe()
      val payload = D()
      val msg = Message(payload)
      val subscriptionF = messagebus.subscribe(probe.ref, Classifier.forClass(classOf[A]))
      messagebus.publishMessage(msg)
      probe.expectMsg(maxMsgDuration, msg)
      subscriptionF.onSuccess(_.cancel)
    }
  }

  describe("""An ActorSystemEventStreamMessageBus with a subscription on C""") {
    it("""should not trigger any Message on the subscriber when a Message(A) is published""") {
      val probe = TestProbe()
      val payload = new A()
      val msg = Message(payload)
      val subscriptionF = messagebus.subscribe(probe.ref, Classifier.forClass(classOf[C]))
      messagebus.publishMessage(msg)
      probe.expectNoMsg(maxMsgDuration)
      subscriptionF.onSuccess(_.cancel)
    }
    it("""should not trigger any Message on the subscriber when a Message(B) is published""") {
      val probe = TestProbe()
      val payload = new B()
      val msg = Message(payload)
      val subscriptionF = messagebus.subscribe(probe.ref, Classifier.forClass(classOf[C]))
      messagebus.publishMessage(msg)
      probe.expectNoMsg(maxMsgDuration)
      subscriptionF.onSuccess(_.cancel)
    }
    it("""should trigger a Message(C) on the subscriber when a Message(C) is published""") {
      val probe = TestProbe()
      val payload = new C()
      val msg = Message(payload)
      val subscriptionF = messagebus.subscribe(probe.ref, Classifier.forClass(classOf[C]))
      messagebus.publishMessage(msg)
      probe.expectMsg(maxMsgDuration, msg)
      subscriptionF.onSuccess(_.cancel)
    }
    it("""should not trigger any Message on the subscriber when a Message(D) is published""") {
      val probe = TestProbe()
      val payload = D()
      val msg = Message(payload)
      val subscriptionF = messagebus.subscribe(probe.ref, Classifier.forClass(classOf[C]))
      messagebus.publishMessage(msg)
      probe.expectNoMsg(maxMsgDuration)
      subscriptionF.onSuccess(_.cancel)
    }
  }

  describe("""A MessageStream subscribed to A on the bus""") {
    it("""should trigger a an A on its subscriber when an Message(A) is published""") {
      val probe = TestProbe()
      val payload = new A()
      val msg = Message(payload)
      val subscriptionF = channelA.subscribe(probe.ref)
      messagebus.publishMessage(msg)
      probe.expectMsg(maxMsgDuration, payload)
      subscriptionF.onSuccess(_.cancel)
    }
    it("""should trigger a B on its subscriber when a Message(B) is published""") {
      val probe = TestProbe()
      val payload = new B()
      val msg = Message(payload)
      val subscriptionF = channelA.subscribe(probe.ref)
      messagebus.publishMessage(msg)
      probe.expectMsg(maxMsgDuration, payload)
      subscriptionF.onSuccess(_.cancel)
    }
    it("""should trigger a C on its subscriber when a Message(C) is published""") {
      val probe = TestProbe()
      val payload = new C()
      val msg = Message(payload)
      val subscriptionF = channelA.subscribe(probe.ref)
      messagebus.publishMessage(msg)
      probe.expectMsg(maxMsgDuration, payload)
      subscriptionF.onSuccess(_.cancel)
    }
    it("""should trigger a D on its subscriber when a Message(D) is published""") {
      val probe = TestProbe()
      val payload = new D()
      val msg = Message(payload)
      val subscriptionF = channelA.subscribe(probe.ref)
      messagebus.publishMessage(msg)
      probe.expectMsg(maxMsgDuration, payload)
      subscriptionF.onSuccess(_.cancel)
    }
  }

  describe("""A MessageStream subscribed to C on the bus""") {
    it("""should not trigger anything when an Message(A) is published""") {
      val probe = TestProbe()
      val payload = new A()
      val msg = Message(payload)
      val subscriptionF = channelC.subscribe(probe.ref)
      messagebus.publishMessage(msg)
      probe.expectNoMsg(maxMsgDuration)
      subscriptionF.onSuccess(_.cancel)
    }
    it("""should not trigger anything on its subscriber when a Message(B) is published""") {
      val probe = TestProbe()
      val payload = new B()
      val msg = Message(payload)
      val subscriptionF = channelC.subscribe(probe.ref)
      messagebus.publishMessage(msg)
      probe.expectNoMsg(maxMsgDuration)
      subscriptionF.onSuccess(_.cancel)
    }
    it("""should trigger a C on its subscriber when a Message(C) is published""") {
      val probe = TestProbe()
      val payload = new C()
      val msg = Message(payload)
      val subscriptionF = channelC.subscribe(probe.ref)
      messagebus.publishMessage(msg)
      probe.expectMsg(maxMsgDuration, payload)
      subscriptionF.onSuccess(_.cancel)
    }
    it("""should not trigger anything on its subscriber when a Message(D) is published""") {
      val probe = TestProbe()
      val payload = new D()
      val msg = Message(payload)
      val subscriptionF = channelC.subscribe(probe.ref)
      messagebus.publishMessage(msg)
      probe.expectNoMsg(maxMsgDuration)
      subscriptionF.onSuccess(_.cancel)
    }
  }
  
  describe("""A MessageStream subscribed to C on a stream(A)""") {
    it("""should not trigger anything on its subscriber when an Message(A) is published""") {
      val probe = TestProbe()
      val payload = new A()
      val msg = Message(payload)
      val subscriptionF = channelAC.subscribe(probe.ref)
      messagebus.publishMessage(msg)
      probe.expectNoMsg(maxMsgDuration)
      subscriptionF.onSuccess(_.cancel)
    }
    it("""should not trigger anything on its subscriber when an Message(B) is published""") {
      val probe = TestProbe()
      val payload = new B()
      val msg = Message(payload)
      val subscriptionF = channelAC.subscribe(probe.ref)
      messagebus.publishMessage(msg)
      probe.expectNoMsg(maxMsgDuration)
      subscriptionF.onSuccess(_.cancel)
    }
    it("""should trigger a C on its subscriber when a Message(C) is published""") {
      val probe = TestProbe()
      val payload = new C()
      val msg = Message(payload)
      val subscriptionF = channelAC.subscribe(probe.ref)
      messagebus.publishMessage(msg)
      probe.expectMsg(maxMsgDuration, payload)
      subscriptionF.onSuccess(_.cancel)
    }
    it("""should not trigger anything on its subscriber when an Message(D) is published""") {
      val probe = TestProbe()
      val payload = new D()
      val msg = Message(payload)
      val subscriptionF = channelAC.subscribe(probe.ref)
      messagebus.publishMessage(msg)
      probe.expectNoMsg(maxMsgDuration)
      subscriptionF.onSuccess(_.cancel)
    }
  }

  describe("""A MessageStream subscribed to D on a stream(A)""") {
    it("""should not trigger anything on its subscriber when an Message(A) is published""") {
      val probe = TestProbe()
      val payload = new A()
      val msg = Message(payload)
      val subscriptionF = channelAD.subscribe(probe.ref)
      messagebus.publishMessage(msg)
      probe.expectNoMsg(maxMsgDuration)
      subscriptionF.onSuccess(_.cancel)
    }
    it("""should not trigger anything on its subscriber when an Message(B) is published""") {
      val probe = TestProbe()
      val payload = new B()
      val msg = Message(payload)
      val subscriptionF = channelAD.subscribe(probe.ref)
      messagebus.publishMessage(msg)
      probe.expectNoMsg(maxMsgDuration)
      subscriptionF.onSuccess(_.cancel)
    }
    it("""should not trigger anything on its subscriber when a Message(C) is published""") {
      val probe = TestProbe()
      val payload = new C()
      val msg = Message(payload)
      val subscriptionF = channelAD.subscribe(probe.ref)
      messagebus.publishMessage(msg)
      probe.expectNoMsg(maxMsgDuration)
      subscriptionF.onSuccess(_.cancel)
    }
    it("""should trigger a D on its subscriber when a Message(D) is published""") {
      val probe = TestProbe()
      val payload = new D("x")
      val msg = Message(payload)
      val subscriptionF = channelAD.subscribe(probe.ref)
      messagebus.publishMessage(msg)
      probe.expectMsg(maxMsgDuration, payload)
      subscriptionF.onSuccess(_.cancel)
    }
  }

  describe("""A MessageStream  subscribed to D on the messageBus where the content of D must not be empty""") {
    it("""should trigger the subscriber when the content of D is not empty""") {
      val probe = TestProbe()
      val payload = new D("x")
      val msg = Message(payload)
      val subscriptionF = channelDPred.subscribe(probe.ref)
      messagebus.publishMessage(msg)
      probe.expectMsg(maxMsgDuration, payload)
      subscriptionF.onSuccess(_.cancel)
    }
    it("""should not trigger anything when the content of D is empty""") {
      val probe = TestProbe()
      val payload = new D()
      val msg = Message(payload)
      val subscriptionF = channelDPred.subscribe(probe.ref)
      messagebus.publishMessage(msg)
      probe.expectNoMsg(maxMsgDuration)
      subscriptionF.onSuccess(_.cancel)
    }
  }

  describe("""A MessageStream  subscribed to D on a stream(A) where the content of D must not be empty""") {
    it("""should trigger the subscriber when the content of D is not empty""") {
      val probe = TestProbe()
      val payload = new D("x")
      val msg = Message(payload)
      val subscriptionF = channelADPred.subscribe(probe.ref)
      messagebus.publishMessage(msg)
      probe.expectMsg(maxMsgDuration, payload)
      subscriptionF.onSuccess(_.cancel)
    }
    it("""should not trigger anything when the content of D is empty""") {
      val probe = TestProbe()
      val payload = new D()
      val msg = Message(payload)
      val subscriptionF = channelADPred.subscribe(probe.ref)
      messagebus.publishMessage(msg)
      probe.expectNoMsg(maxMsgDuration)
      subscriptionF.onSuccess(_.cancel)
    }
  }
}