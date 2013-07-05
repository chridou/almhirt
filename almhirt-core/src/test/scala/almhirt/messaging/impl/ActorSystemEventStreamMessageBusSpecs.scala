package almhirt.messaging.impl

import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers
import akka.testkit.TestKit
import scala.concurrent.duration.FiniteDuration
import akka.actor._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.messaging.Classifier

class ActorSystemEventStreamMessageBusSpecs extends TestKit(ActorSystem("ActorSystemEventStreamMessageBusSpecs")) with FunSpec with ShouldMatchers{
  implicit val defaultDuration = FiniteDuration(1, "s")
  
  class A()
  class B() extends A
  class C() extends A
  case class D(content: String = "") extends A
  
  val classifyD = Classifier.payloadPredicate[D](x => !x.content.isEmpty)
  
  val (messagebus, _) = ActorSystemEventStreamMessageBus(this.system).awaitResult.forceResult
  val channelA = messagebus.channel[A].awaitResult.forceResult
  val channelAB = channelA.channel[B].awaitResult.forceResult
  val channelC = messagebus.channel[C].awaitResult.forceResult
  val channelAC = channelA.channel[C].awaitResult.forceResult
  val channelDPred = messagebus.channel[D](classifyD).awaitResult.forceResult
  val channelADPred = channelA.channel[D](classifyD).awaitResult.forceResult
  
  describe("""A ActorSystemEventStreamMessageBus"""){
    it("""sss""") {
   }
  }
}