package almhirt.messaging

import org.specs2.mutable._
import akka.testkit.TestActorRef
import akka.actor._
import akka.dispatch.Await
import akka.util.Duration
import scalaz.Success

class ChannelMessageSpecs extends Specification {
	
  implicit def randUUID = java.util.UUID.randomUUID
  
  private def getChannel(implicit system: ActorSystem) = {
	Channel("testChannel", (p: Props, n: String) => TestActorRef(p, n)(system))
  }
		
  "A subscription for a payload of type String" should {
	"be triggered by a message with a payload of type String" in {
	  implicit def system = ActorSystem("test")
	  val channel = getChannel
	  var triggered = false
	  val future = channel.subscribe[String]((m: Message[String]) => triggered = true)
	  val subscription = Await.result(future, Duration.Inf) match { case Success(s) => s }
	  channel.publish(Message[String](""))
	  subscription.unsubscribe()
	  system.shutdown()
	  triggered
	}
	"be triggered by a message with a payload 'a' when 'a' is published" in {
	  implicit def system = ActorSystem("test")
	  val channel = getChannel
	  var triggered = false
	  val future = channel.subscribe[String]((m: Message[String]) => triggered = true, (m: Message[String]) => m.payload == "a")
	  val subscription = Await.result(future, Duration.Inf) match { case Success(s) => s }
	  channel.publish(Message[String]("a"))
	  subscription.unsubscribe()
	  system.shutdown()
	  triggered
	}
	"not be triggered by a message with a payload 'a' when 'b' is published " in {
	  implicit def system = ActorSystem("test")
	  val channel = getChannel
	  var triggered = false
	  val future = channel.subscribe[String]((m: Message[String]) => triggered = true, (m: Message[String]) => m.payload == "a")
	  val subscription = Await.result(future, Duration.Inf) match { case Success(s) => s }
	  channel.publish(Message("b"))
	  subscription.unsubscribe()
	  system.shutdown()
	  !triggered
	}
  }

  private class A(val x: Int)
  private class B(x: Int) extends A(x)
  
  "When B is an A, A" should {
  	"be assignable from A" in {
	   classOf[A].isAssignableFrom(classOf[A])
  	}
  	"be assignable from B" in {
	   classOf[A].isAssignableFrom(classOf[B])
  	}
  }

  "When B is an A, B" should {
  	"not be assignable from A" in {
	   !classOf[B].isAssignableFrom(classOf[A])
  	}
  	"be assignable from B" in {
	   classOf[B].isAssignableFrom(classOf[B])
  	}
  }

  "When B is an A: A subscription for a payload of type A" should {
	"be triggered by a message with a payload of type A" in {
	  implicit def system = ActorSystem("test")
	  val channel = getChannel
	  var triggered = false
	  val future = channel.subscribe[A]((m: Message[A]) => triggered = true)
	  val subscription = Await.result(future, Duration.Inf) match { case Success(s) => s }
	  channel.publish(Message(new A(1)))
	  subscription.unsubscribe()
	  system.shutdown()
	  triggered
	}

	"be triggered by a message with a payload of type B" in {
	  implicit def system = ActorSystem("test")
	  val channel = getChannel
	  var triggered = false
	  val future = channel.subscribe[A]((m: Message[A]) => triggered = true)
	  val subscription = Await.result(future, Duration.Inf) match { case Success(s) => s }
	  channel.publish(Message(new B(1)))
	  subscription.unsubscribe()
	  system.shutdown()
	  triggered
	}
  }
  
  "When B is an A: A subscription for a payload of type B" should {
	"not be triggered by a message with a payload of type A" in {
	  implicit def system = ActorSystem("test")
	  val channel = getChannel
	  var triggered = false
	  val future = channel.subscribe[B]((m: Message[B]) => triggered = true)
	  val subscription = Await.result(future, Duration.Inf) match { case Success(s) => s }
	  channel.publish(Message(new A(1)))
	  subscription.unsubscribe()
	  system.shutdown()
	  !triggered
	}

	"be triggered by a message with a payload of type B" in {
	  implicit def system = ActorSystem("test")
	  val channel = getChannel
	  var triggered = false
	  val future = channel.subscribe[B]((m: Message[B]) => triggered = true)
	  val subscription = Await.result(future, Duration.Inf) match { case Success(s) => s }
	  channel.publish(Message(new B(1)))
	  subscription.unsubscribe()
	  system.shutdown()
	  triggered
	}
  }

}
