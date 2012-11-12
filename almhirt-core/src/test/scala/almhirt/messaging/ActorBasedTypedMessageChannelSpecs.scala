package almhirt.messaging

import org.specs2.mutable._
import akka.util.Duration
import almhirt._
import almhirt.syntax.almvalidation._
import almhirt.almhirtsystem.AlmhirtsystemTestkit
import scalaz._, Scalaz._
import almhirt.AlmhirtSystem

class ActorBasedTypedMessageChannelSpecs extends Specification with AlmhirtsystemTestkit {
  private class A(val propa: Int)
  private class B(propa: Int, val propb: String) extends A(propa)
  
  
  implicit val atMost = akka.util.Duration(1, "s")
  implicit def getUUID = java.util.UUID.randomUUID()
  private def getChannel[T <: AnyRef](context: AlmhirtSystem)(implicit m: Manifest[T]): MessageChannel[T] = {
    MessageChannel[T]("testChannel")(context, m)
  }

  """A MessageChannel[A] where B <: A""" should {
    """accept both As and Bs as message payloads""" in {
      inTestSystem { ctx =>
        val channel = getChannel[A](ctx)
        channel.post(Message(new A(1)))
        channel.post(Message(new B(1, "B")))
        true
      }
    }
    """take a handler for A that will be triggered when an A is posted""" in {
      inTestSystem { ctx =>
        val channel = getChannel[A](ctx)
        var hitA = false
        val future = channel <-* (x => hitA = true)
        val subscription = future.awaitResult(Duration.Inf).forceResult
        channel.post(Message(new A(1)))
        subscription.dispose()
        hitA === true
      }
    }
    """take a handler for A that will be triggered when an B is posted""" in {
      inTestSystem { ctx =>
        val channel = getChannel[A](ctx)
        var hitA = false
        val future = channel <-* (x => hitA = true)
        val subscription = future.awaitResult(Duration.Inf).forceResult
        channel.post(Message(new B(1, "A")))
        subscription.dispose()
        hitA === true
      }
    }
  }
  
  """A MessageChannel[B] where B <: A""" should {
    """take a handler for B that will be triggered when a B is posted""" in {
      inTestSystem { ctx =>
        val channel = getChannel[B](ctx)
        var hitB = false
        val future = channel <-* (x => hitB = true)
        val subscription = future.awaitResult(Duration.Inf).forceResult
        channel.post(Message(new B(1, "A")))
        subscription.dispose()
        hitB === true
      }
    }
  }
  
