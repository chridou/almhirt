package almhirt.messaging

import org.specs2.mutable._
import akka.testkit.TestActorRef
import akka.actor._
import akka.dispatch.Await
import akka.util.Duration
import almhirt.validation.AlmValidation._
import almhirt.almakka._
import scalaz._, Scalaz._

class ActorBasedMessageChannelSpecs extends Specification with AlmAkkaComponentForTesting {
  implicit def randUUID = java.util.UUID.randomUUID
  startAlmAkka
  implicit val alm = almAkkaContext.actorSystem
  private def getChannel(implicit context: AlmAkkaContext) = {
	ActorBasedMessageChannel("testChannel", (p: Props, n: String) => TestActorRef(p, n))
  }
	
	
  "An ActorBasedMessageStream" should {
	"return a subscription when subscribed to" in {
	  val channel = getChannel
	  val future = channel <* ({case _ => ()}, _ => true)
	  val subscription = future.result(Duration.Inf)
	  subscription must not beNull
	}
	
	"not execute a handler when the subscription is cancelled" in {
	  implicit def system = ActorSystem("test")
	  val channel = getChannel
	  var hit = false
	  val future = channel <* ({case _ => hit = true}, _ => true)
	  val subscription = future.result(Duration.Inf).forceResult
	  subscription.dispose()
	  channel.deliver(Message("a"))
	  !hit
	}
	"execute one handler when the subscription for one of two was cancelled" in {
	  implicit def system = ActorSystem("test")
	  val channel = getChannel
	  var hitCount = 0
	  val future1 = channel <* ({case _ => hitCount += 1}, _ => true)
	  val future2 = channel <* ({case _ => hitCount += 2}, _ => true)
	  val subscription1 = future1.result(Duration.Inf).forceResult
	  val subscription2 = future2.result(Duration.Inf).forceResult
	  subscription1.dispose()
	  channel.deliver(Message("a"))
	  hitCount must beEqualTo(2)
	}
	"execute a subscribed handler when classifier always returns true" in {
	  implicit def system = ActorSystem("test")
	  val channel = getChannel
	  var hit = false
	  val future = channel <* ({case _ => hit = true}, _ => true)
	  val subscription = future.result(Duration.Inf).forceResult
	  channel.deliver(Message("a"))
	  hit
	}
	"execute two handlers when classifier always returns true for 2 subscriptions" in {
	  implicit def system = ActorSystem("test")
	  val channel = getChannel
	  var hitCount = 0
	  val future1 = channel <* ({case _ => hitCount += 1}, _ => true)
	  val future2 = channel <* ({case _ => hitCount += 2}, _ => true)
	  val subscription1= future1.result(Duration.Inf).forceResult
	  val subscription2 = future2.result(Duration.Inf).forceResult
	  channel.deliver(Message("a"))
	  hitCount must beEqualTo(3)
	}
	"execute only the handler for which the classifier returns true" in {
	  implicit def system = ActorSystem("test")
	  val channel = getChannel
	  var hitCount = 0
	  val future1 = channel <* ({case _ => hitCount += 1}, _ => false)
	  val future2 = channel <* ({case _ => hitCount += 2}, _ => true)
	  val subscription1= future1.result(Duration.Inf).forceResult
	  val subscription2 = future2.result(Duration.Inf).forceResult
	  channel.deliver(Message("a"))
	  hitCount must beEqualTo(2)
	}
	"never execute a subscribed handler when classifier always returns false" in {
	  implicit def system = ActorSystem("test")
	  val channel = getChannel
	  var hit = false
	  val future = channel <* ({case _ => hit = true}, _ => false)
	  val subscription = future.result(Duration.Inf).forceResult
	  channel.deliver(Message("a"))
	  !hit
	}
	"execute a subscribed handler when classifier is met" in {
	  implicit def system = ActorSystem("test")
	  val channel = getChannel
	  var hit = false
	  val future = channel <* ({case _ => hit = true}, x => x.payload match {case "a" => true } )
	  val subscription = future.result(Duration.Inf).forceResult
	  channel.deliver(Message("a"))
	  hit
	}
	"not execute a subscribed handler when classifier is not met but the payload is of the same type" in {
	  implicit def system = ActorSystem("test")
	  val channel = getChannel
	  var hit = false
	  val future = channel <* ({case _ => hit = true}, x => x.payload match {case "a" => true; case _ => false } )
	  val subscription = future.result(Duration.Inf).forceResult
	  channel.deliver(Message("b"))
	  !hit
	}
	"not execute a subscribed handler when classifier is not met because the payload is of a different type" in {
	  implicit def system = ActorSystem("test")
	  val channel = getChannel
	  var hit = false
	  val future = channel <* ({case _ => hit = true}, x => x.payload match { case "1" => true; case _ => false } )
	  val subscription = future.result(Duration.Inf).forceResult
	  channel.deliver(Message("a"))
	  !hit
	}
  }
}

