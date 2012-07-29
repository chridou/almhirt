package almhirt.messaging

import org.specs2.mutable._
import akka.testkit.TestActorRef
import akka.actor._
import akka.dispatch.Await
import akka.util.Duration
import scalaz.{Success}

class ActorBasedMessageStreamSpecs extends Specification {
	
  implicit def randUUID = java.util.UUID.randomUUID
  
  private def getChannel(implicit system: ActorSystem) = {
	ActorBasedMessageStream("testChannel", (p: Props, n: String) => TestActorRef(p, n)(system))
  }
	
	
  "An ActorBasedMessageStream" should {
	"return a subscription when subscribed to" in {
	  implicit def system = ActorSystem("test")
	  val channel = getChannel
	  val future = channel +?= ({case _ => ()}, _ => true)
	  val subscription = Await.result(future.underlying, Duration.Inf) match { case Success(s) => s }
	  system.shutdown()
	  subscription must not beNull
	}
	"not execute a handler when the subscription is cancelled" in {
	  implicit def system = ActorSystem("test")
	  val channel = getChannel
	  var hit = false
	  val future = channel +?= ({case _ => hit = true}, _ => true)
	  val subscription = Await.result(future.underlying, Duration.Inf) match { case Success(s) => s }
	  subscription.dispose()
	  channel.publish(Message("a"))
	  system.shutdown()
	  !hit
	}
	"execute one handler when the subscription for one of two was cancelled" in {
	  implicit def system = ActorSystem("test")
	  val channel = getChannel
	  var hitCount = 0
	  val future1 = channel +?= ({case _ => hitCount += 1}, _ => true)
	  val future2 = channel +?= ({case _ => hitCount += 2}, _ => true)
	  val subscription1 = Await.result(future1.underlying, Duration.Inf) match { case Success(s) => s }
	  val subscription2 = Await.result(future2.underlying, Duration.Inf) match { case Success(s) => s }
	  subscription1.dispose()
	  channel.publish(Message("a"))
	  system.shutdown()
	  hitCount must beEqualTo(2)
	}
	"execute a subscribed handler when classifier always returns true" in {
	  implicit def system = ActorSystem("test")
	  val channel = getChannel
	  var hit = false
	  val future = channel +?= ({case _ => hit = true}, _ => true)
	  val subscription = Await.result(future.underlying, Duration.Inf) match { case Success(s) => s }
	  channel.publish(Message("a"))
	  system.shutdown()
	  hit
	}
	"execute two handlers when classifier always returns true for 2 subscriptions" in {
	  implicit def system = ActorSystem("test")
	  val channel = getChannel
	  var hitCount = 0
	  val future1 = channel +?= ({case _ => hitCount += 1}, _ => true)
	  val future2 = channel +?= ({case _ => hitCount += 2}, _ => true)
	  val subscription1= Await.result(future1.underlying, Duration.Inf) match { case Success(s) => s }
	  val subscription2 = Await.result(future2.underlying, Duration.Inf) match { case Success(s) => s }
	  channel.publish(Message("a"))
	  system.shutdown()
	  hitCount must beEqualTo(3)
	}
	"execute only the handler for which the classifier returns true" in {
	  implicit def system = ActorSystem("test")
	  val channel = getChannel
	  var hitCount = 0
	  val future1 = channel +?= ({case _ => hitCount += 1}, _ => false)
	  val future2 = channel +?= ({case _ => hitCount += 2}, _ => true)
	  val subscription1= Await.result(future1.underlying, Duration.Inf) match { case Success(s) => s }
	  val subscription2 = Await.result(future2.underlying, Duration.Inf) match { case Success(s) => s }
	  channel.publish(Message("a"))
	  system.shutdown()
	  hitCount must beEqualTo(2)
	}
	"never execute a subscribed handler when classifier always returns false" in {
	  implicit def system = ActorSystem("test")
	  val channel = getChannel
	  var hit = false
	  val future = channel +?= ({case _ => hit = true}, _ => false)
	  val subscription = Await.result(future.underlying, Duration.Inf) match { case Success(s) => s }
	  channel.publish(Message("a"))
	  system.shutdown()
	  !hit
	}
	"execute a subscribed handler when classifier is met" in {
	  implicit def system = ActorSystem("test")
	  val channel = getChannel
	  var hit = false
	  val future = channel +?= ({case _ => hit = true}, x => x.payload match {case "a" => true } )
	  val subscription = Await.result(future.underlying, Duration.Inf) match { case Success(s) => s }
	  channel.publish(Message("a"))
	  system.shutdown()
	  hit
	}
	"not execute a subscribed handler when classifier is not met but the payload is of the same type" in {
	  implicit def system = ActorSystem("test")
	  val channel = getChannel
	  var hit = false
	  val future = channel +?= ({case _ => hit = true}, x => x.payload match {case "a" => true; case _ => false } )
	  val subscription = Await.result(future.underlying, Duration.Inf) match { case Success(s) => s }
	  channel.publish(Message("b"))
	  system.shutdown()
	  !hit
	}
	"not execute a subscribed handler when classifier is not met because the payload is of a different type" in {
	  implicit def system = ActorSystem("test")
	  val channel = getChannel
	  var hit = false
	  val future = channel +?= ({case _ => hit = true}, x => x.payload match { case "1" => true; case _ => false } )
	  val subscription = Await.result(future.underlying, Duration.Inf) match { case Success(s) => s }
	  channel.publish(Message("a"))
	  system.shutdown()
	  !hit
	}
  }
}