  """A MessageChannel[A] with a subchannel of MessageChannel[B] where B <: A""" should {
    """trigger the handler on the parent for an A posted on the parent and not the handler on the subchannel""" in {
      inTestSystem { ctx =>
        val channel = getChannel[A](ctx)
        val subChannel = channel.createSubChannel[B]("sub").awaitResult(Duration.Inf).forceResult
        var hitA = false
        var hitB = false
        val subscriptionA = (channel <-* (x => hitA = true)).awaitResult(Duration.Inf).forceResult
        val subscriptionB = (subChannel <-* (x => hitB = true)).awaitResult(Duration.Inf).forceResult
        channel.post(Message(new A(1)))
        subscriptionA.dispose()
        subscriptionB.dispose()
        hitA === true && hitB === false
      }
    }
    """trigger only the handler on the subchannel for a B posted on the subchannel""" in {
      inTestSystem { ctx =>
        val channel = getChannel[A](ctx)
        val subChannel = channel.createSubChannel[B]("sub").awaitResult(Duration.Inf).forceResult
        var hitA = false
        var hitB = false
        val subscriptionA = (channel <-* (x => hitA = true)).awaitResult(Duration.Inf).forceResult
        val subscriptionB = (subChannel <-* (x => hitB = true)).awaitResult(Duration.Inf).forceResult
        subChannel.post(Message(new B(1, "B")))
        subscriptionA.dispose()
        subscriptionB.dispose()
        hitA === false && hitB === true
      }
    }
    """trigger the handler on the parent for an A posted on the parent""" in {
      inTestSystem { ctx =>
        val channel = getChannel[A](ctx)
        val subChannel = channel.createSubChannel[B]("sub").awaitResult(Duration.Inf).forceResult
        var hitA = false
        var hitB = false
        val subscriptionA = (channel <-* (x => hitA = true)).awaitResult(Duration.Inf).forceResult
        val subscriptionB = (subChannel <-* (x => hitB = true)).awaitResult(Duration.Inf).forceResult
        channel.post(Message(new A(1)))
        subscriptionA.dispose()
        subscriptionB.dispose()
        hitA === true
      }
    }
    """not trigger the handler on the subchannel for an A posted on the parent""" in {
      inTestSystem { ctx =>
        val channel = getChannel[A](ctx)
        val subChannel = channel.createSubChannel[B]("sub").awaitResult(Duration.Inf).forceResult
        var hitA = false
        var hitB = false
        val subscriptionA = (channel <-* (x => hitA = true)).awaitResult(Duration.Inf).forceResult
        val subscriptionB = (subChannel <-* (x => hitB = true)).awaitResult(Duration.Inf).forceResult
        channel.post(Message(new A(1)))
        subscriptionA.dispose()
        subscriptionB.dispose()
        hitB === false
      }
    }
    
    """not trigger the handler on the parent for an A posted on the parent when the classifier is not met""" in {
      inTestSystem { ctx =>
        val channel = getChannel[A](ctx)
        val subChannel = channel.createSubChannel[B]("sub").awaitResult(Duration.Inf).forceResult
        var hitA = false
        var hitB = false
        val subscriptionA = (channel <-* (x => hitA = true, x => x.payload.propa == 0)).awaitResult(Duration.Inf).forceResult
        val subscriptionB = (subChannel <-* (x => hitB = true, x => x.payload.propb == "")).awaitResult(Duration.Inf).forceResult
        channel.post(Message(new A(1)))
        subscriptionA.dispose()
        subscriptionB.dispose()
        hitA === false
      }
    }
    """trigger the handler on the parent for an A posted on the parent when the classifier is met""" in {
      inTestSystem { ctx =>
        val channel = getChannel[A](ctx)
        val subChannel = channel.createSubChannel[B]("sub").awaitResult(Duration.Inf).forceResult
        var hitA = false
        var hitB = false
        val subscriptionA = (channel <-* (x => hitA = true, x => x.payload.propa == 1)).awaitResult(Duration.Inf).forceResult
        val subscriptionB = (subChannel <-* (x => hitB = true, x => x.payload.propb == "")).awaitResult(Duration.Inf).forceResult
        channel.post(Message(new A(1)))
        subscriptionA.dispose()
        subscriptionB.dispose()
        hitA === true
      }
    }
    """not trigger the handler on the parent or the subchannelfor an B posted on the parent when the classifier is not met""" in {
      inTestSystem { ctx =>
        val channel = getChannel[A](ctx)
        val subChannel = channel.createSubChannel[B]("sub").awaitResult(Duration.Inf).forceResult
        var hitA = false
        var hitB = false
        val subscriptionA = (channel <-* (x => hitA = true, x => x.payload.propa == 0)).awaitResult(Duration.Inf).forceResult
        val subscriptionB = (subChannel <-* (x => hitB = true, x => x.payload.propb == "A")).awaitResult(Duration.Inf).forceResult
        channel.post(Message(new B(1, "B")))
        subscriptionA.dispose()
        subscriptionB.dispose()
        hitA === false && hitB === false
      }
    }
    """trigger the handler on the parent and the subchannel for an B posted on the parent when the classifier is met""" in {
      inTestSystem { ctx =>
        val channel = getChannel[A](ctx)
        val subChannel = channel.createSubChannel[B]("sub").awaitResult(Duration.Inf).forceResult
        var hitA = false
        var hitB = false
        val subscriptionA = (channel <-* (x => hitA = true, x => x.payload.propa == 1)).awaitResult(Duration.Inf).forceResult
        val subscriptionB = (subChannel <-* (x => hitB = true, x => x.payload.propb == "B")).awaitResult(Duration.Inf).forceResult
        channel.post(Message(new B(1, "B")))
        subscriptionA.dispose()
        subscriptionB.dispose()
        hitA === true && hitB === true
      }
    }
    """trigger the handler on the parent and not on the subchannel for an B posted on the parent when the classifier is met only in the parent""" in {
      inTestSystem { ctx =>
        val channel = getChannel[A](ctx)
        val subChannel = channel.createSubChannel[B]("sub").awaitResult(Duration.Inf).forceResult
        var hitA = false
        var hitB = false
        val subscriptionA = (channel <-* (x => hitA = true, x => x.payload.propa == 1)).awaitResult(Duration.Inf).forceResult
        val subscriptionB = (subChannel <-* (x => hitB = true, x => x.payload.propb == "A")).awaitResult(Duration.Inf).forceResult
        channel.post(Message(new B(1, "B")))
        subscriptionA.dispose()
        subscriptionB.dispose()
        hitA === true && hitB === false
      }
    }
    """not trigger the handler on the parent but on the subchannel for an B posted on the subchannel when the classifier is met only in the subchannel""" in {
      inTestSystem { ctx =>
        val channel = getChannel[A](ctx)
        val subChannel = channel.createSubChannel[B]("sub").awaitResult(Duration.Inf).forceResult
        var hitA = false
        var hitB = false
        val subscriptionA = (channel <-* (x => hitA = true, x => x.payload.propa == 0)).awaitResult(Duration.Inf).forceResult
        val subscriptionB = (subChannel <-* (x => hitB = true, x => x.payload.propb == "B")).awaitResult(Duration.Inf).forceResult
        channel.post(Message(new B(1, "B")))
        subscriptionA.dispose()
        subscriptionB.dispose()
        hitA === false && hitB === true
      }
    }
  }
  
}