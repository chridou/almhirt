package almhirt.messaging

import org.specs2.mutable._
import akka.util.Duration
import almhirt.syntax.almvalidation._
import almhirt.almakka._
import scalaz._, Scalaz._

class ActorBasedMessageHubSpecs extends Specification with AlmAkkaContextTestKit {
  private class A(val propa: Int)
  private class B(propa: Int, val propb: String) extends A(propa)
  
  implicit def randUUID = java.util.UUID.randomUUID
  private def getHub(context: AlmAkkaContext): MessageHub = {
    impl.ActorBasedMessageHub(Some("testHub"), context)
  }

  """A MessageHub""" should {
    """accept a message""" in {
      inOwnContext { ctx =>
        val hub = getHub(ctx)
        hub.broadcast(Message(new B(1, "B")))
        true
      }
    }
    """be able to create a global channel""" in {
      inOwnContext { ctx =>
        val hub = getHub(ctx)
        val channel = hub.createGlobalMessageChannel[AnyRef]
        true
      }
    }
    """be able to create a channel""" in {
      inOwnContext { ctx =>
        val hub = getHub(ctx)
        val channel = hub.createMessageChannel[AnyRef](None)
        true
      }
    }
  }

  """A MessageHub with a created global channel of payload type AnyRef""" should {
    """trigger a handler on the created channel""" in {
      inOwnContext { ctx =>
        val hub = getHub(ctx)
        val channel = (hub.createGlobalMessageChannel[AnyRef]).result(Duration.Inf).forceResult
        var hit = false
        val subscription = (channel <-* (x => hit = true)).result(Duration.Inf).forceResult
        hub.broadcast(Message(new A(1)))
        subscription.dispose()
        hit === true
      }
    }
  }
  """A MessageHub with a created global channel of payload type String""" should {
    """trigger a handler on the created channel when a String is broadcasted""" in {
      inOwnContext { ctx =>
        val hub = getHub(ctx)
        val channel = hub.createGlobalMessageChannel[String].result(Duration.Inf).forceResult
        var hit = false
        val subscription = (channel <-* (x => hit = true, x => x.payload.length == 1)).result(Duration.Inf).forceResult
        hub.broadcast(Message("A"))
        subscription.dispose()
        hit === true
      }
    }
    """not be trigger a handler on the created channel when a UUID is broadcasted""" in {
      inOwnContext { ctx =>
        val hub = getHub(ctx)
        val channel = hub.createGlobalMessageChannel[String].result(Duration.Inf).forceResult
        var hit = false
        val subscription = (channel <-* (x => hit = true, x => x.payload.length == 1)).result(Duration.Inf).forceResult
        hub.broadcast(Message(java.util.UUID.randomUUID))
        subscription.dispose()
        hit === false
      }
    }
  }

  
  """A MessageHub with a created channel with no topic of payload type AnyRef""" should {
    """trigger a handler on the created channel""" in {
      inOwnContext { ctx =>
        val hub = getHub(ctx)
        val channel = hub.createMessageChannel[AnyRef](None).result(Duration.Inf).forceResult
        var hit = false
        val subscription = (channel <-* (x => hit = true)).result(Duration.Inf).forceResult
        hub.broadcast(Message(new A(1)))
        subscription.dispose()
        hit === true
      }
    }
  }
  """A MessageHub with a created channel with no topic of payload type String""" should {
    """trigger a handler on the created channel when a String is broadcasted""" in {
      inOwnContext { ctx =>
        val hub = getHub(ctx)
        val channel = hub.createMessageChannel[String](None).result(Duration.Inf).forceResult
        var hit = false
        val subscription = (channel <-* (x => hit = true, x => x.payload.length == 1)).result(Duration.Inf).forceResult
        hub.broadcast(Message("A"))
        subscription.dispose()
        hit === true
      }
    }
    """not be trigger a handler on the created channel when a UUID is broadcasted""" in {
      inOwnContext { ctx =>
        val hub = getHub(ctx)
        val channel = hub.createMessageChannel[String](None).result(Duration.Inf).forceResult
        var hit = false
        val subscription = (channel <-* (x => hit = true, x => x.payload.length == 1)).result(Duration.Inf).forceResult
        hub.broadcast(Message(java.util.UUID.randomUUID))
        subscription.dispose()
        hit === false
      }
    }
  }
  
  
}