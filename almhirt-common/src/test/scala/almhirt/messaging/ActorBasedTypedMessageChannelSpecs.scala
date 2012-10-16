package almhirt.messaging

import org.specs2.mutable._
import akka.util.Duration
import almhirt.syntax.almvalidation._
import almhirt.almakka._
import scalaz._, Scalaz._

class ActorBasedTypedMessageChannelSpecs extends Specification with AlmAkkaContextTestKit {
  private class A(val propa: Int)
  private class B(propa: Int, val propb: String) extends A(propa)
  
  
  implicit def randUUID = java.util.UUID.randomUUID
  private def getChannel[T <: AnyRef](context: AlmAkkaContext)(implicit m: Manifest[T]): MessageChannel[T] = {
    impl.ActorBasedMessageChannel[T](Some("testChannel"), context)
  }

  """A MessageChannel[A] where B <: A""" should {
    """accept both As and Bs as message payloads""" in {
      inOwnContext { ctx =>
        val channel = getChannel[A](ctx)
        channel.post(Message(new A(1)))
        channel.post(Message(new B(1, "B")))
        true
      }
    }
    """take a handler for A that will be triggered when an A is posted""" in {
      inOwnContext { ctx =>
        val channel = getChannel[A](ctx)
        var hitA = false
        val future = channel <-* (x => hitA = true)
        val subscription = future.result(Duration.Inf).forceResult
        channel.post(Message(new A(1)))
        subscription.dispose()
        hitA === true
      }
    }
    """take a handler for A that will be triggered when an B is posted""" in {
      inOwnContext { ctx =>
        val channel = getChannel[A](ctx)
        var hitA = false
        val future = channel <-* (x => hitA = true)
        val subscription = future.result(Duration.Inf).forceResult
        channel.post(Message(new B(1, "A")))
        subscription.dispose()
        hitA === true
      }
    }
  }
  
  """A MessageChannel[B] where B <: A""" should {
    """take a handler for B that will be triggered when an B is posted""" in {
      inOwnContext { ctx =>
        val channel = getChannel[B](ctx)
        var hitB = false
        val future = channel <-* (x => hitB = true)
        val subscription = future.result(Duration.Inf).forceResult
        channel.post(Message(new B(1, "A")))
        subscription.dispose()
        hitB === true
      }
    }
  }
  
  """A MessageChannel[A] with a subchannel of MessageChannel[B] where B <: A""" should {
    """trigger only the handler on the parent for A posted on the parent""" in {
      inOwnContext { ctx =>
        val channel = getChannel[A](ctx)
        val subChannel = channel.createSubChannel[B].result(Duration.Inf).forceResult
        var hitA = false
        var hitB = false
        val subscriptionA = (channel <-* (x => hitA = true)).result(Duration.Inf).forceResult
        val subscriptionB = (subChannel <-* (x => hitB = true)).result(Duration.Inf).forceResult
        channel.post(Message(new A(1)))
        subscriptionA.dispose()
        subscriptionB.dispose()
        hitA === true && hitB === false
      }
    }
    """trigger handlers on both channels for B posted on the parent""" in {
      inOwnContext { ctx =>
        val channel = getChannel[A](ctx)
        val subChannel = channel.createSubChannel[B].result(Duration.Inf).forceResult
        var hitA = false
        var hitB = false
        val subscriptionA = (channel <-* (x => hitA = true)).result(Duration.Inf).forceResult
        val subscriptionB = (subChannel <-* (x => hitB = true)).result(Duration.Inf).forceResult
        channel.post(Message(new B(1, "A")))
        subscriptionA.dispose()
        subscriptionB.dispose()
        hitA === true && hitB === true
      }
    }
  }
  
}