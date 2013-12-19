package almhirt.messaging.impl

import org.scalatest._
import akka.testkit._
import scala.concurrent.duration.FiniteDuration
import akka.actor._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.messaging.Classifier

class MessageOrderSpec extends TestKit(ActorSystem("MessageOrderSpec")) with FunSpecLike with Matchers{
  implicit val defaultDuration = FiniteDuration(1, "s")
  implicit val ccuad = CanCreateUuidsAndDateTimes()
  implicit val executionContext = this.system.dispatcher
  val maxMsgDuration = FiniteDuration(100, "ms")

  val classifyStringNotEmpty = Classifier.payloadPredicate[String](x => !x.isEmpty)
  val classifyStringNotNO = Classifier.payloadPredicate[String](x => !x.equals("NO"))

  val (messagebus, _) = ActorSystemEventStreamMessageBus(this.system).awaitResult(defaultDuration).forceResult
  
  val channelUnfiltered = messagebus.channel[String].awaitResult(maxMsgDuration).forceResult
  val channelStringNotEmpty = messagebus.channel[String](classifyStringNotEmpty).awaitResult(maxMsgDuration).forceResult
  val channelStringNotNO = messagebus.channel[String](classifyStringNotNO).awaitResult(maxMsgDuration).forceResult
  val channelTwoFilter = channelStringNotEmpty.channel[String](classifyStringNotNO).awaitResult(maxMsgDuration).forceResult
  
  describe("""All messages"""){
    it("""should received in same order as send"""){
      val probe = TestProbe()
      val payloads = List("msg0", "msg1", "msg2")
      val subscriptionF = messagebus.subscribe(probe.ref, Classifier.forClass(classOf[AnyRef]))
      payloads.foreach(e => messagebus.publishMessage(Message(e)))
      val result = probe.receiveWhile(maxMsgDuration, maxMsgDuration, payloads.length){
        case msg : Message => msg.payload
      }
      result should be(payloads)
      subscriptionF.onSuccess(_.cancel)
    }
    it("""should filtered and received in same order as send"""){
      val probe = TestProbe()
      val payloads = List("","msg0", "", "msg1", "msg2", "")
      val subscriptionF = channelStringNotEmpty.subscribe(probe.ref)
      payloads.foreach(e => messagebus.publishMessage(Message(e)))
      val result = probe.receiveWhile(maxMsgDuration, maxMsgDuration, payloads.length){
        case payload: String => payload
      }
      result should be(List("msg0", "msg1", "msg2"))
      subscriptionF.onSuccess(_.cancel)
    }
    it("""send over unfiltered channel should received in same order as send"""){
      val probe = TestProbe()
      val payloads = List("msg0", "msg1", "msg2")
      val subscriptionF = channelUnfiltered.subscribe(probe.ref)
      payloads.foreach(e => messagebus.publishMessage(Message(e)))
      val result = probe.receiveWhile(maxMsgDuration, maxMsgDuration, payloads.length){
        case payload: String => payload
      }
      result should be(payloads)
      subscriptionF.onSuccess(_.cancel)
    }
    it("""should filtered for '' and 'NO' and received in same order as send """){
      val probe = TestProbe()
      val payloads = List("","NO","msg0", "","NO", "msg1", "msg2","NO", "")
      val subscriptionF = channelTwoFilter.subscribe(probe.ref)
      payloads.foreach(e => messagebus.publishMessage(Message(e)))
      val result = probe.receiveWhile(maxMsgDuration, maxMsgDuration, payloads.length){
        case payload: String => payload
      }
      result should be(List("msg0", "msg1", "msg2"))
      subscriptionF.onSuccess(_.cancel)
    }
    ignore("""should received only one time, when subscribe to the same channel twice"""){
      val probe = TestProbe()
      val payloads = List("msg0", "msg1", "msg2")
      val subscriptionF = channelUnfiltered.subscribe(probe.ref)
      val subscriptionG = channelUnfiltered.subscribe(probe.ref)
      payloads.foreach(e => messagebus.publishMessage(Message(e)))
      val result = probe.receiveWhile(maxMsgDuration, maxMsgDuration, payloads.length){
        case payload: String => payload
      }
      result should be(List("msg0", "msg1", "msg2"))
      subscriptionF.onSuccess(_.cancel)
      subscriptionG.onSuccess(_.cancel)
    }
    ignore("""should filtered for '' and 'NO' with two subscriptions and received in same order as send """){
      val probe = TestProbe()
      val payloads = List("","NO","msg0", "","NO", "msg1", "msg2","NO", "")
      val subscriptionF = channelStringNotEmpty.subscribe(probe.ref)
      val subscriptionG = channelStringNotNO.subscribe(probe.ref)
      payloads.foreach(e => messagebus.publishMessage(Message(e)))
      val result = probe.receiveWhile(maxMsgDuration, maxMsgDuration, payloads.length){
        case payload: String => payload
      }
      result should be(List("msg0", "msg1", "msg2"))
      subscriptionF.onSuccess(_.cancel)
      subscriptionG.onSuccess(_.cancel)
    }
  }

}