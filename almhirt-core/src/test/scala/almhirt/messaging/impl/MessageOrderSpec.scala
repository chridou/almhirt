package almhirt.messaging.impl

import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers
import akka.testkit._
import scala.concurrent.duration.FiniteDuration
import akka.actor._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.messaging.Classifier

class MessageOrderSpec extends TestKit(ActorSystem("MessageOrderSpec")) with FunSpec with ShouldMatchers{
  implicit val defaultDuration = FiniteDuration(1, "s")
  implicit val ccuad = CanCreateUuidsAndDateTimes()
  implicit val executionContext = this.system.dispatcher
  val maxMsgDuration = FiniteDuration(100, "ms")

  class A()
  class B() extends A
  class C() extends A
  case class D(content: String = "") extends A

  val classifyD = Classifier.payloadPredicate[D](x => !x.content.isEmpty)
  val classifyStringNotEmpty = Classifier.payloadPredicate[String](x => !x.isEmpty)

  val (messagebus, _) = ActorSystemEventStreamMessageBus(this.system).awaitResult(defaultDuration).forceResult
  val channelA = messagebus.channel[A].awaitResult(maxMsgDuration).forceResult
  val channelAB = channelA.channel[B].awaitResult(maxMsgDuration).forceResult
  val channelC = messagebus.channel[C].awaitResult(maxMsgDuration).forceResult
  val channelAC = channelA.channel[C].awaitResult(maxMsgDuration).forceResult
  val channelAD = channelA.channel[D].awaitResult(maxMsgDuration).forceResult
  val channelDPred = messagebus.channel[D](classifyD).awaitResult(maxMsgDuration).forceResult
  val channelADPred = channelA.channel[D](classifyD).awaitResult(maxMsgDuration).forceResult
  
  val channelStringNotEmpty = messagebus.channel[String](classifyStringNotEmpty).awaitResult(maxMsgDuration).forceResult
  
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
  }
  
  describe("""All messages"""){
    it("""should received in same order as send"""){
      val probe = TestProbe()
      val payloads = List("msg0", "msg1", "msg2")
      val subscriptionF = messagebus.subscribe(probe.ref, Classifier.forClass(classOf[AnyRef]))
      payloads.foreach(e => messagebus.publishMessage(Message(e)))
      val result = probe.receiveWhile(maxMsgDuration, maxMsgDuration, payloads.length){
        case msg : Message => msg.payload.toString
      }
      result should be(payloads)
      subscriptionF.onSuccess(_.cancel)
    }
    it("""should filtered and received in same order as send"""){
      val probe = TestProbe()
      val payloads = List("","msg0", "", "msg1", "msg2", "")
      val subscriptionF = channelStringNotEmpty.subscribe(probe.ref)
      //val subscriptionF = messagebus.subscribe(probe.ref, Classifier.forClass(classOf[AnyRef]))
      payloads.foreach(e => messagebus.publishMessage(Message(e)))
      val result = probe.receiveWhile(maxMsgDuration, maxMsgDuration, payloads.length){
        case payload: String => payload
      }
      result should be(List("msg0", "msg1", "msg2"))
      subscriptionF.onSuccess(_.cancel)
    }
  }

}