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
    """take a handler for B that will be triggered when a B is posted""" in {
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
    """trigger the handler on the parent for an A posted on the parent and not the handler on the subchannel""" in {
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
    """trigger only the handler on the subchannel for a B posted on the subchannel""" in {
      inOwnContext { ctx =>
        val channel = getChannel[A](ctx)
        val subChannel = channel.createSubChannel[B].result(Duration.Inf).forceResult
        var hitA = false
        var hitB = false
        val subscriptionA = (channel <-* (x => hitA = true)).result(Duration.Inf).forceResult
        val subscriptionB = (subChannel <-* (x => hitB = true)).result(Duration.Inf).forceResult
        subChannel.post(Message(new B(1, "B")))
        subscriptionA.dispose()
        subscriptionB.dispose()
        hitA === false && hitB === true
      }
    }
    """trigger the handler on the parent for an A posted on the parent""" in {
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
        hitA === true
      }
    }
    """not trigger the handler on the subchannel for an A posted on the parent""" in {
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
        hitB === false
      }
    }
    
    """not trigger the handler on the parent for an A posted on the parent when the classifier is not met""" in {
      inOwnContext { ctx =>
        val channel = getChannel[A](ctx)
        val subChannel = channel.createSubChannel[B].result(Duration.Inf).forceResult
        var hitA = false
        var hitB = false
        val subscriptionA = (channel <-* (x => hitA = true, x => x.payload.propa == 0)).result(Duration.Inf).forceResult
        val subscriptionB = (subChannel <-* (x => hitB = true)).result(Duration.Inf).forceResult
        channel.post(Message(new A(1)))
        subscriptionA.dispose()
        subscriptionB.dispose()
        hitA === false
      }
    }
    """trigger the handler on the parent for an A posted on the parent when the classifier is met""" in {
      inOwnContext { ctx =>
        val channel = getChannel[A](ctx)
        val subChannel = channel.createSubChannel[B].result(Duration.Inf).forceResult
        var hitA = false
        var hitB = false
        val subscriptionA = (channel <-* (x => hitA = true, x => x.payload.propa == 1)).result(Duration.Inf).forceResult
        val subscriptionB = (subChannel <-* (x => hitB = true)).result(Duration.Inf).forceResult
        channel.post(Message(new A(1)))
        subscriptionA.dispose()
        subscriptionB.dispose()
        hitA === true
      }
    }
    """not trigger the handler on the parent or the subchannelfor an B posted on the parent when the classifier is not met""" in {
      inOwnContext { ctx =>
        val channel = getChannel[A](ctx)
        val subChannel = channel.createSubChannel[B].result(Duration.Inf).forceResult
        var hitA = false
        var hitB = false
        val subscriptionA = (channel <-* (x => hitA = true, x => x.payload.propa == 0)).result(Duration.Inf).forceResult
        val subscriptionB = (subChannel <-* (x => hitB = true, x => x.payload.propb == "A")).result(Duration.Inf).forceResult
        channel.post(Message(new B(1, "B")))
        subscriptionA.dispose()
        subscriptionB.dispose()
        hitA === false && hitB === false
      }
    }
    """trigger the handler on the parent and the subchannel for an B posted on the parent when the classifier is met""" in {
      inOwnContext { ctx =>
        val channel = getChannel[A](ctx)
        val subChannel = channel.createSubChannel[B].result(Duration.Inf).forceResult
        var hitA = false
        var hitB = false
        val subscriptionA = (channel <-* (x => hitA = true, x => x.payload.propa == 1)).result(Duration.Inf).forceResult
        val subscriptionB = (subChannel <-* (x => hitB = true, x => x.payload.propb == "B")).result(Duration.Inf).forceResult
        channel.post(Message(new B(1, "B")))
        subscriptionA.dispose()
        subscriptionB.dispose()
        hitA === true && hitB === true
      }
    }
    """trigger the handler on the parent and not on the subchannel for an B posted on the parent when the classifier is met only in the parent""" in {
      inOwnContext { ctx =>
        val channel = getChannel[A](ctx)
        val subChannel = channel.createSubChannel[B].result(Duration.Inf).forceResult
        var hitA = false
        var hitB = false
        val subscriptionA = (channel <-* (x => hitA = true, x => x.payload.propa == 1)).result(Duration.Inf).forceResult
        val subscriptionB = (subChannel <-* (x => hitB = true, x => x.payload.propb == "A")).result(Duration.Inf).forceResult
        channel.post(Message(new B(1, "B")))
        subscriptionA.dispose()
        subscriptionB.dispose()
        hitA === true && hitB === false
      }
    }
    """not trigger the handler on the parent but on the subchannel for an B posted on the subchannel when the classifier is met only in the subchannel""" in {
      inOwnContext { ctx =>
        val channel = getChannel[A](ctx)
        val subChannel = channel.createSubChannel[B].result(Duration.Inf).forceResult
        var hitA = false
        var hitB = false
        val subscriptionA = (channel <-* (x => hitA = true, x => x.payload.propa == 0)).result(Duration.Inf).forceResult
        val subscriptionB = (subChannel <-* (x => hitB = true, x => x.payload.propb == "B")).result(Duration.Inf).forceResult
        channel.post(Message(new B(1, "B")))
        subscriptionA.dispose()
        subscriptionB.dispose()
        hitA === false && hitB === true
      }
    }
  }
  
}